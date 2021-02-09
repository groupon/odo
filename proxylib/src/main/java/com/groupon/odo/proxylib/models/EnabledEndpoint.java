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

import com.fasterxml.jackson.annotation.JsonView;
import com.groupon.odo.proxylib.OverrideService;

public class EnabledEndpoint {
    private int id;
    private int pathId;
    private int overrideId;
    private int priority;
    private Object[] arguments = new Object[0];
    private com.groupon.odo.proxylib.models.Method method = null;
    private int repeatNumber = -1;
    private String responseCode = "";

    public EnabledEndpoint() {
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getPathId() {
        return this.pathId;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    public int getOverrideId() {
        return this.overrideId;
    }

    public void setOverrideId(int overrideId) {
        this.overrideId = overrideId;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Object[] getArguments() {
        return this.arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public com.groupon.odo.proxylib.models.Method getMethodInformation() {
        return this.method;
    }

    public void setMethodInformation(com.groupon.odo.proxylib.models.Method method) {
        this.method = method;
    }

    public int getRepeatNumber() {
        return repeatNumber;
    }

    public void setRepeatNumber(int repeatNumber) {
        this.repeatNumber = repeatNumber;
    }

    public String getResponseCode() { return responseCode; }

    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    /**
     * This decrements repeat number until it reaches 0
     */
    public void decrementRepeatNumber() throws Exception {
        if (this.repeatNumber > 0)
            OverrideService.Companion.getInstance().updateRepeatNumber(this.id, this.repeatNumber - 1);

        this.repeatNumber--;
    }
}
