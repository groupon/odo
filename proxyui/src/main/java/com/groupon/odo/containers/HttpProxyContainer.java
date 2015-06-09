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
        // "Factory interface that can be used to create EmbeddedServletContainers.
        // Implementations are encouraged to extend AbstractEmbeddedServletContainerFactory when possible."
        // -Spring API

        // "EmbeddedServletContainerFactory that can be used to create TomcatEmbeddedServletContainers.
        // Can be initialized using Spring's ServletContextInitializers or Tomcat LifecycleListeners.
        // Unless explicitly configured otherwise this factory will created containers that
        // listens for HTTP requests on port 8080."
        // -Spring API
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        // Utils can be found in odo/proxylib/src/java/
        // Returns the port as configured by the system variables, fallback is the default port value
        int httpPort = Utils.getSystemPort(Constants.SYS_HTTP_PORT);
        // setPort inherited from AbstractConfigurableEmbeddedServletContainer.
        // Sets the port that the embedded servlet container should listen on.
        // If not specified port '8080' will be used.
        // Use port -1 to disable auto-start (i.e start the web application context but not
        // have it listen to any port).
        // -Spring API
        factory.setPort(httpPort);
        // setSessionTimeout inherited from AbstractConfigurableEmbeddedServletContainer.
        // The session timeout in seconds (default 30). If 0 or negative then sessions never expire.
        // -Spring API
        factory.setSessionTimeout(10, TimeUnit.MINUTES);
        // Add Connectors in addition to the default connector, e.g. for SSL or AJP
        factory.addAdditionalTomcatConnectors(createSslConnector());
        // Add TomcatContextCustomizers that should be added to the Tomcat Context.
        factory.addContextCustomizers(new TomcatContextCustomizer() {
            // The Context element represents a web application, which is run within a particular virtual host.
            // You may define as many Context elements as you wish.
            // Each such Context MUST have a unique context name within a virtual host.
            // The context path does not need to be unique (see parallel deployment below).
            // https://tomcat.apache.org/tomcat-7.0-doc/config/context.html
            @Override
            public void customize(Context context) {
                // Scans a web application and classloader hierarchy for JAR files.
                // Uses include TLD scanning and web-fragment.xml scanning.
                // Uses a call-back mechanism so the caller can process each JAR found.
                // -Tomcat API
                JarScanner jarScanner = new JarScanner() {
                    // usually, it does the following:
                    // Scan the provided ServletContext and classloader for JAR files.
                    @Override
                    public void scan(ServletContext context, ClassLoader loader,
                                     JarScannerCallback scannerCallback, Set<String> args) {
                    }
                };
                context.setJarScanner(jarScanner);
            }
        });

        try {
            // internal class
            // can be found in odo/proxyserver/src/main/java/transparentproxy
            // appears to either return the instance of transparentproxy
            // or create a new instance if the original instance is null and return that
            TransparentProxy.getInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return factory;
    }

    private Connector createSslConnector() {
        // There are two different types of connectors.
        // Connectors that allow browsers to connect directly to the Tomcat and
        // connectors that do it through a Web Server.
        // Implementation of a Coyote connector.
        // -https://tomcat.apache.org/tomcat-4.1-doc/config/connectors.html
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        // Abstract the protocol implementation, including threading, etc.
        // Processor is single threaded and specific to stream-based protocols, will not fit Jk protocols like JNI.
        // -Tomcat API
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        final int httpsPort = Utils.getSystemPort(Constants.SYS_HTTPS_PORT);
        try {
            File keyStore = com.groupon.odo.proxylib.Utils.copyResourceToLocalFile("tomcat.ks", "tomcat.ks");
            // Set the scheme that will be assigned to requests received through this connector.
            connector.setScheme("https");
            // Set the secure connection flag that will be assigned to requests received through this connector.
            connector.setSecure(true);
            // Set the port number on which we listen for requests.
            // -Tomcat API
            connector.setPort(httpsPort);
            protocol.setSSLEnabled(true);
            protocol.setSslProtocol("TLS");
            protocol.setKeystoreFile(keyStore.getAbsolutePath());
            protocol.setKeystorePass("changeit");
            return connector;
        } catch (IOException ex) {
            // Tiffany's note: should "keystore" not have quotations marks around it?
            throw new IllegalStateException("can't access keystore: [" + "keystore"
                                                + "] or truststore: [" + "keystore" + "]", ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}