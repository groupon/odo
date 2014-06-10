/*
Copyright (c) 2014, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.groupon.odo.tests;

import com.groupon.odo.proxylib.ClientService;
import com.groupon.odo.proxylib.HistoryService;
import com.groupon.odo.proxylib.ProfileService;
import com.groupon.odo.proxylib.models.History;
import com.groupon.odo.proxylib.models.Profile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"file:proxyui/src/main/webapp/WEB-INF/spring/root-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class HistoryTests extends TestBase {

    /**
     * Sets up stub data before each test
     *
     * @throws Exception
     */
    private Profile newProfile;

    @Before
    public void setUp() throws Exception {
        resetDatabase();
        newProfile = ProfileService.getInstance().add("Test");
        History testHistory = new History(newProfile.getId(), "-1", "createdAt",
                "requestType", "requestURL", "requestParams",
                "requestPostData", "requestHeaders", "responseCode",
                "responseHeaders", "responseContentType",
                "responseData", "originalRequestURL",
                "originalRequestParams", "originalRequestPostData",
                "originalRequestHeaders", "originalResponseCode",
                "originalResponseHeaders", "originalResponseContentType",
                "originalResponseData", true);
        HistoryService.getInstance().addHistory(testHistory);
        ClientService.getInstance().updateActive(newProfile.getId(), "-1", true);
    }

    @Test
    public void getHistory() throws Exception {
        History[] histories = HistoryService.getInstance().getHistory(newProfile.getId(), "-1", 0, 100, true, null);
        History latest = histories[histories.length - 1];
        assertTrue(latest.getOriginalRequestHeaders().equals("originalRequestHeaders"));
        assertTrue(latest.getOriginalRequestParams().equals("originalRequestParams"));
        assertTrue(latest.getOriginalRequestPostData().equals("originalRequestPostData"));
        assertTrue(latest.getOriginalRequestURL().equals("originalRequestURL"));
        assertTrue(latest.getOriginalResponseCode().equals("originalResponseCode"));
        assertTrue(latest.getOriginalResponseContentType().equals("originalResponseContentType"));
        assertTrue(latest.getOriginalResponseData().equals("originalResponseData"));
        assertTrue(latest.getOriginalResponseHeaders().equals("originalResponseHeaders"));
        assertTrue(latest.getRequestHeaders().equals("requestHeaders"));
        assertTrue(latest.getRequestParams().equals("requestParams"));
        assertTrue(latest.getRequestPostData().equals("requestPostData"));
        assertTrue(latest.getRequestType().equals("requestType"));
        assertTrue(latest.getRequestURL().equals("requestURL"));
        assertTrue(latest.getResponseCode().equals("responseCode"));
        assertTrue(latest.getResponseContentType().equals("responseContentType"));
        assertTrue(latest.getResponseData().equals("responseData"));
        assertTrue(latest.getResponseHeaders().equals("responseHeaders"));
    }

    @Test
    public void getHistoryCount() throws Exception {
        assertTrue(HistoryService.getInstance().getHistoryCount(newProfile.getId(), "-1", null) == 1);
    }

    @Test
    public void getHistoryForID() throws Exception {
        //id for the new entry should be 1, since db is empty
        History h = HistoryService.getInstance().getHistoryForID(1);
        assertTrue(h.getOriginalRequestHeaders().equals("originalRequestHeaders"));
        assertTrue(h.getOriginalRequestParams().equals("originalRequestParams"));
        assertTrue(h.getOriginalRequestPostData().equals("originalRequestPostData"));
        assertTrue(h.getOriginalRequestURL().equals("originalRequestURL"));
        assertTrue(h.getOriginalResponseCode().equals("originalResponseCode"));
        assertTrue(h.getOriginalResponseContentType().equals("originalResponseContentType"));
        assertTrue(h.getOriginalResponseData().equals("originalResponseData"));
        assertTrue(h.getOriginalResponseHeaders().equals("originalResponseHeaders"));
        assertTrue(h.getRequestHeaders().equals("requestHeaders"));
        assertTrue(h.getRequestParams().equals("requestParams"));
        assertTrue(h.getRequestPostData().equals("requestPostData"));
        assertTrue(h.getRequestType().equals("requestType"));
        assertTrue(h.getRequestURL().equals("requestURL"));
        assertTrue(h.getResponseCode().equals("responseCode"));
        assertTrue(h.getResponseContentType().equals("responseContentType"));
        assertTrue(h.getResponseData().equals("responseData"));
        assertTrue(h.getResponseHeaders().equals("responseHeaders"));
    }

}