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
import com.groupon.odo.proxylib.*;

import java.util.ArrayList;
import java.util.List;

public class EndpointOverride {
    private int pathId;
    private String pathName;
    private String path;
    private String bodyFilter;
    private String contentType = "";
    private int requestType = Constants.REQUEST_TYPE_GET;
    private String groupIds;
    private List<Method> possibleEndpoints = null;
    private List<EnabledEndpoint> enabledEndpoints = null;
    private int profileId = -1;
    private String clientUUID;
    private int repeatNumber;
    private boolean responseEnabled = false;
    private boolean requestEnabled = false;
    private String customResponse = null;
    private String customRequest = null;
    private boolean global = false;
    private ArrayList<String> groupNames = null;
    private String[] filters = null;

    //this is not used, not sure if it will be...but its here just in case
    public EndpointOverride(int id, String pathname, ArrayList<Object> initialEndpoints,
                            ArrayList<EnabledEndpoint> enabledEndpoints) {
        setPathName(pathname);
        setEnabledEndpoints(enabledEndpoints);
        setPathId(id);
    }

    public EndpointOverride() {
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getPathId() {
        return this.pathId;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    public int getRequestType() {
        return this.requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathname) {
        this.pathName = pathname;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void setBodyFilter(String bodyFilter) {
        this.bodyFilter = bodyFilter;
    }

    public String getBodyFilter() {
        return this.bodyFilter;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String getGroupIds() {
        // Strip leading and trailing commas
        // This will eventually not be needed once they are stripped from the insert
        if (this.groupIds != null) {
            if (this.groupIds.equals(",")) {
                this.groupIds = "";
            }
            if (this.groupIds.startsWith(",") && this.groupIds.length() > 1) {
                this.groupIds = this.groupIds.substring(1);
            }
            if (this.groupIds.endsWith(",")) {
                this.groupIds = this.groupIds.substring(0, this.groupIds.length() - 1);
            }
        }

        return this.groupIds;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public List<Method> getPossibleEndpoints() throws Exception {
        if (possibleEndpoints == null) {
            possibleEndpoints = EditService.getInstance().getMethodsFromGroupIds(Utils.arrayFromStringOfIntegers(this.getGroupIds()), filters);
        }
        return possibleEndpoints;
    }

    public void setPossibleEndpoints(ArrayList<Method> possibleEndpoints) {
        this.possibleEndpoints = possibleEndpoints;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public List<EnabledEndpoint> getEnabledEndpoints() throws Exception {
        if (enabledEndpoints == null) {
            enabledEndpoints = OverrideService.Companion.getServiceInstance().getEnabledEndpoints(this.getPathId(), clientUUID, this.filters);
        }

        return enabledEndpoints;
    }

    public void setEnabledEndpoints(ArrayList<EnabledEndpoint> enabledEndpoints) {
        this.enabledEndpoints = enabledEndpoints;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public boolean getResponseEnabled() {
        return this.responseEnabled;
    }

    public void setResponseEnabled(boolean responseEnabled) {
        this.responseEnabled = responseEnabled;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public boolean getRequestEnabled() {
        return this.requestEnabled;
    }

    public void setRequestEnabled(boolean requestEnabled) {
        this.requestEnabled = requestEnabled;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getRepeatNumber() {
        return repeatNumber;
    }

    public void setRepeatNumber(int repeatNumber) {
        this.repeatNumber = repeatNumber;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String getCustomResponse() {
        if (this.customResponse == null) {
            try {
                this.customResponse = PathOverrideService.getInstance().getCustomData(this.pathId, this.clientUUID, Constants.REQUEST_RESPONSE_CUSTOM_RESPONSE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.customResponse;
    }

    public void setCustomResponse(String customResponse) {
        this.customResponse = customResponse;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String getCustomRequest() {
        if (this.customRequest == null) {
            // get it
            try {
                this.customRequest = PathOverrideService.getInstance().getCustomData(this.pathId, this.clientUUID, Constants.REQUEST_RESPONSE_CUSTOM_REQUEST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.customRequest;
    }

    public void setCustomRequest(String customRequest) {
        this.customRequest = customRequest;
    }

    /**
     * This updates the repeat # in the database
     *
     * @param repeatNumber
     */
    public void updateRepeatNumber(int repeatNumber) throws Exception {
        EditService.getInstance().updateRepeatNumber(repeatNumber, this.pathId, this.clientUUID);
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String getClientUUID() {
        return clientUUID;
    }

    public void setClientUUID(String clientUUID) {
        this.clientUUID = clientUUID;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns if override is Global
     *
     * @return
     */
    public Boolean getGlobal() {
        return this.global;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }

    public void setGroupNames(ArrayList<String> groupNames) {
        this.groupNames = groupNames;
    }

    public List<String> getGroupNames() {
        if (groupNames == null) {
            groupNames = new ArrayList<String>();
            for (int id : Utils.arrayFromStringOfIntegers(this.getGroupIds())) {
                groupNames.add(PathOverrideService.getInstance().getGroupNameFromId(id));
            }
        }

        return groupNames;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String[] getFilters() {
        return this.filters;
    }

    /**
     * Stores the filters used to get this endpoint
     *
     * @param filters
     */
    public void setFilters(String[] filters) {
        this.filters = filters;
    }
}
