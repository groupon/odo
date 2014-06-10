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
package com.groupon.odo.controllers.models;

public class Identifiers {
    private Integer profileId = null;
    private Integer pathId = null;
    private Integer overrideId = null;

    public Identifiers() {
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getProfileId() {
        return this.profileId;
    }

    public void setPathId(Integer pathId) {
        this.pathId = pathId;
    }

    public Integer getPathId() {
        return this.pathId;
    }

    public void setOverrideId(Integer overrideId) {
        this.overrideId = overrideId;
    }

    public Integer getOverrideId() {
        return this.overrideId;
    }
}
