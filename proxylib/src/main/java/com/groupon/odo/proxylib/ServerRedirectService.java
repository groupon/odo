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
import com.groupon.odo.proxylib.models.ServerGroup;
import com.groupon.odo.proxylib.models.ServerRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerRedirectService {

    private static ServerRedirectService serviceInstance = null;
    private static final Logger logger = LoggerFactory.getLogger(ServerRedirectService.class);
    private static SQLService sqlService = null;

    public ServerRedirectService() {

    }

    /**
     * Obtain instance of ServerRedirectService
     *
     * @return
     */
    public static ServerRedirectService getInstance() {
        if (serviceInstance == null) {
            serviceInstance = new ServerRedirectService();
            try {
                sqlService = SQLService.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return serviceInstance;
    }

    /**
     * Get the server redirects for a given clientId from the database
     *
     * @param clientId
     * @return
     */
    public List<ServerRedirect> tableServers(int clientId) {
        List<ServerRedirect> servers = new ArrayList<ServerRedirect>();
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try {
            Client client = ClientService.getInstance().getClient(clientId);
            servers = tableServers(client.getProfile().getId(), client.getActiveServerGroup());
        } catch (SQLException e) {
            e.printStackTrace();
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
        return servers;
    }

    /**
     * Get the server redirects belonging to a server group
     *
     * @param profileId
     * @param serverGroupId
     * @return
     */
    public List<ServerRedirect> tableServers(int profileId, int serverGroupId) {
        ArrayList<ServerRedirect> servers = new ArrayList<ServerRedirect>();
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            queryStatement = sqlConnection.prepareStatement(
                    "SELECT * FROM " + Constants.DB_TABLE_SERVERS +
                            " WHERE " + Constants.GENERIC_PROFILE_ID + " = ?" +
                            " AND " + Constants.SERVER_REDIRECT_GROUP_ID + " = ?"
            );
            queryStatement.setInt(1, profileId);
            queryStatement.setInt(2, serverGroupId);
            results = queryStatement.executeQuery();
            while (results.next()) {
                ServerRedirect curServer = new ServerRedirect(results.getInt(Constants.GENERIC_ID),
                        results.getString(Constants.SERVER_REDIRECT_REGION),
                        results.getString(Constants.SERVER_REDIRECT_SRC_URL),
                        results.getString(Constants.SERVER_REDIRECT_DEST_URL),
                        results.getString(Constants.SERVER_REDIRECT_HOST_HEADER));
                curServer.setProfileId(profileId);
                servers.add(curServer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        return servers;
    }

    /**
     * Return all server groups for a profile
     *
     * @param profileId
     * @return
     */
    public List<ServerGroup> tableServerGroups(int profileId) {
        ArrayList<ServerGroup> serverGroups = new ArrayList<ServerGroup>();
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            queryStatement = sqlConnection.prepareStatement(
                    "SELECT * FROM " + Constants.DB_TABLE_SERVER_GROUPS +
                            " WHERE " + Constants.GENERIC_PROFILE_ID + " = ? " +
                            "ORDER BY " + Constants.GENERIC_NAME
            );
            queryStatement.setInt(1, profileId);
            results = queryStatement.executeQuery();
            while (results.next()) {
                ServerGroup curServerGroup = new ServerGroup(results.getInt(Constants.GENERIC_ID),
                        results.getString(Constants.GENERIC_NAME),
                        results.getInt(Constants.GENERIC_PROFILE_ID));
                curServerGroup.setServers(tableServers(profileId, curServerGroup.getId()));
                serverGroups.add(curServerGroup);
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
        return serverGroups;
    }

    /**
     * Returns redirect information for the given ID
     *
     * @param id
     * @return
     */
    public ServerRedirect getRedirect(int id) throws Exception {
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            queryStatement = sqlConnection.prepareStatement(
                    "SELECT * FROM " + Constants.DB_TABLE_SERVERS +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            queryStatement.setInt(1, id);
            results = queryStatement.executeQuery();
            if (results.next()) {
                ServerRedirect curServer = new ServerRedirect(results.getInt(Constants.GENERIC_ID),
                        results.getString(Constants.SERVER_REDIRECT_REGION),
                        results.getString(Constants.SERVER_REDIRECT_SRC_URL),
                        results.getString(Constants.SERVER_REDIRECT_DEST_URL),
                        results.getString(Constants.SERVER_REDIRECT_HOST_HEADER));
                curServer.setProfileId(results.getInt(Constants.GENERIC_PROFILE_ID));

                return curServer;
            }
            logger.info("Did not find the ID: {}", id);
        } catch (SQLException e) {
            throw e;
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
     * Returns server group by ID
     *
     * @param id
     * @return
     * @throws Exception
     */
    public ServerGroup getServerGroup(int id, int profileId) throws Exception {
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;


        if (id == 0) {
            return new ServerGroup(0, "Default", profileId);
        }


        try {
            sqlConnection = sqlService.getConnection();
            queryStatement = sqlConnection.prepareStatement(
                    "SELECT * FROM " + Constants.DB_TABLE_SERVER_GROUPS +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            queryStatement.setInt(1, id);
            results = queryStatement.executeQuery();
            if (results.next()) {
                ServerGroup curGroup = new ServerGroup(results.getInt(Constants.GENERIC_ID),
                        results.getString(Constants.GENERIC_NAME),
                        results.getInt(Constants.GENERIC_PROFILE_ID));
                return curGroup;
            }
            logger.info("Did not find the ID: {}", id);
        } catch (SQLException e) {
            throw e;
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
     * Add server redirect to a profile, using current active ServerGroup
     *
     * @param region
     * @param srcUrl
     * @param destUrl
     * @param hostHeader
     * @param profileId
     * @return
     * @throws Exception
     */
    public int addServerRedirectToProfile(String region, String srcUrl, String destUrl, String hostHeader,
                                          int profileId, int clientId) throws Exception {
        int serverId = -1;

        try {
            Client client = ClientService.getInstance().getClient(clientId);
            serverId = addServerRedirect(region, srcUrl, destUrl, hostHeader, profileId, client.getActiveServerGroup());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serverId;
    }

    /**
     * Add server redirect to a profile
     *
     * @param region
     * @param srcUrl
     * @param destUrl
     * @param hostHeader
     * @param profileId
     * @param groupId
     * @return
     * @throws Exception
     */
    public int addServerRedirect(String region, String srcUrl, String destUrl, String hostHeader, int profileId, int groupId) throws Exception {
        int serverId = -1;
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement("INSERT INTO " + Constants.DB_TABLE_SERVERS
                    + "(" + Constants.SERVER_REDIRECT_REGION + "," +
                    Constants.SERVER_REDIRECT_SRC_URL + "," +
                    Constants.SERVER_REDIRECT_DEST_URL + "," +
                    Constants.SERVER_REDIRECT_HOST_HEADER + "," +
                    Constants.SERVER_REDIRECT_PROFILE_ID + "," +
                    Constants.SERVER_REDIRECT_GROUP_ID + ")"
                    + " VALUES (?, ?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, region);
            statement.setString(2, srcUrl);
            statement.setString(3, destUrl);
            statement.setString(4, hostHeader);
            statement.setInt(5, profileId);
            statement.setInt(6, groupId);
            statement.executeUpdate();

            results = statement.getGeneratedKeys();

            if (results.next()) {
                serverId = results.getInt(1);
            } else {
                // something went wrong
                throw new Exception("Could not add path");
            }
        } catch (SQLException e) {
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

        return serverId;
    }

    /**
     * Add a new server group
     *
     * @param groupName name of the group
     * @param profileId ID of associated profile
     * @return id of server group
     * @throws Exception
     */
    public int addServerGroup(String groupName, int profileId) throws Exception {
        int groupId = -1;
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement("INSERT INTO " + Constants.DB_TABLE_SERVER_GROUPS
                    + "(" + Constants.GENERIC_NAME + "," +
                    Constants.GENERIC_PROFILE_ID + ")"
                    + " VALUES (?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, groupName);
            statement.setInt(2, profileId);
            statement.executeUpdate();

            results = statement.getGeneratedKeys();

            if (results.next()) {
                groupId = results.getInt(1);
            } else {
                // something went wrong
                throw new Exception("Could not add group");
            }
        } catch (SQLException e) {
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

        return groupId;
    }


    /**
     * Set the group name
     *
     * @param name
     * @param id
     */
    public void setGroupName(String name, int id) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement(
                    "UPDATE " + Constants.DB_TABLE_SERVER_GROUPS +
                            " SET " + Constants.GENERIC_NAME + " = ?" +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, name);
            statement.setInt(2, id);
            statement.executeUpdate();
            statement.close();
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
     * Activate a server group
     *
     * @param groupId
     * @param clientId
     */
    public void activateServerGroup(int groupId, int clientId) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;


        try {
            sqlConnection = sqlService.getConnection();

            statement = sqlConnection.prepareStatement(
                    "UPDATE " + Constants.DB_TABLE_CLIENT +
                            " SET " + Constants.CLIENT_ACTIVESERVERGROUP + " = ? " +
                            " WHERE " + Constants.GENERIC_ID + " = ? "
            );
            statement.setInt(1, groupId);
            statement.setInt(2, clientId);

            statement.executeUpdate();
            statement.close();
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
     * Set source url for a server
     *
     * @param newUrl
     * @param id
     */
    public void setSourceUrl(String newUrl, int id) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement(
                    "UPDATE " + Constants.DB_TABLE_SERVERS +
                            " SET " + Constants.SERVER_REDIRECT_SRC_URL + " = ?" +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, newUrl);
            statement.setInt(2, id);
            statement.executeUpdate();
            statement.close();
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
     * Set destination url for a server
     *
     * @param newUrl
     * @param id
     */
    public void setDestinationUrl(String newUrl, int id) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement(
                    "UPDATE " + Constants.DB_TABLE_SERVERS +
                            " SET " + Constants.SERVER_REDIRECT_DEST_URL + " = ?" +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, newUrl);
            statement.setInt(2, id);
            statement.executeUpdate();
            statement.close();
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
     * Set (optional) host header for a server
     *
     * @param newHost
     * @param id
     */
    public void setHostHeader(String newHost, int id) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement(
                    "UPDATE " + Constants.DB_TABLE_SERVERS +
                            " SET " + Constants.SERVER_REDIRECT_HOST_HEADER + " = ?" +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, newHost);
            statement.setInt(2, id);
            statement.executeUpdate();
            statement.close();
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
     * Deletes a redirect by id
     *
     * @param id
     */
    public void deleteRedirect(int id) {
        try {
            sqlService.executeUpdate("DELETE FROM " + Constants.DB_TABLE_SERVERS +
                    " WHERE " + Constants.GENERIC_ID + " = " + id + ";");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Delete a server group by id
     *
     * @param id
     */
    public void deleteServerGroup(int id) {
        try {
            sqlService.executeUpdate("DELETE FROM " + Constants.DB_TABLE_SERVER_GROUPS +
                    " WHERE " + Constants.GENERIC_ID + " = " + id + ";");

            sqlService.executeUpdate("DELETE FROM " + Constants.DB_TABLE_SERVERS +
                    " WHERE " + Constants.SERVER_REDIRECT_GROUP_ID + " = " + id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This returns all profiles associated with a server name
     *
     * @param serverName
     * @return profile UUID
     */
    public Profile[] getProfilesForServerName(String serverName) throws Exception {
        int profileId = -1;
        ArrayList<Profile> returnProfiles = new ArrayList<Profile>();

        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;
        ResultSet results = null;
        try {
            sqlConnection = sqlService.getConnection();
            queryStatement = sqlConnection.prepareStatement(
                    "SELECT " + Constants.GENERIC_PROFILE_ID + " FROM " + Constants.DB_TABLE_SERVERS +
                            " WHERE " + Constants.SERVER_REDIRECT_SRC_URL + " = ? GROUP BY " +
                            Constants.GENERIC_PROFILE_ID
            );
            queryStatement.setString(1, serverName);
            results = queryStatement.executeQuery();

            while (results.next()) {
                profileId = results.getInt(Constants.GENERIC_PROFILE_ID);

                Profile profile = ProfileService.getInstance().findProfile(profileId);
                
                returnProfiles.add(profile);
            }
        } catch (SQLException e) {
            throw e;
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

        if (returnProfiles.size() == 0)
            return null;
        return returnProfiles.toArray(new Profile[0]);
    }

    /**
     * Returns true or false depending on whether or not Odo can handle the request for this server/clientUUID pair
     *
     * @param serverName
     * @return
     * @throws Exception
     */
    public Boolean canHandleRequest(String serverName) throws Exception {
        // TODO: Future optimizations
        try {
            Profile[] profiles = this.getProfilesForServerName(serverName);
            if(profiles == null)
            {
                logger.info("No matching profiles found for path");
                return false;
            }
            for (Profile profile : profiles) {
                List<Client> clients = ClientService.getInstance().findAllClients(profile.getId());
                for (Client client : clients) {
                    if (client.getIsActive()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}



