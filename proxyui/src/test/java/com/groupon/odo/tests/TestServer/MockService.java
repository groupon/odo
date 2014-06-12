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


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

public class MockService {

    private static MockService instance = null;
    private String response = "test response";
    private Integer responseCode = 200;
    private HttpExchangeInfo lastExchange;
    private int testPort;


    private MockService() {

    }

    public static MockService getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new MockService();
        return instance;
    }

    public String getResponse() {
        return this.response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Integer getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public HttpExchangeInfo getLastExchange() {
        return this.lastExchange;
    }

    public void setLastExchange(HttpExchangeInfo exchange) {
        this.lastExchange = exchange;
    }

    public int getPort() {
        return this.testPort;
    }

    public void setPort(int port) {
        this.testPort = port;
    }

    public void defaultProcess(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpExchangeInfo exchange = new HttpExchangeInfo();
            exchange.setRequestURI(request.getRequestURI());
            exchange.setRequestBody(request.getInputStream().toString());
            exchange.setResponseCode(this.responseCode);
            exchange.setResponseBody(this.response);
            this.lastExchange = exchange;

            response.setContentType("text/plain");
            response.setStatus(this.responseCode);
            OutputStream outputStreamClientResponse = response.getOutputStream();
            outputStreamClientResponse.write(this.response.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
