package com.aventium.identity_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter to capture client_id from OAuth2 authorization requests
 * and store it in the session so the login page can access it.
 */
@Component
@Order(1)
public class OAuthClientIdFilter extends OncePerRequestFilter {

    private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("[?&]client_id=([^&]+)");
    private static final String CLIENT_ID_SESSION_KEY = "OAUTH_CLIENT_ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Check if this is an OAuth2 authorize request
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.contains("/oauth2/authorize")) {
            String queryString = request.getQueryString();
            if (queryString != null) {
                String clientId = extractClientId(queryString);
                if (clientId != null && !clientId.isEmpty()) {
                    // Store in session for login page to access
                    HttpSession session = request.getSession(true);
                    session.setAttribute(CLIENT_ID_SESSION_KEY, clientId);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractClientId(String queryString) {
        Matcher matcher = CLIENT_ID_PATTERN.matcher(queryString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
