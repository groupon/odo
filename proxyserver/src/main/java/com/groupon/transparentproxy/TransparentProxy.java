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
package com.groupon.transparentproxy;

import com.groupon.odo.bmp.ProxyServer;
import com.groupon.odo.proxylib.Constants;
import com.groupon.odo.proxylib.Utils;

public class TransparentProxy {
    private static ProxyServer _proxyserver = null;
    private static TransparentProxy _instance = null;

    public TransparentProxy() {

    }

    public static void main(String args[]) throws Exception {
        TransparentProxy.getInstance();
    }

    public void startServer() throws Exception {
        int fwdPort = Utils.GetSystemPort(Constants.SYS_FWD_PORT);
        _proxyserver = new ProxyServer(fwdPort);
        _proxyserver.start();
    }

    public static TransparentProxy getInstance() throws Exception {
        if (_instance == null) {
            _instance = new TransparentProxy();
            _instance.startServer();
        }
        return _instance;
    }

    public void shutDown() throws Exception {
        try {
            _proxyserver.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remapHost(String host, String altHost) {
        _proxyserver.remapHost(host, altHost);
    }

}
