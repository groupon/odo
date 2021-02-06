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
package com.groupon.odo.proxylib

import com.groupon.odo.proxylib.models.Client
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*

class ClientService {
    /**
     * Return all Clients for a profile
     *
     * @param profileId ID of profile clients belong to
     * @return collection of the Clients found
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun findAllClients(profileId: Int): List<Client> {
        val clients = ArrayList<Client>()
        var query: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                query = sqlConnection?.prepareStatement(
                        "SELECT * FROM ${Constants.DB_TABLE_CLIENT} WHERE ${Constants.GENERIC_PROFILE_ID} = ?"
                )
                query?.setInt(1, profileId)
                results = query?.executeQuery()
                while (results?.next() != null) {
                    clients.add(getClientFromResultSet(results))
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            try {
                results?.close()
            } catch (e: Exception) {
            }
            try {
                query?.close()
            } catch (e: Exception) {
            }
        }
        return clients
    }

    /**
     * Returns a client object for a clientId
     *
     * @param clientId ID of client to return
     * @return Client or null
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun getClient(clientId: Int): Client? {
        var client: Client? = null
        var statement: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                val queryString = "SELECT * FROM ${Constants.DB_TABLE_CLIENT}" +
                        " WHERE ${Constants.GENERIC_ID} = ?"
                statement = sqlConnection?.prepareStatement(queryString)
                statement?.setInt(1, clientId)

                results = statement?.executeQuery()
                if (results?.next() != null) {
                    client = getClientFromResultSet(results)
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            try {
                results?.close()
            } catch (e: Exception) {
            }
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
        return client
    }

    /**
     * Returns a Client object for a clientUUID and profileId
     *
     * @param clientUUID UUID or friendlyName of client
     * @param profileId - can be null, safer if it is not null
     * @return Client object or null
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun findClient(clientUUID: String?, profileId: Int?): Client? { // TODO: in the Java version, profileId is an object Integer rather than primitive. Figure out whether this is a problem. If Not, remove the nullability
        var client: Client? = null

        /* ERROR CODE: 500 WHEN TRYING TO DELETE A SERVER GROUP.
            THIS APPEARS TO BE BECAUSE CLIENT UUID IS NULL.
         */
        /* CODE ADDED TO PREVENT NULL POINTERS. */
        var clientUUID = clientUUID
        if (clientUUID == null) {
            clientUUID = ""
        }

        // first see if the clientUUID is actually a uuid.. it might be a friendlyName and need conversion
        /* A UUID IS A UNIVERSALLY UNIQUE IDENTIFIER. */
        if (clientUUID.compareTo(Constants.PROFILE_CLIENT_DEFAULT_ID) != 0 &&
                !clientUUID.matches(Regex("[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}"))) {
            val tmpClient = findClientFromFriendlyName(profileId, clientUUID)

            // if we can't find a client then fall back to the default ID
            if (tmpClient == null) {
                clientUUID = Constants.PROFILE_CLIENT_DEFAULT_ID
            } else {
                return tmpClient
            }
        }
        var statement: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                var queryString = "SELECT * FROM ${Constants.DB_TABLE_CLIENT}" +
                        " WHERE ${Constants.CLIENT_CLIENT_UUID} = ?"
                if (profileId != null) {
                    queryString += " AND ${Constants.GENERIC_PROFILE_ID}=?"
                }

                statement = sqlConnection?.prepareStatement(queryString)
                statement?.setString(1, clientUUID)

                if (profileId != null) {
                    statement?.setInt(2, profileId)
                }

                results = statement?.executeQuery()
                if (results?.next() != null) {
                    client = getClientFromResultSet(results)
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            try {
                results?.close()
            } catch (e: Exception) {
            }
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
        return client
    }

    /**
     * Returns a client model from a ResultSet
     *
     * @param result resultset containing client information
     * @return Client or null
     * @throws Exception exception
     */
    @Throws(Exception::class)
    private fun getClientFromResultSet(result: ResultSet?): Client = Client().apply {
        id = result!!.getInt(Constants.GENERIC_ID)
        uuid = result.getString(Constants.CLIENT_CLIENT_UUID)
        friendlyName = result.getString(Constants.CLIENT_FRIENDLY_NAME)
        profile = ProfileService.getInstance().findProfile(result.getInt(Constants.GENERIC_PROFILE_ID))
        isActive = result.getBoolean(Constants.CLIENT_IS_ACTIVE)
        activeServerGroup = result.getInt(Constants.CLIENT_ACTIVESERVERGROUP)
    }

