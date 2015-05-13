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

package com.groupon.odo.client;

import org.json.JSONArray;
import org.json.JSONObject;

public class PathValueClient extends Client {

    /**
     * Create a new endpoint client instance
     *
     * @param profileName name of profile
     * @param useClient create a new client id(false means use the default client)
     * @throws Exception exception
     */
    public PathValueClient(String profileName, boolean useClient) throws Exception {
        super(profileName, useClient);
    }

    /**
     * Retrieves the path using the endpoint value
     *
     * @param pathValue - path (endpoint) value
     * @param requestType - "GET", "POST", etc
     * @return Path or null
     * @throws Exception exception
     */
    public JSONObject getPathFromEndpoint(String pathValue, String requestType) throws Exception {
        int type = getRequestTypeFromString(requestType);
        String url = BASE_PATH;
        JSONObject response = new JSONObject(doGet(url, null));
        JSONArray paths = response.getJSONArray("paths");
        for (int i = 0; i < paths.length(); i++) {
            JSONObject path = paths.getJSONObject(i);
            if (path.getString("path").equals(pathValue) && path.getInt("requestType") == type) {
                return path;
            }
        }
        return null;
    }

    /**
     * Sets a custom response on an endpoint using default profile and client
     *
     * @param pathValue path (endpoint) value
     * @param requestType path request type. "GET", "POST", etc
     * @param customData custom response data
     * @return true if success, false otherwise
     */
    public static boolean setDefaultCustomResponse(String pathValue, String requestType, String customData) {
        try {
            JSONObject profile = getDefaultProfile();
            String profileName = profile.getString("name");
            PathValueClient client = new PathValueClient(profileName, false);
            return client.setCustomResponse(pathValue, requestType, customData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove any overrides for an endpoint on the default profile, client
     *
     * @param pathValue path (endpoint) value
     * @param requestType path request type. "GET", "POST", etc
     * @return true if success, false otherwise
     */
    public static boolean removeDefaultCustomResponse(String pathValue, String requestType) {
        try {
            JSONObject profile = getDefaultProfile();
            String profileName = profile.getString("name");
            PathValueClient client = new PathValueClient(profileName, false);
            return client.removeCustomResponse(pathValue, requestType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove any overrides for an endpoint
     *
     * @param pathValue path (endpoint) value
     * @param requestType path request type. "GET", "POST", etc
     * @return true if success, false otherwise
     */
    public boolean removeCustomResponse(String pathValue, String requestType) {
        try {
            JSONObject path = getPathFromEndpoint(pathValue, requestType);
            if (path == null) {
                return false;
            }
            String pathId = path.getString("pathId");
            return resetResponseOverride(pathId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sets a custom response on an endpoint
     *
     * @param pathValue path (endpoint) value
     * @param requestType path request type. "GET", "POST", etc
     * @param customData custom response data
     * @return true if success, false otherwise
     */
    public boolean setCustomResponse(String pathValue, String requestType, String customData) {
        try {
            JSONObject path = getPathFromEndpoint(pathValue, requestType);
            if (path == null) {
                String pathName = pathValue;
                createPath(pathName, pathValue, requestType);
                path = getPathFromEndpoint(pathValue, requestType);
            }
            String pathId = path.getString("pathId");
            resetResponseOverride(pathId);
            setCustomResponse(pathId, customData);
            return toggleResponseOverride(pathId, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}