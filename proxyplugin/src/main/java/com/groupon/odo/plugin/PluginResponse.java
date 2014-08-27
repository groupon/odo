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


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class PluginResponse extends HttpServletResponseWrapper{
    public PluginResponse(HttpServletResponse response) {
        super(response);
    }

    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final PrintWriter pw = new PrintWriter(outputStream);

    @Override
    public PrintWriter getWriter() {
        return pw;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public void write(int b) {
                outputStream.write(b);
            }
        };
    }

    @Override
    public void resetBuffer() {
        outputStream.reset();
    }


    @Override
    public void flushBuffer() throws IOException {
        OutputStream output = super.getOutputStream();
        output.write(outputStream.toByteArray());
    }
}

