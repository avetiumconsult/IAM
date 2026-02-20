package com.aventium.identity_service.controller;

import com.aventium.identity_service.repository.OAuthClientRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final OAuthClientRepository oauthClientRepository;
    private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("[?&]client_id=([^&]+)");
    private static final String SPRING_SECURITY_SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    private static final String CLIENT_ID_SESSION_KEY = "OAUTH_CLIENT_ID";

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "client_id", required = false) String clientId,
            HttpServletRequest request,
            Model model
    ) {
        // Try to extract client_id from various sources
        if (clientId == null || clientId.isEmpty()) {
            // 1. Try session (stored by OAuthClientIdFilter)
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object sessionClientId = session.getAttribute(CLIENT_ID_SESSION_KEY);
                if (sessionClientId != null) {
                    clientId = sessionClientId.toString();
                }
            }
            
            // 2. Try query string
            if (clientId == null || clientId.isEmpty()) {
                String queryString = request.getQueryString();
                if (queryString != null) {
                    clientId = extractClientId(queryString);
                }
            }
            
            // 3. Try saved request in session (Spring Security stores the original OAuth request here)
            if ((clientId == null || clientId.isEmpty()) && session != null) {
                SavedRequest savedRequest = (SavedRequest) session.getAttribute(SPRING_SECURITY_SAVED_REQUEST);
                if (savedRequest != null) {
                    String redirectUrl = savedRequest.getRedirectUrl();
                    if (redirectUrl != null) {
                        clientId = extractClientId(redirectUrl);
                    }
                }
            }
            
            // 4. Try referer header (original OAuth request)
            if (clientId == null || clientId.isEmpty()) {
                String referer = request.getHeader("Referer");
                if (referer != null) {
                    clientId = extractClientId(referer);
                }
            }
        }

        String[] applicationName = {"Account"};
        String[] loginMessage = {"Login to your account"};

        if (clientId != null && !clientId.isEmpty()) {
            oauthClientRepository.findByClientId(clientId)
                    .ifPresent(client -> {
                        String appName = client.getApplication().getName();
                        applicationName[0] = appName;
                        loginMessage[0] = "Login to your " + appName + " account";
                    });
        }

        model.addAttribute("applicationName", applicationName[0]);
        model.addAttribute("loginMessage", loginMessage[0]);
        if (clientId != null) {
            model.addAttribute("clientId", clientId);
        }

        return "login";
    }

    private String extractClientId(String url) {
        Matcher matcher = CLIENT_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
