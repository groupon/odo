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
import com.groupon.odo.proxylib.ProfileService;
import com.groupon.odo.proxylib.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/pathorder/{profileId}")
public class PathOrderController {

    private static final Logger logger = LoggerFactory.getLogger(EditController.class);

    private PathOverrideService pathOverrideService = PathOverrideService.getInstance();
    private EditService editService = EditService.getInstance();

    /**
     * Display the path priority page
     *
     * @param model
     * @param profileId
     * @param clientUUID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String pathOrderHome(Model model, @PathVariable int profileId,
                                @RequestParam(value = "clientUUID", defaultValue = "-1") String clientUUID) throws Exception {

        model.addAttribute("profile_id", profileId);
        model.addAttribute("profile_name", ProfileService.getInstance().getNamefromId(profileId));
        model.addAttribute("pathnames", PathOverrideService.getInstance().getPaths(profileId, clientUUID, null));

        return "pathOrder";
    }

    /**
     * Called when a path's priority is changed
     *
     * @param model
     * @param profileId
     * @param pathOrder
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public String pathOrderUpdated(Model model, @PathVariable int profileId, String pathOrder) {

        logger.info("new path order = {}", pathOrder);
        int[] intArrayPathOrder = Utils.arrayFromStringOfIntegers(pathOrder);
        pathOverrideService.updatePathOrder(profileId, intArrayPathOrder);

        return "pathOrder";
    }

}
