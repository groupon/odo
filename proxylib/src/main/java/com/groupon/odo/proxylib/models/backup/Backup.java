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

import com.groupon.odo.proxylib.models.Group;
import com.groupon.odo.proxylib.models.Script;

import java.util.ArrayList;
import java.util.List;

public class Backup {
    private ArrayList<Group> groups;
    private ArrayList<Profile> profiles;
    private ArrayList<Script> scripts;

    public List<Group> getGroups() {
        return this.groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = new ArrayList<Group>(groups);
    }

    public List<Profile> getProfiles() {
        return this.profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = new ArrayList<Profile>(profiles);
    }

    public List<Script> getScripts() {
        return this.scripts;
    }

    public void setScripts(List<Script> scripts) {
        this.scripts = new ArrayList<Script>(scripts);
    }
}
