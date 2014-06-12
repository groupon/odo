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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/*")
public class TestHandler extends HttpServlet {

    /**
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public TestHandler() {
        super();
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        MockService.getInstance().defaultProcess(request, response);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        MockService.getInstance().defaultProcess(request, response);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse
     * response)
     */
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        MockService.getInstance().defaultProcess(request, response);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse
     * response)
     */
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException {
        MockService.getInstance().defaultProcess(request, response);
    }

}
