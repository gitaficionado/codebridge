package com.codebridge.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public final class SpaCsrfTokenRequestHandler
        implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestAttributeHandler plain =
            new CsrfTokenRequestAttributeHandler();

    private final XorCsrfTokenRequestAttributeHandler xor =
            new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Supplier<CsrfToken> csrfToken
    ) {
        // Preserve BREACH protection for tokens exposed
        // as request attributes.
        xor.handle(request, response, csrfToken);

        // Ensure a deferred token is loaded so that
        // CookieCsrfTokenRepository can provide the cookie.
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(
            HttpServletRequest request,
            CsrfToken csrfToken
    ) {
        String headerValue =
                request.getHeader(csrfToken.getHeaderName());

        // Browser tools such as Swagger send the plain
        // token from the XSRF-TOKEN cookie in this header.
        if (StringUtils.hasText(headerValue)) {
            return plain.resolveCsrfTokenValue(
                    request,
                    csrfToken
            );
        }

        // Preserve masked-token handling for requests
        // that do not use the CSRF header.
        return xor.resolveCsrfTokenValue(
                request,
                csrfToken
        );
    }
}
