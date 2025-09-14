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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.apicurio.studio.shared.beans.User;
import org.keycloak.KeycloakSecurityContext;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This is a simple filter that extracts authentication information from the 
 * Keycloak 
 * @author eric.wittmann@gmail.com
 */
public class TokenAuthenticationFilter implements Filter {

    @Inject
    private ISecurityContext security;

    /**
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        // Authorization header is required
        String authHeader = httpReq.getHeader("Authorization");
        String username = "test";
        try {
//            JWT.create().withClaim("id", "test").sign(Algorithm.HMAC512("apicurio".getBytes()))
            String authToken = authHeader.substring(7);
            if (authToken.startsWith("Bearer ") || authToken.startsWith("bearer ") ) {
                authToken = authToken.substring(7);
            }
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
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
    }
    
    private KeycloakSecurityContext getSession(HttpServletRequest req) {
        return (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
    }
}
