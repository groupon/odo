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
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpProxyContainer extends GenericProxyContainer {
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        int httpPort = Utils.getSystemPort(Constants.SYS_HTTP_PORT);

        factory.setPort(httpPort);
        factory.setSessionTimeout(10, TimeUnit.MINUTES);
        factory.addAdditionalTomcatConnectors(createSslConnector());
        factory.addContextCustomizers(new TomcatContextCustomizer() {
            // The Context element represents a web application, which is run within a particular virtual host.
            // You may define as many Context elements as you wish.
            // Each such Context MUST have a unique context name within a virtual host.
            // The context path does not need to be unique (see parallel deployment below).
            // https://tomcat.apache.org/tomcat-7.0-doc/config/context.html
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
        final int httpsPort = Utils.getSystemPort(Constants.SYS_HTTPS_PORT);
        try {
            File keyStore = com.groupon.odo.proxylib.Utils.copyResourceToLocalFile("tomcat.ks", "tomcat.ks");
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