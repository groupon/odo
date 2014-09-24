<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page session="false" %>
<html>
<head>
    <title>History</title>
	<%@ include file="/resources/js/webjars.include" %>
	<script src="<c:url value="/resources/js/diff_match_patch_uncompressed.js" />"></script>
	<link rel="stylesheet" type="text/css" media="screen"
             href="<c:url value="/resources/css/odo.css"/>" />
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <script type="text/javascript">
        $.jgrid.no_legacy_api = true;
        $.jgrid.useJSON = true;
    </script>

 	<style type="text/css">
	
	.has-switch {height: 30px}

 	</style>
</head>
<body>
<%@ include file="pathtester_part.jsp" %>

<nav class="navbar navbar-default" role="navigation">
	<div class="container-fluid">
	    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
	       <div class="form-group navbar-form navbar-left">
	         <input type="text" class="form-control" placeholder="Search" id="searchFilter">
	         <button class="btn btn-default" onclick='uriFilter()'>Apply Filter</button>
	         <button class="btn btn-default" onclick='clearFilter()'>Clear Filters</button>
	       </div>
	      
	      <ul class="nav navbar-nav navbar-right">
	        <li class="dropdown">
	          <a href="#" class="dropdown-toggle" data-toggle="dropdown">Options <b class="caret"></b></a>
	          <ul class="dropdown-menu">
	            <li><a href="#" onclick='clearHistory()'>Clear History</a></li>
	            <li ><a href="#" onclick='navigateScripts()'>Edit Scripts</a></li>
	          </ul>
	        </li>
	      </ul>
	    </div>
    </div>
</nav>
<div style="margin-left: 20px; margin-right: 20px;" id="historyGridDiv">
	<table id="historylist"></table>
	<div id="historynavGrid"></div>
