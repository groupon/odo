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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

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

        // check to see if this content is supposed to be compressed
        // if so recompress it
        boolean isEncoded = false;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (response.getHeader("content-encoding") != null &&
                response.getHeader("content-encoding").equals("gzip")) {
            // GZIP the data
            isEncoded = true;
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(content.getBytes());
            gzip.close();
            out.close();
        } else if (response.getHeader("content-encoding") != null &&
                response.getHeader("content-encoding").equals("deflate")) {
            // Deflate the data
            isEncoded = true;
            Deflater compressor = new Deflater();
            compressor.setInput(content.getBytes());
            compressor.finish();

            byte[] buffer = new byte[1024];
            while (!compressor.finished()) {
                int count = compressor.deflate(buffer);
                out.write(buffer, 0, count);
            }
            out.close();
            compressor.end();
        }


        // don't do this if we got a HTTP 304 since there is no data to send back
        if (response.getStatus() != HttpServletResponse.SC_NOT_MODIFIED) {
            if (!chunked) {
                // change the content length header to the new length
                if (content != null && !isEncoded) {
                    response.setContentLength(content.getBytes().length);
                } else if (isEncoded) {
                    response.setContentLength(out.toByteArray().length);
                }
            }

            OutputStream outputStreamClientResponse = response.getOutputStream();
            response.resetBuffer();

            if (content != null && !isEncoded) {
                outputStreamClientResponse.write(content.getBytes());
            } else if (isEncoded) {
                outputStreamClientResponse.write(out.toByteArray());
            }
        }
    }

    public static String readResponseContent(HttpServletResponse response) throws IOException {
        PluginResponse pluginResponse = (PluginResponse)response;
        return pluginResponse.getContentString();
    }

    /**
     * Decodes stream data based on content encoding
     * @param contentEncoding
     * @param bytes
     * @return String representing the stream data
     */
    public static String getByteArrayDataAsString(String contentEncoding, byte[] bytes) {
        ByteArrayOutputStream byteout = null;
        if (contentEncoding != null &&
                contentEncoding.equals("gzip")) {
            // GZIP
            ByteArrayInputStream bytein = null;
            GZIPInputStream zis = null;
            try {
                bytein = new ByteArrayInputStream(bytes);
                zis = new GZIPInputStream(bytein);
                byteout = new ByteArrayOutputStream();

                int res = 0;
                byte buf[] = new byte[1024];
                while (res >= 0) {
                    res = zis.read(buf, 0, buf.length);
                    if (res > 0) {
                        byteout.write(buf, 0, res);
                    }
                }

                zis.close();
                bytein.close();
                byteout.close();
                return byteout.toString();
            } catch (Exception e) {
                // No action to take
            }
        } else if (contentEncoding != null &&
                contentEncoding.equals("deflate")) {
            try {
                // DEFLATE
                byte[] buffer = new byte[1024];
                Inflater decompresser = new Inflater();
                byteout = new ByteArrayOutputStream();
                decompresser.setInput(bytes);
                while (!decompresser.finished()) {
                    int count = decompresser.inflate(buffer);
                    byteout.write(buffer, 0, count);
                }
                byteout.close();
                decompresser.end();

                return byteout.toString();
            } catch (Exception e) {
                // No action to take
            }
        }

        return new String(bytes);
    }
}
