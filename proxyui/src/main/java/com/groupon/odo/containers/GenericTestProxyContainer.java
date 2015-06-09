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

    // "The org.slf4j.Logger interface is the main user entry point of SLF4J API. 
    // It is expected that logging takes place through concrete implementations of this interface."
    // -http://www.slf4j.org/api/org/slf4j/Logger.html
    // NOTE: slf4j is a Java logging API
    // used for logging
    private static final Logger logger = LoggerFactory.getLogger(GenericProxyContainer.class);

    // @bean annotation "Indicates that a method produces a bean to be managed by the Spring container."
    // -http://docs.spring.io/spring-framework/docs/4.0.4.RELEASE/javadoc-api/org/springframework/context/annotation/Bean.html
    @Bean
    public ServletRegistrationBean dispatcherRegistration() {
        // a ServletRegistrationBean is used to register servlets in serlet 3.0 container within spring boot
        // -http://www.leveluplunch.com/blog/2014/04/01/spring-boot-configure-servlet-mapping-filters/
        ServletRegistrationBean registration = new ServletRegistrationBean(new Proxy());

        // adds url mappings to the servlet
        // -http://docs.spring.io/autorepo/docs/spring-boot/1.1.6.RELEASE/api/org/springframework
        //      /boot/context/embedded/ServletRegistrationBean.html
        // url mapping: specifies the web container of which java servlet should be invoked for a url given by client
        // aka maps url patterns to servlets
        // -http://javapapers.com/servlet/what-is-servlet-mapping/
        registration.addUrlMappings("/*");

        // return the ServletRegistrationBean
        return registration;
    }

    @Bean
    public FilterRegistrationBean removeHeaderRegistration() {
        // custom class
        // found in proxyserver/src/main/java/com/groupon/odo
        RemoveHeaderFilter rhf = new RemoveHeaderFilter();
        // "A ServletContextInitializer to register Filters in a Servlet 3.0+ container.
        // Similar to the registration features provided by ServletContext but with a Spring Bean friendly design."
        // -Spring API
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(rhf);

        // log that remove header filter is being added
        logger.info("Adding remove header filter!");
        // "add URL patterns that the filter will be registered against"
        // -Spring API
        // presumably this means the filter will be registered against all URL patterns (wildcard)
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}
