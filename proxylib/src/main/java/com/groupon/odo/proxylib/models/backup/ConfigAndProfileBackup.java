package com.groupon.odo.proxylib.models.backup;

public class ConfigAndProfileBackup {
    private Backup odoBackup;
    private SingleProfileBackup profileBackup;

    public Backup getOdoBackup() {
        return odoBackup;
    }

    public void setOdoBackup(Backup backup) {
        this.odoBackup = backup;
    }

    public SingleProfileBackup getProfileBackup() {
        return profileBackup;
    }

    public void setProfileBackup(SingleProfileBackup profileBackup) {
        this.profileBackup = profileBackup;
    }
}
