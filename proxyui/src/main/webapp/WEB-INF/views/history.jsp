<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
    <title>History: ${profile_name}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <%@ include file="/resources/js/webjars.include" %>
    <script src="<c:url value="/resources/js/diff_match_patch_uncompressed.js" />"></script>

    <script type="text/javascript">
        $.jgrid.no_legacy_api = true;
        $.jgrid.useJSON = true;
    </script>

    <script type="text/javascript" src="<c:url value="/webjars/clipboard.js/2.0.0/clipboard.min.js"/>"></script>

    <style type="text/css">
        ul, li { list-style-type: none; }

        .has-switch { height: 30px; }

        .altRowClass { background: #eee; }

        .ui-jqgrid tr.jqgrow .break-all {
            white-space: normal;
            word-break: break-all;
        }

        .preformatted, textarea.preformatted { font-family: monospace; }

        #historyGridDiv {
            margin-bottom: 1em;
        }

        #historyGridDiv .ui-jqgrid .ui-jqgrid-bdiv {
            font-size: 14px;
        }

        #historyContentDiv textarea {
            background-color: #eee;
            transition: background-color .15s;
        }

        #historyContentDiv textarea:focus {
            background-color: initial;
        }

        .diffarea
        {
            overflow-y: scroll;
            resize: vertical;
            display: none;
            font-family: monospace;
            background-color: #eee;
        }

        .copy-only {
            font-size: 0;
        }

        @media print {
            .copy-only {
                font-size: initial;
            }
        }
     </style>
</head>
<body>
<%@ include file="pathtester_part.jsp" %>

<!-- Hidden div for grid options -->
<div id="gridOptionsDialog" style="display: none;">
    <table>
    <tr><td>
        Number of Rows:
    </td><td>
        <input id="numberOfRows" size=5/>
    </td></tr>
    </table>
</div>

<nav class="navbar navbar-default" role="navigation">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand" href="#">Odo</a>
        </div>

        <form class="navbar-form navbar-left" onsubmit="uriFilter();">
            <div class="form-group">

                <input type="text" class="form-control" placeholder="Filter (f)" id="searchFilter">
                <button class="btn btn-default" type="submit">Apply Filter</button>
                <button class="btn btn-default" type="button" onclick='clearFilter()'>Clear Filters</button>

                <span class="dropdown">
                    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        Filter By <span class="caret"></span>
                    </button>

                    <ul class="dropdown-menu">
                        <li><a href="#" onclick='showItemsWithMessages()'>Items With Messages</a></li>
                    </ul>
                </span>
            </div>
        </form>

        <ul class="nav navbar-nav navbar-right">
            <li><a href="#" onclick='clearHistory()'>Clear History</a></li>
            <li><a href="#" onclick='navigateScripts()'>Edit Scripts</a></li>
            <li><a href="#" onclick='openGridOptions()'>Grid Options</a></li>
        </ul>
    </div>
</nav>

<div id="historyGridDiv">
    <table id="historylist"></table>
    <div id="historynavGrid"></div>
</div>

