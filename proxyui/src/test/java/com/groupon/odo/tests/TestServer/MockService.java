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
