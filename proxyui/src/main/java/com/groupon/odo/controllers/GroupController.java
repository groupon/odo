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

import com.groupon.odo.proxylib.EditService;
import com.groupon.odo.proxylib.PathOverrideService;
import com.groupon.odo.proxylib.PluginManager;
import com.groupon.odo.proxylib.Utils;
import com.groupon.odo.proxylib.models.Group;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class GroupController {

    private static final Logger logger = LoggerFactory.getLogger(EditController.class);
    private EditService editService = EditService.getInstance();
    private PathOverrideService pathOverrideService = PathOverrideService
            .getInstance();
    private PluginManager pluginManager = PluginManager.getInstance();

    /**
     * Redirect to page
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "group", method = RequestMethod.GET)
    public String newGroupGet(Model model) {
        model.addAttribute("groups",
                pathOverrideService.findAllGroups());
        return "groups";
    }

    /**
     * Get all groups
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "api/group", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getAllGroups(Model model) {
        return Utils.getJQGridJSON(pathOverrideService.findAllGroups(), "groups");
    }

    @RequestMapping(value = "api/group/{groupId}", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getGroup(Model model, @PathVariable int groupId) throws Exception {
        return Utils.getJQGridJSON(editService.getMethodsFromGroupId(groupId, null), "methods");
    }


    /**
     * Create a new group
     *
     * @param model
     * @param groupName
     * @return
     */
    @RequestMapping(value = "api/group", method = RequestMethod.POST)
    public
    @ResponseBody
    HashMap<String, Object> createNewGroup(Model model,
                                           @RequestParam(value = "name", required = true) String groupName) {
        logger.info("groupname={}", groupName);
        pathOverrideService.addGroup(groupName);
        return Utils.getJQGridJSON(pathOverrideService.findAllGroups(), "groups");
    }

    /**
     * Redirect to a edit group page
     *
     * @param model
     * @param groupId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "group/{groupId}", method = RequestMethod.GET)
    public String editGroup(Model model, @PathVariable int groupId) throws Exception {
        model.addAttribute("groupName",
                pathOverrideService.getGroupNameFromId(groupId));
        model.addAttribute("groupId", groupId);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(pluginManager.getMethodsNotInGroup(groupId));
        model.addAttribute("methodsNotInGroup",
                json);

        return "editGroup";
    }

    /**
     * Remove a group from the groups page
     *
     * @param model
     * @param groupId
     * @return
     */
    @RequestMapping(value = "api/group/{groupId}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    List<Group> removeGroup(Model model,
                                 @PathVariable int groupId) {
        pathOverrideService.removeGroup(groupId);
        return pathOverrideService.findAllGroups();
    }

    @RequestMapping(value = "api/group/{groupId}", method = RequestMethod.POST)
    public
    @ResponseBody
    String newOverride(Model model, @PathVariable int groupId,
                       @RequestParam(value = "addOverride", required = false) String addOverride,
                       @RequestParam(value = "groupName", required = false) String groupName,
                       @RequestParam(value = "addOverrides[]", required = false) String[] addOverrides,
                       @RequestParam(value = "removeOverride", required = false) String removeOverride) {
        if (addOverride != null) {
            int lastPart = addOverride.lastIndexOf(".");
            String className = addOverride.substring(0, lastPart);
            String methodName = addOverride.substring(lastPart + 1);
            pathOverrideService.createOverride(groupId, methodName,
                    className);
        }

        if (groupName != null) {
            pathOverrideService.updateGroupName(groupName, groupId);
        }

        if (addOverrides != null) {
            for (String override : addOverrides) {
                int lastPart = override.lastIndexOf(".");
                String className = override.substring(0, lastPart);
                String methodName = override.substring(lastPart + 1);
                pathOverrideService.createOverride(groupId, methodName,
                        className);
            }
        }
        
        if (removeOverride != null) {
        	int lastPart = removeOverride.lastIndexOf(".");
            String className = removeOverride.substring(0, lastPart);
            String methodName = removeOverride.substring(lastPart + 1);
            pathOverrideService.removeOverride(groupId, methodName,
                    className);
        }
        return null;
    }

    /**
     * Remove an override from a group
     *
     * @param model
     * @param overrideId
     * @return
     */
    @RequestMapping(value = "api/group/{groupId}/{overrideId}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    String removeOverride(Model model, @PathVariable int overrideId) {
        pathOverrideService.removeOverride(overrideId);
        return null;
    }

}
