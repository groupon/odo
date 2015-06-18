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

import com.groupon.odo.proxylib.Constants;
import com.groupon.odo.proxylib.ProfileService;
import com.groupon.odo.proxylib.Utils;
import com.groupon.odo.proxylib.models.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Arrays;

/**
 * Handles requests for the application home page.
 */
@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);


    private ProfileService profileService = ProfileService.getInstance();


    /**
     * This is the profiles page. this is the 'regular' page when the url is typed in
     */
    @RequestMapping(value = "/profiles", method = RequestMethod.GET)
    public String list(Model model) {
        Profile profiles = new Profile();
        model.addAttribute("addNewProfile", profiles);
        model.addAttribute("version", Constants.VERSION);
        logger.info("Loading initial page");

        return "profiles";
    }

    /**
     * Obtain collection of profiles
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getList(Model model) throws Exception {
        logger.info("Using a GET request to list profiles");
        return Utils.getJQGridJSON(profileService.findAllProfiles(), "profiles");
    }

    /**
     * Add profile to database, return collection of profile data. Called when 'enter' is hit in the UI
     * instead of 'submit' button
     *
     * @param model
     * @param name
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile", method = RequestMethod.POST)
    public
    @ResponseBody
    HashMap<String, Object> addProfile(Model model, String name) throws Exception {
        logger.info("Should be adding the profile name when I hit the enter button={}", name);
        return Utils.getJQGridJSON(profileService.add(name), "profile");
    }

    /**
     * Delete a profile
     *
     * @param model
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile", method = RequestMethod.DELETE)
    public
    @ResponseBody
    HashMap<String, Object> deleteProfile(Model model, int id) throws Exception {
        profileService.remove(id);
        return Utils.getJQGridJSON(profileService.findAllProfiles(), "profiles");
    }

    /**
     * Get a profile
     *
     * @param model
     * @param profileIdentifier
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/{profileIdentifier}", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getProfile(Model model, @PathVariable String profileIdentifier) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        return Utils.getJQGridJSON(profileService.findProfile(profileId), "profile");
    }

    /**
     * Remove a profile
     *
     * @param model
     * @param profileIdentifier
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/{profileIdentifier}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    HashMap<String, Object> removeFromList(Model model, @PathVariable String profileIdentifier) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        logger.info("Want to (preRemove) DELETE on id {}", profileId);
        // TODO: make this remove all clients etc for a profile
        profileService.remove(profileId);
        return Utils.getJQGridJSON(profileService.findAllProfiles(), "profiles");
    }

    /*
    * Bulk remove profiles.
    */
    @RequestMapping(value = "api/profile/delete", method=RequestMethod.POST)
    public
    @ResponseBody
    HashMap<String, Object> removeFromList(Model model, @RequestParam String[] profileIdentifier) throws Exception {
        /* FOR EVERYTHING THAT NEEDS TO BE DELETED,
            DELETE IT.
         */
        logger.info("Want to remove the following ids: {}", Arrays.toString(profileIdentifier));
        for( int i = 0; i < profileIdentifier.length; i++ ) {
            removeFromList(model, profileIdentifier[i]);
        }

        return Utils.getJQGridJSON(profileService.findAllProfiles(), "profiles");
    }

}