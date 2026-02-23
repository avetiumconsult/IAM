package com.aventium.identity_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
// IMPORTANT: correct package for Spring Authorization Server:
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;


@Configuration
public class AuthorizationServerConfig {

    /**
     * Authorization Server endpoints (these are what your Python apps will use):
     * - /.well-known/openid-configuration
     * - /oauth2/authorize
     * - /oauth2/token
     * - /oauth2/jwks
     */
    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (as) -> as.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .formLogin(login -> {
                    login.loginPage("/login")
                            .permitAll();
                    // Use SavedRequestAwareAuthenticationSuccessHandler to redirect back to saved OAuth request
                    SavedRequestAwareAuthenticationSuccessHandler successHandler = 
                            new SavedRequestAwareAuthenticationSuccessHandler();
                    successHandler.setAlwaysUseDefaultTargetUrl(false); // Use saved request if available
                    login.successHandler(successHandler);
                });

        return http.build();
    }

    
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        // Set issuer explicitly in application.yml in production.
        // If not set, Spring will infer it from incoming requests (can be risky behind proxies).
        return AuthorizationServerSettings.builder().build();
    }
}