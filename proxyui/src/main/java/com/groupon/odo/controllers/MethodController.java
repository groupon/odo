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

import com.groupon.odo.proxylib.PathOverrideService;
import com.groupon.odo.proxylib.PluginManager;
import com.groupon.odo.proxylib.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
public class MethodController {
    private PathOverrideService pathOverrideService = PathOverrideService.getInstance();
    private PluginManager pluginManager = PluginManager.getInstance();

    /**
     * Return method information for a method override id
     *
     * @param model
     * @param methodIdentifier
     * @return
     */
    @RequestMapping(value = "/api/method/{methodIdentifier:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getPathsForProfile(Model model,
                                               @PathVariable String methodIdentifier) throws Exception {
        Integer overrideId = ControllerUtils.convertOverrideIdentifier(methodIdentifier);

        com.groupon.odo.proxylib.models.Method method = null;
        try {
            method = pathOverrideService.getMethodForOverrideId(overrideId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("method", method);
        return returnMap;
    }

    @RequestMapping(value = "api/methods", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getMethods(Model model) throws Exception {
        return Utils.INSTANCE.getJQGridJSON(pluginManager.getAllMethods(), "methods");
    }
}
