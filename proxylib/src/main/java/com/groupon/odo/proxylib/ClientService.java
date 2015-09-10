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
import com.groupon.odo.proxylib.models.Profile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private static SQLService sqlService = null;
    private static ClientService serviceInstance = null;

    public ClientService() {

    }

    public static ClientService getInstance() {
        if (serviceInstance == null) {
            serviceInstance = new ClientService();
            try {
                sqlService = SQLService.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return serviceInstance;
    }

    /**
     * Return all Clients for a profile
     *
     * @param profileId ID of profile clients belong to
     * @return collection of the Clients found
     * @throws Exception exception
     */
    public List<Client> findAllClients(int profileId) throws Exception {
        ArrayList<Client> clients = new ArrayList<Client>();

        PreparedStatement query = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            query = sqlConnection.prepareStatement(
                "SELECT * FROM " + Constants.DB_TABLE_CLIENT +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + " = ?"
            );
            query.setInt(1, profileId);
            results = query.executeQuery();
            while (results.next()) {
                clients.add(this.getClientFromResultSet(results));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
            }
            try {
                if (query != null) {
                    query.close();
                }
            } catch (Exception e) {
            }
        }
        return clients;
    }

    /**
     * Returns a client object for a clientId
     *
     * @param clientId ID of client to return
     * @return Client or null
     * @throws Exception exception
     */
    public Client getClient(int clientId) throws Exception {
        Client client = null;

        PreparedStatement statement = null;
        ResultSet results = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            String queryString = "SELECT * FROM " + Constants.DB_TABLE_CLIENT +
                " WHERE " + Constants.GENERIC_ID + " = ?";

            statement = sqlConnection.prepareStatement(queryString);
            statement.setInt(1, clientId);

            results = statement.executeQuery();
            if (results.next()) {
                client = this.getClientFromResultSet(results);
            }
        } catch (Exception e) {
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

        return client;
    }

    /**
     * Returns a Client object for a clientUUID and profileId
     *
     * @param clientUUID UUID or friendlyName of client
     * @param profileId - can be null, safer if it is not null
     * @return Client object or null
     * @throws Exception exception
     */
    public Client findClient(String clientUUID, Integer profileId) throws Exception {
        Client client = null;
        /* ERROR CODE: 500 WHEN TRYING TO DELETE A SERVER GROUP.
            THIS APPEARS TO BE BECAUSE CLIENT UUID IS NULL.
         */
        /* CODE ADDED TO PREVENT NULL POINTERS. */
        if (clientUUID == null) {
            clientUUID = "";
        }

        // first see if the clientUUID is actually a uuid.. it might be a friendlyName and need conversion
        /* A UUID IS A UNIVERSALLY UNIQUE IDENTIFIER. */
        if (clientUUID.compareTo(Constants.PROFILE_CLIENT_DEFAULT_ID) != 0 &&
            !clientUUID.matches("[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}")) {
            Client tmpClient = this.findClientFromFriendlyName(profileId, clientUUID);

            // if we can't find a client then fall back to the default ID
            if (tmpClient == null) {
                clientUUID = Constants.PROFILE_CLIENT_DEFAULT_ID;
            } else {
                return tmpClient;
            }
        }

        PreparedStatement statement = null;
        ResultSet results = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            String queryString = "SELECT * FROM " + Constants.DB_TABLE_CLIENT +
                " WHERE " + Constants.CLIENT_CLIENT_UUID + " = ?";

            if (profileId != null) {
                queryString += " AND " + Constants.GENERIC_PROFILE_ID + "=?";
            }

            statement = sqlConnection.prepareStatement(queryString);
            statement.setString(1, clientUUID);

            if (profileId != null) {
                statement.setInt(2, profileId);
            }

            results = statement.executeQuery();
            if (results.next()) {
                client = this.getClientFromResultSet(results);
            }
        } catch (Exception e) {
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

        return client;
    }

    /**
     * Returns a client model from a ResultSet
     *
     * @param result resultset containing client information
     * @return Client or null
     * @throws Exception exception
     */
    private Client getClientFromResultSet(ResultSet result) throws Exception {
        Client client = new Client();
        client.setId(result.getInt(Constants.GENERIC_ID));
        client.setUUID(result.getString(Constants.CLIENT_CLIENT_UUID));
        client.setFriendlyName(result.getString(Constants.CLIENT_FRIENDLY_NAME));
        client.setProfile(ProfileService.getInstance().findProfile(result.getInt(Constants.GENERIC_PROFILE_ID)));
        client.setIsActive(result.getBoolean(Constants.CLIENT_IS_ACTIVE));
        client.setActiveServerGroup(result.getInt(Constants.CLIENT_ACTIVESERVERGROUP));
        return client;
    }

    private String getUniqueClientUUID() {
        String curClientUUID = UUID.randomUUID().toString();
        PreparedStatement statement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            while (true) {
                statement = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB_TABLE_CLIENT +
                                                               " WHERE " + Constants.GENERIC_CLIENT_UUID + " = ?");
                statement.setString(1, curClientUUID);
                results = statement.executeQuery();
                if (results.next()) {
                    curClientUUID = UUID.randomUUID().toString();
                } else {
                    break;
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
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }
        logger.info("ClientUUID of new client = {}", curClientUUID);
        return curClientUUID;
    }

    /**
     * Create a new client for profile
     * There is a limit of Constants.CLIENT_CLIENTS_PER_PROFILE_LIMIT
     * If this limit is reached an exception is thrown back to the caller
     *
     * @param profileId ID of profile to create a new client for
     * @return The newly created client
     * @throws Exception exception
     */
    public Client add(int profileId) throws Exception {
        Client client = null;
        ArrayList<Integer> pathsToCopy = new ArrayList<Integer>();
        String clientUUID = getUniqueClientUUID();

        // get profile for profileId
        Profile profile = ProfileService.getInstance().findProfile(profileId);
        PreparedStatement statement = null;
        ResultSet rs = null;

        try (Connection sqlConnection = sqlService.getConnection()) {

            // get the current count of clients
            statement = sqlConnection.prepareStatement("SELECT COUNT(" + Constants.GENERIC_ID + ") FROM " +
                                                           Constants.DB_TABLE_CLIENT + " WHERE " + Constants.GENERIC_PROFILE_ID + "=?");
            statement.setInt(1, profileId);
            int clientCount = -1;
            rs = statement.executeQuery();
            if (rs.next()) {
                clientCount = rs.getInt(1);
            }
            statement.close();
            rs.close();

            // check count
            if (clientCount == -1) {
                throw new Exception("Error querying clients for profileId=" + profileId);
            }
            if (clientCount >= Constants.CLIENT_CLIENTS_PER_PROFILE_LIMIT) {
                throw new Exception("Profile(" + profileId + ") already contains 50 clients.  Please remove clients before adding new ones.");
            }

            statement = sqlConnection.prepareStatement(
                "INSERT INTO " + Constants.DB_TABLE_CLIENT +
                    " (" + Constants.CLIENT_CLIENT_UUID + ", " +
                    Constants.CLIENT_IS_ACTIVE + ", " +
                    Constants.CLIENT_PROFILE_ID + ")" +
                    " VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, clientUUID);
            statement.setBoolean(2, false);
            statement.setInt(3, profile.getId());
            statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            int clientId = -1;
            if (rs.next()) {
                clientId = rs.getInt(1);
            } else {
                // something went wrong
                throw new Exception("Could not add client");
            }
            rs.close();
            statement.close();

            // adding entries into request response table for this new client for every path
            // basically a copy of what happens when a path gets created
            statement = sqlConnection.prepareStatement(
                "SELECT * FROM " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + " = ?" +
                    " AND " + Constants.GENERIC_CLIENT_UUID + " = ?"
            );
            statement.setInt(1, profile.getId());
            statement.setString(2, Constants.PROFILE_CLIENT_DEFAULT_ID);
            rs = statement.executeQuery();
            while (rs.next()) {
                // collect up the pathIds we need to copy
                pathsToCopy.add(rs.getInt(Constants.REQUEST_RESPONSE_PATH_ID));
            }
            client = new Client();
            client.setIsActive(false);
            client.setUUID(clientUUID);
            client.setId(clientId);
            client.setProfile(profile);
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
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

        // add all of the request response items
        for (Integer pathId : pathsToCopy) {
            PathOverrideService.getInstance().addPathToRequestResponseTable(profile.getId(), client.getUUID(), pathId);
        }

        return client;
    }

    /**
     * Set a friendly name for a client
     *
     * @param profileId profileId of the client
     * @param clientUUID UUID of the client
     * @param friendlyName friendly name of the client
     * @return return Client object or null
     * @throws Exception exception
     */
    public Client setFriendlyName(int profileId, String clientUUID, String friendlyName) throws Exception {
        // first see if this friendlyName is already in use
        Client client = this.findClientFromFriendlyName(profileId, friendlyName);
        if (client != null && !client.getUUID().equals(clientUUID)) {
            throw new Exception("Friendly name already in use");
        }

        PreparedStatement statement = null;
        int rowsAffected = 0;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_CLIENT +
                    " SET " + Constants.CLIENT_FRIENDLY_NAME + " = ?" +
                    " WHERE " + Constants.CLIENT_CLIENT_UUID + " = ?" +
                    " AND " + Constants.GENERIC_PROFILE_ID + " = ?"
            );
            statement.setString(1, friendlyName);
            statement.setString(2, clientUUID);
            statement.setInt(3, profileId);

            rowsAffected = statement.executeUpdate();
        } catch (Exception e) {

        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }

        if (rowsAffected == 0) {
            return null;
        }
        return this.findClient(clientUUID, profileId);
    }

    /**
     * Get the client for a profileId/friendlyName
     *
     * @param profileId profile ID of the client
     * @param friendlyName friendly name of the client
     * @return Client or null
     * @throws Exception exception
     */
    public Client findClientFromFriendlyName(int profileId, String friendlyName) throws Exception {
        Client client = null;

        // Don't even try if the friendlyName is null/empty
        if (friendlyName == null || friendlyName.compareTo("") == 0) {
            return client;
        }

        PreparedStatement statement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            String queryString = "SELECT * FROM " + Constants.DB_TABLE_CLIENT +
                " WHERE " + Constants.CLIENT_FRIENDLY_NAME + " = ?" +
                " AND " + Constants.GENERIC_PROFILE_ID + " = ?";
            statement = sqlConnection.prepareStatement(queryString);
            statement.setString(1, friendlyName);
            statement.setInt(2, profileId);

            results = statement.executeQuery();
            if (results.next()) {
                client = this.getClientFromResultSet(results);
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

        return client;
    }

    /**
     * Removes a client from the database
     * Also clears all additional override information for the clientId
     *
     * @param profileId profile ID of client to remove
     * @param clientUUID client UUID of client to remove
     * @throws Exception exception
     */
    public void remove(int profileId, String clientUUID) throws Exception {
        PreparedStatement statement = null;
        ResultSet results = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            // first try selecting the row we want to deal with
            statement = sqlConnection.prepareStatement(
                "SELECT * FROM " + Constants.DB_TABLE_CLIENT +
                    " WHERE " + Constants.GENERIC_CLIENT_UUID + " = ?" +
                    " AND " + Constants.CLIENT_PROFILE_ID + "= ?"
            );
            statement.setString(1, clientUUID);
            statement.setInt(2, profileId);
            results = statement.executeQuery();
            if (!results.next()) {
                throw new Exception("Could not find specified clientUUID: " + clientUUID);
            }
        } catch (Exception e) {
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

        // delete from the client table
        String queryString = "DELETE FROM " + Constants.DB_TABLE_CLIENT +
            " WHERE " + Constants.CLIENT_CLIENT_UUID + " = ? " +
            " AND " + Constants.CLIENT_PROFILE_ID + " = ?";

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(queryString);
            statement.setString(1, clientUUID);
            statement.setInt(2, profileId);

            logger.info("Query: {}", statement.toString());
            statement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
            }
        }

        // delete from other tables as appropriate
        // need to delete from enabled_overrides and request_response
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_REQUEST_RESPONSE +
                    " WHERE " + Constants.CLIENT_CLIENT_UUID + " = ? " +
                    " AND " + Constants.CLIENT_PROFILE_ID + " = ?"
            );
            statement.setString(1, clientUUID);
            statement.setInt(2, profileId);
            statement.executeUpdate();
        } catch (Exception e) {
            // ok to swallow this.. just means there wasn't any
        }

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
                    " WHERE " + Constants.CLIENT_CLIENT_UUID + " = ? " +
                    " AND " + Constants.CLIENT_PROFILE_ID + " = ?"
            );
            statement.setString(1, clientUUID);
            statement.setInt(2, profileId);
            statement.executeUpdate();
        } catch (Exception e) {
            // ok to swallow this.. just means there wasn't any
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
     * disables the current active id, enables the new one selected
     *
     * @param profileId profile ID of the client
     * @param clientUUID UUID of the client
     * @param active true to make client active, false to make client inactive
     * @throws Exception exception
     */
    public void updateActive(int profileId, String clientUUID, Boolean active) throws Exception {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "UPDATE " + Constants.DB_TABLE_CLIENT +
                    " SET " + Constants.CLIENT_IS_ACTIVE + "= ?" +
                    " WHERE " + Constants.GENERIC_CLIENT_UUID + "= ? " +
                    " AND " + Constants.GENERIC_PROFILE_ID + "= ?"
            );
            statement.setBoolean(1, active);
            statement.setString(2, clientUUID);
            statement.setInt(3, profileId);
            statement.executeUpdate();
        } catch (Exception e) {
            // ok to swallow this.. just means there wasn't any
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
     * Resets all override settings for the clientUUID and disables it
     *
     * @param profileId profile ID of the client
     * @param clientUUID UUID of the client
     * @throws Exception exception
     */
    public void reset(int profileId, String clientUUID) throws Exception {
        PreparedStatement statement = null;

        // TODO: need a better way to do this than brute force.. but the iterative approach is too slow
        try (Connection sqlConnection = sqlService.getConnection()) {

            // first remove all enabled overrides with this client uuid
            String queryString = "DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
                " WHERE " + Constants.GENERIC_CLIENT_UUID + "= ? " +
                " AND " + Constants.GENERIC_PROFILE_ID + " = ?";
            statement = sqlConnection.prepareStatement(queryString);
            statement.setString(1, clientUUID);
            statement.setInt(2, profileId);
            statement.executeUpdate();
            statement.close();

            // clean up request response table for this uuid
            queryString = "UPDATE " + Constants.DB_TABLE_REQUEST_RESPONSE +
                " SET " + Constants.REQUEST_RESPONSE_CUSTOM_REQUEST + "=?, "
                + Constants.REQUEST_RESPONSE_CUSTOM_RESPONSE + "=?, "
                + Constants.REQUEST_RESPONSE_REPEAT_NUMBER + "=-1, "
                + Constants.REQUEST_RESPONSE_REQUEST_ENABLED + "=0, "
                + Constants.REQUEST_RESPONSE_RESPONSE_ENABLED + "=0 "
                + "WHERE " + Constants.GENERIC_CLIENT_UUID + "=? " +
                " AND " + Constants.GENERIC_PROFILE_ID + "=?";
            statement = sqlConnection.prepareStatement(queryString);
            statement.setString(1, "");
            statement.setString(2, "");
            statement.setString(3, clientUUID);
            statement.setInt(4, profileId);
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

        this.updateActive(profileId, clientUUID, false);
    }

    public String getClientUUIDfromId(int id) {
        return (String) sqlService.getFromTable(Constants.CLIENT_CLIENT_UUID, Constants.GENERIC_ID, id, Constants.DB_TABLE_CLIENT);
    }

    public int getIdFromClientUUID(String uuid) {
        return (Integer) sqlService.getFromTable(Constants.GENERIC_ID, Constants.CLIENT_CLIENT_UUID, uuid, Constants.DB_TABLE_CLIENT);
    }

    //gets the profile_name associated with a specific id
    public String getProfileIdFromClientId(int id) {
        return (String) sqlService.getFromTable(Constants.CLIENT_PROFILE_ID, Constants.GENERIC_ID, id, Constants.DB_TABLE_CLIENT);
    }
}
