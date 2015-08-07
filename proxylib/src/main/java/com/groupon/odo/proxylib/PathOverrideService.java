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

import com.groupon.odo.proxylib.models.Client;
import com.groupon.odo.proxylib.models.EndpointOverride;
import com.groupon.odo.proxylib.models.Group;
import com.groupon.odo.proxylib.models.Method;
import com.groupon.odo.proxylib.models.Profile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has methods to help with adding/updating/deleting paths, overrides and groups
 * It uses tables from the EditService and makes SQL queries for those tables
 * Most methods are pretty simple, with the exception of findAllGroups and removeGroupIdFromTablePaths
 */
public class PathOverrideService {

    static final Logger logger = LoggerFactory.getLogger(PathOverrideService.class);
    private static PathOverrideService serviceInstance = null;
    static SQLService sqlService = null;

    public PathOverrideService() {
    }

    public static PathOverrideService getInstance() {
        if (serviceInstance == null) {
            try {
                sqlService = SQLService.getInstance();
                serviceInstance = new PathOverrideService();
            } catch (Exception e) {
                logger.info("Error starting PathOverrideService: {}", e.getMessage());
                return null;
            }
        }
        return serviceInstance;
    }

    /**
     * Obtain all groups
     *
     * @return All Groups
     */
    public List<Group> findAllGroups() {
        ArrayList<Group> allGroups = new ArrayList<Group>();
        PreparedStatement queryStatement = null;
        ResultSet results = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            queryStatement = sqlConnection.prepareStatement("SELECT * FROM "
                                                                + Constants.DB_TABLE_GROUPS +
                                                                " ORDER BY " + Constants.GROUPS_GROUP_NAME);
            results = queryStatement.executeQuery();
            while (results.next()) {
                Group group = new Group();
                group.setId(results.getInt(Constants.GENERIC_ID));
                group.setName(results.getString(Constants.GROUPS_GROUP_NAME));
                allGroups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) {
                    queryStatement.close();
                }
            } catch (Exception e) {
            }
        }

        return allGroups;
    }

    /**
     * Add a path to a profile, returns the id
     *
     * @param id ID of profile
     * @param pathname name of path
     * @param actualPath value of path
     * @return ID of path created
     * @throws Exception exception
     */
    public int addPathnameToProfile(int id, String pathname, String actualPath) throws Exception {
        int pathOrder = getPathOrder(id).size() + 1;
        int pathId = -1;
        PreparedStatement statement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "INSERT INTO " + Constants.DB_TABLE_PATH
                    + "(" + Constants.PATH_PROFILE_PATHNAME + ","
                    + Constants.PATH_PROFILE_ACTUAL_PATH + ","
                    + Constants.PATH_PROFILE_GROUP_IDS + ","
                    + Constants.PATH_PROFILE_PROFILE_ID + ","
                    + Constants.PATH_PROFILE_PATH_ORDER + ","
                    + Constants.PATH_PROFILE_CONTENT_TYPE + ","
                    + Constants.PATH_PROFILE_REQUEST_TYPE + ","
                    + Constants.PATH_PROFILE_GLOBAL + ")"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
                PreparedStatement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, pathname);
            statement.setString(2, actualPath);
            statement.setString(3, "");
            statement.setInt(4, id);
            statement.setInt(5, pathOrder);
            statement.setString(6, Constants.PATH_PROFILE_DEFAULT_CONTENT_TYPE); // should be set by UI/API
            statement.setInt(7, Constants.REQUEST_TYPE_GET); // should be set by UI/API
            statement.setBoolean(8, false);
            statement.executeUpdate();

            // execute statement and get resultSet which will have the generated path ID as the first field
            results = statement.getGeneratedKeys();

            if (results.next()) {
                pathId = results.getInt(1);
            } else {
                // something went wrong
                throw new Exception("Could not add path");
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }

        // need to add to request response table for all clients
        for (Client client : ClientService.getInstance().findAllClients(id)) {
            this.addPathToRequestResponseTable(id, client.getUUID(), pathId);
        }

        return pathId;
    }

    /**
     * Adds a path to the request response table with the specified values
     *
     * @param profileId ID of profile
     * @param clientUUID UUID of client
     * @param pathId ID of path
     * @throws Exception exception
     */
    public void addPathToRequestResponseTable(int profileId, String clientUUID, int pathId) throws Exception {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection
                .prepareStatement("INSERT INTO " + Constants.DB_TABLE_REQUEST_RESPONSE +
                                      "(" + Constants.REQUEST_RESPONSE_PATH_ID + ","
                                      + Constants.GENERIC_PROFILE_ID + ","
                                      + Constants.GENERIC_CLIENT_UUID + ","
                                      + Constants.REQUEST_RESPONSE_REPEAT_NUMBER + ","
                                      + Constants.REQUEST_RESPONSE_RESPONSE_ENABLED + ","
                                      + Constants.REQUEST_RESPONSE_REQUEST_ENABLED + ","
                                      + Constants.REQUEST_RESPONSE_CUSTOM_RESPONSE + ","
                                      + Constants.REQUEST_RESPONSE_CUSTOM_REQUEST + ")"
                                      + " VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
            statement.setInt(1, pathId);
            statement.setInt(2, profileId);
            statement.setString(3, clientUUID);
            statement.setInt(4, -1);
            statement.setInt(5, 0);
            statement.setInt(6, 0);
            statement.setString(7, "");
            statement.setString(8, "");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Return collection of path Ids in priority order
     *
     * @param profileId ID of profile
     * @return collection of path Ids in priority order
     */
    public List<Integer> getPathOrder(int profileId) {
        ArrayList<Integer> pathOrder = new ArrayList<Integer>();
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            queryStatement = sqlConnection.prepareStatement(
                "SELECT * FROM "
                    + Constants.DB_TABLE_PATH + " WHERE "
                    + Constants.GENERIC_PROFILE_ID + " = ? "
                    + " ORDER BY " + Constants.PATH_PROFILE_PATH_ORDER + " ASC"
            );
            queryStatement.setInt(1, profileId);
            results = queryStatement.executeQuery();
            while (results.next()) {
                pathOrder.add(results.getInt(Constants.GENERIC_ID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) {
                    queryStatement.close();
                }
            } catch (Exception e) {
            }
        }
        logger.info("pathOrder = {}", pathOrder);
        return pathOrder;
    }

    /**
     * Called right now when we add an entry to the path_profile table
     * Then we update the table to add a new string that contains the old groups
     * and the new group (followed by a comma)
     *
     * @param profileId ID of profile
     * @param pathId ID of path
     * @param groupNum Group to add
     */
    public void AddGroupByNumber(int profileId, int pathId, int groupNum) {
        logger.info("adding group_id={}, to pathId={}", groupNum, pathId);
        String oldGroups = getGroupIdsInPathProfile(profileId, pathId);
        // make sure the old groups does not contain the current group we want
        // to add
        if (!intArrayContains(Utils.arrayFromStringOfIntegers(oldGroups), groupNum)) {
            if (!oldGroups.endsWith(",") && !oldGroups.isEmpty()) {
                oldGroups += ",";
            }
            String newGroups = (oldGroups + groupNum);
            EditService.updatePathTable(Constants.PATH_PROFILE_GROUP_IDS, newGroups, pathId);
        } else {
            logger.info("that group is already contained in for this uuid/path");
        }
    }

    /**
     * Set the groups assigned to a path
     *
     * @param groups group IDs to set
     * @param pathId ID of path
     */
    public void setGroupsForPath(Integer[] groups, int pathId) {
        String newGroups = Arrays.toString(groups);
        newGroups = newGroups.substring(1, newGroups.length() - 1).replaceAll("\\s", "");

        logger.info("adding groups={}, to pathId={}", newGroups, pathId);
        EditService.updatePathTable(Constants.PATH_PROFILE_GROUP_IDS, newGroups, pathId);
    }

    /**
     * a simple contains helper method, checks if array contains a numToCheck
     *
     * @param array array of ints
     * @param numToCheck value to find
     * @return True if found, false otherwise
     */
    public static boolean intArrayContains(int[] array, int numToCheck) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == numToCheck) {
                return true;
            }
        }
        return false;
    }

    /**
     * First we get the oldGroups by looking at the database to find the path/profile match
     *
     * @param profileId ID of profile
     * @param pathId ID of path
     * @return Comma-delimited list of groups IDs
     */
    public String getGroupIdsInPathProfile(int profileId, int pathId) {
        return (String) sqlService.getFromTable(Constants.PATH_PROFILE_GROUP_IDS, Constants.GENERIC_ID, pathId,
                                                Constants.DB_TABLE_PATH);
    }

    /**
     * When passed in the name of the group, this creates a new entry for it in the table
     * Afterwards, it gets the groupId for other purposes (ie to create overrides that correspond to this specific group)
     *
     * @param nameOfGroup name of group to add
     * @return ID of group created
     */
    public Integer addGroup(String nameOfGroup) {
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            queryStatement = sqlConnection.prepareStatement(
                "INSERT INTO " + Constants.DB_TABLE_GROUPS
                    + "(" + Constants.GROUPS_GROUP_NAME + ")"
                    + " VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS
            );
            queryStatement.setString(1, nameOfGroup);
            queryStatement.executeUpdate();

            // execute statement and get resultSet which will have the generated path ID as the first field
            results = queryStatement.getGeneratedKeys();
            int groupId = -1;
            if (results.next()) {
                groupId = results.getInt(1);
            } else {
                // something went wrong
                throw new Exception("Could not add group");
            }
            return groupId;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) {
                    queryStatement.close();
                }
            } catch (Exception e) {
            }
        }

        return null;
    }

    /**
     * given the groupName, it returns the groupId
     *
     * @param groupName name of group
     * @return ID of group
     */
    public Integer getGroupIdFromName(String groupName) {
        return (Integer) sqlService.getFromTable(Constants.GENERIC_ID, Constants.GROUPS_GROUP_NAME, groupName,
                                                 Constants.DB_TABLE_GROUPS);
    }

    /**
     * given the groupId, and 2 string arrays, adds the name-responses pair to the table_override
     *
     * @param groupId ID of group
     * @param methodName name of method
     * @param className name of class
     * @throws Exception exception
     */
    public void createOverride(int groupId, String methodName, String className) throws Exception {
        // first make sure this doesn't already exist
        for (Method method : EditService.getInstance().getMethodsFromGroupId(groupId, null)) {
            if (method.getMethodName().equals(methodName) && method.getClassName().equals(className)) {
                // don't add if it already exists in the group
                return;
            }
        }

        try (Connection sqlConnection = sqlService.getConnection()) {
            PreparedStatement statement = sqlConnection.prepareStatement(
                "INSERT INTO " + Constants.DB_TABLE_OVERRIDE
                    + "(" + Constants.OVERRIDE_METHOD_NAME
                    + "," + Constants.OVERRIDE_CLASS_NAME
                    + "," + Constants.OVERRIDE_GROUP_ID
                    + ")"
                    + " VALUES (?, ?, ?)"
            );
            statement.setString(1, methodName);
            statement.setString(2, className);
            statement.setInt(3, groupId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * given the groupId, returns the groupName
     *
     * @param groupId ID of group
     * @return name of group
     */
    public String getGroupNameFromId(int groupId) {
        return (String) sqlService.getFromTable(Constants.GROUPS_GROUP_NAME, Constants.GENERIC_ID, groupId,
                                                Constants.DB_TABLE_GROUPS);
    }

    /**
     * updates the groupname in the table given the id
     *
     * @param newGroupName new group name
     * @param id ID of group
     */
    public void updateGroupName(String newGroupName, int id) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_GROUPS +
                    " SET " + Constants.GROUPS_GROUP_NAME + " = ? " +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, newGroupName);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Remove the group and all references to it
     *
     * @param groupId ID of group
     */
    public void removeGroup(int groupId) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_GROUPS
                    + " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setInt(1, groupId);
            statement.executeUpdate();
            statement.close();

            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_OVERRIDE +
                    " WHERE " + Constants.OVERRIDE_GROUP_ID + " = ?"
            );

            statement.setInt(1, groupId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
        removeGroupIdFromTablePaths(groupId);
    }

    /**
     * Remove all references to a groupId
     *
     * @param groupIdToRemove ID of group
     */
    private void removeGroupIdFromTablePaths(int groupIdToRemove) {
        PreparedStatement queryStatement = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            queryStatement = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB_TABLE_PATH);
            results = queryStatement.executeQuery();
            // this is a hashamp from a pathId to the string of groups
            HashMap<Integer, String> idToGroups = new HashMap<Integer, String>();
            while (results.next()) {
                int pathId = results.getInt(Constants.GENERIC_ID);
                String stringGroupIds = results.getString(Constants.PATH_PROFILE_GROUP_IDS);
                int[] groupIds = Utils.arrayFromStringOfIntegers(stringGroupIds);
                String newGroupIds = "";
                for (int i = 0; i < groupIds.length; i++) {
                    if (groupIds[i] != groupIdToRemove) {
                        newGroupIds += (groupIds[i] + ",");
                    }
                }
                idToGroups.put(pathId, newGroupIds);
            }

            // now i want to go though the hashmap and for each pathId, add
            // update the newGroupIds
            for (Map.Entry<Integer, String> entry : idToGroups.entrySet()) {
                Integer pathId = entry.getKey();
                String newGroupIds = entry.getValue();

                statement = sqlConnection.prepareStatement(
                    "UPDATE " + Constants.DB_TABLE_PATH
                        + " SET " + Constants.PATH_PROFILE_GROUP_IDS + " = ? "
                        + " WHERE " + Constants.GENERIC_ID + " = ?"
                );
                statement.setString(1, newGroupIds);
                statement.setInt(2, pathId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) {
                    queryStatement.close();
                }
            } catch (Exception e) {
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Removes an override from the overrideTable as well as the enabled
     * overrides (cant be enabled if its removed...)
     *
     * @param overrideId ID of override
     */
    public void removeOverride(int overrideId) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_OVERRIDE + " WHERE " + Constants.GENERIC_ID + " = ?");
            statement.setInt(1, overrideId);
            statement.executeUpdate();
            statement.close();

            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE
                    + " WHERE " + Constants.ENABLED_OVERRIDES_OVERRIDE_ID + " = ?"
            );
            statement.setInt(1, overrideId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Remove an override from a group by method/classname
     *
     * @param groupId ID of group
     * @param methodName name of method
     * @param className name of class
     */
    public void removeOverride(int groupId, String methodName, String className) {
        String statementString = "DELETE FROM " + Constants.DB_TABLE_OVERRIDE + " WHERE " + Constants.OVERRIDE_GROUP_ID + " = ? AND " +
            Constants.OVERRIDE_CLASS_NAME + " = ? AND " + Constants.OVERRIDE_METHOD_NAME + " = ?";
        try (Connection sqlConnection = sqlService.getConnection()) {
            try (PreparedStatement statement = sqlConnection.prepareStatement(statementString)) {
                statement.setInt(1, groupId);
                statement.setString(2, className);
                statement.setString(3, methodName);
                statement.executeUpdate();
                statement.close();

                // TODO: delete from enabled overrides
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a path
     *
     * @param pathId ID of path
     */
    public void removePath(int pathId) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            // remove any enabled overrides with this path
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
                    " WHERE " + Constants.ENABLED_OVERRIDES_PATH_ID + " = ?"
            );
            statement.setInt(1, pathId);
            statement.executeUpdate();
            statement.close();

            // remove path
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_PATH
                    + " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setInt(1, pathId);
            statement.executeUpdate();
            statement.close();

            //remove path from responseRequest
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_REQUEST_RESPONSE
                    + " WHERE " + Constants.REQUEST_RESPONSE_PATH_ID + " = ?"
            );
            statement.setInt(1, pathId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns a method object for a path id.
     *
     * @param enabledId ID of path
     * @return Method enabled on path
     */
    // TODO: make this be able to return all enabled methods for a path instead of just the first method
    public com.groupon.odo.proxylib.models.Method getMethodForEnabledId(int enabledId) {
        com.groupon.odo.proxylib.models.Method method = null;
        int override_id = 0;
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            queryStatement = sqlConnection.prepareStatement(
                "SELECT * FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
                    " WHERE " + Constants.ENABLED_OVERRIDES_OVERRIDE_ID + " = ?"
            );
            queryStatement.setInt(1, enabledId);
            results = queryStatement.executeQuery();
            if (results.next()) {
                override_id = results.getInt(Constants.ENABLED_OVERRIDES_OVERRIDE_ID);

                queryStatement = sqlConnection.prepareStatement(
                    "SELECT * FROM " + Constants.DB_TABLE_OVERRIDE +
                        " WHERE " + Constants.GENERIC_ID + " = ?"
                );
                queryStatement.setInt(1, override_id);
                results = queryStatement.executeQuery();
                if (results.next()) {
                    method = new com.groupon.odo.proxylib.models.Method();
                    method.setId(override_id);
                    method.setClassName(results.getString(Constants.OVERRIDE_CLASS_NAME));
                    method.setMethodName(results.getString(Constants.OVERRIDE_METHOD_NAME));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) {
                    queryStatement.close();
                }
            } catch (Exception e) {
            }
        }

        // now get the rest of the data from the plugin manager
        // this gets all of the actual data
        try {
            method = PluginManager.getInstance().getMethod(method.getClassName(), method.getMethodName());
            method.setId(override_id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return method;
    }

    /**
     * Gets a method based on data in the override_db table
     *
     * @param overrideId ID of override
     * @return Method found
     */
    public com.groupon.odo.proxylib.models.Method getMethodForOverrideId(int overrideId) {
        com.groupon.odo.proxylib.models.Method method = null;

        // special case for IDs < 0
        if (overrideId < 0) {
            method = new com.groupon.odo.proxylib.models.Method();
            method.setId(overrideId);

            if (method.getId() == Constants.PLUGIN_RESPONSE_OVERRIDE_CUSTOM ||
                method.getId() == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_ADD ||
                method.getId() == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_REMOVE) {
                method.setMethodType(Constants.PLUGIN_TYPE_RESPONSE_OVERRIDE);
            } else {
                method.setMethodType(Constants.PLUGIN_TYPE_REQUEST_OVERRIDE);
            }
        } else {
            // get method information from the database
            PreparedStatement queryStatement = null;
            ResultSet results = null;
            try (Connection sqlConnection = sqlService.getConnection()) {
                queryStatement = sqlConnection.prepareStatement(
                    "SELECT * FROM " + Constants.DB_TABLE_OVERRIDE +
                        " WHERE " + Constants.GENERIC_ID + " = ?"
                );
                queryStatement.setInt(1, overrideId);
                results = queryStatement.executeQuery();

                if (results.next()) {
                    method = new com.groupon.odo.proxylib.models.Method();
                    method.setClassName(results.getString(Constants.OVERRIDE_CLASS_NAME));
                    method.setMethodName(results.getString(Constants.OVERRIDE_METHOD_NAME));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (results != null) {
                        results.close();
                    }
                } catch (Exception e) {
                }
                try {
                    if (queryStatement != null) {
                        queryStatement.close();
                    }
                } catch (Exception e) {
                }
            }

            // if method is still null then just return
            if (method == null) {
                return method;
            }

            // now get the rest of the data from the plugin manager
            // this gets all of the actual method data
            try {
                method = PluginManager.getInstance().getMethod(method.getClassName(), method.getMethodName());
                method.setId(overrideId);
            } catch (Exception e) {
                // there was some problem.. return null
                return null;
            }
        }

        return method;
    }

    /**
     * Given a id (for the profile) and a pathname, this returns all the groups
     * that are contained within that path/profile combination
     *
     * @param profileId ID of profile
     * @param pathId ID of path
     * @return Collection of Groups for the path
     */
    public List<Group> getGroupsInPathProfile(
        int profileId, int pathId) {
        ArrayList<Group> groupsInProfile = new ArrayList<Group>();
        ArrayList<Group> allGroups = new ArrayList<Group>(findAllGroups());
        int[] groupIds = Utils.arrayFromStringOfIntegers(getGroupIdsInPathProfile(profileId,
                                                                                  pathId));
        // get all the groups, then remove the ones != group ids, leaving us
        // with all the groups in the profile
        for (int j = 0; j < allGroups.size(); j++) {
            for (int i = 0; i < groupIds.length; i++) {
                if (allGroups.get(j).getId() == groupIds[i]) {
                    groupsInProfile.add(allGroups.get(j));
                }
            }
        }
        return groupsInProfile;
    }

    /**
     * Given a profileId (for the profile) and a pathname, this returns all the groups
     * that are NOT contained within that path/profile combination
     *
     * @param profileId ID of profile
     * @param pathId ID of path
     * @return Collection of Groups
     */
    public List<Group> getGroupsNotInPathProfile(
        int profileId, int pathId) {
        ArrayList<Group> allGroups = new ArrayList<Group>(findAllGroups());
        ArrayList<Group> groupsNotInProfile = new ArrayList<Group>();
        int[] groupIds = Utils.arrayFromStringOfIntegers(getGroupIdsInPathProfile(profileId,
                                                                                  pathId));
        // go though each group, if groupIds does not match any of them, then
        // the group must not be added, so we add it
        for (int j = 0; j < allGroups.size(); j++) {
            boolean add = true;
            for (int i = 0; i < groupIds.length; i++) {
                if (allGroups.get(j).getId() == groupIds[i]) {
                    add = false;
                }
            }
            if (add) {
                groupsNotInProfile.add(allGroups.get(j));
            }
        }
        return groupsNotInProfile;
    }

    /**
     * Removes a group from a path/profile combination
     *
     * @param group_id ID of group
     * @param pathId ID of path
     * @param profileId ID of profile
     */
    public void removeGroupFromPathProfile(int group_id, int pathId, int profileId) {
        int[] groupIds = Utils.arrayFromStringOfIntegers(getGroupIdsInPathProfile(profileId,
                                                                                  pathId));
        String newGroupIds = "";
        for (int i = 0; i < groupIds.length; i++) {
            if (groupIds[i] != group_id) {
                newGroupIds += (groupIds[i] + ",");
            }
        }
        EditService.updatePathTable(Constants.PATH_PROFILE_GROUP_IDS, newGroupIds, pathId);
    }

    /**
     * Updates the path_order column in the table, loops though the pathOrder array, and changes the value to the loop
     * index+1 for the specified pathId
     *
     * @param profileId ID of profile
     * @param pathOrder array containing new order of paths
     */
    public void updatePathOrder(int profileId, int[] pathOrder) {
        for (int i = 0; i < pathOrder.length; i++) {
            EditService.updatePathTable(Constants.PATH_PROFILE_PATH_ORDER, (i + 1), pathOrder[i]);
        }
    }

    /**
     * Get path ID for a given profileId and pathName
     *
     * @param pathName Name of path
     * @param profileId ID of profile
     * @return ID of path
     */
    public int getPathId(String pathName, int profileId) {
        PreparedStatement queryStatement = null;
        ResultSet results = null;
        // first get the pathId for the pathName/profileId
        int pathId = -1;
        try (Connection sqlConnection = sqlService.getConnection()) {
            queryStatement = sqlConnection.prepareStatement(
                "SELECT " + Constants.GENERIC_ID + " FROM " + Constants.DB_TABLE_PATH
                    + " WHERE " + Constants.PATH_PROFILE_PATHNAME + "= ? "
                    + " AND " + Constants.GENERIC_PROFILE_ID + "= ?"
            );
            queryStatement.setString(1, pathName);
            queryStatement.setInt(2, profileId);
            results = queryStatement.executeQuery();
            if (results.next()) {
                pathId = results.getInt(Constants.GENERIC_ID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) {
                    queryStatement.close();
                }
            } catch (Exception e) {
            }
        }

        return pathId;
    }

    /**
     * Returns information for a specific path id in the default profile
     *
     * @param pathId ID of Path
     * @return EndpointOverride
     * @throws Exception exception
     */
    public EndpointOverride getPath(int pathId) throws Exception {
        return getPath(pathId, Constants.PROFILE_CLIENT_DEFAULT_ID, null);
    }

    /**
     * Method meant to get custom data from the Request/Response table based on pathId, clientUUID and type of data
     *
     * @param pathId ID of path
     * @param clientUUID UUID of client
     * @param type Type of override
     * @return Value of custom data
     * @throws Exception exception
     */
    public String getCustomData(int pathId, String clientUUID, String type) throws Exception {
        PreparedStatement statement = null;
        ResultSet results = null;
        String data = "";

        try (Connection sqlConnection = sqlService.getConnection()) {
            String queryString = "SELECT " + type + " FROM " + Constants.DB_TABLE_REQUEST_RESPONSE +
                " WHERE " + Constants.REQUEST_RESPONSE_PATH_ID + " = ? " +
                " AND " + Constants.GENERIC_CLIENT_UUID + " = ?";
            statement = sqlConnection.prepareStatement(queryString);
            statement.setInt(1, pathId);
            statement.setString(2, clientUUID);

            results = statement.executeQuery();
            if (results.next()) {
                data = results.getString(type);
            }
            results.close();
            statement.close();
        } catch (Exception e) {

        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }

        return data;
    }

    /**
     * Generate a path select string
     *
     * @return Select query string
     */
    private String getPathSelectString() {
        String queryString = "SELECT " + Constants.DB_TABLE_REQUEST_RESPONSE + "." + Constants.GENERIC_CLIENT_UUID +
            "," + Constants.DB_TABLE_PATH + "." + Constants.GENERIC_ID +
            "," + Constants.PATH_PROFILE_PATHNAME +
            "," + Constants.PATH_PROFILE_ACTUAL_PATH +
            "," + Constants.PATH_PROFILE_BODY_FILTER +
            "," + Constants.PATH_PROFILE_GROUP_IDS +
            "," + Constants.DB_TABLE_PATH + "." + Constants.PATH_PROFILE_PROFILE_ID +
            "," + Constants.PATH_PROFILE_PATH_ORDER +
            "," + Constants.REQUEST_RESPONSE_REPEAT_NUMBER +
            "," + Constants.REQUEST_RESPONSE_REQUEST_ENABLED +
            "," + Constants.REQUEST_RESPONSE_RESPONSE_ENABLED +
            "," + Constants.PATH_PROFILE_CONTENT_TYPE +
            "," + Constants.PATH_PROFILE_REQUEST_TYPE +
            "," + Constants.PATH_PROFILE_GLOBAL +
            " FROM " + Constants.DB_TABLE_PATH +
            " JOIN " + Constants.DB_TABLE_REQUEST_RESPONSE +
            " ON " + Constants.DB_TABLE_PATH + "." + Constants.GENERIC_ID +
            "=" + Constants.DB_TABLE_REQUEST_RESPONSE + "." + Constants.REQUEST_RESPONSE_PATH_ID +
            " AND " + Constants.DB_TABLE_REQUEST_RESPONSE + "." + Constants.GENERIC_CLIENT_UUID + " = ?";

        return queryString;
    }

    /**
     * Turn a resultset into EndpointOverride
     *
     * @param results results containing relevant information
     * @return EndpointOverride
     * @throws Exception exception
     */
    private EndpointOverride getEndpointOverrideFromResultSet(ResultSet results) throws Exception {
        EndpointOverride endpoint = new EndpointOverride();
        endpoint.setPathId(results.getInt(Constants.GENERIC_ID));
        endpoint.setPath(results.getString(Constants.PATH_PROFILE_ACTUAL_PATH));
        endpoint.setBodyFilter(results.getString(Constants.PATH_PROFILE_BODY_FILTER));
        endpoint.setPathName(results.getString(Constants.PATH_PROFILE_PATHNAME));
        endpoint.setContentType(results.getString(Constants.PATH_PROFILE_CONTENT_TYPE));
        endpoint.setRequestType(results.getInt(Constants.PATH_PROFILE_REQUEST_TYPE));
        endpoint.setRepeatNumber(results.getInt(Constants.REQUEST_RESPONSE_REPEAT_NUMBER));
        endpoint.setGroupIds(results.getString(Constants.PATH_PROFILE_GROUP_IDS));
        endpoint.setRequestEnabled(results.getBoolean(Constants.REQUEST_RESPONSE_REQUEST_ENABLED));
        endpoint.setResponseEnabled(results.getBoolean(Constants.REQUEST_RESPONSE_RESPONSE_ENABLED));
        endpoint.setClientUUID(results.getString(Constants.GENERIC_CLIENT_UUID));
        endpoint.setProfileId(results.getInt(Constants.GENERIC_PROFILE_ID));
        endpoint.setGlobal(results.getBoolean(Constants.PATH_PROFILE_GLOBAL));
        return endpoint;
    }

    /**
     * Returns information for a specific path id
     *
     * @param pathId ID of path
     * @param clientUUID client UUID
     * @param filters filters to set on endpoint
     * @return EndpointOverride
     * @throws Exception exception
     */
    public EndpointOverride getPath(int pathId, String clientUUID, String[] filters) throws Exception {
        EndpointOverride endpoint = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            String queryString = this.getPathSelectString();
            queryString += " AND " + Constants.DB_TABLE_PATH + "." + Constants.GENERIC_ID + "=" + pathId + ";";
            statement = sqlConnection.prepareStatement(queryString);
            statement.setString(1, clientUUID);

            results = statement.executeQuery();

            if (results.next()) {
                endpoint = this.getEndpointOverrideFromResultSet(results);
                endpoint.setFilters(filters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }

        return endpoint;
    }

    /**
     * Sets the path name for this ID
     *
     * @param pathId ID of path
     * @param pathName Name of path
     */
    public void setName(int pathId, String pathName) {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_PATH +
                    " SET " + Constants.PATH_PROFILE_PATHNAME + " = ?" +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, pathName);
            statement.setInt(2, pathId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the actual path for this ID
     *
     * @param pathId ID of path
     * @param path value of path
     */
    public void setPath(int pathId, String path) {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_PATH +
                    " SET " + Constants.PATH_PROFILE_ACTUAL_PATH + " = ? " +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, path);
            statement.setInt(2, pathId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the body filter for this ID
     *
     * @param pathId ID of path
     * @param bodyFilter Body filter to set
     */
    public void setBodyFilter(int pathId, String bodyFilter) {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_PATH +
                    " SET " + Constants.PATH_PROFILE_BODY_FILTER + " = ? " +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, bodyFilter);
            statement.setInt(2, pathId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the content type for this ID
     *
     * @param pathId ID of path
     * @param contentType content type value
     */
    public void setContentType(int pathId, String contentType) {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_PATH +
                    " SET " + Constants.PATH_PROFILE_CONTENT_TYPE + " = ? " +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, contentType);
            statement.setInt(2, pathId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the request type for this ID.  Defaults to GET
     *
     * @param pathId ID of path
     * @param requestType type of request to service
     */
    public void setRequestType(int pathId, Integer requestType) {
        if (requestType == null) {
            requestType = Constants.REQUEST_TYPE_GET;
        }
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_PATH +
                    " SET " + Constants.PATH_PROFILE_REQUEST_TYPE + " = ?" +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setInt(1, requestType);
            statement.setInt(2, pathId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the global setting for this ID
     *
     * @param pathId ID of path
     * @param global True if global, False otherwise
     */
    public void setGlobal(int pathId, Boolean global) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_PATH +
                    " SET " + Constants.PATH_PROFILE_GLOBAL + " = ? " +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setBoolean(1, global);
            statement.setInt(2, pathId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns an array of all endpoints
     *
     * @param profileId ID of profile
     * @param clientUUID UUID of client
     * @param filters filters to apply to endpoints
     * @return Collection of endpoints
     * @throws Exception exception
     */
    public List<EndpointOverride> getPaths(int profileId, String clientUUID, String[] filters) throws Exception {
        ArrayList<EndpointOverride> properties = new ArrayList<EndpointOverride>();
        PreparedStatement statement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            String queryString = this.getPathSelectString();
            queryString += " AND " + Constants.DB_TABLE_PATH + "." + Constants.GENERIC_PROFILE_ID + "=? " +
                " ORDER BY " + Constants.PATH_PROFILE_PATH_ORDER + " ASC";

            statement = sqlConnection.prepareStatement(queryString);
            statement.setString(1, clientUUID);
            statement.setInt(2, profileId);

            results = statement.executeQuery();
            while (results.next()) {
                EndpointOverride endpoint = this.getEndpointOverrideFromResultSet(results);
                endpoint.setFilters(filters);
                properties.add(endpoint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }

        return properties;
    }

    /**
     * Sets enabled/disabled for a response
     *
     * @param pathId ID of path
     * @param enabled 1 for enabled, 0 for disabled
     * @param clientUUID client ID
     * @throws Exception exception
     */
    public void setResponseEnabled(int pathId, boolean enabled, String clientUUID) throws Exception {
        PreparedStatement statement = null;
        int profileId = EditService.getProfileIdFromPathID(pathId);
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " SET " + Constants.DB_TABLE_PATH_RESPONSE_ENABLED + "= ?" +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + "= ?" +
                    " AND " + Constants.GENERIC_CLIENT_UUID + "= ?" +
                    " AND " + Constants.REQUEST_RESPONSE_PATH_ID + "= ?"
            );
            statement.setBoolean(1, enabled);
            statement.setInt(2, profileId);
            statement.setString(3, clientUUID);
            statement.setInt(4, pathId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Enable/disable a request
     *
     * @param pathId ID of path
     * @param enabled True for enabled, False for disabled
     * @param clientUUID UUID of client
     */
    public void setRequestEnabled(int pathId, boolean enabled, String clientUUID) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            int profileId = EditService.getProfileIdFromPathID(pathId);
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " SET " + Constants.DB_TABLE_PATH_REQUEST_ENABLED + "= ?" +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + "= ?" +
                    " AND " + Constants.GENERIC_CLIENT_UUID + "= ?" +
                    " AND " + Constants.REQUEST_RESPONSE_PATH_ID + "= ?"
            );
            statement.setBoolean(1, enabled);
            statement.setInt(2, profileId);
            statement.setString(3, clientUUID);
            statement.setInt(4, pathId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Set the value for a custom response
     *
     * @param pathId ID of path
     * @param customResponse value of custom response
     * @param clientUUID UUID of client
     */
    public void setCustomResponse(int pathId, String customResponse, String clientUUID) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            int profileId = EditService.getProfileIdFromPathID(pathId);
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " SET " + Constants.REQUEST_RESPONSE_CUSTOM_RESPONSE + "= ?" +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + "= ?" +
                    " AND " + Constants.GENERIC_CLIENT_UUID + "= ?" +
                    " AND " + Constants.REQUEST_RESPONSE_PATH_ID + "= ?"
            );
            statement.setString(1, customResponse);
            statement.setInt(2, profileId);
            statement.setString(3, clientUUID);
            statement.setInt(4, pathId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Set the value for a custom request
     *
     * @param pathId ID of path
     * @param customRequest value of custom request
     * @param clientUUID UUID of client
     */
    public void setCustomRequest(int pathId, String customRequest, String clientUUID) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            int profileId = EditService.getProfileIdFromPathID(pathId);
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " SET " + Constants.REQUEST_RESPONSE_CUSTOM_REQUEST + "= ?" +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + "= ?" +
                    " AND " + Constants.GENERIC_CLIENT_UUID + "= ?" +
                    " AND " + Constants.REQUEST_RESPONSE_PATH_ID + "= ?"
            );
            statement.setString(1, customRequest);
            statement.setInt(2, profileId);
            statement.setString(3, clientUUID);
            statement.setInt(4, pathId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Clear all overrides, reset repeat counts for a response path
     *
     * @param pathId ID of path
     * @param clientUUID UUID of client
     * @throws Exception exception
     */
    public void clearResponseSettings(int pathId, String clientUUID) throws Exception {
        logger.info("clearing response settings");
        this.setResponseEnabled(pathId, false, clientUUID);
        OverrideService.getInstance().disableAllOverrides(pathId, clientUUID, Constants.OVERRIDE_TYPE_RESPONSE);
        EditService.getInstance().updateRepeatNumber(Constants.OVERRIDE_TYPE_RESPONSE, pathId, clientUUID);
    }

    /**
     * Clear all overrides, reset repeat counts for a request path
     *
     * @param pathId ID of path
     * @param clientUUID UUID of client
     * @throws Exception exception
     */
    public void clearRequestSettings(int pathId, String clientUUID) throws Exception {
        this.setRequestEnabled(pathId, false, clientUUID);
        OverrideService.getInstance().disableAllOverrides(pathId, clientUUID, Constants.OVERRIDE_TYPE_REQUEST);
        EditService.getInstance().updateRepeatNumber(Constants.OVERRIDE_TYPE_REQUEST, pathId, clientUUID);
    }

    /**
     * Obtain matching paths for a request
     *
     * @param overrideType type of override
     * @param client Client
     * @param profile Profile
     * @param uri URI
     * @param requestType type of request
     * @param pathTest If true this will also match disabled paths
     * @return Collection of matching endpoints
     * @throws Exception exception
     */
    public List<EndpointOverride> getSelectedPaths(int overrideType, Client client, Profile profile, String uri,
                                                   Integer requestType, boolean pathTest) throws Exception {
        List<EndpointOverride> selectPaths = new ArrayList<EndpointOverride>();

        // get the paths for the current active client profile
        // this returns paths in priority order
        List<EndpointOverride> paths = new ArrayList<EndpointOverride>();

        if (client.getIsActive()) {
            paths = getPaths(
                profile.getId(),
                client.getUUID(), null);
        }

        boolean foundRealPath = false;
        logger.info("Checking uri: {}", uri);

        // it should now be ordered by priority, i updated tableOverrides to
        // return the paths in priority order
        for (EndpointOverride path : paths) {
            // first see if the request types match..
            // and if the path request type is not ALL
            // if they do not then skip this path
            // If requestType is -1 we evaluate all(probably called by the path tester)
            if (requestType != -1 && path.getRequestType() != requestType && path.getRequestType() != Constants.REQUEST_TYPE_ALL) {
                continue;
            }

            // first see if we get a match
            try {
                Pattern pattern = Pattern.compile(path.getPath());
                Matcher matcher = pattern.matcher(uri);

                // we won't select the path if there aren't any enabled endpoints in it
                // this works since the paths are returned in priority order
                if (matcher.find()) {
                    // now see if this path has anything enabled in it
                    // Only go into the if:
                    // 1. There are enabled items in this path
                    // 2. Caller was looking for ResponseOverride and Response is enabled OR looking for RequestOverride
                    // 3. If pathTest is true then the rest of the conditions are not evaluated.  The path tester ignores enabled states so everything is returned.
                    // and request is enabled
                    if (pathTest ||
                        (path.getEnabledEndpoints().size() > 0 &&
                            ((overrideType == Constants.OVERRIDE_TYPE_RESPONSE && path.getResponseEnabled()) ||
                                (overrideType == Constants.OVERRIDE_TYPE_REQUEST && path.getRequestEnabled())))) {
                        // if we haven't already seen a non global path
                        // or if this is a global path
                        // then add it to the list
                        if (!foundRealPath || path.getGlobal()) {
                            selectPaths.add(path);
                        }
                    }

                    // we set this no matter what if a path matched and it was not the global path
                    // this stops us from adding further non global matches to the list
                    if (!path.getGlobal()) {
                        foundRealPath = true;
                    }
                }
            } catch (PatternSyntaxException pse) {
                // nothing to do but keep iterating over the list
                // this indicates an invalid regex
            }
        }

        return selectPaths;
    }
}