<div id="historyContentDiv">
    <div id="tabs">
        <ul>
            <li><a href="#tabs-1">Response <kbd>1</kbd></a></li>
            <li><a href="#tabs-2">Request <kbd>2</kbd></a></li>
            <li><a href="#tabs-3">Other <kbd>3</kbd></a></li>
        </ul>

        <div id="tabs-1">
            <div class="btn-group btn-group-sm pull-right history-diff-tools" id="responseButtons">
                <button type="button" class="btn btn-default history-modified" id="showModifiedResponseButton" onClick="showModifiedResponse()">Modified <kbd>m</kbd></button>
                <button type="button" class="btn btn-default history-original" id="showOriginalResponseButton" onClick="showOriginalResponse()">Original <kbd>o</kbd></button>
                <button type="button" class="btn btn-default history-diff" id="showChangedResponseButton" onClick="showChangedResponse()">View Diff <kbd>d</kbd></button>
            </div>

            <h3 style="display: inline-block">Headers</h3>
            <div class="btn-group btn-group-sm">
                <button type="button" id="copyResponseHeaders" class="btn btn-default copy-to-clipboard">Copy <kbd>c</kbd>&nbsp;<kbd>h</kbd></button>
            </div>
            <textarea class="form-control preformatted" data-copy-trigger="copyResponseHeaders" rows="4" id="responseHeaders"></textarea>
            <textarea class="form-control preformatted" data-copy-trigger="copyResponseHeaders" rows="4" style="display: none;" id="originalResponseHeaders"></textarea>
            <div class="form-control diffarea" id="originalResponseHeaderChange" data-copy-trigger="copyResponseHeaders"></div>

            <h3>Data <span class="label label-info label-small" id="responseTypeLabel"></span> <span class="label label-primary label-small" id="responseDataDecodedLabel"></span></h3>

            <div class="btn-toolbar">
                <div class="btn-group btn-group-sm">
                    <button type="button" id="copyResponseData" class="btn btn-default copy-to-clipboard">Copy <kbd>c</kbd>&nbsp;<kbd>d</kbd></button>
                </div>

                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-default" id="showRawResponseDataButton" onClick="showRawResponseData()">Raw</button>
                    <button type="button" class="btn btn-default" id="showRawFormattedDataButton" onClick="showFormattedResponseData(false)">Formatted</button>
                </div>

                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-default" id="downloadResponseDataButton" onClick="downloadResponseData()">Export Response</button>
                </div>
            </div>

            <textarea class="form-control preformatted" rows="20" data-copy-trigger="copyResponseData" id="responseRaw"></textarea>
            <textarea class="form-control preformatted" rows="20" data-copy-trigger="copyResponseData" style="display: none;" id="originalResponseRaw"></textarea>
            <div class="form-control diffarea" data-copy-trigger="copyResponseData" id="originalResponseChange"></div>
        </div><!-- /#tabs-1 -->

        <div id="tabs-2">
            <div class="btn-group btn-group-sm pull-right history-diff-tools" id="requestButtons">
                <button type="button" class="btn btn-default history-modified" id="showModifiedRequestButton" onClick="showModifiedRequestData()">Modified <kbd>m</kbd></button>
                <button type="button" class="btn btn-default history-original" id="showOriginalButton" onClick="showOriginalRequestData()">Original <kbd>o</kbd></button>
                <button type="button" class="btn btn-default history-diff" id="showChangedButton" onClick="showChangedRequestData()">View Diff <kbd>d</kbd></button>
            </div>

            <h3>URL</h3>
            <div class="btn-toolbar">
                <div class="btn-group btn-group-sm">
                    <button type="button" id="copyRequestQuery" class="btn btn-default copy-to-clipboard">Copy <kbd>c</kbd>&nbsp;<kbd>u</kbd></button>
                </div>
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-default" onClick="showPathTester()">Test Path</button>
                </div>
            </div>
            <textarea class="form-control preformatted" rows="1" data-copy-trigger="copyRequestQuery" id="requestQuery"></textarea>
            <textarea class="form-control preformatted" rows="1" data-copy-trigger="copyRequestQuery" style="display: none;" id="originalRequestQuery"></textarea>
            <div class="form-control diffarea" data-copy-trigger="copyRequestQuery" id="originalRequestQueryChange"></div>

            <h3 style="display: inline-block;">Parameters</h3>
            <div class="btn-group btn-group-sm">
                <button type="button" id="copyRequestParameters" class="btn btn-default copy-to-clipboard">Copy <kbd>c</kbd>&nbsp;<kbd>p</kbd></button>
            </div>
            <textarea class="form-control preformatted" rows="3" data-copy-trigger="copyRequestParameters" id="requestParameters"></textarea>
            <textarea class="form-control preformatted" rows="3" data-copy-trigger="copyRequestParameters" style="display: none;" id="originalRequestParameters"></textarea>
            <div class="form-control diffarea" data-copy-trigger="copyRequestParameters" id="originalRequestParametersChanged"></div>

            <h3 style="display: inline-block;">Headers</h3>
            <div class="btn-group btn-group-sm">
                <button type="button" id="copyRequestHeaders" class="btn btn-default copy-to-clipboard">Copy <kbd>c</kbd>&nbsp;<kbd>h</kbd></button>
            </div>
            <textarea class="form-control preformatted" rows="3" data-copy-trigger="copyRequestHeaders" id="requestHeaders"></textarea>
            <textarea class="form-control preformatted" rows="3" data-copy-trigger="copyRequestHeaders" style="display: none;" id="originalRequestHeaders"></textarea>
            <div class="form-control diffarea" data-copy-trigger="copyRequestHeaders" id="originalRequestHeadersChanged"></div>

            <h3 style="display: inline-block;">POST Data</h3>
            <div class="btn-group btn-group-sm">
                <button type="button" id="copyPOSTData" class="btn btn-default copy-to-clipboard">Copy <kbd>c</kbd>&nbsp;<kbd>d</kbd></button>
            </div>
            <span class="label label-primary" id="requestDataDecodedLabel"></span>
            <textarea class="form-control preformatted" data-copy-trigger="copyPOSTData" rows="10" id="requestPOSTData"></textarea>
            <textarea class="form-control preformatted" data-copy-trigger="copyPOSTData" rows="10" style="display: none" id="originalRequestPOSTData"></textarea>
            <div class="form-control diffarea" data-copy-trigger="copyPOSTData" id="originalRequestPOSTDataChanged"></div>
        </div><!-- /#tabs-2 -->

        <div id="tabs-3">
            <h3>cURL Command</h3>
            <div class="btn-group btn-group-sm">
                <button type="button" id="copyCURLCommand" class="btn btn-default copy-to-clipboard">Copy <kbd>c</kbd>&nbsp;<kbd>c</kbd></button>
            </div>
            <textarea class="form-control preformatted" rows="29" data-copy-trigger="copyCURLCommand" id="curlCommand"></textarea>
        </div><!-- /#tabs-3 -->
    </div>
