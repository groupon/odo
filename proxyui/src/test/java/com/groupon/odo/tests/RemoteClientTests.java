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

import com.groupon.odo.client.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class RemoteClientTests extends ClientTests {


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setup() throws Exception {
        super.setup();
        this.client.setHostName("127.0.0.1");
        Client.setDefaultHostName("127.0.0.1");
    }

    @After
    public void revert() {
        this.client.setHostName("localhost");
        Client.setDefaultHostName("localhost");
    }

}
