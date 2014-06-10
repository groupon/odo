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
