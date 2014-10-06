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

import com.groupon.odo.proxylib.ClientService;
import com.groupon.odo.proxylib.ProfileService;
import com.groupon.odo.proxylib.ServerRedirectService;
import com.groupon.odo.proxylib.Utils;
import com.groupon.odo.proxylib.hostsedit.rmi.Client;
import com.groupon.odo.proxylib.models.ServerGroup;
import com.groupon.odo.proxylib.models.ServerRedirect;
import net.lightbody.bmp.proxy.selenium.KeyStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


@Controller
public class ServerMappingController {
    private static final Logger logger = LoggerFactory.getLogger(ServerMappingController.class);

    /**
     * Adds a redirect URL to the specified profile ID
     *
     * @param model
     * @param profileId
     * @param srcUrl
     * @param destUrl
     * @param hostHeader
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/server", method = RequestMethod.POST)
    public
    @ResponseBody
    ServerRedirect addRedirectToProfile(Model model,
                                        @RequestParam(value = "profileId", required = false) Integer profileId,
                                        @RequestParam(value = "profileIdentifier", required = false) String profileIdentifier,
                                        @RequestParam(value = "srcUrl", required = true) String srcUrl,
                                        @RequestParam(value = "destUrl", required = true) String destUrl,
                                        @RequestParam(value = "clientUUID", required = true) String clientUUID,
                                        @RequestParam(value = "hostHeader", required = false) String hostHeader) throws Exception {
        if (profileId == null && profileIdentifier == null) {
            throw new Exception("profileId required");
        }
        if (profileId == null && profileIdentifier != null) {
            profileId = ProfileService.getInstance().getIdFromName(profileIdentifier);
        }

        int clientId = ClientService.getInstance().findClient(clientUUID, profileId).getId();

        int redirectId = ServerRedirectService.getInstance().addServerRedirectToProfile("", srcUrl, destUrl, hostHeader,
                profileId, clientId);
        return ServerRedirectService.getInstance().getRedirect(redirectId);
    }

    /**
     * Redirect URL to the specified profile ID
     *
     * @param model
     * @param profileId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/server", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getjqRedirects(Model model,
                                           @RequestParam(value = "profileId", required = false) Integer profileId,
                                           @RequestParam(value = "clientUUID", required = true) String clientUUID,
                                           @RequestParam(value = "profileIdentifier", required = false) String profileIdentifier) throws Exception {
        if (profileId == null && profileIdentifier == null) {
            throw new Exception("profileId required");
        }
        if (profileId == null && profileIdentifier != null) {
            profileId = ProfileService.getInstance().getIdFromName(profileIdentifier);
        }
        int clientId = ClientService.getInstance().findClient(clientUUID, profileId).getId();

        HashMap<String, Object> returnJson = Utils.getJQGridJSON(ServerRedirectService.getInstance().tableServers(clientId), "servers");
        returnJson.put("hostEditor", Client.isAvailable());
        return returnJson;
    }

    /**
     * Obtains the collection of server groups defined for a profile
     *
     * @param model
     * @param profileId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/servergroup", method = RequestMethod.GET)
    public
    @ResponseBody
    HashMap<String, Object> getServerGroups(Model model,
                                            @RequestParam(value = "profileId", required = false) Integer profileId,
                                            @RequestParam(value = "search", required = false) String search,
                                            @RequestParam(value = "profileIdentifier", required = false) String profileIdentifier) throws Exception {
        if (profileId == null && profileIdentifier == null) {
            throw new Exception("profileId required");
        }
        if (profileId == null && profileIdentifier != null) {
            profileId = ProfileService.getInstance().getIdFromName(profileIdentifier);
        }

        List<ServerGroup> serverGroups = ServerRedirectService.getInstance().tableServerGroups(profileId);

        if (search != null) {
            Iterator<ServerGroup> iterator = serverGroups.iterator();
            while (iterator.hasNext()) {
                ServerGroup serverGroup = iterator.next();
                if (!serverGroup.getName().toLowerCase().contains(search.toLowerCase())) {
                    iterator.remove();
                }
            }
        }
        HashMap<String, Object> returnJson = Utils.getJQGridJSON(serverGroups, "servergroups");
        return returnJson;
    }

    /**
     * Obtains a server group
     *
     * @param model
     * @param profileId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/servergroup/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    ServerGroup getServerGroup(Model model,
                               @PathVariable int id,
                               @RequestParam(value = "profileId", required = false) Integer profileId,
                               @RequestParam(value = "profileIdentifier", required = false) String profileIdentifier) throws Exception {
        if (profileId == null && profileIdentifier == null) {
            throw new Exception("profileId required");
        }
        if (profileId == null && profileIdentifier != null) {
            profileId = ProfileService.getInstance().getIdFromName(profileIdentifier);
        }

        return ServerRedirectService.getInstance().getServerGroup(id, profileId);
    }

    /**
     * Create a new server group for a profile
     *
     * @param model
     * @param profileId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/servergroup", method = RequestMethod.POST)
    public
    @ResponseBody
    ServerGroup createServerGroup(Model model,
                                  @RequestParam(value = "name") String name,
                                  @RequestParam(value = "profileId", required = false) Integer profileId,
                                  @RequestParam(value = "profileIdentifier", required = false) String profileIdentifier) throws Exception {
        if (profileId == null && profileIdentifier == null) {
            throw new Exception("profileId required");
        }
        if (profileId == null && profileIdentifier != null) {
            profileId = ProfileService.getInstance().getIdFromName(profileIdentifier);
        }
        int groupId = ServerRedirectService.getInstance().addServerGroup(name, profileId);
        return ServerRedirectService.getInstance().getServerGroup(groupId, profileId);
    }

    /**
     * Updates the src url in the server redirects
     *
     * @param model
     * @param id
     * @param enabled
     * @return
     * @throws Exception
     */
    @ExceptionHandler(Exception.class)
    @RequestMapping(value = "api/edit/server/{id}", method = RequestMethod.POST)
    public
    @ResponseBody
    ServerRedirect updateServer(Model model, @PathVariable int id,
                                @RequestParam(required = false) Boolean enabled) throws Exception {
        logger.info("updating Server");
        if (enabled != null) {
            if (enabled) {
                Client.enableHost(ServerRedirectService.getInstance().getRedirect(id).getSrcUrl());
            } else {
                Client.disableHost(ServerRedirectService.getInstance().getRedirect(id).getSrcUrl());
            }
        }
        return ServerRedirectService.getInstance().getRedirect(id);
    }


