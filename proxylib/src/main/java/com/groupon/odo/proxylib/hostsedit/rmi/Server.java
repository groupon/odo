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
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private int port = 1099;
    private Registry registry = null;
    private static Server _instance = null;
    private String SERVICE_NAME = "hostsService";

    private Server(int port) {
        this.port = port;
        this.startServer();
    }

    public static Server getInstance() {
        if (_instance == null) {
            _instance = new Server(1298);
        }

        return _instance;
    }

    private void startServer() {
        try {
            // create on port 1298
            registry = LocateRegistry.createRegistry(port);

            // create a new service named hostsService
            registry.rebind(SERVICE_NAME, new MessageImpl());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("system is ready on port " + port);
    }

    public void stopServer() {
        try {
            registry.unbind(SERVICE_NAME);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}