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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.groupon.odo.controllers.models.Identifiers;
import com.groupon.odo.proxylib.*;
import com.groupon.odo.proxylib.models.EndpointOverride;
import com.groupon.odo.proxylib.models.ViewFilters;

import flexjson.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class PathController {

    private static final Logger logger = LoggerFactory.getLogger(PathController.class);

    private PathOverrideService pathOverrideService = PathOverrideService.getInstance();

    @SuppressWarnings("deprecation")
	@RequestMapping(value = "/api/path", method = RequestMethod.GET)
    public
    @ResponseBody
    String getPathsForProfile(Model model, String profileIdentifier,
                                               @RequestParam(value = "typeFilter[]", required = false) String[] typeFilter,
                                               @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        int profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        List<EndpointOverride> paths = PathOverrideService.getInstance().getPaths(profileId, clientUUID, typeFilter);

        HashMap<String, Object> jqReturn = Utils.getJQGridJSON(paths, "paths");
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixInAnnotations(Object.class, ViewFilters.GetPathFilter.class);
        String[] ignorableFieldNames = { "possibleEndpoints", "enabledEndpoints" }; 
        FilterProvider filters = new SimpleFilterProvider().addFilter("Filter properties from the PathController GET", 
        		SimpleBeanPropertyFilter.serializeAllExcept(ignorableFieldNames));
        		
        ObjectWriter writer = objectMapper.writer(filters);

        return writer.writeValueAsString(jqReturn);
    }

    @RequestMapping(value = "/api/path", method = RequestMethod.POST)
    public
    @ResponseBody
    EndpointOverride addPath(Model model, String profileIdentifier,
                             @RequestParam(value = "pathName") String pathName,
                             @RequestParam(value = "path") String path,
                             @RequestParam(value = "bodyFilter", required = false) String bodyFilter,
                             @RequestParam(value = "contentType", required = false) String contentType,
                             @RequestParam(value = "requestType", required = false) Integer requestType,
                             @RequestParam(value = "groups[]", required = false) Integer[] groups,
                             @RequestParam(value = "global", defaultValue = "false") Boolean global) throws Exception {
        int profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);

        int pathId = pathOverrideService.addPathnameToProfile(profileId, pathName, path);
        if (groups != null) {
            //then adds all the groups
            for (int j = 0; j < groups.length; j++)
                pathOverrideService.AddGroupByNumber(profileId, pathId, groups[j]);
        }

        pathOverrideService.setContentType(pathId, contentType);
        pathOverrideService.setRequestType(pathId, requestType);
        pathOverrideService.setGlobal(pathId, global);

        if(bodyFilter != null) {
            pathOverrideService.setBodyFilter(pathId, bodyFilter);
        }

        return pathOverrideService.getPath(pathId);
    }

    /**
     * Get information for a specific path name/profileId or pathId
     *
     * @param model
     * @param pathIdentifier
     * @param profileIdentifier
     * @return
     */
    @RequestMapping(value = "/api/path/{pathIdentifier}", method = RequestMethod.GET)
    public
    @ResponseBody
    EndpointOverride getPath(Model model,
                             @PathVariable String pathIdentifier,
                             @RequestParam(required = false) String profileIdentifier,
                             @RequestParam(value = "typeFilter[]", required = false) String[] typeFilter,
                             @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        Identifiers identifiers = ControllerUtils.convertProfileAndPathIdentifier(profileIdentifier, pathIdentifier);

        return PathOverrideService.getInstance().getPath(identifiers.getPathId(), clientUUID, typeFilter);
    }

    /**
     * Deletes a path and returns the list of paths for the profile the path belonged to
     *
     * @param model
     * @param pathId
     * @return
     */
    @RequestMapping(value = "/api/path/{pathId}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    HashMap<String, Object> deletePath(Model model, @PathVariable int pathId,
                                       @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        EndpointOverride currentPath = PathOverrideService.getInstance().getPath(pathId);
        int profileId = currentPath.getProfileId();

        // remove the path
        logger.info("Removing path {}", pathId);
        PathOverrideService.getInstance().removePath(pathId);

        // return the remaining data
        List<EndpointOverride> paths = PathOverrideService.getInstance().getPaths(profileId, clientUUID, null);

        return Utils.getJQGridJSON(paths, "paths");
    }

    /**
     * Handles update requests for specific paths
     *
     * @param model
     * @param pathIdentifier
     * @param profileIdentifier
     * @param clientUUID
     * @param responseEnabled
     * @param requestEnabled
     * @param addOverride
     * @param enabledMoveUp
     * @param enabledMoveDown
     * @param pathName
     * @param path
     * @param bodyFilter
     * @param customResponse
     * @param customRequest
     * @param resetResponse
     * @param resetRequest
     * @param contentType
     * @param repeatNumber
     * @param global
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/path/{pathIdentifier}", method = RequestMethod.POST)
    public
    @ResponseBody
    EndpointOverride setPath(Model model, @PathVariable String pathIdentifier,
                             @RequestParam(value = "profileIdentifier", required = false) String profileIdentifier,
                             @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID,
                             @RequestParam(required = false) Boolean responseEnabled,
                             @RequestParam(required = false) Boolean requestEnabled,
                             @RequestParam(value = "addOverride", required = false) Integer addOverride,
                             @RequestParam(value = "enabledMoveUp", required = false) String enabledMoveUp,
                             @RequestParam(value = "enabledMoveDown", required = false) String enabledMoveDown,
                             @RequestParam(required = false) String pathName,
                             @RequestParam(required = false) String path,
                             @RequestParam(required = false) String bodyFilter,
                             @RequestParam(required = false) String customResponse,
                             @RequestParam(required = false) String customRequest,
                             @RequestParam(required = false) Boolean resetResponse,
                             @RequestParam(required = false) Boolean resetRequest,
                             @RequestParam(required = false) String contentType,
                             @RequestParam(required = false) Integer repeatNumber,
                             @RequestParam(required = false) Boolean global,
                             @RequestParam(value = "groups[]", required = false) Integer[] groups
    ) throws Exception {
        String decodedProfileIdentifier = null;
        if (profileIdentifier != null)
            decodedProfileIdentifier = URLDecoder.decode(profileIdentifier, "UTF-8");
        Identifiers identifiers = ControllerUtils.convertProfileAndPathIdentifier(decodedProfileIdentifier, pathIdentifier);
        Integer pathId = identifiers.getPathId();

        // update the enabled value
        if (responseEnabled != null) {
            PathOverrideService.getInstance().setResponseEnabled(pathId, responseEnabled, clientUUID);
        }

        // update the enabled value
        if (requestEnabled != null) {
            PathOverrideService.getInstance().setRequestEnabled(pathId, requestEnabled, clientUUID);
        }

        // add an override
        if (addOverride != null) {
            OverrideService.getInstance().enableOverride(addOverride, pathId, clientUUID);
        }

        // move priority of an enabled override up
        if (enabledMoveUp != null) {
            // TODO make this handle ordinals correctly
            String[] parts = enabledMoveUp.split(",");
            OverrideService.getInstance().increasePriority(Integer.parseInt(parts[0]), pathId, clientUUID);
        }

        // move priority of an enabled override down
        if (enabledMoveDown != null) {
            // TODO make this handle ordinals correctly
            String[] parts = enabledMoveDown.split(",");
            OverrideService.getInstance().decreasePriority(Integer.parseInt(parts[0]), pathId, clientUUID);
        }

        // update the name of the path
        if (pathName != null) {
            PathOverrideService.getInstance().setName(pathId, pathName);
        }

        // update the actual path
        if (path != null) {
            PathOverrideService.getInstance().setPath(pathId, path);
        }

        // update the bodyFilter
        if (bodyFilter != null) {
            PathOverrideService.getInstance().setBodyFilter(pathId, bodyFilter);
        }

        // update the custom response
        if (customResponse != null) {
            PathOverrideService.getInstance().setCustomResponse(pathId, customResponse, clientUUID);
        }

        // update the custom request
        if (customRequest != null) {
            PathOverrideService.getInstance().setCustomRequest(pathId, customRequest, clientUUID);
        }

        // clears all response settings for the specified path
        if (resetResponse != null) {
            PathOverrideService.getInstance().clearResponseSettings(pathId, clientUUID);
        }

        // clears all request settings for the specified path
        if (resetRequest != null) {
            PathOverrideService.getInstance().clearRequestSettings(pathId, clientUUID);
        }

        // sets content type
        if (contentType != null) {
            PathOverrideService.getInstance().setContentType(pathId, contentType);
        }

        // sets global
        if (global != null) {
            PathOverrideService.getInstance().setGlobal(pathId, global);
        }

        // sets repeat number
        if (repeatNumber != null) {
            EditService.getInstance().updateRepeatNumber(repeatNumber, pathId, clientUUID);
        }

        // sets groups
        if (groups != null) {
            pathOverrideService.setGroupsForPath(groups, pathId);
        }

        return PathOverrideService.getInstance().getPath(pathId, clientUUID, null);
    }

    // setOverrideArgs needs direct access to HttpServletRequest since Spring messes up array entries with commas
    @Autowired
    private HttpServletRequest httpRequest;

    /**
     * Set information for a given path id/override id combination
     *
     * @param model
     * @param pathIdentifier
     * @param overrideIdentifier
     * @param ordinal            - Index of the enabled override to edit if multiple of the same override are enabled
     * @param profileIdentifier
     * @param clientUUID
     * @param arguments
     * @param repeatNumber
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/path/{pathIdentifier}/{overrideIdentifier:.+}", method = RequestMethod.POST)
    public
    @ResponseBody
    HashMap<String, Object> setOverrideArgsWithEnabledId(Model model,
                                                         @PathVariable String pathIdentifier,
                                                         @PathVariable String overrideIdentifier,
                                                         @RequestParam(value = "ordinal", defaultValue = "1") Integer ordinal,
                                                         @RequestParam(required = false) String profileIdentifier,
                                                         @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID,
                                                         @RequestParam(value = "arguments[]", required = false) Object[] arguments,
                                                         @RequestParam(value = "repeatNumber", required = false) Integer repeatNumber) throws Exception {
        Identifiers identifiers = ControllerUtils.convertProfileAndPathIdentifier(profileIdentifier, pathIdentifier);

        // need to get overrideId for identifiers..
        Integer overrideId = ControllerUtils.convertOverrideIdentifier(overrideIdentifier);

        // set arguments
        if (arguments != null) {
            JSONSerializer serializer = new JSONSerializer();

            OverrideService.getInstance().updateArguments(overrideId, identifiers.getPathId(), ordinal, serializer.serialize(httpRequest.getParameterValues("arguments[]")), clientUUID);
        }

        // set repeat number
        if (repeatNumber != null) {
            OverrideService.getInstance().updateRepeatNumber(overrideId, identifiers.getPathId(), ordinal, repeatNumber, clientUUID);
        }

        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("enabledEndpoint", OverrideService.getInstance().getEnabledEndpoint(identifiers.getPathId(), overrideId, ordinal, clientUUID));
        return returnMap;
    }

    /**
     * Delete an override
     *
     * @param model
     * @param pathIdentifier
     * @param overrideIdentifier
     * @param ordinal-           Index of the enabled override to edit if multiple of the same override are enabled
     * @param profileIdentifier
     * @param clientUUID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/path/{pathIdentifier}/{overrideIdentifier:.+}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    HashMap<String, Object> deleteOverride(Model model,
                                           @PathVariable String pathIdentifier,
                                           @PathVariable String overrideIdentifier,
                                           @RequestParam(value = "ordinal", defaultValue = "1") Integer ordinal,
                                           @RequestParam(required = false) String profileIdentifier,
                                           @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        Identifiers identifiers = ControllerUtils.convertProfileAndPathIdentifier(profileIdentifier, pathIdentifier);

        // need to get overrideId for identifiers..
        Integer overrideId = ControllerUtils.convertOverrideIdentifier(overrideIdentifier);

        OverrideService.getInstance().removeOverride(overrideId, identifiers.getPathId(), ordinal, clientUUID);

        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("enabledEndpoint", OverrideService.getInstance().getEnabledEndpoint(identifiers.getPathId(), overrideId, ordinal, clientUUID));
        return returnMap;
    }

    /**
     * @param model
     * @param pathIdentifier     - String/Int ID of the path to edit
     * @param overrideIdentifier - String/Int ID of the override
     * @param ordinal            - Index of the enabled override to edit if multiple of the same override are enabled
     * @param clientUUID         - clientUUID(can be left out)
     * @param profileIdentifier  - profile identifier
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/path/{pathIdentifier}/{overrideIdentifier:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getOverrideInformationWithEnabledId(Model model,
                                                                @PathVariable String pathIdentifier,
                                                                @PathVariable String overrideIdentifier,
                                                                @RequestParam(value = "ordinal", defaultValue = "1") Integer ordinal,
                                                                @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID,
                                                                @RequestParam(required = false) String profileIdentifier) throws Exception {
        Identifiers identifiers = ControllerUtils.convertProfileAndPathIdentifier(profileIdentifier, pathIdentifier);

        // need to get overrideId for identifiers..
        Integer overrideId = ControllerUtils.convertOverrideIdentifier(overrideIdentifier);

        HashMap<String, Object> returnMap = new HashMap<String, Object>();

        if (overrideId != null)
            returnMap.put("enabledEndpoint", OverrideService.getInstance().getEnabledEndpoint(identifiers.getPathId(), overrideId, ordinal, clientUUID));

        return returnMap;
    }

    /*
     * TODO: These were moved from the PathOverrideController
     * Need to clean up path naming and API Conventions
     */
    // goes to the editPathname page
    @RequestMapping(value = "/pathname/{profileId}/{pathId}", method = RequestMethod.GET)
    public String editPathname(Model model,
                               @PathVariable int pathId, @PathVariable int profileId) throws Exception {
        model.addAttribute("profile_id", profileId);
        model.addAttribute("profile_name", ProfileService.getInstance().getNamefromId(profileId));
        model.addAttribute("endpoint", pathOverrideService.getPath(pathId));
        model.addAttribute("groups_in_path", pathOverrideService.getGroupsInPathProfile(profileId, pathId));
        model.addAttribute("groups_not_in_path", pathOverrideService.getGroupsNotInPathProfile(profileId, pathId));
        return "editPathname";
    }

    //this adds groups from the editPathname page
    @RequestMapping(value = "/pathname/{profileId}/{pathId}/addGroups", method = RequestMethod.POST)
    public
    @ResponseBody
    String addGroups(Model model, @PathVariable int profileId, @PathVariable int pathId, int[] group_ids) {
        for (int i = 0; i < group_ids.length; i++)
            pathOverrideService.AddGroupByNumber(profileId, pathId, group_ids[i]);
        return null;
    }

    //this removes groups from the editPathname page
    @RequestMapping(value = "/pathname/{profileId}/{pathId}/removeGroup", method = RequestMethod.POST)
    public
    @ResponseBody
    String removeGroup(Model model, @PathVariable int profileId, @PathVariable int pathId, int group_id) {
        pathOverrideService.removeGroupFromPathProfile(group_id, pathId, profileId);
        return null;
    }

}
