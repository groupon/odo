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
package com.groupon.odo;

import com.groupon.odo.plugin.*;
import com.groupon.odo.proxylib.*;
import com.groupon.odo.proxylib.models.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet implementation class Proxy
 * <p/>
 * Based on http://edwardstx.net/2010/06/http-proxy-servlet/ which is licensed
 * under the Apache License, Version 2.0
 */
@WebServlet("/*")
public class Proxy extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(Proxy.class);

    private ServerRedirectService serverRedirectService = null;

    private static final long serialVersionUID = 1L;

    /**
     * Path value triggers plugin reloading
     */
    private static final String RELOAD_PATH = "/proxy/reload";
    /**
     * Key for redirect location header.
     */
    private static final String STRING_LOCATION_HEADER = "Location";


    /**
     * Key for content length header.
     */
    private static final String STRING_CONTENT_LENGTH_HEADER_NAME = "Content-Length";
    /**
     * Key for host header
     */
    private static final String STRING_HOST_HEADER_NAME = "Host";
    /**
     * The directory to use to temporarily store uploaded files
     */
    private static final File FILE_UPLOAD_TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

    // Proxy host params
    /**
     * The host to which we are proxying requests
     */
    private String stringProxyHost;
    /**
     * The port on the proxy host to which we are proxying requests. Default
     * value is 80.
     */
    private int intProxyPort = 80;
    /**
     * The (optional) path on the proxy host to which we are proxying requests.
     * Default value is "".
     */
    private String stringProxyPath = "";
    /**
     * The maximum size for uploaded files in bytes. Default value is 50MB.
     */
    private int intMaxFileUploadSize = 50 * 1024 * 1024;

    /**
     * ThreadLocal to maintain state throughout the servlet execution cycle
     */
    private static final ThreadLocal<RequestInformation> requestInformation = new ThreadLocal<RequestInformation>();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Proxy() {
        super();

        try {
            serverRedirectService = ServerRedirectService.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setProxyHost("localhost");
    }

    private void printSuccess(HttpServletResponse response) throws Exception {
        response.setContentType(HttpUtilities.STRING_CONTENT_TYPE_JSON);
        PrintWriter pw = response.getWriter();
        pw.println("{\"return\": \"SUCCESS\"}");
    }

    private void printError(HttpServletResponse response, String reason)
            throws Exception {
        response.setContentType(HttpUtilities.STRING_CONTENT_TYPE_JSON);
        PrintWriter pw = response.getWriter();
        pw.println("{\"return\": \"ERROR\", \"reason\": \"" + reason + "\"}");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        // create thread specific session data
        requestInformation.set(new RequestInformation());
        RequestInformation requestInfo = requestInformation.get();
        Boolean alreadyServed = false;
        logger.info("GET Path: {}", request.getPathInfo());
        // some special commands
        try {
            if (request.getPathInfo().equals(RELOAD_PATH)) {
                logger.info("Reloading..");
                PluginManager.destroy();
                try {
                    printSuccess(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    printError(response, e.getMessage());
                }
                alreadyServed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!alreadyServed) {
            History history = new History();
            logOriginalRequestHistory("GET", request, history);
            requestInfo.originalRequestInfo = new HttpRequestInfo(request);
            try {
                GetMethod getMethodProxyRequest = new GetMethod(getProxyURL(
                        request, history, Constants.REQUEST_TYPE_GET));
                // set headers
                setProxyRequestHeaders(request, getMethodProxyRequest);
                // execute request
                logger.info("Executing request");
                executeProxyRequest(getMethodProxyRequest, request, response,
                        history);
            } catch (Exception e) {
                // TODO log to history
                logger.info("ERROR STACK: {}", e);
                logger.info("ERROR: cannot execute request: {}", e.getMessage());
            }
        }
    }

    private DiskFileItemFactory createDiskFactory() {
        // Create a factory for disk-based file items
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        // Set factory constraints
        diskFileItemFactory.setSizeThreshold(this.getMaxFileUploadSize());
        diskFileItemFactory.setRepository(FILE_UPLOAD_TEMP_DIRECTORY);
        return diskFileItemFactory;
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // create thread specific session data
        requestInformation.set(new RequestInformation());
        RequestInformation requestInfo = requestInformation.get();

        History history = new History();
        logOriginalRequestHistory("POST", request, history);
        requestInfo.originalRequestInfo = new HttpRequestInfo(request);

        try {
            PostMethod postMethodProxyRequest = new PostMethod(this.getProxyURL(
                    request, history, Constants.REQUEST_TYPE_POST));
            // Forward the request headers
            setProxyRequestHeaders(request, postMethodProxyRequest);

            // Check if this is a mulitpart (file upload) POST
            if (ServletFileUpload.isMultipartContent(request)) {
                logger.info("POST:: Multipart");
                DiskFileItemFactory diskFactory = createDiskFactory();
                HttpUtilities.handleMultipartPost(postMethodProxyRequest, request, diskFactory);
            } else {
                logger.info("POST:: Not Multipart");
                HttpUtilities.handleStandardPost(postMethodProxyRequest, request, history);
            }
            // use body filter to filter paths
            this.cullPathsByBodyFilter(history);

            // Execute the proxy request
            this.executeProxyRequest(postMethodProxyRequest, request, response,
                    history);
        } catch (Exception e) {
            // TODO log to history
            logger.info("ERROR: cannot execute request: {}", e.getMessage());
        }
    }

    /**
     * @see HttpServlet#doPut(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        // create thread specific session data
        requestInformation.set(new RequestInformation());
        RequestInformation requestInfo = requestInformation.get();

        History history = new History();
        logOriginalRequestHistory("PUT", request, history);
        requestInfo.originalRequestInfo = new HttpRequestInfo(request);

        try {
            PutMethod putMethodProxyRequest = new PutMethod(this.getProxyURL(
                    request, history, Constants.REQUEST_TYPE_PUT));
            // Forward the request headers
            setProxyRequestHeaders(request, putMethodProxyRequest);
            // Check if this is a multipart (file upload) POST
            if (ServletFileUpload.isMultipartContent(request)) {
                logger.info("PUT:: Multipart");
                DiskFileItemFactory diskFactory = createDiskFactory();
                HttpUtilities.handleMultipartPost(putMethodProxyRequest, request, diskFactory);
            } else {
                logger.info("PUT:: Not Multipart");
                HttpUtilities.handleStandardPost(putMethodProxyRequest, request, history);
            }

            // use body filter to filter paths
            this.cullPathsByBodyFilter(history);

            // Execute the proxy request
            this.executeProxyRequest(putMethodProxyRequest, request, response,
                    history);
        } catch (Exception e) {
            // TODO log to history
            logger.info("ERROR: cannot execute request: {}", e.getMessage());
        }
    }

    /**
     * @see HttpServlet#doDelete(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException {
        // create thread specific session data
        requestInformation.set(new RequestInformation());
        RequestInformation requestInfo = requestInformation.get();

        History history = new History();
        logOriginalRequestHistory("DELETE", request, history);
        requestInfo.originalRequestInfo = new HttpRequestInfo(request);

        try {
            DeleteMethod getMethodProxyRequest = new DeleteMethod(getProxyURL(
                    request, history, Constants.REQUEST_TYPE_DELETE));
            // set headers
            setProxyRequestHeaders(request, getMethodProxyRequest);
            // execute request
            executeProxyRequest(getMethodProxyRequest, request, response, history);
        } catch (Exception e) {
            // TODO log to history
            logger.info("ERROR: cannot execute request: {}", e.getMessage());
        }
    }

    /**
     * Match the POST/PUT body data to the selected paths. If the path defines a body filter
     * and the request body does not match, the path is removed.
     */
    @SuppressWarnings("unchecked")
    protected void cullPathsByBodyFilter(History history) throws Exception {
        RequestInformation requestInfo = requestInformation.get();
        try {
            String requestBody = history.getRequestPostData();
            ArrayList<EndpointOverride> removePaths = new ArrayList<EndpointOverride>();

            // requestInformation.get().selectedResponsePaths
            for (EndpointOverride selectedPath : requestInfo.selectedResponsePaths) {
                String pathBodyFilter = selectedPath.getBodyFilter();
                // check selective post/put filters.
                if (pathBodyFilter != null && pathBodyFilter.length() > 0) {
                    Pattern pattern = Pattern.compile(pathBodyFilter);
                    Matcher matcher = pattern.matcher(requestBody);

                    // if request body doesn't match path body filter, add to remove list
                    if (!matcher.matches()) {
                        removePaths.add(selectedPath);
                    }
                }
            }

            // remove paths that do not match filter
            for (EndpointOverride removePath : removePaths) {
                requestInfo.selectedResponsePaths.remove(removePath);
            }
        } catch (Exception e) {
            logger.info("ERROR: failure culling paths");
        }
    }

    /**
     * Retrieves all of the headers from the servlet request and sets them on
     * the proxy request
     *
     * @param httpServletRequest     The request object representing the client's request to the
     *                               servlet engine
     * @param httpMethodProxyRequest The request that we are about to send to the proxy host
     */
    @SuppressWarnings("unchecked")
    private void setProxyRequestHeaders(HttpServletRequest httpServletRequest,
                                        HttpMethod httpMethodProxyRequest) throws Exception {
        RequestInformation requestInfo = requestInformation.get();
        String hostName = HttpUtilities.getHostNameFromURL(httpServletRequest.getRequestURL().toString());
        // Get an Enumeration of all of the header names sent by the client
        Enumeration<String> enumerationOfHeaderNames = httpServletRequest.getHeaderNames();
        while (enumerationOfHeaderNames.hasMoreElements()) {
            String stringHeaderName = enumerationOfHeaderNames.nextElement();
            if (stringHeaderName.equalsIgnoreCase(STRING_CONTENT_LENGTH_HEADER_NAME))
                continue;

            logger.info("Current header: {}", stringHeaderName);
            // As per the Java Servlet API 2.5 documentation:
            // Some headers, such as Accept-Language can be sent by clients
            // as several headers each with a different value rather than
            // sending the header as a comma separated list.
            // Thus, we get an Enumeration of the header values sent by the
            // client
            Enumeration<String> enumerationOfHeaderValues = httpServletRequest.getHeaders(stringHeaderName);

            while (enumerationOfHeaderValues.hasMoreElements()) {
                String stringHeaderValue = enumerationOfHeaderValues.nextElement();
                // In case the proxy host is running multiple virtual servers,
                // rewrite the Host header to ensure that we get content from
                // the correct virtual server
                if (stringHeaderName.equalsIgnoreCase(STRING_HOST_HEADER_NAME) &&
                        requestInfo.handle) {
                    String hostValue = getHostHeaderForHost(hostName);
                    if (hostValue != null) {
                        stringHeaderValue = hostValue;
                    }
                }


                Header header = new Header(stringHeaderName, stringHeaderValue);
                // Set the same header on the proxy request
                httpMethodProxyRequest.addRequestHeader(header);
            }
        }

        // bail if we aren't fully handling this request
        if (!requestInfo.handle) {
            return;
        }

        // deal with header overrides for the request
        processRequestHeaderOverrides(httpMethodProxyRequest);
    }

    /**
     * Apply any applicable header overrides to request
     *
     * @param httpMethodProxyRequest
     * @throws Exception
     */
    private void processRequestHeaderOverrides(HttpMethod httpMethodProxyRequest) throws Exception {
        RequestInformation requestInfo = requestInformation.get();
        for (EndpointOverride selectedPath : requestInfo.selectedRequestPaths) {
            List<EnabledEndpoint> points = selectedPath.getEnabledEndpoints();
            for (EnabledEndpoint endpoint : points) {
                if (endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_ADD) {
                    httpMethodProxyRequest.addRequestHeader(endpoint.getArguments()[0].toString(),
                            endpoint.getArguments()[1].toString());
                    requestInfo.modified = true;
                } else if (endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_HEADER_OVERRIDE_REMOVE) {
                    httpMethodProxyRequest.removeRequestHeader(endpoint.getArguments()[0].toString());
                    requestInfo.modified = true;
                }
            }
        }
    }

    /**
     * Obtain host header value for a hostname
     *
     * @param hostName
     * @return
     */
    private String getHostHeaderForHost(String hostName) {
        List<ServerRedirect> servers = serverRedirectService.tableServers(requestInformation.get().profile.getId());
        for (ServerRedirect server : servers) {
            if (server.getSrcUrl().compareTo(hostName) == 0) {
                String hostHeader = server.getHostHeader();
                if (hostHeader == null || hostHeader.length() == 0)
                    return null;
                return hostHeader;
            }
        }
        return null;
    }

    /**
     * Apply the matching client UUID for the request
     *
     * @param httpServletRequest
     * @param history
     */
    private void processClientId(HttpServletRequest httpServletRequest, History history) {
        // get the client id from the request header if applicable.. otherwise set to default
        // also set the client uuid in the history object
        if (httpServletRequest.getHeader(Constants.PROFILE_CLIENT_HEADER_NAME) != null &&
                !httpServletRequest.getHeader(Constants.PROFILE_CLIENT_HEADER_NAME).equals("")) {
            history.setClientUUID(httpServletRequest.getHeader(Constants.PROFILE_CLIENT_HEADER_NAME));
        } else {
            history.setClientUUID(Constants.PROFILE_CLIENT_DEFAULT_ID);
        }
        logger.info("Client UUID is: {}", history.getClientUUID());
    }

    /**
     * Returns the newly formatted URL based on if the client is enabled, server mappings etc..
     * This also figures out what client ID to use for a profile(default is "-1"(PROFILE_CLIENT_DEFAULT_ID))
     *
     * @param httpServletRequest
     * @param history
     * @param requestType
     * @return
     * @throws Exception
     */
    private String getProxyURL(HttpServletRequest httpServletRequest,
                               History history, Integer requestType) throws Exception {
        // first determine if Odo will even fully handle this request
        RequestInformation requestInfo = requestInformation.get();
        if (ServerRedirectService.getInstance().canHandleRequest(HttpUtilities.getHostNameFromURL(
                httpServletRequest.getRequestURL().toString()))) {
            requestInfo.handle = true;
        }

        String stringProxyURL = "http://";

        // determine http vs https
        String originalURL = httpServletRequest.getRequestURL().toString();
        history.setOriginalRequestURL(originalURL);
        if (originalURL.startsWith("https://")) {
            stringProxyURL = "https://";
        }

        String hostName = HttpUtilities.getHostNameFromURL(originalURL);
        int port = HttpUtilities.getPortFromURL(originalURL);
        
        String origHostName = hostName;
        logger.info("original host name = {}", hostName);

        processClientId(httpServletRequest, history);

        String queryString = httpServletRequest.getQueryString();
        if (queryString == null) {
            queryString = "";
        } else {
            queryString = "?" + queryString;
        }

        // if this can't be overridden we are going to finish the string and bail
        if (!requestInfo.handle) {
            stringProxyURL = stringProxyURL + hostName + ":" + port;;

            // Handle the path given to the servlet
            stringProxyURL += httpServletRequest.getPathInfo();
            stringProxyURL += queryString;

            return stringProxyURL;
        }

        // figure out what profile to use based on source server name and matching paths
        // if no profile has matching paths then we just pick the first enabled one
        // if no profile is enabled then we pick the first one so that we have a URL mapping
        for (Profile tryProfile : ServerRedirectService.getInstance().getProfilesForServerName(origHostName)) {
            logger.info("Trying {}", tryProfile.getName());
            Client tryClient = ClientService.getInstance().findClient(history.getClientUUID(), tryProfile.getId());

            List<EndpointOverride> trySelectedRequestPaths = getSelectedPaths(Constants.OVERRIDE_TYPE_REQUEST, tryClient,
                    tryProfile, httpServletRequest.getRequestURL() + queryString, requestType);
            List<EndpointOverride> trySelectedResponsePaths = getSelectedPaths(Constants.OVERRIDE_TYPE_RESPONSE, tryClient,
                    tryProfile, httpServletRequest.getRequestURL() + queryString, requestType);
            logger.info("Sizes {} {}", trySelectedRequestPaths.size(), trySelectedResponsePaths.size());
            if ((trySelectedRequestPaths.size() > 0 || trySelectedResponsePaths.size() > 0) ||
                    tryClient.getIsActive() || requestInfo.profile == null) {
                logger.info("Selected {}, {}, " + httpServletRequest.getRequestURL() + "?" +
                        httpServletRequest.getQueryString(), tryProfile.getName(), tryClient.getId());
                // reset history UUID based on client
                history.setClientUUID(tryClient.getUUID());

                requestInfo.profile = tryProfile;
                requestInfo.selectedRequestPaths = new ArrayList<EndpointOverride>(trySelectedRequestPaths);
                requestInfo.selectedResponsePaths = new ArrayList<EndpointOverride>(trySelectedResponsePaths);
                requestInfo.client = tryClient;
            }
        }

        // we always should do this mapping since a request coming through us means it was redirected..
        // don't want to cause a loop
        hostName = getDestinationHostName(hostName);

        logger.info("new host name = {}", hostName);
        if (origHostName.compareTo(hostName) == 0)
            throw new Exception("Original and new hostname are the same.  this is a bad loop");

        stringProxyURL = stringProxyURL + hostName;

        // Handle the path given to the servlet
        stringProxyURL += httpServletRequest.getPathInfo();
        stringProxyURL += processQueryString(queryString);
        logger.info("url = {}", stringProxyURL);

        history.setProfileId(requestInfo.profile.getId());
        return stringProxyURL;
    }

    /**
     * Obtain the destination hostname for a source host
     *
     * @param hostName
     * @return
     */
    private String getDestinationHostName(String hostName) {
        List<ServerRedirect> servers = serverRedirectService
                .tableServers(requestInformation.get().profile.getId());
        for (ServerRedirect server : servers) {
            if (server.getSrcUrl().compareTo(hostName) == 0) {
                if (server.getDestUrl() != null && server.getDestUrl().compareTo("") != 0) {
                    return server.getDestUrl();
                } else {
                    logger.warn("Tried to apply redirect, but no destination server is specified for: {}",
                            server.getSrcUrl());
                }

                // only want to apply the first host name change found
                break;
            }
        }

        return hostName;
    }

    /**
     * Apply custom overrides for a request
     *
     * @param queryString
     * @return
     * @throws Exception
     */
    private String processQueryString(String queryString) throws Exception {
        String returnString = queryString;
        Boolean overridden = false;
        RequestInformation requestInfo = requestInformation.get();
        for (EndpointOverride selectedPath : requestInfo.selectedRequestPaths) {
            List<EnabledEndpoint> points = selectedPath.getEnabledEndpoints();
            for (EnabledEndpoint endpoint : points) {
                if (endpoint.getOverrideId() == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM) {
                    if (!overridden) {
                        overridden = true;
                        returnString = "";
                    }
                    logger.info("Overriding request");
                    // need to tokenize this
                    // tokenize the original query string starting at the first character(skips the ?)
                    String originalQueryString = "";
                    if (queryString.length() > 1) {
                        originalQueryString = queryString.substring(1);
                    }
                    returnString = "";
                    HashMap<String, String> originalParams = HttpUtilities.getParameters(originalQueryString);
                    HashMap<String, String> modifierParams = new HashMap<String, String>();

                    List<EnabledEndpoint> overrides = OverrideService.getInstance().getEnabledEndpoints(
                            selectedPath.getPathId(), selectedPath.getClientUUID(), null);

                    // find the first enabled custom request override
                    for (EnabledEndpoint override : overrides) {
                        if (override.getOverrideId() == Constants.PLUGIN_REQUEST_OVERRIDE_CUSTOM &&
                                override.getRepeatNumber() != 0) {
                            modifierParams = HttpUtilities.getParameters((String) override.getArguments()[0]);
                            override.decrementRepeatNumber();
                            break;
                        }
                    }

                    for (String key : modifierParams.keySet()) {
                        if (key.length() == 0) {
                            continue;
                        }
                        requestInfo.modified = true;
                        if (originalParams.containsKey(key)) {
                            logger.info("Removing {}", key);
                            originalParams.remove(key);
                        }

                        originalParams.put(key, modifierParams.get(key));
                        logger.info("Adding {}", key);
                    }

                    // rebuild params

                    for (String key : originalParams.keySet()) {
                        if (returnString.length() > 1) {
                            returnString += "&";
                        } else {
                            returnString = "?";
                        }

                        returnString += key + "=" + originalParams.get(key);
                    }
                }
            }
        }
        return returnString;
    }

    /**
     * Obtain matching paths for a request
     *
     * @param overrideType
     * @param client
     * @param profile
     * @param uri
     * @param requestType
     * @return
     * @throws Exception
     */
    private List<EndpointOverride> getSelectedPaths(int overrideType, Client client, Profile profile, String uri,
                                                         Integer requestType) throws Exception {
        List<EndpointOverride> selectPaths = new ArrayList<EndpointOverride>();

        // get the paths for the current active client profile
        // this returns paths in priority order
        List<EndpointOverride> paths = new ArrayList<EndpointOverride>();

        if (client.getIsActive()) {
            paths = PathOverrideService.getInstance().getPaths(
                    profile.getId(),
                    client.getUUID(), null);
        }

        boolean foundRealPath = false;
        logger.info("Checking uri: {}", uri);

        // it should now be ordered by priority, i updated tableOverrides to
        // return the paths in priority order
        for (EndpointOverride path : paths) {
            // first see if the request types match..
            // and if the path request type is not ALL
            // if they do not then skip this path
            if (path.getRequestType() != requestType && path.getRequestType() != Constants.REQUEST_TYPE_ALL)
                continue;

            // first see if we get a match
            Pattern pattern = Pattern.compile(path.getPath());
            Matcher matcher = pattern.matcher(uri);

            // we won't select the path if there aren't any enabled endpoints in it
            // this works since the paths are returned in priority order
            if (matcher.find()) {
                // now see if this path has anything enabled in it
                // Only go into the if:
                // 1. There are enabled items in this path
                // 2. Caller was looking for ResponseOverride and Response is enabled OR looking for RequestOverride
                // and request is enabled
                if (path.getEnabledEndpoints().size() > 0 &&
                        ((overrideType == Constants.OVERRIDE_TYPE_RESPONSE && path.getResponseEnabled()) ||
                                (overrideType == Constants.OVERRIDE_TYPE_REQUEST && path.getRequestEnabled()))) {
                    // if we haven't already seen a non global path
                    // or if this is a global path
                    // then add it to the list
                    if (!foundRealPath || path.getGlobal())
                        selectPaths.add(path);
                }

                // we set this no matter what if a path matched and it was not the global path
                // this stops us from adding further non global matches to the list
                if (!path.getGlobal()) {
                    foundRealPath = true;
                }
            }
        }

        return selectPaths;
    }

    /**
     * Set virtual host so the server can direct the request. Value is the host header if it is set, otherwise
     * use the hostname from the original request.
     *
     * @param httpMethodProxyRequest
     * @param httpServletRequest
     */
    private void processVirtualHostName(HttpMethod httpMethodProxyRequest, HttpServletRequest httpServletRequest) {
        String virtualHostName;
        if (httpMethodProxyRequest.getRequestHeader(STRING_HOST_HEADER_NAME) != null) {
            virtualHostName = HttpUtilities.removePortFromHostHeaderString(httpMethodProxyRequest.getRequestHeader(STRING_HOST_HEADER_NAME).getValue());
        } else {
            virtualHostName = HttpUtilities.getHostNameFromURL(httpServletRequest.getRequestURL().toString());
        }
        httpMethodProxyRequest.getParams().setVirtualHost(virtualHostName);
    }

    /**
     * Remove paths with no active overrides
     *
     * @throws Exception
     */
    private void cullDisabledPaths() throws Exception {

        ArrayList<EndpointOverride> removePaths = new ArrayList<EndpointOverride>();
        RequestInformation requestInfo = requestInformation.get();
        for (EndpointOverride selectedPath : requestInfo.selectedResponsePaths) {

            // check repeat count on selectedPath
            // -1 is unlimited
            if (selectedPath != null && selectedPath.getRepeatNumber() == 0) {
                // skip
                removePaths.add(selectedPath);
            } else if (selectedPath != null && selectedPath.getRepeatNumber() != -1) {
                // need to decrement the #
                selectedPath.updateRepeatNumber(selectedPath.getRepeatNumber() - 1);
            }
        }

        // remove paths if we need to
        for (EndpointOverride removePath : removePaths) {
            requestInfo.selectedResponsePaths.remove(removePath);
        }
    }

    private Boolean hasRequestBlock() throws Exception {
        for (EndpointOverride selectedPath : requestInformation.get().selectedResponsePaths) {
            // check to see if there is custom override data or if we have headers to remove
            List<EnabledEndpoint> points = selectedPath.getEnabledEndpoints();
            for (EnabledEndpoint endpoint : points) {
                // skip if repeat count is 0
                if (endpoint.getRepeatNumber() == 0)
                    continue;

                if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_OVERRIDE_CUSTOM) {
                    return true;
                }

                com.groupon.odo.proxylib.models.Method methodInfo =
                        PathOverrideService.getInstance().getMethodForEnabledId(endpoint.getOverrideId());
                if(methodInfo.isBlockRequest()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Obtain collection of headers to remove
     *
     * @return
     * @throws Exception
     */
    private ArrayList<String> getRemoveHeaders() throws Exception {
        ArrayList<String> headersToRemove = new ArrayList<String>();

        for (EndpointOverride selectedPath : requestInformation.get().selectedResponsePaths) {
            // check to see if there is custom override data or if we have headers to remove
            List<EnabledEndpoint> points = selectedPath.getEnabledEndpoints();
            for (EnabledEndpoint endpoint : points) {
                // skip if repeat count is 0
                if (endpoint.getRepeatNumber() == 0)
                    continue;

                if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_REMOVE &&
                        endpoint.getRepeatNumber() != 0) {
                    // add to remove headers array
                    headersToRemove.add(endpoint.getArguments()[0].toString());
                    endpoint.decrementRepeatNumber();
                }
            }
        }

        return headersToRemove;
    }

    /**
     * Execute a request through Odo processing
     *
     * @param httpMethodProxyRequest
     * @param httpServletRequest
     * @param httpServletResponse
     * @param history
     */
    private void executeProxyRequest(HttpMethod httpMethodProxyRequest,
                                     HttpServletRequest httpServletRequest,
                                     HttpServletResponse httpServletResponse, History history) {
        try {
            RequestInformation requestInfo = requestInformation.get();

            // Execute the request
            // removing accept headers so that the server doesn't encode anything
            // TODO: make this handle things like gzip encoding
            httpMethodProxyRequest.removeRequestHeader(Constants.HEADER_ACCEPT_ENCODING);
            httpMethodProxyRequest.removeRequestHeader(Constants.HEADER_ACCEPT);

            // set virtual host so the server knows how to direct the request
            // If the host header exists then this uses that value
            // Otherwise the hostname from the URL is used
            processVirtualHostName(httpMethodProxyRequest, httpServletRequest);
            cullDisabledPaths();

            // define output stream
            OutputStream outStream = new ByteArrayOutputStream();
            requestInfo.blockRequest = hasRequestBlock();
            PluginResponse responseWrapper = new PluginResponse(httpServletResponse);
            requestInfo.jsonpCallback = stripJSONPToOutstr(httpServletRequest, responseWrapper);


            if (!requestInfo.blockRequest) {
                logger.info("Sending request to server");

                history.setModified(requestInfo.modified);

                executeRequest(httpMethodProxyRequest,
                        httpServletRequest,
                        responseWrapper,
                        history,
                        outStream);

                writeResponseOutput(responseWrapper, requestInfo.jsonpCallback, outStream.toString());
            }

            logOriginalResponseHistory(responseWrapper, history);
            applyResponseOverrides(responseWrapper, httpServletRequest, history);
            // store history
            history.setModified(requestInfo.modified);
            logRequestHistory(httpMethodProxyRequest, responseWrapper, history);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Execute a request
     *
     * @param httpMethodProxyRequest
     * @param httpServletRequest
     * @param httpServletResponse
     * @param history
     * @param outStream
     * @throws Exception
     */
    private void executeRequest(HttpMethod httpMethodProxyRequest,
                                HttpServletRequest httpServletRequest,
                                PluginResponse httpServletResponse,
                                History history,
                                OutputStream outStream) throws Exception {
        int intProxyResponseCode = 999;
        try {
            // Create a default HttpClient
            HttpClient httpClient = new HttpClient();
            httpMethodProxyRequest.setFollowRedirects(false);
            ArrayList<String> headersToRemove = getRemoveHeaders();

            httpClient.getParams().setSoTimeout(60000);

            httpServletRequest.setAttribute("com.groupon.odo.removeHeaders", headersToRemove);
            intProxyResponseCode = httpClient.executeMethod(httpMethodProxyRequest);
        } catch (Exception e) {
            writeResponseOutput(httpServletResponse, requestInformation.get().jsonpCallback, "TIMEOUT");
            logRequestHistory(httpMethodProxyRequest, httpServletResponse, history);
            throw e;
        }
        logger.info("Response code: {}, {}", intProxyResponseCode,
                HttpUtilities.getURL(httpMethodProxyRequest.getURI().toString()));
        if (intProxyResponseCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */
                && intProxyResponseCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {

            String stringStatusCode = Integer.toString(intProxyResponseCode);
            processRedirect(stringStatusCode, httpMethodProxyRequest, httpServletRequest, httpServletResponse);
        } else {
            // Pass the response code back to the client
            httpServletResponse.setStatus(intProxyResponseCode);

            // Pass response headers back to the client
            Header[] headerArrayResponse = httpMethodProxyRequest.getResponseHeaders();
            for (Header header : headerArrayResponse) {
                httpServletResponse.setHeader(header.getName(), header.getValue());
            }

            // there is no data for a HTTP 304
            if (intProxyResponseCode != HttpServletResponse.SC_NOT_MODIFIED) {
                // Send the content to the client
                InputStream inputStreamProxyResponse = httpMethodProxyRequest.getResponseBodyAsStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);

                int intNextByte;
                // Collect all of the server data
                while ((intNextByte = bufferedInputStream.read()) != -1) {
                    outStream.write(intNextByte);
                }
            }
        }
    }

    /**
     * Execute a redirected request
     *
     * @param stringStatusCode
     * @param httpMethodProxyRequest
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws Exception
     */
    private void processRedirect(String stringStatusCode,
                                 HttpMethod httpMethodProxyRequest,
                                 HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse) throws Exception {
        // Check if the proxy response is a redirect
        // The following code is adapted from
        // org.tigris.noodle.filters.CheckForRedirect
        // Hooray for open source software

        String stringLocation = httpMethodProxyRequest.getResponseHeader(STRING_LOCATION_HEADER).getValue();
        if (stringLocation == null) {
            throw new ServletException("Received status code: "
                    + stringStatusCode + " but no "
                    + STRING_LOCATION_HEADER
                    + " header was found in the response");
        }
        // Modify the redirect to go to this proxy servlet rather than the proxied host
        String stringMyHostName = httpServletRequest.getServerName();
        if (httpServletRequest.getServerPort() != 80) {
            stringMyHostName += ":" + httpServletRequest.getServerPort();
        }
        stringMyHostName += httpServletRequest.getContextPath();
        httpServletResponse.sendRedirect(stringLocation.replace(
                getProxyHostAndPort() + this.getProxyPath(),
                stringMyHostName));
    }

    /**
     * Log original incoming request
     *
     * @param requestType
     * @param request
     * @param history
     */
    private void logOriginalRequestHistory(String requestType,
                                           HttpServletRequest request, History history) {
        logger.info("Storing original request history");
        history.setRequestType(requestType);
        history.setOriginalRequestHeaders(HttpUtilities.getHeaders(request));
        history.setOriginalRequestURL(request.getRequestURL().toString());
        history.setOriginalRequestParams(request.getQueryString() == null ? "" : request.getQueryString());
        logger.info("Done storing");
    }

    /**
     * Log original response
     *
     * @param httpServletResponse
     * @param history
     * @throws URIException
     */
    private void logOriginalResponseHistory(
            PluginResponse httpServletResponse, History history) throws URIException {
        RequestInformation requestInfo = requestInformation.get();
        if (requestInfo.handle && requestInfo.client.getIsActive()) {
            logger.info("Storing original response history");
            history.setOriginalResponseHeaders(HttpUtilities.getHeaders(httpServletResponse));
            history.setOriginalResponseCode(Integer.toString(httpServletResponse.getStatus()));
            history.setOriginalResponseContentType(httpServletResponse.getContentType());
            history.setOriginalResponseData(httpServletResponse.getContentString());
            logger.info("Done storing");
        }
    }

    /**
     * Log modified request
     *
     * @param httpMethodProxyRequest
     * @param httpServletResponse
     * @param history
     */
    private void logRequestHistory(HttpMethod httpMethodProxyRequest, PluginResponse httpServletResponse,
                                   History history) {
        try {
            if (requestInformation.get().handle && requestInformation.get().client.getIsActive()) {
                logger.info("Storing history");
                String createdDate;
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
                sdf.applyPattern("dd MMM yyyy HH:mm:ss");
                createdDate = sdf.format(new Date()) + " GMT";

                history.setCreatedAt(createdDate);
                history.setRequestURL(HttpUtilities.getURL(httpMethodProxyRequest.getURI().toString()));
                history.setRequestParams(httpMethodProxyRequest.getQueryString() == null ? ""
                        : httpMethodProxyRequest.getQueryString());
                history.setRequestHeaders(HttpUtilities.getHeaders(httpMethodProxyRequest));
                history.setResponseHeaders(HttpUtilities.getHeaders(httpServletResponse));
                history.setResponseCode(Integer.toString(httpServletResponse.getStatus()));
                history.setResponseContentType(httpServletResponse.getContentType());
                history.setResponseData(httpServletResponse.getContentString());
                HistoryService.getInstance().addHistory(history);
                logger.info("Done storing");
            }
        } catch (URIException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apply enabled response overrides
     *
     * @param httpServletResponse
     * @param httpServletRequest
     * @throws Exception
     */
    private void applyResponseOverrides(PluginResponse httpServletResponse,
                                        HttpServletRequest httpServletRequest, History history) throws Exception {
        RequestInformation requestInfo = requestInformation.get();

        for (EndpointOverride selectedPath : requestInfo.selectedResponsePaths) {
            // check to see if there is custom override data
            // something like
            List<EnabledEndpoint> points = selectedPath.getEnabledEndpoints();
            for (EnabledEndpoint endpoint : points) {
                // skip if the repeat number is 0
                if (endpoint.getRepeatNumber() == 0)
                    continue;

                // skip if this is a custom override and one has already been applied
                if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_OVERRIDE_CUSTOM &&
                        requestInfo.usedCustomResponse)
                    continue;

                // decrease repeat number
                endpoint.decrementRepeatNumber();

                if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_OVERRIDE_CUSTOM) {
                    // return custom response
                    String response = endpoint.getArguments()[0].toString();
                    httpServletResponse.setContentType(selectedPath.getContentType());
                    requestInfo.usedCustomResponse = true;
                    requestInfo.modified = true;
                    writeResponseOutput(httpServletResponse, requestInfo.jsonpCallback, response);
                } else if (endpoint.getOverrideId() == Constants.PLUGIN_RESPONSE_HEADER_OVERRIDE_ADD) {
                    httpServletResponse = (PluginResponse)HttpUtilities.addHeader(httpServletResponse, endpoint.getArguments());
                    requestInfo.modified = true;
                } else if (endpoint.getMethodInformation() != null &&
                        (endpoint.getMethodInformation().getMethodType().equals(Constants.PLUGIN_TYPE_RESPONSE_OVERRIDE) ||
                                endpoint.getMethodInformation().getMethodType().equals(Constants.PLUGIN_TYPE_RESPONSE_OVERRIDE_V2))) {
                    // run method
                    try {
                        com.groupon.odo.proxylib.models.Method methodInfo =
                                PathOverrideService.getInstance().getMethodForEnabledId(endpoint.getOverrideId());

                        logger.info("method = {}", methodInfo);
                        logger.info("Enabled endpoint: {}", methodInfo.getMethodName());
                        logger.info("Calling override for {}",
                                httpServletRequest.getRequestURL() + "?" + httpServletRequest.getQueryString());


                        // For v1 plugins - verify HTTP code is set correctly
                        if(methodInfo.getOverrideVersion() == 1) {
                            String responseOutput = (String)PluginManager.getInstance().callFunction(methodInfo.getClassName(), methodInfo.getMethodName(),
                                    httpServletResponse.getContentString(), endpoint.getArguments());
                            writeResponseOutput(httpServletResponse, requestInfo.jsonpCallback, responseOutput);

                            if(methodInfo.getHttpCode() != httpServletResponse.getStatus()) {
                                logger.info("Setting HTTP Code to {}", methodInfo.getHttpCode());
                                httpServletResponse.setStatus(methodInfo.getHttpCode());
                                // the server might have set a "Status" header.. so let's reset this too
                                httpServletResponse.setHeader(Constants.HEADER_STATUS,
                                    Integer.toString(methodInfo.getHttpCode()));
                            }
                        } else if (methodInfo.getOverrideVersion() == 2 ) {
                            PluginArguments pluginArgs = new PluginArguments(httpServletResponse, requestInfo.originalRequestInfo);

                            PluginManager.getInstance().callFunction(
                                    methodInfo.getClassName(), methodInfo.getMethodName(),
                                    pluginArgs, endpoint.getArguments());
                        }

                        requestInfo.modified = true;
                        logger.info("Done calling override");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        httpServletResponse.flushBuffer();
    }

    /**
     * @param httpServletResponse
     * @param jsonpCallback
     * @throws IOException
     */
    private void writeResponseOutput(HttpServletResponse httpServletResponse,
                                     String jsonpCallback,
                                     String responseOutput) throws IOException {
        RequestInformation requestInfo = requestInformation.get();

        // check to see if this is chunked
        boolean chunked = false;
        if (httpServletResponse.containsHeader(HttpUtilities.STRING_TRANSFER_ENCODING)
                && httpServletResponse.getHeader(HttpUtilities.STRING_TRANSFER_ENCODING).compareTo("chunked") == 0) {
            httpServletResponse.setHeader(HttpUtilities.STRING_CONNECTION, HttpUtilities.STRING_CHUNKED);
            chunked = true;
        }

        // reattach JSONP if needed
        if (responseOutput != null && jsonpCallback != null) {
            responseOutput = jsonpCallback + "(" + responseOutput + ");";
        }

        // don't do this if we got a HTTP 304 since there is no data to send back
        if (httpServletResponse.getStatus() != HttpServletResponse.SC_NOT_MODIFIED) {
            logger.info("Chunked: {}, {}", chunked, httpServletResponse.getBufferSize());
            if (!chunked) {
                // change the content length header to the new length
                if (responseOutput != null) {
                    httpServletResponse.setContentLength(responseOutput.getBytes().length);
                }
            }

            OutputStream outputStreamClientResponse = httpServletResponse.getOutputStream();
            httpServletResponse.resetBuffer();

            if (responseOutput != null) {
                outputStreamClientResponse.write(responseOutput.getBytes());
            }
            logger.info("Done writing");
        }
    }

    private String getProxyHostAndPort() {
        if (this.getProxyPort() == 80) {
            return this.getProxyHost();
        } else {
            return this.getProxyHost() + ":" + this.getProxyPort();
        }
    }

    private String getProxyHost() {
        return this.stringProxyHost;
    }

    private void setProxyHost(String stringProxyHostNew) {
        this.stringProxyHost = stringProxyHostNew;
    }

    private int getProxyPort() {
        return this.intProxyPort;
    }

    private void setProxyPort(int intProxyPortNew) {
        this.intProxyPort = intProxyPortNew;
    }

    private String getProxyPath() {
        return this.stringProxyPath;
    }

    private void setProxyPath(String stringProxyPathNew) {
        this.stringProxyPath = stringProxyPathNew;
    }

    private int getMaxFileUploadSize() {
        return this.intMaxFileUploadSize;
    }

    private void setMaxFileUploadSize(int intMaxFileUploadSizeNew) {
        this.intMaxFileUploadSize = intMaxFileUploadSizeNew;
    }


    private String stripJSONPToOutstr(HttpServletRequest httpServletRequest, PluginResponse response) throws IOException{
        String responseOutput = response.getContentString();
        RequestInformation requestInfo = requestInformation.get();
        // set outstr if we are overriding anything
        // also strip JSONP from it
        String jsonpCallback = null;
        if (requestInfo.selectedResponsePaths.size() > 0) {
            // look for a JSONP callback and strip it
            for (String callbackName : Constants.JSONP_CALLBACK_NAMES) {
                if (httpServletRequest.getParameter(callbackName) != null) {
                    jsonpCallback = httpServletRequest.getParameter(callbackName);
                    if (responseOutput.startsWith(jsonpCallback)) {
                        responseOutput = responseOutput.replaceFirst(jsonpCallback + "\\(", "");
                        responseOutput = responseOutput.substring(0, responseOutput.length() - 2);
                        writeResponseOutput(response, null, responseOutput);
                    }
                }
            }
        }
        return jsonpCallback;
    }

    /**
     * Struct to hold onto information about a request
     * This will be hashed by thread number during the request
     */
    private class RequestInformation {
        public Boolean modified = false;
        public Boolean handle = false;
        public Profile profile = null;
        public Client client = null;
        public Boolean blockRequest = false;
        public Boolean usedCustomResponse = false;
        public ArrayList<EndpointOverride> selectedRequestPaths = new ArrayList<EndpointOverride>();
        public ArrayList<EndpointOverride> selectedResponsePaths = new ArrayList<EndpointOverride>();
        public HttpRequestInfo originalRequestInfo = null;
        public String jsonpCallback = null;
    }
}
