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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.groupon.odo.proxylib.SQLService;

public class ProxyServletContext implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        try {
            TransparentProxy.getInstance();
            System.out.println("Proxy Started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // shutdown the browsermob proxy
        try {
            TransparentProxy.getInstance().shutDown();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // shutdown the SQL Server
        try {
            SQLService.getInstance().stopServer();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

