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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonView;

public class Method {
    private int id;
    private String idString;
    private int httpCode = 200;
    private String description = null;
    private String methodName = null;
    private String className = null;
    private String methodType = null;
    private Object[] methodArguments = new Object[0];
    private String[] methodArgumentNames = new String[0];
    private String[] methodDefaultArguments = new String[0];
    private java.lang.reflect.Method method = null;
    private boolean blockRequest = false;
    private int overrideVersion;

    public void setId(int id) {
        this.id = id;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getId() {
        return this.id;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getHttpCode() {
        return this.httpCode;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String getIdString() {
        return this.idString;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String getDescription() {
        return this.description;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String getMethodType() {
        return this.methodType;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setMethodArguments(Object[] args) {
        this.methodArguments = args;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public Object[] getMethodArguments() {
        return this.methodArguments;
    }

    public void setMethodArgumentNames(String[] argNames) {
        this.methodArgumentNames = argNames;
    }

    public void setMethodDefaultArguments(String[] defaultArguments) {
        this.methodDefaultArguments = defaultArguments;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String[] getMethodArgumentNames() {
        return this.methodArgumentNames;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public String[] getMethodDefaultArguments() {
        return this.methodDefaultArguments;
    }

    public void setMethod(java.lang.reflect.Method method) {
        this.method = method;
    }

    // do not put this in JSON output
    @JsonIgnore
    public java.lang.reflect.Method getMethod() {
        return this.method;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public boolean isBlockRequest() {
        return blockRequest;
    }

    public void setBlockRequest(boolean blockRequest) {
        this.blockRequest = blockRequest;
    }

    @JsonView(ViewFilters.BackupIgnore.class)
    public int getOverrideVersion() {
        return overrideVersion;
    }

    public void setOverrideVersion(int overrideVersion) {
        this.overrideVersion = overrideVersion;
    }
}
