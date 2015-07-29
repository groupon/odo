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
package com.groupon.odo.sample;

import com.groupon.odo.plugin.PluginArguments;
import com.groupon.odo.plugin.PluginHelper;
import com.groupon.odo.plugin.v2.ResponseOverride;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Contains common override functions such as HTTP errors
 */
public class Common {

    public static String createErrorMessage(int statusValue, String message) {
        return "{\"error\":{\"httpCode\":" + statusValue + ",\"message\":" + message + "}}";
    }

    //HTTP Errors
    @ResponseOverride(
            description = "Return HTTP302",
            blockRequest = true)
    public static void http302(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusFound);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusFound, "\"<html><body>You are being redirected.</body></html>\""));
    }

    @ResponseOverride(
            description = "Return HTTP400",
            blockRequest = true)
    public static void http400(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusBadReq);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusBadReq,
                String.format(Constants.defaultErrorMessage, Constants.statusBadReq)));
    }

    @ResponseOverride(
            description = "Return HTTP401",
            blockRequest = true)
    public static void http401(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusUnauthorized);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusUnauthorized,
                String.format(Constants.defaultErrorMessage, Constants.statusUnauthorized)));
    }

    @ResponseOverride(
            description = "Return HTTP403",
            blockRequest = true)
    public static void http403(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusForbidden);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusForbidden,
                String.format(Constants.defaultErrorMessage, Constants.statusForbidden)));
    }

    @ResponseOverride(
            description = "Return HTTP404",
            blockRequest = true)
    public static void http404(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusNotFound);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusNotFound,
                String.format(Constants.defaultErrorMessage, Constants.statusNotFound)));
    }

    @ResponseOverride(
            description = "Return HTTP408",
            blockRequest = true)
    public static void http408(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusRequestTimeOut);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusRequestTimeOut,
                String.format(Constants.defaultErrorMessage, Constants.statusRequestTimeOut)));
    }

    @ResponseOverride(
            description = "Return HTTP500",
            blockRequest = true)
    public static void http500(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusInternalServerError);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusInternalServerError,
                String.format(Constants.defaultErrorMessage, Constants.statusInternalServerError)));
    }

    @ResponseOverride(
            description = "Return HTTP503",
            blockRequest = true)
    public static void http503(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusServiceUnavailable);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusServiceUnavailable,
                "\"<html><body><h1>\" + Constants.statusServiceUnavailable + \" Service Unavailable</h1> No server is available to handle this request.</body></html>\""));
    }

    @ResponseOverride(
            description = "Return HTTP201",
            blockRequest = true)
    public static void http201(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusCreated);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusCreated,
                String.format(Constants.defaultErrorMessage, Constants.statusCreated)));
    }

    //Network Errors/issues
    @ResponseOverride(
            description = "Slow Down Response",
            parameters = {"Delay Time (milliseconds)"})
    public static void delay(PluginArguments args, Integer milliseconds) throws Exception {
        Thread.sleep(milliseconds);
    }

    //Other Errors
    @ResponseOverride(
            description = "Return malformed json",
            blockRequest = true)
    public static void malformed_json(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusCreated);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusCreated, "\"{\"hello\": [thisismalformedjson}\""));
    }

    @ResponseOverride(
            description = "Return empty response",
            blockRequest = true)
    public static void http_200_empty_response(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(Constants.statusOK);
        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, createErrorMessage(Constants.statusOK, "\"\""));
    }

    /**
     * Set a JSON value. You can access array elements by including regex index in the path. Ex: root.objectArray[0] or root.objectArray[\d]
     * @param args
     * @param name
     * @param value
     * @param path
     * @throws Exception
     */
    @ResponseOverride(
            description = "Set a JSON value.",
            parameters = {"name", "value", "path"}
    )
    public static void set_json_value(PluginArguments args, String name, String value, String path) throws Exception {
        HttpServletResponse response = args.getResponse();
        String content = PluginHelper.readResponseContent(response);
        JSONObject jsonContent = new JSONObject(content);

        process_json_value(jsonContent, name, value, path, true);
        PluginHelper.writeResponseContent(response, jsonContent.toString());
    }

    @ResponseOverride(
            description = "Remove a JSON value",
            parameters = {"name", "path"}
    )
    public static void remove_json_value(PluginArguments args, String name, String path) throws Exception {
        HttpServletResponse response = args.getResponse();
        String content = PluginHelper.readResponseContent(response);
        JSONObject jsonContent = new JSONObject(content);
        process_json_value(jsonContent, name, null, path, false);
        PluginHelper.writeResponseContent(response, jsonContent.toString());
    }

    /**
     * General method for set/remove json value. Recursively called for each element of path, supporting regex in array indices
     * @param object JSONObject to modify
     * @param name JSON key to set/remove
     * @param value value to set (null for remove)
     * @param path Path to the value within the object. Access array elements by including regex index in the path. Ex: root.objectArray[0] or root.objectArray[\d]
     * @param isSet true for set, false for remove
     * @return The modified JSONObject
     * @throws Exception
     */
    private static JSONObject process_json_value(JSONObject object, String name, String value, String path, Boolean isSet) throws Exception {
        if (!path.equals("")) {
            String[] pathElements = path.split("\\.");
            String remainingPath = pathElements.length > 1 ? path.replace(pathElements[0] + ".", "") : "";
            String element = pathElements[0];

            if (element.contains("[")) {
                // array indexer - regular expression
                int startIndex = element.indexOf("[");
                int endIndex = element.indexOf("]");

                if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
                    throw new InvalidParameterException("Invalid array indexer " + element);
                }
                String indexerPattern = element.substring(startIndex + 1, endIndex);
                String arrayName = element.substring(0, startIndex);

                JSONArray array = object.getJSONArray(arrayName);
                for (int i = 0; i < array.length(); ++i) {
                    Pattern pattern = Pattern.compile(indexerPattern);
                    Matcher matcher = pattern.matcher(String.valueOf(i));
                    if (matcher.find()) {
                        array.put(i, process_json_value(array.getJSONObject(i), name, value, remainingPath, isSet));
                    }
                }
                object.put(arrayName, array);
                return object;
            } else {
                process_json_value(object.getJSONObject(element), name, value, remainingPath, isSet);
                return object;
            }
        } else {
            if(isSet) {
                // test & set if value is valid JSONArray or JSONObject
                try {
                    JSONArray jsonVal = new JSONArray(value);
                    object.put(name, jsonVal);
                    return object;
                } catch (Exception e) {
                }

                try {
                    JSONObject jsonVal = new JSONObject(value);
                    object.put(name, jsonVal);
                    return object;
                } catch (Exception e) {
                }

                object.put(name, value);
            } else {
                object.remove(name);
            }
        }

        return object;
    }

    @ResponseOverride(
            description = "Use response from .txt file",
            parameters = {"Filename"},
            blockRequest = true
    )
    public static void response_from_file(PluginArguments args, String filename) throws Exception {
        HttpServletResponse response = args.getResponse();
        File statusFile = new File(filename);

        String statusString = "";
        if (statusFile.exists()) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                statusString = statusString + line;
            }
            response.setStatus(Constants.statusOK);
        } else {
            statusString = createErrorMessage(Constants.statusNotFound, "\"No File\"");
            response.setStatus(Constants.statusNotFound);
        }

        response.setContentType(Constants.contentType);
        PluginHelper.writeResponseContent(response, statusString);
    }
}