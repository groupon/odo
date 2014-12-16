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

import com.groupon.odo.proxylib.hostsedit.HostsFileUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;


public class ServerRedirect {
    private String region;
    private String srcUrl;
    private String destUrl;
    private String hostHeader;
    private int id;
    private int profileId;
    private HostsEntry hostsEntry = new HostsEntry();

    public ServerRedirect(int id, String region, String srcUrl, String destUrl, String hostHeader) {
        setId(id);
        setRegion(region);
        setSrcUrl(srcUrl);
        setDestUrl(destUrl);
        setHostHeader(hostHeader);
    }

    public ServerRedirect() {
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getId() {
        return this.id;
    }

    public void setProfileId(int id) {
        this.profileId = id;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getProfileId() {
        return this.profileId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
        this.hostsEntry.setSrcUrl(srcUrl);
    }

    public String getDestUrl() {
        return destUrl;
    }

    public void setDestUrl(String destUrl) {
        this.destUrl = destUrl;
    }

    public String getHostHeader() {
        return hostHeader;
    }

    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public HostsEntry getHostsEntry() {
        return this.hostsEntry;
    }

    private class HostsEntry {
        // needed for jackson serialization
        private boolean enabled;
        private boolean exists;

        public HostsEntry() {

        }

        private String srcUrl = null;

        public void setSrcUrl(String srcUrl) {
            this.srcUrl = srcUrl;
        }

        public boolean getEnabled() {
            if (srcUrl != null) {
                try {
                    return HostsFileUtils.isEnabled(srcUrl);
                } catch (Exception e) {
                    return false;
                }
            }

            return false;
        }

        @JsonIgnore
        public void setEnabled() {
        }

        @JsonIgnore
        public void setExists() {
        }

        public boolean getExists() {
            if (srcUrl != null) {
                try {
                    return HostsFileUtils.exists(srcUrl);
                } catch (Exception e) {
                    return false;
                }
            }

            return false;
        }
    }
}
