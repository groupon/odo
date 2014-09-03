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

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class manages the SQL life cycle
 */
public class SQLService {
    private static final Logger logger = LoggerFactory
            .getLogger(SQLService.class);
    private Server server = null;
    private static SQLService _instance = null;
    private static Connection _connection = null;
    private String databaseName = "h2proxydb";
    private String databaseHost = null;
    private Boolean externalDatabaseHost = false;
    private int port = 9092;

    public SQLService() {
        // check system props to see if we are using an external H2DB
        databaseHost = System.getProperty("h2Server", "localhost");
        logger.info("Database Host: {}", databaseHost);
        if (System.getProperty("h2Server") != null) {
            externalDatabaseHost = true;
        }
    }

    /**
     * Only meant to be called once
     */
    public void startServer() throws Exception {
        if (!externalDatabaseHost) {
            try {
                this.port = Utils.GetSystemPort(Constants.SYS_DB_PORT);
                server = Server.createTcpServer("-tcpPort", String.valueOf(port), "-tcpAllowOthers").start();
            } catch (SQLException e) {
                if (e.toString().contains("java.net.UnknownHostException")) {
                    logger.error("Startup failure. Potential bug in OSX & Java7. Workaround: add name of local machine to '/etc/hosts.'");
                    logger.error("Example: 127.0.0.1 MacBook");
                    throw e;
                }
            }
        }
    }

