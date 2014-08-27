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
package com.groupon.odo.sample;

import com.groupon.odo.plugin.PluginArguments;
import com.groupon.odo.plugin.PluginHelper;
import com.groupon.odo.plugin.ResponseOverride2;

import javax.servlet.http.HttpServletResponse;

public class Status {
    @ResponseOverride2(
            description="Return HTTP404",
            blockRequest = true)
    public static void http404(PluginArguments args) throws Exception {
        HttpServletResponse response = args.getResponse();
        response.setStatus(404);
        PluginHelper.writeResponseContent(response, "{\"error\":{\"httpCode\":404,\"message\":\"Testing http 404 Error message here\"}}");
    }
}
