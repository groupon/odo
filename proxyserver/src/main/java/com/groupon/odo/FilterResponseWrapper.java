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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FilterResponseWrapper extends HttpServletResponseWrapper {

    ByteArrayOutputStream output;
    FilterServletOutputStream filterOutput;
    PrintWriter pw;

    public FilterResponseWrapper(HttpServletResponse response) {
        super(response);
        // TODO Auto-generated constructor stub
        output = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        if (filterOutput == null) {
            filterOutput = new FilterServletOutputStream(output);
        }
        return filterOutput;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        // TODO Auto-generated method stub
        if (filterOutput == null) {
            filterOutput = new FilterServletOutputStream(output);
        }
        if (pw == null) {
            pw = new PrintWriter(filterOutput, true);
        }
        return pw;
    }

    //get the output from byte stream
    public byte[] getDataStream() {
        return output.toByteArray();
    }

}