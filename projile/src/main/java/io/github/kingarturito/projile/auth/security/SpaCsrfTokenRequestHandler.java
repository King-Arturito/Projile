package io.github.kingarturito.projile.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/** Accept Angular's raw cookie header while retaining masked tokens for rendered forms. */
final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
    private final CsrfTokenRequestHandler headerHandler = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler formHandler = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> token) {
        formHandler.handle(request, response, token);
        token.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken token) {
        CsrfTokenRequestHandler handler = StringUtils.hasText(request.getHeader(token.getHeaderName()))
                ? headerHandler : formHandler;
        return handler.resolveCsrfTokenValue(request, token);
    }
}
