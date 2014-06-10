/*
 Copyright 2014 Groupon, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package com.groupon.odo.controllers;

import com.groupon.odo.proxylib.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * This interceptor checks to make sure that all configuration properties were handled
 * If they were not then it redirects to the Proxy configuration controller
 */
public class ConfigurationInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory
            .getLogger(ConfigurationInterceptor.class);

    /**
     * This will check to see if certain configuration values exist from the ConfigurationService
     * If not then it redirects to the configuration screen
     */
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        String queryString = request.getQueryString() == null ? "" : request.getQueryString();

        if (ConfigurationService.getInstance().isValid()
                || request.getServletPath().startsWith("/configuration")
                || request.getServletPath().startsWith("/resources")
                || queryString.contains("requestFromConfiguration=true")) {
            return true;
        } else {
            response.sendRedirect("configuration");
            return false;
        }
    }
}
