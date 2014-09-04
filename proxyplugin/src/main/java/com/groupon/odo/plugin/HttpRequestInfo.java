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
package com.groupon.odo.plugin;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestInfo {
    private String authType;
    private String contextPath;
    private Map<String, String> headers;
    private String method;
    private String pathInfo;
    private String queryString;
    private String requestURI;
    private String servletPath;
    private String contentType;
    private String characterEncoding;
    private int contentLength;
    private String localName;
    private int localPort;
    private Map parameterMap;
    private String protocol;
    private String remoteAddr;
    private String remoteHost;
    private int remotePort;
    private String serverName;
    private boolean secure;
    private String postContent;
    private Map<String, Object> attributes;

    public HttpRequestInfo(HttpServletRequest request) {
        this.authType = request.getAuthType();
        this.contextPath = request.getContextPath();
        populateHeaders(request);
        this.method = request.getMethod();
        this.pathInfo = request.getPathInfo();
        this.queryString = request.getQueryString();
        this.requestURI = request.getRequestURI();
        this.servletPath = request.getServletPath();
        this.contentType = request.getContentType();
        this.characterEncoding = request.getCharacterEncoding();
        this.contentLength = request.getContentLength();
        this.localName = request.getLocalName();
        this.localPort = request.getLocalPort();
        populateParameters(request);
        this.protocol = request.getProtocol();
        this.remoteAddr = request.getRemoteAddr();
        this.remoteHost = request.getRemoteHost();
        this.remotePort = request.getRemotePort();
        this.serverName = request.getServerName();
        this.secure = request.isSecure();
        populateAttributes(request);
    }


    public HttpRequestInfo(HttpServletRequest request, String postContent) {
        this(request);
        this.postContent = postContent;
    }

    public String getAuthType() {
        return authType;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Map getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.containsKey(name) ? headers.get(name) : null;
    }

    public Map getAttributes() {
        return attributes;
    }

    public Object getAttribute(String name) {
        return attributes.containsKey(name) ? attributes.get(name) : null;
    }

    public String getMethod() {
        return method;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRequestURI() {
        return requestURI;
    }
    public String getServletPath() {
        return servletPath;
    }
    public String getContentType() {
        return contentType;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getLocalName() {
        return localName;
    }

    public int getLocalPort() {
        return localPort;
    }

    public Map getParameterMap() {
        return parameterMap;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getServerName() {
        return serverName;
    }

    public boolean isSecure() {
        return secure;
    }

    private void populateHeaders(HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();
        this.headers = new HashMap<String, String>();

        while(headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            String value = request.getHeader(headerName);
            this.headers.put(headerName, value);
        }
    }

    private void populateAttributes(HttpServletRequest request) {
        Enumeration attributeNames = request.getAttributeNames();
        this.attributes = new HashMap<String, Object>();

        while(attributeNames.hasMoreElements()) {
            String attrName = (String)attributeNames.nextElement();
            Object value = request.getAttribute(attrName);
            this.attributes.put(attrName, value);
        }
    }

    private void populateParameters(HttpServletRequest request) {
        Enumeration paramNames = request.getParameterNames();
        this.parameterMap = new HashMap<String, String>();

        while(paramNames.hasMoreElements()) {
            String paramName = (String)paramNames.nextElement();
            Object value = request.getAttribute(paramName);
            this.parameterMap.put(paramName, value);
        }
    }

    public String getPostContent() {
        return postContent;
    }
}
