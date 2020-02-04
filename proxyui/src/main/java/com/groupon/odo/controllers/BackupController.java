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
import com.groupon.odo.proxylib.BackupService;
import com.groupon.odo.proxylib.SQLService;
import com.groupon.odo.proxylib.models.ViewFilters;
import com.groupon.odo.proxylib.models.backup.Backup;
import com.groupon.odo.proxylib.models.backup.ConfigAndProfileBackup;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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
     * @param profileIdentifier Id of profile to backup
     * @param clientUUID        Client Id to backup
     * @param oldExport         Flag if this is exporting old configuration on a new import, used to change name of file
     * @return
     * @throws Exception
     */

    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/api/backup/profile/{profileIdentifier}/{clientUUID}", method = RequestMethod.GET)
    public
    @ResponseBody
    String getSingleProfileConfiguration(Model model, HttpServletResponse response,
                                         @PathVariable String profileIdentifier,
                                         @PathVariable String clientUUID,
                                         @RequestParam(value = "oldExport", defaultValue = "false") boolean oldExport) throws Exception {
        int profileID = ControllerUtils.convertProfileIdentifier(profileIdentifier);
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
     * @param fileData          File containing profile overrides and server configuration
     * @param profileIdentifier Profile to update for client
     * @param clientUUID        Client to apply overrides to
     * @param odoImport         Param to determine if an odo config will be imported with the overrides import
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/backup/profile/{profileIdentifier}/{clientUUID}", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<String> processSingleProfileBackup(@RequestParam("fileData") MultipartFile fileData,
                                                      @PathVariable String profileIdentifier,
                                                      @PathVariable String clientUUID,
                                                      @RequestParam(value = "odoImport", defaultValue = "true") boolean odoImport) throws Exception {
        int profileID = ControllerUtils.convertProfileIdentifier(profileIdentifier);
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

    /**
     * Backup db
     */
    @RequestMapping(value = "/api/backup/db", method = RequestMethod.GET, produces = "application/zip")
    @ResponseBody
    byte[] getBackupDb(Model model, HttpServletResponse response) throws Exception {
        response.addHeader("Content-Disposition", "attachment; filename=odo-backup-db.zip");

        File file = SQLService.getInstance().getBackupDb("odo-backup-db");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        FileInputStream fileInputStream = new FileInputStream(file);

        IOUtils.copy(fileInputStream, bufferedOutputStream);

        IOUtils.closeQuietly(bufferedOutputStream);
        IOUtils.closeQuietly(byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Restore db
     */
    @RequestMapping(value = "/api/backup/db", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<String> processDbBackup(@RequestParam("fileData") MultipartFile fileData) throws Exception {
        File file = new File("last-restored-db.zip");
        Files.write(Paths.get(file.getPath()), fileData.getBytes());

        SQLService.getInstance().restoreDb(file);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
