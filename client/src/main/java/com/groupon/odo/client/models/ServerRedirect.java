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
package com.groupon.odo.client.models;

public class ServerRedirect {
    private int id;
    private String sourceHost;
    private String destinationHost;
    private String hostHeader;
    private int profileId;

    public int getId() { return this.id; }
    public String getSourceHost() {return this.sourceHost; }
    public String getDestinationHost() {return this.destinationHost; }
    public String getHostHeader() {return this.hostHeader; }
    public int getProfileId() {return this.profileId; }

    public void setId(int id) { this.id = id; }
    public void setSourceHost(String sourceHost) { this.sourceHost = sourceHost; }
    public void setDestinationHost(String destinationHost) { this.destinationHost = destinationHost; }
    public void setHostHeader(String hostHeader) {this.hostHeader = hostHeader; }
    public void setProfileId(int profileId) { this.profileId = profileId; }
}
