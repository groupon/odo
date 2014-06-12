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
package com.groupon.odo.tests;

import com.groupon.odo.client.Client;
import com.groupon.odo.proxylib.PathOverrideService;
import com.groupon.odo.proxylib.ProfileService;
import com.groupon.odo.proxylib.models.Profile;
import com.groupon.odo.tests.TestServer.HttpExchangeInfo;
import com.groupon.odo.tests.TestServer.MockService;
import com.groupon.odo.tests.TestServer.TestHttpProxyContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class HttpTests extends TestBase {

    private static MockService testServer;
    private static ConfigurableApplicationContext context;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testServer = MockService.getInstance();
        testServer.setPort(8080);
        context = SpringApplication.run(TestHttpProxyContainer.class, "");
    }

    @AfterClass
    public static void cleanupAfterClass() {
        context.close();
    }

    @Before
    public void setup() throws Exception {
        try {
            resetDatabase();
            Profile profile = ProfileService.getInstance().add("Consumer API");
            int id = profile.getId();
            int pathId = PathOverrideService.getInstance().addPathnameToProfile(id, "Global", "/");
            PathOverrideService.getInstance().addPathToRequestResponseTable(id, "-1", pathId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRedirect() throws Exception {
        Client client = new Client("Consumer API", false);
        client.toggleProfile(true);
        client.addServerMapping("localhost", "127.0.0.1:8080", null);

        String response = HttpUtils.doProxyGet("http://localhost/", null);
        System.out.println(response);
        HttpExchangeInfo info = testServer.getLastExchange();
        System.out.println(info.getRequestBody());
        assertTrue(info.getResponseBody().compareTo(testServer.getResponse()) == 0);
    }

    @Test
    public void testRedirectCustomOverride() throws Exception {
        String overrideResponse = "overridden";
        testServer.setLastExchange(null);

        Client client = new Client("Consumer API", false);
        client.toggleProfile(true);
        client.addServerMapping("localhost", "127.0.0.1:8080", null);
        client.setCustomResponse("Global", overrideResponse);
        client.toggleResponseOverride("Global", true);

        String response = HttpUtils.doProxyGet("http://localhost/", null);
        System.out.println(response);
        HttpExchangeInfo info = testServer.getLastExchange();
        assertNull(info);
        assertTrue(response.compareTo(overrideResponse) == 0);
    }

}
