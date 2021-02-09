///*
// Copyright 2014 Groupon, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//*/
//package com.groupon.odo.proxylib;
//
//import com.groupon.odo.proxylib.models.EnabledEndpoint;
//import flexjson.JSONSerializer;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import org.json.JSONArray;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class OverrideService2 {
//    static final Logger logger = LoggerFactory
//        .getLogger(OverrideService2.class);
//    private static OverrideService2 serviceInstance = null;
//    static SQLService sqlService = null;
//
//    public OverrideService2() {
//    }
//
//    public static OverrideService2 getInstance() throws Exception {
//        if (serviceInstance == null) {
//            sqlService = SQLService.getInstance();
//            serviceInstance = new OverrideService2();
//        }
//        return serviceInstance;
//    }
//
//    /**
//     * Enable specific override ID for a path
//     *
//     * @param overrideId ID of override to enable
//     * @param pathId ID of path containing override
//     * @param clientUUID UUID of client
//     * @throws Exception exception
//     */
//    public void enableOverride(int overrideId, int pathId, String clientUUID) throws Exception {
//        // get profileId from pathId
//        int profileId = PathOverrideService.getInstance().getPath(pathId).getProfileId();
//        int newPriority = 0;
//
//        // we want to limit -1, -2 to only be added once since they are the Custom responses/requests
//        if (overrideId == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM) {
//            if (this.getEnabledEndpoint(pathId, overrideId, null, clientUUID) != null) {
//                return;
//            }
//        }
//
//        // need to first determine the highest enabled order value for this path
//        HashMap<String, Object> priorities = sqlService.getFirstResult(
//            "SELECT * FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                " WHERE " + Constants.REQUEST_RESPONSE_PATH_ID + "=" + pathId +
//                " AND " + Constants.GENERIC_CLIENT_UUID + "='" + clientUUID +
//                "' ORDER BY + " + Constants.ENABLED_OVERRIDES_PRIORITY + " DESC"
//        );
//        if (priorities != null) {
//            newPriority = Integer.valueOf(priorities.get(Constants.ENABLED_OVERRIDES_PRIORITY.toUpperCase()).toString()) + 1;
//        }
//
//        PreparedStatement statement = null;
//        try (Connection sqlConnection = sqlService.getConnection()) {
//
//            PreparedStatement query = null;
//            ResultSet results = null;
//            SQLService sqlService = SQLService.getInstance();
//            com.groupon.odo.proxylib.models.Method method = null;
//            query = sqlConnection.prepareStatement(
//                "SELECT * FROM " + Constants.DB_TABLE_OVERRIDE +
//                    " WHERE " + Constants.GENERIC_ID + " = ?"
//            );
//            query.setString(1, String.valueOf(overrideId));
//            results = query.executeQuery();
//            JSONSerializer serializer = new JSONSerializer();
//            if (results.next()) {
//                String className = results.getString(Constants.OVERRIDE_CLASS_NAME);
//                String methodName = results.getString(Constants.OVERRIDE_METHOD_NAME);
//                method = PluginManager.getInstance().getMethod(className, methodName);
//            }
//
//            statement = sqlConnection.prepareStatement(
//                "INSERT INTO " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                    "(" + Constants.GENERIC_PROFILE_ID + "," + Constants.GENERIC_CLIENT_UUID + "," +
//                    Constants.REQUEST_RESPONSE_PATH_ID + "," + Constants.ENABLED_OVERRIDES_OVERRIDE_ID + "," +
//                    Constants.ENABLED_OVERRIDES_PRIORITY + "," + Constants.ENABLED_OVERRIDES_ARGUMENTS +  "," +
//                    Constants.ENABLED_OVERRIDES_RESPONSE_CODE + ")" +
//                    " VALUES (?, ?, ?, ?, ?, ?, ?);"
//            );
//            statement.setInt(1, profileId);
//            statement.setString(2, clientUUID);
//            statement.setInt(3, pathId);
//            statement.setInt(4, overrideId);
//            statement.setInt(5, newPriority);
//            if (method == null) {
//                statement.setString(6, "");
//            } else {
//                ArrayList<String> argDefaults = new ArrayList<String>();
//                for (int i = 0; i < method.getMethodArguments().length; i++) {
//                    if (i < method.getMethodDefaultArguments().length && method.getMethodDefaultArguments()[i] != null) {
//                        argDefaults.add(String.valueOf(method.getMethodDefaultArguments()[i]));
//                    } else {
//                        argDefaults.add("");
//                    }
//                }
//                statement.setString(6, serializer.serialize(argDefaults));
//            }
//            statement.setString(7,"200");
//            statement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Update the arguments for a given enabled override
//     *
//     * @param overrideId - override ID to update
//     * @param pathId - path ID to update
//     * @param ordinal - can be null, Index of the enabled override to edit if multiple of the same are enabled
//     * @param arguments - Object array of arguments
//     * @param clientUUID - clientUUID
//     */
//    public void updateArguments(int overrideId, int pathId, Integer ordinal, String arguments, String clientUUID) {
//        if (ordinal == null) {
//            ordinal = 1;
//        }
//
//        PreparedStatement statement = null;
//
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            // get ID of the ordinal
//            int enabledId = getEnabledEndpoint(pathId, overrideId, ordinal, clientUUID).getId();
//
//            String queryString = "UPDATE " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                " SET " + Constants.ENABLED_OVERRIDES_ARGUMENTS + " = ? " +
//                " WHERE " + Constants.GENERIC_ID + " = ?";
//
//            statement = sqlConnection.prepareStatement(queryString);
//            statement.setString(1, arguments);
//            statement.setInt(2, enabledId);
//            statement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Update the repeat number for a given enabled override
//     *
//     * @param overrideId - override ID to update
//     * @param pathId - path ID to update
//     * @param ordinal - can be null, Index of the enabled override to edit if multiple of the same are enabled
//     * @param repeatNumber - number of times to repeat
//     * @param clientUUID - clientUUID
//     */
//    public void updateRepeatNumber(int overrideId, int pathId, Integer ordinal, Integer repeatNumber, String clientUUID) {
//        if (ordinal == null) {
//            ordinal = 1;
//        }
//
//        try {
//            // get ID of the ordinal
//            int enabledId = getEnabledEndpoint(pathId, overrideId, ordinal, clientUUID).getId();
//            updateRepeatNumber(enabledId, repeatNumber);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Update the repeat number for a given enabled override
//     *
//     * @param id enabled override ID to update
//     * @param repeatNumber updated value of repeat
//     */
//    public void updateRepeatNumber(int id, Integer repeatNumber) {
//        PreparedStatement statement = null;
//        try (Connection sqlConnection = sqlService.getConnection()) {
//
//            String queryString = "UPDATE " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                " SET " + Constants.ENABLED_OVERRIDES_REPEAT_NUMBER + "= ? " +
//                " WHERE " + Constants.GENERIC_ID + " = ?";
//            statement = sqlConnection.prepareStatement(queryString);
//            statement.setInt(1, repeatNumber);
//            statement.setInt(2, id);
//            statement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Update the response code for a given enabled override
//     *
//     * @param overrideId - override ID to update
//     * @param pathId - path ID to update
//     * @param ordinal - can be null, Index of the enabled override to edit if multiple of the same are enabled
//     * @param responseCode - response code for the given response
//     * @param clientUUID - clientUUID
//     */
//    public void updateResponseCode(int overrideId, int pathId, Integer ordinal, String responseCode, String clientUUID) {
//        if (ordinal == null) {
//            ordinal = 1;
//        }
//
//        try {
//            // get ID of the ordinal
//            int enabledId = getEnabledEndpoint(pathId, overrideId, ordinal, clientUUID).getId();
//            updateResponseCode(enabledId, responseCode);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Update the response code for a given enabled override
//     *
//     * @param id enabled override ID to update
//     * @param responseCode updated value of responseCode
//     */
//    public void updateResponseCode(int id, String responseCode) {
//        PreparedStatement statement = null;
//        try (Connection sqlConnection = sqlService.getConnection()) {
//
//            String queryString = "UPDATE " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                    " SET " + Constants.ENABLED_OVERRIDES_RESPONSE_CODE + "= ? " +
//                    " WHERE " + Constants.GENERIC_ID + " = ?";
//            statement = sqlConnection.prepareStatement(queryString);
//            statement.setString(1, responseCode);
//            statement.setInt(2, id);
//            statement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Remove specified override id from enabled overrides for path
//     *
//     * @param overrideId ID of override to remove
//     * @param pathId ID of path containing override
//     * @param ordinal index to the instance of the enabled override
//     * @param clientUUID UUID of client
//     */
//    public void removeOverride(int overrideId, int pathId, Integer ordinal, String clientUUID) {
//        // TODO: reorder priorities after removal
//        PreparedStatement statement = null;
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            int enabledId = getEnabledEndpoint(pathId, overrideId, ordinal, clientUUID).getId();
//            statement = sqlConnection.prepareStatement(
//                "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                    " WHERE " + Constants.GENERIC_ID + " = ?"
//            );
//            statement.setInt(1, enabledId);
//            statement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Increase the priority of an overrideId
//     *
//     * @param overrideId ID of override
//     * @param pathId ID of path containing override
//     * @param clientUUID UUID of client
//     */
//    public void increasePriority(int overrideId, int ordinal, int pathId, String clientUUID) {
//        logger.info("Increase priority");
//
//        int origPriority = -1;
//        int newPriority = -1;
//        int origId = 0;
//        int newId = 0;
//
//        PreparedStatement statement = null;
//        ResultSet results = null;
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            results = null;
//            statement = sqlConnection.prepareStatement(
//                "SELECT * FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                    " WHERE " + Constants.ENABLED_OVERRIDES_PATH_ID + " = ?" +
//                    " AND " + Constants.GENERIC_CLIENT_UUID + " = ?" +
//                    " ORDER BY " + Constants.ENABLED_OVERRIDES_PRIORITY
//            );
//            statement.setInt(1, pathId);
//            statement.setString(2, clientUUID);
//            results = statement.executeQuery();
//
//            int ordinalCount = 0;
//            while (results.next()) {
//                if (results.getInt(Constants.ENABLED_OVERRIDES_OVERRIDE_ID) == overrideId) {
//                    ordinalCount++;
//                    if (ordinalCount == ordinal) {
//                        origPriority = results.getInt(Constants.ENABLED_OVERRIDES_PRIORITY);
//                        origId = results.getInt(Constants.GENERIC_ID);
//                        break;
//                    }
//                }
//                newPriority = results.getInt(Constants.ENABLED_OVERRIDES_PRIORITY);
//                newId = results.getInt(Constants.GENERIC_ID);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (results != null) {
//                    results.close();
//                }
//            } catch (Exception e) {
//            }
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            // update priorities
//            if (origPriority != -1 && newPriority != -1) {
//                statement = sqlConnection.prepareStatement(
//                    "UPDATE " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                        " SET " + Constants.ENABLED_OVERRIDES_PRIORITY + "=?" +
//                        " WHERE " + Constants.GENERIC_ID + "=?"
//                );
//                statement.setInt(1, origPriority);
//                statement.setInt(2, newId);
//                statement.executeUpdate();
//                statement.close();
//
//                statement = sqlConnection.prepareStatement(
//                    "UPDATE " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                        " SET " + Constants.ENABLED_OVERRIDES_PRIORITY + "=?" +
//                        " WHERE " + Constants.GENERIC_ID + "=?"
//                );
//                statement.setInt(1, newPriority);
//                statement.setInt(2, origId);
//                statement.executeUpdate();
//            }
//        } catch (Exception e) {
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Decreases the priority of an overrideId
//     *
//     * @param overrideId Id of override to edit
//     * @param pathId ID of path containing override
//     * @param clientUUID ID of client
//     */
//    public void decreasePriority(int overrideId, int ordinal, int pathId, String clientUUID) {
//        logger.info("Decrease priority");
//        int origPriority = -1;
//        int newPriority = -1;
//
//        int origId = 0;
//        int newId = 0;
//
//        PreparedStatement queryStatement = null;
//        ResultSet results = null;
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            queryStatement = sqlConnection.prepareStatement(
//                "SELECT * FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                    " WHERE " + Constants.ENABLED_OVERRIDES_PATH_ID + " = ?" +
//                    " AND " + Constants.GENERIC_CLIENT_UUID + " = ?" +
//                    " ORDER BY " + Constants.ENABLED_OVERRIDES_PRIORITY
//            );
//            queryStatement.setInt(1, pathId);
//            queryStatement.setString(2, clientUUID);
//            results = queryStatement.executeQuery();
//            boolean gotOrig = false;
//            int ordinalCount = 0;
//
//            while (results.next()) {
//                if (results.getInt(Constants.ENABLED_OVERRIDES_OVERRIDE_ID) == overrideId) {
//                    ordinalCount++;
//                    if (ordinalCount == ordinal) {
//                        origPriority = results.getInt(Constants.ENABLED_OVERRIDES_PRIORITY);
//                        origId = results.getInt(Constants.GENERIC_ID);
//                        gotOrig = true;
//                        continue;
//                    }
//                }
//                newPriority = results.getInt(Constants.ENABLED_OVERRIDES_PRIORITY);
//                newId = results.getInt(Constants.GENERIC_ID);
//
//                // break out because this is the one after the one we want to move down
//                if (gotOrig) {
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (results != null) {
//                    results.close();
//                }
//            } catch (Exception e) {
//            }
//            try {
//                if (queryStatement != null) {
//                    queryStatement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//
//        PreparedStatement statement = null;
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            // update priorities
//            if (origPriority != -1 && newPriority != -1) {
//                statement = sqlConnection.prepareStatement(
//                    "UPDATE " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                        " SET " + Constants.ENABLED_OVERRIDES_PRIORITY + "=?" +
//                        " WHERE " + Constants.GENERIC_ID + "=?"
//                );
//                statement.setInt(1, origPriority);
//                statement.setInt(2, newId);
//                statement.executeUpdate();
//                statement.close();
//
//                statement = sqlConnection.prepareStatement(
//                    "UPDATE " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                        " SET " + Constants.ENABLED_OVERRIDES_PRIORITY + "=?" +
//                        " WHERE " + Constants.GENERIC_ID + "=?"
//                );
//                statement.setInt(1, newPriority);
//                statement.setInt(2, origId);
//                statement.executeUpdate();
//            }
//        } catch (Exception e) {
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Creates a list of placeholders for use in a PreparedStatement
//     *
//     * @param length number of placeholders
//     * @return String of placeholders, seperated by comma
//     */
//    private static String preparePlaceHolders(int length) {
//        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < length; ) {
//            builder.append("?");
//            if (++i < length) {
//                builder.append(",");
//            }
//        }
//        return builder.toString();
//    }
//
//    /**
//     * Disable all overrides for a specified path
//     *
//     * @param pathID ID of path containing overrides
//     * @param clientUUID UUID of client
//     */
//    public void disableAllOverrides(int pathID, String clientUUID) {
//        PreparedStatement statement = null;
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            statement = sqlConnection.prepareStatement(
//                "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                    " WHERE " + Constants.ENABLED_OVERRIDES_PATH_ID + " = ? " +
//                    " AND " + Constants.GENERIC_CLIENT_UUID + " = ? "
//            );
//            statement.setInt(1, pathID);
//            statement.setString(2, clientUUID);
//            statement.execute();
//            statement.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Disable all overrides for a specified path with overrideType
//     *
//     * @param pathID ID of path containing overrides
//     * @param clientUUID UUID of client
//     * @param overrideType Override type identifier
//     */
//    public void disableAllOverrides(int pathID, String clientUUID, int overrideType) {
//        PreparedStatement statement = null;
//
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            ArrayList<Integer> enabledOverrides = new ArrayList<Integer>();
//            enabledOverrides.add(Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_ADD);
//            enabledOverrides.add(Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_REMOVE);
//            enabledOverrides.add(Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM);
//            enabledOverrides.add(Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM_POST_BODY);
//
//            String overridePlaceholders = preparePlaceHolders(enabledOverrides.size());
//
//            statement = sqlConnection.prepareStatement(
//                "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                    " WHERE " + Constants.ENABLED_OVERRIDES_PATH_ID + " = ? " +
//                    " AND " + Constants.GENERIC_CLIENT_UUID + " = ? " +
//                    " AND " + Constants.ENABLED_OVERRIDES_OVERRIDE_ID +
//                    (overrideType == Constants.OVERRIDE_TYPE_RESPONSE ? " NOT" : "") +
//                    " IN ( " + overridePlaceholders + " )"
//            );
//            statement.setInt(1, pathID);
//            statement.setString(2, clientUUID);
//            for (int i = 3; i <= enabledOverrides.size() + 2; ++i) {
//                statement.setInt(i, enabledOverrides.get(i - 3));
//            }
//            statement.execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
//
//    /**
//     * Returns an array of the enabled endpoints as Integer IDs
//     *
//     * @param pathId ID of path
//     * @param clientUUID UUID of client
//     * @param filters If supplied, only endpoints ending with values in filters are returned
//     * @return Collection of endpoints
//     * @throws Exception exception
//     */
//    public List<EnabledEndpoint> getEnabledEndpoints(int pathId, String clientUUID, String[] filters) throws Exception {
//        ArrayList<EnabledEndpoint> enabledOverrides = new ArrayList<EnabledEndpoint>();
//        PreparedStatement query = null;
//        ResultSet results = null;
//
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            query = sqlConnection.prepareStatement(
//                "SELECT * FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                    " WHERE " + Constants.ENABLED_OVERRIDES_PATH_ID + "=?" +
//                    " AND " + Constants.GENERIC_CLIENT_UUID + "=?" +
//                    " ORDER BY " + Constants.ENABLED_OVERRIDES_PRIORITY
//            );
//            query.setInt(1, pathId);
//            query.setString(2, clientUUID);
//            results = query.executeQuery();
//
//            while (results.next()) {
//                EnabledEndpoint endpoint = this.getPartialEnabledEndpointFromResultset(results);
//                com.groupon.odo.proxylib.models.Method m = PathOverrideService.getInstance().getMethodForOverrideId(endpoint.getOverrideId());
//
//                // this is an errant entry.. perhaps a method got deleted from a plugin
//                // we'll also remove it from the endpoint
//                if (m == null) {
//                    PathOverrideService.getInstance().removeOverride(endpoint.getOverrideId());
//                    continue;
//                }
//
//                // check filters and see if any match
//                boolean addOverride = false;
//                if (filters != null) {
//                    for (String filter : filters) {
//                        if (m.getMethodType().endsWith(filter)) {
//                            addOverride = true;
//                            break;
//                        }
//                    }
//                } else {
//                    // if there are no filters then we assume that the requester wants all enabled overrides
//                    addOverride = true;
//                }
//
//                if (addOverride) {
//                    enabledOverrides.add(endpoint);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (results != null) {
//                    results.close();
//                }
//            } catch (Exception e) {
//            }
//            try {
//                if (query != null) {
//                    query.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//
//        // now go through the ArrayList and get the method for all of the endpoints
//        // have to do this so we don't have overlapping SQL queries
//        ArrayList<EnabledEndpoint> enabledOverridesWithMethods = new ArrayList<EnabledEndpoint>();
//        for (EnabledEndpoint endpoint : enabledOverrides) {
//            if (endpoint.getOverrideId() >= 0) {
//                com.groupon.odo.proxylib.models.Method m = PathOverrideService.getInstance().getMethodForOverrideId(endpoint.getOverrideId());
//                endpoint.setMethodInformation(m);
//            }
//            enabledOverridesWithMethods.add(endpoint);
//        }
//
//        return enabledOverridesWithMethods;
//    }
//
//    /**
//     * Get the ordinal value for the last of a particular override on a path
//     *
//     * @param overrideId Id of the override to check
//     * @param pathId Path the override is on
//     * @param clientUUID UUID of the client
//     * @param filters If supplied, only endpoints ending with values in filters are returned
//     * @return The integer ordinal
//     * @throws Exception
//     */
//    public int getCurrentMethodOrdinal(int overrideId, int pathId, String clientUUID, String[] filters) throws Exception {
//        int currentOrdinal = 0;
//        List<EnabledEndpoint> enabledEndpoints = getEnabledEndpoints(pathId, clientUUID, filters);
//        for (EnabledEndpoint enabledEndpoint : enabledEndpoints) {
//            if (enabledEndpoint.getOverrideId() == overrideId) {
//                currentOrdinal++;
//            }
//        }
//        return currentOrdinal;
//    }
//
//    /**
//     * @param pathId ID of path
//     * @param overrideId ID of override
//     * @param ordinal Index of the enabled override to get if multiple of the same override are enabled(default is 1)
//     * @param clientUUID UUID of client
//     * @return EnabledEndpoint
//     * @throws Exception exception
//     */
//    public EnabledEndpoint getEnabledEndpoint(int pathId, int overrideId, Integer ordinal, String clientUUID) throws Exception {
//        EnabledEndpoint endpoint = null;
//        PreparedStatement statement = null;
//        ResultSet results = null;
//
//        if (ordinal == null) {
//            ordinal = 1;
//        }
//
//        // try to get it from the database
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            // decrease ordinal by 1 so offset works right
//            ordinal--;
//
//            String queryString = "SELECT * FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
//                " WHERE " + Constants.ENABLED_OVERRIDES_PATH_ID + "=? " +
//                " AND " + Constants.ENABLED_OVERRIDES_OVERRIDE_ID + "=? " +
//                " AND " + Constants.GENERIC_CLIENT_UUID + "=? " +
//                "ORDER BY " + Constants.PRIORITY + " LIMIT 1 OFFSET ?";
//            statement = sqlConnection.prepareStatement(queryString);
//            statement.setInt(1, pathId);
//            statement.setInt(2, overrideId);
//            statement.setString(3, clientUUID);
//            statement.setInt(4, ordinal);
//
//            results = statement.executeQuery();
//            while (results.next()) {
//                endpoint = this.getPartialEnabledEndpointFromResultset(results);
//                break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (results != null) {
//                    results.close();
//                }
//            } catch (Exception e) {
//            }
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//
//        if (endpoint != null) {
//            // get the method also for a real endpoint
//            if (endpoint.getOverrideId() >= 0) {
//                com.groupon.odo.proxylib.models.Method m = PathOverrideService.getInstance().getMethodForOverrideId(endpoint.getOverrideId());
//                endpoint.setMethodInformation(m);
//            } else if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_ADD
//                || endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_ADD) {
//                // set fake method info
//                com.groupon.odo.proxylib.models.Method m = new com.groupon.odo.proxylib.models.Method();
//
//                m.setMethodArgumentNames(new String[] {"key", "value"});
//                m.setMethodArguments(new Object[] {String.class, String.class});
//                m.setClassName("");
//                m.setMethodName("CUSTOM HEADER");
//
//                if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_ADD) {
//                    m.setDescription("Set a response header");
//                } else if (endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_ADD) {
//                    m.setDescription("Set a request header");
//                }
//
//                endpoint.setMethodInformation(m);
//            } else if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_REMOVE
//                || endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_REMOVE) {
//                // set fake method info
//                com.groupon.odo.proxylib.models.Method m = new com.groupon.odo.proxylib.models.Method();
//
//                m.setMethodArgumentNames(new String[] {"key"});
//                m.setMethodArguments(new Object[] {String.class});
//                m.setClassName("");
//                m.setMethodName("REMOVE HEADER");
//
//                if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_REMOVE) {
//                    m.setDescription("Remove a response header");
//                } else if (endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_REMOVE) {
//                    m.setDescription("Remove a request header");
//                }
//
//                endpoint.setMethodInformation(m);
//            } else if (endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM
//                || endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_OVERRIDE_CUSTOM
//                || endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM_POST_BODY) {
//                // set fake method info
//                com.groupon.odo.proxylib.models.Method m = new com.groupon.odo.proxylib.models.Method();
//
//                m.setMethodArgumentNames(new String[] {"response"});
//                m.setMethodArguments(new Object[] {String.class});
//                m.setClassName("");
//                m.setMethodName("CUSTOM");
//
//                if (endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM) {
//                    m.setDescription("Return a custom request");
//                } else if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_OVERRIDE_CUSTOM) {
//                    m.setDescription("Return a custom response");
//                }
//
//                endpoint.setMethodInformation(m);
//            }
//        }
//
//        return endpoint;
//    }
//
//    /**
//     * This only gets half of the EnabledEndpoint from a JDBC ResultSet
//     * Getting the method for the override id requires an additional SQL query and needs to be called after
//     * the SQL connection is released
//     *
//     * @param result result to scan for endpoint
//     * @return EnabledEndpoint
//     * @throws Exception exception
//     */
//    private EnabledEndpoint getPartialEnabledEndpointFromResultset(ResultSet result) throws Exception {
//        EnabledEndpoint endpoint = new EnabledEndpoint();
//        endpoint.setId(result.getInt(Constants.GENERIC_ID));
//        endpoint.setPathId(result.getInt(Constants.ENABLED_OVERRIDES_PATH_ID));
//        endpoint.setOverrideId(result.getInt(Constants.ENABLED_OVERRIDES_OVERRIDE_ID));
//        endpoint.setPriority(result.getInt(Constants.ENABLED_OVERRIDES_PRIORITY));
//        endpoint.setRepeatNumber(result.getInt(Constants.ENABLED_OVERRIDES_REPEAT_NUMBER));
//        endpoint.setResponseCode(result.getString(Constants.ENABLED_OVERRIDES_RESPONSE_CODE));
//
//        ArrayList<Object> args = new ArrayList<Object>();
//        try {
//            JSONArray arr = new JSONArray(result.getString(Constants.ENABLED_OVERRIDES_ARGUMENTS));
//            for (int x = 0; x < arr.length(); x++) {
//                args.add(arr.get(x));
//            }
//        } catch (Exception e) {
//            // ignore it.. this means the entry was null/corrupt
//        }
//
//        endpoint.setArguments(args.toArray(new Object[0]));
//
//        return endpoint;
//    }
//
//    /**
//     * Gets an overrideID for a class name, method name
//     *
//     * @param className name of class
//     * @param methodName name of method
//     * @return override ID of method
//     */
//    public Integer getOverrideIdForMethod(String className, String methodName) {
//        Integer overrideId = null;
//        PreparedStatement query = null;
//        ResultSet results = null;
//
//        try (Connection sqlConnection = sqlService.getConnection()) {
//            query = sqlConnection.prepareStatement(
//                "SELECT * FROM " + Constants.DB_TABLE_OVERRIDE +
//                    " WHERE " + Constants.OVERRIDE_CLASS_NAME + " = ?" +
//                    " AND " + Constants.OVERRIDE_METHOD_NAME + " = ?"
//            );
//            query.setString(1, className);
//            query.setString(2, methodName);
//            results = query.executeQuery();
//
//            if (results.next()) {
//                overrideId = results.getInt(Constants.GENERIC_ID);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            try {
//                if (results != null) {
//                    results.close();
//                }
//            } catch (Exception e) {
//            }
//            try {
//                if (query != null) {
//                    query.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//
//        return overrideId;
//    }
//}
