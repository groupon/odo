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

import com.groupon.odo.containers.HttpProxyContainer;
import com.groupon.odo.proxylib.Constants;
import com.groupon.odo.proxylib.HistoryService;
import com.groupon.odo.proxylib.SQLService;
import com.groupon.odo.proxylib.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.Filter;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handles requests for the application home page.
 */
@Controller
@ComponentScan(basePackages = {"com.groupon.odo.controllers"}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = HttpProxyContainer.class)
})
@EnableAutoConfiguration(exclude = {ServletWebServerFactoryAutoConfiguration.class})
@PropertySources(value = {@PropertySource("classpath:application.properties")})
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    public File baseDirectory;

    @PostConstruct
    public void init() {
        // update SQL schema
        try {
            SQLService.getInstance().updateSchema("/migrations");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("Running destroy");
        try {
            SQLService.getInstance().stopServer();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "redirect:profiles";
    }

    @Bean
    public ServletWebServerFactory servletContainer() throws Exception {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        int apiPort = Utils.getSystemPort(Constants.SYS_API_PORT);
        factory.setPort(apiPort);
        factory.getSession().setTimeout(Duration.ofMinutes(10));
        factory.setContextPath("/testproxy");
        baseDirectory = new File("./tmp");
        factory.setBaseDirectory(baseDirectory);
        List<TomcatConnectorCustomizer> cs = new ArrayList();
        cs.add(tomcatConnectorCustomizers());
        factory.setTomcatConnectorCustomizers(cs);

        if (Utils.getEnvironmentOptionValue(Constants.SYS_LOGGING_DISABLED) != null) {
            HistoryService.Companion.getInstance().disableHistory();
        }
        return factory;
    }

    @Bean
    public TomcatConnectorCustomizer tomcatConnectorCustomizers() {
        return connector -> {
            connector.setMaxPostSize(-1);
            connector.setProperty("relaxedQueryChars", "<>[\\]^`{|}");
            connector.setProperty("relaxedPathChars", "<>[\\]^`{|}");
        };
    }

    @Bean
    public Filter hiddenHttpMethodFilter() {
        HiddenHttpMethodFilter filter = new HiddenHttpMethodFilter();
        return filter;
    }

    public static void main(String[] args) {
        SpringApplication.run(HomeController.class, args);
        SpringApplication.run(HttpProxyContainer.class, args);
    }
}
