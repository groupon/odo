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

import java.util.List;
import org.apache.bsf.BSFManager;

public class GroovyService {
    private static GroovyService serviceInstance = null;

    public GroovyService() {

    }

    public static GroovyService getInstance() {
        if (serviceInstance == null) {
            serviceInstance = new GroovyService();
        }
        return serviceInstance;
    }

    /**
     * Runs a groovy script with a list of arguments
     * Each argument is passed to groovy as argX(arg0, arg1, arg2 etc..)
     *
     * @param script script to execute
     * @param args arguments to pass to the script
     * @return list of results
     * @throws Exception exception
     */
    public List<?> runGroovy(String script, Object... args) throws Exception {
        List<?> answer;
        BSFManager manager = new BSFManager();

        for (int x = 0; x < args.length; x++) {
            manager.declareBean("arg" + x, args[x], String.class);
        }
        answer = (List) manager.eval("groovy", "script.groovy", 0, 0, script);

        return answer;
    }
}
