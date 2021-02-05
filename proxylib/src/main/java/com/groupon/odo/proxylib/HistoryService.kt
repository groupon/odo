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

import com.groupon.odo.proxylib.models.History
import com.groupon.odo.proxylib.models.Script
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern

class HistoryService {
    private var sqlService: SQLService? = null
    private val maxHistorySize = System.getProperty("historySize", "30000").toInt()
    private var threadActive = false
    var shouldDisableHistoryWrite = false

    /**
     * Removes old entries in the history table for the given profile and client UUID
     *
     * @param profileId ID of profile
     * @param clientUUID UUID of client
     * @param limit Maximum number of history entries to remove
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun cullHistory(profileId: Int, clientUUID: String?, limit: Int) {

        // Allow only 1 delete thread to run
        if (threadActive) return

        threadActive = true
        // Create a thread so proxy will continue to work during long delete
        val t1 = Thread(Runnable {
            var statement: PreparedStatement? = null // TODO: we may be able to remove this
            try {
                sqlService?.connection.use { sqlConnection ->
                    val sqlQuery = StringBuilder().apply {
                        append("SELECT COUNT(${Constants.GENERIC_ID}) FROM ${Constants.DB_TABLE_HISTORY} ")

                        // see if profileId is set or not (-1)
                        if (profileId != -1) {
                            append("WHERE ${Constants.GENERIC_PROFILE_ID}=$profileId ")
                        }
                        if (clientUUID != null && clientUUID.compareTo("") != 0) {
                            append("AND ${Constants.GENERIC_CLIENT_UUID}='$clientUUID' ")
                        }
                        append(";")
                    }.toString()

                    val results = sqlConnection?.createStatement()?.executeQuery(sqlQuery)
                    if (results?.next() != null) {
                        if (results.getInt("COUNT(${Constants.GENERIC_ID})") < limit + 10000) {
                            return@Runnable
                        }
                    }
                    //Find the last item in the table
                    statement = sqlConnection?.prepareStatement(
                            "SELECT ${Constants.GENERIC_ID} FROM ${Constants.DB_TABLE_HISTORY}" +
                                " WHERE ${Constants.CLIENT_CLIENT_UUID} = \'$clientUUID\'" +
                                " AND ${Constants.CLIENT_PROFILE_ID} = $profileId" +
                                " ORDER BY ${Constants.GENERIC_ID} ASC LIMIT 1")
                    val resultSet = statement?.executeQuery()
                    if (resultSet?.next() != null) {
                        var currentSpot = resultSet.getInt(Constants.GENERIC_ID) + 100
                        val finalDelete = currentSpot + 10000
                        // Delete 100 items at a time until 10000 are deleted
                        // Do this so table is unlocked frequently to allow other proxy items to access it
                        while (currentSpot < finalDelete) {
                            // TODO: Why aren't we keeping track of the follow prepared statement but we are keeping track of the previous?
                            val deleteStatement = sqlConnection?.prepareStatement(
                                    "DELETE FROM ${Constants.DB_TABLE_HISTORY}"+
                                    " WHERE ${Constants.CLIENT_CLIENT_UUID} = \'$clientUUID\'" +
                                    " AND ${Constants.CLIENT_PROFILE_ID} = $profileId" +
                                    " AND ${Constants.GENERIC_ID} < $currentSpot")
                            deleteStatement?.executeUpdate()
                            currentSpot += 100
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    threadActive = false
                    statement?.close()
                } catch (e: Exception) {
                }
            }
        })
        t1.start()
    }

    /**
     * Add a history object to the history table
     *
     * @param history History object to add
     */
    fun addHistory(history: History) {
        if (shouldDisableHistoryWrite) {
            return
        }
        var statement: PreparedStatement? = null
        val dbTableHistoryValues = listOf(
                Constants.GENERIC_PROFILE_ID,
                Constants.GENERIC_CLIENT_UUID,
                Constants.HISTORY_CREATED_AT,
                Constants.GENERIC_REQUEST_TYPE,
                Constants.HISTORY_REQUEST_URL,
                Constants.HISTORY_REQUEST_PARAMS,
                Constants.HISTORY_REQUEST_POST_DATA,
                Constants.HISTORY_REQUEST_HEADERS,
                Constants.HISTORY_RESPONSE_CODE,
                Constants.HISTORY_RESPONSE_HEADERS,
                Constants.HISTORY_RESPONSE_CONTENT_TYPE,
                Constants.HISTORY_RESPONSE_DATA,
                Constants.HISTORY_ORIGINAL_REQUEST_URL,
                Constants.HISTORY_ORIGINAL_REQUEST_PARAMS,
                Constants.HISTORY_ORIGINAL_REQUEST_POST_DATA,
                Constants.HISTORY_ORIGINAL_REQUEST_HEADERS,
                Constants.HISTORY_ORIGINAL_RESPONSE_CODE,
                Constants.HISTORY_ORIGINAL_RESPONSE_HEADERS,
                Constants.HISTORY_ORIGINAL_RESPONSE_CONTENT_TYPE,
                Constants.HISTORY_ORIGINAL_RESPONSE_DATA,
                Constants.HISTORY_MODIFIED,
                Constants.HISTORY_REQUEST_SENT,
                Constants.HISTORY_REQUEST_BODY_DECODED,
                Constants.HISTORY_RESPONSE_BODY_DECODED,
                Constants.HISTORY_EXTRA_INFO,
                Constants.HISTORY_RAW_POST_DATA
        )
        try {
            sqlService?.connection.use { sqlConnection ->
                statement = sqlConnection?.prepareStatement("INSERT INTO ${Constants.DB_TABLE_HISTORY}" +
                        "(${dbTableHistoryValues.joinToString(",")})" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")
                statement?.setInt(1, history.profileId)
                statement?.setString(2, history.clientUUID)
                statement?.setString(3, history.createdAt)
                statement?.setString(4, history.requestType)
                statement?.setString(5, history.requestURL)
                statement?.setString(6, history.requestParams)
                statement?.setString(7, history.requestPostData)
                statement?.setString(8, history.requestHeaders)
                statement?.setString(9, history.responseCode)
                statement?.setString(10, history.responseHeaders)
                statement?.setString(11, history.responseContentType)
                statement?.setString(12, history.responseData)
                statement?.setString(13, history.originalRequestURL)
                statement?.setString(14, history.originalRequestParams)
                statement?.setString(15, history.originalRequestPostData)
                statement?.setString(16, history.originalRequestHeaders)
                statement?.setString(17, history.originalResponseCode)
                statement?.setString(18, history.originalResponseHeaders)
                statement?.setString(19, history.originalResponseContentType)
                statement?.setString(20, history.originalResponseData)
                statement?.setBoolean(21, history.isModified)
                statement?.setBoolean(22, history.requestSent)
                statement?.setBoolean(23, history.requestBodyDecoded)
                statement?.setBoolean(24, history.responseBodyDecoded)
                statement?.setString(25, history.extraInfoString)
                statement?.setBytes(26, history.rawPostData)
                statement?.executeUpdate()

                // cull history
                cullHistory(history.profileId, history.clientUUID, maxHistorySize)
            }
        } catch (e: Exception) {
            logger.info(e.message)
        } finally {
            try {
                statement?.close()
            } catch (e: Exception) {
            }
        }
    }

    @Throws(Exception::class)
    private fun historyFromSQLResult(result: ResultSet?, withResponseData: Boolean, scripts: Array<Script>): History {
        val history = History().apply {
            id = result!!.getInt(Constants.GENERIC_ID) // TODO: need to figure something out for this !!
            profileId = result.getInt(Constants.GENERIC_PROFILE_ID)
            clientUUID = result.getString(Constants.GENERIC_CLIENT_UUID)
            createdAt = result.getString(Constants.HISTORY_CREATED_AT)
            requestType = result.getString(Constants.GENERIC_REQUEST_TYPE)
            requestURL = result.getString(Constants.HISTORY_REQUEST_URL)
            requestParams = result.getString(Constants.HISTORY_REQUEST_PARAMS)
            requestPostData = result.getString(Constants.HISTORY_REQUEST_POST_DATA)
            requestHeaders = result.getString(Constants.HISTORY_REQUEST_HEADERS)
            responseCode = result.getString(Constants.HISTORY_RESPONSE_CODE)
            responseContentType = result.getString(Constants.HISTORY_RESPONSE_CONTENT_TYPE)
            responseHeaders = result.getString(Constants.HISTORY_RESPONSE_HEADERS)
            originalRequestHeaders = result.getString(Constants.HISTORY_ORIGINAL_REQUEST_HEADERS)
            originalRequestParams = result.getString(Constants.HISTORY_ORIGINAL_REQUEST_PARAMS)
            originalRequestPostData = result.getString(Constants.HISTORY_ORIGINAL_REQUEST_POST_DATA)
            originalRequestURL = result.getString(Constants.HISTORY_ORIGINAL_REQUEST_URL)
            originalResponseCode = result.getString(Constants.HISTORY_ORIGINAL_RESPONSE_CODE)
            originalResponseContentType = result.getString(Constants.HISTORY_ORIGINAL_RESPONSE_CONTENT_TYPE)
            originalResponseHeaders = result.getString(Constants.HISTORY_ORIGINAL_RESPONSE_HEADERS)
            isModified = result.getBoolean(Constants.HISTORY_MODIFIED)
            requestSent = result.getBoolean(Constants.HISTORY_REQUEST_SENT)
            requestBodyDecoded = result.getBoolean(Constants.HISTORY_REQUEST_BODY_DECODED)
            responseBodyDecoded = result.getBoolean(Constants.HISTORY_RESPONSE_BODY_DECODED)
            setExtraInfoFromString(result.getString(Constants.HISTORY_EXTRA_INFO))
            rawPostData = result.getBytes(Constants.HISTORY_RAW_POST_DATA)

            if (withResponseData) {
                responseData = result.getString(Constants.HISTORY_RESPONSE_DATA)
                originalResponseData = result.getString(Constants.HISTORY_ORIGINAL_RESPONSE_DATA)
            } else {
                responseData = null
                originalResponseData = null
            }
        }

        // evaluate all scripts
        for (script in scripts) {
            try {
                val gResult = GroovyService.getInstance().runGroovy(
                        script.script,
                        history.requestType,
                        history.requestURL,
                        history.requestParams,
                        history.requestPostData,
                        history.requestHeaders,
                        history.responseCode,
                        history.responseContentType,
                        history.responseHeaders,
                        history.originalRequestURL,
                        history.originalRequestParams,
                        history.originalRequestPostData,
                        history.originalRequestHeaders,
                        history.originalResponseData,
                        history.originalResponseContentType,
                        history.originalResponseHeaders,
                        history.isModified)

                // this returns a list where [0] is the status
                // and the rest is messages
                if (gResult[0].toString().toInt() == 1) {
                    history.valid = false
                    var validString = history.validationMessage
                    for (x in 1 until gResult.size) {
                        if (validString != "") {
                            validString += "\n"
                        }
                        validString += gResult[x]
                    }
                    history.validationMessage = validString
                }
            } catch (e: Exception) {
            }
        }
        return history
    }

    /**
     * Returns the number of history entries for a client
     *
     * @param profileId ID of profile
     * @param clientUUID UUID of client
     * @param searchFilter unused
     * @return number of history entries
     */
    fun getHistoryCount(profileId: Int, clientUUID: String?, searchFilter: HashMap<String?, Array<String?>?>?): Int {
        var count = 0
        var query: Statement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                val sqlQuery = StringBuilder().apply {
                    append("SELECT COUNT(${Constants.GENERIC_ID}) FROM ${Constants.DB_TABLE_HISTORY} ")

                    // see if profileId is set or not (-1)
                    if (profileId != -1) {
                        append("WHERE ${Constants.GENERIC_PROFILE_ID}=$profileId ")
                    }
                    if (clientUUID != null && clientUUID.compareTo("") != 0) {
                        append("AND ${Constants.GENERIC_CLIENT_UUID}='$clientUUID' ")
                    }
                    append(";")
                }.toString()

                logger.info("Query: {}", sqlQuery)
                results = sqlConnection?.createStatement()?.executeQuery(sqlQuery)
                results?.let {
                    count = it.getInt(1)
                }
                query?.close()
            }
        } catch (e: Exception) {
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
        return count
    }

