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
import com.groupon.transparentproxy.TransparentProxy;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.time.Duration;

@Configuration
public class HttpProxyContainer extends GenericProxyContainer {
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        int httpPort = Utils.INSTANCE.getSystemPort(Constants.SYS_HTTP_PORT);

        factory.setPort(httpPort);
        factory.getSession().setTimeout(Duration.ofMinutes(10));
        factory.addAdditionalTomcatConnectors(createSslConnector());
        // The Context element represents a web application, which is run within a particular virtual host.
// You may define as many Context elements as you wish.
// Each such Context MUST have a unique context name within a virtual host.
// The context path does not need to be unique (see parallel deployment below).
// https://tomcat.apache.org/tomcat-7.0-doc/config/context.html
        factory.addContextCustomizers((TomcatContextCustomizer) context -> {
            JarScanner jarScanner = new JarScanner() {

                @Override
                public void scan(JarScanType scanType, ServletContext context, JarScannerCallback callback) {

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
        }, (TomcatContextCustomizer) context -> {
            context.setCookieProcessor(new LegacyCookieProcessor());
        });

        try {
            TransparentProxy.getInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return factory;
    }

    private Connector createSslConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        final int httpsPort = Utils.INSTANCE.getSystemPort(Constants.SYS_HTTPS_PORT);
        try {
            File keyStore = Utils.INSTANCE.copyResourceToLocalFile("tomcat.ks", "tomcat.ks");
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setPort(httpsPort);
            protocol.setSSLEnabled(true);
            protocol.setSslProtocol("TLS");
            protocol.setKeystoreFile(keyStore.getAbsolutePath());
            protocol.setKeystorePass("changeit");
            return connector;
        } catch (IOException ex) {
            throw new IllegalStateException("can't access keystore: [" + "keystore"
                                                + "] or truststore: [" + "keystore" + "]", ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}