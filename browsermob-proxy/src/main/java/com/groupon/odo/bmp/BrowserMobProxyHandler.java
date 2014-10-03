/*
SOURCE: https://raw.github.com/lightbody/browsermob-proxy/7f0a6ec2663bace3f64c878e7f006090c38fbfdc/src/main/java/net/lightbody/bmp/proxy/BrowserMobProxyHandler.java

ORIGINAL LICENSE:
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."

      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or
          Derivative Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.

      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   APPENDIX: How to apply the Apache License to your work.

      To apply the Apache License to your work, attach the following
      boilerplate notice, with the fields enclosed by brackets "[]"
      replaced with your own identifying information. (Don't include
      the brackets!)  The text should be enclosed in the appropriate
      comment syntax for the file format. We also recommend that a
      file or class name and description of purpose be included on the
      same "printed page" as the copyright notice for easier
      identification within third-party archives.

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

GROUPON LICENSE:

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

package com.groupon.odo.bmp;

import com.groupon.odo.proxylib.Constants;
import net.lightbody.bmp.proxy.FirefoxErrorConstants;
import net.lightbody.bmp.proxy.FirefoxErrorContent;
import net.lightbody.bmp.proxy.http.BadURIException;
import net.lightbody.bmp.proxy.http.BrowserMobHttpResponse;
import net.lightbody.bmp.proxy.http.RequestCallback;
import net.lightbody.bmp.proxy.jetty.http.*;
import net.lightbody.bmp.proxy.jetty.jetty.Server;
import net.lightbody.bmp.proxy.jetty.util.InetAddrPort;
import net.lightbody.bmp.proxy.jetty.util.URI;
import net.lightbody.bmp.proxy.selenium.KeyStoreManager;
import net.lightbody.bmp.proxy.selenium.LauncherUtils;
import net.lightbody.bmp.proxy.selenium.SeleniumProxyHandler;
import net.lightbody.bmp.proxy.util.Log;

import org.apache.http.Header;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;

import com.groupon.odo.bmp.http.BrowserMobHttpClient;
import com.groupon.odo.bmp.http.BrowserMobHttpRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrowserMobProxyHandler extends SeleniumProxyHandler {
    private static final Log LOG = new Log();

    private static final int HEADER_BUFFER_DEFAULT = 2;
    private static final long serialVersionUID = 1L;

    private Server jettyServer;
    private int headerBufferMultiplier = HEADER_BUFFER_DEFAULT;
    private BrowserMobHttpClient httpClient;
    protected final Set<SslRelay> sslRelays = new HashSet<SslRelay>();
    // BEGIN ODO CHANGES
    // Map to hold onto listeners per hostname
    protected final Map<String, SslRelayOdo> _sslMap = new LinkedHashMap<String, SslRelayOdo>();
    protected final Map<String, Date> _certExpirationMap = new LinkedHashMap<String, Date>();
    // END ODO CHANGES

    public BrowserMobProxyHandler() {
        super(true, "", "", false, false);
        setShutdownLock(new Object());

        // set the tunnel timeout to something larger than the default 30 seconds
        // we're doing this because SSL connections taking longer than this timeout
        // will result in a socket connection close that does NOT get handled by the
        // normal socket connection closing reportError(). Further, it has been seen
        // that Firefox will actually retry the connection, causing very strange
        // behavior observed in case http://browsermob.assistly.com/agent/case/27843
        //
        // You can also reproduce it by simply finding some slow loading SSL site
        // that takes greater than 30 seconds to response.
        //
        // Finally, it should be noted that we're setting this timeout to some value
        // that we anticipate will be larger than any reasonable response time of a
        // real world request. We don't set it to -1 because the underlying ProxyHandler
        // will not use it if it's <= 0. We also don't set it to Long.MAX_VALUE because
        // we don't yet know if this will cause a serious resource drain, so we're
        // going to try something like 5 minutes for now.
        setTunnelTimeoutMs(300000);
    }
    
    // BEGIN ODO CHANGES
    private static final ThreadLocal<String> requestOriginalHostName = new ThreadLocal<String>();
    private static final ThreadLocal<URI> requestOriginalURI = new ThreadLocal<URI>();

    @Override
    /**
     * This is the original handleConnect from BrowserMobProxyHandler with the following changes:
     * 
     * 1. Store the original URI in a ThreadLocal so that we can determine the host addr later
     * 2. Store the original hostname in a ThreadLocal so we don't need to do the same string processing again later
     * 2. Set the URI to 127.0.0.1 to pass into Odo
     * 3. Call the original handleConnect from SeleniumProxyHandler(copied to handle an Odo SslRelay)
     */
    public void handleConnect(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        URI uri = request.getURI();
        String original = uri.toString();
        LOG.info("Hostname: " + original);
        String host = original;
        String port = null;
        int colon = original.indexOf(':');
        if (colon != -1) {
            host = original.substring(0, colon);
            port = original.substring(colon + 1);
        }
        
        // store the original host name
        requestOriginalHostName.set(host);
        
        // make a copy of the URI(have to create a new URI otherwise things are copied by reference and get changed)
        URI realURI = new URI(request.getURI());
        requestOriginalURI.set(realURI);
        
    	// send requests to Odo HTTPS port
        int httpsPort = com.groupon.odo.proxylib.Utils.GetSystemPort(Constants.SYS_HTTPS_PORT);
    	uri.setURI("127.0.0.1:" + httpsPort);
    	uri.setPort(httpsPort);

        String altHost = httpClient.remappedHost(host);
        if (altHost != null) {
            if (port != null) {
                uri.setURI(altHost + ":" + port);
            } else {
                uri.setURI(altHost);
            }
        }

        handleConnectOriginal(pathInContext, pathParams, request, response);
    }
    // END ODO CHANGES
    
    // BEGIN ODO CHANGES
    /**
     * Copied from original SeleniumProxyHandler
     * Changed SslRelay to SslListener and getSslRelayOrCreateNew to getSslRelayOrCreateNewOdo
     * No other changes to the function
     * @param pathInContext
     * @param pathParams
     * @param request
     * @param response
     * @throws HttpException
     * @throws IOException
     */
    public void handleConnectOriginal(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        URI uri = request.getURI();

        try {
            LOG.fine("CONNECT: " + uri);
            InetAddrPort addrPort;
            // When logging, we'll attempt to send messages to hosts that don't exist
            if (uri.toString().endsWith(".selenium.doesnotexist:443")) {
                // so we have to do set the host to be localhost (you can't new up an IAP with a non-existent hostname)
                addrPort = new InetAddrPort(443);
            } else {
                addrPort = new InetAddrPort(uri.toString());
            }

            if (isForbidden(HttpMessage.__SSL_SCHEME, addrPort.getHost(), addrPort.getPort(), false)) {
                sendForbid(request, response, uri);
            } else {
                HttpConnection http_connection = request.getHttpConnection();
                http_connection.forceClose();

                HttpServer server = http_connection.getHttpServer();

                SslListener listener = getSslRelayOrCreateNewOdo(uri, addrPort, server);

                int port = listener.getPort();

                // Get the timeout
                int timeoutMs = 30000;
                Object maybesocket = http_connection.getConnection();
                if (maybesocket instanceof Socket) {
                    Socket s = (Socket) maybesocket;
                    timeoutMs = s.getSoTimeout();
                }

                // Create the tunnel
                HttpTunnel tunnel = newHttpTunnel(request, response, InetAddress.getByName(null), port, timeoutMs);

                if (tunnel != null) {
                    // TODO - need to setup semi-busy loop for IE.
                    if (_tunnelTimeoutMs > 0) {
                        tunnel.getSocket().setSoTimeout(_tunnelTimeoutMs);
                        if (maybesocket instanceof Socket) {
                            Socket s = (Socket) maybesocket;
                            s.setSoTimeout(_tunnelTimeoutMs);
                        }
                    }
                    tunnel.setTimeoutMs(timeoutMs);

                    customizeConnection(pathInContext, pathParams, request, tunnel.getSocket());
                    request.getHttpConnection().setHttpTunnel(tunnel);
                    response.setStatus(HttpResponse.__200_OK);
                    response.setContentLength(0);
                }
                request.setHandled(true);
            }
        }
        catch (Exception e) {
            LOG.fine("error during handleConnect", e);
            response.sendError(HttpResponse.__500_Internal_Server_Error, e.toString());
        }
    }
    // END ODO CHANGES

    // BEGIN ODO CHANGES
    /**
     * This function wires up a SSL Listener with the cyber villians root CA and cert with the correct CNAME for the request
     * @param host
     * @param listener
     */
    protected X509Certificate wireUpSslWithCyberVilliansCAOdo(String host, SslListener listener) {
        List<String> originalHosts = httpClient.originalHosts(host);
        if (originalHosts != null && !originalHosts.isEmpty()) {
            if (originalHosts.size() == 1) {
                host = originalHosts.get(0);
            } else {
                // Warning: this is NASTY, but people rarely even run across this and those that do are solved by this
                // ok, this really isn't legal in real SSL land, but we'll make an exception and just pretend it's a wildcard
                String first = originalHosts.get(0);
                host = "*" + first.substring(first.indexOf('.'));
            }
        }
        
        host = requestOriginalHostName.get();

        
        // Add cybervillians CA(from browsermob)
        try {
        	// see https://github.com/webmetrics/browsermob-proxy/issues/105
            String escapedHost = host.replace('*', '_');
            
            KeyStoreManager keyStoreManager = Utils.getKeyStoreManager(escapedHost);
            keyStoreManager.getKeyStore().deleteEntry(KeyStoreManager._caPrivKeyAlias);
            keyStoreManager.persist();
            listener.setKeystore(new File("seleniumSslSupport" + File.separator + escapedHost + File.separator + "cybervillainsCA.jks").getAbsolutePath());
            
            return keyStoreManager.getCertificateByAlias(escapedHost);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SslRelayOdo getSslRelayOrCreateNewOdo(URI uri, InetAddrPort addrPort, HttpServer server) throws Exception {
    	URI realURI = requestOriginalURI.get();
    	InetAddrPort realPort = new InetAddrPort(realURI.toString());
    	LOG.info("GETSSLRELAY: {}, {}", realURI, realPort);
    	String host = new URL("https://" + realURI.toString()).getHost();
    	
    	// create a host and port string so the listener sslMap can be keyed off the combination
    	String hostAndPort = host.concat(String.valueOf(realPort.getPort()));
    	LOG.info("getSSLRelay host: {}", hostAndPort);
    	SslRelayOdo listener = null;
    	
    	synchronized(_sslMap) {
    		listener = _sslMap.get(hostAndPort);
    		
    		// check the certificate expiration to see if we need to reload it
    		if (listener != null) {
    			Date exprDate = _certExpirationMap.get(hostAndPort);
    			if (exprDate.before(new Date())) {
    				// destroy the listener
    				if (listener.getHttpServer() != null && listener.isStarted()) {
    					listener.getHttpServer().removeListener(listener);
                    }
    				listener = null;
    			}
    		}
    		
    		// create the listener if it didn't exist
    		if (listener == null) {
    			listener = new SslRelayOdo(addrPort);
    	        listener.setNukeDirOrFile(null);
    	        
    	        _certExpirationMap.put(hostAndPort, wireUpSslWithCyberVilliansCAOdo(host, listener).getNotAfter());
    	        
    	        listener.setPassword("password");
    	        listener.setKeyPassword("password");

    	        if (!listener.isStarted()) {
    	            server.addListener(listener);

    	            startRelayWithPortTollerance(server, listener, 1);
    	        }
    	        
    	        _sslMap.put(hostAndPort,  listener);
    		}
    	}
        return listener;
    }
    // END ODO CHANGES
    
    /* Commenting out original implementations
    @Override
    protected void wireUpSslWithCyberVilliansCA(String host, SeleniumProxyHandler.SslRelay listener) {
        List<String> originalHosts = httpClient.originalHosts(host);
        if (originalHosts != null && !originalHosts.isEmpty()) {
            if (originalHosts.size() == 1) {
                host = originalHosts.get(0);
            } else {
                // Warning: this is NASTY, but people rarely even run across this and those that do are solved by this
                // ok, this really isn't legal in real SSL land, but we'll make an exception and just pretend it's a wildcard
                String first = originalHosts.get(0);
                host = "*" + first.substring(first.indexOf('.'));
            }
        }
        super.wireUpSslWithCyberVilliansCA(host, listener);
    }
    
    @Override
    protected SslRelay getSslRelayOrCreateNew(URI uri, InetAddrPort addrPort, HttpServer server) throws Exception {
        SslRelay relay = super.getSslRelayOrCreateNew(uri, addrPort, server);
        relay.setNukeDirOrFile(null);

        synchronized (sslRelays) {
            sslRelays.add(relay);
        }

        if (!relay.isStarted()) {
            server.addListener(relay);

            startRelayWithPortTollerance(server, relay, 1);
        }

        return relay;
    }
    */

    // BEGIN ODO CHANGES
    // Changed method signature from SslRelay to SslListener
    // END ODO CHANGES
    private void startRelayWithPortTollerance(HttpServer server, SslListener relay, int tries) throws Exception {
        if (tries >= 5) {
            throw new BindException("Unable to bind to several ports, most recently " + relay.getPort() + ". Giving up");
        }
        try {
            if (server.isStarted()) {
                relay.start();
            } else {
                throw new RuntimeException("Can't start SslRelay: server is not started (perhaps it was just shut down?)");
            }
        } catch (BindException e) {
            // doh - the port is being used up, let's pick a new port
            LOG.info("Unable to bind to port %d, going to try port %d now", relay.getPort(), relay.getPort() + 1);
            relay.setPort(relay.getPort() + 1);
            startRelayWithPortTollerance(server, relay, tries + 1);
        }
    }

    @Override
    protected HttpTunnel newHttpTunnel(HttpRequest httpRequest, HttpResponse httpResponse, InetAddress inetAddress, int i, int i1) throws IOException {
        // we're opening up a new tunnel, so let's make sure that the associated SslRelay (which may or may not be new) has the proper buffer settings
        adjustListenerBuffers();

        return super.newHttpTunnel(httpRequest, httpResponse, inetAddress, i, i1);
    }

    @SuppressWarnings({"unchecked"})
    protected long proxyPlainTextRequest(final URL url, String pathInContext, String pathParams, HttpRequest request, final HttpResponse response) throws IOException {
        try {
            String urlStr = url.toString();

            // We don't want selenium-related showing up in the detailed transaction logs
            if (urlStr.contains("/selenium-server/")) {
                return super.proxyPlainTextRequest(url, pathInContext, pathParams, request, response);
            }

            // BEGIN ODO CHANGES
            if (urlStr.toLowerCase().startsWith(Constants.ODO_INTERNAL_WEBAPP_URL)) {
                urlStr = "http://localhost:" + com.groupon.odo.proxylib.Utils.GetSystemPort(Constants.SYS_HTTP_PORT) +"/odo";
            }
            // END ODO CHANGES

            // we also don't URLs that Firefox always loads on startup showing up, or even wasting bandwidth.
            // so for these we just nuke them right on the spot!
            if (urlStr.startsWith("https://sb-ssl.google.com:443/safebrowsing")
                    || urlStr.startsWith("http://en-us.fxfeeds.mozilla.com/en-US/firefox/headlines.xml")
                    || urlStr.startsWith("http://fxfeeds.mozilla.com/firefox/headlines.xml")
                    || urlStr.startsWith("http://fxfeeds.mozilla.com/en-US/firefox/headlines.xml")
                    || urlStr.startsWith("http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml")) {
                // don't even xfer these!
                request.setHandled(true);
                return -1;
            }

            // this request must have come in just as we were shutting down, since there is no more associted http client
            // so let's just handle it like we do any other request we don't know what to do with :)
            if (httpClient == null) {
                // don't even xfer these!
                request.setHandled(true);
                return -1;

                // for debugging purposes, NOT to be used in product!
                // httpClient = new BrowserMobHttpClient(Integer.MAX_VALUE);
                // httpClient.setDecompress(false);
                // httpClient.setFollowRedirects(false);
            }

            BrowserMobHttpRequest httpReq = null;
            if ("GET".equals(request.getMethod())) {
                httpReq = httpClient.newGet(urlStr, request);
            } else if ("POST".equals(request.getMethod())) {
                httpReq = httpClient.newPost(urlStr, request);
            } else if ("PUT".equals(request.getMethod())) {
                httpReq = httpClient.newPut(urlStr, request);
            } else if ("DELETE".equals(request.getMethod())) {
                httpReq = httpClient.newDelete(urlStr, request);
            } else if ("OPTIONS".equals(request.getMethod())) {
                httpReq = httpClient.newOptions(urlStr, request);
            } else if ("HEAD".equals(request.getMethod())) {
                httpReq = httpClient.newHead(urlStr, request);
            } else {
                LOG.warn("Unexpected request method %s, giving up", request.getMethod());
                request.setHandled(true);
                return -1;
            }

            // copy request headers
            boolean isGet = "GET".equals(request.getMethod());
            boolean hasContent = false;
            Enumeration<?> enm = request.getFieldNames();
            long contentLength = 0;
            while (enm.hasMoreElements()) {
                String hdr = (String) enm.nextElement();

                if (!isGet && HttpFields.__ContentType.equals(hdr)) {
                    hasContent = true;
                }
                if (!isGet && HttpFields.__ContentLength.equals(hdr)) {
                    contentLength = Long.parseLong(request.getField(hdr));
                    continue;
                }

                Enumeration<?> vals = request.getFieldValues(hdr);
                while (vals.hasMoreElements()) {
                    String val = (String) vals.nextElement();
                    if (val != null) {
                        if (!isGet && HttpFields.__ContentLength.equals(hdr) && Integer.parseInt(val) > 0) {
                            hasContent = true;
                        }

                        if (!_DontProxyHeaders.containsKey(hdr)) {
                            httpReq.addRequestHeader(hdr, val);
                        }
                    }
                }
            }

            try {
                // do input thang!
                InputStream in = request.getInputStream();
                if (hasContent) {
                    // BEGIN ODO CHANGES
                    httpReq.setRequestInputStream(in);
                    // END ODO CHANGES
                }
            } catch (Exception e) {
                LOG.fine(e.getMessage(), e);
            }

            // execute the request
            httpReq.setOutputStream(response.getOutputStream());
            httpReq.setRequestCallback(new RequestCallback() {
                @Override
                public void handleStatusLine(StatusLine statusLine) {
                    response.setStatus(statusLine.getStatusCode());
                    response.setReason(statusLine.getReasonPhrase());
                }

                @Override
                public void handleHeaders(Header[] headers) {
                    for (Header header : headers) {
                        if (reportHeader(header)) {
                            response.addField(header.getName(), header.getValue());
                        }
                    }
                }

                @Override
                public boolean reportHeader(Header header) {
                    // don't pass in things like Transfer-Encoding and other headers that are being masked by the underlying HttpClient impl
                    return !_DontProxyHeaders.containsKey(header.getName()) && !_ProxyAuthHeaders.containsKey(header.getName());
                }

                @Override
                public void reportError(Exception e) {
                    BrowserMobProxyHandler.reportError(e, url, response);
                }
            });

            BrowserMobHttpResponse httpRes = httpReq.execute();

            // ALWAYS mark the request as handled if we actually handled it. Otherwise, Jetty will think non 2xx responses
            // mean it wasn't actually handled, resulting in totally valid 304 Not Modified requests turning in to 404 responses
            // from Jetty. NOT good :(
            request.setHandled(true);
            return httpRes.getEntry().getResponse().getBodySize();
        } catch (BadURIException e) {
            // this is a known error case (see MOB-93)
            LOG.info(e.getMessage());
            BrowserMobProxyHandler.reportError(e, url, response);
            return -1;
        } catch (Exception e) {
            LOG.info("Exception while proxying " + url, e);
            BrowserMobProxyHandler.reportError(e, url, response);
            return -1;
        }
    }

    private static void reportError(Exception e, URL url, HttpResponse response) {
        FirefoxErrorContent error = FirefoxErrorContent.GENERIC;
        if (e instanceof UnknownHostException) {
            error = FirefoxErrorContent.DNS_NOT_FOUND;
        } else if (e instanceof ConnectException) {
            error = FirefoxErrorContent.CONN_FAILURE;
        } else if (e instanceof ConnectTimeoutException) {
            error = FirefoxErrorContent.NET_TIMEOUT;
        } else if (e instanceof NoHttpResponseException) {
            error = FirefoxErrorContent.NET_RESET;
        } else if (e instanceof EOFException) {
            error = FirefoxErrorContent.NET_INTERRUPT;
        } else if (e instanceof IllegalArgumentException && e.getMessage().startsWith("Host name may not be null")){
            error = FirefoxErrorContent.DNS_NOT_FOUND;
        } else if (e instanceof BadURIException){
            error = FirefoxErrorContent.MALFORMED_URI;
        }

        String shortDesc = String.format(error.getShortDesc(), url.getHost());
        String text = String.format(FirefoxErrorConstants.ERROR_PAGE, error.getTitle(), shortDesc, error.getLongDesc());


        try {
            response.setStatus(HttpResponse.__502_Bad_Gateway);
            response.setContentLength(text.length());
            response.getOutputStream().write(text.getBytes());
        } catch (IOException e1) {
            LOG.warn("IOException while trying to report an HTTP error");
        }
    }

    public void autoBasicAuthorization(String domain, String username, String password) {
        httpClient.autoBasicAuthorization(domain, username, password);
    }

    public void rewriteUrl(String match, String replace) {
        httpClient.rewriteUrl(match, replace);
    }

    public void remapHost(String source, String target) {
        httpClient.remapHost(source, target);
    }

    public void setJettyServer(Server jettyServer) {
        this.jettyServer = jettyServer;
    }

    public void adjustListenerBuffers(int headerBufferMultiplier) {
        // limit to 10 so there can't be any out of control memory consumption by a rogue script
        if (headerBufferMultiplier > 10) {
            headerBufferMultiplier = 10;
        }

        this.headerBufferMultiplier = headerBufferMultiplier;
        adjustListenerBuffers();
    }

    public void resetListenerBuffers() {
        this.headerBufferMultiplier = HEADER_BUFFER_DEFAULT;
        adjustListenerBuffers();
    }

    public void adjustListenerBuffers() {
        // configure the listeners to have larger buffers. We do this because we've seen cases where the header is
        // too large. Normally this would happen on "meaningless" JS includes for ad networks, but we eventually saw
        // it in a way that caused a Selenium script not to work due to too many headers (see tom.schwenk@musictoday.com)
        HttpListener[] listeners = jettyServer.getListeners();
        for (HttpListener listener : listeners) {
            if (listener instanceof SocketListener) {
                SocketListener sl = (SocketListener) listener;
                if (sl.getBufferReserve() != 512 * headerBufferMultiplier) {
                    sl.setBufferReserve(512 * headerBufferMultiplier);
                }

                if (sl.getBufferSize() != 8192 * headerBufferMultiplier) {
                    sl.setBufferSize(8192 * headerBufferMultiplier);
                }
            }
        }
    }

    public void setHttpClient(BrowserMobHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    // BEGIN ODO CHANGES
    /**
     * Cleanup function to remove all allocated listeners
     */
    public void cleanup() {
        synchronized (_sslMap) {
            for (SslRelayOdo relay : _sslMap.values()) {
                if (relay.getHttpServer() != null && relay.isStarted()) {
                    relay.getHttpServer().removeListener(relay);
                }
            }

            sslRelays.clear();
        }
    }
    // END ODO CHANGES
    
    /* Commenting out replaced code
    public void cleanup() {
        synchronized (sslRelays) {
            for (SslRelay relay : sslRelays) {
                if (relay.getHttpServer() != null && relay.isStarted()) {
                    relay.getHttpServer().removeListener(relay);
                }
            }

            sslRelays.clear();
        }
    }
    */
    
    // BEGIN ODO CHANGES
    // Copied from SeleniumProxyHandler(renamed to SslRelayOdo from SslRelay; no other changes)
    public static class SslRelayOdo extends SslListener
    {
        InetAddrPort _addr;
        File nukeDirOrFile;
        private static final long serialVersionUID = 1L;

        SslRelayOdo(InetAddrPort addr)
        {
            _addr=addr;
        }

        public void setNukeDirOrFile(File nukeDirOrFile) {
            this.nukeDirOrFile = nukeDirOrFile;
        }

        protected void customizeRequest(Socket socket, HttpRequest request)
        {
            super.customizeRequest(socket,request);
            URI uri=request.getURI();

            // Convert the URI to a proxy URL
            //
            // NOTE: Don't just add a host + port to the request URI, since this causes the URI to
            // get "dirty" and be rewritten, potentially breaking the proxy slightly. Instead,
            // create a brand new URI that includes the protocol, the host, and the port, but leaves
            // intact the path + query string "as is" so that it does not get rewritten.
            request.setURI(new URI("https://" + _addr.getHost() + ":" + _addr.getPort() + uri.toString()));
        }

        public void stop() throws InterruptedException {
            super.stop();

            if (nukeDirOrFile != null) {
                if (nukeDirOrFile.isDirectory()) {
                    LauncherUtils.recursivelyDeleteDir(nukeDirOrFile);
                } else {
                    nukeDirOrFile.delete();
                }
            }
        }
    }
    // END ODO CHANGES
}