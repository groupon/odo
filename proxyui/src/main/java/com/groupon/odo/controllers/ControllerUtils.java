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

import com.groupon.odo.controllers.models.Identifiers;
import com.groupon.odo.proxylib.OverrideService;
import com.groupon.odo.proxylib.PathOverrideService;
import com.groupon.odo.proxylib.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerUtils {
    private static final Logger logger = LoggerFactory.getLogger(ControllerUtils.class);

    /**
     * Obtain the path ID for a profile
     *
     * @param identifier Can be the path ID, or friendly name
     * @param profileId
     * @return
     * @throws Exception
     */
    public static Integer convertPathIdentifier(String identifier, Integer profileId) throws Exception {
        Integer pathId = -1;
        try {
            pathId = Integer.parseInt(identifier);
        } catch (NumberFormatException ne) {
            // this is OK.. just means it's not a #
            if (profileId == null)
                throw new Exception("A profileId must be specified");

            pathId = PathOverrideService.getInstance().getPathId(identifier, profileId);
        }

        return pathId;
    }

    /**
     * Obtain the profile identifier.
     *
     * @param profileIdentifier Can be profile ID, or friendly name
     * @return
     * @throws Exception
     */
    public static Integer convertProfileIdentifier(String profileIdentifier) throws Exception {
        Integer profileId = -1;
        if (profileIdentifier == null) {
            throw new Exception("A profileIdentifier must be specified");
        } else {
            try {
                profileId = Integer.parseInt(profileIdentifier);
            } catch (NumberFormatException ne) {
                // this is OK.. just means it's not a #
                // try to get it by name instead
                profileId = ProfileService.getInstance().getIdFromName(profileIdentifier);

            }
        }
        logger.info("Profile id is {}", profileId);
        return profileId;
    }

    /**
     * Obtain override ID
     *
     * @param overrideIdentifier can be the override ID or class name
     * @return
     * @throws Exception
     */
    public static Integer convertOverrideIdentifier(String overrideIdentifier) throws Exception {
        Integer overrideId = -1;

        try {
            // there is an issue with parseInt where it does not parse negative values correctly
            boolean isNegative = false;
            if (overrideIdentifier.startsWith("-")) {
                isNegative = true;
                overrideIdentifier = overrideIdentifier.substring(1);
            }
            overrideId = Integer.parseInt(overrideIdentifier);

            if (isNegative) {
                overrideId = 0 - overrideId;
            }
        } catch (NumberFormatException ne) {
            // this is OK.. just means it's not a #

            // split into two parts
            String className = null;
            String methodName = null;
            int lastDot = overrideIdentifier.lastIndexOf(".");
            className = overrideIdentifier.substring(0, lastDot);
            methodName = overrideIdentifier.substring(lastDot + 1);

            overrideId = OverrideService.Companion.getServiceInstance().getOverrideIdForMethod(className, methodName);
        }

        return overrideId;
    }

    /**
     * Obtain the IDs of profile and path as Identifiers
     *
     * @param profileIdentifier actual ID or friendly name of profile
     * @param pathIdentifier    actual ID or friendly name of path
     * @return
     * @throws Exception
     */
    public static Identifiers convertProfileAndPathIdentifier(String profileIdentifier, String pathIdentifier) throws Exception {
        Identifiers id = new Identifiers();

        Integer profileId = null;
        try {
            profileId = ControllerUtils.convertProfileIdentifier(profileIdentifier);
        } catch (Exception e) {
            // this is OK for this since it isn't always needed
        }
        Integer pathId = convertPathIdentifier(pathIdentifier, profileId);

        id.setProfileId(profileId);
        id.setPathId(pathId);

        return id;
    }
}
