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

import com.groupon.odo.proxylib.models.EnabledEndpoint
import com.groupon.odo.proxylib.models.Method
import flexjson.JSONSerializer
import org.json.JSONArray
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.StringBuilder
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList

class OverrideService {
    /**
     * Enable specific override ID for a path
     *
     * @param overrideId ID of override to enable
     * @param pathId ID of path containing override
     * @param clientUUID UUID of client
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun enableOverride(overrideId: Int, pathId: Int, clientUUID: String) {
        // get profileId from pathId
        val profileId = PathOverrideService.getInstance().getPath(pathId).profileId
        var newPriority = 0

        // we want to limit -1, -2 to only be added once since they are the Custom responses/requests
        if (overrideId == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM) {
            if (getEnabledEndpoint(pathId, overrideId, null, clientUUID) != null) {
                return
            }
        }

        // need to first determine the highest enabled order value for this path
        val priorities = sqlService?.getFirstResult(
                "SELECT * FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                        " WHERE ${Constants.REQUEST_RESPONSE_PATH_ID}=$pathId" +
                        " AND ${Constants.GENERIC_CLIENT_UUID}='$clientUUID" +
                        "' ORDER BY + ${Constants.ENABLED_OVERRIDES_PRIORITY} DESC"
        )
        if (priorities != null) {
            newPriority = Integer.valueOf(priorities[Constants.ENABLED_OVERRIDES_PRIORITY.toUpperCase()].toString()) + 1
        }
        var statement: PreparedStatement? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                var query: PreparedStatement? = null
                var results: ResultSet? = null
                val sqlService: SQLService = SQLService.getInstance()
                var method: Method? = null
                query = sqlConnection?.prepareStatement(
                        ("SELECT * FROM ${Constants.DB_TABLE_OVERRIDE}" +
                                " WHERE ${Constants.GENERIC_ID} = ?")
                )
                query?.setString(1, overrideId.toString())
                results = query?.executeQuery()
                val serializer: JSONSerializer = JSONSerializer()
                if (results?.next() != null) {
                    val className: String = results.getString(Constants.OVERRIDE_CLASS_NAME)
                    val methodName: String = results.getString(Constants.OVERRIDE_METHOD_NAME)
                    method = PluginManager.getInstance().getMethod(className, methodName)
                }
                statement = sqlConnection?.prepareStatement(
                        ("INSERT INTO ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                            "(${Constants.GENERIC_PROFILE_ID},${Constants.GENERIC_CLIENT_UUID}," +
                            "${Constants.REQUEST_RESPONSE_PATH_ID},${Constants.ENABLED_OVERRIDES_OVERRIDE_ID}," +
                            "${Constants.ENABLED_OVERRIDES_PRIORITY},${Constants.ENABLED_OVERRIDES_ARGUMENTS}," +
                            "${Constants.ENABLED_OVERRIDES_RESPONSE_CODE})" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?);")
                )
                statement?.setInt(1, profileId)
                statement?.setString(2, clientUUID)
                statement?.setInt(3, pathId)
                statement?.setInt(4, overrideId)
                statement?.setInt(5, newPriority)
                if (method == null) {
                    statement?.setString(6, "")
                } else {
                    val argDefaults: ArrayList<String> = ArrayList()
                    for (i in method.getMethodArguments().indices) {
                        if (i < method.getMethodDefaultArguments().size && method.getMethodDefaultArguments().get(i) != null) {
                            argDefaults.add(method.getMethodDefaultArguments().get(i).toString())
                        } else {
                            argDefaults.add("")
                        }
                    }
                    statement?.setString(6, serializer.serialize(argDefaults))
                }
                statement?.setString(7, "200")
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
    }

    /**
     * Update the arguments for a given enabled override
     *
     * @param overrideId - override ID to update
     * @param pathId - path ID to update
     * @param ordinal - can be null, Index of the enabled override to edit if multiple of the same are enabled
     * @param arguments - Object array of arguments
     * @param clientUUID - clientUUID
     */
    fun updateArguments(overrideId: Int, pathId: Int, ordinal: Int?, arguments: String?, clientUUID: String?) {
        var ordinal = ordinal
        if (ordinal == null) {
            ordinal = 1
        }
        var statement: PreparedStatement? = null
        try {
            sqlService!!.connection.use { sqlConnection ->
                // get ID of the ordinal
                val enabledId: Int = getEnabledEndpoint(pathId, overrideId, ordinal, clientUUID)!!.getId()
                val queryString: String = ("UPDATE ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                        " SET ${Constants.ENABLED_OVERRIDES_ARGUMENTS} = ? " +
                        " WHERE ${Constants.GENERIC_ID} = ?")
                statement = sqlConnection.prepareStatement(queryString)
                statement?.setString(1, arguments)
                statement?.setInt(2, enabledId)
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
    }

    /**
     * Update the repeat number for a given enabled override
     *
     * @param overrideId - override ID to update
     * @param pathId - path ID to update
     * @param ordinal - can be null, Index of the enabled override to edit if multiple of the same are enabled
     * @param repeatNumber - number of times to repeat
     * @param clientUUID - clientUUID
     */
    fun updateRepeatNumber(overrideId: Int, pathId: Int, ordinal: Int?, repeatNumber: Int?, clientUUID: String?) {
        var ordinal = ordinal
        if (ordinal == null) {
            ordinal = 1
        }
        try {
            // get ID of the ordinal
            val enabledId = getEnabledEndpoint(pathId, overrideId, ordinal, clientUUID)!!.id
            updateRepeatNumber(enabledId, repeatNumber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Update the repeat number for a given enabled override
     *
     * @param id enabled override ID to update
     * @param repeatNumber updated value of repeat
     */
    fun updateRepeatNumber(id: Int, repeatNumber: Int?) {
        var statement: PreparedStatement? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                val queryString: String = ("UPDATE ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                        " SET ${Constants.ENABLED_OVERRIDES_REPEAT_NUMBER}= ? " +
                        " WHERE ${Constants.GENERIC_ID} = ?")
                statement = sqlConnection?.prepareStatement(queryString)
                statement?.setInt(1, (repeatNumber)!!)
                statement?.setInt(2, id)
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
    }

    /**
     * Update the response code for a given enabled override
     *
     * @param overrideId - override ID to update
     * @param pathId - path ID to update
     * @param ordinal - can be null, Index of the enabled override to edit if multiple of the same are enabled
     * @param responseCode - response code for the given response
     * @param clientUUID - clientUUID
     */
    fun updateResponseCode(overrideId: Int, pathId: Int, ordinal: Int?, responseCode: String?, clientUUID: String?) {
        var ordinal = ordinal
        if (ordinal == null) {
            ordinal = 1
        }
        try {
            // get ID of the ordinal
            val enabledId = getEnabledEndpoint(pathId, overrideId, ordinal, clientUUID)!!.id
            updateResponseCode(enabledId, responseCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Update the response code for a given enabled override
     *
     * @param id enabled override ID to update
     * @param responseCode updated value of responseCode
     */
    fun updateResponseCode(id: Int, responseCode: String?) {
        var statement: PreparedStatement? = null
        try {
            sqlService!!.connection.use { sqlConnection ->
                val queryString: String = ("UPDATE ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                        " SET ${Constants.ENABLED_OVERRIDES_RESPONSE_CODE}= ? " +
                        " WHERE ${Constants.GENERIC_ID} = ?")
                statement = sqlConnection.prepareStatement(queryString)
                statement?.setString(1, responseCode)
                statement?.setInt(2, id)
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
    }

    /**
     * Remove specified override id from enabled overrides for path
     *
     * @param overrideId ID of override to remove
     * @param pathId ID of path containing override
     * @param ordinal index to the instance of the enabled override
     * @param clientUUID UUID of client
     */
    fun removeOverride(overrideId: Int, pathId: Int, ordinal: Int?, clientUUID: String?) {
        // TODO: reorder priorities after removal
        var statement: PreparedStatement? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                val enabledId: Int = getEnabledEndpoint(pathId, overrideId, ordinal, clientUUID)!!.getId()
                statement = sqlConnection?.prepareStatement(
                        ("DELETE FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                " WHERE ${Constants.GENERIC_ID} = ?")
                )
                statement?.setInt(1, enabledId)
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
    }

    /**
     * Increase the priority of an overrideId
     *
     * @param overrideId ID of override
     * @param pathId ID of path containing override
     * @param clientUUID UUID of client
     */
    fun increasePriority(overrideId: Int, ordinal: Int, pathId: Int, clientUUID: String?) {
        logger.info("Increase priority")
        var origPriority = -1
        var newPriority = -1
        var origId = 0
        var newId = 0
        var statement: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                results = null
                statement = sqlConnection?.prepareStatement(
                        ("SELECT * FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                " WHERE ${Constants.ENABLED_OVERRIDES_PATH_ID} = ?" +
                                " AND ${Constants.GENERIC_CLIENT_UUID} = ?" +
                                " ORDER BY ${Constants.ENABLED_OVERRIDES_PRIORITY}")
                )
                statement?.setInt(1, pathId)
                statement?.setString(2, clientUUID)
                results = statement?.executeQuery()
                var ordinalCount: Int = 0
                while (results?.next() != null) {
                    if (results?.getInt(Constants.ENABLED_OVERRIDES_OVERRIDE_ID) == overrideId) {
                        ordinalCount++
                        if (ordinalCount == ordinal) {
                            origPriority = results!!.getInt(Constants.ENABLED_OVERRIDES_PRIORITY) // TODO: Need to remove this !! op
                            origId = results!!.getInt(Constants.GENERIC_ID) // TODO: Need to remove this !! op
                            break
                        }
                    }
                    newPriority = results!!.getInt(Constants.ENABLED_OVERRIDES_PRIORITY) // TODO: need to remove this !! op
                    newId = results!!.getInt(Constants.GENERIC_ID) // TODO: need to remove this !! op
                }
            }
        } catch (e: Exception) {
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
        try {
            sqlService?.connection.use { sqlConnection ->
                // update priorities
                if (origPriority != -1 && newPriority != -1) {
                    statement = sqlConnection?.prepareStatement(
                            ("UPDATE ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                    " SET ${Constants.ENABLED_OVERRIDES_PRIORITY}=?" +
                                    " WHERE ${Constants.GENERIC_ID}=?")
                    )
                    statement?.setInt(1, origPriority)
                    statement?.setInt(2, newId)
                    statement?.executeUpdate()
                    statement?.close()
                    statement = sqlConnection?.prepareStatement(
                            ("UPDATE ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                    " SET ${Constants.ENABLED_OVERRIDES_PRIORITY}=?" +
                                    " WHERE ${Constants.GENERIC_ID}=?")
                    )
                    statement?.setInt(1, newPriority)
                    statement?.setInt(2, origId)
                    statement?.executeUpdate()
                }
            }
        } catch (e: Exception) {
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Decreases the priority of an overrideId
     *
     * @param overrideId Id of override to edit
     * @param pathId ID of path containing override
     * @param clientUUID ID of client
     */
    fun decreasePriority(overrideId: Int, ordinal: Int, pathId: Int, clientUUID: String?) {
        logger.info("Decrease priority")
        var origPriority = -1
        var newPriority = -1
        var origId = 0
        var newId = 0
        var queryStatement: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService!!.connection.use { sqlConnection ->
                queryStatement = sqlConnection.prepareStatement(
                        ("SELECT * FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                " WHERE ${Constants.ENABLED_OVERRIDES_PATH_ID} = ?" +
                                " AND ${Constants.GENERIC_CLIENT_UUID} = ?" +
                                " ORDER BY ${Constants.ENABLED_OVERRIDES_PRIORITY}")
                )
                queryStatement?.setInt(1, pathId)
                queryStatement?.setString(2, clientUUID)
                results = queryStatement?.executeQuery()
                var gotOrig: Boolean = false
                var ordinalCount: Int = 0
                while (results?.next() != null) {
                    if (results?.getInt(Constants.ENABLED_OVERRIDES_OVERRIDE_ID) == overrideId) {
                        ordinalCount++
                        if (ordinalCount == ordinal) {
                            origPriority = results!!.getInt(Constants.ENABLED_OVERRIDES_PRIORITY) // TODO: Remove !! op
                            origId = results!!.getInt(Constants.GENERIC_ID) // TODO: Remove !! op
                            gotOrig = true
                            continue
                        }
                    }
                    newPriority = results!!.getInt(Constants.ENABLED_OVERRIDES_PRIORITY) // TODO: Remove !! op
                    newId = results!!.getInt(Constants.GENERIC_ID) // TODO: Remove !! op

                    // break out because this is the one after the one we want to move down
                    if (gotOrig) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                results?.close()
            } catch (e: Exception) {
            }
            try {
                queryStatement?.close()
            } catch (e: Exception) {
            }
        }
        var statement: PreparedStatement? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                // update priorities
                if (origPriority != -1 && newPriority != -1) {
                    statement = sqlConnection?.prepareStatement(
                            ("UPDATE ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                    " SET ${Constants.ENABLED_OVERRIDES_PRIORITY}=?" +
                                    " WHERE ${Constants.GENERIC_ID}=?")
                    )
                    statement?.setInt(1, origPriority)
                    statement?.setInt(2, newId)
                    statement?.executeUpdate()
                    statement?.close()
                    statement = sqlConnection?.prepareStatement(
                            ("UPDATE ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                    " SET ${Constants.ENABLED_OVERRIDES_PRIORITY} =?" +
                                    " WHERE ${Constants.GENERIC_ID}=?")
                    )
                    statement?.setInt(1, newPriority)
                    statement?.setInt(2, origId)
                    statement?.executeUpdate()
                }
            }
        } catch (e: Exception) {
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Disable all overrides for a specified path
     *
     * @param pathID ID of path containing overrides
     * @param clientUUID UUID of client
     */
    fun disableAllOverrides(pathID: Int, clientUUID: String?) {
        var statement: PreparedStatement? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                statement = sqlConnection?.prepareStatement(
                        ("DELETE FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                " WHERE ${Constants.ENABLED_OVERRIDES_PATH_ID} = ? " +
                                " AND ${Constants.GENERIC_CLIENT_UUID} = ? ")
                )
                statement?.setInt(1, pathID)
                statement?.setString(2, clientUUID)
                statement?.execute()
                statement?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Disable all overrides for a specified path with overrideType
     *
     * @param pathID ID of path containing overrides
     * @param clientUUID UUID of client
     * @param overrideType Override type identifier
     */
    fun disableAllOverrides(pathID: Int, clientUUID: String?, overrideType: Int) {
        var statement: PreparedStatement? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                val enabledOverrides: ArrayList<Int> = ArrayList()
                enabledOverrides.add(Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_ADD)
                enabledOverrides.add(Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_REMOVE)
                enabledOverrides.add(Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM)
                enabledOverrides.add(Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM_POST_BODY)
                val overridePlaceholders: String = preparePlaceHolders(enabledOverrides.size)
                statement = sqlConnection?.prepareStatement(
                        ("DELETE FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                " WHERE ${Constants.ENABLED_OVERRIDES_PATH_ID} = ? " +
                                " AND ${Constants.GENERIC_CLIENT_UUID} = ? " +
                                " AND ${Constants.ENABLED_OVERRIDES_OVERRIDE_ID}" +
                                (if (overrideType == Constants.OVERRIDE_TYPE_RESPONSE) " NOT" else "") +
                                " IN ( $overridePlaceholders )")
                )
                statement?.setInt(1, pathID)
                statement?.setString(2, clientUUID)
                for (i in 3..enabledOverrides.size + 2) {
                    statement?.setInt(i, enabledOverrides.get(i - 3))
                }
                statement?.execute()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Returns an array of the enabled endpoints as Integer IDs
     *
     * @param pathId ID of path
     * @param clientUUID UUID of client
     * @param filters If supplied, only endpoints ending with values in filters are returned
     * @return Collection of endpoints
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun getEnabledEndpoints(pathId: Int, clientUUID: String?, filters: Array<String?>?): List<EnabledEndpoint> {
        val enabledOverrides = ArrayList<EnabledEndpoint>()
        var query: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                query = sqlConnection?.prepareStatement(
                        ("SELECT * FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                                " WHERE ${Constants.ENABLED_OVERRIDES_PATH_ID}=?" +
                                " AND ${Constants.GENERIC_CLIENT_UUID}=?" +
                                " ORDER BY ${Constants.ENABLED_OVERRIDES_PRIORITY}")
                )
                query?.setInt(1, pathId)
                query?.setString(2, clientUUID)
                results = query?.executeQuery()
                while (results?.next() != null) {
                    val endpoint: EnabledEndpoint = getPartialEnabledEndpointFromResultset(results)
                    val m: Method? = PathOverrideService.getInstance().getMethodForOverrideId(endpoint.getOverrideId())

                    // this is an errant entry.. perhaps a method got deleted from a plugin
                    // we'll also remove it from the endpoint
                    if (m == null) {
                        PathOverrideService.getInstance().removeOverride(endpoint.getOverrideId())
                        continue
                    }

                    // check filters and see if any match
                    var addOverride: Boolean = false
                    if (filters != null) {
                        for (filter: String? in filters) {
                            if (m.getMethodType().endsWith((filter)!!)) {
                                addOverride = true
                                break
                            }
                        }
                    } else {
                        // if there are no filters then we assume that the requester wants all enabled overrides
                        addOverride = true
                    }
                    if (addOverride) {
                        enabledOverrides.add(endpoint)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

        // now go through the ArrayList and get the method for all of the endpoints
        // have to do this so we don't have overlapping SQL queries
        val enabledOverridesWithMethods = ArrayList<EnabledEndpoint>()
        for (endpoint: EnabledEndpoint in enabledOverrides) {
            if (endpoint.overrideId >= 0) {
                val m = PathOverrideService.getInstance().getMethodForOverrideId(endpoint.overrideId)
                endpoint.methodInformation = m
            }
            enabledOverridesWithMethods.add(endpoint)
        }
        return enabledOverridesWithMethods
    }

    /**
     * Get the ordinal value for the last of a particular override on a path
     *
     * @param overrideId Id of the override to check
     * @param pathId Path the override is on
     * @param clientUUID UUID of the client
     * @param filters If supplied, only endpoints ending with values in filters are returned
     * @return The integer ordinal
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getCurrentMethodOrdinal(overrideId: Int, pathId: Int, clientUUID: String?, filters: Array<String?>?): Int {
        var currentOrdinal = 0
        val enabledEndpoints = getEnabledEndpoints(pathId, clientUUID, filters)
        for (enabledEndpoint: EnabledEndpoint in enabledEndpoints) {
            if (enabledEndpoint.overrideId == overrideId) {
                currentOrdinal++
            }
        }
        return currentOrdinal
    }

    /**
     * @param pathId ID of path
     * @param overrideId ID of override
     * @param ordinal Index of the enabled override to get if multiple of the same override are enabled(default is 1)
     * @param clientUUID UUID of client
     * @return EnabledEndpoint
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun getEnabledEndpoint(pathId: Int, overrideId: Int, ordinal: Int?, clientUUID: String?): EnabledEndpoint? {
        var ordinal = ordinal
        var endpoint: EnabledEndpoint? = null
        var statement: PreparedStatement? = null
        var results: ResultSet? = null
        if (ordinal == null) {
            ordinal = 1
        }

        // try to get it from the database
        try {
            sqlService?.connection.use { sqlConnection ->
                // decrease ordinal by 1 so offset works right
                ordinal--
                val queryString: String = ("SELECT * FROM ${Constants.DB_TABLE_ENABLED_OVERRIDE}" +
                        " WHERE ${Constants.ENABLED_OVERRIDES_PATH_ID}=? " +
                        " AND ${Constants.ENABLED_OVERRIDES_OVERRIDE_ID}=? " +
                        " AND ${Constants.GENERIC_CLIENT_UUID}=? " +
                        "ORDER BY ${Constants.PRIORITY} LIMIT 1 OFFSET ?")
                statement = sqlConnection?.prepareStatement(queryString)
                statement?.setInt(1, pathId)
                statement?.setInt(2, overrideId)
                statement?.setString(3, clientUUID)
                statement?.setInt(4, ordinal)
                results = statement?.executeQuery()
                while (results?.next() != null) {
                    endpoint = getPartialEnabledEndpointFromResultset(results)
                    break
                }
            }
        } catch (e: Exception) {
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
        if (endpoint != null) {
            // get the method also for a real endpoint
            if (endpoint!!.overrideId >= 0) {
                val m = PathOverrideService.getInstance().getMethodForOverrideId(endpoint!!.overrideId)
                endpoint!!.methodInformation = m
            } else if ((endpoint!!.overrideId == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_ADD
                            || endpoint!!.overrideId == Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_ADD)) {
                // set fake method info
                endpoint!!.methodInformation = Method().apply {
                    methodArgumentNames = arrayOf("key", "value")
                    methodArguments = arrayOf<Any>(String::class.java, String::class.java)
                    className = ""
                    methodName = "CUSTOM HEADER"
                    description = when (endpoint!!.overrideId) {
                        Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_ADD -> "Set a response header"
                        Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_ADD -> "Set a request header"
                        else -> ""
                    }
                }
            } else if ((endpoint!!.overrideId == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_REMOVE
                            || endpoint!!.overrideId == Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_REMOVE)) {
                // set fake method info
                endpoint!!.methodInformation = Method().apply {
                    methodArgumentNames = arrayOf("key")
                    methodArguments = arrayOf<Any>(String::class.java)
                    className = ""
                    methodName = "REMOVE HEADER"
                    description = when (endpoint!!.overrideId) {
                        Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_REMOVE -> "Remove a response header"
                        Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_REMOVE -> "Remove a request header"
                        else -> ""
                    }
                }
            } else if ((endpoint!!.overrideId == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM
                            ) || (endpoint!!.overrideId == Constants.PLUGIN_RESPONSE_OVERRIDE_CUSTOM
                            ) || (endpoint!!.overrideId == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM_POST_BODY)) {
                // set fake method info
                endpoint!!.methodInformation = Method().apply {
                    methodArgumentNames = arrayOf("response")
                    methodArguments = arrayOf<Any>(String::class.java)
                    className = ""
                    methodName = "CUSTOM"
                    description = when (endpoint!!.overrideId) {
                        Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM -> "Return a custom request"
                        Constants.PLUGIN_RESPONSE_OVERRIDE_CUSTOM -> "Return a custom response"
                        else -> ""
                    }
                }
            }
        }
        return endpoint
    }

    /**
     * This only gets half of the EnabledEndpoint from a JDBC ResultSet
     * Getting the method for the override id requires an additional SQL query and needs to be called after
     * the SQL connection is released
     *
     * @param result result to scan for endpoint
     * @return EnabledEndpoint
     * @throws Exception exception
     */
    @Throws(Exception::class)
    private fun getPartialEnabledEndpointFromResultset(result: ResultSet?): EnabledEndpoint {
        val endpoint = EnabledEndpoint()
        endpoint.id = result!!.getInt(Constants.GENERIC_ID)
        endpoint.pathId = result.getInt(Constants.ENABLED_OVERRIDES_PATH_ID)
        endpoint.overrideId = result.getInt(Constants.ENABLED_OVERRIDES_OVERRIDE_ID)
        endpoint.priority = result.getInt(Constants.ENABLED_OVERRIDES_PRIORITY)
        endpoint.repeatNumber = result.getInt(Constants.ENABLED_OVERRIDES_REPEAT_NUMBER)
        endpoint.responseCode = result.getString(Constants.ENABLED_OVERRIDES_RESPONSE_CODE)
        val args = ArrayList<Any>()
        try {
            val arr = JSONArray(result.getString(Constants.ENABLED_OVERRIDES_ARGUMENTS))
            for (x in 0 until arr.length()) {
                args.add(arr[x])
            }
        } catch (e: Exception) {
            // ignore it.. this means the entry was null/corrupt
        }
        endpoint.arguments = args.toTypedArray()
        return endpoint
    }

    /**
     * Gets an overrideID for a class name, method name
     *
     * @param className name of class
     * @param methodName name of method
     * @return override ID of method
     */
    fun getOverrideIdForMethod(className: String?, methodName: String?): Int? {
        var overrideId: Int? = null
        var query: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                query = sqlConnection?.prepareStatement(
                        ("SELECT * FROM " + Constants.DB_TABLE_OVERRIDE +
                                " WHERE " + Constants.OVERRIDE_CLASS_NAME + " = ?" +
                                " AND " + Constants.OVERRIDE_METHOD_NAME + " = ?")
                )
                query?.setString(1, className)
                query?.setString(2, methodName)
                results = query?.executeQuery()
                if (results?.next() != null) {
                    overrideId = results?.getInt(Constants.GENERIC_ID)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
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
        return overrideId
    }

    companion object {
        val logger = LoggerFactory.getLogger(OverrideService::class.java)
        var sqlService: SQLService? = null

        @get:Throws(Exception::class)
        var serviceInstance: OverrideService? = null
            get() {
                if (field == null) {
                    sqlService = SQLService.getInstance()
                    field = OverrideService()
                }
                return field
            }

        /**
         * Creates a list of placeholders for use in a PreparedStatement
         *
         * @param length number of placeholders
         * @return String of placeholders, seperated by comma
         */
        private fun preparePlaceHolders(length: Int): String {
            val builder = StringBuilder()
            var i = 0
            while (i < length) {
                builder.append("?")
                if (++i < length) {
                    builder.append(",")
                }
            }
            return builder.toString()
        }
    }
}