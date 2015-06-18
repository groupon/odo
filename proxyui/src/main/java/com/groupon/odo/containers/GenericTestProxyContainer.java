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
package com.groupon.odo.containers;

import com.groupon.odo.Proxy;
import com.groupon.odo.RemoveHeaderFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

class GenericProxyContainer {


    private static final Logger logger = LoggerFactory.getLogger(GenericProxyContainer.class);

    @Bean
    public ServletRegistrationBean dispatcherRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new Proxy());
        registration.addUrlMappings("/*");

        return registration;
    }

    @Bean
    public FilterRegistrationBean removeHeaderRegistration() {
        RemoveHeaderFilter rhf = new RemoveHeaderFilter();
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(rhf);

        logger.info("Adding remove header filter!");
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}