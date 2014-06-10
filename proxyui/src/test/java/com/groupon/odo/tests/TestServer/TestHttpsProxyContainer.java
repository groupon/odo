/*
Copyright (c) 2014, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.groupon.odo.tests.TestServer;


import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
public class TestHttpsProxyContainer extends GenericTestProxyContainer {
    @Bean
    public EmbeddedServletContainerFactory servletContainer() throws Exception {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        MockService config = MockService.getInstance();
        factory.setPort(config.getPort());
        factory.setSessionTimeout(10, TimeUnit.MINUTES);
        factory.addContextCustomizers(new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                JarScanner jarScanner = new JarScanner() {
                    @Override
                    public void scan(ServletContext arg0, ClassLoader arg1,
                                     JarScannerCallback arg2, Set<String> arg3) {
                    }
                };
                context.setJarScanner(jarScanner);
            }
        });

        // extract keystores to temp file
        // the keystore needs to be in the filesystem and not just on the classpath
        // this ensures that it gets unpacked from the jar/war
        final File keyStore = com.groupon.odo.proxylib.Utils.copyResourceToLocalFile("tomcat.ks", "tomcat.ks");

        // Add HTTPS customization to connector
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                MockService config = MockService.getInstance();
                connector.setPort(config.getPort());
                connector.setSecure(true);
                Http11NioProtocol proto = (Http11NioProtocol) connector.getProtocolHandler();
                proto.setSSLEnabled(true);
                connector.setScheme("https");
                connector.setAttribute("keystorePass", "changeit");
                connector.setAttribute("keystoreFile", keyStore.getAbsolutePath());
                connector.setAttribute("clientAuth", "false");
                connector.setAttribute("sslProtocol", "TLS");
                connector.setAttribute("sslEnabled", true);
            }
        });

        return factory;
    }
}