package com.zzn.librarysystem.authServer.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class AuthErrorAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException authenticationException) throws IOException, ServletException {
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);

        if (authenticationException instanceof OAuth2AuthenticationException) {
            OAuth2Error error = ((OAuth2AuthenticationException) authenticationException).getError();
            String body = String.format("{\"failedReason\":\"AUTH_FAILED\", \"message\":\"%s\" }", error.getErrorCode());
            httpResponse.getBody().write(body.getBytes());
        }
        else {
            String body = "{\"failedReason\":\"AUTH_FAILED\"}";
            httpResponse.getBody().write(body.getBytes());
        }
    }
}
