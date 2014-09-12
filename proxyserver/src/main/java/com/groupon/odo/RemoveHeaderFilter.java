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

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.ArrayList;

public class RemoveHeaderFilter implements Filter {
    /**
     * This looks at the servlet attributes to get the list of response headers to remove while the response object gets created by the servlet
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final ServletRequest r1 = request;
        chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {
            @SuppressWarnings("unchecked")
            public void setHeader(String name, String value) {
                ArrayList<String> removeHeaders = new ArrayList<String>();
                
                if (r1.getAttribute("com.groupon.odo.removeHeaders") != null)
                    removeHeaders = (ArrayList<String>) r1.getAttribute("com.groupon.odo.removeHeaders");

                removeHeaders.add("transfer-encoding");
                if (!removeHeaders.contains(name.toLowerCase())) {
                    super.setHeader(name, value);
                }
            }
        });
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }

}
