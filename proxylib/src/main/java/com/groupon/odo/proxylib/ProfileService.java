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

import com.groupon.odo.proxylib.models.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ProfileService {
    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);
    private static SQLService sqlService = null;
    private static ProfileService serviceInstance = null;

    public ProfileService() {

    }

    /**
     * Tries to open the table(database), if it fails to open it, creates a new one
     *
     * @return
     */
    public static ProfileService getInstance() {
        if (serviceInstance == null) {
            serviceInstance = new ProfileService();
            try {
                sqlService = SQLService.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
        return serviceInstance;
    }

    /**
     * Returns true if the default profile for the specified uuid is active
     *
     * @return
     */
    public boolean isActive(int profileId) {
        boolean active = false;
        Connection sqlConnection = null;
        PreparedStatement queryStatement = null;

        try {
            sqlConnection = sqlService.getConnection();
            queryStatement = sqlConnection.prepareStatement(
                    "SELECT " + Constants.CLIENT_IS_ACTIVE + " FROM " + Constants.DB_TABLE_CLIENT +
                            " WHERE " + Constants.GENERIC_CLIENT_UUID + "= '-1' " +
                            " AND " + Constants.GENERIC_PROFILE_ID + "= ? "
            );
            queryStatement.setInt(1, profileId);
            logger.info(queryStatement.toString());
            ResultSet results = queryStatement.executeQuery();
            if (results.next()) {
                active = results.getBoolean(Constants.CLIENT_IS_ACTIVE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (queryStatement != null) queryStatement.close();
            } catch (Exception e) {
            }
        }

        return active;
    }

    /**
     * Returns a collection of all profiles
     *
     * @return
     */
    public List<Profile> findAllProfiles() throws Exception {
        ArrayList<Profile> allProfiles = new ArrayList<Profile>();
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB_TABLE_PROFILE);
            results = statement.executeQuery();
            while (results.next()) {
                allProfiles.add(this.getProfileFromResultSet(results));
            }
        } catch (Exception e) {
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
        return allProfiles;
    }

    /**
     * Returns a specific profile
     *
     * @param profileId
     * @return
     * @throws Exception
     */
    public Profile findProfile(int profileId) throws Exception {
        Profile profile = null;
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement(
                    "SELECT * FROM " + Constants.DB_TABLE_PROFILE +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setInt(1, profileId);
            results = statement.executeQuery();
            if (results.next()) {
                profile = this.getProfileFromResultSet(results);
            }
        } catch (Exception e) {
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
        return profile;
    }

    /**
     * Creates a Profile object from a SQL resultset
     *
     * @param result
     * @return
     * @throws Exception
     */
    private Profile getProfileFromResultSet(ResultSet result) throws Exception {
        Profile profile = new Profile();
        profile.setId(result.getInt(Constants.GENERIC_ID));
        Clob clobProfileName = result.getClob(Constants.PROFILE_PROFILE_NAME);
        String profileName = clobProfileName.getSubString(1, (int) clobProfileName.length());
        profile.setName(profileName);
        return profile;
    }

    /**
     * Add a new profile with the profileName given.
     *
     * @param profileName
     * @return
     * @throws Exception
     */
    public Profile add(String profileName) throws Exception {
        Profile profile = new Profile();
        int id = -1;
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            Clob clobProfileName = sqlService.toClob(profileName, sqlConnection);

            statement = sqlConnection.prepareStatement(
                    "INSERT INTO " + Constants.DB_TABLE_PROFILE
                            + "(" + Constants.PROFILE_PROFILE_NAME + ") " +
                            " VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS
            );

            statement.setClob(1, clobProfileName);
            statement.executeUpdate();
            results = statement.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            } else {
                // something went wrong
                throw new Exception("Could not add client");
            }
            results.close();
            statement.close();

            statement = sqlConnection.prepareStatement("INSERT INTO " + Constants.DB_TABLE_CLIENT +
                    "(" + Constants.CLIENT_CLIENT_UUID + "," + Constants.CLIENT_IS_ACTIVE + ","
                    + Constants.CLIENT_PROFILE_ID + ") " +
                    " VALUES (?, ?, ?)");
            statement.setString(1, Constants.PROFILE_CLIENT_DEFAULT_ID);
            statement.setBoolean(2, false);
            statement.setInt(3, id);
            statement.executeUpdate();

            profile.setName(profileName);
            profile.setId(id);
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

        return profile;
    }

    //passed in the user id to delete, finds it in database and deletes it

    /**
     * Deletes data associated with the given profile ID
     *
     * @param profileId
     */
    public void remove(int profileId) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = sqlService.getConnection();
            statement = sqlConnection.prepareStatement("DELETE FROM " + Constants.DB_TABLE_PROFILE +
                    " WHERE " + Constants.GENERIC_ID + " = ?");
            statement.setInt(1, profileId);
            statement.executeUpdate();
            statement.close();
            //also want to delete what is in the server redirect table
            statement = sqlConnection.prepareStatement("DELETE FROM " + Constants.DB_TABLE_SERVERS +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + " = ?");
            statement.setInt(1, profileId);
            statement.executeUpdate();
            statement.close();
            //also want to delete the path_profile table
            statement = sqlConnection.prepareStatement("DELETE FROM " + Constants.DB_TABLE_PATH +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + " = ?");
            statement.setInt(1, profileId);
            statement.executeUpdate();
            statement.close();
            //and the enabled overrides table
            statement = sqlConnection.prepareStatement("DELETE FROM " + Constants.DB_TABLE_ENABLED_OVERRIDE +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + " = ?");
            statement.setInt(1, profileId);
            statement.executeUpdate();
            statement.close();
            //and delete all the clients associated with this profile including the default client
            statement = sqlConnection.prepareStatement("DELETE FROM " + Constants.DB_TABLE_CLIENT +
                    " WHERE " + Constants.GENERIC_PROFILE_ID + " = ?");
            statement.setInt(1, profileId);
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
     * Obtain the profile name associated with a profile ID
     *
     * @param id
     * @return
     */
    public String getNamefromId(int id) {
        return (String) sqlService.getFromTable(
                Constants.PROFILE_PROFILE_NAME, Constants.GENERIC_ID,
                id, Constants.DB_TABLE_PROFILE);
    }


    //gets the profileId given the name

    /**
     * Obtain the ID associated with a profile name
     *
     * @param profileName
     * @return
     */
    public Integer getIdFromName(String profileName) {
        Connection sqlConnection = null;
        PreparedStatement query = null;
        ResultSet results = null;

        try {
            sqlConnection = sqlService.getConnection();
            query = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB_TABLE_PROFILE +
                    " WHERE " + Constants.PROFILE_PROFILE_NAME + " = ?");
            query.setString(1, profileName);
            results = query.executeQuery();
            if (results.next()) {
                Object toReturn = results.getObject(Constants.GENERIC_ID);
                query.close();
                return (Integer) toReturn;
            }
            query.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (results != null) results.close();
            } catch (Exception e) {
            }
            try {
                if (query != null) query.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

}