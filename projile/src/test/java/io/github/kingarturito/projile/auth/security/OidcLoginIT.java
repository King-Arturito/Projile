package io.github.kingarturito.projile.auth.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.*;
import com.sun.net.httpserver.HttpServer;
import io.github.kingarturito.projile.support.AuthIntegrationSupport;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real Spring authorization-code exchange and signed-ID-token decoder, against a local provider. */
@Import(OidcLoginIT.ProviderConfiguration.class)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class OidcLoginIT extends AuthIntegrationSupport {
    private static final String CLIENT="test-oidc-client";
    private static final Provider PROVIDER=new Provider();
    record Attempt(MockHttpSession session,String state,String nonce) { }

    @TestConfiguration(proxyBeanMethods=false)
    static class ProviderConfiguration {
        @Bean ClientRegistrationRepository clients() {
            return new InMemoryClientRegistrationRepository(ClientRegistration.withRegistrationId("google")
                    .clientId(CLIENT).clientSecret("local-test-secret").authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .scope("openid","email","profile").redirectUri("http://localhost:4200/login/oauth2/code/google")
                    .issuerUri(ISSUER).authorizationUri(PROVIDER.url("/authorize")).tokenUri(PROVIDER.url("/token"))
                    .jwkSetUri(PROVIDER.url("/jwks")).build());
        }
    }
    @AfterAll static void stopProvider() { PROVIDER.close(); }
    private Attempt start() throws Exception {
        var result=mvc.perform(get("/oauth2/authorization/google")).andExpect(status().is3xxRedirection()).andReturn();
        String location=result.getResponse().getRedirectedUrl();
        assertThat(location).startsWith(PROVIDER.url("/authorize"));
        Map<String,String> params=query(URI.create(location).getRawQuery());
        assertThat(params.get("response_type")).isEqualTo("code");
        assertThat(params.get("client_id")).isEqualTo(CLIENT);
        assertThat(params.get("redirect_uri")).isEqualTo("http://localhost:4200/login/oauth2/code/google");
        assertThat(params.get("state")).isNotBlank(); assertThat(params.get("nonce")).isNotBlank();
        return new Attempt((MockHttpSession)result.getRequest().getSession(false),params.get("state"),params.get("nonce"));
    }
    private MvcResult callback(Attempt attempt,String variant) throws Exception {
        String code=UUID.randomUUID().toString();
        PROVIDER.codes.put(code,new Claims(attempt.nonce(),variant));
        return mvc.perform(get("/login/oauth2/code/google").session(attempt.session())
                .param("code",code).param("state",attempt.state())).andExpect(status().is3xxRedirection()).andReturn();
    }
    @Test void enabledProviderReportsAvailabilityAndNewIdentityReceivesOnlyOnboardingAuthority() throws Exception {
        mvc.perform(get("/api/auth/session")).andExpect(jsonPath("$.googleEnabled").value(true));
        Attempt attempt=start();
        assertThat(callback(attempt,"valid").getResponse().getRedirectedUrl()).isEqualTo("http://localhost:4200/onboarding/username");
        assertThat(repository.count()).isEqualTo(1);
        var result=mvc.perform(get("/api/auth/session").session(attempt.session()))
                .andExpect(jsonPath("$.authenticated").value(true)).andExpect(jsonPath("$.user.emailVerified").value(true))
                .andExpect(jsonPath("$.user.profileComplete").value(false)).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("provider-access-token","id_token","eyJ","nonce");
        mvc.perform(get("/api/users").param("username","someone").session(attempt.session())).andExpect(status().isForbidden());
    }
    @ParameterizedTest @ValueSource(strings={"signature","issuer","audience","expired","nonce","unverified","missing-email"})
    void rejectsInvalidProtocolTokensAndUntrustedClaimsWithoutCreatingAccounts(String variant) throws Exception {
        Attempt attempt=start();
        assertThat(callback(attempt,variant).getResponse().getRedirectedUrl()).isEqualTo("http://localhost:4200/login?error=oauth");
        assertThat(repository.count()).isZero();
        mvc.perform(get("/api/auth/session").session(attempt.session())).andExpect(jsonPath("$.authenticated").value(false));
    }
    @Test void rejectsForgedStateAndProviderCancellationUsingTheSameFailureRoute() throws Exception {
        Attempt attempt=start();
        mvc.perform(get("/login/oauth2/code/google").session(attempt.session()).param("state","forged-state").param("code","unused"))
                .andExpect(redirectedUrl("http://localhost:4200/login?error=oauth"));
        Attempt cancelled=start();
        mvc.perform(get("/login/oauth2/code/google").session(cancelled.session()).param("state",cancelled.state()).param("error","access_denied"))
                .andExpect(redirectedUrl("http://localhost:4200/login?error=oauth"));
        assertThat(repository.count()).isZero();
    }
    @Test void returnsToRemainingProfileStepAndThenWelcomeForReturningIdentity() throws Exception {
        var id=registration.registerOidc("person@example.com",ISSUER,"local-subject");
        users.chooseUsername(id.userId(),"google_person");
        assertThat(callback(start(),"valid").getResponse().getRedirectedUrl()).isEqualTo("http://localhost:4200/onboarding/display-name");
        users.chooseDisplayName(id.userId(),"Google Person");
        assertThat(callback(start(),"valid").getResponse().getRedirectedUrl()).isEqualTo("http://localhost:4200/welcome");
        assertThat(repository.count()).isEqualTo(1);
    }
    @Test void emailCollisionCannotAttachGoogleToAVerifiedPasswordAccount() throws Exception {
        complete("person@example.com","password_person");
        assertThat(callback(start(),"valid").getResponse().getRedirectedUrl()).isEqualTo("http://localhost:4200/login?error=oauth");
        var user=repository.findByUserEmail("person@example.com").orElseThrow();
        assertThat(user.getOidcSubject()).isNull(); assertThat(user.getPassword()).isNotNull();
    }
    private static Map<String,String> query(String raw) {
        Map<String,String> params=new HashMap<>();
        for(String item:raw.split("&")) { String[] pair=item.split("=",2); params.put(URLDecoder.decode(pair[0],StandardCharsets.UTF_8),URLDecoder.decode(pair.length==2?pair[1]:"",StandardCharsets.UTF_8)); }
        return params;
    }
    record Claims(String nonce,String variant) { }
    static class Provider implements AutoCloseable {
        final HttpServer server;
        final ExecutorService executor;
        final RSAKey trusted,untrusted;
        final Map<String,Claims> codes=new ConcurrentHashMap<>();
        Provider() {
            try {
                trusted=new RSAKeyGenerator(2048).keyID("provider-key").generate();
                untrusted=new RSAKeyGenerator(2048).keyID("provider-key").generate();
                server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
                executor=Executors.newCachedThreadPool(r->{Thread thread=new Thread(r,"local-oidc-provider");thread.setDaemon(true);return thread;});
                server.setExecutor(executor);
                server.createContext("/jwks", exchange->{
                    byte[] content=new JWKSet(trusted.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type","application/json"); exchange.sendResponseHeaders(200,content.length);
                    try(var out=exchange.getResponseBody()){out.write(content);}
                });
                server.createContext("/token", exchange->{
                    try {
                        Map<String,String> form=query(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
                        Claims claims=codes.remove(form.get("code"));
                        if(claims==null) {exchange.sendResponseHeaders(400,-1);exchange.close();return;}
                        String jwt=token(claims);
                        byte[] content=("{\"access_token\":\"provider-access-token\",\"token_type\":\"Bearer\",\"expires_in\":300,\"scope\":\"openid email profile\",\"id_token\":\""+jwt+"\"}").getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,content.length);
                        try(var out=exchange.getResponseBody()){out.write(content);}
                    } catch(Exception failure) {exchange.sendResponseHeaders(500,-1);exchange.close();}
                });
                server.start();
            } catch(Exception failure){throw new IllegalStateException("Cannot start the local OIDC test provider",failure);}
        }
        String url(String path){return "http://127.0.0.1:"+server.getAddress().getPort()+path;}
        String token(Claims data) throws Exception {
            String kind=data.variant(); Instant now=Instant.now();
            var claims=new JWTClaimsSet.Builder().issuer(kind.equals("issuer")?"https://untrusted.example":ISSUER)
                    .subject("local-subject").audience(kind.equals("audience")?"another-client":CLIENT)
                    .issueTime(Date.from(now.minusSeconds(kind.equals("expired")?600:5)))
                    .expirationTime(Date.from(now.plusSeconds(kind.equals("expired")?-120:300)))
                    .claim("nonce",kind.equals("nonce")?"replayed-nonce":data.nonce())
                    .claim("email_verified",!kind.equals("unverified"));
            if(!kind.equals("missing-email")) claims.claim("email","person@example.com");
            SignedJWT jwt=new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("provider-key").type(JOSEObjectType.JWT).build(),claims.build());
            jwt.sign(new RSASSASigner(kind.equals("signature")?untrusted:trusted));return jwt.serialize();
        }
        public void close(){server.stop(0);executor.shutdownNow();}
    }
}
