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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class PluginHelper {

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

    public static void writeResponseContent(HttpServletResponse response, String content) throws IOException {
        // check to see if this is chunked
        boolean chunked = false;
        if (response.containsHeader(PluginHelper.STRING_TRANSFER_ENCODING)
                && response.getHeader(PluginHelper.STRING_TRANSFER_ENCODING).compareTo("chunked") == 0) {
            response.setHeader(PluginHelper.STRING_CONNECTION, PluginHelper.STRING_CHUNKED);
            chunked = true;
        }

        // don't do this if we got a HTTP 304 since there is no data to send back
        if (response.getStatus() != HttpServletResponse.SC_NOT_MODIFIED) {
            if (!chunked) {
                // change the content length header to the new length
                if (content != null) {
                    response.setContentLength(content.getBytes().length);
                }
            }

            OutputStream outputStreamClientResponse = response.getOutputStream();
            response.resetBuffer();

            if (content != null) {
                outputStreamClientResponse.write(content.getBytes());
            }
        }
    }

    public static String readResponseContent(HttpServletResponse response) throws IOException {
        PluginResponse pluginResponse = (PluginResponse)response;
        return pluginResponse.getContentString();
    }
}
