package io.github.kingarturito.projile.auth.security;

import io.github.kingarturito.projile.support.AuthIntegrationSupport;
import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionTrackingMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Actual Tomcat responses: MockMvc alone cannot prove container-created session cookie flags. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties={"spring.config.location=classpath:application-test.properties","spring.profiles.active=test"})
class ServerCookieIT extends AuthIntegrationSupport {
    @LocalServerPort int port;
    @Autowired ServletContext servletContext;
    @Test void containerSessionCookieIsSecureHttpOnlySameSiteAndInvalidAfterLogout() throws Exception {
        pending("cookie@example.com");String token=emailedToken();

        HttpClient client=HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

        URI base = URI.create("http://127.0.0.1:"+port);

        var bootstrap = client.send(HttpRequest.newBuilder(base.resolve("/api/auth/session")).GET().build(),HttpResponse.BodyHandlers.ofString());

        String csrf=bootstrap.headers().allValues("Set-Cookie").stream().filter(c->c.startsWith("XSRF-TOKEN=")).findFirst().orElseThrow().split(";",2)[0];

        var verify = client.send(HttpRequest.newBuilder(base.resolve("/api/auth/verify")).header("Cookie",csrf).header("X-XSRF-TOKEN",csrf.substring("XSRF-TOKEN=".length()))
                .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body(Map.of("token",token,"password",PASSWORD)))).build(),HttpResponse.BodyHandlers.ofString());
        assertThat(verify.statusCode()).isEqualTo(204);

        String session = verify.headers().allValues("Set-Cookie").stream().filter(c->c.startsWith("JSESSIONID=")).findFirst().orElseThrow();
        assertThat(session).contains("Secure","HttpOnly","SameSite=Lax");
        assertThat(servletContext.getEffectiveSessionTrackingModes()).containsExactly(SessionTrackingMode.COOKIE);

        String cookie = session.split(";",2)[0];
        var identity = client.send(HttpRequest.newBuilder(base.resolve("/api/auth/session")).header("Cookie",cookie).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertThat(json.readTree(identity.body()).get("authenticated").asBoolean()).isTrue();

        String fresh = identity.headers().allValues("Set-Cookie").stream().filter(c->c.startsWith("XSRF-TOKEN=")).findFirst().orElseThrow().split(";",2)[0];
        var logout = client.send(HttpRequest.newBuilder(base.resolve("/api/auth/logout")).header("Cookie",cookie+"; "+fresh)
                .header("X-XSRF-TOKEN",fresh.substring("XSRF-TOKEN=".length())).POST(HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
        assertThat(logout.statusCode()).isEqualTo(204);

        var replay = client.send(HttpRequest.newBuilder(base.resolve("/api/auth/session")).header("Cookie",cookie).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertThat(json.readTree(replay.body()).get("authenticated").asBoolean()).isFalse();
    }
}
