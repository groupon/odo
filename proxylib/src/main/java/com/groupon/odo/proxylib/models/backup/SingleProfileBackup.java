package com.groupon.odo.proxylib.models.backup;

import com.groupon.odo.proxylib.hostsedit.rmi.Server;
import com.groupon.odo.proxylib.models.ServerGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dorenfro on 7/20/15.
 */
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
