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

import com.groupon.odo.proxylib.ClientService;
import com.groupon.odo.proxylib.ProfileService;
import com.groupon.odo.proxylib.ServerRedirectService;
import com.groupon.odo.proxylib.models.Client;
import com.groupon.odo.proxylib.models.Profile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class ServerRedirectTest extends TestBase {

    private Profile newProfile;

    /**
     * Sets up stub data before each test
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        resetDatabase();

        // add some stub data
        newProfile = ProfileService.getInstance().add("Test");
        ServerRedirectService.getInstance().addServerRedirectToProfile("en", "api.groupon.com", "blah.com", null, newProfile.getId(), -1);
    }

    @Test
    public void profileOff() throws Exception {
        // by default the profile is off
        assertFalse(ServerRedirectService.getInstance().canHandleRequest("api.groupon.com"));
    }

    @Test
    public void profileOn() throws Exception {
        // turn the client on
        ClientService.getInstance().updateActive(newProfile.getId(), "-1", true);

        assertTrue(ServerRedirectService.getInstance().canHandleRequest("api.groupon.com"));
    }

    @Test
    public void serverNameNotExist() throws Exception {
        // by default the profile is off
        assertFalse(ServerRedirectService.getInstance().canHandleRequest("bad server name"));
    }

    /**
     * Tests to make sure canHandleRequest works for a non default client
     */
    @Test
    public void nonDefaultClientActive() throws Exception {
        // create a client
        Client client = ClientService.getInstance().add(newProfile.getId());
        assertFalse(ServerRedirectService.getInstance().canHandleRequest("api.groupon.com"));
        ClientService.getInstance().updateActive(newProfile.getId(), client.getUUID(), true);
        assertTrue(ServerRedirectService.getInstance().canHandleRequest("api.groupon.com"));
    }
}
