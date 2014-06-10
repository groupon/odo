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

import com.groupon.odo.proxylib.ScriptService;
import com.groupon.odo.proxylib.Utils;
import com.groupon.odo.proxylib.models.Script;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
public class ScriptController {
    /**
     * Returns script view
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/scripts", method = RequestMethod.GET)
    public String scriptView(Model model) throws Exception {
        return "script";
    }

    /**
     * Returns all scripts
     *
     * @param model
     * @param type  - optional to specify type of script to return
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/scripts", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getScripts(Model model,
                                       @RequestParam(required = false) Integer type) throws Exception {
        Script[] scripts = ScriptService.getInstance().getScripts(type);
        return Utils.getJQGridJSON(scripts, "scripts");
    }

    /**
     * Get a specific script
     *
     * @param model
     * @param scriptIdentifier
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/scripts/{scriptIdentifier}", method = RequestMethod.GET)
    public
    @ResponseBody
    Script getScript(Model model, @PathVariable String scriptIdentifier) throws Exception {
        return ScriptService.getInstance().getScript(Integer.parseInt(scriptIdentifier));
    }

    /**
     * Update a script
     *
     * @param model
     * @param scriptIdentifier
     * @param name
     * @param script
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/scripts/{scriptIdentifier}", method = RequestMethod.POST)
    public
    @ResponseBody
    Script updateScript(Model model, @PathVariable String scriptIdentifier,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String script) throws Exception {
        int scriptId = Integer.parseInt(scriptIdentifier);

        if (name != null) {
            ScriptService.getInstance().updateName(scriptId, name);
        }

        if (script != null) {
            ScriptService.getInstance().updateScript(scriptId, script);
        }

        return ScriptService.getInstance().getScript(scriptId);
    }

    /**
     * Delete a script
     *
     * @param model
     * @param scriptIdentifier
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/scripts/{scriptIdentifier}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    HashMap<String, Object> deleteScript(Model model, @PathVariable String scriptIdentifier) throws Exception {
        int scriptId = Integer.parseInt(scriptIdentifier);

        ScriptService.getInstance().removeScript(scriptId);
        return this.getScripts(model, null);
    }

    /**
     * Add a new script
     *
     * @param model
     * @param name
     * @param script
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/scripts", method = RequestMethod.POST)
    public
    @ResponseBody
    Script addScript(Model model,
                     @RequestParam(required = true) String name,
                     @RequestParam(required = true) String script) throws Exception {

        return ScriptService.getInstance().addScript(name, script);
    }
}
