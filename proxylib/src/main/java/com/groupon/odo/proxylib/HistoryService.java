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

import com.groupon.odo.proxylib.models.History;
import com.groupon.odo.proxylib.models.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryService {
    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);

    private static HistoryService _instance = null;
    private SQLService sqlService = null;
    private int maxHistorySize = 30000;

    public HistoryService() {
        maxHistorySize = Integer.parseInt(System.getProperty("historySize", "30000"));
    }

    public static HistoryService getInstance() {
        if (_instance == null) {
            _instance = new HistoryService();

            try {
                _instance.sqlService = SQLService.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return _instance;
    }

    /**
     * Removes old entries in the history table for the given profile and client UUID
     *
     * @param profileId
     * @param clientUUID
     * @param limit
     */
    public void cullHistory(int profileId, String clientUUID, int limit) throws Exception {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            String sqlQuery = "SELECT COUNT(" + Constants.GENERIC_ID + ") FROM " + Constants.DB_TABLE_HISTORY + " ";

            // see if profileId is set or not (-1)
            if (profileId != -1) {
                sqlQuery += "WHERE " + Constants.GENERIC_PROFILE_ID + "=" + profileId + " ";
            }

            if (clientUUID != null && clientUUID.compareTo("") != 0) {
                sqlQuery += "AND " + Constants.GENERIC_CLIENT_UUID + "='" + clientUUID + "' ";
            }
            sqlQuery += ";";

            Statement query = sqlConnection.createStatement();
            ResultSet results = query.executeQuery(sqlQuery);
            if (results.next()) {
                if (results.getInt("COUNT(" + Constants.GENERIC_ID + ")") < (limit + 10000)) {
                    return;
                }
            }
            statement = sqlConnection.prepareStatement("SELECT " + Constants.GENERIC_ID + " FROM " + Constants.DB_TABLE_HISTORY +
                    " WHERE " + Constants.CLIENT_CLIENT_UUID + " = \'" + clientUUID + "\'" +
                    " AND " + Constants.CLIENT_PROFILE_ID + " = " + profileId  +
                    " LIMIT 1 OFFSET 10000");

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt(Constants.GENERIC_ID);
                PreparedStatement deleteStatement = sqlConnection.prepareStatement("DELETE FROM " + Constants.DB_TABLE_HISTORY +
                        " WHERE " + Constants.CLIENT_CLIENT_UUID + " = \'" + clientUUID + "\'" +
                        " AND " + Constants.CLIENT_PROFILE_ID + " = " + profileId  +
                        " AND " + Constants.GENERIC_ID + " < " + id);
                deleteStatement.executeUpdate();
            }

        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Add a history object to the history table
     *
     * @param history - History object to add
     */
    public void addHistory(History history) {
        PreparedStatement statement = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement("INSERT INTO " + Constants.DB_TABLE_HISTORY +
                    "(" + Constants.GENERIC_PROFILE_ID + "," + Constants.GENERIC_CLIENT_UUID + "," +
                    Constants.HISTORY_CREATED_AT + "," + Constants.GENERIC_REQUEST_TYPE + "," +
                    Constants.HISTORY_REQUEST_URL + "," + Constants.HISTORY_REQUEST_PARAMS + "," +
                    Constants.HISTORY_REQUEST_POST_DATA + "," + Constants.HISTORY_REQUEST_HEADERS + "," +
                    Constants.HISTORY_RESPONSE_CODE + "," + Constants.HISTORY_RESPONSE_HEADERS + "," +
                    Constants.HISTORY_RESPONSE_CONTENT_TYPE + "," + Constants.HISTORY_RESPONSE_DATA + "," +
                    Constants.HISTORY_ORIGINAL_REQUEST_URL + "," + Constants.HISTORY_ORIGINAL_REQUEST_PARAMS + "," +
                    Constants.HISTORY_ORIGINAL_REQUEST_POST_DATA + "," + Constants.HISTORY_ORIGINAL_REQUEST_HEADERS + "," +
                    Constants.HISTORY_ORIGINAL_RESPONSE_CODE + "," + Constants.HISTORY_ORIGINAL_RESPONSE_HEADERS + "," +
                    Constants.HISTORY_ORIGINAL_RESPONSE_CONTENT_TYPE + "," + Constants.HISTORY_ORIGINAL_RESPONSE_DATA + "," +
                    Constants.HISTORY_MODIFIED + "," + Constants.HISTORY_REQUEST_SENT + "," +
                    Constants.HISTORY_REQUEST_BODY_DECODED + "," + Constants.HISTORY_RESPONSE_BODY_DECODED + ")" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            statement.setInt(1, history.getProfileId());
            statement.setString(2, history.getClientUUID());
            statement.setString(3, history.getCreatedAt());
            statement.setString(4, history.getRequestType());
            statement.setString(5, history.getRequestURL());
            statement.setString(6, history.getRequestParams());
            statement.setString(7, history.getRequestPostData());
            statement.setString(8, history.getRequestHeaders());
            statement.setString(9, history.getResponseCode());
            statement.setString(10, history.getResponseHeaders());
            statement.setString(11, history.getResponseContentType());
            statement.setString(12, history.getResponseData());
            statement.setString(13, history.getOriginalRequestURL());
            statement.setString(14, history.getOriginalRequestParams());
            statement.setString(15, history.getOriginalRequestPostData());
            statement.setString(16, history.getOriginalRequestHeaders());
            statement.setString(17, history.getOriginalResponseCode());
            statement.setString(18, history.getOriginalResponseHeaders());
            statement.setString(19, history.getOriginalResponseContentType());
            statement.setString(20, history.getOriginalResponseData());
            statement.setBoolean(21, history.isModified());
            statement.setBoolean(22, history.getRequestSent());
            statement.setBoolean(23, history.getRequestBodyDecoded());
            statement.setBoolean(24, history.getResponseBodyDecoded());
            statement.executeUpdate();

            // cull history
            cullHistory(history.getProfileId(), history.getClientUUID(), maxHistorySize);

        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
    }

    private History historyFromSQLResult(ResultSet result, boolean withResponseData, Script[] scripts) throws Exception {
        History history = new History();
        history.setId(result.getInt(Constants.GENERIC_ID));
        history.setProfileId(result.getInt(Constants.GENERIC_PROFILE_ID));
        history.setClientUUID(result.getString(Constants.GENERIC_CLIENT_UUID));
        history.setCreatedAt(result.getString(Constants.HISTORY_CREATED_AT));
        history.setRequestType(result.getString(Constants.GENERIC_REQUEST_TYPE));
        history.setRequestURL(result.getString(Constants.HISTORY_REQUEST_URL));
        history.setRequestParams(result.getString(Constants.HISTORY_REQUEST_PARAMS));
        history.setRequestPostData(result.getString(Constants.HISTORY_REQUEST_POST_DATA));
        history.setRequestHeaders(result.getString(Constants.HISTORY_REQUEST_HEADERS));
        history.setResponseCode(result.getString(Constants.HISTORY_RESPONSE_CODE));
        history.setResponseContentType(result.getString(Constants.HISTORY_RESPONSE_CONTENT_TYPE));
        history.setResponseHeaders(result.getString(Constants.HISTORY_RESPONSE_HEADERS));
        history.setOriginalRequestHeaders(result.getString(Constants.HISTORY_ORIGINAL_REQUEST_HEADERS));
        history.setOriginalRequestParams(result.getString(Constants.HISTORY_ORIGINAL_REQUEST_PARAMS));
        history.setOriginalRequestPostData(result.getString(Constants.HISTORY_ORIGINAL_REQUEST_POST_DATA));
        history.setOriginalRequestURL(result.getString(Constants.HISTORY_ORIGINAL_REQUEST_URL));
        history.setOriginalResponseCode(result.getString(Constants.HISTORY_ORIGINAL_RESPONSE_CODE));
        history.setOriginalResponseContentType(result.getString(Constants.HISTORY_ORIGINAL_RESPONSE_CONTENT_TYPE));
        history.setOriginalResponseHeaders(result.getString(Constants.HISTORY_ORIGINAL_RESPONSE_HEADERS));
        history.setModified(result.getBoolean(Constants.HISTORY_MODIFIED));
        history.setRequestSent(result.getBoolean(Constants.HISTORY_REQUEST_SENT));
        history.setRequestBodyDecoded(result.getBoolean(Constants.HISTORY_REQUEST_BODY_DECODED));
        history.setResponseBodyDecoded(result.getBoolean(Constants.HISTORY_RESPONSE_BODY_DECODED));

        if (withResponseData) {
            history.setResponseData(result.getString(Constants.HISTORY_RESPONSE_DATA));
            history.setOriginalResponseData(result.getString(Constants.HISTORY_ORIGINAL_RESPONSE_DATA));
        } else {
            history.setResponseData(null);
            history.setOriginalResponseData(null);
        }


        // evaluate all scripts
        for (Script script : scripts) {
            try {
                List<?> gresult = GroovyService.getInstance().runGroovy(script.getScript(), history.getRequestType(),
                        history.getRequestURL(),
                        history.getRequestParams(),
                        history.getRequestPostData(),
                        history.getRequestHeaders(),
                        history.getResponseCode(),
                        history.getResponseContentType(),
                        history.getResponseHeaders(),
                        history.getOriginalRequestURL(),
                        history.getOriginalRequestParams(),
                        history.getOriginalRequestPostData(),
                        history.getOriginalRequestHeaders(),
                        history.getOriginalResponseData(),
                        history.getOriginalResponseContentType(),
                        history.getOriginalResponseHeaders(),
                        history.isModified());

                // this returns a list where [0] is the status
                // and the rest is messages
                if (Integer.parseInt(gresult.get(0).toString()) == 1) {
                    history.setValid(false);

                    String validString = history.getValidationMessage();

                    for (int x = 1; x < gresult.size(); x++) {
                        if (!validString.equals(""))
                            validString += "\n";

                        validString += gresult.get(x);
                    }

                    history.setValidationMessage(validString);
                }
            } catch (Exception e) {

            }
        }

        return history;
    }

    public int getHistoryCount(int profileId, String clientUUID, HashMap<String, String[]> searchFilter) {
        int count = 0;
        Statement query = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {

            String sqlQuery = "SELECT COUNT(" + Constants.GENERIC_ID + ") FROM " + Constants.DB_TABLE_HISTORY + " ";

            // see if profileId is set or not (-1)
            if (profileId != -1) {
                sqlQuery += "WHERE " + Constants.GENERIC_PROFILE_ID + "=" + profileId + " ";
            }

            if (clientUUID != null && clientUUID.compareTo("") != 0) {
                sqlQuery += "AND " + Constants.GENERIC_CLIENT_UUID + "='" + clientUUID + "' ";
            }

            sqlQuery += ";";

            logger.info("Query: {}", sqlQuery);

            query = sqlConnection.createStatement();
            results = query.executeQuery(sqlQuery);
            if (results.next()) {
                count = results.getInt(1);
            }
            query.close();
        } catch (Exception e) {

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

        return count;
    }

    /**
     * Returns a set of history data ordered by most recent first
     *
     * @param profileId        - UUID of the profile we want history from(null for all)
     * @param clientUUID       - UUID of the client we want history from(null for all)
     * @param offset           - offset of the history data being looked for(0 for no offset), must be combined with a limit setting
     * @param limit            - limit of the amount of data(-1 for all data)
     * @param withResponseData - false if you want returnData to be null, true otherwise
     * @param searchFilter     - HashMap of search filters.  This is a string(search type)/strings(regex) pair to search based on.  Search types are defined in Constants
     * @return
     */
    public History[] getHistory(int profileId, String clientUUID, int offset, int limit, boolean withResponseData, HashMap<String, String[]> searchFilter) throws Exception {
        ArrayList<History> returnData = new ArrayList<History>();
        Statement query = null;
        ResultSet results = null;

        Script[] scripts = ScriptService.getInstance().getScripts(Constants.SCRIPT_TYPE_HISTORY);
        String sqlQuery = "SELECT * FROM " + Constants.DB_TABLE_HISTORY + " ";

        // see if profileId is set or not (-1)
        if (profileId != -1) {
            sqlQuery += "WHERE " + Constants.GENERIC_PROFILE_ID + "=" + profileId + " ";
        }

        if (clientUUID != null && clientUUID.compareTo("") != 0) {
            sqlQuery += "AND " + Constants.GENERIC_CLIENT_UUID + "='" + clientUUID + "' ";
        }

        sqlQuery += " ORDER BY " + Constants.GENERIC_ID + " DESC ";

        if (searchFilter == null && limit != -1) {
            sqlQuery += "LIMIT " + limit + " ";
            sqlQuery += "OFFSET " + offset;
        }

        sqlQuery += ";";

        logger.info("Query: {}", sqlQuery);

        try (Connection sqlConnection = sqlService.getConnection()) {
            int entriesMatched = 0;
            // loop through all of the results, process filters and build an array of the results to return
            query = sqlConnection.createStatement();
            results = query.executeQuery(sqlQuery);
            while (results.next()) {
                if (searchFilter != null) {
                    // iterate over searchFilter and try to match the source URI
                    if (searchFilter.containsKey(Constants.HISTORY_FILTER_SOURCE_URI)) {
                        String[] sourceURIFilters = searchFilter.get(Constants.HISTORY_FILTER_SOURCE_URI);

                        int numMatches = 0;

                        // this will loop through all filters and count up the # of matches
                        // an item will be removed if the # of matches doesn't equal the number of filters
                        for (String uriFilter : sourceURIFilters) {
                            Pattern pattern = Pattern.compile(uriFilter);
                            Matcher matcher = pattern.matcher(results.getString(Constants.HISTORY_REQUEST_URL) +
                                    "?" + results.getString(Constants.HISTORY_REQUEST_PARAMS));

                            if (matcher.find()) {
                                numMatches++;
                            }
                        }

                        if (numMatches != sourceURIFilters.length) {
                            // skip this item
                            continue;
                        }
                    }
                }

                entriesMatched++;

                if (offset < entriesMatched) {
                    returnData.add(historyFromSQLResult(results, withResponseData, scripts));
                    if (limit != -1 && returnData.size() >= limit) {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            throw e;
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

        return returnData.toArray(new History[0]);
    }

    /**
     * Get history for a specific database ID
     *
     * @param id
     * @return
     */
    public History getHistoryForID(int id) {
        History history = null;
        PreparedStatement query = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            query = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB_TABLE_HISTORY +
                    " WHERE " + Constants.GENERIC_ID + "=?");
            query.setInt(1, id);

            logger.info("Query: {}", query.toString());
            results = query.executeQuery();
            if (results.next()) {
                history = historyFromSQLResult(results, true, ScriptService.getInstance().getScripts(Constants.SCRIPT_TYPE_HISTORY));
            }
            query.close();
        } catch (Exception e) {
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
        return history;
    }

    public void clearHistory(int profileId, String clientUUID) {
        PreparedStatement query = null;

        try (Connection sqlConnection = sqlService.getConnection()) {
            String sqlQuery = "DELETE FROM " + Constants.DB_TABLE_HISTORY + " ";

            // see if profileId is null or not (-1)
            if (profileId != -1) {
                sqlQuery += "WHERE " + Constants.GENERIC_PROFILE_ID + "=" + profileId;
            }

            // see if clientUUID is null or not
            if (clientUUID != null && clientUUID.compareTo("") != 0) {
                sqlQuery += " AND " + Constants.GENERIC_CLIENT_UUID + "='" + clientUUID + "'";
            }

            sqlQuery += ";";

            logger.info("Query: {}", sqlQuery);
            query = sqlConnection.prepareStatement(sqlQuery);
            query.executeUpdate();
        } catch (Exception e) {
        } finally {
            try {
                if (query != null) query.close();
            } catch (Exception e) {
            }
        }
    }
}
