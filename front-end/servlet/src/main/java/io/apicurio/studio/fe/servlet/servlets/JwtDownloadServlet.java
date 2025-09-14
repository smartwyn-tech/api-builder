/*
 * Copyright 2020 JBoss Inc
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

package io.apicurio.studio.fe.servlet.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet used to download content (e.g. an API design) based on JWT Auth.
 *
 * @author carles.arnal@redhat.com
 */
public class JwtDownloadServlet extends DownloadServlet {

    private static final long serialVersionUID = 8432874125909707075L;
    private static Logger logger = LoggerFactory.getLogger(JwtDownloadServlet.class);

    @Override
    protected void proxyUrlTo(String url, HttpServletRequest request, HttpServletResponse response) {
        proxyUrlWithToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpZCI6InRlc3QifQ.BaGxNQFedbRaRJUZkeRaq3Wo_4elMmmS7FKSqgAB80dy3aveXGhSbOCZUt9JkNsBijZYexxhp5TRw2ffkS1v8A", url, response);
//        try {
//
//            JWTCallerPrincipal principal = (JWTCallerPrincipal) request.getUserPrincipal();
//
//            if (principal != null) {
//                proxyUrlWithToken(principal.getRawToken(), url, response);
//            } else {
//                throw new IllegalStateException("No user present at request");
//            }
//        } catch (IllegalStateException e) {
//            logger.error("Error proxying URL: " + url, e);
//            try {
//                response.sendError(500);
//            } catch (IOException e1) {
//            }
//        }
    }
}