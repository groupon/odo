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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
}
