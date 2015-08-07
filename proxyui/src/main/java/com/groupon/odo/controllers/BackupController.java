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

import com.groupon.odo.proxylib.BackupService;
import com.groupon.odo.proxylib.models.ViewFilters;
import com.groupon.odo.proxylib.models.backup.Backup;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.groupon.odo.proxylib.models.backup.ConfigAndProfileBackup;
import com.groupon.odo.proxylib.models.backup.SingleProfileBackup;
import flexjson.JSON;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Controller that deals with backup/restore of all data
 */
@Controller
public class BackupController {
    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);

    /**
     * Get all backup data
     *
     * @param model
     * @return
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/api/backup", method = RequestMethod.GET)
    public
    @ResponseBody
    String getBackup(Model model, HttpServletResponse response) throws Exception {
        response.addHeader("Content-Disposition", "attachment; filename=backup.json");
        response.setContentType("application/json");

        Backup backup = BackupService.getInstance().getBackupData();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

        return writer.withView(ViewFilters.Default.class).writeValueAsString(backup);
    }

    /**
     * Restore backup data
     *
     * @param fileData - json file with restore data
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/backup", method = RequestMethod.POST)
    public
    @ResponseBody
    Backup processBackup(@RequestParam("fileData") MultipartFile fileData) throws Exception {
        // Method taken from: http://spring.io/guides/gs/uploading-files/
        if (!fileData.isEmpty()) {
            try {
                byte[] bytes = fileData.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File("backup-uploaded.json")));
                stream.write(bytes);
                stream.close();

            } catch (Exception e) {
            }
        }
        File f = new File("backup-uploaded.json");
        BackupService.getInstance().restoreBackupData(new FileInputStream(f));
        return BackupService.getInstance().getBackupData();
    }

    /**
     * API call to backup active overides and active server group for a client
     * Also backs up entire odo configuration
     *
     * @param model
     * @param response
     * @param profileID Id of profile to backup
     * @param clientUUID Client Id to backup
     * @param oldExport Flag if this is exporting old configuration on a new import, used to change name of file
     * @return
     * @throws Exception
     */

    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/api/backup/profile/{profileID}/{clientUUID}", method = RequestMethod.GET)
    public
    @ResponseBody
    String getSingleProfileConfiguration(Model model, HttpServletResponse response,
                                         @PathVariable int profileID,
                                         @PathVariable String clientUUID,
                                         @RequestParam(value = "oldExport", defaultValue = "false") boolean oldExport) throws Exception {
        response.setContentType("application/json");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

        if (oldExport) {
            response.addHeader("Content-Disposition", "attachment; filename=Config_and_Profile_OLD.json");
        } else {
            response.addHeader("Content-Disposition", "attachment; filename=Config_and_Profile_Backup.json");
        }
        ConfigAndProfileBackup configAndProfileBackup = BackupService.getInstance().
            getConfigAndProfileData(profileID, clientUUID);
        return writer.withView(ViewFilters.Default.class).writeValueAsString(configAndProfileBackup);
    }

    /**
     * Set client server configuration and overrides according to backup
     *
     * @param fileData File containing profile overrides and server configuration
     * @param profileID Profile to update for client
     * @param clientUUID Client to apply overrides to
     * @param odoImport Param to determine if an odo config will be imported with the overrides import
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/backup/profile/{profileID}/{clientUUID}", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<String> processSingleProfileBackup(@RequestParam("fileData") MultipartFile fileData,
                         @PathVariable int profileID,
                         @PathVariable String clientUUID,
                         @RequestParam(value = "odoImport", defaultValue = "true") boolean odoImport) throws Exception {
        if (!fileData.isEmpty()) {
            try {
                // Read in file
                InputStream inputStream = fileData.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String singleLine;
                String fullFileString = "";
                while ((singleLine = bufferedReader.readLine()) != null) {
                    fullFileString += singleLine;
                }
                JSONObject fileBackup = new JSONObject(fullFileString);

                if (odoImport) {
                    JSONObject odoBackup = fileBackup.getJSONObject("odoBackup");
                    byte[] bytes = odoBackup.toString().getBytes();
                    // Save to second file to be used in importing odo configuration
                    BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File("backup-uploaded.json")));
                    stream.write(bytes);
                    stream.close();
                    File f = new File("backup-uploaded.json");
                    BackupService.getInstance().restoreBackupData(new FileInputStream(f));
                }

                // Get profile backup if json contained both profile backup and odo backup
                if (fileBackup.has("profileBackup")) {
                    fileBackup = fileBackup.getJSONObject("profileBackup");
                }

                // Import profile overrides
                BackupService.getInstance().setProfileFromBackup(fileBackup, profileID, clientUUID);
            } catch (Exception e) {
                try {
                    JSONArray errorArray = new JSONArray(e.getMessage());
                    return new ResponseEntity<>(errorArray.toString(), HttpStatus.BAD_REQUEST);
                } catch (Exception k) {
                    // Catch for exceptions other than ones defined in backup service
                    return new ResponseEntity<>("[{\"error\" : \"Upload Error\"}]", HttpStatus.BAD_REQUEST);
                }
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
