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
package com.groupon.odo.tests;

import com.groupon.odo.proxylib.Constants;
import com.groupon.odo.proxylib.Utils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

@SuppressWarnings("deprecation")
public class HttpUtils {
    public static String doProxyGet(String url, BasicNameValuePair[] data) throws Exception {
        String fullUrl = url;

        if (data != null) {
            if (data.length > 0)
                fullUrl += "?";

            for (BasicNameValuePair bnvp : data) {
                fullUrl += bnvp.getName() + "=" + uriEncode(bnvp.getValue()) + "&";
            }
        }

        HttpGet get = new HttpGet(fullUrl);
        int port = Utils.GetSystemPort(Constants.SYS_HTTP_PORT);
        HttpHost proxy = new HttpHost("localhost", port);
        HttpClient client = new org.apache.http.impl.client.DefaultHttpClient();
        client.getParams().setParameter(org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY, proxy);
        HttpResponse response = client.execute(get);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String accumulator = "";
        String line = "";
        Boolean firstLine = true;
        while ((line = rd.readLine()) != null) {
            accumulator += line;
            if (!firstLine) {
                accumulator += "\n";
            } else {
                firstLine = false;
            }
        }
        return accumulator;
    }

    public static String doProxyHttpsGet(String url, BasicNameValuePair[] data) throws Exception {
        String fullUrl = url;

        if (data != null) {
            if (data.length > 0)
                fullUrl += "?";

            for (BasicNameValuePair bnvp : data) {
                fullUrl += bnvp.getName() + "=" + uriEncode(bnvp.getValue()) + "&";
            }
        }

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        URL uri = new URL(fullUrl);
        int port = Utils.GetSystemPort(Constants.SYS_FWD_PORT);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", port));
        URLConnection connection = uri.openConnection(proxy);


        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String accumulator = "";
        String line = "";
        Boolean firstLine = true;
        while ((line = rd.readLine()) != null) {
            accumulator += line;
            if (!firstLine) {
                accumulator += "\n";
            } else {
                firstLine = false;
            }
        }

        return accumulator;
    }

    public static String uriEncode(String input) throws Exception {
        return URLEncoder.encode(input, "UTF-8").replace("+", "%20");
    }

    static {
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier() {

                    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                        if (hostname.equals("localhost")) {
                            return true;
                        }
                        return false;
                    }
                }
        );
    }

}
