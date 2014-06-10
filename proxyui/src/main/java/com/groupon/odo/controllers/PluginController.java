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
package com.groupon.odo.controllers;

import com.groupon.odo.proxylib.PluginManager;
import com.groupon.odo.proxylib.models.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;


@Controller
public class PluginController {
    private static final Logger logger = LoggerFactory.getLogger(PluginController.class);

    /**
     * Obtain plugin information
     *
     * @return
     */
    @RequestMapping(value = "/api/plugins", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getPluginInformation() {
        return pluginInformation();
    }


    private HashMap<String, Object> pluginInformation() {
        HashMap<String, Object> returnVal = new HashMap<String, Object>();

        Plugin[] plugins = PluginManager.getInstance().getPlugins(false);
        returnVal.put("plugins", plugins);

        return returnVal;
    }

    /**
     * Add a plugin path
     *
     * @param model
     * @param add
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/plugins", method = RequestMethod.POST)
    public
    @ResponseBody
    HashMap<String, Object> addPluginPath(Model model, Plugin add) throws Exception {
        PluginManager.getInstance().addPluginPath(add.getPath());

        return pluginInformation();
    }

    /**
     * Reload plugins
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/api/plugins/reload", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> reloadPlugins(Model model) {
        try {
            PluginManager.destroy();
        } catch (Exception e) {
        }

        return pluginInformation();
    }

    /**
     * Delete a plugin path
     *
     * @param model
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/plugins/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    HashMap<String, Object> deletePluginPath(Model model, @PathVariable Integer id) {
        try {
            PluginManager.getInstance().deletePluginPath(id);
        } catch (Exception e) {
        }

        return pluginInformation();
    }

    /**
     * Get class information
     *
     * @param model
     * @param id
     * @param className
     * @return
     */
    @RequestMapping(value = "/api/plugins/{id}/{className:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getClassInformation(Model model, @PathVariable Integer id, @PathVariable String className) {
        HashMap<String, Object> returnValues = new HashMap<String, Object>();

        try {
            returnValues.put("methods", PluginManager.getInstance().getMethods(className));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return returnValues;
    }

    /**
     * Fetches a resource from a plugin jar with the given pluginName
     * Returns 404 if the resource cannot be found
     */
    @RequestMapping(value = "/api/resource/{pluginName}/**", method = RequestMethod.GET)
    public
    @ResponseBody
    byte[] getResourceFile(Model model, @PathVariable String pluginName, HttpServletRequest request) throws Exception {
        try {
            String url = (String) request.getAttribute(
                    HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String fileName = url.replaceFirst("/api/resource/" + pluginName + "/", "");

            return PluginManager.getInstance().getResource(pluginName, fileName);
        } catch (Exception e) {
            throw new ResourceNotFoundException();
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
    }
}