    // TODO: Should this be a property? It almost seems like the UUID changes every time
    private val uniqueClientUUID: String
        get() {
            var curClientUUID = UUID.randomUUID().toString()
            var statement: PreparedStatement? = null
            var results: ResultSet? = null
            try {
                sqlService?.connection.use { sqlConnection ->
                    while (true) {
                        statement = sqlConnection?.prepareStatement("SELECT * FROM ${Constants.DB_TABLE_CLIENT}" +
                                " WHERE ${Constants.GENERIC_CLIENT_UUID} = ?")
                        statement?.setString(1, curClientUUID)
                        results = statement?.executeQuery()
                        if (results?.next() != null) {
                            curClientUUID = UUID.randomUUID().toString()
                        } else {
                            break
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                try {
                    results?.close()
                } catch (e: Exception) {
                }
                try {
                    statement?.close()
                } catch (e: Exception) {
                }
            }
            logger.info("ClientUUID of new client = {}", curClientUUID)
            return curClientUUID
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
    @Throws(Exception::class)
    fun add(profileId: Int): Client? {
        var client: Client? = null
        val pathsToCopy = ArrayList<Int>()
        val clientUUID = uniqueClientUUID

        // get profile for profileId
        val profile = ProfileService.getInstance().findProfile(profileId)
        var statement: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->

                // get the current count of clients
                statement = sqlConnection?.prepareStatement("SELECT COUNT(${Constants.GENERIC_ID}) FROM "  +
                         "${Constants.DB_TABLE_CLIENT} WHERE ${Constants.GENERIC_PROFILE_ID}=?")
                statement?.setInt(1, profileId)
                var clientCount = -1
                rs = statement?.executeQuery()
                if (rs?.next() != null) { // TODO: We need to capture this somewhere
                    clientCount = rs!!.getInt(1)
                }
                statement?.close()
                rs?.close()

                // check count
                if (clientCount == -1) {
                    throw Exception("Error querying clients for profileId=$profileId")
                }
                if (clientCount >= Constants.CLIENT_CLIENTS_PER_PROFILE_LIMIT) {
                    throw Exception("Profile($profileId) already contains 50 clients.  Please remove clients before adding new ones.")
                }
                statement = sqlConnection?.prepareStatement(
                        "INSERT INTO ${Constants.DB_TABLE_CLIENT} (${Constants.CLIENT_CLIENT_UUID}, ${Constants.CLIENT_IS_ACTIVE}, ${Constants.CLIENT_PROFILE_ID})" +
                                " VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS
                )
                statement?.setString(1, clientUUID)
                statement?.setBoolean(2, false)
                statement?.setInt(3, profile.id)
                statement?.executeUpdate()
                rs = statement?.generatedKeys
                val clientId = if (rs?.next() != null) { // TODO: this could prob be replaced with an inline try
                    rs?.getInt(1) ?: -1
                } else {
                    // something went wrong
                    throw Exception("Could not add client")
                }
                rs?.close()
                statement?.close()

                // adding entries into request response table for this new client for every path
                // basically a copy of what happens when a path gets created
                statement = sqlConnection?.prepareStatement(
                        "SELECT * FROM ${Constants.DB_TABLE_REQUEST_RESPONSE}" +
                                " WHERE ${Constants.GENERIC_PROFILE_ID} = ?" +
                                " AND ${Constants.GENERIC_CLIENT_UUID} = ?"
                )
                statement?.setInt(1, profile.id)
                statement?.setString(2, Constants.PROFILE_CLIENT_DEFAULT_ID)
                rs = statement?.executeQuery()
                while (rs?.next() != null) {
                    // collect up the pathIds we need to copy
                    pathsToCopy.add(rs!!.getInt(Constants.REQUEST_RESPONSE_PATH_ID)) // TODO: take out this !! operator
                }
                client = Client().also { client ->
                    client.isActive = false
                    client.uuid = clientUUID
                    client.id = clientId
                    client.profile = profile
                }
            }
        } catch (e: SQLException) {
            throw e
        } finally {
            try {
                rs?.close()
            } catch (e: Exception) {
            }
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }

        // add all of the request response items
        for (pathId in pathsToCopy) {
            PathOverrideService.getInstance().addPathToRequestResponseTable(profile.id, client?.uuid, pathId)
        }
        return client
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
    @Throws(Exception::class)
    fun setFriendlyName(profileId: Int?, clientUUID: String, friendlyName: String?): Client? {
        // first see if this friendlyName is already in use
        val client = findClientFromFriendlyName(profileId, friendlyName)
        if (client != null && client.uuid != clientUUID) {
            throw Exception("Friendly name already in use")
        }
        var statement: PreparedStatement? = null
        var rowsAffected = 0
        try {
            sqlService?.connection.use { sqlConnection ->
                statement = sqlConnection?.prepareStatement(
                        "UPDATE " + Constants.DB_TABLE_CLIENT +
                                " SET " + Constants.CLIENT_FRIENDLY_NAME + " = ?" +
                                " WHERE " + Constants.CLIENT_CLIENT_UUID + " = ?" +
                                " AND " + Constants.GENERIC_PROFILE_ID + " = ?"
                )
                statement?.setString(1, friendlyName)
                statement?.setString(2, clientUUID)
                statement?.setInt(3, profileId ?: 0) // TODO: if null then 0. This is a temporary fix. This seems to be overlooked in the original java code
                rowsAffected = statement?.executeUpdate() ?: 0 // TODO: this is the default number of rules affected as can be seen at `var rowsAffected = 0`
            }
        } catch (e: Exception) {
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
        return if (rowsAffected == 0) {
            null
        } else findClient(clientUUID, profileId)
    }

    /**
     * Get the client for a profileId/friendlyName
     *
     * @param profileId profile ID of the client
     * @param friendlyName friendly name of the client
     * @return Client or null
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun findClientFromFriendlyName(profileId: Int?, friendlyName: String?): Client? {
        var client: Client? = null

        // Don't even try if the friendlyName is null/empty
        if (friendlyName == null || friendlyName.compareTo("") == 0) {
            return null
        }
        var statement: PreparedStatement? = null
        var results: ResultSet? = null

        try {
            sqlService?.connection.use { sqlConnection ->
                val queryString = "SELECT * FROM ${Constants.DB_TABLE_CLIENT}" +
                        " WHERE ${Constants.CLIENT_FRIENDLY_NAME} = ?" +
                        " AND ${Constants.GENERIC_PROFILE_ID} = ?"
                statement = sqlConnection?.prepareStatement(queryString)
                statement?.setString(1, friendlyName)
                statement?.setInt(2, profileId ?: 0) // TODO: Handling of profileId possibily being null seems to be overlooked here to in the original java code. What should the default be? 0? -1?

                results = statement?.executeQuery()
                if (results?.next() != null) {
                    client = getClientFromResultSet(results)
                }
            }
        } catch (e: SQLException) {
            throw e
        } finally {
            try {
                results?.close()
            } catch (e: Exception) {
            }
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
        return client
    }

    /**
     * Removes a client from the database
     * Also clears all additional override information for the clientId
     *
     * @param profileId profile ID of client to remove
     * @param clientUUID client UUID of client to remove
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun remove(profileId: Int, clientUUID: String) {
        var statement: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                // first try selecting the row we want to deal with
                statement = sqlConnection?.prepareStatement(
                        "SELECT * FROM ${Constants.DB_TABLE_CLIENT}" +
                                " WHERE ${Constants.GENERIC_CLIENT_UUID} = ?" +
                                " AND ${Constants.CLIENT_PROFILE_ID}= ?"
                )
                statement?.setString(1, clientUUID)
                statement?.setInt(2, profileId)
                results = statement?.executeQuery()
                if (results?.next() == null) {
                    throw Exception("Could not find specified clientUUID: $clientUUID")
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            try {
                results?.close()
            } catch (e: Exception) {
            }
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }

        // delete from the client table
        val queryString = "DELETE FROM ${Constants.DB_TABLE_CLIENT}" +
                " WHERE ${Constants.CLIENT_CLIENT_UUID} = ? " +
                " AND ${Constants.CLIENT_PROFILE_ID} = ?"
        try {
            sqlService?.connection.use { sqlConnection ->
                statement = sqlConnection?.prepareStatement(queryString)
                statement?.setString(1, clientUUID)
                statement?.setInt(2, profileId)

                logger.info("Query: {}", statement.toString())
                statement?.executeUpdate()
            }
        } catch (e: Exception) {
            throw e
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }

        // delete from other tables as appropriate
        // need to delete from enabled_overrides and request_response
        try {
            sqlService?.connection.use { sqlConnection ->
                statement = sqlConnection?.prepareStatement(
                        "DELETE FROM ${Constants.DB_TABLE_REQUEST_RESPONSE}" +
                                " WHERE ${Constants.CLIENT_CLIENT_UUID} = ? " +
                                " AND ${Constants.CLIENT_PROFILE_ID} = ?"
                )
                statement?.setString(1, clientUUID)
                statement?.setInt(2, profileId)
                statement?.executeUpdate()
            }
        } catch (e: Exception) {
            // ok to swallow this.. just means there wasn't any
        }
        try {
            sqlService?.connection.use { sqlConnection ->
                statement = sqlConnection?.prepareStatement(
                        "DELETE FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                " WHERE ${Constants.CLIENT_CLIENT_UUID} = ? " +
                                " AND ${Constants.CLIENT_PROFILE_ID} = ?"
                )
                statement?.setString(1, clientUUID)
                statement?.setInt(2, profileId)
                statement?.executeUpdate()
            }
        } catch (e: Exception) {
            // ok to swallow this.. just means there wasn't any
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
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
    @Throws(Exception::class)
    fun updateActive(profileId: Int, clientUUID: String?, active: Boolean?) {
        var statement: PreparedStatement? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                statement = sqlConnection?.prepareStatement(
                        "UPDATE ${Constants.DB_TABLE_CLIENT}" +
                                " SET ${Constants.CLIENT_IS_ACTIVE}= ?" +
                                " WHERE ${Constants.GENERIC_CLIENT_UUID}= ? " +
                                " AND ${Constants.GENERIC_PROFILE_ID}= ?"
                )
                statement?.setBoolean(1, active!!)
                statement?.setString(2, clientUUID)
                statement?.setInt(3, profileId)
                statement?.executeUpdate()
            }
        } catch (e: Exception) {
            // ok to swallow this.. just means there wasn't any
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
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
    @Throws(Exception::class)
    fun reset(profileId: Int, clientUUID: String?) {
        var statement: PreparedStatement? = null

        // TODO: need a better way to do this than brute force.. but the iterative approach is too slow
        try {
            sqlService?.connection.use { sqlConnection ->

                // first remove all enabled overrides with this client uuid
                var queryString = "DELETE FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                        " WHERE ${Constants.GENERIC_CLIENT_UUID}= ? " +
                        " AND ${Constants.GENERIC_PROFILE_ID} = ?"
                statement = sqlConnection?.prepareStatement(queryString)
                statement?.setString(1, clientUUID)
                statement?.setInt(2, profileId)
                statement?.executeUpdate()
                statement?.close()

                // clean up request response table for this uuid
                queryString = ("UPDATE ${Constants.DB_TABLE_REQUEST_RESPONSE}" +
                        " SET ${Constants.REQUEST_RESPONSE_CUSTOM_REQUEST}=?, "
                        + "${Constants.REQUEST_RESPONSE_CUSTOM_RESPONSE}=?, "
                        + "${Constants.REQUEST_RESPONSE_REPEAT_NUMBER}=-1, "
                        + "${Constants.REQUEST_RESPONSE_REQUEST_ENABLED}=0, "
                        + "${Constants.REQUEST_RESPONSE_RESPONSE_ENABLED}=0 "
                        + "WHERE ${Constants.GENERIC_CLIENT_UUID}=? " +
                        " AND ${Constants.GENERIC_PROFILE_ID}=?")
                statement = sqlConnection?.prepareStatement(queryString)
                statement?.setString(1, "")
                statement?.setString(2, "")
                statement?.setString(3, clientUUID)
                statement?.setInt(4, profileId)
                statement?.executeUpdate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
        updateActive(profileId, clientUUID, false)
    }

    fun getClientUUIDfromId(id: Int): String {
        return sqlService?.getFromTable(Constants.CLIENT_CLIENT_UUID, Constants.GENERIC_ID, id, Constants.DB_TABLE_CLIENT) as String
    }

    fun getIdFromClientUUID(uuid: String?): Int {
        return sqlService?.getFromTable(Constants.GENERIC_ID, Constants.CLIENT_CLIENT_UUID, uuid, Constants.DB_TABLE_CLIENT) as Int
    }

    //gets the profile_name associated with a specific id
    fun getProfileIdFromClientId(id: Int): String {
        return sqlService?.getFromTable(Constants.CLIENT_PROFILE_ID, Constants.GENERIC_ID, id, Constants.DB_TABLE_CLIENT) as String
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ClientService::class.java)
        private var sqlService: SQLService? = null
        var instance: ClientService? = null
            get() {
                if (field == null) {
                    field = ClientService()
                    try {
                        sqlService = SQLService.getInstance()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return field
            }
    }
}