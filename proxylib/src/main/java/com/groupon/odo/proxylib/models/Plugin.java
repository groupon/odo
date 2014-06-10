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
package com.groupon.odo.proxylib.models;

import com.groupon.odo.proxylib.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;

public class Plugin {
    private String path = null;
    private String statusMessage = null;
    private int status = 0;
    private int id = -1;

    public Plugin() {

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    /**
     * Sets the path to the set of plugins that this model represents
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public Object[] getClasses() {
        ArrayList<Object> returnValues = new ArrayList<Object>();

        for (String className : PluginManager.getInstance().getPluginClasses()) {
            HashMap<String, Object> classValue = new HashMap<String, Object>();
            try {
                classValue.put("name", className);
                classValue.put("methods", PluginManager.getInstance().getMethods(className));
                returnValues.add(classValue);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (java.lang.NoClassDefFoundError ncdfe) {
                // ignore
            }

        }

        return returnValues.toArray(new Object[0]);
    }
}
