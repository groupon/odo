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
package com.groupon.odo.tests.TestServer;

import com.sun.net.httpserver.Headers;

public class HttpExchangeInfo {
    private Headers requestHeaders;
    private String requestURI;
    private String requestMethod;
    private String requestBody;

    private Headers responseHeaders;
    private String responseBody;
    private int responseCode;


    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Headers headers) {
        requestHeaders = headers;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Headers headers) {
        responseHeaders = headers;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String uri) {
        requestURI = uri;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String method) {
        requestMethod = method;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int code) {
        this.responseCode = code;
    }

}
