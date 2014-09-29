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


/**
 * Represents a single history object
 */
public class History {
    public static final String PROFILE_CLIENT_DEFAULT_ID = "-1";
    private int id = -1;
    private int profileId = -1;
    private String clientUUID = PROFILE_CLIENT_DEFAULT_ID;
    private String createdAt = "";
    private String requestType = "";
    private String requestURL = "";
    private String requestParams = "";
    private String requestPostData = "";
    private String requestHeaders = "";
    private String responseCode = "";
    private String responseHeaders = "";
    private String responseContentType = "";
    private String responseData = "";
    private String originalRequestURL = "";
    private String originalRequestParams = "";
    private String originalRequestPostData = "";
    private String originalRequestHeaders = "";
    private String originalResponseCode = "";
    private String originalResponseHeaders = "";
    private String originalResponseContentType = "";
    private String originalResponseData = "";
    private boolean modified = false;
    private boolean requestSent = true;


    public History() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setProfileId(int profile_id) {
        this.profileId = profile_id;
    }

    public void setCreatedAt(String timestamp) {
        this.createdAt = timestamp;
    }

    public void setRequestType(String type) {
        this.requestType = type;
    }

    public void setRequestURL(String url) {
        this.requestURL = url;
    }

    public void setRequestParams(String params) {
        this.requestParams = params;
    }

    public void setRequestPostData(String data) {
        this.requestPostData = data;
    }

    public void setRequestHeaders(String headers) {
        this.requestHeaders = headers;
    }

    public void setResponseCode(String code) {
        this.responseCode = code;
    }

    public void setResponseHeaders(String headers) {
        this.responseHeaders = headers;
    }

    public void setResponseContentType(String type) {
        this.responseContentType = type;
    }

    public void setResponseData(String data) {
        this.responseData = data;
    }

    public int getProfileId() {
        return this.profileId;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public String getRequestType() {
        return this.requestType;
    }

    public String getRequestURL() {
        return this.requestURL;
    }

    public String getRequestParams() {
        return this.requestParams;
    }

    public String getRequestPostData() {
        return this.requestPostData;
    }

    public String getRequestHeaders() {
        return this.requestHeaders;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public String getResponseHeaders() {
        return this.responseHeaders;
    }

    public String getResponseContentType() {
        return this.responseContentType;
    }

    public String getResponseData() {
        return this.responseData;
    }

    public String getClientUUID() {
        return clientUUID;
    }

    public void setClientUUID(String clientUUID) {
        this.clientUUID = clientUUID;
    }

    public String getOriginalRequestURL() {
        return originalRequestURL;
    }

    public void setOriginalRequestURL(String originalRequestURL) {
        this.originalRequestURL = originalRequestURL;
    }

    public String getOriginalRequestParams() {
        return originalRequestParams;
    }

    public void setOriginalRequestParams(String originalRequestParams) {
        this.originalRequestParams = originalRequestParams;
    }

    public String getOriginalRequestPostData() {
        return originalRequestPostData;
    }

    public void setOriginalRequestPostData(String originalRequestPostData) {
        this.originalRequestPostData = originalRequestPostData;
    }

    public String getOriginalRequestHeaders() {
        return originalRequestHeaders;
    }

    public void setOriginalRequestHeaders(String originalRequestHeaders) {
        this.originalRequestHeaders = originalRequestHeaders;
    }

    public String getOriginalResponseCode() {
        return originalResponseCode;
    }

    public void setOriginalResponseCode(String originalResponseCode) {
        this.originalResponseCode = originalResponseCode;
    }

    public String getOriginalResponseHeaders() {
        return originalResponseHeaders;
    }

    public void setOriginalResponseHeaders(String originalResponseHeaders) {
        this.originalResponseHeaders = originalResponseHeaders;
    }

    public String getOriginalResponseContentType() {
        return originalResponseContentType;
    }

    public void setOriginalResponseContentType(String originalResponseContentType) {
        this.originalResponseContentType = originalResponseContentType;
    }

    public String getOriginalResponseData() {
        return originalResponseData;
    }

    public void setOriginalResponseData(String originalResponseData) {
        this.originalResponseData = originalResponseData;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean getRequestSent() {
        return requestSent;
    }

    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }

}
