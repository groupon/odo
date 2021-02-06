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

import com.groupon.odo.proxylib.*;
import com.groupon.odo.proxylib.models.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class EditController {

    private static final Logger logger = LoggerFactory.getLogger(EditController.class);
    private ClientService clientService = ClientService.Companion.getInstance();
    private EditService editService = EditService.getInstance();

    @RequestMapping(value = "edit/{profileIdentifier}", method = RequestMethod.GET)
    public String edit2Request(Model model, @PathVariable String profileIdentifier,
                               @RequestParam(defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        Client client = ClientService.Companion.getInstance().findClient(clientUUID, profileId);

        // check to see if client is null
        if (client == null) {
            // get the default client instead
            client = ClientService.Companion.getInstance().findClient(Constants.PROFILE_CLIENT_DEFAULT_ID, profileId);
        }

        model.addAttribute("clientUUID", client.getUUID());
        model.addAttribute("clientFriendlyName", client.getFriendlyName());
        model.addAttribute("profile_id", profileId);
        model.addAttribute("isActive", client.getIsActive());
        model.addAttribute("profile_name", ProfileService.getInstance().getNamefromId(profileId));
        model.addAttribute("default_content_type", Constants.PATH_PROFILE_DEFAULT_CONTENT_TYPE);
        return "edit";
    }

    public HashMap<String, Object> getJQGridJSON(ArrayList<?> rows, String rowName) {
        HashMap<String, Object> returnVal = new HashMap<String, Object>();
        returnVal.put("records", rows.size());
        returnVal.put("total", 1);
        returnVal.put("page", "1");
        returnVal.put(rowName, rows);
        return returnVal;
    }

    /**
     * Disables all the overrides for a specific profile
     *
     * @param model
     * @param profileID
     * @param clientUUID
     * @return
     */
    @RequestMapping(value = "api/edit/disableAll", method = RequestMethod.POST)
    public
    @ResponseBody
    String disableAll(Model model, int profileID,
                      @RequestParam(defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) {
        editService.disableAll(profileID, clientUUID);
        return null;
    }

    /**
     * Makes all the repeat numbers unlimited by setting them = -1
     *
     * @param model
     * @param profileID
     * @param clientUUID
     * @return
     */
    @RequestMapping(value = "api/edit/allUnlimited", method = RequestMethod.POST)
    public
    @ResponseBody
    String allUnlimited(Model model, int profileID,
                        @RequestParam(defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) {
        editService.makeAllRepeatUnlimited(profileID, clientUUID);
        return null;
    }

    /**
     * Calls a method from editService to update the repeat number for a path
     *
     * @param model
     * @param newNum
     * @param path_id
     * @param clientUUID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/repeatNumber", method = RequestMethod.POST)
    public
    @ResponseBody
    String updateRepeatNumber(Model model, int newNum, int path_id,
                              @RequestParam(defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        logger.info("want to update repeat number of path_id={}, to newNum={}", path_id, newNum);
        editService.updateRepeatNumber(newNum, path_id, clientUUID);
        return null;
    }

    /**
     * Enables a custom response
     *
     * @param model
     * @param custom
     * @param path_id
     * @param clientUUID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/enable/custom", method = RequestMethod.POST)
    public
    @ResponseBody
    String enableCustomResponse(Model model, String custom, int path_id,
                                @RequestParam(defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        if (custom.equals("undefined"))
            return null;
        editService.enableCustomResponse(custom, path_id, clientUUID);
        return null;
    }

    /**
     * disables the responses for a given pathname and user id
     *
     * @param model
     * @param path_id
     * @param clientUUID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/disable", method = RequestMethod.POST)
    public
    @ResponseBody
    String disableResponses(Model model, int path_id, @RequestParam(defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        OverrideService.getInstance().disableAllOverrides(path_id, clientUUID);
        //TODO also need to disable custom override if there is one of those
        editService.removeCustomOverride(path_id, clientUUID);

        return null;
    }

    /**
     * removes a pathname from a profile
     *
     * @param model
     * @param pathId
     * @param profileId
     * @return
     */
    @RequestMapping(value = "api/edit/{profileId}/{pathId}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    String removePathname(Model model, @PathVariable int pathId, @PathVariable int profileId) {
        editService.removePathnameFromProfile(pathId, profileId);
        return null;
    }

    /**
     * makes the current client under the profile active
     *
     * @param model
     * @param profileId
     * @param clientUUID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/activate", method = RequestMethod.POST)
    public
    @ResponseBody
    String activateProfile(Model model,
                           @RequestParam int profileId,
                           @RequestParam(defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        clientService.updateActive(profileId, clientUUID, true);
        return null;
    }


}