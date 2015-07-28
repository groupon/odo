package com.groupon.odo.proxylib.models.backup;

import com.groupon.odo.proxylib.models.ServerGroup;
import java.util.ArrayList;
import java.util.List;

public class SingleProfileBackup {
    private ArrayList<PathOverride> enabledPaths;
    private ServerGroup activeServerGroup;

    public ArrayList<PathOverride> getEnabledPaths() {
        return enabledPaths;
    }

    public void setEnabledPaths(List<PathOverride> enabledPaths) {
        this.enabledPaths = new ArrayList<>(enabledPaths);
    }

    public ServerGroup getActiveServerGroup() {
        return activeServerGroup;
    }

    public void setActiveServerGroup(ServerGroup activeServerGroup) {
        this.activeServerGroup = activeServerGroup;
    }
}
