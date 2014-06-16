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

import com.groupon.odo.proxylib.ClientService;
import com.groupon.odo.proxylib.Constants;
import com.groupon.odo.proxylib.ProfileService;
import com.groupon.odo.proxylib.Utils;
import com.groupon.odo.proxylib.models.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;

@Controller
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private ClientService clientService = ClientService.getInstance();

    /**
     * @param model
     * @param profileIdentifier
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "edit/{profileIdentifier}/clients", method = RequestMethod.GET)
    public String clientPage(Model model, @PathVariable String profileIdentifier) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        model.addAttribute("profile_id", profileId);
        model.addAttribute("profile_name", ProfileService.getInstance().getNamefromId(profileId));
        return "clients";
    }

    /**
     * Returns information about all clients for a profile
     *
     * @param model
     * @param profileIdentifier
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/{profileIdentifier}/clients", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getClientList(Model model,
                                          @PathVariable("profileIdentifier") String profileIdentifier) throws Exception {

        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        return Utils.getJQGridJSON(clientService.findAllClients(profileId), "clients");
    }

    /**
     * Returns information for a specific client
     *
     * @param model
     * @param profileIdentifier
     * @param clientUUID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/{profileIdentifier}/clients/{clientUUID}", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getClient(Model model,
                                      @PathVariable("profileIdentifier") String profileIdentifier,
                                      @PathVariable("clientUUID") String clientUUID) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        HashMap<String, Object> valueHash = new HashMap<String, Object>();
        valueHash.put("client", clientService.findClient(clientUUID, profileId));
        return valueHash;
    }

    /**
     * Returns a new client id for the profileIdentifier
     *
     * @param model
     * @param profileIdentifier
     * @return json with a new client_id
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/{profileIdentifier}/clients", method = RequestMethod.POST)
    public
    @ResponseBody
    HashMap<String, Object> addClient(Model model,
                                      @PathVariable("profileIdentifier") String profileIdentifier,
                                      @RequestParam(required = false) String friendlyName) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        
        // make sure client with this name does not already exist
        if (null != clientService.findClientFromFriendlyName(profileId, friendlyName)) {
        	throw new Exception("Cannot add client. Friendly name already in use.");
        }
        
        Client client = clientService.add(profileId);

        // set friendly name if it was specified
        if (friendlyName != null) {
            clientService.setFriendlyName(profileId, client.getUUID(), friendlyName);
            client.setFriendlyName(friendlyName);
        }

        HashMap<String, Object> valueHash = new HashMap<String, Object>();
        valueHash.put("client", client);
        return valueHash;
    }

    /**
     * Update properties for a specific client id
     *
     * @param model
     * @param profileIdentifier
     * @param clientUUID
     * @param active            - true false depending on if the client should be active
     * @param reset             - true to reset the state of a client(clears settings for all paths and disables the client)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/{profileIdentifier}/clients/{clientUUID}", method = RequestMethod.POST)
    public
    @ResponseBody
    HashMap<String, Object> updateClient(Model model,
                                         @PathVariable("profileIdentifier") String profileIdentifier,
                                         @PathVariable("clientUUID") String clientUUID,
                                         @RequestParam(required = false) Boolean active,
                                         @RequestParam(required = false) String friendlyName,
                                         @RequestParam(required = false) Boolean reset) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);

        if (active != null) {
            logger.info("Active: {}", active);
            clientService.updateActive(profileId, clientUUID, active);
        }

        if (friendlyName != null) {
            clientService.setFriendlyName(profileId, clientUUID, friendlyName);
        }

        if (reset != null && reset) {
            clientService.reset(profileId, clientUUID);
        }

        HashMap<String, Object> valueHash = new HashMap<String, Object>();
        valueHash.put("client", clientService.findClient(clientUUID, profileId));
        return valueHash;
    }

    @ExceptionHandler(Exception.class)
    public
    @ResponseBody
    HashMap<String, Object> handleException(Exception ex, WebRequest request, HttpServletResponse response) {
        response.setHeader("Content-Type", "application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        HashMap<String, Object> returnVal = new HashMap<String, Object>();
        returnVal.put("message", ex.getMessage());
        returnVal.put("httpCode", HttpServletResponse.SC_BAD_REQUEST);

        HashMap<String, Object> errorVal = new HashMap<String, Object>();
        errorVal.put("error", returnVal);
        return errorVal;
    }

    /**
     * Deletes a specific client id for a profile
     *
     * @param model
     * @param profileIdentifier
     * @param clientUUID
     * @return returns the table of the remaining clients or an exception if deletion failed for some reason
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/{profileIdentifier}/clients/{clientUUID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    HashMap<String, Object> deleteClient(Model model,
                                         @PathVariable("profileIdentifier") String profileIdentifier,
                                         @PathVariable("clientUUID") String clientUUID) throws Exception {
        if (clientUUID.compareTo(Constants.PROFILE_CLIENT_DEFAULT_ID) == 0)
            throw new Exception("Default client cannot be deleted");

        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        clientService.remove(profileId, clientUUID);

        HashMap<String, Object> valueHash = new HashMap<String, Object>();
        valueHash.put("clients", clientService.findAllClients(profileId));
        return valueHash;
    }
}
