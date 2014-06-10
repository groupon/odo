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

package com.groupon.odo.proxylib.hostsedit;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HostsFileUtils {
    /**
     * Only works as root and should only be called by the HostsEdit RMI Server
     *
     * @param hostName
     * @throws Exception
     */
    public static void enableHost(String hostName) throws Exception {
        changeHost(hostName, true, false, false, false, false);
    }

    /**
     * Only works as root and should only be called by the HostsEdit RMI Server
     *
     * @param hostName
     * @throws Exception
     */
    public static void disableHost(String hostName) throws Exception {
        changeHost(hostName, false, true, false, false, false);
    }

    /**
     * Only works as root and should only be called by the HostsEdit RMI Server
     *
     * @param hostName
     * @throws Exception
     */
    public static void removeHost(String hostName) throws Exception {
        changeHost(hostName, false, false, true, false, false);
    }

    public static boolean isEnabled(String hostName) throws Exception {
        return changeHost(hostName, false, false, false, true, false);
    }

    public static boolean exists(String hostName) throws Exception {
        return changeHost(hostName, false, false, false, false, true);
    }

    /**
     * Only one boolean param should be true at a time for this function to return the proper results
     *
     * @param hostName
     * @param enable
     * @param disable
     * @param remove
     * @param isEnabled
     * @param exists
     * @return
     * @throws Exception
     */
    public static boolean changeHost(String hostName,
                                     boolean enable,
                                     boolean disable,
                                     boolean remove,
                                     boolean isEnabled,
                                     boolean exists) throws Exception {

        // Open the file that is the first
        // command line parameter
        File hostsFile = new File("/etc/hosts");
        FileInputStream fstream = new FileInputStream("/etc/hosts");

        File outFile = null;
        BufferedWriter bw = null;
        // only do file output for destructive operations
        if (!exists && !isEnabled) {
            outFile = File.createTempFile("HostsEdit", ".tmp");
            bw = new BufferedWriter(new FileWriter(outFile));
            System.out.println("File name: " + outFile.getPath());
        }

        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        boolean foundHost = false;
        boolean hostEnabled = false;

		/*
         * Group 1 - possible commented out host entry
		 * Group 2 - destination address
		 * Group 3 - host name
		 * Group 4 - everything else
		 */
        Pattern pattern = Pattern.compile("\\s*(#?)\\s*([^\\s]+)\\s*([^\\s]+)(.*)");
        while ((strLine = br.readLine()) != null) {

            Matcher matcher = pattern.matcher(strLine);

            // if there is a match to the pattern and the host name is the same as the one we want to set
            if (matcher.find() &&
                    matcher.group(3).toLowerCase().compareTo(hostName.toLowerCase()) == 0) {
                foundHost = true;
                if (remove) {
                    // skip this line altogether
                    continue;
                } else if (enable) {
                    // we will disregard group 2 and just set it to 127.0.0.1
                    if (!exists && !isEnabled)
                        bw.write("127.0.0.1 " + matcher.group(3) + matcher.group(4));
                } else if (disable) {
                    if (!exists && !isEnabled)
                        bw.write("# " + matcher.group(2) + " " + matcher.group(3) + matcher.group(4));
                } else if (isEnabled && matcher.group(1).compareTo("") == 0) {
                    // host exists and there is no # before it
                    hostEnabled = true;
                }
            } else {
                // just write the line back out
                if (!exists && !isEnabled)
                    bw.write(strLine);
            }

            if (!exists && !isEnabled)
                bw.write('\n');
        }

        // if we didn't find the host in the file but need to enable it
        if (!foundHost && enable) {
            // write a new host entry
            if (!exists && !isEnabled)
                bw.write("127.0.0.1 " + hostName + '\n');
        }
        // Close the input stream
        in.close();

        if (!exists && !isEnabled) {
            bw.close();
            outFile.renameTo(hostsFile);
        }

        // return false if the host wasn't found
        if (exists && !foundHost)
            return false;

        // return false if the host wasn't enabled
        if (isEnabled && !hostEnabled)
            return false;

        return true;
    }
}
