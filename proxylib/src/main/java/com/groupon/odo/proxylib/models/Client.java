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

import com.groupon.odo.proxylib.HistoryService;
import com.fasterxml.jackson.annotation.JsonView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Client {

    private int id = -1;
    private String uuid = "";
    private boolean isActive = false;
    private Profile profile = null;
    private String friendlyName = "";
    private String lastAccessedFormatted = null;
    private int activeServerGroup;

    public Client() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public Profile getProfile() {
        return this.profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Long getLastAccessed() throws Exception {
        if (this.getlastAccessedFormatted() == null)
            return null;

        // 14 Mar 2013 15:59:06 GMT
        SimpleDateFormat parserSDF = new SimpleDateFormat("d MMM yyyy HH:mm:ss zzz");
        Date date = parserSDF.parse(this.getlastAccessedFormatted());
        return date.getTime();
    }

    public String getlastAccessedFormatted() throws Exception {
        if (this.lastAccessedFormatted == null) {
            History[] history;
            history = HistoryService.Companion.getInstance().getHistory(this.profile.getId(), this.uuid, 0, 1, false, null, false);

            if (history.length != 0)
                this.lastAccessedFormatted = history[0].getCreatedAt();
        }

        return this.lastAccessedFormatted;
    }


    @JsonView(ViewFilters.BackupIgnore.class)
    public int getActiveServerGroup() {
        return this.activeServerGroup;
    }

    public void setActiveServerGroup(int serverGroupId) {
        this.activeServerGroup = serverGroupId;
    }
}