    /**
     * Returns a set of history data ordered by most recent first
     *
     * @param profileId UUID of the profile we want history from(null for all)
     * @param clientUUID UUID of the client we want history from(null for all)
     * @param offset offset of the history data being looked for(0 for no offset), must be combined with a limit setting
     * @param limit limit of the amount of data(-1 for all data)
     * @param withResponseData false if you want returnData to be null, true otherwise
     * @param searchFilter HashMap of search filters.  This is a string(search type)/strings(regex) pair to search based on.  Search types are defined in Constants
     * @param hasMessage hasMessage
     * @return History entries found
     * @throws Exception exception
     */
    @Throws(Exception::class)
    fun getHistory(profileId: Int, clientUUID: String?, offset: Int, limit: Int, withResponseData: Boolean, searchFilter: HashMap<String?, Array<String?>>?, hasMessage: Boolean): Array<History> {
        val returnData = ArrayList<History>()
        var query: Statement? = null
        var results: ResultSet? = null
        val scripts = ScriptService.getInstance().getScripts(Constants.SCRIPT_TYPE_HISTORY)
        var totalSearchLimit = -1
        val sqlQuery = StringBuilder().apply {
            append("SELECT * FROM ${Constants.DB_TABLE_HISTORY} ")

            // see if profileId is set or not (-1)
            if (profileId != -1) {
                append("WHERE ${Constants.GENERIC_PROFILE_ID}=$profileId ")
            }
            if (clientUUID != null && clientUUID.compareTo("") != 0) {
                append("AND ${Constants.GENERIC_CLIENT_UUID}='$clientUUID' ")
            }
            append(" ORDER BY ${Constants.GENERIC_ID} DESC ")
            if (searchFilter == null && limit != -1) {
                append("LIMIT $limit ")
                append("OFFSET $offset")
            }
            if (hasMessage) {
                totalSearchLimit = 1000
            }
            append(";")
        }.toString()

        logger.info("Query: {}", sqlQuery)
        try {
            sqlService?.connection.use { sqlConnection ->
                var entriesMatched = 0
                // loop through all of the results, process filters and build an array of the results to return
                results = sqlConnection?.createStatement()?.executeQuery(sqlQuery)
                var itemsViewed = 0
                while (results?.next() != null && (itemsViewed < totalSearchLimit || totalSearchLimit == -1)) {
                    itemsViewed++
                    if (hasMessage && historyFromSQLResult(results, withResponseData, scripts).valid) {
                        continue
                    }
                    if (searchFilter != null) {
                        // iterate over searchFilter and try to match the source URI
                        if (searchFilter.containsKey(Constants.HISTORY_FILTER_SOURCE_URI)) {
                            val sourceURIFilters = searchFilter[Constants.HISTORY_FILTER_SOURCE_URI]!!
                            var numMatches = 0

                            // this will loop through all filters and count up the # of matches
                            // an item will be removed if the # of matches doesn't equal the number of filters
                            for (uriFilter in sourceURIFilters) {
                                val pattern = Pattern.compile(uriFilter)
                                val matcher = pattern.matcher(results?.getString(Constants.HISTORY_REQUEST_URL) +
                                        "?" + results?.getString(Constants.HISTORY_REQUEST_PARAMS))
                                if (matcher.find()) {
                                    numMatches++
                                }
                            }
                            if (numMatches != sourceURIFilters.size) {
                                // skip this item
                                continue
                            }
                        }
                    }
                    entriesMatched++
                    if (offset < entriesMatched) {
                        returnData.add(historyFromSQLResult(results, withResponseData, scripts))
                        if (limit != -1 && returnData.size >= limit) {
                            break
                        }
                    }
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
        return returnData.toTypedArray()
    }

    /**
     * Get history for a specific database ID
     *
     * @param id ID of history entry
     * @return History entry
     */
    fun getHistoryForID(id: Int): History? {
        var history: History? = null
        var query: PreparedStatement? = null
        var results: ResultSet? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                query = sqlConnection?.prepareStatement("SELECT * FROM ${Constants.DB_TABLE_HISTORY}" +
                        " WHERE ${Constants.GENERIC_ID}=?")?.apply { setInt(1, id) }
                logger.info("Query: {}", query.toString())
                results = query?.executeQuery()
                if (results?.next() != null) {
                    history = historyFromSQLResult(results, true, ScriptService.getInstance().getScripts(Constants.SCRIPT_TYPE_HISTORY))
                }
                query?.close()
            }
        } catch (e: Exception) {
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
        return history
    }

    /**
     * Clear history for a client
     *
     * @param profileId ID of profile
     * @param clientUUID UUID of client
     */
    fun clearHistory(profileId: Int, clientUUID: String?) {
        var query: PreparedStatement? = null
        try {
            sqlService?.connection.use { sqlConnection ->
                val sqlQuery = StringBuilder().apply {
                    append("DELETE FROM ${Constants.DB_TABLE_HISTORY} ")

                    // see if profileId is null or not (-1)
                    if (profileId != -1) {
                        append("WHERE ${Constants.GENERIC_PROFILE_ID}=$profileId")
                    }

                    // see if clientUUID is null or not
                    if (clientUUID != null && clientUUID.compareTo("") != 0) {
                        append(" AND ${Constants.GENERIC_CLIENT_UUID}='$clientUUID'")
                    }
                    append(";")
                }.toString()

                logger.info("Query: {}", sqlQuery)
                query = sqlConnection?.prepareStatement(sqlQuery)
                query?.executeUpdate()
            }
        } catch (e: Exception) {
        } finally {
            try {
                query?.close()
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HistoryService::class.java)

        var instance: HistoryService? = null
            private set
            get() {
                return if (field == null) {
                    HistoryService().apply {
                        try {
                            sqlService = SQLService.getInstance()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else field
            }
    }
}