    /**
     * Shutdown the server
     *
     * @throws Exception
     */
    public void stopServer() throws Exception {
        if (!externalDatabaseHost) {
            try {
                getConnection().prepareStatement("SHUTDOWN").execute();
            } catch (Exception e) {
            }

            try {
                server.stop();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Obtain instance of the SQL Service
     *
     * @return
     * @throws Exception
     */
    public static SQLService getInstance() throws Exception {
        if (_instance == null) {
            _instance = new SQLService();
            _instance.startServer();

            try {
                Driver d = (Driver) Class.forName("org.h2.Driver")
                        .newInstance();
                DriverManager.registerDriver(d);
            } catch (Exception e) {
                logger.info("Driver not found or exception in registering driver");
                e.printStackTrace();
            }
        }
        return _instance;
    }

    /**
     * This sets the database name
     * Generally this will only be used for test purposes
     *
     * @param name
     */
    public void setDatabaseName(String name) throws Exception {
        this.databaseName = name;
        // reset connection
        if (_connection != null) {
            _connection.close();
            _connection = null;
        }
        this.stopServer();
        this.startServer();
    }

    /**
     * Obtain database connection
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        try {
            if (_connection != null) {
                // test it
                if (_connection.isValid(1)) {
                    _connection = null;

                    // try to start the server
                    try {
                        startServer();
                    } catch (Exception e) {
                        // assume it was already started and the connection was just closed
                    }
                }
            }

            if (_connection == null) {
                String connectString = "jdbc:h2:tcp://" + databaseHost + ":" + String.valueOf(port) + "/" +
                        this.databaseName + "/proxydb;MULTI_THREADED=true;AUTO_RECONNECT=TRUE;AUTOCOMMIT=ON";
                _connection = DriverManager.getConnection(connectString, "sa", "");
            }
        } catch (Exception e) {
            logger.info("getConnection got exception of type: {}",
                    e.getClass());
        }

        return _connection;
    }

    /**
     * Release database connection
     */
    public void releaseConnection() {
        try {
            if (_connection != null) {
                _connection.close();
                _connection = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * Update database schema
     *
     * @param migrationPath
     */
    public void updateSchema(String migrationPath) {
        try {
            logger.info("Updating schema... ");
            int current_version = 0;

            // first check the current schema version
            HashMap<String, Object> configuration = getFirstResult("SELECT * FROM " + Constants.DB_TABLE_CONFIGURATION +
                    " WHERE " + Constants.DB_TABLE_CONFIGURATION_NAME + " = \'" + Constants.DB_TABLE_CONFIGURATION_DATABASE_VERSION + "\'");

            if (configuration == null) {
                logger.info("Creating configuration table..");
                // create configuration table
                executeUpdate("CREATE TABLE "
                        + Constants.DB_TABLE_CONFIGURATION
                        + " (" + Constants.GENERIC_ID + " INTEGER IDENTITY,"
                        + Constants.DB_TABLE_CONFIGURATION_NAME + " VARCHAR(256),"
                        + Constants.DB_TABLE_CONFIGURATION_VALUE + " VARCHAR(1024));");

                executeUpdate("INSERT INTO " + Constants.DB_TABLE_CONFIGURATION
                        + "(" + Constants.DB_TABLE_CONFIGURATION_NAME + "," + Constants.DB_TABLE_CONFIGURATION_VALUE + ")"
                        + " VALUES (\'"
                        + Constants.DB_TABLE_CONFIGURATION_DATABASE_VERSION
                        + "\', '0');");
            } else {
                logger.info("Getting current schema version..");
                // get current version
                current_version = new Integer(configuration.get("VALUE").toString());
                logger.info("Current schema version is {}", current_version);
            }

            // loop through until we get up to the right schema version
            while (current_version < Constants.DB_CURRENT_SCHEMA_VERSION) {
                current_version++;

                // look for a schema file for this version
                logger.info("Updating to schema version {}", current_version);
                String currentFile = migrationPath + "/schema."
                        + current_version;
                Resource migFile = new ClassPathResource(currentFile);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        migFile.getInputStream()));

                String str;
                while ((str = in.readLine()) != null) {
                    // execute each line
                    if (str.length() > 0)
                        executeUpdate(str);
                }
                in.close();
            }

            // update the configuration table with the correct version
            executeUpdate("UPDATE " + Constants.DB_TABLE_CONFIGURATION
                    + " SET " + Constants.DB_TABLE_CONFIGURATION_VALUE + "='" + current_version
                    + "' WHERE " + Constants.DB_TABLE_CONFIGURATION_NAME + "='"
                    + Constants.DB_TABLE_CONFIGURATION_DATABASE_VERSION + "';");
        } catch (Exception e) {
            logger.info("Error in executeUpdate");
            e.printStackTrace();
        }
    }

    /**
     * Wrapped version of standard jdbc executeUpdate Pays attention to DB
     * locked exception and waits up to 1s
     *
     * @param query
     * @throws Exception - will throw an exception if we can never get a lock
     */
    public int executeUpdate(String query) throws Exception {
        int returnVal = 0;
        Connection sqlConnection = null;
        Statement queryStatement = null;

        try {
            sqlConnection = getConnection();
            queryStatement = sqlConnection.createStatement();
            returnVal = queryStatement.executeUpdate(query);
        } catch (Exception e) {
        } finally {
            try {
                if (queryStatement != null) queryStatement.close();
            } catch (Exception e) {
            }
        }

        return returnVal;
    }

    /**
     * Gets the first row for a query
     *
     * @param query
     * @return result or NULL
     */
    public HashMap<String, Object> getFirstResult(String query)
            throws Exception {
        HashMap<String, Object> result = null;

        Connection sqlConnection = null;
        Statement queryStatement = null;
        ResultSet results = null;
        try {
            sqlConnection = getConnection();
            queryStatement = sqlConnection.createStatement();
            results = queryStatement.executeQuery(query);
            if (results.next()) {
                result = new HashMap<String, Object>();
                String[] columns = getColumnNames(results.getMetaData());

                for (String column : columns) {
                    result.put(column, results.getObject(column));
                }
            }
        } catch (Exception e) {

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

        return result;
    }

    /**
     * Converts the given string to a clob object
     *
     * @return Clob object or NULL
     * @params string, sqlConnection object
     */

    public Clob toClob(String stringName, Connection sqlConnection) {
        Clob clobName = null;
        try {
            clobName = sqlConnection.createClob();
            clobName.setString(1, stringName);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.info("Unable to create clob object");
            e.printStackTrace();
        }
        return clobName;
    }

    /**
     * Gets all of the column names for a result meta data
     *
     * @param rsmd
     * @return
     */
    private String[] getColumnNames(ResultSetMetaData rsmd) throws Exception {
        ArrayList<String> names = new ArrayList<String>();

        // Get result set meta data
        int numColumns = rsmd.getColumnCount();

        // Get the column names; column indices start from 1
        for (int i = 1; i < numColumns + 1; i++) {
            String columnName = rsmd.getColumnName(i);

            names.add(columnName);
        }

        return names.toArray(new String[0]);
    }

    /**
     * @param getColumn  , the data that will be returned comes from this column
     * @param fromColumn , we search on this column
     * @param fromData   , searching for this specified data
     * @param tableName  , using this table
     * @return the Object of the getColumn we return Used for methods such as
     * 'getPathnameFromId' or 'getUUIDfromId'
     */
    public Object getFromTable(String getColumn, String fromColumn, Object fromData, String tableName) {
        Connection sqlConnection = null;
        Statement query = null;
        ResultSet results = null;
        try {
            sqlConnection = getConnection();
            query = sqlConnection.createStatement();
            results = query.executeQuery("SELECT * FROM " + tableName
                    + " WHERE " + fromColumn + "='" + fromData + "';");
            if (results.next()) {
                Object toReturn = results.getObject(getColumn);
                query.close();
                return toReturn;
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
        PathOverrideService.logger.info("error, get info from {}, to {}", fromColumn, getColumn);
        return null;
    }
}
