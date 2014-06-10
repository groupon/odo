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
package com.groupon.odo.proxylib.models.backup;

import com.groupon.odo.proxylib.models.EndpointOverride;
import com.groupon.odo.proxylib.models.ServerGroup;
import com.groupon.odo.proxylib.models.ServerRedirect;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    private String name;
    private ArrayList<EndpointOverride> paths;
    private ArrayList<ServerRedirect> servers;
    private ArrayList<ServerGroup> serverGroups;
    private boolean active = false;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setPaths(List<EndpointOverride> paths) {
        this.paths = new ArrayList<EndpointOverride>(paths);
    }

    public List<EndpointOverride> getPaths() {
        return this.paths;
    }

    public void setServers(List<ServerRedirect> servers) {
        this.servers = new ArrayList<ServerRedirect>(servers);
    }

    public List<ServerRedirect> getServers() {
        return this.servers;
    }

    public void setServerGroups(List<ServerGroup> serverGroups) {
        this.serverGroups = new ArrayList<ServerGroup>(serverGroups);
    }

    public List<ServerGroup> getServerGroups() {
        return this.serverGroups;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return this.active;
    }
}
