
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ page session="false"%>
    <head>
        <title>Edit Profile: ${profile_name}</title>
        <%@ include file="/resources/js/webjars.include" %>
        <link rel="stylesheet" type="text/css" media="screen"
             href="<c:url value="/resources/css/odo.css"/>" />
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <style type="text/css">
            .detailsLeft
            {
                overflow: hidden;
                margin: 12px;
                padding: 8px;
                max-width: 50%;
                display:inline-block;
                vertical-align: top;
            }

            .detailsRight
            {
                margin: 12px;
                margin-left: 0px;
                display:inline-block;
                vertical-align: top;
            }

            .overrideParameters
            {
                padding: 12px;
            }

            #details {
                position: sticky;
                top: 10px;
            }

            #container, #tabs {
                white-space: nowrap;
            }

            #container>div {
                display:inline-block;
                vertical-align: top;
            }

            #listContainer
            {
                min-width: 400px;
                margin-right: 20px;
            }

            #editDiv {
                width: 45vw;
                max-width: 45vw; /* ALLOWS FOR SCROLLING TO THE END OF THE DIV IF LARGER THAN WINDOW */
            }

            #serverEdit {
                display:none;
            }

            #nav>li>a { /* MAKES THE PILLS SMALLER */
                padding-top:3px !important;
                padding-bottom:3px !important;
            }

            #pg_packagePager .ui-pg-table, #pg_servernavGrid .ui-pg-table, #servernavGrid_left, #packagePager_left { /* KEEPS PAGER BUTTONS THE RIGHT SIZE */
                width: auto !important;
            }

            /* This changes the color of the active pill */
            .nav-pills>li.active>a, .nav-pills>li.active>a:hover, .nav-pills>li.active>a:focus {
                background-color:#e9e9e9 !important;
                color: black !important;
            }

            /* This changes the color of the inactive pills */
            .nav-pills>li>a {
                background-color:#767676 !important;
                color: black !important;
            }

            /* This changes the color of the inactive pills */
            .nav-pills>li>a:hover, .nav-pills>li>a:focus {
                background-color:#696969 !important;
                color: black !important;
            }

        </style>

        <script type="text/javascript">
            $.jgrid.no_legacy_api = true;
            $.jgrid.useJSON = true;
        </script>
        <script type="text/javascript">

            var clientUUID = '${clientUUID}';
            var currentPathId = -1;
            var editServerGroupId = 0;

            function navigateHelp() {
                window.open("https://github.com/groupon/odo#readme","help");
            }

            function navigateEditGroups() {
                window.open('<c:url value = '/group' />', "edit-groups");
            }

            function navigateRequestHistory() {
                window.open('<c:url value='/history/${profile_id}'/>?clientUUID=${clientUUID}', '<c:url value='/history/${profile_id}'/>?clientUUID=${clientUUID}');
            }

            function navigateProfiles() {
                window.location = '<c:url value='/profiles'/>';
            }

            function navigatePathPriority() {
                window.location = '<c:url value = '/pathorder/${profile_id}'/>';
            }

            function updateStatus() {
                var status = $("#status");
                if (${isActive} == true) {
                    status.html('<button id="make_active" class="btn btn-default" onclick="changeActive(\'false\')">Deactivate Profile</button>');
                } else {
                    status.html('<button id="make_active" class="btn btn-danger" onclick="changeActive(\'true\')">Activate Profile</button>');
                }

                // set client ID information
                var clientInfo = $("#clientInfo");
                var clientInfoHTML = "";
                if (clientUUID == '-1') {
                    clientInfoHTML = "<li id='clientButton'><a href='#' data-toggle='tooltip' data-placement='bottom' title='Click here to manage clients.' onclick='manageClientPopup()'>Client UUID: Default</a>";
                } else {
                    clientInfoHTML = "<li id='clientButton'><a href='#' data-toggle='tooltip' data-placement='bottom' title='Click here to manage clients.' onclick='manageClientPopup()'>Client UUID: " + clientUUID + "</a>";
                }

                clientInfoHTML += '</ul></li>'
                clientInfo.html(clientInfoHTML);
            }

            function manageClients() {
                var url = '<c:url value="/edit/${profile_id}/clients"/>';
                window.location.href = url;
            }

            function changeClientFriendlyNamePopup() {
                $("#changeClientFriendlyNameDialog").dialog({
                    title: "Set Client Friendly Name For: ${profile_name}",
                    modal: true,
                    position:['top',20],
                    buttons: {
                      "Submit": function() {
                          changeClientFriendlyNameSubmit();
                      },
                      "Cancel": function() {
                          $("#changeClientFriendlyNameDialog").dialog("close");
                      }
                    }
                });
            }

            function changeClientFriendlyNameSubmit() {
                var value = $('#changeClientFriendlyName').val();
                if (value == "${clientFriendlyName}") {
                    $("#changeClientFriendlyNameDialog").dialog("close");
                    return;
                }

                $.ajax({
                    type:"POST",
                    url: '<c:url value="/api/profile/${profile_id}/clients/${clientUUID}"/>',
                    data: {friendlyName: value},
                    success: function() {
                        var url = '<c:url value="/edit/${profile_id}"/>?clientUUID=${clientUUID}';
                        window.location.href = url;
                    },
                    error: function(xhr) {
                        var json;
                        try {
                            json = $.parseJSON(xhr.responseText);
                            $("#friendlyNameError").html(json.error.message);
                        } catch(e) {
                            $("#friendlyNameError").html("An unknown error occurred");
                        }

                    }
                });
            }

            function changeClientPopup() {
                $("#switchClientDialog").dialog({
                    title: "Switch Client For: ${profile_name}",
                    modal: true,
                    position:['top',20],
                    buttons: {
                      "Submit": function() {
                          changeClientSubmit();
                      },
                      "Cancel": function() {
                          $("#switchClientDialog").dialog("close");
                      }
                    }
                });
                $("#switchClientName").select();
            }

            function changeClientSubmit() {
                $.removeCookie("UUID", { expires: 10000, path: '/testproxy/' });
                var value = $('#switchClientName').val();
                var url = '<c:url value="/edit/${profile_id}"/>?clientUUID=' + value;
                window.location.href = url;
            }

            function changeActive(value){
                $.ajax({
                    type:"POST",
                    url: '<c:url value="/api/profile/${profile_id}/clients/${clientUUID}"/>',
                    data: "active=" + value,
                    success: function(){
                        window.location.reload();
                    }
                });
            }

            function resetProfile(){
                var active = ${isActive};

                $.ajax({
                    type:"POST",
                    url: '<c:url value="/api/profile/${profile_id}/clients/${clientUUID}"/>',
                    data: {reset: true},
                    success: function(){
                        // set the profile to active if it was active previously
                        // reset deactivates it
                        if (active === true) {
                            changeActive(true);
                        } else {
                            // just reload
                            window.location.reload();
                        }
                    }
                });
            }

            function deleteServer(id) {
                $.ajax({
                    type: 'POST',
                    url: '<c:url value="/api/edit/server/"/>' + id,
                    data: ({_method: 'DELETE'}),
                    success: function(){

                    }
                });
            }

            // This overrides the jgrid delete button to be more REST friendly
            $.extend($.jgrid.del, {
                mtype: "DELETE",
                serializeDelData: function () {
                    return ""; // don't send and body for the HTTP DELETE
                },
                onclickSubmit: function (params, postdata) {
                    params.url += '/' + encodeURIComponent(postdata);
                }
            });

            function requestTypeFormatter( cellvalue, options, rowObject ) {
                if (cellvalue == 0) {
                    return "ALL";
                } else if (cellvalue == 1) {
                    return "GET";
                } else if (cellvalue == 2) {
                    return "PUT";
                } else if (cellvalue == 3) {
                    return "POST";
                } else if (cellvalue == 4) {
                    return "DELETE";
                }
            }

            // Keeps the list of pills updated
            function updateDetailPills() {
                var pathToLoad = currentPathId;

                // first get the data of all of the existing pills
                // this will be used to mark which pills are kept/deleted
                var existingPills = [];
                $(".nav-pills>li").each( function( index, element ) {
                    // use $(this)
                    var id = $(this).attr("id");
                    existingPills[existingPills.length] = id;
                });

                // look through the overrides list to see which are enabled/active
                var ids = jQuery("#packages").jqGrid('getDataIDs');
                for (var i = 0; i < ids.length; i++) {
                    var rowdata = $("#packages").getRowData(ids[i]);

                    // check to see if response or request is enabled
                    // or if this is the currently selected row
                    if ($("#request_enabled_" + rowdata.pathId).prop("checked") === true ||
                        $("#response_enabled_" + rowdata.pathId).prop("checked") === true ||
                        rowdata.pathId === currentPathId) {

                        if (pathToLoad === -1) {
                            pathToLoad = rowdata.pathId;
                        }

                        if ($.inArray(rowdata.pathId, existingPills) !== -1) {
                            // mark as seen in the existingpills array
                            // since it exists we don't need to add it
                            existingPills[$.inArray(rowdata.pathId, existingPills)] = -1;
                        } else {
                            // add it to the <ul> pill nav
                            $('<li id="' + rowdata.pathId + '"><a href="#tab'+rowdata.pathId+'" data-toggle="tab">' + rowdata.pathName + '</a></li>').appendTo('#nav');
                        }
                    }
                }

                // remove tabs that should no longer exist
                // at this point the existingPills list is just the pills that need to be removed
                for (var x = 0; x < existingPills.length; x++) {
                    if (existingPills[x] !== -1) {
                        //console.log(existingPills[x]);
                        $("#nav").find("#" + existingPills[x]).remove();

                        // reset pathToLoad if it is equal to the removed path
                        if (pathToLoad === existingPills[x]) {
                            pathToLoad = -1;
                        }
                    }
                }

                // make all pills non-active
                $(".nav-pills>li").removeClass("active")

                // make the currently selected pill active
                $("#nav").find("#" + pathToLoad).addClass("active");

                // register click events on all of the nav pills to load the appropriate detail data
                $('.nav-pills > li > a').click( function() {
                    loadPath($(this).parent().attr("id"));
                });

                // load the currently selected path data
                loadPath(pathToLoad);
            }

            // common function for grid reload
            function reloadGrid(gridId) {
                jQuery(gridId).setGridParam({datatype:'json', page:1}).trigger("reloadGrid");
            }

            function responseEnabledFormatter( cellvalue, options, rowObject ) {
                var checkedValue = 0;
                if (cellvalue == true) {
                    checkedValue = 1;
                }
                var newCellValue = '<input id="response_enabled_' + rowObject.pathId + '" onChange="responseEnabledChanged(response_enabled_' + rowObject.pathId + ')" type="checkbox" offval="0" value="' + checkedValue + '"';
                if (checkedValue == 1) {
                    newCellValue += 'checked="checked"';
                }
                newCellValue += '>';
                return newCellValue;
            }

            function responseEnabledChanged(element) {
                var id = element.id;
                var pathId = element.id.substring(17, element.id.length);

                var enabled = element.checked;
                if (enabled == true) {
                    enabled = 1;
                } else {
                    enabled = 0;
                }

                var type = type + 'Enabled';
                $.ajax({
                    type:"POST",
                    url: '<c:url value="/api/path/"/>' + pathId,
                    data: 'responseEnabled=' + enabled + '&clientUUID=' + clientUUID,
                    error: function() {
                        alert("Could not properly set value");
                    },
                    success: function() {
                        updateDetailPills();
                    }
                });
            }

            function requestEnabledFormatter( cellvalue, options, rowObject ) {
                var checkedValue = 0;
                if (cellvalue == true) {
                    checkedValue = 1;
                }
                var newCellValue = '<input id="request_enabled_' + rowObject.pathId + '" onChange="requestEnabledChanged(request_enabled_' + rowObject.pathId + ')" type="checkbox" offval="0" value="' + checkedValue + '"';
                if (checkedValue == 1) {
                    newCellValue += 'checked="checked"';
                }
                newCellValue += '>';
                return newCellValue;
            }

            function requestEnabledChanged(element) {
                var id = element.id;
                var pathId = element.id.substring(16, element.id.length);

                var enabled = element.checked;
                if (enabled == true) {
                    enabled = 1;
                } else {
                    enabled = 0;
                }

                var type = type + 'Enabled';
                $.ajax({
                    type:"POST",
                    url: '<c:url value="/api/path/"/>' + pathId,
                    data: 'requestEnabled=' + enabled + '&clientUUID=' + clientUUID,
                    error: function() {
                        alert("Could not properly set value");
                    },
                    success: function() {
                        updateDetailPills();
                    }
                });
            }

            function getRequestTypes() {
                return "0:ALL;1:GET;2:PUT;3:POST;4:DELETE";
            }

            var currentServerId = -1;
            function serverIdFormatter( cellvalue, options, rowObject ) {
                currentServerId = cellvalue;
                return cellvalue;
            }

            // called when an enabled checkbox is changed
            function serverEnabledChanged(id) {
                var enabled = $("#serverEnabled_" + id).is(":checked");

                $.ajax({
                    type:"POST",
                    url: '<c:url value="/api/edit/server/"/>' + id,
                    data: "enabled=" + enabled,
                    success: function() {
                        reloadGrid("#serverlist");
                    },
                    error: function(xhr) {
                        document.getElementById("serverEnabled_" + id).checked = origEnabled;

                        alert("Error updating host entry.  Please make sure the hostsedit RMI server is running");
                    }
                });
            }

            // formats the enable/disable check box
            function serverEnabledFormatter( cellvalue, options, rowObject ) {
                var checkedValue = 0;
                if (cellvalue == true) {
                    checkedValue = 1;
                }

                var newCellValue = '<input id="serverEnabled_' + currentServerId + '" onChange="serverEnabledChanged(' + currentServerId + ')" type="checkbox" offval="0" value="' + checkedValue + '"';

                if (checkedValue == 1) {
                    newCellValue += 'checked="checked"';
                }

                newCellValue += '>';

                return newCellValue;
            }

            // format button to download a server certificate
            function certDownloadButtonFormatter(cellvalue, options, rowObject) {
                var format = "<button type='button' class='btn btn-xs' onclick='downloadCert(\"" + rowObject.srcUrl + "\")'><span class='glyphicon glyphicon-download'></span></button>";
                return format;
            }


            function downloadCert(serverHost) {
                window.location = '<c:url value="/cert/"/>' + serverHost;
            }
            
            function destinationHostFormatter (cellvalue, options, rowObject)
            {
            	if (cellvalue === "") {
            		return "<span style=\"display: none\">hidden</span><span class=\"ui-icon ui-icon-info\" style=\"float: left; margin-right: .3em;\" title=\"Click here to enter a destination address if it is different from the source\"></span>Forwarding to source";
            	}
            	return cellvalue;
            }
            
            function destinationHostUnFormatter (cellvalue, options, rowObject)
            {
            	// "hidden" is hidden text in the input box
            	if (cellvalue.indexOf("hidden") === 0) {
            		return "";
            	}
            	return cellvalue;
            }

            $(document).ready(function () {
                // turn on all tooltips
                $("#statusBar").tooltip({selector: '[data-toggle=tooltip]'});
                $.ajax({
                    type : "GET",
                    url : '<c:url value="/api/profile/${profile_id}/clients/"/>' + $.cookie("UUID"),
                    success : function(data) {
                        if (data.client == null || (data.client.uuid == -1 && $.cookie("UUID") != null)) {
                            $.removeCookie("UUID", { expires: 10000, path: '/testproxy/' });
                            document.location.href =  "http://" + document.location.hostname + ":" +  document.location.port + document.location.pathname;
                        } else {
                            if ("${clientUUID}" == "-1" && $.cookie("UUID") != null) {
                                document.location.href =  "http://" + document.location.hostname + ":" +  document.location.port + document.location.pathname +
                                    "?" + 'clientUUID='+$.cookie("UUID");
                            } else if ("${clientUUID}" != "-1") {
                                $.cookie("UUID", "${clientUUID}", { expires: 10000, path: '/testproxy/' });
                            }
                        }
                    }
                });
                'use strict';

                updateStatus();
                $("#responseOverrideSelect").select2({dropdownAutoWidth : true});
                $("#requestOverrideSelect").select2({dropdownAutoWidth : true});

                var serverList = jQuery("#serverlist");
                var initServerWidth = 0;
                serverList.jqGrid({
                    autowidth : false,
                    caption : 'API Servers',
                    cellEdit : true,
                    cellurl : '/testproxy/api/edit/server',
                    colNames : [ 'Cert', 'ID', 'Enabled', 'Source Hostname/IP',
                            'Destination Hostname/IP', 'Host Header (optional)' ],
                    colModel : [ {
                        name : 'cert',
                        index: 'cert',
                        width: 35,
                        formatter: certDownloadButtonFormatter,
                        editable: false
                    }, {
                        name : 'id',
                        index : 'id',
                        width : 15,
                        hidden : true,
                        formatter: serverIdFormatter
                    }, {
                        name: 'hostsEntry.enabled',
                        path: 'hostsEntry.enabled',
                        width: 50,
                        editable: true,
                        align: 'center',
                        edittype: 'checkbox',
                        formatter: serverEnabledFormatter,
                        formatoptions: {disabled : false}
                    }, {
                        name : 'srcUrl',
                        index : 'srcUrl',
                        width : 160,
                        editable : true,
                        edittype:"text",
                        editrules:{required:true, custom:true, custom_func: srcValidation},
                        formoptions:{label: "Source Hostname/IP (*)"}
                    }, {
                        name : 'destUrl',
                        index : 'destUrl',
                        width : 160,
                        editable : true,
                        formatter : destinationHostFormatter,
                        unformat : destinationHostUnFormatter,
                        editoptions:{title:"default: forwards to source"},
                        editrules:{required:false, custom:true, custom_func: destValidation},
                    }, {
                        name : 'hostHeader',
                        index : 'hostHeader',
                        width : 175,
                        editable : true
                     }],
                    jsonReader : {
                        page : "page",
                        total : "total",
                        records : "records",
                        root : 'servers',
                        repeatitems : false
                    },
                    afterEditCell : function(rowid, cellname, value, iRow, iCol) {
                        if (cellname == "srcUrl") {
                            serverList.setGridParam({
                                cellurl : '<c:url value="/api/edit/server/"/>' + rowid
                                        + '/src'
                            });
                        } else if (cellname == "destUrl") {
                            serverList.setGridParam({
                                cellurl : '<c:url value="/api/edit/server/"/>' + rowid
                                        + '/dest'
                            });
                        } else if (cellname == "hostHeader") {
                            serverList.setGridParam({
                                cellurl : '<c:url value="/api/edit/server/"/>' + rowid
                                        + '/host'
                            });
                        }
                    },
                    afterSaveCell : function() {
                        serverList.trigger("reloadGrid");
                    },
                    loadComplete: function(data) {
                        // hide host enable/disable if host editor is not available
                        if (data.hostEditor == false) {
                            serverList.hideCol("hostsEntry.enabled");
                        }
                    },
                    gridComplete: function() {
                        initServerWidth = serverList.jqGrid('getGridParam', 'width');
                    },
                    datatype : "json",
                    height: "100%",
                    hiddengrid: false,
                    loadonce: true,
                    pager : '#servernavGrid',
                    pgbuttons : false,
                    pgtext : null,
                    rowNum: 10000,
                    rowList : [],
                    sortname : 'srcUrl',
                    sortorder : "asc",
                    url : '<c:url value="/api/edit/server?profileId=${profile_id}&clientUUID=${clientUUID}"/>',
                    viewrecords : true
                });
                serverList.jqGrid('navGrid', '#servernavGrid', {
                    edit : false,
                    add : true,
                    del : true,
                    search: false,
                    addtext:"Add API server",
                    deltext:"Delete API server"
                },
                {},
                {
                    url: '<c:url value="/api/edit/server"/>?profileId=${profile_id}&clientUUID=${clientUUID}',
                    reloadAfterSubmit: false,
                    closeAfterAdd: true,
                    closeAfterEdit:true,
                    topinfo:"Fields marked with a (*) are required.",
                    width: 400,
                    beforeShowForm: function(formid) {
                        /* CREATE PLACEHOLDERS FOR ADD FORM. */
                        /* INITIALLY, GRAY */
                        $("#srcUrl").val(getExampleText("src"));
                        $("#srcUrl").css("color", "gray");

                        $("#destUrl").val(getExampleText("dest"));
                        $("#destUrl").css("color", "gray");
                    },
                    afterShowForm: function(formid) {
                        /* SHIFT INITIAL FOCUS TO CANCEL TO MINIMIZE ACCIDENTAL CREATION OF
                            INCORRECT HOSTNAME.
                         */
                        $("#cData").focus();

                        var src = true;
                        $("#srcUrl").focus(function(){
                            if( src ) {
                                $("#srcUrl").val("");
                                $("#srcUrl").css("color", "black");
                                src = false;
                            }
                        });

                        var dest = true;
                        $("#destUrl").focus(function(){
                            if( dest ) {
                                $("#destUrl").val("");
                                $("#destUrl").css("color", "black");
                                dest = false;
                            }
                        });
                    },
                    afterSubmit: function () {
                        reloadGrid("#serverlist");
                        return [true];
                    }
                },
                {
                    mtype: 'DELETE',
                    url: '<c:url value="/api/edit/server"/>',
                    reloadAfterSubmit: false,
                    afterSubmit: function () {
                        reloadGrid("#serverlist");
                        return [true];
                    }
                });
                serverList.jqGrid('gridResize');

                serverList.jqGrid('navButtonAdd', '#servernavGrid', {
                    caption: "Change font size",
                    buttonicon: "ui-icon-gear",
                    title: "Change the table font size",
                    id: "resize_server",
                    onClickButton: function() {
                        resizeFontDialog( "#serverlist", "API Servers" );
                    }
                });

                var serverGroupList = jQuery("#serverGroupList");
                serverGroupList.jqGrid({
                    autowidth : false,
                    cellEdit : true,
                    colNames : [ 'ID', 'Name'],
                    colModel : [ {
                        name : 'id',
                        index : 'id',
                        width : 55,
                        hidden : true
                    }, {
                        name: 'name',
                        path: 'name',
                        width: 300,
                        editable: true,
                        align: 'left',
                        formatoptions: {disabled : false}
                    }],
                    jsonReader : {
                        page : "page",
                        total : "total",
                        records : "records",
                        root : 'servergroups',
                        repeatitems : false
                    },
                    afterEditCell : function(rowid, cellname, value, iRow, iCol) {
                        if (cellname == "name") {
                            editServerGroupId = rowid;
                            serverGroupList.setGridParam({
                                cellurl : '<c:url value="/api/servergroup/"/>' + rowid + "?profileId=${profile_id}"
                            });
                        }
                    },
                    datatype : "json",
                    height: "auto",
                    loadonce: true,
                    pager : '#serverGroupNavGrid',
                    pgbuttons : false,
                    pgtext : null,
                    rowNum: 10000,
                    rowList : [],
                    sortname : 'name',
                    sortorder : "desc",
                    url : '<c:url value="/api/servergroup?profileId=${profile_id}"/>',
                    viewrecords : true
                });

                serverGroupList.jqGrid('navGrid', '#serverGroupNavGrid', {
                    edit : false,
                    add : true,
                    del : true,
                    search: false
                },
                {},
                {
                    url: '<c:url value="/api/servergroup"/>?profileId=${profile_id}&clientUUID=${clientUUID}',
                     reloadAfterSubmit: false,
                     closeAfterAdd: true,
                     afterSubmit: function () {
                         reloadGrid("#serverGroupList");
                         return [true];
                     }
                },
                {
                    mtype: 'DELETE',
                    reloadAfterSubmit: false,
                    closeAfterAdd:true,
                    closeAfterEdit:true,
                    afterSubmit: function () {
                        reloadGrid("#serverGroupList");
                        return [true];
                    },
                    onclickSubmit: function(rp_ge, postdata) {
                        rp_ge.url = '<c:url value="/api/servergroup/" />' + editServerGroupId + "?profileId=${profile_id}";
                        return [true];
                    }
                },
                {});
                serverGroupList.jqGrid('gridResize');

                var grid = $("#packages");
                var initGridWidth = 0;
                grid.jqGrid({
                    autowidth: false,
                    caption: "Paths",
                    cellurl : '<c:url value="/api/path?profileIdentifier=${profile_id}&clientUUID=${clientUUID}"/>',
                    colModel: [
                        { name: 'pathId', index: 'pathId', width: "20", hidden: true},
                        {
                            name: 'pathName',
                            index: 'pathName',
                            width: "330",
                            editrules: {
                                required: true,
                                custom: true,
                                custom_func: pathNameValidation
                            },
                            editable: true,
                            formoptions:{label: "Path name (*)"}
                        },
                        {
                            name: 'path',
                            index: 'path',
                            hidden: true,
                            editrules: {
                                required: true,
                                edithidden: true,
                                custom: true,
                                custom_func: pathValidation
                            },
                            editable: true,
                            formoptions:{label: "Path (*)"}
                        },
                        { name: 'requestType',
                          index: 'requestType',
                          align: 'center',
                          width: 80,
                          editable: true,
                          edittype: 'select',
                          editoptions: {defaultValue: 0, value: getRequestTypes()},
                                        editrules: {edithidden: true},
                          formatter: requestTypeFormatter
                         }, {
                          name: 'responseEnabled',
                          index: 'responseEnabled',
                          width: "60",
                          editable: false,
                          edittype: 'checkbox',
                          align: 'center',
                          editoptions: { value:"True:False" },
                          formatter: responseEnabledFormatter,
                          formatoptions: {disabled: false}
                        }, {
                          name: 'requestEnabled',
                          index: 'requestEnabled',
                          width: "60",
                          editable: false,
                          edittype: 'checkbox',
                          align: 'center',
                          editoptions: { value:"True:False" },
                          formatter: requestEnabledFormatter,
                          formatoptions: {disabled: false} }
                    ],
                    colNames: ['ID', 'Path Name', 'Path', 'Type', 'Response', 'Request'],
                    datatype: "json",
                    editUrl: '<c:url value="/api/path?profileIdentifier=${profile_id}"/>',
                    jsonReader : {
                        page : "page",
                        total : "total",
                        records : "records",
                        root : 'paths',
                        repeatitems : false
                    },
                    height: "100%",
                    ignoreCase: true,
                    loadonce: true,
                    onSelectRow: function (id) {
                        var data = jQuery("#packages").jqGrid('getRowData',id);
                        currentPathId = data.pathId;

                        updateDetailPills();
                    },
                    loadComplete: function() {
                        updateDetailPills();
                    },
                    pager: '#packagePager',
                    pgbuttons: false,
                    pgtext: null,
                    rowList : [],
                    rowNum: 10000,
                    sortname : 'id',
                    sortorder : "desc",
                    url : '<c:url value="/api/path?profileIdentifier=${profile_id}&clientUUID=${clientUUID}"/>',
                    viewrecords: true,
                });
                grid.jqGrid('navGrid', '#packagePager',
                    {
                        add: true,
                        edit: false,
                        del: true,
                        search: false,
                        addtext: "Add path",
                        deltext: "Delete path"
                    },
                    {},
                    {
                        // Add path
                        url: '<c:url value="/api/path"/>?profileIdentifier=${profile_id}',
                        reloadAfterSubmit: true,
                        width: 460,
                        closeAfterAdd: true,
                        closeAfterEdit:true,
                        topinfo:"Fields marked with a (*) are required.",
                        errorTextFormat: function (data) {
                            console.log(data);
                            return data.responseText;
                        },
                        afterComplete: function(data) {
                            reloadGrid("#packages");
                            $("#statusNotificationText").html("Path added.  Don't forget to add a hostname <b>above</b> and<br>adjust path priorities by <b>dragging and dropping</b> rows<br>in the path table!");
                            $("#statusNotificationDiv").fadeIn();
                        },
                        beforeShowForm: function(data) {
                            $("#statusNotificationDiv").fadeOut();

                            /* CREATE PLACEHOLDERS FOR ADD FORM. */
                            /* INITIALLY, GRAY */
                            $("#pathName").val(getExampleText("pathName"));
                            $("#pathName").css("color", "gray");

                            $("#path").val(getExampleText("path"));
                            $("#path").css("color", "gray");
                        },
                        afterShowForm: function(formid) {
                            /* SHIFT INITIAL FOCUS TO CANCEL TO MINIMIZE ACCIDENTAL CREATION OF
                             INCORRECT HOSTNAME.
                             */
                            $("#cData").focus();

                            var name = true;
                            $("#pathName").focus(function(){
                                if( name ) {
                                    $("#pathName").val("");
                                    $("#pathName").css("color", "black");
                                    name = false;
                                }
                            });

                            var path = true;
                            $("#path").focus(function(){
                                if( path ) {
                                    $("#path").val("");
                                    $("#path").css("color", "black");
                                    path = false;
                                }
                            });
                        },
                    },
                    {
                        // Delete path
                        mtype: 'DELETE',
                        reloadAfterSubmit: true,
                        onclickSubmit: function(rp_ge, postdata) {
                         rp_ge.url = '<c:url value="/api/path/" />' + currentPathId + "?clientUUID=" + clientUUID;
                        }},
                    {});
                grid.jqGrid('gridResize');
                grid.jqGrid('filterToolbar', { defaultSearch: 'cn', stringResult: true });

                var options = {
                    update: function(event, ui) {
                        var pathOrder = "";
                        var paths = grid.jqGrid('getRowData');
                        for( var i = 0; i < paths.length; i++ ) {
                            if( i === paths.length - 1 ) {
                                pathOrder += paths[i]["pathId"];
                            } else {
                                pathOrder += paths[i]["pathId"] + ",";
                            }
                        }
                        $.ajax({
                            type: "POST",
                            url: '<c:url value="/pathorder/"/>${profile_id}',
                            data: ({pathOrder : pathOrder}),
                            success: function() {
                                $('#info').html('Path Order Updated');
                                $('#info').fadeOut(1).delay(50).fadeIn(150);
                            }
                        });
                    },
                    placeholder: "ui-state-highlight"
                };

                var sortableAllowed = false;
                grid.jqGrid('navButtonAdd', '#packagePager', {
                    caption: "Reorder",
                    buttonicon: "ui-icon-carat-2-n-s",
                    title: "Toggle Reorder Path Priority",
                    id: "reorder_packages",
                    onClickButton: function() {
                        sortableAllowed = !sortableAllowed;

                        if( sortableAllowed ) {
                            /* ALLOWS THE PATH PRIORITY TO BE SET INSIDE OF THE PATH TABLE, INSTEAD OF ON A SEPARATE PAGE */
                            grid.jqGrid('sortableRows', options);
                            $("#reorder_packages").addClass("ui-state-highlight");
                        } else {
                            $("#packages tbody").sortable('destroy');
                            $("#reorder_packages").removeClass("ui-state-highlight");
                        }
                    }
                });

                grid.jqGrid('navButtonAdd', '#packagePager', {
                    caption: "Change font size",
                    buttonicon: "ui-icon-gear",
                    title: "Change the table font size",
                    id: "resize_packages",
                    onClickButton: function() {
                        resizeFontDialog( "#packages", "Paths" );
                    }
                });

                /* ADD PLACEHOLDER TO FILTER BAR */
                $("#gs_pathName").val("Type here to filter columns.");
                $("#gs_pathName").focus(function() {
                    $("#gs_pathName").val("");
                });

                $("#tabs").tabs();
                $("#tabs").css("overflow", "auto");
                $("#tabs").css("min-height", "500px");
                $("#tabs").css("resize", "both");
                $("#sel1").select2();

                var currentHTML = $("#gview_serverlist > .ui-jqgrid-titlebar > span").html();
                var dropDown = "&nbsp;&nbsp;&nbsp;<input id='serverGroupSelection' style='width:300px%'></input>&nbsp;&nbsp;<button id='editServerGroups' type='button' class='btn btn-xs' onClick='toggleServerGroupEdit()'>Server Grps ></button>";
                $("#gview_serverlist > .ui-jqgrid-titlebar > span").html(currentHTML + dropDown);

                $("#serverGroupSelection").select2({
                    initSelection: function(element, callback){
                            $.ajax('<c:url value="/api/profile/${profile_id}/clients/${clientUUID}"/>').done(function(data) {
                                $.ajax('<c:url value="/api/servergroup"/>' + '/' + data.client.activeServerGroup +'?profileId=${profile_id}'
                                ).done(function(data2) {
                                    callback({id: data.client.activeServerGroup, text: data2.name});
                                });
                            });
                    },
                    ajax: {
                        url: '<c:url value="/api/servergroup"/>' + '?profileId=${profile_id}',
                        dataType: "json",
                        data: function (term, page) {
                            return {search: term};
                        },
                        results: function(data){
                            var myResults = [];
                            myResults.push({id: 0, text: "Default"});
                            jQuery.each(data.servergroups, function(index, value){
                                myResults.push({
                                    id: value.id,
                                    text: value.name
                                });
                            });
                            return {
                                results: myResults
                            };
                        }
                    },
                    createSearchChoice: function(term, data) {
                        if ($(data).filter(function() {
                            return this.text.localeCompare(term)===0;
                        }).length===0) {
                            return {id:term, text:term, isNew:true};
                        }
                    },
                    formatResult: function(term) {
                        if (term.isNew) {
                            return 'create "' + term.text + '"';
                        }
                        else {
                            return term.text;
                        }
                    }
                });
                $("#serverGroupSelection").on("change", function(e) {
                    if(e.added.isNew) {
                        $.ajax({
                            type: "POST",
                            url: '<c:url value="/api/servergroup" />',
                            data: ({name : e.added.id, profileId: "${profile_id}"}),
                            success: function(data){
                                setActiveServerGroup(data.id);
                                reloadGrid("#serverGroupList");
                            }
                        });
                    }
                    else {
                        setActiveServerGroup(e.added.id);
                    }
                });
                populateGroups();

            });
            jQuery("#packages").jqGrid('navGrid','#packages',{
                edit:false,
                add:true,
                del:true,
                search:false
            });
            loadPath(currentPathId);

            function getExampleText(item) {
                switch(item) {
                    case "src":
                        return "ex. groupon.com";
                    case "dest":
                        return "ex. 123.45.67.890";
                    case "pathName":
                        return "ex. My Path Name";
                    case "path":
                        return "ex. /http500, /(a|b)"
                    default:
                        return null;
                }
            }

            function resizeFontDialog( val, name ) {
                $("#resizeFontDialog").dialog({
                    title: "Change Font Size of "+name+" table",
                    modal: true,
                    resizeable: true,
                    width: 'auto',
                    height:'auto',
                    close: function() {
                        // get the font size that has been entered
                        var size = $("#fontSize").val();
                        // if it says nothing/is NaN, do nothing
                        // if it says <11, change size to 11
                        // if it says >18, change size fo 18
                        var min = 11;
                        var max = 18;
                        if( parseInt(size) >= min && parseInt(size) <= max ) {
                            $(".ui-jqgrid "+val+" tr.jqgrow td").css('font-size', parseInt(size));
                        } else if ( parseInt(size) < min ) {
                            $(".ui-jqgrid "+val+" tr.jqgrow td").css('font-size', min);
                        } else if( parseInt(size) > max ) {
                            $(".ui-jqgrid "+val+" tr.jqgrow td").css('font-size', max);
                        }

                        // reset the value
                        $("#fontSize").val("");
                    },
                    buttons: {
                        "Close": function() {
                            $("#resizeFontDialog").dialog("close");
                        }
                    }
                })
            }

            function srcValidation(val, colname) {
                if( val.trim() === getExampleText("src") ) {
                    return [false, "Please replace the example source hostname with a valid hostname."]
                } else {
                    return [true, ""];
                }
            }

            function destValidation(val, colname) {
                if( val.trim() === getExampleText("dest") ) {
                    return [false, "Please replace the example destination hostname with a valid hostname."]
                } else {
                    return [true, ""];
                }
            }

            function pathNameValidation(val, colname) {
                if( val.trim() === getExampleText("pathName") ) {
                    return [false, "Please replace the example path name with a valid name."]
                } else {
                    return [true, ""];
                }
            }

            function pathValidation(val, colname) {
                if( val.trim() === getExampleText("path") ) {
                    return [false, "Please replace the example destination hostname with a valid hostname."]
                } else {
                    return [true, ""];
                }
            }

            function setActiveServerGroup(groupId) {
                $.ajax({
                    type: "POST",
                    url: '<c:url value="/api/servergroup/" />' + groupId,
                    data: ({activate: true, profileId: "${profile_id}", clientUUID: "${clientUUID}" }),
                    success: function(data) {
                        console.log("reloading server list");
                        reloadGrid("#serverlist");

                    }
                });
            }

            function loadPath(pathId) {
                if(pathId < 0) {
                    $("#editDiv").hide();
                }
                else {
                    $.ajax({
                        type:"GET",
                        url: '<c:url value="/api/path/"/>' + pathId,
                        data: 'clientUUID=${clientUUID}',
                        success: function(data){

                            // populate Configuration values
                            $("#editDiv").show();
                            $("#pathName").attr("value", data.pathName);
                            $("#pathValue").attr("value", data.path);
                            $("#contentType").attr("value", data.contentType);
                            $("#pathGlobal").attr("checked", data.global);
                            $("#requestType").val(data.requestType);
                            $("#postBodyFilter").val(data.bodyFilter);
                            $("#pathRepeatCount").attr("value", data.repeatNumber);
                            pathRequestTypeChanged();
                            $("#title").html(data.pathName);

                            $("#responseOverrideDetails").hide();
                            $("#requestOverrideDetails").hide();
                            currentPathId = pathId;
                            highlightSelectedGroups(data.groupIds);
                            populateResponseOverrideList(data.possibleEndpoints);
                            populateRequestOverrideList(data.possibleEndpoints);
                            populateEnabledOverrides();
                            changeResponseOverrideDiv();
                            changeRequestOverrideDiv();

                            // reset informational divs
                            $('#applyPathChangeSuccessDiv').css('display', 'none');
                            $('#applyPathChangeAlertDiv').css('display', 'none');
                        }
                    });
                }
            }

            function pathRequestTypeChanged() {
                var requestType = $("#requestType").val();
                if(requestType != "1" && requestType != "4") {
                    $("#postGeneral").show();
                }
                else {
                    $("#postGeneral").hide();
                }
            }

            // http://stackoverflow.com/questions/11112127/prevent-backspace-from-navigating-back-with-jquery-like-googles-homepage
            $(document).on("keydown", function (e){
                if(e.keyCode == 46 || e.keyCode == 8) {
                    var active = $("#tabs").tabs("option", "active");
                    if (document.activeElement.id == "responseOverrideEnabled") {
                        var type = "response"
                    } else if (document.activeElement.id == "requestOverrideEnabled") {
                        var type = "request";
                    } else {
                        return;
                    }
                    var selector = "select#" + type + "OverrideEnabled" + " option:selected";
                    var selection = $(selector);
                    if (selection.length > 0) {
                        e.preventDefault();
                        overrideRemove(type);
                    }
                }
            });

            $(document).scroll(function() {
                $("#details").animate({"margin-top": $(document).scrollTop()}, 0, "linear");
            })

            function overrideRemove(type) {
                var id = currentPathId;
                var selector = "select#" + type + "OverrideEnabled" + " option:selected";
                var selection = $(selector);

                selection.each(function(i, selected){
                    var splitId = selected.value.split(",");
                    var methodId = splitId[0];
                    var ordinal = splitId[1];

                    var args = '?ordinal=' + ordinal;
                    args += '&clientUUID=' + clientUUID;

                    $.ajax({
                        type: 'POST',
                        url: '<c:url value="/api/path/"/>' + id + '/' + methodId,
                        data: ({ordinal: ordinal, clientUUID: clientUUID, _method: 'DELETE'}),
                        success: function() {
                            if(type == "response") {
                                selectedResponseOverride = 0;
                            }
                            else {
                                selectedRequestOverride = 0;
                            }

                            if(type == "response") {
                                populateEnabledResponseOverrides();
                            }
                            else {
                                populateEnabledRequestOverrides();
                            }
                        }
                    });
                });

            }

            function overrideMoveUp(type) {
                var id = currentPathId;
                var selector = "select#" + type + "OverrideEnabled" + " option:selected";
                var selection = $(selector);

                selection.each(function(i, selected){
                    console.log(selected.value);
                    $.ajax({
                        type: 'POST',
                        url: '<c:url value="/api/path/"/>' + id,
                        data: ({enabledMoveUp : selected.value, clientUUID : clientUUID}),
                        success: function(){
                            if(type == "response") {
                                populateEnabledResponseOverrides();
                            }
                            else {
                                populateEnabledRequestOverrides();
                            }
                        }
                    });
                });
            }

            function overrideMoveDown(type) {
                var id = currentPathId;
                var selector = "select#" + type + "OverrideEnabled" + " option:selected";
                var selection = $(selector);

                selection.each(function(i, selected){
                    console.log(selected.value);
                    $.ajax({
                        type:"POST",
                        url: '<c:url value="/api/path/"/>' + id,
                        data: ({enabledMoveDown : selected.value, clientUUID : clientUUID}),
                       success: function(){
                           if(type == "response") {
                               populateEnabledResponseOverrides();
                           }
                           else {
                               populateEnabledRequestOverrides();
                           }
                       }
                    });
                });
            }

            function changeResponseOverrideDiv() {
                var selections = $("select#responseOverrideEnabled option:selected");

                if (selections.length > 1) {
                    $("#responseOverrideParameters").html("");
                    $("#responseOverrideDetails").hide();
                    selectedResponseOverride = 0;
                } else if (selections.length == 1) {
                    var id = selections[0].value;
                    selectedResponseOverride = id;
                    var splitId = id.split(",");
                    var methodId = splitId[0];
                    var ordinal = splitId[1];
                    var argContent = editEndpointArgs(currentPathId, methodId, ordinal, "response");
                }
                else {
                    // nothing selected
                    selectedResponseOverride = 0;
                    $("#responseOverrideParameters").html("");
                    $("#responseOverrideDetails").hide();
                }
            }

            function changeRequestOverrideDiv() {
                var selections = $("select#requestOverrideEnabled option:selected");

                if (selections.length > 1) {
                    $("#requestOverrideParameters").html("");
                    $("#requestOverrideDetails").hide();
                    selectedRequestOverride = 0;
                } else if (selections.length == 1) {
                    var id = selections[0].value;
                    selectedRequestOverride = id;
                    var splitId = id.split(",");
                    var methodId = splitId[0];
                    var ordinal = splitId[1];
                    var argContent = editEndpointArgs(currentPathId, methodId, ordinal, "request");
                }
                else {
                    // nothing selected
                    selectedRequestOverride = 0;
                    $("#requestOverrideParameters").html("");
                    $("#requestOverrideDetails").hide();
                }
            }

            function populateGroups() {
                $.ajax({
                    type:"GET",
                    url: '<c:url value="/api/group/"/>',
                    success: function(data){
                        var content = "";
                        for(var index in data.groups) {
                            var group = data.groups[index];
                            content += '<option value="' + group.id + '">' + group.name + '</option>';
                        }
                        $("#groupSelect").html(content);
                    }
                });
            }

            // get the next available ordinal for methodId on a specific path
            function getNextOrdinal(selectId, methodId) {
                var selector = "select#" + selectId + " option";
                var selection = $(selector);
                var lastOrdinal = 0;

                selection.each(function(i, selected){
                    var splitId = selected.value.split(",");
                    var foundMethodId = splitId[0];
                    var foundOrdinal = splitId[1];

                    if (methodId == foundMethodId)
                        lastOrdinal = foundOrdinal;
                });

                return parseInt(lastOrdinal, 10) + 1;
            }


            var selectedResponseOverride = 0;
            var selectedRequestOverride = 0;

            // Called when a different override is selected from the select box
            function overrideSelectChanged(type) {
                var selector = "select#"+type+"OverrideSelect option:selected";
                var selection = $(selector);

                var overrides = $("select#" + type + "OverrideEnabled option");
                var enabledCount = overrides.length;

                selection.each(function(i, selected){
                    if (selected.value == -999)
                        return true;

                    // get the next ordinal so we can pop up the argument dialogue
                    var ordinal = getNextOrdinal(type + "OverrideEnabled", selected.value);


                    if(isNaN(ordinal)) {
                        ordinal = 1;
                    }

                    $.ajax({
                        type:"POST",
                        url: '<c:url value="/api/path/"/>' + currentPathId,
                        data: ({addOverride : selected.value, clientUUID: clientUUID}),
                        success: function() {
                            populateEnabledOverrides();
                            if(enabledCount == 0) {
                                // automatically enable the response if a first override is added
                                if($("#" + type + "_enabled_" + currentPathId).attr("checked") != "checked") {
                                    enablePath(type, currentPathId);
                                }
                            }
                            if(type == "response") {
                                selectedResponseOverride = selected.value + "," + ordinal;
                            }
                            else {
                                selectedRequestOverride = selected.value + "," + ordinal;
                            }
                        }
                    });
                });
            }

            function enablePath(type, pathId) {
                $("#" + type + "_enabled_" + pathId).click();
            }

            function populateResponseOverrideList(possibleEndpoints){
                // preprocess methods into buckets based on class name
                var classHash = {};
                jQuery.each(possibleEndpoints, function() {
                    var methodArray = [];
                    if (this.className in classHash) {
                        methodArray = classHash[this.className];
                    }

                    methodArray.push(this);
                    classHash[this.className] = methodArray;
                });

                var content = "";
                content += '<option value="-999">Select Override</option>';

                content += '<optgroup label="General">';
                content += '<option value="-1">Custom Response</option>';
                content += '<option value="-3">Set Header</option>';
                content += '<option value="-4">Remove Header</option>';
                content += '</optgroup>';

                jQuery.each(classHash, function(hashKey, hashValue) {
                    content += '<optgroup label="' + hashKey + '">';
                    jQuery.each(hashValue, function(arrayKey, arrayValue) {
                        content += '<option value="' + arrayValue.id + '">' + arrayValue.methodName + " (" + arrayValue.description + ")" + '</option>';
                    });
                    content += '</optgroup>';
                });

                $("#responseOverrideSelect").html(content);
            }

            // this returns a formatted string of arguments for display in the "Order" column
            function getFormattedArguments( arguments, length ) {
                var argString = '';

                // show XX instead of an argument since they aren't all set
                if (length > arguments.length) {
                    for (var x = 0; x < length; x++) {
                        argString += "XX";

                        if (length - 1 > x)
                            argString += ",";
                    }
                } else { // show actual arguments
                    jQuery.each(arguments, function(methodArgsX, methodArgs) {
                        // truncate methodArgs if it is > 10 char
                        var displayStr = methodArgs;
                        if (methodArgs.length > 10) {
                            displayStr = displayStr.substring(0, 7) + '..';
                        }

                        argString += displayStr;

                        if (length - 1 > methodArgsX)
                            argString += ",";
                    });
                }

                return argString;
            }

            // called to load the edit endpoint args
            function editEndpointArgs(pathId, methodId, ordinal, type) {
                $.ajax({
                    type:"GET",
                    url: '<c:url value="/api/path/"/>' + pathId + '/' + methodId,
                    data: 'ordinal=' + ordinal + '&clientUUID=${clientUUID}',
                    success: function(data){
                        var formData = "";
                        if(data.enabledEndpoint == null) {
                            return;
                        }

                        formData += '<div class="bg-info">' + data.enabledEndpoint.methodInformation.className + " " + data.enabledEndpoint.methodInformation.methodName + '</div><dl>';
                        var x = 0;
                        $.each(data.enabledEndpoint.methodInformation.methodArguments, function(i, el) {
                            if (typeof data.enabledEndpoint.methodInformation.methodArgumentNames[i] != 'undefined') {
                                formData += '<dt>' + data.enabledEndpoint.methodInformation.methodArgumentNames[i] + '</dt>';
                            }

                            // special case for custom responses
                            if (methodId == -1) {
                                formData += '<dd><textarea id="' + type + '_args_' + x + '" ROWS=10 style="width:100%;">' + '</textarea></dd><br>';
                            } else {
                                formData += '<dd>(' + el + ')<input id="' + type + '_args_' + x + '" style="width:60%;" type="text" value=""/></dd>';
                            }
                            x++;
                        });

                        formData += '<dt>Repeat Count</dt> <dd><input id="setRepeatNumber" type="text" value="' + data.enabledEndpoint.repeatNumber + '"/></dd><br>';
                        formData += '<button class="btn btn-primary" onClick="submitOverrideData(&quot;' + type + '&quot;,' + pathId + ',' + methodId + ',' + ordinal + ',' + x  + ')">Apply</button>';
                        $("#"+type+"OverrideParameters").html(formData);

                        x = 0;
                        $.each(data.enabledEndpoint.methodInformation.methodArguments, function(i, el) {
                            // populate the override parameters
                            if (data.enabledEndpoint.arguments.length > i) {
                                $("#"+type+"_args_"+x).val(data.enabledEndpoint.arguments[i]);
                            } else {
                                if (data.enabledEndpoint.methodInformation.methodDefaultArguments[i] != null) {
                                    $("#"+type+"_args_"+x).val(data.enabledEndpoint.methodInformation.methodDefaultArguments[i]);
                                }
                            }
                            x++;
                        });

                        $("#"+type+"OverrideDetails").show();
                    }
                });
            }

            function applyGeneralPathChanges() {

                var pathName = $("#pathName").attr("value");
                var path = $("#pathValue").attr("value");
                var contentType = $("#contentType").attr("value");
                var global = $("#pathGlobal").attr("checked") == "checked";
                var requestType = $("#requestType").val();
                var bodyFilter = $("#postBodyFilter").val();
                var repeat = $("#pathRepeatCount").attr("value");

                var groups = $("#groupSelect").val();
                var groupArray = new Array(groups);

                // reset informational divs
                $('#applyPathChangeSuccessDiv').css('display', 'none');
                $('#applyPathChangeAlertDiv').css('display', 'none');

                $.ajax({
                    type:"POST",
                    async: false,
                    url: '<c:url value="/api/path/"/>' + currentPathId,
                    data: ({clientUUID: "${clientUUID}", pathName: pathName, path: path, bodyFilter: bodyFilter, contentType: contentType, repeatNumber: repeat, requestType: requestType, global: global, 'groups[]': groupArray}),
                    success: function() {
                        $('#applyPathChangeSuccessDiv').css('display', '');
                    },
                    error: function(jqXHR) {
                        $('#applyPathChangeAlertDiv').css('display', '');
                        $('#applyPathChangeAlertTextDiv').html(jqXHR.responseText);
                    }
                });
            }

            function submitOverrideData(type, pathId, methodId, ordinal, numArgs) {
                submitEndPointArgs(type, pathId, methodId, ordinal, numArgs);
                submitOverrideRepeatCount(type, pathId, methodId, ordinal);
                loadPath(currentPathId);
            }

            function submitOverrideRepeatCount(type, pathId, methodId, ordinal) {
                var selector = "#setRepeatNumber";
                var value = $(selector).val();

                $.ajax({
                    type:"POST",
                    url: '<c:url value="/api/path/"/>' + pathId + '/' + methodId,
                    data: ({repeatNumber: value, ordinal: ordinal, clientUUID: clientUUID}),
                    async: false,
                    success: function(){
                        populateEnabledResponseOverrides();
                        populateEnabledRequestOverrides();
                    }
                });
            }

            function submitEndPointArgs(type, pathId, methodId, ordinal, numArgs) {
                var args = new Array();
                for (var x = 0; x < numArgs; x++) {
                    var selector = "#" + type + "_args_" + x;
                    var value = $(selector).val();
                    args[x] = value;
                }

                $.ajax({
                    type:"POST",
                    url: '<c:url value="/api/path/"/>' + pathId + '/' + methodId,
                    data: ({'arguments[]' : args, ordinal: ordinal, clientUUID: clientUUID}),
                    async: false,
                    success: function(){

                    }
                });
            }

            function populateEnabledOverrides() {
                populateEnabledResponseOverrides();
                populateEnabledRequestOverrides();
            }

            function populateEnabledResponseOverrides() {

                $.ajax({
                    type:"GET",
                    url : '<c:url value="/api/path/"/>' + currentPathId + '?profileIdentifier=${profile_id}&typeFilter[]=ResponseOverride&typeFilter[]=ResponseHeaderOverride&clientUUID=${clientUUID}',
                    success: function(data) {
                        var content = "";
                        var usedIndexes = {};

                        jQuery.each(data.enabledEndpoints, function() {
                            var enabledId = this.overrideId;
                            var enabledArgs = this.arguments;
                            var repeatNumber = this.repeatNumber;

                            // keep track of the ordinal for a specific overrideId so we can pass it around
                            if (usedIndexes.hasOwnProperty(enabledId)) {
                                usedIndexes[enabledId]++;
                            } else {
                                usedIndexes[enabledId] = 1;
                            }

                            // add a repeat number if it exists
                            var repeat = '';
                            if (repeatNumber >= 0)
                                repeat = repeatNumber + 'x ';

                            // custom response/request
                            if (enabledId < 0) {
                                content += '<option value="' + enabledId + ',' + usedIndexes[enabledId] + '">';
                                content += repeat;

                                if (enabledId == -1) {
                                    content += 'Custom Response(' + getFormattedArguments( enabledArgs, 1 ) + ')';
                                } else if (enabledId == -2) {
                                    content += "Custom Request";
                                } else if (enabledId == -3 || enabledId == -5) {
                                    content += 'Set Header(' + getFormattedArguments( enabledArgs, 2 ) + ')';
                                } else if (enabledId == -4 || enabledId == -6) {
                                    content += 'Remove Header(' + getFormattedArguments( enabledArgs, 1 ) + ')';
                                }
                                content += '</option>';
                            } else {
                                jQuery.each(data.possibleEndpoints, function(methodX, method){
                                    if (method.id == enabledId) {
                                        var methodName = method.methodName;

                                        // Add arguments to method name if they exist
                                        if (method.methodArguments.length > 0) {
                                            methodName += "(";
                                            methodName += getFormattedArguments( enabledArgs, method.methodArguments.length );
                                            methodName += ")";
                                        }
                                        content += '<option value="' + enabledId + ',' + usedIndexes[enabledId] + '">' + repeat + methodName + '</option>';
                                        return(false);
                                    }
                                });
                            }
                        });

                        $("#responseOverrideEnabled").html(content);
                        $("#responseOverrideEnabled").css("resize", "both");

                        if(selectedResponseOverride != 0) {
                            $("#responseOverrideEnabled").val(selectedResponseOverride);
                        }
                        changeResponseOverrideDiv();
                    }
                });
            }

            function populateEnabledRequestOverrides(enabledEndpoints, possibleEndpoints) {
                $.ajax({
                    type:"GET",
                    url : '<c:url value="/api/path/"/>' + currentPathId + '?profileIdentifier=${profile_id}&typeFilter[]=RequestOverride&typeFilter[]=RequestHeaderOverride&clientUUID=${clientUUID}',
                    success: function(data) {
                        var content = "";
                        var usedIndexes = {};

                        jQuery.each(data.enabledEndpoints, function() {
                            var enabledId = this.overrideId;
                            var enabledArgs = this.arguments;
                            var repeatNumber = this.repeatNumber;

                            // keep track of the ordinal for a specific overrideId so we can pass it around
                            if (usedIndexes.hasOwnProperty(enabledId)) {
                                usedIndexes[enabledId]++;
                            } else {
                                usedIndexes[enabledId] = 1;
                            }

                            // add a repeat number if it exists
                            var repeat = '';
                            if (repeatNumber >= 0) {
                                repeat = repeatNumber + 'x ';
                            }

                            // custom response/request
                            if (enabledId < 0) {
                                content += '<option value="' + enabledId + ',' + usedIndexes[enabledId] + '">';
                                content += repeat;

                                if (enabledId == -1) {
                                    content += 'Custom Response(' + getFormattedArguments( enabledArgs, 1 ) + ')';
                                } else if (enabledId == -2) {
                                    content += "Custom Request";
                                } else if (enabledId == -3 || enabledId == -5) {
                                    content += 'Set Header(' + getFormattedArguments( enabledArgs, 2 ) + ')';
                                } else if (enabledId == -4 || enabledId == -6) {
                                    content += 'Remove Header(' + getFormattedArguments( enabledArgs, 1 ) + ')';
                                }
                                content += '</option>';
                            } else {
                                jQuery.each(data.possibleEndpoints, function(methodX, method){
                                    if (method.id == enabledId) {
                                        var methodName = method.methodName;

                                        // Add arguments to method name if they exist
                                        if (method.methodArguments.length > 0) {
                                            methodName += "(";
                                            methodName += getFormattedArguments( enabledArgs, method.methodArguments.length );
                                            methodName += ")";
                                        }
                                        content += '<option value="' + enabledId + ',' + usedIndexes[enabledId] + '">' + repeat + methodName + '</option>';
                                    }
                                });
                            }
                        });

                        $("#requestOverrideEnabled").html(content);
                        $("#requestOverrideEnabled").css("resize", "both");

                        if(selectedRequestOverride != 0) {
                            $("#requestOverrideEnabled").val(selectedRequestOverride);
                        }
                        changeRequestOverrideDiv();
                    }
                });
            }

            function populateRequestOverrideList(possibleEndpoints) {
                var content = "";
                content += '<option value="-999" selected>Select Override</option>';

                content += '<optgroup label="General">';
				content += '<option value="-2">Custom Request</option>';
				content += '<option value="-5">Set Header</option>';
				content += '<option value="-6">Remove Header</option>';
                content += '</optgroup>';

                $("#requestOverrideSelect").html(content);
            }

            function toggleServerGroupEdit(){
                if($('#serverEdit').is(':visible') ) {
                    $("#serverEdit").hide();
                    $("#editServerGroups").attr("class", "btn-xs btn-default");
                }
                else {
                    reloadGrid("#serverGroupList");
                    $("#serverEdit").show();
                    $("#editServerGroups").attr("class", "btn-xs btn-primary");
                }
            }

            function highlightSelectedGroups(groupIds) {
                var ids = groupIds.split(",");
                for(var index in ids) {
                    $("#groupSelect").val(ids);
                }
            }

            function dismissStatusNotificationDiv() {
                $("#statusNotificationDiv").fadeOut();
            }
        </script>
    </head>
    <body>
    	<!-- Hidden div for changing client friendly name --
        <div id="changeClientFriendlyNameDialog" style="display:none;">
            Client Friendly Name: <input id="changeClientFriendlyName" value="${clientFriendlyName}"/>
            <div id="friendlyNameError" style="color: red"></div>
        </div>

        <!-- Hidden div for switching clients --
        <div id="switchClientDialog" style="display:none;">
            Client UUID/Name: <input id="switchClientName" value="${clientFriendlyName}"/>
        </div>-->

        <!-- Hidden div for changing font size -->
        <div id="resizeFontDialog" style="display:none;">
            Please enter a number between 11 and 18.<br>
            Font size: <input id="fontSize"/>
        </div>

        <%@ include file="clients_part.jsp" %>
        <%@ include file="pathtester_part.jsp" %>

        <nav class="navbar navbar-default" role="navigation">
            <div class="container-fluid">
                <div id="statusBar" class="collapse navbar-collapse">
                    <ul id="status2"  class="nav navbar-nav navbar-left">
                        <li class="navbar-brand">Odo</li>
                        <li><a href="#" onClick="navigateProfiles()">Profiles</a> </li>
                        <li><a href="#" onClick="navigateRequestHistory()">Request History</a></li>
                        <!--<li id="navbarPathPriority"><a href="#" onClick="navigatePathPriority()">Path Priority</a></li>-->
                        <li><a href="#" onClick="navigatePathTester()">Path Tester</a></li>
                        <li><a href="#" onClick="navigateEditGroups()">Edit Groups</a></li>
                    </ul>
                    <div id="status" class="form-group navbar-form navbar-left" ></div>
                    <div class="form-group navbar-form navbar-left">
                        <button id="resetProfileButton" class="btn btn-danger" onclick="resetProfile()"
                                data-toggle="tooltip" data-placement="bottom" title="Click here to reset all path settings in this profile.">Reset Profile</button>
                    </div>
                    <!-- TO FIND HELP -->
                    <div class="form-group navbar-form navbar-left">
                        <button id="helpButton" class="btn btn-info" onclick="navigateHelp()"
                                target="_blank" data-toggle="tooltip" data-placement="bottom" title="Click here to read the readme.">Need Help?</button>
                    </div>

                    <ul id="clientInfo" class="nav navbar-nav navbar-right">
                    </ul>
                </div>
            </div>
        </nav>

        <div id="container">
        <div id="listContainer">
            <div>
                <table id="serverlist"></table>
                <div id="servernavGrid"></div>
            </div>



            <div>
                <table id="packages">
                    <tr><td></td></tr>
                </table>
                <div id="packagePager" >
                </div>
            </div>
        <!-- div for top bar notice -->
        <div class="ui-widget" id="statusNotificationDiv" style="display: none;" onClick="dismissStatusNotificationDiv()">
            <div class="ui-state-highlight ui-corner-all" style="margin-top: 10px;  margin-bottom: 10px; padding: 0 .7em;">
                <p style="margin-top: 10px; margin-bottom:10px;"><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
                    <span id="statusNotificationText"/></p>
            </div>
        </div>
        </div>

        <div id="details" >
            <div class="serverGroupEdit" id="serverEdit">
                <div>
                    <h2><span class="label label-default" >Edit Server Groups</span></h2>
                </div>
                <div>
                    <table id="serverGroupList"></table>
                    <div id="serverGroupNavGrid"></div>
                </div>
            </div>

            <div class="detailsView" id="editDiv">
                <div>
                    <ul class="nav nav-pills" id="nav">
                    </ul>
                </div>
                <div id="tabs">
                    <ul>
                        <li><a href="#tabs-1">Response</a></li>
                        <li><a href="#tabs-2">Request</a></li>
                        <li><a href="#tabs-3">Configuration</a></li>
                    </ul>

                    <div id="tabs-1" >
                        <div class="ui-widget-content ui-corner-all detailsLeft">
                            <dl>
                                <dt>
                                    Overrides
                                </dt>
                                <dd>
                                    <select id="responseOverrideEnabled" class="ui-corner-all" multiple="multiple"
                                            style="min-height: 160px; width: 100%" onChange="changeResponseOverrideDiv()">
                                    </select>
                                    <br>
                                    <div style="display: inline-block" class="ui-state-default">
                                        <span class="ui-icon ui-icon-circle-triangle-n" title="Up" style="float: left;" onClick="overrideMoveUp('response')"></span>
                                        <span class="ui-icon ui-icon-circle-triangle-s" title="Down" style="float: left;" onClick="overrideMoveDown('response')"></span>
                                        <span class="ui-icon ui-icon-trash" title="Delete" style="float: right;" onClick="overrideRemove('response')"></span>
                                    </div>
                                    <br>
                                </dd>

                                <dt>
                                    Add Override
                                </dt>
                                <dd>
                                    <select id="responseOverrideSelect" onfocus="this.selectedIndex = -999;" style="width:100%" onChange="overrideSelectChanged('response')">
                                        <option value="-999">Select Override</option>
                                    </select>
                                </dd>
                            </dl>
                        </div>


                        <div id="responseOverrideDetails" style="display: none;" class="ui-corner-all detailsRight" >
                            <div class="ui-widget">
                                <div class="ui-widget-header ui-corner-all">
                                    Override Parameters
                                </div>
                                <div id="responseOverrideParameters" class="ui-widget-content ui-corner-all overrideParameters">
                                </div>
                            </div>
                        </div>
                        <div style="clear: both" ></div>
                    </div>


                    <div id="tabs-2" >
                        <div  class="ui-widget-content ui-corner-all detailsLeft">
                            <dl>
                                <dt>
                                    Overrides
                                </dt>
                                <dd>
                                    <select id="requestOverrideEnabled" class="ui-corner-all" multiple="multiple"
                                            style="min-height: 160px; width: 100%" onChange="changeRequestOverrideDiv()">
                                    </select>
                                    <br>
                                    <div style="display: inline-block" class="ui-state-default">
                                        <span class="ui-icon ui-icon-circle-triangle-n" title="Up" style="float: left;" onClick="overrideMoveUp('request')"></span>
                                        <span class="ui-icon ui-icon-circle-triangle-s" title="Down" style="float: left;" onClick="overrideMoveDown('request')"></span>
                                        <span class="ui-icon ui-icon-trash" title="Delete" style="float: right;" onClick="overrideRemove('request')"></span>
                                    </div>
                                    <br>
                                </dd>

                                <dt>
                                    Add Override
                                </dt>
                                <dd>
                                    <select id="requestOverrideSelect" onfocus="this.selectedIndex = -999;" style="width:100%" onChange="overrideSelectChanged('request')">
                                        <option value="-999">Select Override</option>
                                    </select>
                                </dd>
                            </dl>
                        </div>

                        <div id="requestOverrideDetails"  style="display:none" class="ui-corner-all detailsRight">
                            <div class="ui-widget">
                                <div class="ui-widget-header ui-corner-all" >
                                    Override Parameters
                                </div>
                                <div id="requestOverrideParameters" class="ui-widget-content ui-corner-all overrideParameters">
                                    None
                                </div>
                            </div>
                        </div>
                    </div>

                    <div id="tabs-3">
                        <dl class="dl-horizontal">
                            <dt>
                                Global
                            </dt>
                            <dd>
                                <input id="pathGlobal" type="checkbox" />
                            </dd>
                            <dt>
                                Path Name
                            </dt>
                            <dd>
                                <input id="pathName" style="width: 70%" />
                            </dd>

                            <dt>
                                Path Value
                            </dt>
                            <dd>
                                <input id="pathValue" style="width: 100%" />
                            </dd>

                            <dt>
                                Content Type
                            </dt>
                            <dd>
                                <input id="contentType" style="width: 100%" />
                            </dd>

                            <dt>
                                Repeat Count
                            </dt>
                            <dd>
                                <input id="pathRepeatCount" style="width: 20%" />
                            </dd>

                            <dt>
                                Request Type
                            </dt>
                            <dd>
                                <select id="requestType" class="form-control" style="width:auto;" onChange="pathRequestTypeChanged()">
                                    <option value="0">ALL</option>
                                    <option value="1">GET</option>
                                    <option value="2">PUT</option>
                                    <option value="3">POST</option>
                                    <option value="4">DELETE</option>
                                </select>
                            </dd>

                        </dl>
                        <dl id="postGeneral" class="dl-horizontal" style="display:none;">
                            <dt>
                                Request Body<br>Filter<br> (optional)
                            </dt>
                            <dd>
                                <textarea id="postBodyFilter" ROWS="6" style="width:80%;" ></textarea>
                            </dd>
                        </dl>
                        <dl class="dl-horizontal" style="display:inline;">
                            <dt>
                                Groups
                            </dt>
                            <dd>
                                <select id="groupSelect" class="form-control" multiple="multiple" style="min-width:150px;width:auto;">
                                </select>
                            </dd>
                            <dd>
                                <table>
                                    <tr>
                                    <td><button class="btn btn-primary" onclick="applyGeneralPathChanges();">Apply</button></td>
                                    <td>
                                        <div class="ui-widget" style="display:none;" id="applyPathChangeAlertDiv">
                                            <div class="ui-state-error ui-corner-all" style="padding: 0 .7em;">
                                                <p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
                                                <strong>Alert: </strong><span id="applyPathChangeAlertTextDiv"/></p>
                                            </div>
                                        </div>
                                        <div class="ui-widget" style="display:none;" id="applyPathChangeSuccessDiv">
                                        	<div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;">
                                        		<p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
                                        		<strong>Success: </strong>Configuration saved.</p>
                                        	</div>
                                        </div>
                                    </td>
                                    </tr>
                                </table>
                            </dd>
                        </dl>
                    </div>

                </div>
            </div>
        </div>
        </div>
    </body>
</html>