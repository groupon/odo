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
package com.groupon.odo.tests;

import com.groupon.odo.proxylib.Constants;
import com.groupon.odo.proxylib.Utils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;

@SuppressWarnings("deprecation")
public class HttpUtils {
    public static String doProxyGet(String url, BasicNameValuePair[] data) throws Exception {
        String fullUrl = url;

        if (data != null) {
            if (data.length > 0) {
                fullUrl += "?";
            }

            for (BasicNameValuePair bnvp : data) {
                fullUrl += bnvp.getName() + "=" + uriEncode(bnvp.getValue()) + "&";
            }
        }

        HttpGet get = new HttpGet(fullUrl);
        int port = Utils.getSystemPort(Constants.SYS_HTTP_PORT);
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
            if (data.length > 0) {
                fullUrl += "?";
            }

            for (BasicNameValuePair bnvp : data) {
                fullUrl += bnvp.getName() + "=" + uriEncode(bnvp.getValue()) + "&";
            }
        }

        TrustManager[] trustAllCerts = new TrustManager[] {
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
        int port = Utils.getSystemPort(Constants.SYS_FWD_PORT);
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
