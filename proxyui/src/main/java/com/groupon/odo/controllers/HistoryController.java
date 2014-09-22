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
import com.groupon.odo.proxylib.HistoryService;
import com.groupon.odo.proxylib.Utils;
import com.groupon.odo.proxylib.models.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
public class HistoryController {
    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

    @RequestMapping(value = "/history/{profileIdentifier}", method = RequestMethod.GET)
    public String list(Model model, @PathVariable String profileIdentifier, @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                       @RequestParam(value = "limit", defaultValue = "-1") Integer limit,
                       @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);

        model.addAttribute("profile_id", profileId);
        model.addAttribute("limit", limit);
        model.addAttribute("offset", offset);
        model.addAttribute("clientUUID", clientUUID);

        return "history";
    }

    /**
     * Retrieve the history for a profile
     *
     * @param mode
     * @param profileIdentifier
     * @param clientUUID
     * @param offset
     * @param limit
     * @param sourceURIFilters
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/history/{profileIdentifier}", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getHistory(Model mode, @PathVariable String profileIdentifier,
                                       @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID,
                                       @RequestParam(value = "offset", defaultValue = "0") int offset,
                                       @RequestParam(value = "limit", defaultValue = "-1") int limit,
                                       @RequestParam(value = "source_uri[]", required = false) String[] sourceURIFilters,
                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                       @RequestParam(value = "rows", defaultValue = "-1") int rows) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        HashMap<String, String[]> filters = new HashMap<String, String[]>();
        if (sourceURIFilters != null) {
            filters.put(Constants.HISTORY_FILTER_SOURCE_URI, sourceURIFilters);
        }

        // rows exists because jqgrid uses it.. but limit is more common
        // set limit to rows if it was set
        if (rows != -1) {
            limit = rows;
        }

        // offset id # of page(-1) * rows
        offset = (page - 1) * rows;

        History[] histories = HistoryService.getInstance().getHistory(profileId, clientUUID, offset, limit, false, filters);
        int totalRows = HistoryService.getInstance().getHistoryCount(profileId, clientUUID, filters);
        HashMap<String, Object> returnJSON = Utils.getJQGridJSON(histories, "history", offset, totalRows, limit);

        return returnJSON;
    }

    /**
     * Clear history for a profile
     *
     * @param mode
     * @param profileIdentifier
     * @param clientUUID
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/history/{profileIdentifier}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    HashMap<String, Object> deleteHistory(Model mode, @PathVariable String profileIdentifier,
                                          @RequestParam(value = "clientUUID", defaultValue = Constants.PROFILE_CLIENT_DEFAULT_ID) String clientUUID) throws Exception {
        Integer profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        HistoryService.getInstance().clearHistory(profileId, clientUUID);
        logger.info("Called");
        History[] histories = new History[0];
        HashMap<String, Object> returnData = new HashMap<String, Object>();
        returnData.put("history", histories);

        return returnData;
    }

    /**
     * Retrieve history details for an entry
     * Formatted string is optional
     *
     * @param mode
     * @param profileIdentifier
     * @param id
     * @return
     */

    @RequestMapping(value = "/api/history/{profileIdentifier}/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getHistoryForId(Model mode, @PathVariable String profileIdentifier, @PathVariable Integer id,
                                                     @RequestParam(value = "format", required = false) String formatted) {
        HashMap<String, Object> returnData = new HashMap<String, Object>();
        History history = HistoryService.getInstance().getHistoryForID(id);
        if(formatted != null) {
        	try{
        		if(formatted.equals("formattedAll")){
        			history.setFormattedResponseData(history.getResponseData());
        			history.setFormattedOriginalResponseData(history.getOriginalResponseData());
        		}else if(formatted.equals("formattedModified")){
        			history.setFormattedResponseData(history.getResponseData());
        		}else if(formatted.equals("formattedOriginal")){
        			history.setFormattedOriginalResponseData(history.getOriginalResponseData());
        		}
                returnData.put("history", history);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            returnData = new HashMap<String, Object>();
            returnData.put("history", HistoryService.getInstance().getHistoryForID(id));
        }

        return returnData;
    }

}
