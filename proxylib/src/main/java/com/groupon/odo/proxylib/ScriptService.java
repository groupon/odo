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

import com.groupon.odo.proxylib.models.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ScriptService {
    private static final Logger logger = LoggerFactory.getLogger(ScriptService.class);

    private static ScriptService _instance = null;

    public ScriptService() {

    }

    /**
     * Get instance of ScriptService
     *
     * @return
     */
    public static ScriptService getInstance() {
        if (_instance == null) {
            _instance = new ScriptService();
        }

        return _instance;
    }

    private Script scriptFromSQLResult(ResultSet result) throws Exception {
        Script script = new Script();

        script.setId(result.getInt("id"));
        script.setName(result.getString("name"));
        script.setScript(result.getString("script"));

        return script;
    }

    /**
     * Get the script for a given ID
     *
     * @param id
     * @return
     */
    public Script getScript(int id) {
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = SQLService.getInstance().getConnection();

            statement = sqlConnection.prepareStatement(
                    "SELECT * FROM " + Constants.DB_TABLE_SCRIPT +
                            " WHERE id = ?"
            );
            statement.setInt(1, id);
            results = statement.executeQuery();
            if (results.next()) {
                return scriptFromSQLResult(results);
            }
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

        return null;
    }

    /**
     * Return all scripts
     *
     * @return
     */
    public Script[] getScripts() {
        return getScripts(null);
    }

    /**
     * Return all scripts of a given type
     *
     * @param type
     * @return
     */
    public Script[] getScripts(Integer type) {
        ArrayList<Script> returnData = new ArrayList<Script>();
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = SQLService.getInstance().getConnection();

            statement = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB_TABLE_SCRIPT +
                    " ORDER BY " + Constants.GENERIC_ID);
            if (type != null) {
                statement = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB_TABLE_SCRIPT +
                        " WHERE " + Constants.SCRIPT_TYPE + "= ?" +
                        " ORDER BY " + Constants.GENERIC_ID);
                statement.setInt(1, type);
            }

            logger.info("Query: {}", statement);

            results = statement.executeQuery();
            while (results.next()) {
                returnData.add(scriptFromSQLResult(results));
            }
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

        return returnData.toArray(new Script[0]);
    }

    /**
     * Add a script
     * TODO: Make this take type as a param
     *
     * @param name   - name of script
     * @param script - script
     * @return
     * @throws Exception
     */
    public Script addScript(String name, String script) throws Exception {
        int id = -1;
        Connection sqlConnection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            sqlConnection = SQLService.getInstance().getConnection();
            statement = sqlConnection.prepareStatement(
                    "INSERT INTO " + Constants.DB_TABLE_SCRIPT
                            + "(" + Constants.SCRIPT_NAME + "," + Constants.SCRIPT_SCRIPT + "," + Constants.SCRIPT_TYPE + ")"
                            + " VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, name);
            statement.setString(2, script);
            statement.setInt(3, 0);
            statement.executeUpdate();

            // execute statement and get resultSet which will have the generated path ID as the first field
            results = statement.getGeneratedKeys();

            if (results.next()) {
                id = results.getInt(1);
            } else {
                // something went wrong
                throw new Exception("Could not add script");
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

        return this.getScript(id);
    }

    /**
     * Update the name of a script
     *
     * @param id
     * @param name
     * @return
     * @throws Exception
     */
    public Script updateName(int id, String name) throws Exception {
        PreparedStatement statement = null;

        try {
            statement = SQLService.getInstance().getConnection().prepareStatement(
                    "UPDATE " + Constants.DB_TABLE_SCRIPT +
                            " SET " + Constants.SCRIPT_NAME + " = ? " +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, name);
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

        return this.getScript(id);
    }

    /**
     * Update a script
     *
     * @param id
     * @param script
     * @return
     * @throws Exception
     */
    public Script updateScript(int id, String script) throws Exception {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = SQLService.getInstance().getConnection();
            statement = sqlConnection.prepareStatement(
                    "UPDATE " + Constants.DB_TABLE_SCRIPT +
                            " SET " + Constants.SCRIPT_SCRIPT + " = ? " +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setString(1, script);
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

        return this.getScript(id);
    }

    /**
     * Remove script for a given ID
     *
     * @param id
     * @throws Exception
     */
    public void removeScript(int id) throws Exception {
        Connection sqlConnection = null;
        PreparedStatement statement = null;

        try {
            sqlConnection = SQLService.getInstance().getConnection();
            statement = sqlConnection.prepareStatement(
                    "DELETE FROM " + Constants.DB_TABLE_SCRIPT +
                            " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setInt(1, id);
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
}