    /**
     * Updates for a server group
     *
     * @param model
     * @param id
     * @param name
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/servergroup/{id}", method = RequestMethod.POST)
    public
    @ResponseBody
    ServerGroup updateServerGroup(Model model,
                                  @PathVariable int id,
                                  @RequestParam(required = false) Integer profileId,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) Boolean activate,
                                  @RequestParam(required = false) String clientUUID,
                                  @RequestParam(value = "profileIdentifier", required = false) String profileIdentifier) throws Exception {
        if (profileId == null && profileIdentifier == null) {
            throw new Exception("profileId required");
        }
        if (profileId == null && profileIdentifier != null) {
            profileId = ProfileService.getInstance().getIdFromName(profileIdentifier);
        }
        if (name != null) {
            ServerRedirectService.getInstance().setGroupName(name, id);
        }

        if (activate != null) {
            if (clientUUID == null) {
                throw new Exception("clientUUID required");
            }

            int clientId = ClientService.getInstance().findClient(clientUUID, profileId).getId();

            if (activate == true) {
                ServerRedirectService.getInstance().activateServerGroup(id, clientId);
            } else {
                ServerRedirectService.getInstance().activateServerGroup(0, clientId);
            }
        }

        return ServerRedirectService.getInstance().getServerGroup(id, profileId);
    }

    /**
     * Updates the src url in the server redirects
     *
     * @param model
     * @param id
     * @param srcUrl
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/server/{id}/src", method = RequestMethod.POST)
    public
    @ResponseBody
    ServerRedirect updateSrcRedirectUrl(Model model, @PathVariable int id, String srcUrl) throws Exception {
        ServerRedirectService.getInstance().setSourceUrl(srcUrl, id);
        return ServerRedirectService.getInstance().getRedirect(id);
    }

    /**
     * Updates the dest URL in the server redirects
     *
     * @param model
     * @param id
     * @param destUrl
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/server/{id}/dest", method = RequestMethod.POST)
    public
    @ResponseBody
    ServerRedirect updateDestRedirectUrl(Model model, @PathVariable int id, String destUrl) throws Exception {
        ServerRedirectService.getInstance().setDestinationUrl(destUrl, id);
        return ServerRedirectService.getInstance().getRedirect(id);
    }

    /**
     * Updates the dest host header in the server redirects
     *
     * @param model
     * @param id
     * @param hostHeader
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/server/{id}/host", method = RequestMethod.POST)
    public
    @ResponseBody
    ServerRedirect updateDestRedirectHost(Model model, @PathVariable int id, String hostHeader) throws Exception {
        ServerRedirectService.getInstance().setHostHeader(hostHeader, id);
        return ServerRedirectService.getInstance().getRedirect(id);
    }

    /**
     * Deletes an API server mapping
     *
     * @param model
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/edit/server/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    List<ServerRedirect> deleteRedirectUrl(Model model, @PathVariable int id) throws Exception {
        // get the profile id from this redirect so we can return the remaining
        // data
        ServerRedirect redir = ServerRedirectService.getInstance().getRedirect(id);
        int profileId = redir.getProfileId();
        ServerRedirectService.getInstance().deleteRedirect(id);
        return ServerRedirectService.getInstance().tableServers(profileId);
    }

    /**
     * Delete a server group
     *
     * @param model
     * @param id
     * @param profileId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "api/servergroup/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    List<ServerGroup> deleteServerGroup(Model model,
                                             @PathVariable int id,
                                             @RequestParam(value = "profileId", required = false) Integer profileId,
                                             @RequestParam(value = "clientUUID", required = false) String clientUUID,
                                             @RequestParam(value = "profileIdentifier", required = false) String profileIdentifier) throws Exception {
        if (profileId == null && profileIdentifier == null) {
            throw new Exception("profileId required");
        }
        if (profileId == null && profileIdentifier != null) {
            profileId = ProfileService.getInstance().getIdFromName(profileIdentifier);
        }

        int clientId = ClientService.getInstance().findClient(clientUUID, profileId).getId();

        ServerGroup group = ServerRedirectService.getInstance().getServerGroup(id, profileId);
        ServerRedirectService.getInstance().deleteServerGroup(id);
        return ServerRedirectService.getInstance().tableServerGroups(clientId);
    }

    /**
     * Returns a simple web page where certs can be downloaded.  This is meant for mobile device setup.
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/cert", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String certPage() throws Exception {
        return "cert";
    }

    /**
     * Returns a X509 binary certificate for a given domain name if a certificate has been generated for it
     *
     * @param locale
     * @param model
     * @param response
     * @param hostname
     * @throws Exception
     */
    @RequestMapping(value = "/cert/{hostname:.+}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public
    @ResponseBody
    void getCert(Locale locale, Model model, HttpServletResponse response, @PathVariable String hostname) throws Exception {
        // Set the appropriate headers so the browser thinks this is a file
        response.reset();
        response.setContentType("application/x-x509-ca-cert");
        response.setHeader("Content-Disposition", "attachment;filename=" + hostname + ".cer");

        // special handling for hostname=="root"
        // return the CyberVillians Root Cert in this case
        if (hostname.equals("root")) {
            hostname = "cybervillainsCA";
            response.setContentType("application/pkix-cert ");
        }

        // get the cert for the hostname
        KeyStoreManager keyStoreManager = com.groupon.odo.bmp.Utils.getKeyStoreManager(hostname);

        if (hostname.equals("cybervillainsCA")) {
            // get the cybervillians cert from resources
            File root = new File("seleniumSslSupport" + File.separator + hostname);

            // return the root cert
            Files.copy(new File(root.getAbsolutePath() + File.separator + hostname + ".cer").toPath(), response.getOutputStream());
            response.flushBuffer();
        } else {
            // return the cert for the appropriate alias
            response.getOutputStream().write(keyStoreManager.getCertificateByAlias(hostname).getEncoded());
            response.flushBuffer();
        }


    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
    }
}