</div>
<br>
	<div style="margin-left: 20px; margin-right: 20px;"
		id="historyContentDiv">
		<div id="tabs" style="overflow: scroll;">
			<ul>
				<li><a href="#tabs-1">Response</a></li>
				<li><a href="#tabs-2">Request</a></li>
				<li><a href="#tabs-3">Other</a></li>
			</ul>

			<div id="tabs-1">
				<div class="" style="width: 100%">
					<h3 style="display: inline">
						<span class="label label-default">Headers</span>
					</h3>
					<h3 style="display: inline;">
						<div class="btn-group btn-group-sm" style="float:right" id="responseButtons">
							<button type="button" class="btn btn-default"
								id="showModifiedResponseButton" onClick="showModifiedResponse()">Modified</button>
							<button type="button" class="btn btn-default"
								id="showOriginalResponseButton" onClick="showOriginalResponse()">Original</button>
							<button type="button" class="btn btn-default"
								id="showChangedResponseButton" onClick="showChangedResponse()">View Diff</button>
						</div>
					</h3>
					<textarea class="form-control" rows="3" style="width: 100%; margin-top: 6px"
						id="responseHeaders"></textarea>
					<textarea class="form-control" rows="3" style="width: 100%; display:none"
						id="originalResponseHeaders"></textarea>
					<div class="form-control" id = "originalResponseHeaderChange" style="width: 100%; height: 80px;overflow-y: scroll; resize:both; display:none"></div>
					<div style="clear: both"></div>
					<h3 style="display: inline;">
						<span class="label label-default">Data</span>
					</h3>
					<h3 style="display: inline;">
						<div class="btn-group btn-group-sm">
							<button type="button" class="btn btn-default"
								id="showRawResponseDataButton" onClick="showRawResponseData()">Raw</button>
							<button type="button" class="btn btn-default"
								id="showRawFormattedDataButton" onClick="showFormattedResponseData(false)">Formatted</button>
						</div>
					</h3>
					<h3 style="display: inline;">
						<span class="label label-info" id="responseTypeLabel"></span>
					</h3>
					<textarea class="form-control" rows="20" style="width: 100%"
						id="responseRaw"></textarea>
					<textarea class="form-control" rows="20" style="width: 100%; display:none"
						id="originalResponseRaw"></textarea>
					<div class="form-control" id = "originalResponseChange" style="width: 100%; height: 450px;overflow-y: scroll;  resize:both; display:none"></div>
				</div>
			</div>
			<div id="tabs-2">
				<div class="" style="float: left; width: 100%">
					<h3 style="display: inline;">
						<span class="label label-default">URL</span>
						<div class="btn-group btn-group-sm">
							<button type="button" class="btn btn-default" onClick="showPathTester()">Test Path</button>
						</div>
					</h3>
					<h3 style="display: inline">
						<div class="btn-group btn-group-sm" style="float:right" id="requestButtons">
							<button type="button" class="btn btn-default"
								id="showModifiedRequestButton" onClick="showModifiedRequestData()">Modified</button>
							<button type="button" class="btn btn-default"
								id="showOriginalButton" onClick="showOriginalRequestData()">Original</button>
							<button type="button" class="btn btn-default"
								id="showChangedButton" onClick="showChangedRequestData()">View Diff</button>
						</div>
					</h3>
					<textarea class="form-control" rows="1" style="width: 100%; margin-top: 6px"
						id="requestQuery"></textarea>
					<textarea class="form-control" rows="1" style="width: 100%; display: none"
						id="originalRequestQuery"></textarea>
					<div class="form-control" id = "originalRequestQueryChange" style="width: 100%; height: 40px;overflow-y: scroll; resize:both; display: none"></div>
					<div>
					<div>
						<h3>
							<span class="label label-default">Parameters</span>
						</h3>
					</div>
					<textarea class="form-control" rows="1" style="width: 100%; float:left"
						id="requestParameters"></textarea>
					<textarea class="form-control" rows="1" style="width: 100%; float: left; display: none"
						id="originalRequestParameters"></textarea>
					<div class="form-control" style="width: 100%; height: 40px; float: left; display: none; overflow-y: scroll; resize:both"
						id="originalRequestParametersChanged"></div>
					</div>
					<div style="clear: both"></div>
					<div>
						<h3>
							<span class="label label-default">Headers</span>
						</h3>
					</div>
					<textarea class="form-control" rows="3" style="width: 100%; float:left"
						id="requestHeaders"></textarea>
					<textarea class="form-control" rows="3" style="width: 100%; float: left; display: none"
						id="originalRequestHeaders"></textarea>
					<div class="form-control" style="width: 100%; height: 80px; float: left; display: none; overflow-y: scroll; resize:both"
						id="originalRequestHeadersChanged"></div>
					<div style="clear: both"></div>
					<div>
						<h3>
							<span class="label label-default">POST Data</span>
						</h3>
					</div>
					<textarea class="form-control" rows="3" style="width: 100%; float:left"
						id="requestPOSTData"></textarea>
					<textarea class="form-control" rows="3" style="width: 100%; float: left; display: none"
						id="originalRequestPOSTData"></textarea>
					<div class="form-control" style="width: 100%; height: 80px; float: left; display: none; overflow-y: scroll; resize:both"
						id="originalRequestPOSTDataChanged"></div>
				</div>
			</div>
			<div id="tabs-3">
				<div class="" style="float: left; width: 100%; overflow: scroll;">
					<div>
						<h3>
							<span class="label label-default">CURL Command</span>
						</h3>
					</div>
				</div>
				<textarea class="form-control" rows="3" style="width: 100%"
					id="curlCommand"></textarea>
			</div>
		</div>
	</div>
	<script>
		var clientUUID = '${clientUUID}';

		function navigateScripts() {
			window.open('<c:url value = '/scripts' />');
		}

		function noenter() {
			return !(window.event && window.event.keyCode == 13);
		}

		function clearHistory() {
			$.ajax({
				type : "POST",
				url : '<c:url value="/api/history/${profile_id}"/>',
				data : ({
					clientUUID : clientUUID,
					_method : 'DELETE'
				}),
				success : function(data) { //this is the data that comes back from the server (the array<array<object>>) 
					historyList.trigger("reloadGrid");
				},
				error : function(xhr, ajaxOptions, thrownError) {
					$('#info').html("Whoops!");
				}
			});
		}

		function uriFilter() {
			var filter = $("#searchFilter").val();
			jQuery("#historylist")
					.jqGrid(
							'setGridParam',
							{
								url : '<c:url value="/api/history/${profile_id}"/>?clientUUID=${clientUUID}&source_uri[]='
										+ filter,
								page : 1
							}).trigger("reloadGrid");
			$("#searchFilter").val("");
		}

		function clearFilter() {
			jQuery("#historylist")
					.jqGrid(
							'setGridParam',
							{
								url : '<c:url value="/api/history/${profile_id}"/>?clientUUID=${clientUUID}',
								page : 1
							}).trigger("reloadGrid");
		}

		function scrollableFormatter(cellvalue, options, rowObject) {
			// this divId will be used to get pop over content
			var divId = options.colModel.name + '_' + currentHistoryId;
			var scrollView = '<div id="' + divId + '" style="overflow-y:scroll;">';
			scrollView += cellvalue + '</div>';
			return scrollView;
		}

		var currentHistoryId = -1;

		// this just sets the current path ID so that other formatters can use it
		function idFormatter(cellvalue, options, rowObject) {
			currentHistoryId = cellvalue;
			return cellvalue;
		}

		var invalidRows = []
		function validFormatter(cellvalue, options, rowObject) {
			if (cellvalue == false) {
				invalidRows[invalidRows.length] = options.rowId;
			}
			return cellvalue;
		}
		
		function showOriginalResponse(){
			$("#originalResponseHeaders").val(historyData.history.originalResponseHeaders);
			if(historyData.history.responseContentType == null ||
			    historyData.history.responseContentType.toLowerCase().indexOf("application/json") == -1 ||
			    historyData.history.responseData == "" || $.cookie("formatted") == "false"){
			        $("#originalResponseRaw").val(originalResponseRaw);
			} else {
			    if(historyData.history.formattedOriginalResponseData == "") {
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
			document.getElementById("showChangedResponseButton").className = "btn btn-default";
			document.getElementById("showOriginalResponseButton").className = "btn btn-primary";
			document.getElementById("showModifiedResponseButton").className = "btn btn-default";
		}
		
		var dmp = new diff_match_patch();
		function showChangedData(originalData, changedData, originalID, changedID, modifiedID){
			var d = dmp.diff_main(originalData, changedData);
			dmp.diff_cleanupSemantic(d);
			var ds = diff_prettyHtml(d);
			//document.getElementById(changedID).innerHTML = ds.replace(/[^\x00-\x7F]/g, "");
			document.getElementById(changedID).innerHTML = ds;
			$("#"+originalID).hide();
			$("#"+changedID).show();
			$("#"+modifiedID).hide();
		}
		
		function showChangedResponse(){
			showFormattedResponseData(true);
		}

		function showChangedResponsePostFormattedAJAX() {
			showChangedData(historyData.history.formattedOriginalResponseData.replace(/[<]/g, '&lt;'), historyData.history.formattedResponseData.replace(/[<]/g, '&lt;'), "originalResponseRaw", "originalResponseChange", "responseRaw");
			document.getElementById("showChangedResponseButton").className = "btn btn-primary";
			document.getElementById("showOriginalResponseButton").className = "btn btn-default";
			document.getElementById("showModifiedResponseButton").className = "btn btn-default";
		}
		
		function showModifiedResponse(){
			$("#responseHeaders").val(historyData.history.responseHeaders);
			if(historyData.history.responseContentType == null ||
			    historyData.history.responseContentType.toLowerCase().indexOf("application/json") == -1 ||
			    historyData.history.responseData == "" || $.cookie("formatted") == "false") {
			        $("#responseRaw").val(responseRaw);
			} else {
			    if(historyData.history.formattedResponseData == "") {
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
			document.getElementById("showOriginalResponseButton").className = "btn btn-default";
			document.getElementById("showChangedResponseButton").className = "btn btn-default";
			document.getElementById("showModifiedResponseButton").className = "btn btn-primary";
		}
		
		var responseRaw, originalResponseRaw;
		function showFormattedResponseData(forDiff) {
		    $.ajax({
		        type : "GET",
		        url : '<c:url value="/api/history/${profile_id}/"/>'
		            + currentHistoryId,
		        data : 'clientUUID=${clientUUID}&format=formattedAll',
		        success : function(data) {
		            historyData = data;
		            if (forDiff == true) {
		                showChangedResponsePostFormattedAJAX();
		            } else {
			            $("#responseRaw").val(data.history.formattedResponseData);
			            $("#originalResponseRaw").val(data.history.formattedOriginalResponseData);
			            $.cookie("formatted", "true");
			            document.getElementById("showRawFormattedDataButton").className = "btn btn-primary";
			            document.getElementById("showRawResponseDataButton").className = "btn btn-default";
		            }
		        }
		    });
		}

		function showRawResponseData() {
			responseRaw = historyData.history.responseData.replace(/[<]/g, '&lt;');
			originalResponseRaw = historyData.history.originalResponseData.replace(/[<]/g, '&lt;');
			$("#responseRaw").val(responseRaw);
			$("#originalResponseRaw").val(originalResponseRaw);
			$.cookie("formatted", "false");
			document.getElementById("showRawResponseDataButton").className = "btn btn-primary";
			document.getElementById("showRawFormattedDataButton").className = "btn btn-default";
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
			document.getElementById("showOriginalButton").className = "btn btn-primary";
			document.getElementById("showChangedButton").className = "btn btn-default";
			document.getElementById("showModifiedRequestButton").className = "btn btn-default";
		}
		
		function showChangedRequestData(){
			showChangedData(historyData.history.originalRequestURL, historyData.history.requestURL, "originalRequestQuery", "originalRequestQueryChange", "requestQuery");
			showChangedData(historyData.history.originalRequestParams, historyData.history.requestParams, "originalRequestParameters", "originalRequestParametersChanged", "requestParameters");
			showChangedData(historyData.history.originalRequestHeaders, historyData.history.requestHeaders, "originalRequestHeaders", "originalRequestHeadersChanged", "requestHeaders");
			showChangedData(historyData.history.originalRequestPostData, historyData.history.requestPostData, "originalRequestPOSTData", "originalRequestPOSTDataChanged", "requestPOSTData");
			document.getElementById("showChangedButton").className = "btn btn-primary";
			document.getElementById("showOriginalButton").className = "btn btn-default";
			document.getElementById("showModifiedRequestButton").className = "btn btn-default";
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
			document.getElementById("showOriginalButton").className = "btn btn-default";
			document.getElementById("showChangedButton").className = "btn btn-default";
			document.getElementById("showModifiedRequestButton").className = "btn btn-primary";
		}


		function showCurlCommand() {
			var headers = historyData.history.requestHeaders.split('\n');
			var requestType = historyData.history.requestType;

			var commandLine = "curl --insecure --proxy " + window.location.hostname + ":9090 -X " + requestType;
			for ( var x in headers) {
				commandLine += " --header \"" + headers[x] + "\"";
			}

			if (historyData.history.requestPostData != null
					&& historyData.history.requestPostData !== "") {
				commandLine += " -d \"" + historyData.history.requestPostData
						+ "\"";
			}

			commandLine += " \"" + historyData.history.originalRequestURL;

			if (historyData.history.originalRequestParams != null
					&& historyData.history.originalRequestParams !== "") {
				commandLine += "?" + historyData.history.originalRequestParams;
			}

			commandLine += "\"";

			$("#curlCommand").val(commandLine);
		}

		var historyData;
		function loadData(historyId) {
			$
					.ajax({
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

                            showModifiedRequestData();
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
                                document.getElementById("showModifiedResponseButton").className = "btn btn-primary";
                                document.getElementById("showModifiedRequestButton").className = "btn btn-primary";
                            } else {
                                // set the query back to the original query data
                                $("#requestQuery").val(data.history.originalRequestURL);

                                $("#responseButtons").hide();
                                $("#requestButtons").hide();
                            }

                            showCurlCommand();
                        }
				    });
		}

		$(document).ready(function() {
			$("#tabs").tabs();
			$("#tabs").css("overflow", "scroll");
			$("#radioset").buttonset();

			$('#searchFilter').keydown(function(event) {
				if (event.keyCode == 13) {
					uriFilter();
					return false;
				}
			});
			
			// bind window resize to fix grid width
			$(window).bind('resize', function() {
				$("#historylist").setGridWidth($("#historyGridDiv").width());
			});
		});

		var selectRowUsed = false;
		var historyList = jQuery("#historylist");
		historyList
				.jqGrid({
					url : '<c:url value="/api/history/${profile_id}"/>?clientUUID=${clientUUID}',
					height : 300,
					autowidth : true,
					pgbuttons : true, // disable page control like next, back button
					pgtext : null,
					datatype : "json",
					page : "${page}",
					colNames : [ 'ID', 'Created At', 'Method', 'Query',
							'Query Params', 'Response Code', 'Valid', 'Message', 'Modified' ],
					colModel : [{
						name : 'id',
						index : 'id',
						width : 55,
						hidden : true,
						formatter : idFormatter
					}, {
						name : 'createdAt',
						index : 'createdAt',
						width : 250,
						editable : false,
						align : 'center'
					}, {
						name : 'requestType',
						index : 'requestType',
						width : 90,
						editable : false,
						align : 'center'
					}, {
						name : 'originalRequestURL',
						index : 'originalRequestURL',
						width : 300,
						editable : false,
						formatter : scrollableFormatter
					}, {
						name : 'requestParams',
						index : 'requestParams',
						width : 300,
						editable : false,
						formatter : scrollableFormatter
					}, {
						name : 'responseCode',
						index : 'responseCode',
						width : 140,
						editable : false,
						align : 'center'
					}, {
						name : 'valid',
						index : 'valid',
						width : 55,
						hidden : true,
						formatter : validFormatter
					}, {
						name : 'validationMessage',
						index : 'validationMessage',
						width : 300,
						hidden : false
					}, {
						name : 'modified',
						index : 'modified',
						width : 80,
						editable: false,
                        edittype: 'checkbox',
                        align: 'center',
                        editoptions: { value:"True:False" },
                        formatter: modifiedFormatter,
                        formatoptions: {disabled: false}
					}, ],
					jsonReader : {
						page : "page",
						total : "total",
						records : "records",
						root : 'history',
						repeatitems : false
					},
					gridComplete : function() {
					    for (var i = 0; i < invalidRows.length; i++) {
					        $("#" + invalidRows[i]).find("td").addClass(
					            "ui-state-error");
					    }

                        if("${historyID}" != -1 && !selectRowUsed) {
                            jQuery("#historylist").setSelection("${historyID}", true);
                            selectRowUsed = true;
                        } else {
                            jQuery("#historylist").setSelection(
                            $("#historylist").getDataIDs()[0], true);
                        }
                    },
                    onSelectRow : function(id) {
                        var data = jQuery("#historylist").jqGrid('getRowData',
                            id);
                        currentHistoryId = data.id;
                        loadData(data.id);
                    },
                    rowList : [],
                    pager : '#historynavGrid',
                    sortname : 'id',
                    viewrecords : true,
                    sortorder : "desc",
                    caption : '<font size="5">History</font>'
                });
        historyList.jqGrid('navGrid', '#historynavGrid', {
            edit : false,
            add : false,
            del : false
        }, {}, {}, {});

		function modifiedFormatter( cellvalue, options, rowObject ) {
		    var checkedValue = 0;
		    if (cellvalue == true) {
		        checkedValue = 1;
		    }
		    var newCellValue = '<input id="modified_' + rowObject.pathId + '"type="checkbox" offval="0" value="' + checkedValue + '"';
		    if (checkedValue == 1) {
		        newCellValue += 'checked="checked"';
		    }
		    newCellValue += ' disabled=true>';
		    return newCellValue;
		}
		
		function showPathTester() {
			$('#pathTesterURL').val($("#requestQuery").val() + "?" + $("#requestParameters").val());
			navigatePathTester();
			pathTesterSubmit();
		}

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
