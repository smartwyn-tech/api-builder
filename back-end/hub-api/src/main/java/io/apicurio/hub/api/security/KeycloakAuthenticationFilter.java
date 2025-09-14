/*
 * Copyright 2017 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.hub.api.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.keycloak.KeycloakSecurityContext;
import io.apicurio.studio.shared.beans.User;

/**
 * This is a simple filter that extracts authentication information from the 
 * Keycloak 
 * @author eric.wittmann@gmail.com
 */
public class KeycloakAuthenticationFilter implements Filter {

    @Inject
    private ISecurityContext security;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
//        KeycloakSecurityContext session = getSession(httpReq);
//        if (session != null) {
//            // Fabricate a User object from information in the access token and store it in the security context.
//            AccessToken token = session.getToken();
//            if (token != null) {
//                User user = new User();
//                user.setEmail(token.getEmail());
//                user.setLogin(token.getPreferredUsername());
//                user.setName(token.getName());
//                if (token.getRealmAccess() == null || token.getRealmAccess().getRoles() == null) {
//                    user.setRoles(Collections.emptyList());
//                } else {
//                    user.setRoles(token.getRealmAccess().getRoles().stream()
//                            .map(StudioRole::forName)
//                            .filter(Objects::nonNull)
//                            .collect(Collectors.toUnmodifiableList()));
//                }
//                ((SecurityContext) security).setUser(user);
//                ((SecurityContext) security).setToken(session.getTokenString());
//            }
//        }
//        chain.doFilter(request, response);
//        HttpServletRequest httpReq = (HttpServletRequest) request;
//        HttpServletResponse httpResp = (HttpServletResponse) response;
//
//        if (override.isManualOverride()) {
//            override.applyUser();
//            chain.doFilter(request, response);
//            return;
//        }
//
        // Authorization header is required
        String authHeader = httpReq.getHeader("Authorization");
        String username = "test";
        try {

//            JWT.create().withClaim("id", "test").sign(Algorithm.HMAC512("apicurio".getBytes()))
            String authToken = authHeader.substring(7);
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512("apicurio".getBytes()))
                    .build()
                    .verify(authToken);
            username = decodedJWT.getClaim("id").asString();
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("e");
        }

        // Authentication successful, configure the security context for the request.
        User user = new User();
        user.setEmail(username + "@gmail.com");
        user.setLogin(username);
        user.setName(username);
        ((SecurityContext) security).setUser(user);
        ((SecurityContext) security).setToken(authHeader);
        chain.doFilter(request, response);
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }
    
    private KeycloakSecurityContext getSession(HttpServletRequest req) {
        return (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
    }
}
