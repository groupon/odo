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

import com.groupon.odo.client.Client;
import com.groupon.odo.client.PathValueClient;
import com.groupon.odo.client.models.History;

public class SampleClient {

    /**
     * Demonstrates how to create a new path
     */
    public static void createPath() throws Exception {
        // First argument is the existing profile name
        // Second argument indicates if a new client should be created "true" or use the profile default "false"
        Client client = new Client("ProfileName", false);

        // createPath takes three arguments
        // 1. pathName: friendly name of the new path
        // 2. pathValue: regular expression to compare to request path
        // 3. requestType: GET, POST, PUT, DELETE
        client.createPath("Test Path", "/test", "GET");
    }

    /**
     * Demonstrates how to add an override to an existing path
     */
    public static void addOverrideToPath() throws Exception {
        Client client = new Client("ProfileName", false);

        // Use the fully qualified name for a plugin override.
        client.addMethodToResponseOverride("Test Path", "com.groupon.odo.sample.Common.delay");

        // The third argument is the ordinal - the nth instance of this override added to this path
        // The final arguments count and type are determined by the override. "delay" used in this sample
        // has a single int argument - # of milliseconds delay to simulate
        client.setMethodArguments("Test Path", "com.groupon.odo.sample.Common.delay", 1, "100");
    }

    /**
     * There are some different ways to easily set up a path with a mock response.
     * Here are some samples.
     */
    public static void mockPath1() {
        // A simple method that makes some assumptions.
        // PathValueClient operates based on the path value instead of the path name.
        // This method assumes you are using a single Profile (or uses the first one), and assumes use of the
        // default client.
        // setDefaultCustomResponse will create the path if it does not exist.
        PathValueClient.setDefaultCustomResponse("/test", "GET", "test response data");
    }

    public static void mockPath2() {
        // Similar to the first method, this assumes the default profile and default client
        // Request type is not specified since this is a method for an existing path
        Client.setCustomResponseForDefaultProfile("/test", "test response data");
    }

    public static void mockPath3() {
        // The Profile name is specified, but the client is assumed to be the default
        Client.setCustomResponseForDefaultClient("ProfileName", "Test path", "test response data");
    }

    public static void mockPath4() throws Exception{
        // This demonstrates how to mock a path for a specific profile and non-default client
        Client client = new Client("ProfileName", true);
        client.setCustomResponse("Test Path", "test response data");

        // Since a new client is generated, we'll call destroy to remote the clientId generated from the profile
        client.destroy();
    }

    /**
     * By default, the client assumes Odo is running on localhost. This demonstrates how to
     * configure client to target an Odo instance on a remote host.
     */
    public static void targetRemoteHost() throws Exception{
        Client client = new Client("ProfileName", false);
        client.setHostName("RemoteHostName");
    }

    /**
     * Demonstrates obtaining the request history data from a test run
     */
    public static void getHistory() throws Exception{
        Client client = new Client("ProfileName", false);

        // Obtain the 100 history entries starting from offset 0
        History[] history = client.refreshHistory(100, 0);

        client.clearHistory();
    }
}