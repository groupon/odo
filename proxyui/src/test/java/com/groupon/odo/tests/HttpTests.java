/*
Copyright (c) 2014, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
