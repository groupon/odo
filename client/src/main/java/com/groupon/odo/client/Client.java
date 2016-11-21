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

import com.groupon.odo.client.models.History;
import com.groupon.odo.client.models.ServerGroup;
import com.groupon.odo.client.models.ServerRedirect;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    protected String ODO_HOST = "localhost";
    protected String BASE_URL = "http://localhost:8090/testproxy/api/";
    protected int API_PORT = 8090;

    protected static String DEFAULT_BASE_URL = "http://localhost:8090/testproxy/api/";
    public static int DEFAULT_API_PORT = 8090;

    // API Paths
    protected static String API_BASE = "testproxy/api";
    protected static String BASE_PATH = "path/";
    protected static String BASE_PROFILE = "profile/";
    protected static String BASE_METHOD = "method/";
    protected static String BASE_CLIENTS = "clients";
    protected static String HISTORY = "history/";
    protected static String BASE_SERVER = "edit/server";
    protected static String BASE_SERVERGROUP = "servergroup";
    protected static String BASE_BACKUP_PROFILE = "backup/profile";

    protected String _profileName = null;
    protected int _profileId;
    protected String _clientId = null;
    protected int _timeout = 60000;

    protected static int REQUEST_TYPE_ALL = 0;
    protected static int REQUEST_TYPE_GET = 1;
    protected static int REQUEST_TYPE_PUT = 2;
    protected static int REQUEST_TYPE_POST = 3;
    protected static int REQUEST_TYPE_DELETE = 4;
    protected History[] history;

    /**
     * Create a new client instance
     *
     * @param profileName name of existing profile to create client for
     * @param createNewClient create a new client id(false means use the default client)
     * @param hostName hostName(or IP) for the ODO host
     * @throws Exception
     */
    public Client(String profileName, boolean createNewClient, String hostName) throws Exception {
        if (hostName != null) {
            this.setHostName(hostName);
        }

        this._profileName = profileName;

        if (createNewClient) {
            this.createNewClientId();
        } else {
            this._clientId = "-1";
        }
    }

    /**
     * Create a new client instance
     *
     * @param profileName name of existing profile to create client for
     * @param createNewClient create a new client id(false means use the default client)
     * @throws Exception
     */
    public Client(String profileName, boolean createNewClient) throws Exception {
        this(profileName, createNewClient, null);
    }

    /**
     * Create a client for a clientId that already exists in Odo
     *
     * @param profileName name of existing profile to create client for
     * @param clientId clientId of existing Odo client
     * @param hostName hostName(or IP) for the ODO host
     * @throws Exception
     */
    public Client(String profileName, String clientId, String hostName) throws Exception {
        if (hostName != null) {
            this.setHostName(hostName);
        }

        this._profileName = profileName;
        this._clientId = clientId;
    }

    /**
     * Create a client for a clientId that already exists in Odo
     *
     * @param profileName name of existing profile to create client for
     * @param clientId clientId of existing Odo client
     * @throws Exception
     */
    public Client(String profileName, String clientId) throws Exception {
        this(profileName, clientId, null);
    }

    /**
     * Call when you are done with the client
     *
     * @throws Exception
     */
    public void destroy() throws Exception {
        if (_clientId == null) {
            return;
        }

        // delete the clientId here
        String uri = BASE_PROFILE + uriEncode(_profileName) + "/" + BASE_CLIENTS + "/" + _clientId;
        try {
            doDelete(uri, null);
        } catch (Exception e) {
            // some sort of error
            throw new Exception("Could not delete a proxy client");
        }
    }

    /**
     * Get the connection timeout value in ms
     *
     * @return timeout value in ms
     */
    public int getTimeout() {
        return _timeout;
    }

    /**
     * Set the connection timeout value in ms
     *
     * @param timeout value in ms
     */
    public void setTimeout(int timeout) {
        _timeout = timeout;
    }

    protected void createNewClientId() throws Exception {
        String uri = BASE_PROFILE + uriEncode(_profileName) + "/" + BASE_CLIENTS;
        try {
            JSONObject response = new JSONObject(doPost(uri, null));
            _clientId = response.getJSONObject("client").getString("uuid");
            toggleProfile(true);
        } catch (Exception e) {
            // some sort of error
            throw new Exception("Could not create a proxy client");
        }
    }

    /**
     * Set the host running the Odo instance to configure
     *
     * @param hostName name of host
     */
    public void setHostName(String hostName) {
        if (hostName == null || hostName.contains(":")) {
            return;
        }
        ODO_HOST = hostName;
        BASE_URL = "http://" + ODO_HOST + ":" + API_PORT + "/" + API_BASE + "/";
    }

    /**
     * Set the default host running the Odo instance to configure. Allows default profile methods and PathValueClient to
     * operate on remote hosts
     *
     * @param hostName name of host
     */
    public static void setDefaultHostName(String hostName) {
        if (hostName == null || hostName.contains(":")) {
            return;
        }
        DEFAULT_BASE_URL = "http://" + hostName + ":" + DEFAULT_API_PORT + "/" + API_BASE + "/";
    }

    /**
     * Retrieve the request History based on the specified filters.
     * If no filter is specified, return the default size history.
     *
     * @param filters filters to be applied
     * @return array of History items
     * @throws Exception exception
     */
    public History[] filterHistory(String... filters) throws Exception {
        BasicNameValuePair[] params;
        if (filters.length > 0) {
            params = new BasicNameValuePair[filters.length];
            for (int i = 0; i < filters.length; i++) {
                params[i] = new BasicNameValuePair("source_uri[]", filters[i]);
            }
        } else {
            return refreshHistory();
        }

        return constructHistory(params);
    }

    /**
     * Construct the history array based on the given parameters
     *
     * @param params parameters applied
     * @return array of History items
     * @throws Exception exception
     */
    protected History[] constructHistory(BasicNameValuePair[] params) throws Exception {
        String uri = HISTORY + uriEncode(_profileName);

        try {
            JSONObject response = new JSONObject(doGet(uri, params));
            JSONArray historyArray = response.getJSONArray("history");
            history = new History[historyArray.length()];

            for (int i = 0; i < historyArray.length(); i++) {
                history[i] = new History();
                JSONObject jsonHistory = historyArray.getJSONObject(i);
                if (jsonHistory == null) {
                    continue;
                }

                if (!jsonHistory.isNull("id")) {
                    history[i].setId(jsonHistory.getInt("id"));
                }
                if (!jsonHistory.isNull("profileId")) {
                    history[i].setProfileId(jsonHistory.getInt("profileId"));
                }
                if (!jsonHistory.isNull("clientUUID")) {
                    history[i].setClientUUID(jsonHistory.getString("clientUUID"));
                }
                if (!jsonHistory.isNull("createdAt")) {
                    history[i].setCreatedAt(jsonHistory.getString("createdAt"));
                }
                if (!jsonHistory.isNull("requestType")) {
                    history[i].setRequestType(jsonHistory.getString("requestType"));
                }
                if (!jsonHistory.isNull("requestURL")) {
                    history[i].setRequestURL(jsonHistory.getString("requestURL"));
                }
                if (!jsonHistory.isNull("requestParams")) {
                    history[i].setRequestParams(jsonHistory.getString("requestParams"));
                }
                if (!jsonHistory.isNull("requestPostData")) {
                    history[i].setRequestPostData(jsonHistory.getString("requestPostData"));
                }
                if (!jsonHistory.isNull("requestHeaders")) {
                    history[i].setRequestHeaders(jsonHistory.getString("requestHeaders"));
                }
                if (!jsonHistory.isNull("responseCode")) {
                    history[i].setResponseCode(jsonHistory.getString("responseCode"));
                }
                if (!jsonHistory.isNull("responseHeaders")) {
                    history[i].setResponseHeaders(jsonHistory.getString("responseHeaders"));
                }
                if (!jsonHistory.isNull("responseContentType")) {
                    history[i].setResponseContentType(jsonHistory.getString("responseContentType"));
                }
                if (!jsonHistory.isNull("originalRequestURL")) {
                    history[i].setOriginalRequestURL(jsonHistory.getString("originalRequestURL"));
                }
                if (!jsonHistory.isNull("originalRequestParams")) {
                    history[i].setOriginalRequestParams(jsonHistory.getString("originalRequestParams"));
                }
                if (!jsonHistory.isNull("originalRequestPostData")) {
                    history[i].setOriginalRequestPostData(jsonHistory.getString("originalRequestPostData"));
                }
                if (!jsonHistory.isNull("originalRequestHeaders")) {
                    history[i].setOriginalRequestHeaders(jsonHistory.getString("originalRequestHeaders"));
                }
                if (!jsonHistory.isNull("originalResponseCode")) {
                    history[i].setOriginalResponseCode(jsonHistory.getString("originalResponseCode"));
                }
                if (!jsonHistory.isNull("originalResponseHeaders")) {
                    history[i].setOriginalResponseHeaders(jsonHistory.getString("originalResponseHeaders"));
                }
                if (!jsonHistory.isNull("originalResponseContentType")) {
                    history[i].setResponseContentType(jsonHistory.getString("originalResponseContentType"));
                }
                if (!jsonHistory.isNull("modified")) {
                    history[i].setModified(jsonHistory.getBoolean("modified"));
                }
                if (!jsonHistory.isNull("requestBodyDecoded")) {
                    history[i].setRequestBodyDecoded(jsonHistory.getBoolean("requestBodyDecoded"));
                }
                if (!jsonHistory.isNull("responseBodyDecoded")) {
                    history[i].setResponseBodyDecoded(jsonHistory.getBoolean("responseBodyDecoded"));
                }
                if (!jsonHistory.isNull("extraInfo")) {
                    history[i].setExtraInfoFromString(jsonHistory.getString("extraInfo"));
                }
                /**
                 * To get the json responseData make a call specifically using the id
                 */

                int id = history[i].getId();
                String idUri = uri + "/" + id;
                JSONObject historyId = new JSONObject(doGet(idUri, null));

                String responseData = historyId.getJSONObject("history").getString("responseData");
                history[i].setResponseData(responseData);
                String originalResponseData = historyId.getJSONObject("history").getString("originalResponseData");
                history[i].setOriginalResponseData(originalResponseData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    /**
     * refresh the most recent history entries
     *
     * @return populated history entries
     * @throws Exception exception
     */
    public History[] refreshHistory() throws Exception {
        return refreshHistory(15, 0);
    }

    /**
     * refresh the most recent history entries
     *
     * @param limit number of entries to populate
     * @param offset number of most recent entries to skip
     * @return populated history entries
     * @throws Exception exception
     */
    public History[] refreshHistory(int limit, int offset) throws Exception {
        BasicNameValuePair[] params = {
            new BasicNameValuePair("limit", String.valueOf(limit)),
            new BasicNameValuePair("offset", String.valueOf(offset))
        };
        return constructHistory(params);
    }

    /**
     * Delete the proxy history for the active profile
     *
     * @throws Exception exception
     */
    public void clearHistory() throws Exception {
        String uri;
        try {
            uri = HISTORY + uriEncode(_profileName);
            doDelete(uri, null);
        } catch (Exception e) {
            throw new Exception("Could not delete proxy history");
        }
    }

    public void setClientUUID(String clientId) {
        _clientId = clientId;
    }

    public String getClientUUID() {
        return _clientId;
    }

    /**
     * Turn this profile on or off
     *
     * @param enabled true or false
     * @return true on success, false otherwise
     */
    public boolean toggleProfile(Boolean enabled) {
        // TODO: make this return values properly
        BasicNameValuePair[] params = {
            new BasicNameValuePair("active", enabled.toString())
        };
        try {
            String uri = BASE_PROFILE + uriEncode(this._profileName) + "/" + BASE_CLIENTS + "/";
            if (_clientId == null) {
                uri += "-1";
            } else {
                uri += _clientId;
            }
            JSONObject response = new JSONObject(doPost(uri, params));
        } catch (Exception e) {
            // some sort of error
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Reset all overrrides
     *
     * @return true if successful, otherwise false
     */
    public boolean resetProfile() {
        Boolean enabled = new Boolean(true);
        BasicNameValuePair[] params = {
            new BasicNameValuePair("reset", enabled.toString())
        };

        try {
            String uri = BASE_PROFILE + uriEncode(this._profileName) + "/" + BASE_CLIENTS + "/";
            if (_clientId == null) {
                uri += "-1";
            } else {
                uri += _clientId;
            }
            JSONObject response = new JSONObject(doPost(uri, params));
        } catch (Exception e) {
            // some sort of error
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Enable/disable request overrides for a path
     *
     * @param pathName name of path
     * @param enabled true or false
     * @return true if success, false otherwise
     */
    public boolean toggleRequestOverride(String pathName, Boolean enabled) {
        return toggleOverride(pathName, "requestEnabled", enabled);
    }

    /**
     * Enable/disable response overrides for a path
     *
     * @param pathName name of path
     * @param enabled true or false
     * @return true if success, false otherwise
     */
    public boolean toggleResponseOverride(String pathName, Boolean enabled) {
        return toggleOverride(pathName, "responseEnabled", enabled);
    }

    protected boolean toggleOverride(String pathName, String type, Boolean enabled) {
        BasicNameValuePair[] params = {
            new BasicNameValuePair(type, enabled.toString()),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_PATH + uriEncode(pathName), params));
            if (response.getBoolean(type) == enabled) {
                return true;
            }
        } catch (Exception e) {
            // some sort of error
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reset all request override information for a path
     *
     * @param pathName name of path
     * @return true if success, false otherwise
     */
    public boolean resetRequestOverride(String pathName) {
        return togglePathReset(pathName, "resetRequest");
    }

    /**
     * Reset all response override information for a path
     *
     * @param pathName name of path
     * @return true if success, false otherwise
     */
    public boolean resetResponseOverride(String pathName) {
        return togglePathReset(pathName, "resetResponse");
    }

    protected boolean togglePathReset(String pathName, String type) {
        BasicNameValuePair[] params = {
            new BasicNameValuePair(type, "true"),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_PATH + uriEncode(pathName), params));
            return true;
        } catch (Exception e) {
            // some sort of error
            e.printStackTrace();
        }
        return false;
    }

    protected boolean setCustom(Boolean isResponse, String pathName, String custom) {
        // first remove the custom entry for this path if it is the custom request
        if (!isResponse) {
            this.removeCustomRequest(pathName);
        }

        // now add it(-1 is the custom response identifier)
        if (isResponse) {
            this.addMethodToResponseOverride(pathName, "-1");
        } else {
            this.addMethodToResponseOverride(pathName, "-2");
        }

        // now set the string
        String command = "customResponse";
        if (!isResponse) {
            command = "customRequest";
        }

        try {
            BasicNameValuePair[] params = {
                new BasicNameValuePair(command, custom),
                new BasicNameValuePair("profileIdentifier", this._profileName)
            };

            JSONObject response = new JSONObject(doPost(BASE_PATH + uriEncode(pathName), params));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Set a custom response for this path
     *
     * @param pathName name of path
     * @param customResponse value of custom response
     * @return true if success, false otherwise
     * @throws Exception exception
     */
    public boolean setCustomResponse(String pathName, String customResponse) throws Exception {
        // figure out the new ordinal
        int nextOrdinal = this.getNextOrdinalForMethodId(-1, pathName);

        // add override
        this.addMethodToResponseOverride(pathName, "-1");

        // set argument
        return this.setMethodArguments(pathName, "-1", nextOrdinal, customResponse);
    }

    /**
     * Set a custom request for this path
     *
     * @param pathName name of path
     * @param customRequest value of custom request
     * @return true if success, false otherwise
     */
    public boolean setCustomRequest(String pathName, String customRequest) {
        return this.setCustom(true, pathName, customRequest);
    }

    /**
     * Remove a custom response for a path
     *
     * @param pathName name of path
     * @return true if success, false otherwise
     */
    public boolean removeCustomResponse(String pathName) {
        return this.removeMethodFromResponseOverride(pathName, "-1");
    }

    /**
     * Remove a custom request for a path
     *
     * @param pathName name of path
     * @return true if success, false otherwise
     */
    public boolean removeCustomRequest(String pathName) {
        return this.removeMethodFromResponseOverride(pathName, "-2");
    }

    /**
     * Add a method to the enabled response overrides for a path
     *
     * @param pathName name of path
     * @param methodName name of method
     * @return true if success, false otherwise
     */
    public boolean addMethodToResponseOverride(String pathName, String methodName) {
        // need to find out the ID for the method
        // TODO: change api for adding methods to take the name instead of ID
        try {
            Integer overrideId = getOverrideIdForMethodName(methodName);

            // now post to path api to add this is a selected override
            BasicNameValuePair[] params = {
                new BasicNameValuePair("addOverride", overrideId.toString()),
                new BasicNameValuePair("profileIdentifier", this._profileName)
            };
            JSONObject response = new JSONObject(doPost(BASE_PATH + uriEncode(pathName), params));
            // check enabled endpoints array to see if this overrideID exists
            JSONArray enabled = response.getJSONArray("enabledEndpoints");
            for (int x = 0; x < enabled.length(); x++) {
                if (enabled.getJSONObject(x).getInt("overrideId") == overrideId) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Set the repeat count of an override at ordinal index
     *
     * @param pathName Path name
     * @param methodName Fully qualified method name
     * @param ordinal 1-based index of the override within the overrides of type methodName
     * @param repeatCount new repeat count to set
     * @return true if success, false otherwise
     */
    public boolean setOverrideRepeatCount(String pathName, String methodName, Integer ordinal, Integer repeatCount) {
        try {
            String methodId = getOverrideIdForMethodName(methodName).toString();
            BasicNameValuePair[] params = {
                new BasicNameValuePair("profileIdentifier", this._profileName),
                new BasicNameValuePair("ordinal", ordinal.toString()),
                new BasicNameValuePair("repeatNumber", repeatCount.toString())
            };

            JSONObject response = new JSONObject(doPost(BASE_PATH + uriEncode(pathName) + "/" + methodId, params));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Set the response code of an override at ordinal index
     *
     * @param pathName Path name
     * @param methodName Fully qualified method name
     * @param ordinal 1-based index of the override within the overrides of type methodName
     * @param responseCode new response code to set
     * @return true if success, false otherwise
     */
    public boolean setOverrideResponseCode(String pathName, String methodName, Integer ordinal, String responseCode) {
        try {
            String methodId = getOverrideIdForMethodName(methodName).toString();
            BasicNameValuePair[] params = {
                    new BasicNameValuePair("profileIdentifier", this._profileName),
                    new BasicNameValuePair("ordinal", ordinal.toString()),
                    new BasicNameValuePair("responseCode", responseCode)
            };

            JSONObject response = new JSONObject(doPost(BASE_PATH + uriEncode(pathName) + "/" + methodId, params));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Set the method arguments for an enabled method override
     *
     * @param pathName Path name
     * @param methodName Fully qualified method name
     * @param ordinal 1-based index of the override within the overrides of type methodName
     * @param arguments Array of arguments to set(specify all arguments)
     * @return true if success, false otherwise
     */
    public boolean setMethodArguments(String pathName, String methodName, Integer ordinal, Object... arguments) {
        try {
            BasicNameValuePair[] params = new BasicNameValuePair[arguments.length + 2];
            int x = 0;
            for (Object argument : arguments) {
                params[x] = new BasicNameValuePair("arguments[]", argument.toString());
                x++;
            }
            params[x] = new BasicNameValuePair("profileIdentifier", this._profileName);
            params[x + 1] = new BasicNameValuePair("ordinal", ordinal.toString());

            JSONObject response = new JSONObject(doPost(BASE_PATH + uriEncode(pathName) + "/" + methodName, params));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Add a method to the enabled response overrides for a path
     *
     * @param pathName name of path
     * @param methodName name of method
     * @return true if success, false otherwise
     */
    public boolean removeMethodFromResponseOverride(String pathName, String methodName) {
        // need to find out the ID for the method
        try {
            Integer overrideId = getOverrideIdForMethodName(methodName);

            // now post to path api to add this is a selected override
            BasicNameValuePair[] params = {
                new BasicNameValuePair("removeOverride", overrideId.toString()),
                new BasicNameValuePair("profileIdentifier", this._profileName)
            };

            JSONObject response = new JSONObject(doPost(BASE_PATH + uriEncode(pathName), params));
            // check enabled endpoints array to see if this overrideID exists
            JSONArray enabled = response.getJSONArray("enabledEndpoints");
            for (int x = 0; x < enabled.length(); x++) {
                if (enabled.getJSONObject(x).getInt("overrideId") == overrideId) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Create a new path
     *
     * @param pathName friendly name of path
     * @param pathValue path value or regex
     * @param requestType path request type. "GET", "POST", etc
     */
    public void createPath(String pathName, String pathValue, String requestType) {
        try {
            int type = getRequestTypeFromString(requestType);
            String url = BASE_PATH;
            BasicNameValuePair[] params = {
                new BasicNameValuePair("pathName", pathName),
                new BasicNameValuePair("path", pathValue),
                new BasicNameValuePair("requestType", String.valueOf(type)),
                new BasicNameValuePair("profileIdentifier", this._profileName)
            };

            JSONObject response = new JSONObject(doPost(BASE_PATH, params));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set custom response or request for a profile's default client, ensures profile and path are enabled
     *
     * @param profileName profileName to modift, default client is used
     * @param pathName friendly name of path
     * @param isResponse true if response, false for request
     * @param customData custom response/request data
     * @return true if success, false otherwise
     */
    protected static boolean setCustomForDefaultClient(String profileName, String pathName, Boolean isResponse, String customData) {
        try {
            Client client = new Client(profileName, false);
            client.toggleProfile(true);
            client.setCustom(isResponse, pathName, customData);
            if (isResponse) {
                client.toggleResponseOverride(pathName, true);
            } else {
                client.toggleRequestOverride(pathName, true);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * set custom request for profile's default client
     *
     * @param profileName profileName to modify
     * @param pathName friendly name of path
     * @param customData custom request data
     * @return true if success, false otherwise
     */
    public static boolean setCustomRequestForDefaultClient(String profileName, String pathName, String customData) {
        try {
            return setCustomForDefaultClient(profileName, pathName, false, customData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * set custom response for profile's default client
     *
     * @param profileName profileName to modify
     * @param pathName friendly name of path
     * @param customData custom request data
     * @return true if success, false otherwise
     */
    public static boolean setCustomResponseForDefaultClient(String profileName, String pathName, String customData) {
        try {
            return setCustomForDefaultClient(profileName, pathName, true, customData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * set custom request/response for the default profile's default client
     *
     * @param pathName friendly name of path
     * @param isResponse true for response, false for request
     * @param customData custom response/request data
     * @return true if success, false otherwise
     */
    protected static boolean setCustomForDefaultProfile(String pathName, Boolean isResponse, String customData) {
        try {
            JSONObject profile = getDefaultProfile();
            String profileName = profile.getString("name");
            Client client = new Client(profileName, false);
            return client.setCustom(isResponse, pathName, customData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * set custom request for the default profile's default client
     *
     * @param pathName friendly name of path
     * @param customData custom response/request data
     * @return true if success, false otherwise
     */
    public static boolean setCustomRequestForDefaultProfile(String pathName, String customData) {
        try {
            return setCustomForDefaultProfile(pathName, false, customData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * set custom response for the default profile's default client
     *
     * @param pathName friendly name of path
     * @param customData custom response/request data
     * @return true if success, false otherwise
     */
    public static boolean setCustomResponseForDefaultProfile(String pathName, String customData) {
        try {
            return setCustomForDefaultProfile(pathName, true, customData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get the default profile
     *
     * @return representation of default profile
     * @throws Exception exception
     */
    protected static JSONObject getDefaultProfile() throws Exception {
        String uri = DEFAULT_BASE_URL + BASE_PROFILE;
        try {
            JSONObject response = new JSONObject(doGet(uri, 60000));
            JSONArray profiles = response.getJSONArray("profiles");

            if (profiles.length() > 0) {
                return profiles.getJSONObject(0);
            }
        } catch (Exception e) {
            // some sort of error
            throw new Exception("Could not create a proxy client");
        }

        return null;
    }

    protected Integer getOverrideIdForMethodName(String methodName) throws Exception {
        String methodInfo = doGet(BASE_METHOD + methodName, new BasicNameValuePair[0]);
        JSONObject methodJson = new JSONObject(methodInfo);
        return methodJson.getJSONObject("method").getInt("id");
    }

    /**
     * Get the next available ordinal for a method ID
     *
     * @param methodId ID of method
     * @return value of next ordinal
     * @throws Exception exception
     */
    private Integer getNextOrdinalForMethodId(int methodId, String pathName) throws Exception {
        String pathInfo = doGet(BASE_PATH + uriEncode(pathName), new BasicNameValuePair[0]);
        JSONObject pathResponse = new JSONObject(pathInfo);

        JSONArray enabledEndpoints = pathResponse.getJSONArray("enabledEndpoints");
        int lastOrdinal = 0;
        for (int x = 0; x < enabledEndpoints.length(); x++) {
            if (enabledEndpoints.getJSONObject(x).getInt("overrideId") == methodId) {
                lastOrdinal++;
            }
        }
        return lastOrdinal + 1;
    }

    // helper functions
    protected String uriEncode(String input) throws Exception {
        return URLEncoder.encode(input, "UTF-8").replace("+", "%20");
    }

    /**
     * Convert a request type string to value
     *
     * @param requestType String value of request type GET/POST/PUT/DELETE
     * @return Matching REQUEST_TYPE. Defaults to ALL
     */
    protected int getRequestTypeFromString(String requestType) {
        if ("GET".equals(requestType)) {
            return REQUEST_TYPE_GET;
        }
        if ("POST".equals(requestType)) {
            return REQUEST_TYPE_POST;
        }
        if ("PUT".equals(requestType)) {
            return REQUEST_TYPE_PUT;
        }
        if ("DELETE".equals(requestType)) {
            return REQUEST_TYPE_DELETE;
        }
        return REQUEST_TYPE_ALL;
    }

    protected ServerRedirect getServerRedirectFromJSON(JSONObject jsonServer) {
        ServerRedirect redirect = new ServerRedirect();
        if (jsonServer == null) {
            return null;
        }

        try {
            if (!jsonServer.isNull("id")) {
                redirect.setId(jsonServer.getInt("id"));
            }
            if (!jsonServer.isNull("srcUrl")) {
                redirect.setSourceHost(jsonServer.getString("srcUrl"));
            }
            if (!jsonServer.isNull("destUrl")) {
                redirect.setDestinationHost(jsonServer.getString("destUrl"));
            }
            if (!jsonServer.isNull("hostHeader")) {
                redirect.setHostHeader(jsonServer.getString("hostHeader"));
            }
            if (!jsonServer.isNull("profileId")) {
                redirect.setProfileId(jsonServer.getInt("profileId"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return redirect;
    }

    /**
     * Add a new server mapping to current profile
     *
     * @param sourceHost source hostname
     * @param destinationHost destination hostname
     * @param hostHeader host header
     * @return ServerRedirect
     */
    public ServerRedirect addServerMapping(String sourceHost, String destinationHost, String hostHeader) {
        JSONObject response = null;

        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("srcUrl", sourceHost));
        params.add(new BasicNameValuePair("destUrl", destinationHost));
        params.add(new BasicNameValuePair("profileIdentifier", this._profileName));
        if (hostHeader != null) {
            params.add(new BasicNameValuePair("hostHeader", hostHeader));
        }

        try {
            BasicNameValuePair paramArray[] = new BasicNameValuePair[params.size()];
            params.toArray(paramArray);
            response = new JSONObject(doPost(BASE_SERVER, paramArray));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return getServerRedirectFromJSON(response);
    }

    /**
     * Remove a server mapping from current profile by ID
     *
     * @param serverMappingId server mapping ID
     * @return Collection of updated ServerRedirects
     */
    public List<ServerRedirect> deleteServerMapping(int serverMappingId) {
        ArrayList<ServerRedirect> servers = new ArrayList<ServerRedirect>();
        try {
            JSONArray serverArray = new JSONArray(doDelete(BASE_SERVER + "/" + serverMappingId, null));
            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject jsonServer = serverArray.getJSONObject(i);
                ServerRedirect server = getServerRedirectFromJSON(jsonServer);
                if (server != null) {
                    servers.add(server);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return servers;
    }

    /**
     * Get a list of all active server mappings defined for current profile
     *
     * @return Collection of ServerRedirects
     */
    public List<ServerRedirect> getServerMappings() {
        ArrayList<ServerRedirect> servers = new ArrayList<ServerRedirect>();
        try {
            JSONObject response = new JSONObject(doGet(BASE_SERVER, null));
            JSONArray serverArray = response.getJSONArray("servers");

            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject jsonServer = serverArray.getJSONObject(i);
                ServerRedirect server = getServerRedirectFromJSON(jsonServer);
                if (server != null) {
                    servers.add(server);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return servers;
    }

    /**
     * Enable/disable a server mapping
     *
     * @param serverMappingId ID of server mapping
     * @param enabled true to enable, false to disable
     * @return updated info for the ServerRedirect
     */
    public ServerRedirect enableServerMapping(int serverMappingId, Boolean enabled) {
        ServerRedirect redirect = new ServerRedirect();
        BasicNameValuePair[] params = {
            new BasicNameValuePair("enabled", enabled.toString()),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_SERVER + "/" + serverMappingId, params));
            redirect = getServerRedirectFromJSON(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return redirect;
    }

    /**
     * Update server mapping's source host
     *
     * @param serverMappingId ID of server mapping
     * @param sourceHost hostname of source host
     * @return updated ServerRedirect
     */
    public ServerRedirect updateServerRedirectSrc(int serverMappingId, String sourceHost) {
        ServerRedirect redirect = new ServerRedirect();
        BasicNameValuePair[] params = {
            new BasicNameValuePair("srcUrl", sourceHost),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_SERVER + "/" + serverMappingId + "/src", params));
            redirect = getServerRedirectFromJSON(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return redirect;
    }

    /**
     * Update server mapping's destination host
     *
     * @param serverMappingId ID of server mapping
     * @param destinationHost hostname of destination host
     * @return updated ServerRedirect
     */
    public ServerRedirect updateServerRedirectDest(int serverMappingId, String destinationHost) {
        ServerRedirect redirect = new ServerRedirect();
        BasicNameValuePair[] params = {
            new BasicNameValuePair("destUrl", destinationHost),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_SERVER + "/" + serverMappingId + "/dest", params));
            redirect = getServerRedirectFromJSON(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return redirect;
    }

    /**
     * Update server mapping's host header
     *
     * @param serverMappingId ID of server mapping
     * @param hostHeader value of host header
     * @return updated ServerRedirect
     */
    public ServerRedirect updateServerRedirectHost(int serverMappingId, String hostHeader) {
        ServerRedirect redirect = new ServerRedirect();
        BasicNameValuePair[] params = {
            new BasicNameValuePair("hostHeader", hostHeader),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_SERVER + "/" + serverMappingId + "/host", params));
            redirect = getServerRedirectFromJSON(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return redirect;
    }

    protected ServerGroup getServerGroupFromJSON(JSONObject jsonServerGroup) {
        ServerGroup group = new ServerGroup();
        try {
            if (jsonServerGroup == null) {
                return null;
            }

            if (!jsonServerGroup.isNull("id")) {
                group.setId(jsonServerGroup.getInt("id"));
            }
            if (!jsonServerGroup.isNull("name")) {
                group.setName(jsonServerGroup.getString("name"));
            }
            if (!jsonServerGroup.isNull("profileId")) {
                group.setProfileId(jsonServerGroup.getInt("profileId"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return group;
    }

    /**
     * Create a new server group
     *
     * @param groupName name of server group
     * @return Created ServerGroup
     */
    public ServerGroup addServerGroup(String groupName) {
        ServerGroup group = new ServerGroup();

        BasicNameValuePair[] params = {
            new BasicNameValuePair("name", groupName),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_SERVERGROUP, params));
            group = getServerGroupFromJSON(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return group;
    }

    /**
     * Delete a server group
     *
     * @param serverGroupId ID of serverGroup
     * @return Collection of active Server Groups
     */
    public List<ServerGroup> deleteServerGroup(int serverGroupId) {
        ArrayList<ServerGroup> groups = new ArrayList<ServerGroup>();
        try {
            JSONArray serverArray = new JSONArray(doDelete(BASE_SERVERGROUP + "/" + serverGroupId, null));
            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject jsonServerGroup = serverArray.getJSONObject(i);
                ServerGroup group = getServerGroupFromJSON(jsonServerGroup);
                groups.add(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return groups;
    }

    /**
     * Get the collection of the server groups
     *
     * @return Collection of active server groups
     */
    public List<ServerGroup> getServerGroups() {
        ArrayList<ServerGroup> groups = new ArrayList<ServerGroup>();
        try {
            JSONObject response = new JSONObject(doGet(BASE_SERVERGROUP, null));
            JSONArray serverArray = response.getJSONArray("servergroups");

            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject jsonServerGroup = serverArray.getJSONObject(i);
                ServerGroup group = getServerGroupFromJSON(jsonServerGroup);
                groups.add(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return groups;
    }

    /**
     * Update the server group's name
     *
     * @param serverGroupId ID of server group
     * @param name new name of server group
     * @return updated ServerGroup
     */
    public ServerGroup updateServerGroupName(int serverGroupId, String name) {
        ServerGroup serverGroup = null;
        BasicNameValuePair[] params = {
            new BasicNameValuePair("name", name),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_SERVERGROUP + "/" + serverGroupId, params));
            serverGroup = getServerGroupFromJSON(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return serverGroup;
    }

    /**
     * Activate a server group
     *
     * @param serverGroupId ID of server group
     * @return Updated ServerGroup
     */
    public ServerGroup activateServerGroup(int serverGroupId) {
        ServerGroup serverGroup = null;
        BasicNameValuePair[] params = {
            new BasicNameValuePair("activate", String.valueOf(true)),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };
        try {
            JSONObject response = new JSONObject(doPost(BASE_SERVERGROUP + "/" + serverGroupId, params));
            serverGroup = getServerGroupFromJSON(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return serverGroup;
    }

    /**
     * Activate a server group
     *
     * @param groupName name of serverGroup
     * @return update ServerGroup
     */
    public ServerGroup activateServerGroup(String groupName) {
        ServerGroup serverGroup = null;
        BasicNameValuePair[] params = {
            new BasicNameValuePair("activate", String.valueOf(true)),
            new BasicNameValuePair("profileIdentifier", this._profileName)
        };

        int serverGroupId = getServerGroupId(groupName);
        if (serverGroupId == -1) {
            return null;
        }

        try {
            JSONObject response = new JSONObject(doPost(BASE_SERVERGROUP + "/" + serverGroupId, params));
            serverGroup = getServerGroupFromJSON(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return serverGroup;
    }

    /**
     * Upload file and set odo overrides and configuration of odo
     *
     * @param fileName File containing configuration
     * @param odoImport Import odo configuration in addition to overrides
     * @return If upload was successful
     */
    public boolean uploadConfigurationAndProfile(String fileName, String odoImport) {
        File file = new File(fileName);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        FileBody fileBody = new FileBody(file, ContentType.MULTIPART_FORM_DATA);
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntityBuilder.addPart("fileData", fileBody);
        multipartEntityBuilder.addTextBody("odoImport", odoImport);
        try {
            JSONObject response = new JSONObject(doMultipartPost(BASE_BACKUP_PROFILE + "/" + uriEncode(this._profileName) + "/" + this._clientId, multipartEntityBuilder));
            if (response.length() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Export the odo overrides setup and odo configuration
     *
     * @param oldExport Whether this is a backup from scratch or backing up because user will upload after (matches API)
     * @return The odo configuration and overrides in JSON format, can be written to a file after
     */
    public JSONObject exportConfigurationAndProfile(String oldExport) {
        try {
            BasicNameValuePair[] params = {
                new BasicNameValuePair("oldExport", oldExport)
            };
            String url = BASE_BACKUP_PROFILE + "/" + uriEncode(this._profileName) + "/" + this._clientId;
            return new JSONObject(doGet(url, new BasicNameValuePair[]{}));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    protected int getServerGroupId(String groupName) {
        List<ServerGroup> groups = getServerGroups();
        for (ServerGroup group : groups) {
            if (groupName.compareTo(group.getName()) == 0) {
                return group.getId();
            }
        }
        return -1;
    }

    protected static String doGet(String fullUrl, int timeout) throws Exception {
        HttpGet get = new HttpGet(fullUrl);

        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), timeout);
        HttpConnectionParams.setSoTimeout(client.getParams(), timeout);

        HttpResponse response = client.execute(get);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String accumulator = "";
        String line = "";
        while ((line = rd.readLine()) != null) {
            accumulator += line;
            accumulator += "\n";
        }
        return accumulator;
    }

    protected String doGet(String apiUrl, BasicNameValuePair[] data) throws Exception {
        String fullUrl = BASE_URL + apiUrl;

        if (data != null) {
            if (data.length > 0) {
                fullUrl += "?";
            }

            for (BasicNameValuePair bnvp : data) {
                fullUrl += bnvp.getName() + "=" + uriEncode(bnvp.getValue()) + "&";
            }
        }

        // add clientUUID if necessary
        if (_clientId != null) {
            if (data == null || data.length == 0) {
                fullUrl += "?";
            }
            fullUrl += "clientUUID=" + _clientId;
        }

        fullUrl += "&profileIdentifier=" + uriEncode(this._profileName);

        HttpGet get = new HttpGet(fullUrl);

        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), _timeout);
        HttpConnectionParams.setSoTimeout(client.getParams(), _timeout);

        HttpResponse response = client.execute(get);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String accumulator = "";
        String line = "";
        while ((line = rd.readLine()) != null) {
            accumulator += line;
            accumulator += "\n";
        }
        return accumulator;
    }

    protected String doDelete(String apiUrl, BasicNameValuePair[] data) throws Exception {
        String fullUrl = BASE_URL + apiUrl;

        if (data != null) {
            if (data.length > 0) {
                fullUrl += "?";
            }

            for (BasicNameValuePair bnvp : data) {
                fullUrl += bnvp.getName() + "=" + uriEncode(bnvp.getValue()) + "&";
            }
        }

        // add clientUUID if necessary
        if (_clientId != null) {
            if (data == null || data.length == 0) {
                fullUrl += "?";
            }
            fullUrl += "clientUUID=" + _clientId;
        }

        fullUrl += "&profileIdentifier=" + uriEncode(this._profileName);

        HttpDelete get = new HttpDelete(fullUrl);

        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), _timeout);
        HttpConnectionParams.setSoTimeout(client.getParams(), _timeout);

        HttpResponse response = client.execute(get);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String accumulator = "";
        String line = "";
        while ((line = rd.readLine()) != null) {
            accumulator += line;
            accumulator += "\n";
        }
        return accumulator;
    }

    protected String doPost(String apiUrl, BasicNameValuePair[] data) throws Exception {
        String fullUrl = BASE_URL + apiUrl;
        HttpPost post = new HttpPost(fullUrl);

        post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        ArrayList<BasicNameValuePair> dataList = new ArrayList<>();
        if (data != null) {
            dataList.addAll(Arrays.asList(data));
        }

        // add clientUUID if necessary
        if (_clientId != null) {
            BasicNameValuePair clientPair = new BasicNameValuePair("clientUUID", _clientId);
            dataList.add(clientPair);
        }

        if (dataList.size() > 0) {
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(dataList, StandardCharsets.UTF_8.name());
            post.setEntity(urlEncodedFormEntity);
        }

        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), _timeout);
        HttpConnectionParams.setSoTimeout(client.getParams(), _timeout);

        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String accumulator = "";
        String line = "";
        while ((line = rd.readLine()) != null) {
            accumulator += line;
            accumulator += "\n";
        }
        return accumulator;
    }

    protected String doMultipartPost(String apiUrl, MultipartEntityBuilder multipartEntityBuilder) throws Exception {
        String boundary = "23ljkw4ljefw093ljk";
        String fullUrl = BASE_URL + apiUrl;
        HttpPost post = new HttpPost(fullUrl);

        post.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        multipartEntityBuilder.setBoundary(boundary);
        post.setEntity(multipartEntityBuilder.build());

        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), _timeout);
        HttpConnectionParams.setSoTimeout(client.getParams(), _timeout);

        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String accumulator = "";
        String line = "";
        while ((line = rd.readLine()) != null) {
            accumulator += line;
            accumulator += "\n";
        }
        return accumulator;
    }
}
