package com.groupon.odo.proxylib.models.backup;

import com.groupon.odo.proxylib.models.EnabledEndpoint;
import java.util.ArrayList;
import java.util.List;

public class PathOverride {
    private ArrayList<EnabledEndpoint> enabledEndpoints;
    private boolean requestEnabled;
    private boolean responseEnabled;
    private String pathName;

    public ArrayList<EnabledEndpoint> getEnabledEndpoints() {
        return enabledEndpoints;
    }

    public void setEnabledEndpoints(List<EnabledEndpoint> enabledEndpoints) {
        this.enabledEndpoints = new ArrayList<>(enabledEndpoints);
    }

    public boolean getRequestEnabled() {
        return requestEnabled;
    }

    public void setRequestEnabled(boolean requestEnabled) {
        this.requestEnabled = requestEnabled;
    }

    public boolean getResponseEnabled() {
        return responseEnabled;
    }

    public void setResponseEnabled(boolean responseEnabled) {
        this.responseEnabled = responseEnabled;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathname) {
        this.pathName = pathname;
    }
}
