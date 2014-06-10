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

package com.groupon.odo.proxylib.hostsedit.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private static final String SERVICE_NAME = "hostsService";

    private static int port = 1298;

    /**
     * Enable a host
     *
     * @param hostName
     * @throws Exception
     */
    public static void enableHost(String hostName) throws Exception {
        Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", port);
        com.groupon.odo.proxylib.hostsedit.rmi.Message impl = (com.groupon.odo.proxylib.hostsedit.rmi.Message) myRegistry.lookup(SERVICE_NAME);

        impl.enableHost(hostName);
    }

    /**
     * Disable a host
     *
     * @param hostName
     * @throws Exception
     */
    public static void disableHost(String hostName) throws Exception {
        Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", port);
        com.groupon.odo.proxylib.hostsedit.rmi.Message impl = (com.groupon.odo.proxylib.hostsedit.rmi.Message) myRegistry.lookup(SERVICE_NAME);

        impl.disableHost(hostName);
    }

    /**
     * Remove a host
     *
     * @param hostName
     * @throws Exception
     */
    public static void removeHost(String hostName) throws Exception {
        Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", port);
        com.groupon.odo.proxylib.hostsedit.rmi.Message impl = (com.groupon.odo.proxylib.hostsedit.rmi.Message) myRegistry.lookup(SERVICE_NAME);

        impl.removeHost(hostName);
    }

    /**
     * Returns whether a host exists
     *
     * @param hostName
     * @return
     * @throws Exception
     */
    public static boolean exists(String hostName) throws Exception {
        Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", port);
        com.groupon.odo.proxylib.hostsedit.rmi.Message impl = (com.groupon.odo.proxylib.hostsedit.rmi.Message) myRegistry.lookup(SERVICE_NAME);

        return impl.exists(hostName);
    }

    /**
     * Returns whether a host is enabled
     *
     * @param hostName
     * @return
     * @throws Exception
     */
    public static boolean isEnabled(String hostName) throws Exception {
        Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", port);
        com.groupon.odo.proxylib.hostsedit.rmi.Message impl = (com.groupon.odo.proxylib.hostsedit.rmi.Message) myRegistry.lookup(SERVICE_NAME);

        return impl.isEnabled(hostName);
    }

    /**
     * Returns whether or not the host editor service is available
     *
     * @return
     * @throws Exception
     */
    public static boolean isAvailable() throws Exception {
        try {
            Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", port);
            com.groupon.odo.proxylib.hostsedit.rmi.Message impl = (com.groupon.odo.proxylib.hostsedit.rmi.Message) myRegistry.lookup(SERVICE_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}