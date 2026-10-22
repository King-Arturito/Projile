package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.support.AuthIntegrationSupport;
import io.github.kingarturito.projile.user.UserId;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthHttpIT extends AuthIntegrationSupport {
    private Cookie csrf(MockHttpSession session) throws Exception {
        return mvc.perform(get("/api/auth/session").session(session).secure(true)).andExpect(status().isOk()).andReturn().getResponse().getCookie("XSRF-TOKEN");
    }
    private MockHttpServletRequestBuilder mutation(MockHttpServletRequestBuilder request, MockHttpSession session) throws Exception {
        Cookie cookie = csrf(session);
        return request.session(session).secure(true).cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue())
                .with(req -> { req.setRemoteAddr(remoteAddress); return req; });
    }
    private MockHttpSession login(String identifier) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(mutation(post("/api/auth/login").param("username",identifier).param("password",PASSWORD),session))
                .andExpect(status().isNoContent());
        return session;
    }
    private String randomToken() { return UUID.randomUUID().toString().replace("-", "") + "a".repeat(11); }

    @Test void anonymousBootstrapReturnsNoIdentityAndAReadableSecureCsrfCookie() throws Exception {
        var result = mvc.perform(get("/api/auth/session").secure(true)).andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false)).andExpect(jsonPath("$.user").isEmpty())
                .andExpect(jsonPath("$.googleEnabled").value(false))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().exists("Content-Security-Policy")).andReturn();
        Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull(); assertThat(cookie.isHttpOnly()).isFalse(); assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax");
        assertThat(result.getRequest().getSession(false)).isNull();
    }
    @ParameterizedTest @ValueSource(strings={"register","verification","verify","login","logout","profile"})
    void everyMutationRejectsMissingAndIncorrectCsrf(String route) throws Exception {
        var path = "/api/auth/"+route;
        for (boolean incorrect : List.of(false,true)) {
            var request = route.equals("profile") ? patch(path) : post(path);
            request.contentType("application/json").content(body(Map.of("email","csrf@example.com","token",randomToken(),"password",PASSWORD)));
            if (incorrect) request.cookie(new Cookie("XSRF-TOKEN","expected")).header("X-XSRF-TOKEN","wrong");
            mvc.perform(request).andExpect(status().isForbidden()).andExpect(header().exists("Content-Security-Policy"));
        }
    }
    @Test void publicRegistrationUsesActualCookieHeaderAndDoesNotSignTheCallerIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        for (int i=0;i<2;i++) mvc.perform(mutation(post("/api/auth/register").contentType("application/json")
                .content(body(Map.of("email","PUBLIC@example.com"))),session)).andExpect(status().isAccepted()).andExpect(content().string(""));
        assertThat(repository.count()).isEqualTo(1);
        mvc.perform(get("/api/auth/session").session(session)).andExpect(jsonPath("$.authenticated").value(false));
        mvc.perform(get("/api/users").param("username","someone")).andExpect(status().isUnauthorized()).andExpect(header().doesNotExist("Location"));
    }
    @Test void verificationRotatesSessionAndCsrfAndOnlyProfileCompletionGrantsApplicationAccess() throws Exception {
        pending("flow@example.com"); String token=emailedToken();
        MockHttpSession session = new MockHttpSession(); String oldId=session.getId(); Cookie oldCsrf=csrf(session);
        mvc.perform(post("/api/auth/verify").session(session).cookie(oldCsrf).header("X-XSRF-TOKEN",oldCsrf.getValue())
                .contentType("application/json").content(body(Map.of("token",token,"password",PASSWORD))))
                .andExpect(status().isNoContent());
        assertThat(session.getId()).isNotEqualTo(oldId);
        Cookie fresh=csrf(session); assertThat(fresh.getValue()).isNotEqualTo(oldCsrf.getValue());
        mvc.perform(get("/api/users").param("username","someone").session(session)).andExpect(status().isForbidden());
        mvc.perform(mutation(patch("/api/auth/profile").contentType("application/json")
                .content(body(Map.of("displayName","Too Early"))),session)).andExpect(status().isBadRequest());
        mvc.perform(mutation(patch("/api/auth/profile").contentType("application/json")
                .content(body(Map.of("username","flow_person"))),session)).andExpect(status().isOk());
        mvc.perform(mutation(patch("/api/auth/profile").contentType("application/json")
                .content(body(Map.of("displayName","Flow Person"))),session)).andExpect(status().isOk());
        SecurityContext context=(SecurityContext)session.getAttribute("SPRING_SECURITY_CONTEXT");
        assertThat(context.getAuthentication().getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        mvc.perform(get("/api/users").param("username","FLOW_PERSON").session(session)).andExpect(status().isOk());
        var response=mvc.perform(get("/api/auth/session").session(session)).andExpect(jsonPath("$.user.profileComplete").value(true)).andReturn();
        assertThat(response.getResponse().getContentAsString()).doesNotContain(PASSWORD,"hashedPassword","$argon2",token);
        mvc.perform(mutation(post("/api/auth/verify").contentType("application/json")
                .content(body(Map.of("token",token,"password",PASSWORD))),session)).andExpect(status().isBadRequest());
    }
    @ParameterizedTest @ValueSource(strings={"case_user","CASE_USER","case@example.com","CASE@EXAMPLE.COM"})
    void passwordLoginAcceptsUsernameOrEmailCaseInsensitively(String identifier) throws Exception {
        complete("case@example.com","case_user");
        var session=login(identifier);
        mvc.perform(get("/api/auth/session").session(session)).andExpect(jsonPath("$.authenticated").value(true));
    }
    @Test void emailLoginCanResumeVerifiedAccountBeforeUsernameChoice() throws Exception {
        verified("unfinished@example.com");
        var session=login("unfinished@example.com");
        mvc.perform(get("/api/auth/session").session(session)).andExpect(jsonPath("$.user.username").isEmpty()).andExpect(jsonPath("$.user.profileComplete").value(false));
    }
    @ParameterizedTest @ValueSource(strings={"unknown","wrong-password","unverified","oidc-only"})
    void loginFailuresHaveTheSameGenericResponse(String kind) throws Exception {
        String identifier="unknown@example.com";
        if(kind.equals("wrong-password")) { complete("known@example.com","known_user"); identifier="known_user"; }
        if(kind.equals("unverified")) { credentialBearingUnverified("pending@example.com"); identifier="pending@example.com"; }
        if(kind.equals("oidc-only")) { registration.registerOidc("google@example.com",ISSUER,"sub"); identifier="google@example.com"; }
        mvc.perform(mutation(post("/api/auth/login").param("username",identifier)
                .param("password",kind.equals("wrong-password") ? "wrong password" : PASSWORD),new MockHttpSession()))
                .andExpect(status().isUnauthorized()).andExpect(content().json("{\"message\":\"Invalid username or password.\"}"))
                .andExpect(header().doesNotExist("Location"));
    }
    @Test void loginRotatesExistingSessionAndLogoutInvalidatesIt() throws Exception {
        complete("logout@example.com","logout_user");
        MockHttpSession session=new MockHttpSession(); String oldId=session.getId(); Cookie oldCsrf=csrf(session);
        mvc.perform(post("/api/auth/login").session(session).cookie(oldCsrf).header("X-XSRF-TOKEN",oldCsrf.getValue())
                .param("username","logout_user").param("password",PASSWORD)).andExpect(status().isNoContent());
        assertThat(session.getId()).isNotEqualTo(oldId);
        assertThat(csrf(session).getValue()).isNotEqualTo(oldCsrf.getValue());
        var response=mvc.perform(mutation(post("/api/auth/logout"),session)).andExpect(status().isNoContent()).andReturn().getResponse();
        assertThat(session.isInvalid()).isTrue();
        assertThat(response.getCookie("JSESSIONID").getMaxAge()).isZero();
        mvc.perform(get("/api/users").param("username","logout_user").cookie(new Cookie("JSESSIONID",session.getId())))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/session")).andExpect(jsonPath("$.authenticated").value(false)).andExpect(cookie().exists("XSRF-TOKEN"));
    }
    @Test void profileCannotModifyAnotherIdentityAndRequiresExactlyOneField() throws Exception {
        UserId mine=verified("mine@example.com"), other=verified("other@example.com");
        var session=login("mine@example.com");
        mvc.perform(mutation(patch("/api/auth/profile").contentType("application/json").content("{}"),session)).andExpect(status().isBadRequest());
        mvc.perform(mutation(patch("/api/auth/profile").contentType("application/json")
                .content(body(Map.of("username","mine_user","displayName","Mine"))),session)).andExpect(status().isBadRequest());
        mvc.perform(mutation(patch("/api/auth/profile").contentType("application/json")
                .content(body(Map.of("username","mine_user","userId",other.userId()))),session)).andExpect(status().isOk());
        assertThat(users.findById(mine.userId()).username()).isEqualTo("mine_user");
        assertThat(users.findById(other.userId()).username()).isNull();
    }
    @Test void preflightsAllowOnlyTrustedOriginsAndTheCsrfHeader() throws Exception {
        mvc.perform(options("/api/auth/profile").header("Origin","http://localhost:4200")
                .header("Access-Control-Request-Method","PATCH").header("Access-Control-Request-Headers","X-XSRF-TOKEN,Content-Type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials","true"));
        mvc.perform(options("/api/auth/profile").header("Origin","https://untrusted.example")
                .header("Access-Control-Request-Method","PATCH")).andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
    @Test void invalidBodiesAreSanitizedAndDoNotEchoSecrets() throws Exception {
        var result=mvc.perform(mutation(post("/api/auth/verify").contentType("application/json")
                .content(body(Map.of("token","private-invalid-token","password",PASSWORD))),new MockHttpSession()))
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain(PASSWORD,"private-invalid-token","trace");
    }
    @Test void verificationAndRegistrationLimitsUseRemoteAddressNotSpoofedForwardingHeaders() throws Exception {
        MockHttpSession session=new MockHttpSession(); String token=randomToken();
        for(int i=0;i<6;i++) {
            var result=mvc.perform(mutation(post("/api/auth/verify").contentType("application/json")
                    .header("X-Forwarded-For","forged-"+i).content(body(Map.of("token",token,"password",PASSWORD))),session)).andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(i<5 ? 400 : 429);
        }
        for(int i=0;i<6;i++) {
            var result=mvc.perform(mutation(post("/api/auth/verification").contentType("application/json")
                    .content(body(Map.of("email","unknown-limit@example.com"))),session)).andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(i<5 ? 202 : 429);
        }
    }
    @Test void independentIpLimitsCannotBeBypassedWithForwardedHeadersOrChangingIdentities() throws Exception {
        MockHttpSession session=new MockHttpSession();
        for(int i=0;i<21;i++) {
            var request=mutation(post("/api/auth/verify").contentType("application/json")
                    .header("X-Forwarded-For","203.0.113."+(i+1))
                    .content(body(Map.of("token",randomToken(),"password",PASSWORD))),session)
                    .with(req->{req.setRemoteAddr("192.0.2.10");return req;});
            mvc.perform(request).andExpect(i<20 ? status().isBadRequest() : status().isTooManyRequests());
        }
        for(int i=0;i<31;i++) {
            var request=mutation(post("/api/auth/verification").contentType("application/json")
                    .header("X-Forwarded-For","203.0.113."+(i+1))
                    .content(body(Map.of("email","ip-limit-"+i+"@example.com"))),session)
                    .with(req->{req.setRemoteAddr("192.0.2.11");return req;});
            mvc.perform(request).andExpect(i<30 ? status().isAccepted() : status().isTooManyRequests());
        }
    }
}
