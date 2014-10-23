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

import com.groupon.odo.proxylib.models.EndpointOverride;
import com.groupon.odo.proxylib.models.Group;
import com.groupon.odo.proxylib.models.Method;
import com.groupon.odo.proxylib.models.Script;
import com.groupon.odo.proxylib.models.ServerGroup;
import com.groupon.odo.proxylib.models.ServerRedirect;
import com.groupon.odo.proxylib.models.backup.Backup;
import com.groupon.odo.proxylib.models.backup.Profile;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Return the structured backup data
     *
     * @return
     * @throws Exception
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
     * @param streamData
     * @return
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
                if (groupId == null)
                    groupId = PathOverrideService.getInstance().addGroup(group.getName());

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
}
