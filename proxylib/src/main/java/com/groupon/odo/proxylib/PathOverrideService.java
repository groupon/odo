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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
     * @return
     */
    public List<Group> findAllGroups() {
        ArrayList<Group> allGroups = new ArrayList<Group>();
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;
        try {
            sqlConnection = sqlService.getConnection();
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) queryStatement.close();
            } catch (Exception e) {
            }
        }

        return allGroups;
    }

    /**
     * Add a path to a profile, returns the id
     *
     * @param id
     * @param pathname
     * @param actualPath
     * @return
     * @throws Exception
     */
    public int addPathnameToProfile(int id, String pathname, String actualPath) throws Exception {
        int pathOrder = getPathOrder(id).size() + 1;
        int pathId = -1;
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (statement != null) statement.close();
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
     * @param profileId
     * @param clientUUID
     * @param pathId
     * @throws Exception
     */
    public void addPathToRequestResponseTable(int profileId, String clientUUID, int pathId) throws Exception {
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        try {
            sqlConnection = sqlService.getConnection();

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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Return collection of path Ids in priority order
     *
     * @param profileId
     * @return
     */
    public List<Integer> getPathOrder(int profileId) {
        ArrayList<Integer> pathOrder = new ArrayList<Integer>();
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) queryStatement.close();
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
     * @param profileId
     * @param pathId
     * @param groupNum
     */
    public void AddGroupByNumber(int profileId, int pathId, int groupNum) {
        logger.info("adding group_id={}, to pathId={}", groupNum, pathId);
        String oldGroups = getGroupIdsInPathProfile(profileId, pathId);
        // make sure the old groups does not contain the current group we want
        // to add
        if (!intArrayContains(Utils.arrayFromStringOfIntegers(oldGroups), groupNum)) {
            if (!oldGroups.endsWith(",") && !oldGroups.isEmpty())
                oldGroups += ",";
            String newGroups = (oldGroups + groupNum);
            EditService.updatePathTable(Constants.PATH_PROFILE_GROUP_IDS, newGroups, pathId);
        } else {
            logger.info("that group is already contained in for this uuid/path");
        }
    }


    /**
     * Set the groups assigned to a path
     *
     * @param groups
     * @param pathId
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
     * @param array
     * @param numToCheck
     * @return
     */
    public static boolean intArrayContains(int[] array, int numToCheck) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == numToCheck)
                return true;
        }
        return false;
    }

    /**
     * First we get the oldGroups by looking at the database to find the path/profile match
     *
     * @param profileId
     * @param pathId
     * @return
     */
    public String getGroupIdsInPathProfile(int profileId, int pathId) {
        return (String) sqlService.getFromTable(Constants.PATH_PROFILE_GROUP_IDS, Constants.GENERIC_ID, pathId,
                Constants.DB_TABLE_PATH);
    }

    /**
     * When passed in the name of the group, this creates a new entry for it in the table
     * Afterwards, it gets the groupId for other purposes (ie to create overrides that correspond to this specific group)
     *
     * @param nameOfGroup
     * @return
     */
    public Integer addGroup(String nameOfGroup) {
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            PreparedStatement statement = sqlConnection.prepareStatement(
                    "INSERT INTO " + Constants.DB_TABLE_GROUPS
                            + "(" + Constants.GROUPS_GROUP_NAME + ")"
                            + " VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, nameOfGroup);
            statement.executeUpdate();

            // execute statement and get resultSet which will have the generated path ID as the first field
            results = statement.getGeneratedKeys();
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) queryStatement.close();
            } catch (Exception e) {
            }
        }

        return null;
    }

    /**
     * given the groupName, it returns the groupId
     *
     * @param groupName
     * @return
     */
    public Integer getGroupIdFromName(String groupName) {
        return (Integer) sqlService.getFromTable(Constants.GENERIC_ID, Constants.GROUPS_GROUP_NAME, groupName,
                Constants.DB_TABLE_GROUPS);
    }

    /**
     * given the groupId, and 2 string arrays, adds the name-responses pair to the table_override
     *
     * @param groupId
     * @param methodName
     * @param className
     */
    public void createOverride(int groupId, String methodName, String className) {
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
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
        } finally {
            try {
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) queryStatement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * given the groupId, returns the groupName
     *
     * @param groupId
     * @return
     */
    public String getGroupNameFromId(int groupId) {
        return (String) sqlService.getFromTable(Constants.GROUPS_GROUP_NAME, Constants.GENERIC_ID, groupId,
                Constants.DB_TABLE_GROUPS);
    }

    /**
     * updates the groupname in the table given the id
     *
     * @param newGroupName
     * @param id
     */
    public void updateGroupName(String newGroupName, int id) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Remove the group and all references to it
     *
     * @param groupId
     */
    public void removeGroup(int groupId) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();

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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
        removeGroupIdFromTablePaths(groupId);
    }

    /**
     * Remove all references to a groupId
     *
     * @param groupIdToRemove
     */
    private void removeGroupIdFromTablePaths(int groupIdToRemove) {
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();

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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) queryStatement.close();
            } catch (Exception e) {
            }
            try {
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Removes an override from the overrideTable as well as the enabled
     * overrides (cant be enabled if its removed...)
     *
     * @param overrideId
     */
    public void removeOverride(int overrideId) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();

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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Remove a path
     *
     * @param pathId
     */
    public void removePath(int pathId) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();

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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns a method object for a path id.
     *
     * @param enabledId
     * @return
     */
    // TODO: make this be able to return all enabled methods for a path instead of just the first method
    public com.groupon.odo.proxylib.models.Method getMethodForEnabledId(int enabledId) {
        com.groupon.odo.proxylib.models.Method method = null;
        int override_id = 0;
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) queryStatement.close();
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
     * @param overrideId
     * @return
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
            Connection sqlConnection = null;
            PreparedStatement queryStatement = null;
            ResultSet results = null;
            try {
                sqlConnection = sqlService.getConnection();
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
                    if (results != null) results.close();
                } catch (Exception e) {
                }
                try {
                    if (queryStatement != null) queryStatement.close();
                } catch (Exception e) {
                }
            }

            // if method is still null then just return
            if (method == null)
                return method;

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
     * Given a id (for the profile) and a pathname, this returns all the groups (in the array<hash> format)
     * that are contained within that path/profile combination
     *
     * @param profileId
     * @param pathId
     * @return
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
                if (((Integer) allGroups.get(j).getId() == groupIds[i]))
                    groupsInProfile.add(allGroups.get(j));
            }
        }
        return groupsInProfile;
    }

    /**
     * Given a profileId (for the profile) and a pathname, this returns all the groups (in the array<hash> format)
     * that are NOT contained within that path/profile combination
     *
     * @param profileId
     * @param pathId
     * @return
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
                if (((Integer) allGroups.get(j).getId() == groupIds[i])) {
                    add = false;
                }
            }
            if (add)
                groupsNotInProfile.add(allGroups.get(j));
        }
        return groupsNotInProfile;
    }

    /**
     * Removes a group from a path/profile combination
     *
     * @param group_id
     * @param pathId
     * @param profileId
     */
    public void removeGroupFromPathProfile(int group_id, int pathId, int profileId) {
        int[] groupIds = Utils.arrayFromStringOfIntegers(getGroupIdsInPathProfile(profileId,
                pathId));
        String newGroupIds = "";
        for (int i = 0; i < groupIds.length; i++) {
            if (groupIds[i] != group_id)
                newGroupIds += (groupIds[i] + ",");
        }
        EditService.updatePathTable(Constants.PATH_PROFILE_GROUP_IDS, newGroupIds, pathId);
    }

    /**
     * Updates the path_order column in the table, loops though the pathOrder array, and changes the value to the loop
     * index+1 for the specified pathId
     *
     * @param profileId
     * @param pathOrder
     */
    public void updatePathOrder(int profileId, int[] pathOrder) {
        for (int i = 0; i < pathOrder.length; i++) {
            EditService.updatePathTable(Constants.PATH_PROFILE_PATH_ORDER, (i + 1), pathOrder[i]);
        }
    }

    /**
     * Get path ID for a given profileId and pathName
     *
     * @param pathName
     * @param profileId
     * @return
     */
    public int getPathId(String pathName, int profileId) {
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;
        // first get the pathId for the pathName/profileId
        int pathId = -1;
        try {
            sqlConnection = sqlService.getConnection();
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (queryStatement != null) queryStatement.close();
            } catch (Exception e) {
            }
        }

        return pathId;
    }

    /**
     * Returns information for a specific path id in the default profile
     *
     * @param pathId
     * @return
     * @throws Exception
     */
    public EndpointOverride getPath(int pathId) throws Exception {
        return getPath(pathId, Constants.PROFILE_CLIENT_DEFAULT_ID, null);
    }

    /**
     * Method meant to get custom data from the Request/Response table based on pathId, clientUUID and type of data
     *
     * @param pathId
     * @param clientUUID
     * @param type
     * @return
     * @throws Exception
     */
    public String getCustomData(int pathId, String clientUUID, String type) throws Exception {
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String data = "";

        try {
            String queryString = "SELECT " + type + " FROM " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " WHERE " + Constants.REQUEST_RESPONSE_PATH_ID + " = ? " +
                    " AND " + Constants.GENERIC_CLIENT_UUID + " = ?";
            statement = sqlService.getConnection().prepareStatement(queryString);
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }

        return data;
    }

    /**
     * Generate a path select string
     *
     * @return
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
                " FROM " + Constants.DB_TABLE_REQUEST_RESPONSE +
                " JOIN " + Constants.DB_TABLE_PATH +
                " ON " + Constants.DB_TABLE_PATH + "." + Constants.GENERIC_ID +
                "=" + Constants.DB_TABLE_REQUEST_RESPONSE + "." + Constants.REQUEST_RESPONSE_PATH_ID +
                " AND " + Constants.DB_TABLE_REQUEST_RESPONSE + "." + Constants.GENERIC_CLIENT_UUID + " = ?";

        return queryString;
    }

    /**
     * Turn a resultset into EndpointOverride
     *
     * @param results
     * @return
     * @throws Exception
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
     * @param pathId
     * @return
     */
    public EndpointOverride getPath(int pathId, String clientUUID, String[] filters) throws Exception {
        EndpointOverride endpoint = null;
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            String queryString = this.getPathSelectString();
            queryString += " AND " + Constants.DB_TABLE_PATH + "." + Constants.GENERIC_ID + "=" + pathId + ";";
            sqlConnection = sqlService.getConnection();
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }

        return endpoint;
    }

    /**
     * Sets the path name for this ID
     *
     * @param pathId
     * @param pathName
     */
    public void setName(int pathId, String pathName) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlService.getConnection().prepareStatement(
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the actual path for this ID
     *
     * @param pathId
     * @param path
     */
    public void setPath(int pathId, String path) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        try {
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the body filter for this ID
     *
     * @param pathId
     * @param bodyFilter
     */
    public void setBodyFilter(int pathId, String bodyFilter) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        try {
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the content type for this ID
     *
     * @param pathId
     * @param contentType
     */
    public void setContentType(int pathId, String contentType) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        try {
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the request type for this ID.  Defaults to GET
     *
     * @param pathId
     * @param requestType
     */
    public void setRequestType(int pathId, Integer requestType) {
        if (requestType == null)
            requestType = Constants.REQUEST_TYPE_GET;
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Sets the global setting for this ID
     *
     * @param pathId
     * @param global
     */
    public void setGlobal(int pathId, Boolean global) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns an array of all endpoints
     *
     * @param profileId
     * @param clientUUID
     * @param filters
     * @return
     * @throws Exception
     */
    // TODO: enable path filtering
    public List<EndpointOverride> getPaths(int profileId, String clientUUID, String[] filters) throws Exception {
        ArrayList<EndpointOverride> properties = new ArrayList<EndpointOverride>();
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            String queryString = this.getPathSelectString();
            queryString += " AND " + Constants.DB_TABLE_PATH + "." + Constants.GENERIC_PROFILE_ID + "=? " +
                    " ORDER BY " + Constants.PATH_PROFILE_PATH_ORDER + " ASC";
            sqlConnection = sqlService.getConnection();
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
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }

        return properties;

    }

    /**
     * Sets enabled/disabled for a response
     *
     * @param pathId
     * @param enabled - 1 for enabled, 0 for disabled
     */
    public void setResponseEnabled(int pathId, boolean enabled, String clientUUID) throws Exception {
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        int profileId = EditService.getProfileIdFromPathID(pathId);
        try {
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Enable/disable a request
     *
     * @param pathId
     * @param enabled
     * @param clientUUID
     */
    public void setRequestEnabled(int pathId, boolean enabled, String clientUUID) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            int profileId = EditService.getProfileIdFromPathID(pathId);
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Set the value for a custom response
     *
     * @param pathId
     * @param customResponse
     * @param clientUUID
     */
    public void setCustomResponse(int pathId, String customResponse, String clientUUID) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            int profileId = EditService.getProfileIdFromPathID(pathId);
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Set the value for a custom request
     *
     * @param pathId
     * @param customRequest
     * @param clientUUID
     */
    public void setCustomRequest(int pathId, String customRequest, String clientUUID) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            int profileId = EditService.getProfileIdFromPathID(pathId);
            sqlConnection = sqlService.getConnection();
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
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Clear all overrides, reset repeat counts for a response path
     *
     * @param pathId
     * @param clientUUID
     * @throws Exception
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
     * @param pathId
     * @param clientUUID
     * @throws Exception
     */
    public void clearRequestSettings(int pathId, String clientUUID) throws Exception {
        this.setRequestEnabled(pathId, false, clientUUID);
        OverrideService.getInstance().disableAllOverrides(pathId, clientUUID, Constants.OVERRIDE_TYPE_REQUEST);
        EditService.getInstance().updateRepeatNumber(Constants.OVERRIDE_TYPE_REQUEST, pathId, clientUUID);
    }
}
