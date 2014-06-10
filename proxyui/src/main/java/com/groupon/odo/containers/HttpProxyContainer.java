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

import com.groupon.odo.proxylib.Constants;
import com.groupon.odo.proxylib.Utils;
import org.apache.catalina.Context;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

@Configuration
public class HttpProxyContainer extends GenericProxyContainer {
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        int httpPort = Utils.GetSystemPort(Constants.SYS_HTTP_PORT);
        factory.setPort(httpPort);
        factory.setSessionTimeout(10, TimeUnit.MINUTES);
        factory.addContextCustomizers(new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                JarScanner jarScanner = new JarScanner() {
                    @Override
                    public void scan(ServletContext context, ClassLoader loader,
                                     JarScannerCallback scannerCallback, Set<String> args) {
                    }
                };
                context.setJarScanner(jarScanner);
            }
        });
        return factory;
    }
}