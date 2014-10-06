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
package com.groupon.odo.proxylib;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

public class Utils {
    // found this on a website, splits a string of ints "1,2,3,4,5" into an
    // array [1,2,3,4,5]
    // http://www.java2s.com/Code/Java/Development-Class/Parsecommaseparatedlistofintsandreturnasarray.htm

    /**
     * Split string of comma-delimited ints into an a int array
     *
     * @param str
     * @return
     * @throws IllegalArgumentException
     */
    public static int[] arrayFromStringOfIntegers(String str) throws IllegalArgumentException {
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        int n = tokenizer.countTokens();
        int[] list = new int[n];
        for (int i = 0; i < n; i++) {
            String token = tokenizer.nextToken();
            list[i] = Integer.parseInt(token);
        }
        return list;
    }

    public static HashMap<String, Object> getJQGridJSON(Object row, String rowName) {
        HashMap<String, Object> returnVal = new HashMap<String, Object>();
        returnVal.put("records", 1);
        returnVal.put("total", 1);
        returnVal.put("page", 1);
        returnVal.put(rowName, row);
        return returnVal;
    }

    public static HashMap<String, Object> getJQGridJSON(Object[] rows, String rowName) {
        HashMap<String, Object> returnVal = new HashMap<String, Object>();
        returnVal.put("records", rows.length);
        returnVal.put("total", 1);
        returnVal.put("page", 1);
        returnVal.put(rowName, rows);
        return returnVal;
    }
    
    public static HashMap<String, Object> getJQGridJSON(List<?> rows, String rowName) {
    	return getJQGridJSON(rows.toArray(), rowName);
    }
    
    public static HashMap<String, Object> getJQGridJSON(ArrayList<?> rows, String rowName) {
    	return getJQGridJSON(rows.toArray(), rowName);
    }

    public static HashMap<String, Object> getJQGridJSON(Object[] rows, String rowName, int offset, int totalRows, int requestedRows) {
        HashMap<String, Object> returnVal = new HashMap<String, Object>();

        double totalPages = Math.ceil((double) totalRows / (double) requestedRows);

        returnVal.put("records", totalRows);
        returnVal.put("total", totalPages);
        returnVal.put("page", offset / requestedRows + 1);
        returnVal.put(rowName, rows);
        return returnVal;
    }

    /**
     * Copies file from a resource to a local temp file
     *
     * @param sourceResource
     * @return Absolute filename of the temp file
     * @throws Exception
     */
    public static File copyResourceToLocalFile(String sourceResource, String destFileName) throws Exception {
        try {
            Resource keystoreFile = new ClassPathResource(sourceResource);
            InputStream in = keystoreFile.getInputStream();

            File outKeyStoreFile = new File(destFileName);
            FileOutputStream fop = new FileOutputStream(outKeyStoreFile);
            byte[] buf = new byte[512];
            int num;
            while ((num = in.read(buf)) != -1) {
                fop.write(buf, 0, num);
            }
            fop.flush();
            fop.close();
            in.close();
            return outKeyStoreFile;
        } catch (IOException ioe) {
            throw new Exception("Could not copy keystore file: " + ioe.getMessage());
        }
    }

    /**
     * Returns the port as configured by teh system variables, fallback is the default port value
     * @param portIdentifier - SYS_*_PORT defined in Constants
     * @return
     */
    public static int GetSystemPort(String portIdentifier) {
        int defaultPort = 0;

        if(portIdentifier.compareTo(Constants.SYS_API_PORT) == 0) {
            defaultPort = Constants.DEFAULT_API_PORT;
        }
        else if(portIdentifier.compareTo(Constants.SYS_DB_PORT) == 0) {
            defaultPort = Constants.DEFAULT_DB_PORT;
        }
        else if(portIdentifier.compareTo(Constants.SYS_FWD_PORT) == 0) {
            defaultPort = Constants.DEFAULT_FWD_PORT;
        }
        else if(portIdentifier.compareTo(Constants.SYS_HTTP_PORT) == 0) {
            defaultPort = Constants.DEFAULT_HTTP_PORT;
        }
        else if(portIdentifier.compareTo(Constants.SYS_HTTPS_PORT) == 0) {
            defaultPort = Constants.DEFAULT_HTTPS_PORT;
        }
        else {
            return defaultPort;
        }

        String portStr = System.getenv(portIdentifier);
        return (portStr == null || portStr.isEmpty()) ? defaultPort : Integer.valueOf(portStr);
    }

    /**
     * This function returns the first external IP address encountered
     *
     * @return IP address or null
     * @throws Exception
     */
    public static String getPublicIPAddress() throws Exception {
        final String IPV4_REGEX = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";

        String ipAddr = null;
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();

                // Pick the first non loop back address
                if((!i.isLoopbackAddress() && i.isSiteLocalAddress()) ||
                        i.getHostAddress().matches(IPV4_REGEX)) {
                    ipAddr = i.getHostAddress();
                    break;
                }
            }
            if(ipAddr != null) break;
        }

        return ipAddr;
    }
}
