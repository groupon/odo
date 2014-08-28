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

import com.groupon.odo.proxylib.models.History;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.io.IOUtils;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class HttpUtilities {

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
     * @param httpServletRequest
     * @return
     * @throws Exception
     */
    public static Map<String, String[]> mapUrlEncodedParameters(HttpServletRequest httpServletRequest) throws Exception {

        InputStream body = httpServletRequest.getInputStream();
        java.util.Scanner s = new java.util.Scanner(body).useDelimiter("\\A");
        Map<String, String[]> mapPostParameters = new HashMap<String, String[]>();

        try {
            if (s.hasNext()) {
                String requestData = s.next();
                String[] splitRequestData = requestData.split("&");
                for (String requestPart : splitRequestData) {
                    String[] parts = requestPart.split("=");
                    ArrayList<String> values = new ArrayList<String>();
                    if (mapPostParameters.containsKey(parts[0])) {
                        values = new ArrayList<String>(Arrays.asList(mapPostParameters.get(parts[0])));
                        mapPostParameters.remove(parts[0]);
                    }

                    if (parts.length > 1) {
                        values.add(parts[1]);
                    }

                    mapPostParameters.put(parts[0], values.toArray(new String[values.size()]));
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
            if (headerString.length() != 0)
                headerString += "\n";

            String name = headerNames.nextElement();
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
            if (headerString.length() != 0)
                headerString += "\n";

            headerString += headerName + ": " + response.getHeader(headerName);
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

        // Create a new file upload handler
        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        // Parse the request
        try {
            // Get the multipart items as a list
            List<FileItem> listFileItems = (List<FileItem>) servletFileUpload.parseRequest(httpServletRequest);
            // Create a list to hold all of the parts
            List<Part> listParts = new ArrayList<Part>();
            // Iterate the multipart items list
            for (FileItem fileItemCurrent : listFileItems) {
                // If the current item is a form field, then create a string
                // part
                if (fileItemCurrent.isFormField()) {
                    StringPart stringPart = new StringPart(
                            fileItemCurrent.getFieldName(), // The field name
                            fileItemCurrent.getString() // The field value
                    );
                    // Add the part to the list
                    listParts.add(stringPart);
                } else {
                    // The item is a file upload, so we create a FilePart
                    FilePart filePart = new FilePart(
                            fileItemCurrent.getFieldName(), // The field name
                            new ByteArrayPartSource(fileItemCurrent.getName(), // The uploaded file name
                                    fileItemCurrent.get() // The uploaded file contents
                            )
                    );
                    // Add the part to the list
                    listParts.add(filePart);
                }
            }
            MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(
                    listParts.toArray(new Part[listParts.size()]),
                    postMethodProxyRequest.getParams());

            postMethodProxyRequest.setRequestEntity(multipartRequestEntity);

            // The current content-type header (received from the client) IS of
            // type "multipart/form-data", but the content-type header also
            // contains the chunk boundary string of the chunks. Currently, this
            // header is using the boundary of the client request, since we
            // blindly copied all headers from the client request to the proxy
            // request. However, we are creating a new request with a new chunk
            // boundary string, so it is necessary that we re-set the
            // content-type string to reflect the new chunk boundary string
            postMethodProxyRequest.setRequestHeader(
                    STRING_CONTENT_TYPE_HEADER_NAME,
                    multipartRequestEntity.getContentType());
        } catch (FileUploadException fileUploadException) {
            throw new ServletException(fileUploadException);
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
        java.util.Scanner s = new java.util.Scanner(body).useDelimiter("\\A");

        if (httpServletRequest.getContentType() != null &&
                httpServletRequest.getContentType().contains(STRING_CONTENT_TYPE_FORM_URLENCODED)) {
            // Get the client POST data as a Map if content type is: application/x-www-form-urlencoded
            // We do this manually since some data is not properly parseable by the servlet request
            Map<String, String[]> mapPostParameters = HttpUtilities.mapUrlEncodedParameters(httpServletRequest);

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
            ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(requestByteArray);
            Unpacker unpacker = msgpack.createUnpacker(byteArrayIS);

            for (Value message : unpacker) {
                deserialisedMessages += message;
                deserialisedMessages += "\n";
            }
        } else {
            // just set the request body to the POST body
            if (s.hasNext()) {
                requestBody.append(s.next());
            }
        }
        // Set the proxy request data
        StringRequestEntity stringEntity = new StringRequestEntity(
                requestBody.toString(), null, null);

        // set post body in history object
        history.setRequestPostData(requestBody.toString());


        // set post body in proxy request object
        methodProxyRequest.setRequestEntity(stringEntity);

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
