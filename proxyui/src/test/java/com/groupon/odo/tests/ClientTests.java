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
import com.groupon.odo.client.PathValueClient;
import com.groupon.odo.client.models.ServerGroup;
import com.groupon.odo.client.models.ServerRedirect;
import com.groupon.odo.proxylib.PathOverrideService;
import com.groupon.odo.proxylib.ProfileService;
import com.groupon.odo.proxylib.models.Profile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
public class ClientTests extends TestBase {
    Client client = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setup() throws Exception {
        try {
            resetDatabase();
            Profile profile = ProfileService.getInstance().add("Consumer API");
            int id = profile.getId();
            int pathId = PathOverrideService.getInstance().addPathnameToProfile(id, "Global", "/");
            PathOverrideService.getInstance().addPathToRequestResponseTable(id, "-1", pathId);
            client = new Client("Consumer API", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void retrieveDefaultClient() throws Exception {
        assertFalse(client == null);
    }

    @Test
    public void createClient() throws Exception {
        Client newClient = new Client("Consumer API", true);
        assertFalse(newClient == null);
        newClient.destroy();
    }

    @Test
    public void createInvalidClient() throws Exception {
        try {
            Client newClient = new Client("Fake PROFILE", true);
            assertFalse(client == newClient);
        } catch (Exception e) {
            assertTrue(e != null);
        }
    }

    @Test
    public void disableProfile() throws Exception {
        assertTrue(client.toggleProfile(false));
    }

    @Test
    public void enableProfile() throws Exception {
        assertTrue(client.toggleProfile(true));
    }

    @Test
    public void enablePath() throws Exception {
        assertTrue(client.toggleResponseOverride("Global", true));
    }

    @Test
    public void resetProfile() throws Exception {
        assertTrue(client.resetProfile());
    }

    @Test
    public void addMethodToOverride() throws Exception {
        Client newClient = new Client("Consumer API", true);
        assertTrue(newClient.addMethodToResponseOverride("Global", "com.groupon.odo.sample.Status.http404"));
        newClient.resetProfile();
        newClient.destroy();
    }

    @Test
    public void setMethodArguments() throws Exception {
        Client newClient = new Client("Consumer API", true);
        newClient.addMethodToResponseOverride("Global", "com.groupon.odo.sample.Common.delay");
        assertTrue(newClient.setMethodArguments("Global", "com.groupon.odo.sample.Common.delay", 1, "100"));
        newClient.destroy();
    }

    @Test
    public void setCustomResponse() throws Exception {
        Client newClient = new Client("Consumer API", true);
        assertTrue(newClient.setCustomResponse("Global", "response text"));
        newClient.destroy();
    }

    @Test
    public void setCustomResponseForDefaultClient() throws Exception {
        assertTrue(Client.setCustomResponseForDefaultClient("Consumer API", "Global", "test response"));
    }

    @Test
    public void setCustomRequestForDefaultClient() throws Exception {
        assertTrue(Client.setCustomRequestForDefaultClient("Consumer API", "Global", "test response"));
    }

    @Test
    public void setCustomRequestForDefaultProfile() throws Exception {
        assertTrue(Client.setCustomRequestForDefaultProfile("Global", "test response"));
    }

    @Test
    public void setCustomResponseForDefaultProfile() throws Exception {
        assertTrue(Client.setCustomResponseForDefaultProfile("Global", "test response"));
    }

    @Test
    public void stubEndpoint() throws Exception {
        assertTrue(PathValueClient.setDefaultCustomResponse("/test path/", "GET", "test response data"));
    }

    @Test
    public void clearEndpoint() throws Exception {
        PathValueClient.setDefaultCustomResponse("/test path/", "GET", "test response data");
        assertTrue(PathValueClient.removeDefaultCustomResponse("/test path/", "GET"));
    }

    @Test
    public void addServerMapping() throws Exception {
        String srcHost = "testSrc";
        String destHost = "testDest";
        String hostHeader = "testHost";
        List<ServerRedirect> originalList = client.getServerMappings();
        client.addServerMapping(srcHost, destHost, hostHeader);
        List<ServerRedirect> newList = client.getServerMappings();
        assertTrue(originalList.size() + 1 == newList.size());

        //validate host values
        Boolean found = false;
        for (ServerRedirect s : newList) {
            if (s.getSourceHost().compareTo(srcHost) == 0) {
                found = true;
                assertTrue(s.getDestinationHost().compareTo(destHost) == 0);
                assertTrue(s.getHostHeader().compareTo(hostHeader) == 0);
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void deleteServerMapping() throws Exception {
        String srcHost = "testDeleteSrc";
        String destHost = "testDeleteDest";
        ServerRedirect redirect = client.addServerMapping(srcHost, destHost, null);
        List<ServerRedirect> originalList = client.getServerMappings();

        List<ServerRedirect> newList = client.deleteServerMapping(redirect.getId());
        assertTrue(originalList.size() - 1 == newList.size());

        //validate host values
        Boolean found = false;
        for (ServerRedirect s : newList) {
            if (s.getSourceHost().compareTo(srcHost) == 0) {
                found = true;
                break;
            }
        }
        assertFalse(found);
    }

    @Test
    public void updateServerMapping() throws Exception {
        String srcHost = "testUpdateSrc";
        String updatedSrcHost = "testUpdatedSrc";
        String destHost = "testUpdateDest";
        String updatedDestHost = "testUpdatedDest";
        ServerRedirect serverMapping = client.addServerMapping(srcHost, destHost, null);

        List<ServerRedirect> serverList = client.getServerMappings();
        client.updateServerRedirectSrc(serverMapping.getId(), updatedSrcHost);
        client.updateServerRedirectDest(serverMapping.getId(), updatedDestHost);

        serverList = client.getServerMappings();

        for (ServerRedirect s : serverList) {
            if (s.getId() == serverMapping.getId()) {
                assertTrue(s.getSourceHost().compareTo(updatedSrcHost) == 0);
                assertTrue(s.getDestinationHost().compareTo(updatedDestHost) == 0);
                break;
            }
        }
    }

    @Test
    public void addServerGroup() throws Exception {
        String groupName = "addedGroup";

        List<ServerGroup> originalGroups = client.getServerGroups();
        client.addServerGroup(groupName);
        List<ServerGroup> newGroups = client.getServerGroups();

        assertTrue(originalGroups.size() + 1 == newGroups.size());

        Boolean found = false;
        for (ServerGroup s : newGroups) {
            if (s.getName().compareTo(groupName) == 0) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void deleteServerGroup() throws Exception {
        String groupName = "testDeleteGroup";

        ServerGroup addedGroup = client.addServerGroup(groupName);
        List<ServerGroup> originalGroups = client.getServerGroups();
        List<ServerGroup> newGroups = client.deleteServerGroup(addedGroup.getId());

        assertTrue(originalGroups.size() - 1 == newGroups.size());

        Boolean found = false;
        for (ServerGroup s : newGroups) {
            if (s.getName().compareTo(groupName) == 0) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    public void updateServerGroup() throws Exception {

        String groupName = "testUpdateGroup";
        String updatedGroupName = "testUpdatedGroup";

        ServerGroup addedGroup = client.addServerGroup(groupName);

        client.updateServerGroupName(addedGroup.getId(), updatedGroupName);
        List<ServerGroup> newGroups = client.getServerGroups();

        Boolean found = false;
        for (ServerGroup s : newGroups) {
            if (s.getId() == addedGroup.getId()) {
                found = true;
                assertTrue(s.getName().compareTo(updatedGroupName) == 0);
            }
        }
        assertTrue(found);
    }

    @Test
    public void activateServerGroup() throws Exception{
        String groupName = "addedGroup";
        String groupName2 = "secondGroup";

        client.addServerGroup(groupName);
        client.addServerGroup(groupName2);
        ServerGroup activeGroup = client.activateServerGroup(groupName2);
        assertTrue(activeGroup.getName().compareTo(groupName2) == 0);
    }
}
