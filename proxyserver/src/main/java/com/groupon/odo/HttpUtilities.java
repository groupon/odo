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
package com.groupon.odo;

import com.groupon.odo.plugin.PluginHelper;
import com.groupon.odo.proxylib.Constants;
import com.groupon.odo.proxylib.models.History;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.io.IOUtils;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpUtilities {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtilities.class);

    /**
     * Key for content type header.
     */
    public static final String STRING_CONTENT_TYPE_HEADER_NAME = "Content-Type";

    /**
     * Transfer Encoding header value
     */
    public static final String STRING_TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * MessagePack content type value
     */
    public static final String STRING_CONTENT_TYPE_MESSAGEPACK = "binary/messagepack";

    /**
     * Connection header value
     */
    public static final String STRING_CONNECTION = "Connection";

    /**
     * Chunked value
     */
    public static final String STRING_CHUNKED = "chunked";

    /**
     * Application JSON content type value
     */
    public static final String STRING_CONTENT_TYPE_JSON = "application/json";

    /**
     * Form encoded content type value
     */
    public static final String STRING_CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * @param url
     * @return
     */
    public static String getHostNameFromURL(String url) {
        int urlLeftPos = url.indexOf("//");
        String hostName = url.substring(urlLeftPos + 2);
        int urlRightPos = hostName.indexOf("/");
        if (urlRightPos != -1)
            hostName = hostName.substring(0, urlRightPos);
        // now look for a port
        int portPos = hostName.indexOf(":");
        if (portPos != -1)
            hostName = hostName.substring(0, portPos);

        return hostName;
    }
    
    public static int getPortFromURL(String url) {
        int urlLeftPos = url.indexOf("//");
        Boolean isHttps = url.startsWith("https");
        
        // set port defaults
        int port = 80;
        if (isHttps) {
        	port = 443;
        }
        
        String portStr = null;
        String hostName = url.substring(urlLeftPos + 2);
        int urlRightPos = hostName.indexOf("/");
        if (urlRightPos != -1)
            hostName = hostName.substring(0, urlRightPos);
        // now look for a port
        int portPos = hostName.indexOf(":");
        if (portPos != -1)
        	portStr = hostName.substring(portPos + 1, urlRightPos);

        if (portStr != null) {
        	port = Integer.parseInt(portStr);
        }
        
        return port;
    }
    
    public static String removePortFromHostHeaderString(String host) {
    	String hostName = host;
    	int portPos = host.indexOf(":");
        if (portPos != -1)
            hostName = host.substring(0, portPos);
        
        return hostName;
    }


    /**
     * Obtain collection of Parameters from request
     *
     * @param dataArray
     * @return
     * @throws Exception
     */
    public static Map<String, String[]> mapUrlEncodedParameters(byte[] dataArray) throws Exception {
        Map<String, String[]> mapPostParameters = new HashMap<String, String[]>();

        try {
            ByteArrayOutputStream byteout = new ByteArrayOutputStream();
            for (int x = 0; x < dataArray.length; x++) {
                // split the data up by & to get the parts
                if (dataArray[x] == '&' || x == (dataArray.length - 1)) {
                    if (x == (dataArray.length - 1)) {
                        byteout.write(dataArray[x]);
                    }
                    // find '=' and split the data up into key value pairs
                    int equalsPos = -1;
                    ByteArrayOutputStream key = new ByteArrayOutputStream();
                    ByteArrayOutputStream value = new ByteArrayOutputStream();
                    byte[] byteArray = byteout.toByteArray();
                    for (int xx = 0; xx < byteArray.length; xx++) {
                        if (byteArray[xx] == '=') {
                            equalsPos = xx;
                        } else {
                            if (equalsPos == -1) {
                                key.write(byteArray[xx]);
                            } else {
                                value.write(byteArray[xx]);
                            }
                        }
                    }

                    ArrayList<String> values = new ArrayList<String>();

                    if (mapPostParameters.containsKey(key.toString())) {
                        values = new ArrayList<String>(Arrays.asList(mapPostParameters.get(key.toString())));
                        mapPostParameters.remove(key.toString());
                    }

                    values.add(value.toString());

                    mapPostParameters.put(key.toString(), values.toArray(new String[values.size()]));

                    byteout = new ByteArrayOutputStream();
                } else {
                    byteout.write(dataArray[x]);
                }
            }
        } catch (Exception e) {
            throw new Exception("Could not parse request data: " + e.getMessage());
        }

        return mapPostParameters;
    }


    public static HttpServletResponse addHeader(HttpServletResponse response, Object[] headerPair) {
        // set header
        response.setHeader(headerPair[0].toString(), headerPair[1].toString());

        return response;
    }

    /*****
     * Pretty formatting methods for storing History
     */

    /**
     * Retrieve URL without parameters
     *
     * @param sourceURI
     * @return
     */
    public static String getURL(String sourceURI) {
        String retval = sourceURI;
        int qPos = sourceURI.indexOf("?");
        if (qPos != -1) {
            retval = retval.substring(0, qPos);
        }

        return retval;
    }

    /**
     * Obtain newline-delimited headers from method
     *
     * @param method
     * @return
     */
    public static String getHeaders(HttpMethod method) {
        String headerString = "";
        Header[] headers = method.getRequestHeaders();
        for (Header header : headers) {
            String name = header.getName();
            if (name.equals(Constants.ODO_PROXY_HEADER)) {
                // skip.. don't want to log this
                continue;
            }

            if (headerString.length() != 0)
                headerString += "\n";

            headerString += header.getName() + ": " + header.getValue();
        }

        return headerString;
    }

    /**
     * Obtain newline-delimited headers from request
     *
     * @param request
     * @return
     */
    public static String getHeaders(HttpServletRequest request) {
        String headerString = "";
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name.equals(Constants.ODO_PROXY_HEADER)) {
                // skip.. don't want to log this
                continue;
            }

            if (headerString.length() != 0)
                headerString += "\n";

            headerString += name + ": " + request.getHeader(name);
        }

        return headerString;
    }

    /**
     * Obtain newline-delimited headers from response
     *
     * @param response
     * @return
     */
    public static String getHeaders(HttpServletResponse response) {
        String headerString = "";
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            // there may be multiple headers per header name
            for (String headerValue: response.getHeaders(headerName)) {
                if (headerString.length() != 0)
                    headerString += "\n";

                headerString += headerName + ": " + headerValue;
            }
        }

        return headerString;
    }

    /**
     * Obtain parameters from query
     *
     * @param query
     * @return
     */
    public static HashMap<String, String> getParameters(String query) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (query == null || query.length() == 0) {
            return params;
        }

        String[] splitQuery = query.split("&");
        for (String splitItem : splitQuery) {
            String[] items = splitItem.split("=");

            if (items.length == 1) {
                params.put(items[0], "");
            } else {
                params.put(items[0], items[1]);
            }
        }

        return params;
    }


    /**
     * Sets up the given {@link org.apache.commons.httpclient.methods.PostMethod} to send the same multipart POST data
     * as was sent in the given {@link HttpServletRequest}
     *
     * @param postMethodProxyRequest The {@link org.apache.commons.httpclient.methods.PostMethod} that we are configuring to send a
     *                               multipart POST request
     * @param httpServletRequest     The {@link HttpServletRequest} that contains the multipart
     *                               POST data to be sent via the {@link org.apache.commons.httpclient.methods.PostMethod}
     */
    @SuppressWarnings("unchecked")
    public static void handleMultipartPost(
            EntityEnclosingMethod postMethodProxyRequest,
            HttpServletRequest httpServletRequest,
            DiskFileItemFactory diskFileItemFactory)
            throws ServletException {
        // TODO: this function doesn't set any history data
        try {
            // just pass back the binary data
            InputStreamRequestEntity ire = new InputStreamRequestEntity(httpServletRequest.getInputStream());
            postMethodProxyRequest.setRequestEntity(ire);
            postMethodProxyRequest.setRequestHeader(STRING_CONTENT_TYPE_HEADER_NAME, httpServletRequest.getHeader(STRING_CONTENT_TYPE_HEADER_NAME));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Sets up the given {@link org.apache.commons.httpclient.methods.PostMethod} to send the same standard POST data
     * as was sent in the given {@link HttpServletRequest}
     *
     * @param methodProxyRequest The {@link org.apache.commons.httpclient.methods.PostMethod} that we are configuring to send a
     *                           standard POST request
     * @param httpServletRequest The {@link HttpServletRequest} that contains the POST data to
     *                           be sent via the {@link org.apache.commons.httpclient.methods.PostMethod}
     * @param history            The {@link com.groupon.odo.proxylib.models.History} log for this request
     */
    @SuppressWarnings("unchecked")
    public static void handleStandardPost(EntityEnclosingMethod methodProxyRequest,
                                          HttpServletRequest httpServletRequest,
                                          History history) throws Exception {
        String deserialisedMessages = "";
        byte[] requestByteArray = null;
        // Create a new StringBuffer with the data to be passed
        StringBuilder requestBody = new StringBuilder();
        InputStream body = httpServletRequest.getInputStream();
        RequestEntity requestEntity = null;

        if (httpServletRequest.getContentType() != null &&
                httpServletRequest.getContentType().contains(STRING_CONTENT_TYPE_FORM_URLENCODED)
                && httpServletRequest.getHeader("content-encoding") == null) {
            requestByteArray = IOUtils.toByteArray(body);

            // this is binary.. just return it as is
            requestEntity = new ByteArrayRequestEntity(requestByteArray);

            // Get the client POST data as a Map if content type is: application/x-www-form-urlencoded
            // We do this manually since some data is not properly parseable by the servlet request
            Map<String, String[]> mapPostParameters = HttpUtilities.mapUrlEncodedParameters(requestByteArray);

            // Iterate the parameter names
            for (String stringParameterName : mapPostParameters.keySet()) {
                // Iterate the values for each parameter name
                String[] stringArrayParameterValues = mapPostParameters
                        .get(stringParameterName);
                for (String stringParameterValue : stringArrayParameterValues) {
                    // Create a NameValuePair and store in list

                    // add an & if there is already data
                    if (requestBody.length() > 0) {
                        requestBody.append("&");
                    }

                    requestBody.append(stringParameterName);

                    // not everything has a value so lets check
                    if (stringParameterValue.length() > 0) {
                        requestBody.append("=");
                        requestBody.append(stringParameterValue);
                    }
                }
            }
        } else if (httpServletRequest.getContentType() != null &&
                httpServletRequest.getContentType().contains(STRING_CONTENT_TYPE_MESSAGEPACK)) {

            /**
             * Convert input stream to bytes for it to be read by the deserializer
             * Unpack and iterate the list to see the contents
             */
            MessagePack msgpack = new MessagePack();
            requestByteArray = IOUtils.toByteArray(body);
            requestEntity = new ByteArrayRequestEntity(requestByteArray);
            ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(requestByteArray);
            Unpacker unpacker = msgpack.createUnpacker(byteArrayIS);

            for (Value message : unpacker) {
                deserialisedMessages += message;
                deserialisedMessages += "\n";
            }

            history.setRequestBodyDecoded(true);
        } else {
            requestByteArray = IOUtils.toByteArray(body);

            // this is binary.. just return it as is
            requestEntity = new ByteArrayRequestEntity(requestByteArray);

            // decode this for history if it is encoded
            String requestBodyString = PluginHelper.getByteArrayDataAsString(httpServletRequest.getHeader("content-encoding"), requestByteArray);
            requestBody.append(requestBodyString);

            // mark in history if the body has been decoded
            if (! requestBodyString.equals(new String(requestByteArray)))
                history.setRequestBodyDecoded(true);
        }

        // set post body in history object
        history.setRequestPostData(requestBody.toString());

        // set post body in proxy request object
        methodProxyRequest.setRequestEntity(requestEntity);

        /**
         * Set the history to have decoded messagepack. Pass the byte data back to request
         */
        if (httpServletRequest.getContentType() != null &&
                httpServletRequest.getContentType().contains(STRING_CONTENT_TYPE_MESSAGEPACK)) {
            history.setRequestPostData(deserialisedMessages);
            ByteArrayRequestEntity byteRequestEntity = new ByteArrayRequestEntity(requestByteArray);
            methodProxyRequest.setRequestEntity(byteRequestEntity);

        }
    }
}
