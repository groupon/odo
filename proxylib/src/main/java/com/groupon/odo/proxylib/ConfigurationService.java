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

import com.groupon.odo.proxylib.models.Configuration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    private static SQLService sqlService = null;
    private static ConfigurationService _instance = null;

    private ConfigurationService() {
        Constants.VERSION = getClass().getPackage().getImplementationVersion();
        logger.info("Version: {}", Constants.VERSION);
    }

    public static ConfigurationService getInstance() {
        if (_instance == null) {
            _instance = new ConfigurationService();
            try {
                sqlService = SQLService.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }

        return _instance;
    }

    /**
     * Returns true if the configuration is valid, false otherwise
     *
     * @return true if valid, false if invalid
     */
    public boolean isValid() {
        if (PluginManager.getInstance().getPlugins(true).length == 0) {
            return false;
        }

        return true;
    }

    /**
     * Get the value for a particular configuration property
     *
     * @param name - name of the property
     * @return The first value encountered or null
     */
    public Configuration getConfiguration(String name) {
        Configuration[] values = getConfigurations(name);

        if (values == null) {
            return null;
        }

        return values[0];
    }

    /**
     * Get the values for a particular configuration property
     *
     * @param name - name of the property
     * @return All values encountered or null
     */
    public Configuration[] getConfigurations(String name) {
        ArrayList<Configuration> valuesList = new ArrayList<Configuration>();

        logger.info("Getting data for {}", name);

        PreparedStatement statement = null;
        ResultSet results = null;

        try (Connection sqlConnection = sqlService.getConnection()) {

            String queryString = "SELECT * FROM " + Constants.DB_TABLE_CONFIGURATION;
            if (name != null) {
                queryString += " WHERE " + Constants.DB_TABLE_CONFIGURATION_NAME + "=?";
            }

            statement = sqlConnection.prepareStatement(queryString);
            if (name != null) {
                statement.setString(1, name);
            }

            results = statement.executeQuery();
            while (results.next()) {
                Configuration config = new Configuration();
                config.setValue(results.getString(Constants.DB_TABLE_CONFIGURATION_VALUE));
                config.setKey(results.getString(Constants.DB_TABLE_CONFIGURATION_NAME));
                config.setId(results.getInt(Constants.GENERIC_ID));
                logger.info("the configValue is = {}", config.getValue());
                valuesList.add(config);
            }
        } catch (SQLException sqe) {
            logger.info("Exception in sql");
            sqe.printStackTrace();
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

        if (valuesList.size() == 0) {
            return null;
        }

        return valuesList.toArray(new Configuration[0]);
    }

    /**
     * Add a name/value pair to the configuration table
     *
     * @param name name of configuration item
     * @param value value of configuration item
     * @throws Exception exception
     */
    public void addValue(String name, String value) throws Exception {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "INSERT INTO " + Constants.DB_TABLE_CONFIGURATION +
                    "(" + Constants.DB_TABLE_CONFIGURATION_NAME + "," + Constants.DB_TABLE_CONFIGURATION_VALUE +
                    ") VALUES (?, ?)"
            );
            statement.setString(1, name);
            statement.setString(2, value);
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
    }

    public void deleteValue(int id) throws Exception {
        PreparedStatement statement = null;
        try (Connection sqlConnection = sqlService.getConnection()) {
            statement = sqlConnection.prepareStatement(
                "DELETE FROM " + Constants.DB_TABLE_CONFIGURATION +
                    " WHERE " + Constants.GENERIC_ID + " = ?"
            );
            statement.setInt(1, id);
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
    }
}
