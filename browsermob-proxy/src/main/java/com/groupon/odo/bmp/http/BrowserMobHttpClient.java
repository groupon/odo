/*
SOURCE: https://raw.github.com/lightbody/browsermob-proxy/fe022160af49995c5ac9d3f6ed39c0ba7b5da281/src/main/java/net/lightbody/bmp/proxy/http/BrowserMobHttpClient.java

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

package com.groupon.odo.bmp.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarCookie;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarNameVersion;
import net.lightbody.bmp.core.har.HarPostData;
import net.lightbody.bmp.core.har.HarPostDataParam;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;
import net.lightbody.bmp.core.har.HarTimings;
import net.lightbody.bmp.proxy.http.AllowAllHostnameVerifier;
import net.lightbody.bmp.proxy.http.BadURIException;
import net.lightbody.bmp.proxy.http.BlankCookieStore;
import net.lightbody.bmp.proxy.http.BrowserMobHttpResponse;
import net.lightbody.bmp.proxy.http.HttpDeleteWithBody;
import net.lightbody.bmp.proxy.http.RequestCallback;
import net.lightbody.bmp.proxy.http.RequestInfo;
import net.lightbody.bmp.proxy.http.ResponseInterceptor;
import net.lightbody.bmp.proxy.http.WildcardMatchingCredentialsProvider;
import net.lightbody.bmp.proxy.util.Base64;
import net.lightbody.bmp.proxy.util.CappedByteArrayOutputStream;
import net.lightbody.bmp.proxy.util.ClonedOutputStream;
import net.lightbody.bmp.proxy.util.IOUtils;
import net.lightbody.bmp.proxy.util.Log;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.java_bandwidthlimiter.StreamManager;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;

@SuppressWarnings("deprecation")
public class BrowserMobHttpClient {
    private static final Log LOG = new Log();
    public static UserAgentStringParser PARSER = UADetectorServiceFactory.getCachingAndUpdatingParser();

    private static final int BUFFER = 4096;

    private Har har;
    private String harPageRef;

    private boolean captureHeaders;
    private boolean captureContent;
    // if captureContent is set, default policy is to capture binary contents too
    private boolean captureBinaryContent = true;

    private SimulatedSocketFactory socketFactory;
    private TrustingSSLSocketFactory sslSocketFactory;
    private ThreadSafeClientConnManager httpClientConnMgr;
    private DefaultHttpClient httpClient;
    private List<BlacklistEntry> blacklistEntries = new CopyOnWriteArrayList<BrowserMobHttpClient.BlacklistEntry>();
    private WhitelistEntry whitelistEntry = null;
    private List<RewriteRule> rewriteRules = new CopyOnWriteArrayList<RewriteRule>();
    private List<RequestInterceptor> requestInterceptors = new CopyOnWriteArrayList<RequestInterceptor>();
    private List<ResponseInterceptor> responseInterceptors = new CopyOnWriteArrayList<ResponseInterceptor>();
    private HashMap<String, String> additionalHeaders = new LinkedHashMap<String, String>();
    private int requestTimeout;
    private AtomicBoolean allowNewRequests = new AtomicBoolean(true);
    private BrowserMobHostNameResolver hostNameResolver;
    private boolean decompress = true;
    // not using CopyOnWriteArray because we're WRITE heavy and it is for READ heavy operations
    // instead doing it the old fashioned way with a synchronized block
    private final Set<ActiveRequest> activeRequests = new HashSet<ActiveRequest>();
    private WildcardMatchingCredentialsProvider credsProvider;
    private boolean shutdown = false;
    private AuthType authType;

    private boolean followRedirects = true;
    private static final int MAX_REDIRECT = 10;
    private AtomicInteger requestCounter;

    public BrowserMobHttpClient(StreamManager streamManager, AtomicInteger requestCounter) {
        this.requestCounter = requestCounter;
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        hostNameResolver = new BrowserMobHostNameResolver(new Cache(DClass.ANY));

        this.socketFactory = new SimulatedSocketFactory(hostNameResolver, streamManager);
        this.sslSocketFactory = new TrustingSSLSocketFactory(hostNameResolver, streamManager);

        this.sslSocketFactory.setHostnameVerifier(new AllowAllHostnameVerifier());

        schemeRegistry.register(new Scheme("http", 80, socketFactory));
        schemeRegistry.register(new Scheme("https", 443, sslSocketFactory));

        httpClientConnMgr = new ThreadSafeClientConnManager(schemeRegistry) {
            @Override
            public ClientConnectionRequest requestConnection(HttpRoute route, Object state) {
                final ClientConnectionRequest wrapped = super.requestConnection(route, state);
                return new ClientConnectionRequest() {
                    @Override
                    public ManagedClientConnection getConnection(long timeout, TimeUnit tunit) throws InterruptedException, ConnectionPoolTimeoutException {
                        Date start = new Date();
                        try {
                            return wrapped.getConnection(timeout, tunit);
                        } finally {
                            RequestInfo.get().blocked(start, new Date());
                        }
                    }

                    @Override
                    public void abortRequest() {
                        wrapped.abortRequest();
                    }
                };
            }
        };

        // MOB-338: 30 total connections and 6 connections per host matches the behavior in Firefox 3
        httpClientConnMgr.setMaxTotal(30);
        httpClientConnMgr.setDefaultMaxPerRoute(6);

        httpClient = new DefaultHttpClient(httpClientConnMgr) {
            @Override
            protected HttpRequestExecutor createRequestExecutor() {
                return new HttpRequestExecutor() {
                    /*
                    @Override
                    protected HttpResponse doSendRequest(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
                        long requestHeadersSize = request.getRequestLine().toString().length() + 4;
                        long requestBodySize = 0;
                        for (Header header : request.getAllHeaders()) {
                            requestHeadersSize += header.toString().length() + 2;
                            if (header.getName().equals("Content-Length")) {
                                requestBodySize += Integer.valueOf(header.getValue());
                            }
                        }

                        HarEntry entry = RequestInfo.get().getEntry();
                        if (entry != null) {
                            entry.getRequest().setHeadersSize(requestHeadersSize);
                            entry.getRequest().setBodySize(requestBodySize);
                        }

                        Date start = new Date();
                        HttpResponse response = super.doSendRequest(request, conn, context);
                        RequestInfo.get().send(start, new Date());
                        return response;
                    }

                    @Override
                    protected HttpResponse doReceiveResponse(HttpRequest request, HttpClientConnection conn, HttpContext context) throws HttpException, IOException {
                        Date start = new Date();
                        HttpResponse response = super.doReceiveResponse(request, conn, context);
                        long responseHeadersSize = response.getStatusLine().toString().length() + 4;
                        for (Header header : response.getAllHeaders()) {
                            responseHeadersSize += header.toString().length() + 2;
                        }

                        HarEntry entry = RequestInfo.get().getEntry();
                        if (entry != null) {
                            entry.getResponse().setHeadersSize(responseHeadersSize);
                        }

                        RequestInfo.get().wait(start, new Date());
                        return response;
                    }
                    */
                };
            }
        };
        credsProvider = new WildcardMatchingCredentialsProvider();
        httpClient.setCredentialsProvider(credsProvider);
        httpClient.addRequestInterceptor(new PreemptiveAuth(), 0);
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        httpClient.getParams().setParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, Boolean.TRUE);
        setRetryCount(0);

        // we always set this to false so it can be handled manually:
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);

        HttpClientInterrupter.watch(this);
        setConnectionTimeout(60000);
        setSocketOperationTimeout(60000);
        setRequestTimeout(-1);
    }

    public void setRetryCount(int count) {
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(count, false));
    }

    public void remapHost(String source, String target) {
        hostNameResolver.remap(source, target);
    }

    @Deprecated
    public void addRequestInterceptor(HttpRequestInterceptor i) {
        httpClient.addRequestInterceptor(i);
    }

    public void addRequestInterceptor(RequestInterceptor interceptor) {
        requestInterceptors.add(interceptor);
    }

    @Deprecated
    public void addResponseInterceptor(HttpResponseInterceptor i) {
        httpClient.addResponseInterceptor(i);
    }

    public void addResponseInterceptor(ResponseInterceptor interceptor) {
        responseInterceptors.add(interceptor);
    }

    public void createCookie(String name, String value, String domain) {
        createCookie(name, value, domain, null);
    }

    public void createCookie(String name, String value, String domain, String path) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(domain);
        if (path != null) {
            cookie.setPath(path);
        }
        httpClient.getCookieStore().addCookie(cookie);
    }

    public void clearCookies() {
        httpClient.getCookieStore().clear();
    }

    public Cookie getCookie(String name) {
        return getCookie(name, null, null);
    }

    public Cookie getCookie(String name, String domain) {
        return getCookie(name, domain, null);
    }

    public Cookie getCookie(String name, String domain, String path) {
        for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
            if (cookie.getName().equals(name)) {
                if (domain != null && !domain.equals(cookie.getDomain())) {
                    continue;
                }
                if (path != null && !path.equals(cookie.getPath())) {
                    continue;
                }

                return cookie;
            }
        }

        return null;
    }

    public BrowserMobHttpRequest newPost(String url, net.lightbody.bmp.proxy.jetty.http.HttpRequest proxyRequest) {
        try {
            URI uri = makeUri(url);
            return new BrowserMobHttpRequest(new HttpPost(uri), this, -1, captureContent, proxyRequest);
        } catch (URISyntaxException e) {
            throw reportBadURI(url, "POST");
        }
    }

    public BrowserMobHttpRequest newGet(String url, net.lightbody.bmp.proxy.jetty.http.HttpRequest proxyRequest) {
        try {
            URI uri = makeUri(url);
            return new BrowserMobHttpRequest(new HttpGet(uri), this, -1, captureContent, proxyRequest);
        } catch (URISyntaxException e) {
            throw reportBadURI(url, "GET");
        }
    }

    public BrowserMobHttpRequest newPut(String url, net.lightbody.bmp.proxy.jetty.http.HttpRequest proxyRequest) {
        try {
            URI uri = makeUri(url);
            return new BrowserMobHttpRequest(new HttpPut(uri), this, -1, captureContent, proxyRequest);
        } catch (Exception e) {
            throw reportBadURI(url, "PUT");
        }
    }

    public BrowserMobHttpRequest newDelete(String url, net.lightbody.bmp.proxy.jetty.http.HttpRequest proxyRequest) {
        try {
            URI uri = makeUri(url);
            return new BrowserMobHttpRequest(new HttpDeleteWithBody(uri), this, -1, captureContent, proxyRequest);
        } catch (URISyntaxException e) {
            throw reportBadURI(url, "DELETE");
        }
    }

    public BrowserMobHttpRequest newOptions(String url, net.lightbody.bmp.proxy.jetty.http.HttpRequest proxyRequest) {
        try {
            URI uri = makeUri(url);
            return new BrowserMobHttpRequest(new HttpOptions(uri), this, -1, captureContent, proxyRequest);
        } catch (URISyntaxException e) {
            throw reportBadURI(url, "OPTIONS");
        }
    }

    public BrowserMobHttpRequest newHead(String url, net.lightbody.bmp.proxy.jetty.http.HttpRequest proxyRequest) {
        try {
            URI uri = makeUri(url);
            return new BrowserMobHttpRequest(new HttpHead(uri), this, -1, captureContent, proxyRequest);
        } catch (URISyntaxException e) {
            throw reportBadURI(url, "HEAD");
        }
    }

    private URI makeUri(String url) throws URISyntaxException {
        // MOB-120: check for | character and change to correctly escaped %7C
        url = url.replace(" ", "%20");
        url = url.replace(">", "%3C");
        url = url.replace("<", "%3E");
        url = url.replace("#", "%23");
        url = url.replace("{", "%7B");
        url = url.replace("}", "%7D");
        url = url.replace("|", "%7C");
        url = url.replace("\\", "%5C");
        url = url.replace("^", "%5E");
        url = url.replace("~", "%7E");
        url = url.replace("[", "%5B");
        url = url.replace("]", "%5D");
        url = url.replace("`", "%60");
        url = url.replace("\"", "%22");

        URI uri = new URI(url);

        // are we using the default ports for http/https? if so, let's rewrite the URI to make sure the :80 or :443
        // is NOT included in the string form the URI. The reason we do this is that in HttpClient 4.0 the Host header
        // would include a value such as "yahoo.com:80" rather than "yahoo.com". Not sure why this happens but we don't
        // want it to, and rewriting the URI solves it
        if ((uri.getPort() == 80 && "http".equals(uri.getScheme()))
            || (uri.getPort() == 443 && "https".equals(uri.getScheme()))) {
            // we rewrite the URL with a StringBuilder (vs passing in the components of the URI) because if we were
            // to pass in these components using the URI's 7-arg constructor query parameters get double escaped (bad!)
            StringBuilder sb = new StringBuilder(uri.getScheme()).append("://");
            if (uri.getRawUserInfo() != null) {
                sb.append(uri.getRawUserInfo()).append("@");
            }
            sb.append(uri.getHost());
            if (uri.getRawPath() != null) {
                sb.append(uri.getRawPath());
            }
            if (uri.getRawQuery() != null) {
                sb.append("?").append(uri.getRawQuery());
            }
            if (uri.getRawFragment() != null) {
                sb.append("#").append(uri.getRawFragment());
            }

            uri = new URI(sb.toString());
        }
        return uri;
    }

    private RuntimeException reportBadURI(String url, String method) {
        if (this.har != null && harPageRef != null) {
            HarEntry entry = new HarEntry(harPageRef);
            entry.setTime(0);
            entry.setRequest(new HarRequest(method, url, "HTTP/1.1"));
            entry.setResponse(new HarResponse(-998, "Bad URI", "HTTP/1.1"));
            entry.setTimings(new HarTimings());
            har.getLog().addEntry(entry);
        }

        throw new BadURIException("Bad URI requested: " + url);
    }

    public void checkTimeout() {
        synchronized (activeRequests) {
            for (ActiveRequest activeRequest : activeRequests) {
                activeRequest.checkTimeout();
            }
        }
    }

    public BrowserMobHttpResponse execute(BrowserMobHttpRequest req) {
        if (!allowNewRequests.get()) {
            throw new RuntimeException("No more requests allowed");
        }

        try {
            requestCounter.incrementAndGet();

            for (RequestInterceptor interceptor : requestInterceptors) {
                interceptor.process(req, har);
            }

            BrowserMobHttpResponse response = execute(req, 1);
            for (ResponseInterceptor interceptor : responseInterceptors) {
                interceptor.process(response, har);
            }

            return response;
        } finally {
            requestCounter.decrementAndGet();
        }
    }

    //
    //If we were making cake, this would be the filling :)
    //
    private BrowserMobHttpResponse execute(BrowserMobHttpRequest req, int depth) {
        if (depth >= MAX_REDIRECT) {
            throw new IllegalStateException("Max number of redirects (" + MAX_REDIRECT + ") reached");
        }

        RequestCallback callback = req.getRequestCallback();

        HttpRequestBase method = req.getMethod();
        String verificationText = req.getVerificationText();
        String url = method.getURI().toString();

        // save the browser and version if it's not yet been set
        if (har != null && har.getLog().getBrowser() == null) {
            Header[] uaHeaders = method.getHeaders("User-Agent");
            if (uaHeaders != null && uaHeaders.length > 0) {
                String userAgent = uaHeaders[0].getValue();
                try {
                    // note: this doesn't work for 'Fandango/4.5.1 CFNetwork/548.1.4 Darwin/11.0.0'
                    ReadableUserAgent uai = PARSER.parse(userAgent);
                    String browser = uai.getName();
                    String version = uai.getVersionNumber().toVersionString();
                    har.getLog().setBrowser(new HarNameVersion(browser, version));
                } catch (Exception e) {
                    LOG.warn("Failed to parse user agent string", e);
                }
            }
        }

        // process any rewrite requests
        boolean rewrote = false;
        String newUrl = url;
        for (RewriteRule rule : rewriteRules) {
            Matcher matcher = rule.match.matcher(newUrl);
            newUrl = matcher.replaceAll(rule.replace);
            rewrote = true;
        }

        if (rewrote) {
            try {
                method.setURI(new URI(newUrl));
                url = newUrl;
            } catch (URISyntaxException e) {
                LOG.warn("Could not rewrite url to %s", newUrl);
            }
        }

        // handle whitelist and blacklist entries
        int mockResponseCode = -1;
        synchronized (this) {
            // guard against concurrent modification of whitelistEntry
            if (whitelistEntry != null) {
                boolean found = false;
                for (Pattern pattern : whitelistEntry.patterns) {
                    if (pattern.matcher(url).matches()) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    mockResponseCode = whitelistEntry.responseCode;
                }
            }
        }

        if (blacklistEntries != null) {
            for (BlacklistEntry blacklistEntry : blacklistEntries) {
                if (blacklistEntry.pattern.matcher(url).matches()) {
                    mockResponseCode = blacklistEntry.responseCode;
                    break;
                }
            }
        }

        if (!additionalHeaders.isEmpty()) {
            // Set the additional headers
            for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                method.removeHeaders(key);
                method.addHeader(key, value);
            }
        }

        String charSet = "UTF-8";
        String responseBody = null;

        InputStream is = null;
        int statusCode = -998;
        long bytes = 0;
        boolean gzipping = false;
        boolean contentMatched = true;
        OutputStream os = req.getOutputStream();
        if (os == null) {
            os = new CappedByteArrayOutputStream(1024 * 1024); // MOB-216 don't buffer more than 1 MB
        }
        if (verificationText != null) {
            contentMatched = false;
        }

        // link the object up now, before we make the request, so that if we get cut off (ie: favicon.ico request and browser shuts down)
        // we still have the attempt associated, even if we never got a response
        HarEntry entry = new HarEntry(harPageRef);

        // clear out any connection-related information so that it's not stale from previous use of this thread.
        RequestInfo.clear(url, entry);

        entry.setRequest(new HarRequest(method.getMethod(), url, method.getProtocolVersion().getProtocol()));
        entry.setResponse(new HarResponse(-999, "NO RESPONSE", method.getProtocolVersion().getProtocol()));
        if (this.har != null && harPageRef != null) {
            har.getLog().addEntry(entry);
        }

        String query = method.getURI().getRawQuery();
        if (query != null) {
            MultiMap<String> params = new MultiMap<String>();
            UrlEncoded.decodeTo(query, params, "UTF-8");
            for (String k : params.keySet()) {
                for (Object v : params.getValues(k)) {
                    entry.getRequest().getQueryString().add(new HarNameValuePair(k, (String) v));
                }
            }
        }

        String errorMessage = null;
        HttpResponse response = null;

        BasicHttpContext ctx = new BasicHttpContext();

        ActiveRequest activeRequest = new ActiveRequest(method, ctx, entry.getStartedDateTime());
        synchronized (activeRequests) {
            activeRequests.add(activeRequest);
        }

        // for dealing with automatic authentication
        if (authType == AuthType.NTLM) {
            // todo: not supported yet
            //ctx.setAttribute("preemptive-auth", new NTLMScheme(new JCIFSEngine()));
        } else if (authType == AuthType.BASIC) {
            ctx.setAttribute("preemptive-auth", new BasicScheme());
        }

        StatusLine statusLine = null;
        try {
            // set the User-Agent if it's not already set
            if (method.getHeaders("User-Agent").length == 0) {
                method.addHeader("User-Agent", "BrowserMob VU/1.0");
            }

            // was the request mocked out?
            if (mockResponseCode != -1) {
                statusCode = mockResponseCode;

                // TODO: HACKY!!
                callback.handleHeaders(new Header[] {
                    new Header() {
                        @Override
                        public String getName() {
                            return "Content-Type";
                        }

                        @Override
                        public String getValue() {
                            return "text/plain";
                        }

                        @Override
                        public HeaderElement[] getElements() throws ParseException {
                            return new HeaderElement[0];
                        }
                    }
                });
                // Make sure we set the status line here too.
                // Use the version number from the request
                ProtocolVersion version = null;
                int reqDotVersion = req.getProxyRequest().getDotVersion();
                if (reqDotVersion == -1) {
                    version = new HttpVersion(0, 9);
                } else if (reqDotVersion == 0) {
                    version = new HttpVersion(1, 0);
                } else if (reqDotVersion == 1) {
                    version = new HttpVersion(1, 1);
                }
                // and if not any of these, trust that a Null version will
                // cause an appropriate error
                callback.handleStatusLine(new BasicStatusLine(version, statusCode, "Status set by browsermob-proxy"));
                // No mechanism to look up the response text by status code,
                // so include a notification that this is a synthetic error code.
            } else {
                response = httpClient.execute(method, ctx);
                statusLine = response.getStatusLine();
                statusCode = statusLine.getStatusCode();

                if (callback != null) {
                    callback.handleStatusLine(statusLine);
                    callback.handleHeaders(response.getAllHeaders());
                }

                if (response.getEntity() != null) {
                    is = response.getEntity().getContent();
                }

                // check for null (resp 204 can cause HttpClient to return null, which is what Google does with http://clients1.google.com/generate_204)
                if (is != null) {
                    Header contentEncodingHeader = response.getFirstHeader("Content-Encoding");
                    if (contentEncodingHeader != null && "gzip".equalsIgnoreCase(contentEncodingHeader.getValue())) {
                        gzipping = true;
                    }

                    // deal with GZIP content!
                    if (decompress && gzipping) {
                        is = new GZIPInputStream(is);
                    }

                    if (captureContent) {
                        // todo - something here?
                        os = new ClonedOutputStream(os);
                    }

                    bytes = copyWithStats(is, os);
                }
            }
        } catch (Exception e) {
            errorMessage = e.toString();

            if (callback != null) {
                callback.reportError(e);
            }

            // only log it if we're not shutdown (otherwise, errors that happen during a shutdown can likely be ignored)
            if (!shutdown) {
                LOG.info(String.format("%s when requesting %s", errorMessage, url));
            }
        } finally {
            // the request is done, get it out of here
            synchronized (activeRequests) {
                activeRequests.remove(activeRequest);
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // this is OK to ignore
                }
            }
        }

        // record the response as ended
        RequestInfo.get().finish();

        // set the start time and other timings
        entry.setStartedDateTime(RequestInfo.get().getStart());
        entry.setTimings(RequestInfo.get().getTimings());
        entry.setServerIPAddress(RequestInfo.get().getResolvedAddress());
        entry.setTime(RequestInfo.get().getTotalTime());

        // todo: where you store this in HAR?
        // obj.setErrorMessage(errorMessage);
        entry.getResponse().setBodySize(bytes);
        entry.getResponse().getContent().setSize(bytes);
        entry.getResponse().setStatus(statusCode);
        if (statusLine != null) {
            entry.getResponse().setStatusText(statusLine.getReasonPhrase());
        }

        boolean urlEncoded = false;
        if (captureHeaders || captureContent) {
            for (Header header : method.getAllHeaders()) {
                if (header.getValue() != null && header.getValue().startsWith(URLEncodedUtils.CONTENT_TYPE)) {
                    urlEncoded = true;
                }

                entry.getRequest().getHeaders().add(new HarNameValuePair(header.getName(), header.getValue()));
            }

            if (response != null) {
                for (Header header : response.getAllHeaders()) {
                    entry.getResponse().getHeaders().add(new HarNameValuePair(header.getName(), header.getValue()));
                }
            }
        }

        if (captureContent) {
            // can we understand the POST data at all?
            if (method instanceof HttpEntityEnclosingRequestBase && req.getCopy() != null) {
                HttpEntityEnclosingRequestBase enclosingReq = (HttpEntityEnclosingRequestBase) method;
                HttpEntity entity = enclosingReq.getEntity();

                HarPostData data = new HarPostData();
                data.setMimeType(req.getMethod().getFirstHeader("Content-Type").getValue());
                entry.getRequest().setPostData(data);

                if (urlEncoded || URLEncodedUtils.isEncoded(entity)) {
                    try {
                        final String content = new String(req.getCopy().toByteArray(), "UTF-8");
                        if (content != null && content.length() > 0) {
                            List<NameValuePair> result = new ArrayList<NameValuePair>();
                            URLEncodedUtils.parse(result, new Scanner(content), null);

                            ArrayList<HarPostDataParam> params = new ArrayList<HarPostDataParam>();
                            data.setParams(params);

                            for (NameValuePair pair : result) {
                                params.add(new HarPostDataParam(pair.getName(), pair.getValue()));
                            }
                        }
                    } catch (Exception e) {
                        LOG.info("Unexpected problem when parsing input copy", e);
                    }
                } else {
                    // not URL encoded, so let's grab the body of the POST and capture that
                    data.setText(new String(req.getCopy().toByteArray()));
                }
            }
        }

        //capture request cookies
        javax.servlet.http.Cookie[] cookies = req.getProxyRequest().getCookies();
        for (javax.servlet.http.Cookie cookie : cookies) {
            HarCookie hc = new HarCookie();
            hc.setName(cookie.getName());
            hc.setValue(cookie.getValue());
            entry.getRequest().getCookies().add(hc);
        }

        String contentType = null;

        if (response != null) {
            try {
                Header contentTypeHdr = response.getFirstHeader("Content-Type");
                if (contentTypeHdr != null) {
                    contentType = contentTypeHdr.getValue();
                    entry.getResponse().getContent().setMimeType(contentType);

                    if (captureContent && os != null && os instanceof ClonedOutputStream) {
                        ByteArrayOutputStream copy = ((ClonedOutputStream) os).getOutput();

                        if (gzipping) {
                            // ok, we need to decompress it before we can put it in the har file
                            try {
                                InputStream temp = new GZIPInputStream(new ByteArrayInputStream(copy.toByteArray()));
                                copy = new ByteArrayOutputStream();
                                IOUtils.copy(temp, copy);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        if (hasTextualContent(contentType)) {
                            setTextOfEntry(entry, copy, contentType);
                        } else if (captureBinaryContent) {
                            setBinaryContentOfEntry(entry, copy);
                        }
                    }

                    NameValuePair nvp = contentTypeHdr.getElements()[0].getParameterByName("charset");

                    if (nvp != null) {
                        charSet = nvp.getValue();
                    }
                }

                if (os instanceof ByteArrayOutputStream) {
                    responseBody = ((ByteArrayOutputStream) os).toString(charSet);

                    if (verificationText != null) {
                        contentMatched = responseBody.contains(verificationText);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        if (contentType != null) {
            entry.getResponse().getContent().setMimeType(contentType);
        }

        // checking to see if the client is being redirected
        boolean isRedirect = false;

        String location = null;
        if (response != null && statusCode >= 300 && statusCode < 400 && statusCode != 304) {
            isRedirect = true;

            // pulling the header for the redirect
            Header locationHeader = response.getLastHeader("location");
            if (locationHeader != null) {
                location = locationHeader.getValue();
            } else if (this.followRedirects) {
                throw new RuntimeException("Invalid redirect - missing location header");
            }
        }

        //
        // Response validation - they only work if we're not following redirects
        //

        int expectedStatusCode = req.getExpectedStatusCode();

        // if we didn't mock out the actual response code and the expected code isn't what we saw, we have a problem
        if (mockResponseCode == -1 && expectedStatusCode > -1) {
            if (this.followRedirects) {
                throw new RuntimeException("Response validation cannot be used while following redirects");
            }
            if (expectedStatusCode != statusCode) {
                if (isRedirect) {
                    throw new RuntimeException("Expected status code of " + expectedStatusCode + " but saw " + statusCode
                                                   + " redirecting to: " + location);
                } else {
                    throw new RuntimeException("Expected status code of " + expectedStatusCode + " but saw " + statusCode);
                }
            }
        }

        // Location header check:
        if (isRedirect && (req.getExpectedLocation() != null)) {
            if (this.followRedirects) {
                throw new RuntimeException("Response validation cannot be used while following redirects");
            }

            if (location.compareTo(req.getExpectedLocation()) != 0) {
                throw new RuntimeException("Expected a redirect to  " + req.getExpectedLocation() + " but saw " + location);
            }
        }

        // end of validation logic

        // basic tail recursion for redirect handling
        if (isRedirect && this.followRedirects) {
            // updating location:
            try {
                URI redirectUri = new URI(location);
                URI newUri = method.getURI().resolve(redirectUri);
                method.setURI(newUri);

                return execute(req, ++depth);
            } catch (URISyntaxException e) {
                LOG.warn("Could not parse URL", e);
            }
        }

        return new BrowserMobHttpResponse(entry, method, response, contentMatched, verificationText, errorMessage, responseBody, contentType, charSet);
    }

    private boolean hasTextualContent(String contentType) {
        if (StringUtils.isNotBlank(contentType)) {
            return contentType.startsWith("text/") ||
                contentType.startsWith("application/x-javascript") ||
                contentType.startsWith("application/javascript") ||
                contentType.startsWith("application/json") ||
                contentType.startsWith("application/xml") ||
                contentType.startsWith("application/xhtml+xml");
        }

        return false;
    }

    private void setBinaryContentOfEntry(HarEntry entry,
                                         ByteArrayOutputStream copy) {
        entry.getResponse().getContent().setText(Base64.byteArrayToBase64(copy.toByteArray()));
    }

    private void setTextOfEntry(HarEntry entry,
                                ByteArrayOutputStream copy, String contentType) {
        ContentType contentTypeCharset = ContentType.parse(contentType);
        Charset charset = contentTypeCharset.getCharset();
        if (charset != null) {
            entry.getResponse().getContent().setText(new String(copy.toByteArray(), charset));
        } else {
            entry.getResponse().getContent().setText(new String(copy.toByteArray()));
        }
    }

    public void shutdown() {
        shutdown = true;
        abortActiveRequests();
        rewriteRules.clear();
        blacklistEntries.clear();
        credsProvider.clear();
        httpClientConnMgr.shutdown();
        HttpClientInterrupter.release(this);
    }

    public void abortActiveRequests() {
        allowNewRequests.set(true);

        synchronized (activeRequests) {
            for (ActiveRequest activeRequest : activeRequests) {
                activeRequest.abort();
            }
            activeRequests.clear();
        }
    }

    public void setHar(Har har) {
        this.har = har;
    }

    public void setHarPageRef(String harPageRef) {
        this.harPageRef = harPageRef;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public void setSocketOperationTimeout(int readTimeout) {
        httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);
    }

    public void setConnectionTimeout(int connectionTimeout) {
        httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void autoBasicAuthorization(String domain, String username, String password) {
        authType = AuthType.BASIC;
        httpClient.getCredentialsProvider().setCredentials(
            new AuthScope(domain, -1),
            new UsernamePasswordCredentials(username, password));
    }

    public void autoNTLMAuthorization(String domain, String username, String password) {
        authType = AuthType.NTLM;
        httpClient.getCredentialsProvider().setCredentials(
            new AuthScope(domain, -1),
            new NTCredentials(username, password, "workstation", domain));
    }

    public void rewriteUrl(String match, String replace) {
        rewriteRules.add(new RewriteRule(match, replace));
    }

    public void clearRewriteRules() {
        rewriteRules.clear();
    }

    // this method is provided for backwards compatibility before we renamed it to
    // blacklistRequests (note the plural)
    public void blacklistRequest(String pattern, int responseCode) {
        blacklistRequests(pattern, responseCode);
    }

    public void blacklistRequests(String pattern, int responseCode) {
        blacklistEntries.add(new BlacklistEntry(pattern, responseCode));
    }

    public void clearBlacklist() {
        blacklistEntries.clear();
    }

    public synchronized void whitelistRequests(String[] patterns, int responseCode) {
        // synchronized to guard against concurrent modification
        whitelistEntry = new WhitelistEntry(patterns, responseCode);
    }

    public synchronized void clearWhitelist() {
        // synchronized to guard against concurrent modification
        whitelistEntry = null;
    }

    public void addHeader(String name, String value) {
        additionalHeaders.put(name, value);
    }

    public void prepareForBrowser() {
        // Clear cookies, let the browser handle them
        httpClient.setCookieStore(new BlankCookieStore());
        httpClient.getCookieSpecs().register("easy", new CookieSpecFactory() {
            @Override
            public CookieSpec newInstance(HttpParams params) {
                return new BrowserCompatSpec() {
                    @Override
                    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
                        // easy!
                    }
                };
            }
        });
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, "easy");
        decompress = false;
        setFollowRedirects(false);
    }

    public String remappedHost(String host) {
        return hostNameResolver.remapping(host);
    }

    public List<String> originalHosts(String host) {
        return hostNameResolver.original(host);
    }

    public Har getHar() {
        return har;
    }

    public void setCaptureHeaders(boolean captureHeaders) {
        this.captureHeaders = captureHeaders;
    }

    public void setCaptureContent(boolean captureContent) {
        this.captureContent = captureContent;
    }

    public void setCaptureBinaryContent(boolean captureBinaryContent) {
        this.captureBinaryContent = captureBinaryContent;
    }

    public void setHttpProxy(String httpProxy) {
        String host = httpProxy.split(":")[0];
        Integer port = Integer.parseInt(httpProxy.split(":")[1]);
        HttpHost proxy = new HttpHost(host, port);
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    static class PreemptiveAuth implements HttpRequestInterceptor {
        public void process(
            final HttpRequest request,
            final HttpContext context) throws HttpException, IOException {

            AuthState authState = (AuthState) context.getAttribute(
                ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute(
                    "preemptive-auth");
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                    ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(
                    ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(
                        new AuthScope(
                            targetHost.getHostName(),
                            targetHost.getPort()));
                    if (creds != null) {
                        authState.setAuthScheme(authScheme);
                        authState.setCredentials(creds);
                    }
                }
            }
        }
    }

    class ActiveRequest {
        HttpRequestBase request;
        BasicHttpContext ctx;
        Date start;

        ActiveRequest(HttpRequestBase request, BasicHttpContext ctx, Date start) {
            this.request = request;
            this.ctx = ctx;
            this.start = start;
        }

        void checkTimeout() {
            if (requestTimeout != -1) {
                if (request != null && start != null && new Date(System.currentTimeMillis() - requestTimeout).after(start)) {
                    LOG.info("Aborting request to %s after it failed to complete in %d ms", request.getURI().toString(), requestTimeout);

                    abort();
                }
            }
        }

        public void abort() {
            request.abort();

            // try to close the connection? is this necessary? unclear based on preliminary debugging of HttpClient, but
            // it doesn't seem to hurt to try
            HttpConnection conn = (HttpConnection) ctx.getAttribute("http.connection");
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException e) {
                    // this is fine, we're shutting it down anyway
                }
            }
        }
    }

    private class WhitelistEntry {
        private List<Pattern> patterns = new CopyOnWriteArrayList<Pattern>();
        private int responseCode;

        private WhitelistEntry(String[] patterns, int responseCode) {
            for (String pattern : patterns) {
                this.patterns.add(Pattern.compile(pattern));
            }
            this.responseCode = responseCode;
        }
    }

    private class BlacklistEntry {
        private Pattern pattern;
        private int responseCode;

        private BlacklistEntry(String pattern, int responseCode) {
            this.pattern = Pattern.compile(pattern);
            this.responseCode = responseCode;
        }
    }

    private class RewriteRule {
        private Pattern match;
        private String replace;

        private RewriteRule(String match, String replace) {
            this.match = Pattern.compile(match);
            this.replace = replace;
        }
    }

    private enum AuthType {
        NONE, BASIC, NTLM
    }

    public void clearDNSCache() {
        this.hostNameResolver.clearCache();
    }

    public void setDNSCacheTimeout(int timeout) {
        this.hostNameResolver.setCacheTimeout(timeout);
    }

    public static long copyWithStats(InputStream is, OutputStream os) throws IOException {
        long bytesCopied = 0;
        byte[] buffer = new byte[BUFFER];
        int length;

        try {
            // read the first byte
            int firstByte = is.read();

            if (firstByte == -1) {
                return 0;
            }

            os.write(firstByte);
            bytesCopied++;

            do {
                length = is.read(buffer, 0, BUFFER);
                if (length != -1) {
                    bytesCopied += length;
                    os.write(buffer, 0, length);
                    os.flush();
                }
            } while (length != -1);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ok to ignore
            }

            try {
                os.close();
            } catch (IOException e) {
                // ok to ignore
            }
        }

        return bytesCopied;
    }
}