</div>

<script type="text/javascript">
    var clientUUID = '${clientUUID}';
    var historyFilter;

    function openGridOptions() {
        $("#gridOptionsDialog").dialog({
            title: "Grid Options",
            width: 750,
            modal: true,
            position:['top',20],
            buttons: {
              "Save": function() {
                  if (! isNaN($("#numberOfRows").val())) {
                      $.cookie("historyGridRows", $("#numberOfRows").val(), { expires: 10000, path: '/testproxy/history' });
                  }
                  $("#gridOptionsDialog").dialog("close");
                  location.reload();
              },
              "Close": function() {
                  $("#gridOptionsDialog").dialog("close");
              }
            },
            open: function( event, ui ) {
                $("#numberOfRows").val(getNumberOfRows());
            }
        });
    }

    function getNumberOfRows() {
        return $.cookie("historyGridRows") || 10;
    }

    function navigateScripts() {
        window.open('<c:url value = '/scripts' />');
    }

    function clearHistory() {
        $.ajax({
            type : "POST",
            url : '<c:url value="/api/history/${profile_id}"/>',
            data : ({
                clientUUID : clientUUID,
                _method : 'DELETE'
            }),
            success : function(data) {
                $("#historylist").trigger("reloadGrid");
            },
            error : function(xhr, ajaxOptions, thrownError) {
                $('#info').html("Whoops!");
            }
        });
    }

    function uriFilter() {
        historyFilter = $("#searchFilter").val();
        jQuery("#historylist")
            .jqGrid(
                'setGridParam',
                {
                    url : '<c:url value="/api/history/${profile_id}"/>?clientUUID=${clientUUID}&source_uri[]=' + historyFilter,
                    page : 1
                })
            .jqGrid('setCaption', historyCaption())
            .trigger("reloadGrid")
        return false; // prevent form redirection
    }

    function showItemsWithMessages() {
        jQuery("#historylist")
            .jqGrid(
                'setGridParam',
                {
                    url : '<c:url value="/api/history/${profile_id}"/>?clientUUID=${clientUUID}&hasMessage=true',
                    page : 1
                }).trigger("reloadGrid");
    }

    function clearFilter() {
        historyFilter = null;
        jQuery("#historylist")
            .jqGrid(
                'setGridParam',
                {
                    url : '<c:url value="/api/history/${profile_id}"/>?clientUUID=${clientUUID}',
                    page : 1
                })
            .jqGrid("setCaption", historyCaption())
            .trigger("reloadGrid")
    }

    var currentHistoryId = -1;

    // this just sets the current path ID so that other formatters can use it
    function idFormatter(cellvalue, options, rowObject) {
        currentHistoryId = cellvalue;
        return cellvalue;
    }

    function dateFormatter(cellvalue, options, rowObject) {
      var date = new Date(cellvalue)

      if (date instanceof Date && isFinite(date)) {
        var options = { hour: '2-digit', minute: '2-digit', second: '2-digit', month: 'short', day: 'numeric' };
        return date.toLocaleDateString("en-US", options);
      }

      return cellvalue;
    }

    var invalidRows = []
    function validFormatter(cellvalue, options, rowObject) {
        if (!cellvalue) {
            invalidRows[invalidRows.length] = options.rowId;
        }
        return cellvalue;
    }

    var originalResponseFlag = 0;

    function showOriginalResponse() {
        originalResponseFlag = 1;
        $("#originalResponseHeaders").val(historyData.history.originalResponseHeaders);
        if(historyData.history.responseContentType == null ||
            historyData.history.responseContentType.toLowerCase().indexOf("application/json") == -1 ||
            historyData.history.responseData == "" || $.cookie("formatted") == "false"){
                $("#originalResponseRaw").val(originalResponseRaw);
        } else {
            if (historyData.history.formattedOriginalResponseData == "") {
                showFormattedResponseData(false);
            } else {
                $("#originalResponseRaw").val(historyData.history.formattedOriginalResponseData);
            }
        }
        $("#originalResponseHeaders").show();
        $("#originalResponseRaw").show();
        $("#originalResponseHeaderChange").hide();
        $("#originalResponseChange").hide();
        $("#responseRaw").hide();
        $("#responseHeaders").hide();
        $("#showChangedResponseButton, #showModifiedResponseButton").removeClass("btn-primary").addClass("btn-default");
        $("#showOriginalResponseButton").removeClass("btn-default").addClass("btn-primary");
    }

    var dmp = new diff_match_patch();
    function showChangedData(originalData, changedData, originalID, changedID, modifiedID){
        var d = dmp.diff_main(originalData, changedData);
        dmp.diff_cleanupSemantic(d);
        var ds = diff_prettyHtml(d);
        var patchdiff = dmp.patch_toText(dmp.patch_make(originalData, changedData));
        //$("#" + changedID).html(ds.replace(/[^\x00-\x7F]/g, ""));
        $("#" + changedID)
            .height($("#" + originalID).height())
            .attr("data-copy-content", patchdiff)
            .html(ds)
            .show();
        $("#" + originalID + ", #" + modifiedID).hide();
    }

    function showChangedResponse() {
        showFormattedResponseData(true);
    }

    function showChangedResponsePostFormattedAJAX() {
        showChangedData(historyData.history.originalResponseHeaders, historyData.history.responseHeaders, "originalResponseHeaders", "originalResponseHeaderChange", "responseHeaders");
        showChangedData(historyData.history.formattedOriginalResponseData, historyData.history.formattedResponseData, "originalResponseRaw", "originalResponseChange", "responseRaw");
        $("#showChangedResponseButton").removeClass("btn-default").addClass("btn-primary");
        $("#showOriginalResponseButton, #showModifiedResponseButton").removeClass("btn-primary").addClass("btn-default");
    }

    function showModifiedResponse() {
        originalResponseFlag = 0;
        $("#responseHeaders").val(historyData.history.responseHeaders);
        if(historyData.history.responseContentType == null ||
            historyData.history.responseContentType.toLowerCase().indexOf("application/json") == -1 ||
            historyData.history.responseData == "" || $.cookie("formatted") == "false") {
                $("#responseRaw").val(responseRaw);
        } else {
            if (historyData.history.formattedResponseData == "") {
                showFormattedResponseData(false);
            } else {
                $("#responseRaw").val(historyData.history.formattedResponseData);
            }
        }
        $("#responseHeaders").show();
        $("#responseRaw").show();
        $("#originalResponseHeaderChange").hide();
        $("#originalResponseChange").hide();
        $("#originalResponseRaw").hide();
        $("#originalResponseHeaders").hide();
        $("#showOriginalResponseButton, #showChangedResponseButton").removeClass("btn-primary").addClass("btn-default");
        $("#showModifiedResponseButton").removeClass("btn-default").addClass("btn-primary");
    }

    var responseRaw, originalResponseRaw;
    function showFormattedResponseData(forDiff) {
        $.ajax({
            type : "GET",
            url : '<c:url value="/api/history/${profile_id}/"/>' + currentHistoryId,
            data : 'clientUUID=${clientUUID}&format=formattedAll',
            success : function(data) {
                historyData = data;
                if (forDiff) {
                    showChangedResponsePostFormattedAJAX();
                } else {
                    $("#responseRaw").val(data.history.formattedResponseData);
                    $("#originalResponseRaw").val(data.history.formattedOriginalResponseData);
                    $.cookie("formatted", "true");
                    $("#showRawFormattedDataButton").removeClass("btn-default").addClass("btn-primary");
                    $("#showRawResponseDataButton").removeClass("btn-primary").addClass("btn-default");
                }
            }
        });
    }

    function showRawResponseData() {
        responseRaw = historyData.history.responseData;
        originalResponseRaw = historyData.history.originalResponseData;
        $("#responseRaw").val(responseRaw);
        $("#originalResponseRaw").val(originalResponseRaw);
        $.cookie("formatted", "false");
        $("#showRawResponseDataButton").removeClass("btn-default").addClass("btn-primary");
        $("#showRawFormattedDataButton").removeClass("btn-primary").addClass("btn-default");
    }

    function showOriginalRequestData(){
        $("#originalRequestQuery").show();
        $("#originalRequestQueryChange").hide();
        $("#requestQuery").hide();
        $("#originalRequestParameters").show();
        $("#originalRequestParametersChanged").hide();
        $("#requestParameters").hide();
        $("#originalRequestHeaders").show();
        $("#originalRequestHeadersChanged").hide();
        $("#requestHeaders").hide();
        $("#originalRequestPOSTData").show();
        $("#originalRequestPOSTDataChanged").hide();
        $("#requestPOSTData").hide();
        $("#showOriginalButton").removeClass("btn-default").addClass("btn-primary");
        $("#showChangedButton, #showModifiedRequestButton").removeClass("btn-primary").addClass("btn-default");
    }

    function showChangedRequestData(){
        showChangedData(historyData.history.originalRequestURL, historyData.history.requestURL, "originalRequestQuery", "originalRequestQueryChange", "requestQuery");
        showChangedData(historyData.history.originalRequestParams, historyData.history.requestParams, "originalRequestParameters", "originalRequestParametersChanged", "requestParameters");
        showChangedData(historyData.history.originalRequestHeaders, historyData.history.requestHeaders, "originalRequestHeaders", "originalRequestHeadersChanged", "requestHeaders");
        showChangedData(historyData.history.originalRequestPostData, historyData.history.requestPostData, "originalRequestPOSTData", "originalRequestPOSTDataChanged", "requestPOSTData");
        $("#showChangedButton").removeClass("btn-default").addClass("btn-primary");
        $("#showOriginalButton, #showModifiedRequestButton").removeClass("btn-primary").addClass("btn-default");
    }

    function showModifiedRequestData(){
        $("#originalRequestQuery").hide();
        $("#originalRequestQueryChange").hide();
        $("#requestQuery").show();
        $("#originalRequestParameters").hide();
        $("#originalRequestParametersChanged").hide();
        $("#requestParameters").show();
        $("#originalRequestHeaders").hide();
        $("#originalRequestHeadersChanged").hide();
        $("#requestHeaders").show();
        $("#originalRequestPOSTData").hide();
        $("#originalRequestPOSTDataChanged").hide();
        $("#requestPOSTData").show();
        $("#showOriginalButton, #showChangedButton").removeClass("btn-primary").addClass("btn-default");
        $("#showModifiedRequestButton").removeClass("btn-default").addClass("btn-primary");
    }


    function showCurlCommand() {
        var headers = historyData.history.requestHeaders.split('\n');
        var requestType = historyData.history.requestType;

        var commandLine = "curl --insecure -X " + requestType;
        for (var x in headers) {
            if(headers[x].toLowerCase().lastIndexOf("content-length", 0) === 0) {
                continue;
            }
            commandLine += " -H '" + headers[x].replace("'", "\\u0027") + "'";
        }

        if (historyData.history.requestPostData != null
                && historyData.history.requestPostData !== "") {
            commandLine += " -d '" + historyData.history.requestPostData.replace("'", "\\u0027")
                    + "'";
        }

        commandLine += " '" + historyData.history.requestURL;

        if (historyData.history.requestParams != null
                && historyData.history.requestParams !== "") {
            commandLine += "?" + historyData.history.requestParams;
        }

        commandLine += "'";

        $("#curlCommand").val(commandLine);
    }

    //http://stackoverflow.com/questions/17564103/using-javascript-to-download-file-as-a-csv-file
    function downloadResponseData() {
        var responseDownload = $("<a>")
            .attr("download", "response");
        if (originalResponseFlag == 1) {
            responseDownload.attr("href", "data:text/json;charset=utf-8," + historyData.history.originalResponseData);
        } else {
            responseDownload.attr("href", "data:text/json;charset=utf-8," + historyData.history.responseData);
        }
        $("body").append(responseDownload)
        responseDownload.click();
    }

    var historyData;
    function loadData(historyId) {
        $.ajax({
            type : "GET",
            url : '<c:url value="/api/history/${profile_id}/"/>'
                + historyId,
            data : 'clientUUID=${clientUUID}',
            success : function(data) {
                // populate data
                historyData = data;

                // optionally turn off the Formatted button
                if (data.history.responseContentType == null
                    || data.history.responseContentType.toLowerCase().indexOf(
                    "application/json") == -1 || data.history.responseData == "") {
                        showRawResponseData();
                        showModifiedResponse();
                        $("#showRawFormattedDataButton").attr("disabled", "disabled");
                } else {
                    if($.cookie("formatted") == "true") {
                        showFormattedResponseData(false);
                    } else {
                        showRawResponseData();
                        showModifiedResponse();
                    }
                    $("#showRawFormattedDataButton").removeAttr("disabled");
                }

                if (data.history.responseData == "") {
                    $("#downloadResponseDataButton").hide();
                } else {
                    $("#downloadResponseDataButton").show();
                }


                $("#responseHeaders").val(data.history.responseHeaders);
                $("#originalResponseHeaders").val(data.history.originalResponseHeaders);
                $("#responseTypeLabel").html(data.history.responseContentType);
                $("#requestQuery").val(data.history.requestURL);
                $("#requestParameters").val(data.history.requestParams);
                $("#requestHeaders").val(data.history.requestHeaders);
                $("#requestPOSTData").val(data.history.requestPostData);

                if(data.history.modified) {
                    $("#originalResponseHeaders").val(historyData.history.originalResponseHeaders);
                    $("#originalRequestQuery").val(data.history.originalRequestURL);
                    $("#originalRequestParameters").val(data.history.originalRequestParams);
                    $("#originalRequestHeaders").val(data.history.originalRequestHeaders);
                    $("#originalRequestPOSTData").val(data.history.originalRequestPostData);
                    $("#responseButtons").show();
                    $("#requestButtons").show();
                    $("#showModifiedResponseButton, #showModifiedRequestButton").removeClass("btn-default").addClass("btn-primary");
                } else {
                    // set the query back to the original query data
                    $("#requestQuery").val(data.history.originalRequestURL);

                    $("#responseButtons").hide();
                    $("#requestButtons").hide();
                }

                // set response data decoded
                if(data.history.responseBodyDecoded) {
                    // mark as decoded in the UI
                    $("#responseDataDecodedLabel").html("decoded");

                    // try to make the label more specific
                    var headerParts = data.history.responseHeaders.split('\n');
                    for (var x in headerParts) {
                        var parts = headerParts[x].split(": ");
                        if (parts[0].toLowerCase() === "content-encoding") {
                            $("#responseDataDecodedLabel").html("decoded: " + parts[1]);
                            break;
                        }
                    }
                } else {
                    $("#responseDataDecodedLabel").html("");
                }

                // set request data decoded
                if(data.history.requestBodyDecoded) {
                    // mark as decoded in the UI
                    $("#requestDataDecodedLabel").html("decoded");
                } else {
                    $("#requestDataDecodedLabel").html("");
                }

                showModifiedRequestData();
                showCurlCommand();
            }
        });
    }

    $(document).ready(function() {
        $("#tabs").tabs();
        $("#tabs").css("overflow", "scroll");
        $("#radioset").buttonset();

        // bind window resize to fix grid width
        $(window).bind('resize', function() {
            $("#historylist").setGridWidth($("#historyGridDiv").width());
        });

        $(".copy-to-clipboard")
            .tooltip({
                    "placement": "top",
                    "trigger": "manual"
                })
            .on("shown.bs.tooltip", function(event) {
                setTimeout(function() {
                    $(event.target).tooltip("hide");
                }, 1500);
            });

        new ClipboardJS(".copy-to-clipboard", {
            text: function(trigger) {
                var $target = $("[data-copy-trigger=\"" + trigger.id + "\"]").filter(function(index) {
                    return $(this).is(":visible");
                }).first();

                var copytext;

                if ($target.is('textarea')) {
                    copytext = $target.val();
                } else if ($target.attr('data-copy-content')) {
                    copytext = decodeURIComponent($target.attr('data-copy-content'));
                } else {
                    copytext = $target.text();
                }

                if (copytext.length === 0) {
                    $(trigger).data('bs.tooltip').options.title = "Nothing copied.";
                    $(trigger).tooltip('show')
                }

                return copytext;
            }
        }).on('success', function(e) {
            $(e.trigger).data('bs.tooltip').options.title = "Copied!";
            $(e.trigger).tooltip("show");

            e.clearSelection();
        }).on('error', function(e) {
            $(e.trigger).data('bs.tooltip').options.title = "Could not copy text.";
            $(e.trigger).tooltip("show");

            e.clearSelection();
        });

        var selectRowUsed = false;

        /* Keyboard shortcuts configuration */
        // Tabs: response/request/other navigation
        Mousetrap.bind('1', function() {
            $("[href=\"#tabs-1\"]:visible").click();
        });
        Mousetrap.bind('2', function() {
            $("[href=\"#tabs-2\"]:visible").click();
        });
        Mousetrap.bind('3', function() {
            $("[href=\"#tabs-3\"]:visible").click();
        });
        // Filter
        Mousetrap.bind('f', function(event) {
            event.preventDefault();
            event.stopPropagation();
            $("#searchFilter").focus();
        });
        // View data modified/original/diff
        Mousetrap.bind('m', function() { // shared by Response/Request
            $(getActiveTabId() + " .history-diff-tools:visible .history-modified").click();
        });
        Mousetrap.bind('o', function() { // shared by Response/Request
            $(getActiveTabId() + " .history-diff-tools:visible .history-original").click();
        });
        Mousetrap.bind('d', function() { // shared by Response/Request
            var selectedTabId = $("#tabs .ui-state-active a.ui-tabs-anchor").attr("href");
            $(getActiveTabId() + " .history-diff-tools:visible .history-diff").click();
        });
        // Refresh history
        Mousetrap.bind('r', function() {
            jQuery("#historylist").trigger("reloadGrid");
        });
        // Copy operations
        Mousetrap.bind('c u', function() { // Request URL
            $("#copyRequestQuery:visible").click();
        });
        Mousetrap.bind('c p', function() { // Request Params
            $("#copyRequestParameters:visible").click();
        });
        Mousetrap.bind('c h', function() { // Request/Response Headers
            $("#copyRequestHeaders:visible, #copyResponseHeaders:visible").first().click();
        });
        Mousetrap.bind('c d', function() { // Request/Response Data
            $("#copyPOSTData:visible, #copyResponseData:visible").first().click();
        });
        Mousetrap.bind('c c', function() { // cURL command
            $("#copyCURLCommand:visible").click();
        });

        $("#historylist")
            .jqGrid({
                url : '<c:url value="/api/history/${profile_id}"/>?clientUUID=${clientUUID}',
                autowidth : true,
                pgbuttons : true, // disable page control like next, back button
                pgtext : null,
                datatype : "json",
                page : "${page}",
                toppager: true,
                gridview: true,
                rowNum: getNumberOfRows(),
                altRows: true,
                altclass: 'altRowClass',
                colNames : [ 'ID', 'Created At', 'Method', 'Query',
                        'Query Params', 'Code', 'Valid', 'Message', 'Modified?' ],
                colModel : [
                    {
                        name : 'id',
                        index : 'id',
                        hidden : true,
                        formatter : idFormatter,
                        sortable: false
                    }, {
                        name : 'createdAt',
                        index : 'createdAt',
                        width : 125,
                        editable : false,
                        sortable: false,
                        align : 'center',
                        formatter : dateFormatter,
                    }, {
                        name : 'requestType',
                        index : 'requestType',
                        width : 60,
                        editable : false,
                        sortable: false,
                        align : 'center'
                    }, {
                        name : 'originalRequestURL',
                        index : 'originalRequestURL',
                        width : 375,
                        editable : false,
                        sortable: false,
                        classes: 'break-all'
                    }, {
                        name : 'requestParams',
                        index : 'requestParams',
                        width : 300,
                        editable : false,
                        sortable: false,
                        classes: 'break-all preformatted'
                    }, {
                        name : 'responseCode',
                        index : 'responseCode',
                        width : 50,
                        editable : false,
                        sortable: false,
                        align : 'center'
                    }, {
                        name : 'valid',
                        index : 'valid',
                        width : 55,
                        hidden : true,
                        sortable: false,
                        formatter : validFormatter
                    }, {
                        name : 'validationMessage',
                        index : 'validationMessage',
                        width : 200,
                        hidden : false,
                        sortable: false,
                        cellattr: function (rowId, tv, rawObject, cm, rdata) {
                            return 'style="white-space: normal;'
                        }
                    }, {
                        name : 'modified',
                        index : 'modified',
                        width : 50,
                        editable: false,
                        sortable: false,
                        align: 'center',
                        formatter: modifiedFormatter
                    }
                ],
                jsonReader : {
                    page : "page",
                    total : "total",
                    records : "records",
                    root : 'history',
                    repeatitems : false
                },
                gridComplete : function() {
                    for (var i = 0; i < invalidRows.length; i++) {
                        $("#" + invalidRows[i]).find("td").addClass("ui-state-error");
                    }

                    if("${historyID}" != -1 && !selectRowUsed) {
                        jQuery("#historylist").setSelection("${historyID}", true);
                        selectRowUsed = true;
                    } else {
                        jQuery("#historylist").setSelection($("#historylist").getDataIDs()[0], true);
                    }
                },
                loadComplete : function() {
                    // this gets/sets a cookie for grid height and makes the grid resizable
                    var initialGridSize = 300;
                    if($.cookie("historyGridHeight") != null) {
                        initialGridSize = $.cookie("historyGridHeight");
                    }

                    jQuery("#historylist").jqGrid('setGridHeight', initialGridSize);

                    // allow grid resize
                    jQuery("#historylist").jqGrid('gridResize',
                    {
                        minHeight:300,
                        maxHeight:1000,
                        stop: function( event, ui ) {
                            $.cookie("historyGridHeight", ui.size.height, { expires: 10000, path: '/testproxy/history' });
                        }
                    });

                    // set row height to be a little larger
                    var grid = $("#historylist");
                    var ids = grid.getDataIDs();
                    for (var i = 0; i < ids.length; i++) {
                        grid.setRowData(ids[i], false, {height: 20+i*2});
                    }
                },
                onSelectRow : function(id) {
                    var data = jQuery("#historylist").jqGrid('getRowData', id);
                    currentHistoryId = data.id;
                    loadData(data.id);
                },
                rowList : [],
                pager : '#historynavGrid',
                sortname : 'id',
                viewrecords : true,
                sortorder : "desc",
                caption : historyCaption()
            });

        $("#historylist").jqGrid('navGrid', '#historynavGrid', {
            edit : false,
            add : false,
            del : false,
            refreshtext: 'Refresh <kbd>r</kbd>',
            searchtext: 'Search',
            cloneToTop: true
        });
    });

    function modifiedFormatter(cellvalue, options, rowObject) {
        return $('<div>').append($('<span>')
                .addClass("glyphicon " + (cellvalue ? "glyphicon-ok text-success" : "glyphicon-remove"))
                .attr("style", cellvalue ? "" : "opacity: 0.15;"))
            .append($('<span>')
                .addClass('copy-only')
                .text(cellvalue ? 'Yes' : 'No'))
            .html();
    }

    function getActiveTabId() {
        return $("#tabs .ui-state-active a.ui-tabs-anchor").attr("href");
    }

    function historyCaption() {
        let $container = $("<div>"); // necessary
        let $baseCaption = $("<h1>")
            .attr("style", "font-weight: bold; font-size: 2em; margin: 0.25em;")
            .text("History: ${profile_name}");
        if (historyFilter) {
            $baseCaption
                .append($("<span>").text(" "))
                .append($("<span>")
                .attr({
                    class: "label label-success label-small",
                    title: historyFilter
                })
                .text("Filtered"));
        }
        return $container.append($baseCaption).html();
    }

    function showPathTester() {
        // map request type
        switch(historyData.history.requestType) {
            case 'ALL':
                $('#pathTesterRequestType').val(0);
                break;
            case 'GET':
                $('#pathTesterRequestType').val(1);
                break;
            case 'PUT':
                $('#pathTesterRequestType').val(2);
                break;
            case 'POST':
                $('#pathTesterRequestType').val(3);
                break;
            case 'DELETE':
                $('#pathTesterRequestType').val(4);
                break;
        }

        $('#pathTesterURL').val($("#requestQuery").val() + "?" + $("#requestParameters").val());
        navigatePathTester();
        pathTesterSubmit();
    }

    //http://stackoverflow.com/questions/10655202/detect-multiple-keys-on-single-keypress-event-on-jquery
    //17 = CTRL, 8 = DEL, 46 = Backspace
    var map = {17: false, 8: false, 46: false};
    $(document).keydown(function(e) {
        if (e.keyCode in map) {
            map[e.keyCode] = true;
            if (map[17] && (map[8] || map[46])) {
                clearHistory();
            }
        }
    }).keyup(function(e) {
        if (e.keyCode in map) {
            map[e.keyCode] = false;
        }
    });

    /**
    This is adapted from https://code.google.com/p/google-diff-match-patch/ as instructed in the api documentation
    */
    /**
     * Convert a diff array into a pretty HTML report.
     * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples.
     * @return {string} HTML representation.
     */
    diff_prettyHtml = function(diffs) {
        var html = [];
        var pattern_amp = /&/g;
        var pattern_lt = /</g;
        var pattern_gt = />/g;
        var pattern_para = /\n/g;
        for (var x = 0; x < diffs.length; x++) {
            var op = diffs[x][0];    // Operation (insert, delete, equal)
            var data = diffs[x][1];  // Text of change.
            var text = data.replace(pattern_amp, '&amp;').replace(pattern_lt, '&lt;')
                .replace(pattern_gt, '&gt;').replace(pattern_para, '<br>');
            switch (op) {
                case DIFF_INSERT:
                    html[x] = '<ins style="background:#e6ffe6;">' + text + '</ins>';
                    break;
                case DIFF_DELETE:
                    html[x] = '<del style="background:#ffe6e6;">' + text + '</del>';
                    break;
                case DIFF_EQUAL:
                    html[x] = '<span>' + text + '</span>';
                    break;
            }
        }
        return html.join('');
    };
</script>
</body>
</html>
