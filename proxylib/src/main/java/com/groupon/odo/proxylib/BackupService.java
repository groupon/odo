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
package com.groupon.odo.proxylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupon.odo.proxylib.models.Client;
import com.groupon.odo.proxylib.models.EndpointOverride;
import com.groupon.odo.proxylib.models.Group;
import com.groupon.odo.proxylib.models.Method;
import com.groupon.odo.proxylib.models.Script;
import com.groupon.odo.proxylib.models.ServerGroup;
import com.groupon.odo.proxylib.models.ServerRedirect;
import com.groupon.odo.proxylib.models.backup.Backup;
import com.groupon.odo.proxylib.models.backup.ConfigAndProfileBackup;
import com.groupon.odo.proxylib.models.backup.PathOverride;
import com.groupon.odo.proxylib.models.backup.Profile;
import com.groupon.odo.proxylib.models.backup.SingleProfileBackup;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.net.ssl.HostnameVerifier;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static BackupService serviceInstance = null;

    public BackupService() {

    }

    public static BackupService getInstance() {
        if (serviceInstance == null) {
            serviceInstance = new BackupService();
        }
        return serviceInstance;
    }

    /**
     * Get all Groups
     *
     * @return
     * @throws Exception
     */
    private List<Group> getGroups() throws Exception {
        List<Group> groups = new ArrayList<Group>();
        List<Group> sourceGroups = PathOverrideService.getInstance().findAllGroups();

        // loop through the groups
        for (Group sourceGroup : sourceGroups) {
            Group group = new Group();

            // add all methods
            ArrayList<Method> methods = new ArrayList<Method>();
            for (Method sourceMethod : EditService.getInstance().getMethodsFromGroupId(sourceGroup.getId(), null)) {
                Method method = new Method();
                method.setClassName(sourceMethod.getClassName());
                method.setMethodName(sourceMethod.getMethodName());
                methods.add(method);
            }

            group.setMethods(methods);
            group.setName(sourceGroup.getName());
            groups.add(group);
        }

        return groups;
    }

    private List<Profile> getProfiles() throws Exception {
        ArrayList<Profile> profiles = new ArrayList<Profile>();

        for (com.groupon.odo.proxylib.models.Profile sourceProfile : ProfileService.getInstance().findAllProfiles()) {
            Profile profile = new Profile();
            profile.setName(sourceProfile.getName());

            // get paths
            profile.setPaths(PathOverrideService.getInstance().getPaths(sourceProfile.getId(), Constants.PROFILE_CLIENT_DEFAULT_ID, null));

            // get default servers
            profile.setServers(ServerRedirectService.getInstance().tableServers(sourceProfile.getId(), 0));

            //get server groups
            profile.setServerGroups(ServerRedirectService.getInstance().tableServerGroups(sourceProfile.getId()));

            // set active
            profile.setActive(ProfileService.getInstance().isActive(sourceProfile.getId()));
            profiles.add(profile);
        }
        return profiles;
    }

    /**
     * Get the active overrides with parameters and the active server group for a client
     *
     * @param profileID Id of profile to get configuration for
     * @param clientUUID Client Id to export configuration
     * @return SingleProfileBackup containing active overrides and active server group
     * @throws Exception exception
     */
    public SingleProfileBackup getProfileBackupData(int profileID, String clientUUID) throws Exception {
        SingleProfileBackup singleProfileBackup = new SingleProfileBackup();
        List<PathOverride> enabledPaths = new ArrayList<>();

        List<EndpointOverride> paths = PathOverrideService.getInstance().getPaths(profileID, clientUUID, null);
        for (EndpointOverride override : paths) {
            if (override.getRequestEnabled() || override.getResponseEnabled()) {
                PathOverride pathOverride = new PathOverride();
                pathOverride.setPathName(override.getPathName());
                if (override.getRequestEnabled()) {
                    pathOverride.setRequestEnabled(true);
                }
                if (override.getResponseEnabled()) {
                    pathOverride.setResponseEnabled(true);
                }

                pathOverride.setEnabledEndpoints(override.getEnabledEndpoints());
                enabledPaths.add(pathOverride);
            }
        }
        singleProfileBackup.setEnabledPaths(enabledPaths);

        Client backupClient = ClientService.getInstance().findClient(clientUUID, profileID);
        ServerGroup activeServerGroup = ServerRedirectService.getInstance().getServerGroup(backupClient.getActiveServerGroup(), profileID);
        singleProfileBackup.setActiveServerGroup(activeServerGroup);

        return singleProfileBackup;
    }

    /**
     * Get the single profile backup (active overrides and active server group) for a client
     * and the full odo backup
     *
     * @param profileID Id of profile to get configuration for
     * @param clientUUID Client Id to export configuration
     * @return Odo backup and client backup
     * @throws Exception exception
     */
    public ConfigAndProfileBackup getConfigAndProfileData(int profileID, String clientUUID) throws Exception {
        SingleProfileBackup singleProfileBackup = getProfileBackupData(profileID, clientUUID);
        Backup backup = getBackupData();

        ConfigAndProfileBackup configAndProfileBackup = new ConfigAndProfileBackup();
        configAndProfileBackup.setOdoBackup(backup);
        configAndProfileBackup.setProfileBackup(singleProfileBackup);

        return configAndProfileBackup;
    }

    /**
     * Return the structured backup data
     *
     * @return Backup of current configuration
     * @throws Exception exception
     */
    public Backup getBackupData() throws Exception {
        Backup backupData = new Backup();

        backupData.setGroups(getGroups());
        backupData.setProfiles(getProfiles());
        ArrayList<Script> scripts = new ArrayList<Script>();
        Collections.addAll(scripts, ScriptService.getInstance().getScripts());
        backupData.setScripts(scripts);

        return backupData;
    }

    /**
     * Restore configuration from backup data
     *
     * @param streamData InputStream for configuration to restore
     * @return true if succeeded, false if operation failed
     */
    public boolean restoreBackupData(InputStream streamData) {
        // convert stream to string
        java.util.Scanner s = new java.util.Scanner(streamData).useDelimiter("\\A");
        String data = s.hasNext() ? s.next() : "";

        // parse JSON
        ObjectMapper mapper = new ObjectMapper();
        Backup backupData = null;
        try {
            backupData = mapper.readValue(data, Backup.class);
        } catch (Exception e) {
            logger.error("Could not parse input data: {}, {}", e.getClass(), e.getMessage());
            return false;
        }

        // TODO: validate json against a schema for safety

        // GROUPS
        try {
            logger.info("Number of groups: {}", backupData.getGroups().size());

            for (Group group : backupData.getGroups()) {
                // determine if group already exists.. if not then add it
                Integer groupId = PathOverrideService.getInstance().getGroupIdFromName(group.getName());
                if (groupId == null) {
                    groupId = PathOverrideService.getInstance().addGroup(group.getName());
                }

                // get all methods from the group.. we are going to remove ones that don't exist in the new configuration
                List<Method> originalMethods = EditService.getInstance().getMethodsFromGroupId(groupId, null);

                for (Method originalMethod : originalMethods) {
                    Boolean matchInImportGroup = false;

                    int importCount = 0;
                    for (Method importMethod : group.getMethods()) {
                        if (originalMethod.getClassName().equals(importMethod.getClassName()) &&
                            originalMethod.getMethodName().equals(importMethod.getMethodName())) {
                            matchInImportGroup = true;
                            break;
                        }
                        importCount++;
                    }

                    if (!matchInImportGroup) {
                        // remove it from current database since it is a delta to the current import
                        PathOverrideService.getInstance().removeOverride(originalMethod.getId());
                    } else {
                        // remove from import list since it already exists
                        group.getMethods().remove(importCount);
                    }
                }

                // add methods to groups
                for (Method method : group.getMethods()) {
                    PathOverrideService.getInstance().createOverride(groupId, method.getMethodName(), method.getClassName());
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // PROFILES
        try {
            logger.info("Number of profiles: {}", backupData.getProfiles().size());

            // remove all servers
            // don't care about deltas here.. we'll just recreate them all
            // removed default servers (belong to group id=0)
            ServerRedirectService.getInstance().deleteServerGroup(0);

            for (com.groupon.odo.proxylib.models.backup.Profile profile : backupData.getProfiles()) {
                // see if a profile with this name already exists
                Integer profileId = ProfileService.getInstance().getIdFromName(profile.getName());
                com.groupon.odo.proxylib.models.Profile newProfile;
                if (profileId == null) {
                    // create new profile
                    newProfile = ProfileService.getInstance().add(profile.getName());
                } else {
                    // get the existing profile
                    newProfile = ProfileService.getInstance().findProfile(profileId);
                }

                // add new servers
                if (profile.getServers() != null) {
                    for (ServerRedirect server : profile.getServers()) {
                        ServerRedirectService.getInstance().addServerRedirect(server.getRegion(), server.getSrcUrl(), server.getDestUrl(), server.getHostHeader(), newProfile.getId(), 0);
                    }
                }

                // remove all server groups
                for (ServerGroup group : ServerRedirectService.getInstance().tableServerGroups(newProfile.getId())) {
                    ServerRedirectService.getInstance().deleteServerGroup(group.getId());
                }

                // add new server groups
                if (profile.getServerGroups() != null) {
                    for (ServerGroup group : profile.getServerGroups()) {
                        int groupId = ServerRedirectService.getInstance().addServerGroup(group.getName(), newProfile.getId());
                        for (ServerRedirect server : group.getServers()) {
                            ServerRedirectService.getInstance().addServerRedirect(server.getRegion(), server.getSrcUrl(), server.getDestUrl(), server.getHostHeader(), newProfile.getId(), groupId);
                        }
                    }
                }

                // remove all paths
                // don't care about deltas here.. we'll just recreate them all
                for (EndpointOverride path : PathOverrideService.getInstance().getPaths(newProfile.getId(), Constants.PROFILE_CLIENT_DEFAULT_ID, null)) {
                    PathOverrideService.getInstance().removePath(path.getPathId());
                }

                // add new paths
                if (profile.getPaths() != null) {
                    for (EndpointOverride path : profile.getPaths()) {
                        int pathId = PathOverrideService.getInstance().addPathnameToProfile(newProfile.getId(), path.getPathName(), path.getPath());

                        PathOverrideService.getInstance().setContentType(pathId, path.getContentType());
                        PathOverrideService.getInstance().setRequestType(pathId, path.getRequestType());
                        PathOverrideService.getInstance().setGlobal(pathId, path.getGlobal());

                        // add groups to path
                        for (String groupName : path.getGroupNames()) {
                            int groupId = PathOverrideService.getInstance().getGroupIdFromName(groupName);
                            PathOverrideService.getInstance().AddGroupByNumber(newProfile.getId(), pathId, groupId);
                        }
                    }
                }

                // set active
                ClientService.getInstance().updateActive(newProfile.getId(), Constants.PROFILE_CLIENT_DEFAULT_ID, profile.getActive());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // SCRIPTS
        try {
            // delete all scripts
            for (Script script : ScriptService.getInstance().getScripts()) {
                ScriptService.getInstance().removeScript(script.getId());
            }

            // add scripts
            for (Script script : backupData.getScripts()) {
                ScriptService.getInstance().addScript(script.getName(), script.getScript());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // tell http/https proxies to reload plugins
        try {
            org.apache.http.conn.ssl.SSLSocketFactory sslsf = new org.apache.http.conn.ssl.SSLSocketFactory(new TrustStrategy() {
                @Override
                public boolean isTrusted(
                    final X509Certificate[] chain, String authType) throws CertificateException {
                    // ignore SSL cert issues
                    return true;
                }
            });
            HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            sslsf.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            for (String connectstring : getConnectorStrings("https_proxy")) {
                HttpGet request = new HttpGet(connectstring + "/proxy/reload");

                HttpClient httpClient = new org.apache.http.impl.client.DefaultHttpClient();
                String[] parts = connectstring.split(":");
                httpClient.getConnectionManager().getSchemeRegistry().register(new org.apache.http.conn.scheme.Scheme("https", Integer.parseInt(parts[parts.length - 1]), sslsf));
                HttpResponse response = httpClient.execute(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception caught during proxy reload.  Things may be in an inconsistent state.");
        }

        // restart plugin service for this process
        PluginManager.destroy();

        return true;
    }

    /**
     * Returns an MBeanServer with the specified name
     *
     * @param name
     * @return
     */
    private MBeanServer getServerForName(String name) {
        try {
            MBeanServer mbeanServer = null;
            final ObjectName objectNameQuery = new ObjectName(name + ":type=Service,*");

            for (final MBeanServer server : MBeanServerFactory.findMBeanServer(null)) {
                if (server.queryNames(objectNameQuery, null).size() > 0) {
                    mbeanServer = server;
                    // we found it, bail out
                    break;
                }
            }

            return mbeanServer;
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * Get a list of strings(scheme + host + port) that the specified connector is running on
     *
     * @param name
     * @return
     */
    private ArrayList<String> getConnectorStrings(String name) {
        ArrayList<String> connectorStrings = new ArrayList<String>();

        try {
            MBeanServer mbeanServer = getServerForName(name);
            Set<ObjectName> objs = mbeanServer.queryNames(new ObjectName("*:type=Connector,*"), null);
            String hostname = InetAddress.getLocalHost().getHostName();
            InetAddress[] addresses = InetAddress.getAllByName(hostname);

            for (Iterator<ObjectName> i = objs.iterator(); i.hasNext(); ) {
                ObjectName obj = i.next();
                String scheme = mbeanServer.getAttribute(obj, "scheme").toString();
                String port = obj.getKeyProperty("port");
                connectorStrings.add(scheme + "://localhost:" + port);
                logger.info("Adding: {}", scheme + "://localhost:" + port);
            }
        } catch (Exception e) {
        }

        return connectorStrings;
    }

    /**
     * @param groupName Name of server group to get ID for
     * @param profileId Profile ID server group is in
     * @return ID of group
     */
    private int getServerIdFromName(String groupName, int profileId) {
        List<ServerGroup> serverGroups = ServerRedirectService.getInstance().tableServerGroups(profileId);
        if (groupName.equals("Default")) {
            return 0;
        }
        for (ServerGroup group : serverGroups) {
            if (groupName.compareTo(group.getName()) == 0) {
                return group.getId();
            }
        }
        return -1;
    }

    /**
     * 1. Resets profile to get fresh slate
     * 2. Updates active server group to one from json
     * 3. For each path in json, sets request/response enabled
     * 4. Adds active overrides to each path
     * 5. Update arguments and repeat count for each override
     *
     * @param profileBackup JSON containing server configuration and overrides to activate
     * @param profileId Profile to update
     * @param clientUUID Client UUID to apply update to
     * @throws Exception Array of errors for things that could not be imported
     */
    public void setProfileFromBackup(JSONObject profileBackup, int profileId, String clientUUID) throws Exception {
        // Reset the profile before applying changes
        ClientService clientService = ClientService.getInstance();
        clientService.reset(profileId, clientUUID);
        clientService.updateActive(profileId, clientUUID, true);
        JSONArray errors = new JSONArray();

        // Change to correct server group
        JSONObject activeServerGroup = profileBackup.getJSONObject(Constants.BACKUP_ACTIVE_SERVER_GROUP);
        int activeServerId = getServerIdFromName(activeServerGroup.getString(Constants.NAME), profileId);
        if (activeServerId == -1) {
            errors.put(formErrorJson("Server Error", "Cannot change to '" + activeServerGroup.getString(Constants.NAME) + "' - Check Server Group Exists"));
        } else {
            Client clientToUpdate = ClientService.getInstance().findClient(clientUUID, profileId);
            ServerRedirectService.getInstance().activateServerGroup(activeServerId, clientToUpdate.getId());
        }

        JSONArray enabledPaths = profileBackup.getJSONArray(Constants.ENABLED_PATHS);
        PathOverrideService pathOverrideService = PathOverrideService.getInstance();
        OverrideService overrideService = OverrideService.getInstance();

        for (int i = 0; i < enabledPaths.length(); i++) {
            JSONObject path = enabledPaths.getJSONObject(i);
            int pathId = pathOverrideService.getPathId(path.getString(Constants.PATH_NAME), profileId);
            // Set path to have request/response enabled as necessary
            try {
                if (path.getBoolean(Constants.REQUEST_ENABLED)) {
                    pathOverrideService.setRequestEnabled(pathId, true, clientUUID);
                }

                if (path.getBoolean(Constants.RESPONSE_ENABLED)) {
                    pathOverrideService.setResponseEnabled(pathId, true, clientUUID);
                }
            } catch (Exception e) {
                errors.put(formErrorJson("Path Error", "Cannot update path: '" + path.getString(Constants.PATH_NAME) + "' - Check Path Exists"));
                continue;
            }

            JSONArray enabledOverrides = path.getJSONArray(Constants.ENABLED_ENDPOINTS);

            /**
             * 2 for loops to ensure overrides are added with correct priority
             * 1st loop is priority currently adding override to
             * 2nd loop is to find the override with matching priority in profile json
             */
            for (int j = 0; j < enabledOverrides.length(); j++) {
                for (int k = 0; k < enabledOverrides.length(); k++) {
                    JSONObject override = enabledOverrides.getJSONObject(k);
                    if (override.getInt(Constants.PRIORITY) != j) {
                        continue;
                    }

                    int overrideId;
                    // Name of method that can be used by error message as necessary later
                    String overrideNameForError = "";
                    // Get the Id of the override
                    try {
                        // If method information is null, then the override is a default override
                        if (override.get(Constants.METHOD_INFORMATION) != JSONObject.NULL) {
                            JSONObject methodInformation = override.getJSONObject(Constants.METHOD_INFORMATION);
                            overrideNameForError = methodInformation.getString(Constants.METHOD_NAME);
                            overrideId = overrideService.getOverrideIdForMethod(methodInformation.getString(Constants.CLASS_NAME),
                                                                                methodInformation.getString(Constants.METHOD_NAME));
                        } else {
                            overrideNameForError = "Default Override";
                            overrideId = override.getInt(Constants.OVERRIDE_ID);
                        }

                        // Enable override and set repeat number and arguments
                        overrideService.enableOverride(overrideId, pathId, clientUUID);
                        int overrideOrdinal = overrideService.getCurrentMethodOrdinal(overrideId, pathId, clientUUID, null);
                        overrideService.updateRepeatNumber(overrideId, pathId, overrideOrdinal, override.getInt(Constants.REPEAT_NUMBER), clientUUID);
                        overrideService.updateResponseCode(overrideId, pathId, overrideOrdinal, override.getString(Constants.RESPONSE_CODE), clientUUID);
                        overrideService.updateArguments(overrideId, pathId, overrideOrdinal, override.getString(Constants.ARGUMENTS), clientUUID);
                    } catch (Exception e) {
                        errors.put(formErrorJson("Override Error", "Cannot add/update override: '" + overrideNameForError + "' - Check Override Exists"));
                        continue;
                    }
                }
            }
        }

        // Throw exception if any errors occured
        if (errors.length() > 0) {
            throw new Exception(errors.toString());
        }
    }

    private JSONObject formErrorJson(String type, String errorMessage) throws Exception {
        JSONObject error = new JSONObject();
        error.put("type", type);
        error.put("error", errorMessage);
        return error;
    }
}
