<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false"%>
<%@ page session="false"%>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Profile: ${profile_name}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <%@ include file="/resources/js/webjars.include" %>

    <style type="text/css">
        #details {
            position: sticky;
            top: 12px;
        }

        #serverEdit {
            display: none;
        }

        #listContainer > div {
            margin-bottom: 1em;
        }

        #pg_packagePager .ui-pg-table,
        #pg_servernavGrid .ui-pg-table,
        #servernavGrid_left,
        #packagePager_left { /* KEEPS PAGER BUTTONS THE RIGHT SIZE */
            width: auto !important;
        }

        #nav > li > a {
            padding-top: 3px;
            padding-bottom: 3px;
            background-color: #767676;
            color: black;
        }

        #nav > li.active > a,
        #nav > li.active > a:hover,
        #nav > li.active > a:focus {
            background-color: #e9e9e9;
            font-weight: bold;
        }

        #nav > li > a:hover,
        #nav > li > a:focus {
            background-color: #696969;
        }

        #requestOverrideDetails form .form-group label,
        #responseOverrideDetails form .form-group label,
        #requestOverrideDetails form button,
        #responseOverrideDetails form button {
            margin-right: .25em;
        }
    </style>

    <script type="text/javascript">
        'use strict';
        $.jgrid.no_legacy_api = true;
        $.jgrid.useJSON = true;

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

        function updateStatus() {
            var active = ${isActive};

            $("#status")
                .empty()
                .append($("<button>")
                    .addClass("btn")
                    .addClass(active ? "btn-default" : "btn-danger")
                    .attr("id", "make_active")
                    .text((active ? "Deactivate" : "Activate") + " Profile")
                    .click(changeActive.bind(!!active)));

            // set client ID information
            $("#clientInfo")
                .empty()
                .append($("<li>").attr("id", "clientButton")
                    .append($("<a>")
                        .attr({
                            href: "#",
                            "data-toggle": "tooltip",
                            "data-placement": "bottom",
                            title: "Click here to manage clients.",
                        })
                        .text("Client UUID: " + (clientUUID == "-1" ? "Default" : clientUUID))
                        .click(manageClientPopup)))
        }

        function changeActive(value) {
            $.ajax({
                type: "POST",
                url: '<c:url value="/api/profile/${profile_id}/clients/${clientUUID}"/>',
                data: "active=" + value,
                success: window.location.reload,
                error: function() {
                    alert("Unable to " + (value ? "de" : "") + "activate the profile.");
                }
            });
        }

        function importConfiguration() {
            $("#configurationUploadDialog").dialog({
                title: "Upload Active Override & Server Configuration",
                modal: true,
                width: 400,
                buttons: {
                    Submit: function() {
                        $("#configurationUploadForm").submit();
                    },
                    Cancel: function() {
                        $("#configurationUploadDialog").dialog("close");
                    }
                }
            });
        }

        function exportConfigurationFile() {
            download('<c:url value="/api/backup/profile/${profile_id}/${clientUUID}"/>');
        }

        function importConfigurationRequest(file) {
            download('<c:url value="/api/backup/profile/${profile_id}/${clientUUID}?oldExport=true"/>');
            var formData = new FormData();
            formData.append('odoImport', $('[name="IncludeOdoConfiguration"]').is(":checked"));
            formData.append('fileData', file, file.name);
            $.ajax({
                type: "POST",
                url: '<c:url value="/api/backup/profile/${profile_id}/${clientUUID}"/>',
                data: formData,
                processData: false,
                contentType: false,
                success: function() {
                    window.location.reload();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    var errorResponse = JSON.parse(jqXHR.responseText);
                    var alertText = errorResponse.join("\n");
                    window.alert(alertText);
                    $("#configurationUploadDialog").dialog("close");
                }
            });
        }

        function exportProfileConfiguration() {
            download('<c:url value="/api/backup/profile/${profile_id}/${clientUUID}?odoExport=false"/>');
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

        function requestTypeFormatter(cellvalue, options, rowObject) {
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
            $(".nav-pills > li").each( function( index, element ) {
                var id = $(this).attr("id");
                existingPills.push(id)
            });

            // look through the overrides list to see which are enabled/active
            var ids = $("#packages").jqGrid('getDataIDs');
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
                        $("#nav").append($("<li>")
                            .attr("id", rowdata.pathId)
                            .append($("<a>")
                                .attr({
                                    "href": "#tab" + rowdata.pathId,
                                    "data-toggle": "tab"})
                                .text(rowdata.pathName).
                                click(function() {
                                    loadPath($(this).parent().attr("id"));
                                })))
                    }
                }
            }

            // remove tabs that should no longer exist
            // at this point the existingPills list is just the pills that need to be removed
            for (var x = 0; x < existingPills.length; x++) {
                if (existingPills[x] !== -1) {
                    $("#nav").find("#" + existingPills[x]).remove();

                    // reset pathToLoad if it is equal to the removed path
                    if (pathToLoad === existingPills[x]) {
                        pathToLoad = -1;
                    }
                }
            }

            // make all pills non-active
            $(".nav-pills > li").removeClass("active")

            // make the currently selected pill active
            $("#nav").find("#" + pathToLoad).addClass("active");

            // load the currently selected path data
            loadPath(pathToLoad);
        }

        // common function for grid reload
        function reloadGrid(gridId) {
            $(gridId).setGridParam({datatype:'json', page:1}).trigger("reloadGrid");
        }

        function responseEnabledFormatter(cellvalue, options, rowObject) {
            var elementId = "response_enabled_" + rowObject.pathId;
            return $("<div>").append(
                $("<label>")
                    .attr({
                        for: elementId,
                        style: "display: inline-block; height: 100%; width: 100%; margin: 0; padding: 0;"
                    })
                    .append($("<input>")
                        .attr({
                            type: "checkbox",
                            id: elementId,
                            onchange: "responseEnabledChanged(" + elementId + ")",
                            checked: cellvalue,
                            offval: "0",
                            "data-path": rowObject.pathId,
                            "data-row": options.rowId
                        })
                        .addClass("mousetrap")
                        .val(cellvalue ? "1" : "0")))
                .html();
        }

        function responseEnabledChanged(element) {
            var pathId = $(element).data("path");
            var enabled = element.checked ? 1 : 0;

            $.ajax({
                type: "POST",
                url: '<c:url value="/api/path/"/>' + pathId,
                data: 'responseEnabled=' + enabled + '&clientUUID=' + clientUUID,
                rowId: $(element).data("row"),
                isEnabled: element.checked,
                error: function() {
                    alert("Could not properly set value");
                },
                success: function() {
                    updateDetailPills();
                    var rowData = $("#packages").getLocalRow(this.rowId);
                    rowData.responseEnabled = this.isEnabled;
                    $("#packages").setRowData(this.rowId, rowData);
                }
            });
        }

        function requestEnabledFormatter(cellvalue, options, rowObject) {
            var elementId = "request_enabled_" + rowObject.pathId;
            return $("<div>").append(
                $("<label>")
                    .attr({
                        for: elementId,
                        style: "display: inline-block; height: 100%; width: 100%; margin: 0; padding: 0;"
                    })
                    .append($("<input>")
                        .attr({
                            type: "checkbox",
                            id: elementId,
                            onchange: "requestEnabledChanged(" + elementId + ")",
                            checked: cellvalue,
                            offval: "0",
                            "data-path": rowObject.pathId,
                            "data-row": options.rowId
                        })
                        .addClass("mousetrap")
                        .val(cellvalue ? "1" : "0")))
                .html();
        }

        function requestEnabledChanged(element) {
            var pathId = $(element).data("path");
            var enabled = element.checked ? 1 : 0;

            $.ajax({
                type: "POST",
                url: '<c:url value="/api/path/"/>' + pathId,
                data: 'requestEnabled=' + enabled + '&clientUUID=' + clientUUID,
                rowId: $(element).data("row"),
                isEnabled: element.checked,
                error: function() {
                    alert("Could not properly set value");
                },
                success: function() {
                    updateDetailPills();
                    var rowData = $("#packages").getLocalRow(this.rowId);
                    rowData.requestEnabled = this.isEnabled;
                    $("#packages").setRowData(this.rowId, rowData);
                }
            });
        }

        var currentServerId = -1;
        function serverIdFormatter(cellvalue, options, rowObject) {
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
                    $("#serverEnabled_" + id).prop("checked", origEnabled);

                    alert("Error updating host entry.  Please make sure the hostsedit RMI server is running");
                }
            });
        }

        // formats the enable/disable check box
        function serverEnabledFormatter(cellvalue, options, rowObject) {
            var checkedValue = cellvalue ? 1 : 0;
            return $("<div>").append($("<input>")
                .attr({
                    id: "serverEnabled_" + currentServerId,
                    onchange: "serverEnabledChanged(" + currentServerId + ")",
                    type: "checkbox",
                    offval: "0",
                    checked: checkedValue == 1 ? "checked" : ""
                })
                .val(checkedValue))
                .html();
        }

        // format button to download a server certificate
        function certDownloadButtonFormatter(cellvalue, options, rowObject) {
            return $("<div>").append($("<button>")
                .addClass("btn btn-xs")
                .attr("onclick", "downloadCert(\"" + rowObject.srcUrl + "\")")
                .append($("<span>").addClass("glyphicon glyphicon-download")))
            .html();
        }

        function downloadCert(serverHost) {
            window.location = '<c:url value="/cert/"/>' + serverHost;
        }

        function destinationHostFormatter(cellvalue, options, rowObject) {
            if (cellvalue === "") {
                return "<span class=\"glyphicon glyphicon-info-sign\"></span> Forwarding to source";
            }
            return cellvalue;
        }

        function destinationHostUnFormatter(cellvalue, options, rowObject) {
            // "hidden" is hidden text in the input box
            if (cellvalue.indexOf(" Forwarding to source") === 0) {
                return "";
            }
            return cellvalue;
        }

        $(document).ready(function() {
            // Adapted from: http://blog.teamtreehouse.com/uploading-files-ajax
            $('#configurationUploadForm').submit(function(event) {
                event.preventDefault();

                var file = $("#configurationUploadFile").get(0).files[0];
                importConfigurationRequest(file);
            });

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

            updateStatus();
            $("#responseOverrideSelect").select2();
            $("#requestOverrideSelect").select2();

            // Tab nav
            Mousetrap.bind('1', function() {
                $("[href=\"#tabs-1\"]").click();
                $("#responseOverrideEnabled").focus();
            });
            Mousetrap.bind('2', function() {
                $("[href=\"#tabs-2\"]").click();
                $("#requestOverrideEnabled").focus();
            });
            Mousetrap.bind('3', function() {
                $("[href=\"#tabs-3\"]").click();
            });
            // Filter
            Mousetrap.bind('f', function(event) {
                event.preventDefault();
                $("#gs_pathName").focus();
            });
            // Active paths navigation
            Mousetrap.bind('alt+right', function(event) {
                $("#nav .active").next().find("a").click();
            });
            Mousetrap.bind('alt+left', function(event) {
                $("#nav .active").prev().find("a").click();
            });
            // Overrides navigation
            Mousetrap.bind('+', function(event) {
                event.preventDefault();
                $("#tabs-1:visible #responseOverrideSelect, #tabs-2:visible #requestOverrideSelect").first().select2("open");
            });
            Mousetrap.bind('alt+up', function(event) {
                if ($("#responseOverrideEnabled").is(":visible:focus")) {
                    event.preventDefault();
                    overrideMoveUp("response");
                } else if ($("#requestOverrideEnabled").is(":visible:focus")) {
                    event.preventDefault();
                    overrideMoveUp("request");
                }
            });
            Mousetrap.bind('alt+down', function(event) {
                if ($("#responseOverrideEnabled").is(":visible:focus")) {
                    event.preventDefault();
                    overrideMoveDown("response");
                } else if ($("#requestOverrideEnabled").is(":visible:focus")) {
                    event.preventDefault();
                    overrideMoveDown("request");
                }
            });
            Mousetrap.bind(['backspace', 'del'], function(event) {
                if ($("#responseOverrideEnabled").is(":visible:focus")) {
                    event.preventDefault();
                    overrideRemove("response");
                } else if ($("#requestOverrideEnabled").is(":visible:focus")) {
                    event.preventDefault();
                    overrideRemove("request");
                }
            });


            $("#serverlist").jqGrid({
                autowidth : true,
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
                    editrules: {required: true},
                    formoptions:{label: "Source Hostname/IP (*)"}
                }, {
                    name : 'destUrl',
                    index : 'destUrl',
                    width : 160,
                    editable : true,
                    formatter : destinationHostFormatter,
                    unformat : destinationHostUnFormatter,
                    editoptions: {title: "default: forwards to source"},
                    editrules: {required: false},
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
                        $("#serverlist").setGridParam({
                            cellurl : '<c:url value="/api/edit/server/"/>' + rowid
                                    + '/src'
                        });
                    } else if (cellname == "destUrl") {
                        $("#serverlist").setGridParam({
                            cellurl : '<c:url value="/api/edit/server/"/>' + rowid
                                    + '/dest'
                        });
                    } else if (cellname == "hostHeader") {
                        $("#serverlist").setGridParam({
                            cellurl : '<c:url value="/api/edit/server/"/>' + rowid
                                    + '/host'
                        });
                    }
                },
                afterSaveCell : function() {
                    $("#serverlist").trigger("reloadGrid");
                },
                loadComplete: function(data) {
                    // hide host enable/disable if host editor is not available
                    if (data.hostEditor == false) {
                        $("#serverlist").hideCol("hostsEntry.enabled");
                    }
                },
                datatype : "json",
                height: "auto",
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
            $("#serverlist").jqGrid('navGrid', '#servernavGrid', {
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
                    $("#srcUrl").attr("placeholder", getPlaceholderText("src"));
                },
                afterShowForm: function(formid) {
                    /* SHIFT INITIAL FOCUS TO CANCEL TO MINIMIZE ACCIDENTAL CREATION OF
                        INCORRECT HOSTNAME.
                     */
                    $("#cData").focus();
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
            $("#serverlist").jqGrid('gridResize', { handles: "n, s" });


            $("#serverGroupList").jqGrid({
                autowidth : true,
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
                        $("#serverGroupList").setGridParam({
                            cellurl : '<c:url value="/api/servergroup/"/>' + rowid + "?profileId=${profile_id}"
                        });
                    }
                },
                datatype : "json",
                loadonce: true,
                height: "auto",
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

            $("#serverGroupList").jqGrid('navGrid', '#serverGroupNavGrid', {
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

            $("#packages").jqGrid({
                autowidth: true,
                caption: "Paths",
                cellurl : '<c:url value="/api/path?profileIdentifier=${profile_id}&clientUUID=${clientUUID}"/>',
                colNames: ['ID', 'Path Name', 'Path', 'Type', 'Response', 'Request'],
                colModel: [
                    {
                        name: 'pathId',
                        index: 'pathId',
                        hidden: true
                    },
                    {
                        name: 'pathName',
                        index: 'pathName',
                        width: 330,
                        editable: true,
                        editrules: {
                            required: true
                        },
                        formoptions: {label: "Path name (*)"},
                        search: true,
                        searchoptions: {
                            clearSearch: false
                        }
                    },
                    {
                        name: 'path',
                        index: 'path',
                        hidden: true,
                        editable: true,
                        editrules: {
                            required: true,
                            edithidden: true
                        },
                        formoptions: {label: "Path (*)"}
                    },
                    {
                        name: 'requestType',
                        index: 'requestType',
                        align: 'center',
                        width: 80,
                        editable: true,
                        edittype: 'select',
                        editoptions: {defaultValue: 0, value: "0:ALL;1:GET;2:PUT;3:POST;4:DELETE"},
                        editrules: {edithidden: true},
                        formatter: requestTypeFormatter,
                        search: true,
                        searchoptions: {
                            clearSearch: false
                        }
                    },
                    {
                        name: 'responseEnabled',
                        index: 'responseEnabled',
                        width: "60",
                        align: 'center',
                        editable: false,
                        formatter: responseEnabledFormatter,
                        formatoptions: {disabled: false},
                        search: true,
                        searchoptions: {
                            clearSearch: false
                        }
                    }, {
                        name: 'requestEnabled',
                        index: 'requestEnabled',
                        width: "60",
                        align: 'center',
                        editable: false,
                        formatter: requestEnabledFormatter,
                        formatoptions: {disabled: false},
                        search: true,
                        searchoptions: {
                            clearSearch: false
                        }
                    }
                ],
                datatype: "json",
                editUrl: '<c:url value="/api/path?profileIdentifier=${profile_id}"/>',
                jsonReader : {
                    page : "page",
                    total : "total",
                    records : "records",
                    root : 'paths',
                    repeatitems : false
                },
                height: "auto",
                ignoreCase: true,
                loadonce: true,
                onSelectRow: function (id) {
                    var data = $("#packages").jqGrid('getRowData',id);
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
                viewrecords: true
            });
            $("#packages").jqGrid('navGrid', '#packagePager',
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
                        return data.responseText;
                    },
                    afterComplete: function(data) {
                        reloadGrid("#packages");
                        $("#statusNotificationText").html("Path added.  Don't forget to add a hostname <b>above</b> and<br>adjust path priorities by <b>clicking Reorder</b> at the bottom<br>of the path table!");
                        $("#statusNotificationDiv").fadeIn();
                    },
                    beforeShowForm: function(data) {
                        $("#statusNotificationDiv").fadeOut();

                        /* CREATE PLACEHOLDERS FOR ADD FORM. */
                        /* INITIALLY, GRAY */
                        $("#pathName").attr("placeholder", getPlaceholderText("pathName"));
                        $("#pathName").css("color", "gray");

                        $("#path").attr("placeholder", getPlaceholderText("path"));
                        $("#path").css("color", "gray");
                    },
                    afterShowForm: function(formid) {
                        /* SHIFT INITIAL FOCUS TO CANCEL TO MINIMIZE ACCIDENTAL CREATION OF
                         INCORRECT HOSTNAME.
                         */
                        $("#cData").focus();
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
            $("#packages").jqGrid('filterToolbar', {
                defaultSearch: 'cn',
                stringResult: true,
                searchOnEnter: false,
            });
            $("#packages").jqGrid('gridResize', { handles: "n, s" });


            var options = {
                update: function(event, ui) {
                    var pathOrder = "";
                    var paths = $("#packages").jqGrid('getRowData');
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
            $("#packages").jqGrid('navButtonAdd', '#packagePager', {
                caption: "Reorder",
                buttonicon: "ui-icon-carat-2-n-s",
                title: "Toggle Reorder Path Priority",
                id: "reorder_packages",
                onClickButton: function() {
                    sortableAllowed = !sortableAllowed;

                    if( sortableAllowed ) {
                        /* ALLOWS THE PATH PRIORITY TO BE SET INSIDE OF THE PATH TABLE, INSTEAD OF ON A SEPARATE PAGE */
                        $("#packages").jqGrid('sortableRows', options);
                        $("#reorder_packages").addClass("ui-state-highlight");
                        /* GIVE HELPER TEXT*/
                        $("#reorderNotificationText").html("Drag and drop rows to change the path priority.<br>The ordering of paths impacts how requests are handled. <br>In general if a higher priority path matches a request then<br>further paths will not be evaluated. <br>The only exception is Global paths. In the case that a global path<br>is matched the matcher will continue to search for a non-global<br>matching path.");
                        $("#reorderNotificationDiv").fadeIn();
                    } else {
                        $("#packages tbody").sortable('destroy');
                        $("#reorder_packages").removeClass("ui-state-highlight");
                        /* REMOVE HELPER TEXT */
                        $("#reorderNotificationDiv").fadeOut();
                    }
                }
            });

            $("#groupTable").jqGrid({
                url : '<c:url value="/api/group"/>',
                width: 300,
                height: 190,
                pgbuttons : false, // disable page control like next, back button
                pgtext : null,
                multiselect: true,
                multiboxonly: true,
                datatype : "json",
                colNames : [ 'ID', 'Group Name'],
                colModel : [ {
                    name : 'id',
                    index : 'id',
                    width : 55,
                    hidden : true
                }, {
                    name: 'name',
                    path: 'name',
                    width: 200,
                    editable: true,
                    align: 'left'
                } ],
                jsonReader : {
                    page : "page",
                    total : "total",
                    records : "records",
                    root : 'groups',
                    repeatitems : false
                },
                loadonce: true,
                cellurl : '/testproxy/api/group',
                gridComplete : function() {
                    if($("#groupsTable").length > 0){
                        $("#groupsTable").setSelection(
                                $("#groupsTable").getDataIDs()[0], true);
                    }
                },
                editurl: '<c:url value="/api/group/" />',
                rowList : [],
                sortable: false,
                rowNum: 10000,
                viewrecords : true,
                sortorder : "asc"
            });

            // bind window resize to fix grid width
            $(window).bind('resize', function() {
                $("#serverlist").setGridWidth($("#listContainer").width());
                $("#packages").setGridWidth($("#listContainer").width());
            });

            $("#gs_pathName").attr("placeholder", "Type here to filter columns (f)");
            $("#gs_requestType").attr("placeholder", "0...4");
            $("#gs_responseEnabled").attr("placeholder", "t/f");
            $("#gs_requestEnabled").attr("placeholder", "t/f");

            $("#tabs").tabs();
            $("#sel1").select2();

            $("#gview_serverlist .ui-jqgrid-titlebar")
                .append(document.createTextNode("\xa0\xa0"))
                .append($("<input>")
                    .attr("id", "serverGroupSelection"))
                .append($("<button>")
                    .attr({
                        type: "button",
                        id: "editServerGroups",
                        class: "btn btn-xs btn-default",
                        style: "float: right; margin-right: 2em;"
                    })
                    .on("click", showServerGroupEdit)
                    .text("Server Groups ")
                    .append($("<span>").addClass("glyphicon glyphicon-new-window")));

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
                        $.each(data.servergroups, function(index, value){
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
                    } else {
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
                } else {
                    setActiveServerGroup(e.added.id);
                }
            });

            $("#packages").jqGrid('navGrid','#packages',{
                edit: false,
                add: true,
                del: true,
                search: false
            });
            loadPath(currentPathId);

            if ($.ui.dialog.prototype._allowInteraction) {
                var originalAllowInteraction = $.ui.dialog.prototype._allowInteraction;
                $.ui.dialog.prototype._allowInteraction = function(e) {
                    return !!$(e.target).parents(".ui-jqdialog").length || originalAllowInteraction(e);
                }
            }

            // temporary kludge to get the sizes right
            setTimeout(function() {
                $("#serverlist").setGridWidth($("#listContainer").width());
            }, 100);
        });

        function getPlaceholderText(item) {
            switch(item) {
                case "src":
                    return "ex. groupon.com";
                case "pathName":
                    return "ex. My Path Name";
                case "path":
                    return "ex. /http500, /(a|b)"
                default:
                    return null;
            }
        }

        function setActiveServerGroup(groupId) {
            $.ajax({
                type: "POST",
                url: '<c:url value="/api/servergroup/" />' + groupId,
                data: ({activate: true, profileId: "${profile_id}", clientUUID: "${clientUUID}" }),
                success: function(data) {
                    reloadGrid("#serverlist");

                }
            });
        }

        function loadPath(pathId) {
            if(pathId < 0) {
                $("#editDiv").hide();
                return;
            }

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
                    $("#pathResponseCode").attr("value", data.responseCode);
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

                    $("#responseOverrideEnabled:visible, #requestOverrideEnabled:visible").first().focus();

                    // reset informational divs
                    $('#applyPathChangeSuccessDiv').hide();
                    $('#applyPathChangeAlertDiv').hide();
                }
            });
        }

        function pathRequestTypeChanged() {
            var requestType = $("#requestType").val();
            if(requestType != "1" && requestType != "4") {
                $("#postGeneral").show();
            } else {
                $("#postGeneral").hide();
            }
        }

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
                        } else {
                            selectedRequestOverride = 0;
                        }

                        if(type == "response") {
                            populateEnabledResponseOverrides();
                        } else {
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

            selection.each(function(i, selected) {
                $.ajax({
                    type: 'POST',
                    url: '<c:url value="/api/path/"/>' + id,
                    data: ({enabledMoveUp : selected.value, clientUUID : clientUUID}),
                    success: function(){
                        if(type == "response") {
                            populateEnabledResponseOverrides();
                        } else {
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

            selection.each(function(i, selected) {
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

        function changeResponseOverrideDiv(autofocusOnForm) {
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
                populateEditOverrideArgs(currentPathId, methodId, ordinal, "response", autofocusOnForm);
            }
            else {
                // nothing selected
                selectedResponseOverride = 0;
                $("#responseOverrideParameters").html("");
                $("#responseOverrideDetails").hide();
            }
        }

        function changeRequestOverrideDiv(autofocusOnForm) {
            var selections = $("select#requestOverrideEnabled option:selected");

            if (selections.length > 1) {
                $("#requestOverrideParameters").empty();
                $("#requestOverrideDetails").hide();
                selectedRequestOverride = 0;
            } else if (selections.length == 1) {
                var id = selections[0].value;
                selectedRequestOverride = id;
                var splitId = id.split(",");
                var methodId = splitId[0];
                var ordinal = splitId[1];
                populateEditOverrideArgs(currentPathId, methodId, ordinal, "request", autofocusOnForm);
            }
            else {
                // nothing selected
                selectedRequestOverride = 0;
                $("#requestOverrideParameters").empty();
                $("#requestOverrideDetails").hide();
            }
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
                        populateEnabledOverrides(true);
                        if(enabledCount == 0) {
                            // automatically enable the response if a first override is added
                            if($("#" + type + "_enabled_" + currentPathId).attr("checked") != "checked") {
                                enablePath(type, currentPathId);
                            }
                        }
                        if(type == "response") {
                            selectedResponseOverride = selected.value + "," + ordinal;
                            $("#responseOverrideSelect").val(-999).trigger("change");
                        }
                        else {
                            selectedRequestOverride = selected.value + "," + ordinal;
                            $("#requestOverrideSelect").val(-999).trigger("change");
                        }
                    }
                });
            });
        }

        function enablePath(type, pathId) {
            $("#" + type + "_enabled_" + pathId).click();
        }

        function populateResponseOverrideList(possibleEndpoints) {
            // preprocess methods into buckets based on class name
            var classHash = {};
            $.each(possibleEndpoints, function() {
                var methodArray = [];
                if (this.className in classHash) {
                    methodArray = classHash[this.className];
                }

                methodArray.push(this);
                classHash[this.className] = methodArray;
            });

            $("#responseOverrideSelect")
                .empty()
                .append($("<option>")
                    .val(-999).text("Select Override"))
                .append($("<optgroup>").attr("label", "General")
                    .append($("<option>")
                        .val("-1").text("Custom Response"))
                    .append($("<option>")
                        .val("-3").text("Set Header"))
                    .append($("<option>")
                        .val("-4").text("Remove Header")));

            $.each(classHash, function(hashKey, hashValue) {
                let $content = $("<optgroup>").attr("label", hashKey);
                $.each(hashValue, function(arrayKey, arrayValue) {
                    $content.append($("<option>")
                        .val(arrayValue.id)
                        .text(arrayValue.methodName + " (" + arrayValue.description + ")"));
                });

                $("#responseOverrideSelect").append($content);
            });
        }

        // this returns a formatted string of arguments for display in the "Order" column
        function getFormattedArguments(args, length) {
            var argString = [];

            // show XX instead of an argument since they aren't all set
            if (length > args.length) {
                for (var x = 0; x < length - 1; x++) {
                    argString.push("XX,");
                }
                argString.push("XX");
                return argString.join("");
            }

            // show actual args
            $.each(args, function(methodArgsX, methodArg) {
                var displayStr = methodArg;
                if (methodArg.length > 10) {
                    // truncate methodArg if it is > 10 char
                    displayStr = displayStr.substring(0, 7).trim() + "\u2026";
                } else if (!methodArg) {
                    displayStr = "XX";
                }
                argString.push(displayStr);
                argString.push(",");
            });

            argString.pop();

            return argString.join("");
        }

        // called to load the edit endpoint args
        function populateEditOverrideArgs(pathId, methodId, ordinal, type, autofocusOnForm) {
            $.ajax({
                type: "GET",
                url: '<c:url value="/api/path/"/>' + pathId + '/' + methodId,
                data: 'ordinal=' + ordinal + '&clientUUID=${clientUUID}',
                success: function(data) {
                    if(data.enabledEndpoint == null) {
                        return;
                    }

                    $("#" + type + "OverrideDetails .panel-title")
                        .text(data.enabledEndpoint.methodInformation.className + " " + data.enabledEndpoint.methodInformation.methodName);

                    var $formData = $("<form>");
                    var $formDiv = $("<div>").addClass("form-group");
                    $.each(data.enabledEndpoint.methodInformation.methodArguments, function(i, el) {
                        var inputId = type + "_args_" + i;
                        var inputValue;
                        if (data.enabledEndpoint.arguments.length > i) {
                            inputValue = data.enabledEndpoint.arguments[i];
                        } else if (data.enabledEndpoint.methodInformation.methodDefaultArguments[i] != null) {
                            inputValue = data.enabledEndpoint.methodInformation.methodDefaultArguments[i];
                        }

                        if (typeof data.enabledEndpoint.methodInformation.methodArgumentNames[i] != 'undefined') {
                            $formDiv
                                .append($("<label>")
                                    .attr("for", inputId)
                                    .text(data.enabledEndpoint.methodInformation.methodArgumentNames[i]));
                        }

                        if (methodId == -1) {
                            $formDiv
                                .append($("<textarea>")
                                    .attr({
                                        id: inputId,
                                        class: "form-control",
                                        rows: 10
                                    })
                                    .text(inputValue))
                                .append($("<label>")
                                    .attr("for", "setResponseCode")
                                    .text("Response Code"))
                                .append($("<dd>")
                                    .append($("<input>")
                                        .attr({
                                            "id": "setResponseCode",
                                            "min": 100,
                                            "max": 599,
                                            "class": "form-control",
                                            "type": "number"
                                        })
                                        .val(data.enabledEndpoint.responseCode)));
                        } else {
                            $formDiv
                                .append($("<label>")
                                    .attr({
                                        for: inputId,
                                        style: "font-weight: normal; font-style: italic;"
                                    })
                                    .text("(" + el + ")"))
                                .append($("<input>")
                                    .attr({
                                        "id": inputId,
                                        "class": "form-control",
                                        "type": "text",
                                    })
                                    .val(inputValue));
                        }
                    });

                    $formDiv
                        .append($("<label>")
                            .attr("for", "setRepeatNumber")
                            .text("Repeat Count"))
                        .append($("<input>")
                            .attr({
                                "id": "setRepeatNumber",
                                "type": "number",
                                "min": -1,
                                "class": "form-control"
                            })
                            .val(data.enabledEndpoint.repeatNumber));

                    $formData
                        .append($formDiv)
                        .append($("<button>")
                            .addClass("btn btn-primary")
                            .text("Apply"))
                        .append($("<button>")
                            .addClass("btn btn-default")
                            .attr("type", "reset")
                            .text("Clear"))
                        .submit(function(e) {
                            submitOverrideData(type, parseInt(pathId), parseInt(methodId), parseInt(ordinal), data.enabledEndpoint.methodInformation.methodArguments.length);
                            e.preventDefault();
                        });

                    $("#" + type + "OverrideParameters").empty().append($formData).show();
                    $("#" + type + "OverrideDetails").show();

                    if (autofocusOnForm === true) {
                        $("#" + type + "OverrideDetails form")
                            .find("input, textarea")
                            .first()
                            .focus();
                    }
                }
            });
        }

        function applyGeneralPathChanges() {
            event.preventDefault();
            var pathName = $("#pathName").attr("value");
            var path = $("#pathValue").attr("value");
            var contentType = $("#contentType").attr("value");
            var global = $("#pathGlobal").attr("checked") == "checked";
            var requestType = $("#requestType").val();
            var bodyFilter = $("#postBodyFilter").val();
            var repeat = $("#pathRepeatCount").attr("value");
            var code = $("#pathResponseCode").attr("value");

            var groupArray = $("#groupTable").jqGrid("getGridParam", "selarrrow");

            // reset informational divs
            $('#applyPathChangeSuccessDiv').hide();
            $('#applyPathChangeAlertDiv').hide();

            $.ajax({
                type: "POST",
                async: false,
                url: '<c:url value="/api/path/"/>' + currentPathId,
                data: ({clientUUID: "${clientUUID}", pathName: pathName, path: path, bodyFilter: bodyFilter, contentType: contentType, repeatNumber: repeat, requestType: requestType, global: global, responseCode: code, 'groups[]': groupArray}),
                success: function() {
                    $('#applyPathChangeSuccessDiv').show();
                },
                error: function(jqXHR) {
                    $('#applyPathChangeAlertDiv').show();
                    $('#applyPathChangeAlertTextDiv').html(jqXHR.responseText);
                }
            });
        }

        function submitOverrideData(type, pathId, methodId, ordinal, numArgs) {
            submitEndPointArgs(type, pathId, methodId, ordinal, numArgs);
            submitOverrideRepeatCountAndResponseCode(type, pathId, methodId, ordinal);
            loadPath(currentPathId);
        }

        function submitOverrideRepeatCountAndResponseCode(type, pathId, methodId, ordinal) {
            var repeatNumberValue = $("#setRepeatNumber").val();
            var responseCodeValue = $("#setResponseCode").val();

            $.ajax({
                type:"POST",
                url: '<c:url value="/api/path/"/>' + pathId + '/' + methodId,
                data: {
                    repeatNumber: repeatNumberValue,
                    responseCode: responseCodeValue,
                    ordinal: ordinal,
                    clientUUID: clientUUID
                },
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

            var formData = new FormData();
            formData.append("ordinal", ordinal);
            formData.append("clientUUID", clientUUID);
            for (var i = 0; i < args.length; i++) {
                formData.append('arguments[]', args[i]);
            }

            $.ajax({
                type:"POST",
                url: '<c:url value="/api/path/"/>' + pathId + '/' + methodId,
                data: formData,
                cache: false,
                processData: false,
                contentType: false,
                async: false,
                success: function(){

                }
            });
        }

        function populateEnabledOverrides(autofocusOnForm) {
            populateEnabledResponseOverrides(autofocusOnForm);
            populateEnabledRequestOverrides(autofocusOnForm);
        }

        function populateEnabledResponseOverrides(autofocusOnForm) {
            $.ajax({
                type: "GET",
                url : '<c:url value="/api/path/"/>' + currentPathId + '?profileIdentifier=${profile_id}&typeFilter[]=ResponseOverride&typeFilter[]=ResponseHeaderOverride&clientUUID=${clientUUID}',
                autofocusOnForm: autofocusOnForm,
                success: function(data) {
                    enabledOverridesSuccess('response', data);
                    if (selectedResponseOverride != 0) {
                        $("#responseOverrideEnabled").val(selectedResponseOverride);
                    }
                    changeResponseOverrideDiv(this.autofocusOnForm);
                }
            });
        }

        function populateEnabledRequestOverrides(autofocusOnForm) {
            $.ajax({
                type: "GET",
                url : '<c:url value="/api/path/"/>' + currentPathId + '?profileIdentifier=${profile_id}&typeFilter[]=RequestOverride&typeFilter[]=RequestHeaderOverride&clientUUID=${clientUUID}',
                autofocusOnForm: autofocusOnForm,
                success: function(data) {
                    enabledOverridesSuccess('request', data);
                    if (selectedRequestOverride != 0) {
                        $("#requestOverrideEnabled").val(selectedRequestOverride);
                    }
                    changeRequestOverrideDiv(this.autofocusOnForm);
                }
            });
        }

        function enabledOverridesSuccess(type, data) {
            var usedIndexes = {};

            $("#" + type + "OverrideEnabled").empty();
            $.each(data.enabledEndpoints, function() {
                var enabledId = this.overrideId;

                usedIndexes[enabledId] = (usedIndexes[enabledId] || 0) + 1;
                var repeat = this.repeatNumber >= 0 ? this.repeatNumber + "x " : "";

                // custom response/request
                if (enabledId < 0) {
                    $("#" + type + "OverrideEnabled").append($("<option>")
                        .val(enabledId + ',' + usedIndexes[enabledId])
                        .text(repeat + requestOverrideText(enabledId, this.arguments)));
                } else {
                    var method = data.possibleEndpoints.find(function(endpoint) {
                        return endpoint.id == enabledId;
                    });
                    if (!method) { return; }

                    var methodName = method.methodName;

                    // Add arguments to method name if they exist
                    if (method.methodArguments.length > 0) {
                        methodName += "(" + getFormattedArguments(this.arguments, method.methodArguments.length) + ")";
                    }

                    $("#" + type + "OverrideEnabled").append($("<option>")
                        .val(enabledId + ',' + usedIndexes[enabledId])
                        .text(repeat + methodName));
                }
            });
        }

        function requestOverrideText(enabledId, enabledArgs) {
            switch (enabledId) {
                case -1:
                    return 'Custom Response(' + getFormattedArguments(enabledArgs, 1) + ')';
                    break;
                case -2:
                    return "Custom Request";
                    break;
                case -3:
                case -5:
                    return 'Set Header(' + getFormattedArguments(enabledArgs, 2) + ')';
                    break;
                case -4:
                case -6:
                    return 'Remove Header(' + getFormattedArguments(enabledArgs, 1) + ')';
                    break;
                case -7:
                    return "Custom Post Body";
                    break;
                default:
                    return null;
                    break;
            }
        }

        function populateRequestOverrideList() {
            $("#requestOverrideSelect")
                .empty()
                .append($("<option>").val(-999).attr("selected", "selected").text("Select Override"))
                .append($("<optgroup>").attr("label", "General")
                    .append($("<option>").val("-2").text("Custom Request"))
                    .append($("<option>").val("-5").text("Set Header"))
                    .append($("<option>").val("-6").text("Remove Header"))
                    .append($("<option>").val("-7").text("Custom Post Body")));
        }

        function showServerGroupEdit() {
            let toggleButton = function() {
                $("#editServerGroups").toggleClass("btn-default btn-primary");
            }

            $("#serverEdit").dialog({
                title: "Edit Server Groups",
                width: 400,
                modal: true,
                buttons: {
                    "Done": function() {
                        $("#serverEdit").dialog("close");
                    }
                },
                open: toggleButton,
                close: toggleButton
            });
        }

        function highlightSelectedGroups(groupIds) {
            if (!groupIds) { return; }
            $("#groupTable").jqGrid("resetSelection");
            var ids = groupIds.split(",");
            for(var i = 0; i < ids.length; i++) {
                $("#groupTable").jqGrid("setSelection", ids[i], true);
            }
        }

        function dismissStatusNotificationDiv() {
            $("#statusNotificationDiv").fadeOut();
        }

        function dismissReorderNotificationDiv() {
            $("#reorderNotificationDiv").fadeOut();
        }
    </script>
</head>
<body>
    <!-- Hidden div for configuration file upload -->
    <div id="configurationUploadDialog" style="display:none;">
        <form id="configurationUploadForm">
            <div class="form-group">
                <label for="configurationUploadFile">Configuration file:</label>
                <input id="configurationUploadFile" class="form-control" type="file" accept=".json,application/json" name="fileData" />
            </div>
            <div class="form-group form-check">
                <label for="includeOdoConfiguration" class="form-check-label">Also Import Odo Configuration</label>
                <input id="includeOdoConfiguration" type="checkbox" class="form-check-input" name="IncludeOdoConfiguration" />
            </div>
        </form>
    </div>

    <%@ include file="clients_part.jsp" %>
    <%@ include file="pathtester_part.jsp" %>

    <nav class="navbar navbar-default" role="navigation">
        <div id="statusBar" class="container-fluid">
            <div class="navbar-header">
                <a class="navbar-brand" href="#">Odo</a>
            </div>

            <ul id="status2" class="nav navbar-nav navbar-left">
                <li><a href="#" onClick="navigateProfiles()">Profiles</a> </li>
                <li><a href="#" onClick="navigateRequestHistory()">Request History</a></li>
                <li><a href="#" onClick="navigatePathTester()">Path Tester</a></li>
                <li><a href="#" onClick="navigateEditGroups()">Edit Groups</a></li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Import/Export <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="#" onclick='exportConfigurationFile()'
                               data-toggle="tooltip" data-placement="right"
                               title="Click here to export active overrides and active server group">Export Override Configuration</a></li>
                        <li><a href="#" onclick='importConfiguration()'
                               data-toggle="tooltip" data-placement="right"
                               title="Click here to import active overrides and server group">Import Override Configuration</a></li>
                    </ul>
                </li>
            </ul>

            <div class="form-group navbar-form navbar-left">
                <span id="status"></span>
                <button id="resetProfileButton" class="btn btn-danger" onclick="resetProfile()"
                        data-toggle="tooltip" data-placement="bottom" title="Click here to reset all path settings in this profile.">Reset Profile</button>
                <!-- TO FIND HELP -->
                <button id="helpButton" class="btn btn-info" onclick="navigateHelp()"
                        target="_blank" data-toggle="tooltip" data-placement="bottom" title="Click here to read the readme.">Need Help?</button>
            </div>

            <ul id="clientInfo" class="nav navbar-nav navbar-right"></ul>
        </div>
    </nav><!-- /.navbar -->

    <div class="container-fluid">
        <div class="row">
            <div id="listContainer" class="col-xs-5">
                <table id="serverlist"></table>
                <div id="servernavGrid"></div>
                <table id="packages">
                    <tr><td></td></tr>
                </table>
                <div id="packagePager">
                </div>
                <!-- div for top bar notice -->
                <div class="ui-widget" id="statusNotificationDiv" style="display: none;" onClick="dismissStatusNotificationDiv()">
                    <div class="ui-state-highlight ui-corner-all" style="margin-top: 10px;  margin-bottom: 10px; padding: 0 .7em;">
                        <p style="margin-top: 10px; margin-bottom:10px;"><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
                            <span id="statusNotificationText"/></p>
                    </div>
                </div>
                <div class="ui-widget" id="reorderNotificationDiv" style="display: none;" onClick="dismissReorderNotificationDiv()">
                    <div class="ui-state-highlight ui-corner-all" style="margin-top: 10px;  margin-bottom: 10px; padding: 0 .7em;">
                        <p style="margin-top: 10px; margin-bottom:10px;"><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
                            <span id="reorderNotificationText"/></p>
                    </div>
                </div>
            </div>

            <div id="details" data-spy="affix" class="col-xs-7">
                <div class="detailsView" id="editDiv" style="display: none;">
                    <div style="position: relative;">
                        <ul class="nav nav-pills" id="nav">
                        </ul>
                        <div style="position: absolute; right: 1em; top: 0;">
                            <kbd>alt</kbd> <kbd>&larr;</kbd> <kbd>&rarr;</kbd>
                        </div>
                    </div>
                    <div id="tabs">
                        <ul>
                            <li><a href="#tabs-1">Response <kbd>1</kbd></a></li>
                            <li><a href="#tabs-2">Request <kbd>2</kbd></a></li>
                            <li><a href="#tabs-3">Configuration <kbd>3</kbd></a></li>
                        </ul>

                        <div id="tabs-1" class="container-flex">
                            <div class="row">
                                <div class="col-xs-5">
                                    <div class="panel panel-info">
                                        <div class="panel-heading">
                                            <h3 class="panel-title">Response Overrides</h3>
                                        </div>
                                        <div class="panel-body">
                                            <select id="responseOverrideEnabled" class="form-control mousetrap" multiple="multiple" style="height: 200px; resize: vertical;" onChange="changeResponseOverrideDiv()"></select>
                                            <div class="ui-state-default" style="display: inline-block;">
                                                <span class="ui-icon ui-icon-circle-triangle-n" title="Up" onClick="overrideMoveUp('response')"></span>
                                                <span class="ui-icon ui-icon-circle-triangle-s" title="Down" onClick="overrideMoveDown('response')"></span>
                                                <span class="ui-icon ui-icon-trash" title="Delete" onClick="overrideRemove('response')"></span>
                                            </div>

                                            <div class="form-group">
                                                <label for="responseOverrideSelect">Add override <kbd>+</kbd></label>
                                                <br />
                                                <select id="responseOverrideSelect" style="width: 100%;" onChange="overrideSelectChanged('response')">
                                                    <option value=-999>Select Override</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div><!-- /.col-xs-5 -->

                                <div class="col-xs-7">
                                    <div id="responseOverrideDetails" style="display: none;" class="panel panel-default">
                                        <div class="panel-heading">
                                            <h3 class="panel-title">Override Parameters</h3>
                                        </div>
                                        <div id="responseOverrideParameters" class="panel-body"></div>
                                    </div>
                                </div>
                            </div>
                        </div><!-- /#tabs-1 -->


                        <div id="tabs-2" class="container-flex">
                            <div class="row">
                                <div class="col-xs-5">
                                    <div class="panel panel-info">
                                        <div class="panel-heading">
                                            <h3 class="panel-title">Request Overrides</h3>
                                        </div>
                                        <div class="panel-body">
                                            <select id="requestOverrideEnabled" class="form-control mousetrap" multiple="multiple" style="height: 200px; resize: vertical;" onChange="changeRequestOverrideDiv()"></select>
                                            <div style="display: inline-block" class="ui-state-default">
                                                <span class="ui-icon ui-icon-circle-triangle-n" title="Up" onClick="overrideMoveUp('request')"></span>
                                                <span class="ui-icon ui-icon-circle-triangle-s" title="Down" onClick="overrideMoveDown('request')"></span>
                                                <span class="ui-icon ui-icon-trash" title="Delete" onClick="overrideRemove('request')"></span>
                                            </div>

                                            <div class="form-group">
                                                <label for="requestOverrideSelect">Add override <kbd>+</kbd></label>
                                                <br />
                                                <select id="requestOverrideSelect" style="width: 100%;" onChange="overrideSelectChanged('request')">
                                                    <option value=-999>Select Override</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div><!-- /.col-xs-5 -->

                                <div class="col-xs-7">
                                    <div id="requestOverrideDetails"  style="display:none" class="panel panel-default">
                                        <div class="panel-heading">
                                            <h3 class="panel-title">Override Parameters</h3>
                                        </div>
                                        <div id="requestOverrideParameters" class="panel-body">None</div>
                                    </div>
                                </div>
                            </div>
                        </div><!-- /#tabs-2 -->

                        <div id="tabs-3" class="container-flex">
                            <form onsubmit="applyGeneralPathChanges();">
                                <div class="form-group form-check row">
                                    <label for="pathGlobal" class="col-sm-3 form-check-label mousetrap">Global?</label>
                                    <div class="col-sm-9">
                                        <input id="pathGlobal" type="checkbox" class="form-check-input" />
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="pathName" class="col-sm-3">Path Name</label>
                                    <div class="col-sm-9">
                                        <input id="pathName" type="text" class="form-control" />
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="pathValue" class="col-sm-3">Path Value</label>
                                    <div class="col-sm-9">
                                        <input id="pathValue" type="text" class="form-control" />
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="contentType" class="col-sm-3">Content Type</label>
                                    <div class="col-sm-9">
                                        <input id="contentType" type="text" class="form-control" />
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="requestType" class="col-sm-3">Request Type</label>
                                    <div class="col-sm-3">
                                        <select id="requestType" class="form-control mousetrap" onChange="pathRequestTypeChanged()">
                                            <option value="0">ALL</option>
                                            <option value="1">GET</option>
                                            <option value="2">PUT</option>
                                            <option value="3">POST</option>
                                            <option value="4">DELETE</option>
                                        </select>
                                    </div>
                                    <label for="pathRepeatCount" class="col-sm-3">Repeat Count</label>
                                    <div class="col-sm-3">
                                        <input id="pathRepeatCount" type="number" min="-1" class="form-control" />
                                    </div>
                                </div>

                                <div id="postGeneral" class="form-group row" style="display:none;">
                                    <label for="postBodyFilter" class="col-sm-3">
                                        Request body<br />filter<br />(optional)
                                    </label>
                                    <div class="col-sm-9">
                                        <textarea id="postBodyFilter" rows="3" class="form-control"></textarea>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <div class="col-sm-3">
                                        <label>Groups</label>
                                    </div>
                                    <div class="col-sm-9">
                                        <table id="groupTable"></table>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <div class="col-sm-9 col-sm-offset-3">
                                        <input type="submit" class="btn btn-primary" value="Apply" />
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
                                    </div>
                                </div>
                            </form>
                        </div><!-- /#tabs-3 -->
                    </div>
                </div><!-- /#editDiv -->
            </div><!-- /#details -->
        </div>
    </div>

    <div id="serverEdit" style="display: none;">
        <table id="serverGroupList"></table>
        <div id="serverGroupNavGrid"></div>
    </div>
</body>
</html>
