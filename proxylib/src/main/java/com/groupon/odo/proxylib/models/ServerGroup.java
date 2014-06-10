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
package com.groupon.odo.proxylib.models;

import org.codehaus.jackson.map.annotate.JsonView;

import java.util.ArrayList;
import java.util.List;

public class ServerGroup {
    private int id;
    private String name;
    private int profileId;
    private ArrayList<ServerRedirect> servers;

    public ServerGroup(int id, String name, int profileId) {
        setId(id);
        setName(name);
        setProfileId(profileId);
    }

    public ServerGroup() {
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setProfileId(int id) {
        this.profileId = id;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getProfileId() {
        return this.profileId;
    }

    public List<ServerRedirect> getServers() {
        return this.servers;
    }

    public void setServers(List<ServerRedirect> servers) {
        this.servers = new ArrayList<ServerRedirect>(servers);
    }
}
