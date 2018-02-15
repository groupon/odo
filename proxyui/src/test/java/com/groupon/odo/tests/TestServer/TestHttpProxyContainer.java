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
package com.groupon.odo.tests.TestServer;

import org.apache.catalina.Context;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
public class TestHttpProxyContainer extends GenericTestProxyContainer {
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        MockService config = MockService.getInstance();
        factory.setPort(config.getPort());
        factory.setSessionTimeout(10, TimeUnit.MINUTES);
        factory.addContextCustomizers(new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                JarScanner jarScanner = new JarScanner() {
                    @Override
                    public void scan(JarScanType jarScanType, ServletContext servletContext, JarScannerCallback jarScannerCallback) {

                    }

                    @Override
                    public JarScanFilter getJarScanFilter() {
                        return null;
                    }

                    @Override
                    public void setJarScanFilter(JarScanFilter jarScanFilter) {
                    }
                };
                context.setJarScanner(jarScanner);
            }
        });
        return factory;
    }
}