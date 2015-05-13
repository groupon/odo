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

import com.groupon.odo.proxylib.models.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditService {

    private static final Logger logger = LoggerFactory.getLogger(EditService.class);

    private static EditService serviceInstance = null;
    private static SQLService sqlService = null;

    public EditService() {

    }

    public static EditService getInstance() {
        if (serviceInstance == null) {
            serviceInstance = new EditService();
            try {
                sqlService = SQLService.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("ran jdbc-edit(4 tables) stuff");
        }
        return serviceInstance;
    }

    /**
     * Return all methods for a list of groupIds
     *
     * @param groupIds array of group IDs
     * @param filters array of filters to apply to method selection
     * @return collection of Methods found
     * @throws Exception exception
     */
    public List<Method> getMethodsFromGroupIds(int[] groupIds, String[] filters) throws Exception {
        ArrayList<Method> methods = new ArrayList<Method>();

        for (int groupId : groupIds) {
            methods.addAll(getMethodsFromGroupId(groupId, filters));
        }

        return methods;
    }

    /**
     * Set all repeat counts to unlimited (-1) for a client
     *
     * @param profileId profile ID of the client
     * @param client_uuid UUID of the client
     */
    public void makeAllRepeatUnlimited(int profileId, String client_uuid) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " SET " + Constants.REQUEST_RESPONSE_REPEAT_NUMBER + " = ?" +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + " = ?" +
                    " AND " + Constants.GENERIC_CLIENT_UUID + " = ?"
            );
            statement.setInt(1, -1);
            statement.setInt(2, profileId);
            statement.setString(3, client_uuid);
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
     * Update the repeat number for a client path
     *
     * @param newNum new repeat number of the path
     * @param path_id ID of the path
     * @param client_uuid UUID of the client
     * @throws Exception exception
     */
    public void updateRepeatNumber(int newNum, int path_id, String client_uuid) throws Exception {
        updateRequestResponseTables("repeat_number", newNum, getProfileIdFromPathID(path_id), client_uuid, path_id);
    }

    /**
     * Delete all enabled overrides for a client
     *
     * @param profileId profile ID of teh client
     * @param client_uuid UUID of teh client
     */
    public void disableAll(int profileId, String client_uuid) {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
                    " WHERE " + Constants.CLIENT_PROFILE_ID + " = ?" +
                    " AND " + Constants.CLIENT_CLIENT_UUID + " =? "
            );
            statement.setInt(1, profileId);
            statement.setString(2, client_uuid);
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
     * Remove a path from a profile
     *
     * @param path_id path ID to remove
     * @param profileId profile ID to remove path from
     */
    public void removePathnameFromProfile(int path_id, int profileId) {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
                    " WHERE " + Constants.ENABLED_OVERRIDES_PATH_ID + " = ?"
            );
            statement.setInt(1, path_id);
            statement.executeUpdate();
            statement.close();

            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_PATH +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setInt(1, path_id);
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
     * Returns all methods for a specific group
     *
     * @param groupId group ID to remove methods from
     * @param filters array of method types to filter by, null means no filter
     * @return Collection of methods found
     * @throws Exception exception
     */
    public List<Method> getMethodsFromGroupId(int groupId, String[] filters) throws Exception {
        ArrayList<Method> methods = new ArrayList<Method>();
        PreparedStatement statement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "SELECT * FROM " + Constants.DB_TABLE_OVERRIDE +
                    " WHERE " + Constants.OVERRIDE_GROUP_ID + " = ?"
            );
            statement.setInt(1, groupId);
            results = statement.executeQuery();
            while (results.next()) {
                Method method = PathOverrideService.getInstance().getMethodForOverrideId(results.getInt("id"));
                if (method == null) {
                    continue;
                }

                // decide whether or not to add this method based on the filters
                boolean add = true;
                if (filters != null) {
                    add = false;
                    for (String filter : filters) {
                        if (method.getMethodType().endsWith(filter)) {
                            add = true;
                            break;
                        }
                    }
                }

                if (add && !methods.contains(method)) {
                    methods.add(method);
                }
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

        return methods;
    }

    /**
     * Enable a custom response
     *
     * @param custom custom response
     * @param path_id path ID of the response
     * @param client_uuid client UUID
     * @throws Exception exception
     */
    public void enableCustomResponse(String custom, int path_id, String client_uuid) throws Exception {

        updateRequestResponseTables("custom_response", custom, getProfileIdFromPathID(path_id), client_uuid, path_id);
    }

    public static void updateRequestResponseTables(String columnName, Object newData, int profileId, String client_uuid, int path_id) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " SET " + columnName + " = ?" +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + "= ?" +
                    " AND " + Constants.GENERIC_CLIENT_UUID + "= ?" +
                    " AND " + Constants.REQUEST_RESPONSE_PATH_ID + "= ?"
            );
            statement.setObject(1, newData);
            statement.setInt(2, profileId);
            statement.setString(3, client_uuid);
            statement.setInt(4, path_id);
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
     * Updates a path table value for column columnName
     *
     * @param columnName name of the column to update
     * @param newData new content to set
     * @param path_id ID of the path to update
     */
    public static void updatePathTable(String columnName, Object newData, int path_id) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_PATH +
                    " SET " + columnName + " = ?" +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setObject(1, newData);
            statement.setInt(2, path_id);
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
     * Remove custom overrides
     *
     * @param path_id ID of path containing custom override
     * @param client_uuid UUID of the client
     * @throws Exception exception
     */
    public void removeCustomOverride(int path_id, String client_uuid) throws Exception {
        updateRequestResponseTables("custom_response", "", getProfileIdFromPathID(path_id), client_uuid, path_id);
    }

    /**
     * Return the profileId for a path
     *
     * @param path_id ID of path
     * @return ID of profile
     * @throws Exception exception
     */
    public static int getProfileIdFromPathID(int path_id) throws Exception {
        return (Integer) SQLService.getInstance().getFromTable(Constants.GENERIC_PROFILE_ID, Constants.GENERIC_ID, path_id, Constants.DB_TABLE_PATH);
    }
}
