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
package com.groupon.odo.tests;

import com.groupon.odo.proxylib.SQLService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring/root-context.xml"})
public class TestBase {
    @Autowired
    private ApplicationContext applicationContext;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // change the database name to the test database
        SQLService.getInstance().setDatabaseName("OdoTest");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // set back to normal production database
        SQLService.getInstance().setDatabaseName("xdb");
    }

    public void resetDatabase() throws Exception {
        // clear the database
        SQLService.getInstance().executeUpdate("DROP ALL OBJECTS");

        // make sure database schema is up to date
        SQLService.getInstance().updateSchema("/migrations");
    }
}
