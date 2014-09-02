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
package com.groupon.odo.proxylib.models;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * This class stores the view filters for Jackson JSON output
 * <p/>
 * When the ObjectMapper is used to write json output then items annotated with @JsonView(ViewFilters.BackupIgnore.class) will be skipped
 */
public class ViewFilters {
    public static class Default {
    }

    public static class BackupIgnore {
    }
    
    @JsonFilter("Filter properties from the PathController GET")  
    public static class GetPathFilter {
    }
}
