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
        #editDiv.affix {
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

        /* custom styling for response/request enabled cells */
        #packages td input[type=checkbox] {
            position: absolute;
            z-index: -9999;
            width: 0;
            height: 0;
        }

        #packages td label[for] {
            display: block;
            font-weight: normal;
            height: 100%;
            width: 100%;
            margin-bottom: 0;
            cursor: pointer;
            font-size: 1.333333333em;
        }

        #packages td input[type=checkbox] + label {
            color: #aaa;
        }

        #packages td input[type=checkbox]:active:checked + label:before,
        #packages td input[type=checkbox] + label:before {
            content: "✗";
        }

        #packages td input[type=checkbox]:checked + label {
            background-color: #3399cc;
            color: white;
        }

        #packages td input[type=checkbox]:active + label,
        #packages td input[type=checkbox]:active:checked + label {
            background-color: #bbddee;
        }

        #packages td input[type=checkbox]:active + label:before,
        #packages td input[type=checkbox]:checked + label:before {
            content: "✔";
        }

        #packages td input[type=checkbox]:focus  + label,
        #packages td input[type=checkbox]:hover  + label {
            color: #777;
        }

        #packages td input[type=checkbox]:focus  + label {
            background: linear-gradient(315deg, #cc1111 5px, rgba(0,0,0,0) 6px);
        }

        #packages td input[type=checkbox]:checked:hover  + label {
            background-color: #1177aa;
            color: white;
        }

        #packages td input[type=checkbox]:checked:focus  + label {
            background: linear-gradient(315deg, white 5px, #1177aa 6px);
            color: #eee;
        }

        #packages td input[type=checkbox]:focus:active  + label,
        #packages td input[type=checkbox]:hover:active  + label,
        #packages td input[type=checkbox]:checked:focus:active  + label,
        #packages td input[type=checkbox]:checked:focus:hover:active  + label,
        #packages td input[type=checkbox]:checked:hover:active  + label {
            background: linear-gradient(315deg, white 5px, #bbddee 6px);
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

        var clientUUID = '${clientUUID}';
        var currentPathId = -1;
        var editServerGroupId = 0;

        function navigateHelp() {
            window.open("https://github.com/groupon/odo#readme","help");
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
                        .click(manageClientPopup)));
        }

        function changeActive(value) {
            $.ajax({
                type: "POST",
                url: '<c:url value="/api/profile/${profile_id}/clients/${clientUUID}"/>',
                data: "active=" + value,
                success: function() {
                    window.location.reload();
                },
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
            var rowData = $("#packages").jqGrid('getGridParam', 'data'); // all rows and data
            var rowIds = $("#packages").jqGrid('getDataIDs'); // visible rows IDs

            $(".nav-pills > li").remove();
            if (rowIds.length === 0) {
                loadPath(-1);
                return;
            }

            var selectedRow = $("#packages").jqGrid("getGridParam", "selrow");
            var pillsShown = false;
            $.each(rowIds, function(_index_, rowId) {
                var rowInfo = rowData[parseInt(rowId, 10) - 1];
                if (!(rowInfo.requestEnabled || rowInfo.responseEnabled || rowId == selectedRow)) {
                    return;
                }
                $("#nav").append($("<li>")
                    .attr("id", rowInfo.pathId)
                    .append($("<a>")
                        .attr({
                            href: "#tab" + rowInfo.pathId,
                            "data-toggle": "tab"})
                        .text(rowInfo.pathName)
                        .click(function() {
                            $("#packages").setSelection(rowId, true);
                        })));

                pillsShown = true;
            });

            if (!pillsShown) {
                loadPath(-1);
                return;
            }

            let $currentPathPill = $("#nav").find("#" + currentPathId);

            if ($currentPathPill.length) {
                $currentPathPill.addClass("active");
                loadPath(currentPathId);
            } else {
                loadPath(-1);
            }
        }

        // common function for grid reload
        function reloadGrid(gridId) {
            $(gridId).setGridParam({datatype:'json', page:1}).trigger("reloadGrid");
        }

        function overrideEnabledFormatter(cellvalue, options, rowObject) {
            var overrideType = options.colModel.name === "requestEnabled" ? "request" : "response";

            var elementId = overrideType + "_enabled_" + rowObject.pathId;

            return $("<div>")
                .append($("<input>")
                    .attr({
                        type: "checkbox",
                        id: elementId,
                        onchange: "overrideEnabledChanged(event, " + "\"" + overrideType + "\")",
                        checked: cellvalue,
                        offval: "0",
                        "data-path": rowObject.pathId,
                        "data-row": options.rowId
                    })
                    .addClass("mousetrap")
                    .val(cellvalue ? "1" : "0"))
                .append($("<label>").attr("for", elementId))
                .html();
        }

        function overrideEnabledChanged(event, overrideType) {
            var $checkbox = $(event.target);
            var pathId = $checkbox.data("path");
            var isEnabled = $checkbox.is(":checked") ? 1 : 0;

            toggleOverride(pathId, overrideType, isEnabled, function() {
                var rowId = $checkbox.data("row");
                var originalTargetId = $checkbox.attr("id");
                var isEnabled = $checkbox.is(":checked");

                var rowData = $("#packages").getLocalRow(rowId);
                rowData[overrideType + "Enabled"] = isEnabled;
                $("#packages").setRowData(rowId, rowData);
                $("#packages").setSelection(rowId, true);
                // maintain focus on checkbox
                $("#" + originalTargetId).focus();

                if (!isEnabled) return;

                if (overrideType == "response") {
                    $("[href=\"#tabs-1\"]").click();
                } else {
                    $("[href=\"#tabs-2\"]").click();
                }
            }, function() { alert("Could not properly set value"); });
        }

        function toggleOverride(pathId, overrideType, isEnabled, successCb, errorCb) {
            return $.ajax({
                type: "POST",
                url: '<c:url value="/api/path/"/>' + pathId,
                data: overrideType + 'Enabled=' + isEnabled + '&clientUUID=' + clientUUID,
                success: successCb,
                error: errorCb
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
            $.jgrid.no_legacy_api = true;
            $.jgrid.useJSON = true;
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
                        document.location.href =  document.location.protocol + "//" + document.location.hostname + ":" +  document.location.port + document.location.pathname;
                    } else {
                        if ("${clientUUID}" == "-1" && $.cookie("UUID") != null) {
                            document.location.href =  document.location.protocol + "//" + document.location.hostname + ":" +  document.location.port + document.location.pathname +
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
                $("#pathGlobal").focus();
            });
            // Filter
            Mousetrap.bind('f', function(event) {
                event.preventDefault();
                $("#gs_pathName").focus();
            });
            // Request/response override toggle navigation
            Mousetrap.bind(['left', 'right', 'up', 'down'], function(event) {
                var $focusedCheckbox = $("#packages td input[type=checkbox]:focus");
                if (!$focusedCheckbox.length) return;

                var $closestTd = $focusedCheckbox.closest("td");
                switch (event.key) {
                    case "ArrowLeft":
                        if ($closestTd.attr("aria-describedby") == "packages_requestEnabled") {
                            $closestTd.prev("td").find("input:checkbox").focus();
                        } else {
                            $closestTd.closest("tr").prev("tr").find("td[aria-describedby=packages_requestEnabled] input:checkbox").focus();
                        }
                        break;
                    case "ArrowRight":
                        if ($closestTd.attr("aria-describedby") == "packages_responseEnabled") {
                            $closestTd.next("td").find("input:checkbox").focus();
                        } else {
                            $closestTd.closest("tr").next("tr").find("td[aria-describedby=packages_responseEnabled] input:checkbox").focus();
                        }
                        break;
                    case "ArrowUp":
                        var describedBy = $closestTd.attr("aria-describedby");
                        $closestTd.closest("tr").prev("tr").find("td[aria-describedby=" + describedBy + "] input:checkbox").focus();
                        break;
                    case "ArrowDown":
                        var describedBy = $closestTd.attr("aria-describedby");
                        $closestTd.closest("tr").next("tr").find("td[aria-describedby=" + describedBy + "] input:checkbox").focus();
                        break;
                    default: return; break;
                }

                event.preventDefault();
            });
            // Active paths navigation
            Mousetrap.bind('alt+right', function(event) {
                var $activeTab = $("#nav .active");
                if ($activeTab.length) {
                    $activeTab.next().find("a").click();
                } else {
                    $("#nav li").first().find("a").click();
                }
            });
            Mousetrap.bind('alt+left', function(event) {
                var $activeTab = $("#nav .active");
                if ($activeTab.length) {
                    $activeTab.prev().find("a").click();
                } else {
                    $("#nav li").first().find("a").click();
                }
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
            Mousetrap.bind(['command+a', 'ctrl+a'], function(event) {
                if ($("#responseOverrideEnabled").is(":visible:focus")) {
                    event.preventDefault();
                    $("#responseOverrideEnabled option").prop("selected", true);
                } else if ($("#requestOverrideEnabled").is(":visible:focus")) {
                    event.preventDefault();
                    $("#requestOverrideEnabled option").prop("selected", true);
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
                        width: 400,
                        editable: true,
                        editrules: {
                            required: true
                        },
                        formoptions: {label: "Path name (*)"},
                        search: true,
                        searchoptions: {
                            clearSearch: false,
                            attr: { placeholder: "Type here to filter columns (f)" }
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
                        width: 60,
                        editable: true,
                        edittype: 'select',
                        editoptions: {
                            defaultValue: 0,
                            value: "0:ALL;1:GET;2:PUT;3:POST;4:DELETE"
                        },
                        editrules: {edithidden: true},
                        formatter: requestTypeFormatter,
                        search: true,
                        searchoptions: {
                            clearSearch: false,
                            attr: { placeholder: "0...4" }
                        }
                    },
                    {
                        name: 'responseEnabled',
                        index: 'responseEnabled',
                        width: 50,
                        align: 'center',
                        editable: false,
                        formatter: overrideEnabledFormatter,
                        formatoptions: {disabled: false},
                        search: true,
                        searchoptions: {
                            clearSearch: false,
                            attr: { placeholder: "T/F" }
                        }
                    }, {
                        name: 'requestEnabled',
                        index: 'requestEnabled',
                        width: 50,
                        align: 'center',
                        editable: false,
                        formatter: overrideEnabledFormatter,
                        formatoptions: {disabled: false},
                        search: true,
                        searchoptions: {
                            clearSearch: false,
                            attr: { placeholder: "T/F" }
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
                beforeSelectRow: function(rowid, event) {
                    var $target = $(event.target);
                    return !($target.is(":checkbox") || $target.is("label[for]"));
                },
                onSelectRow: function(id) {
                    var data = $("#packages").jqGrid('getRowData', id);
                    currentPathId = data.pathId;
                    updateDetailPills();

                    var $rowElement = $("tr#" + id);

                    if (!$rowElement.find(":checkbox").is(":focus").length) {
                        $rowElement.find(":checkbox").first().focus();
                    }

                    var docViewTop = $(window).scrollTop();
                    var docViewBottom = docViewTop + $(window).height();

                    var elemTop = $rowElement.offset().top;
                    var elemBottom = elemTop + $rowElement.height();

                    if ((elemBottom <= docViewBottom) && (elemTop >= docViewTop)) {
                        return;
                    }

                    $([document.documentElement, document.body]).animate({
                        scrollTop: $rowElement.offset().top - $(window).height() / 2
                    }, 100);
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
                        updateDetailPills();
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
                    }
                },
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
                    for (var i = 0; i < paths.length; i++) {
                        if (i === paths.length - 1) {
                            pathOrder += paths[i]["pathId"];
                        } else {
                            pathOrder += paths[i]["pathId"] + ",";
                        }
                    }
                    $.ajax({
                        type: "POST",
                        url: '<c:url value="/pathorder/"/>${profile_id}',
                        data: {pathOrder: pathOrder},
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

                    if (sortableAllowed) {
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
                initSelection: function(element, callback) {
                    $.ajax('<c:url value="/api/profile/${profile_id}/clients/${clientUUID}"/>').done(function(data) {
                        $.ajax('<c:url value="/api/servergroup"/>' + '/' + data.client.activeServerGroup +'?profileId=${profile_id}')
                            .done(function(data2) {
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
                    results: function(data) {
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
                        return this.text.localeCompare(term) === 0;
                    }).length === 0) {
                        return {id:term, text:term, isNew:true};
                    }
                },
                formatResult: function(term) {
                    if (term.isNew) {
                        return 'create "' + term.text + '"';
                    } else {
                        return term.text;
                    }
                },
                dropdownAutoWidth: true
            });
            $("#serverGroupSelection").on("change", function(e) {
                if(e.added.isNew) {
                    $.ajax({
                        type: "POST",
                        url: '<c:url value="/api/servergroup" />',
                        data: {
                            name: e.added.id,
                            profileId: "${profile_id}"
                        },
                        success: function(data) {
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

            $("#editDiv").affix({
                offset: {
                    top: function() {
                        return $("nav.navbar").position().top + $("nav.navbar").height() + parseInt($("nav.navbar").css("margin-bottom"), 10) - 12;
                    }
                }
            });

            $("#editDiv").on("affix.bs.affix", function(event) {
                var pctWidth = $(event.target).width() / $(window).width() * 100;
                $(event.target).css("width", pctWidth + "%");
            });

            $("#editDiv").on("affix-top.bs.affix", function(event) {
                $(event.target).css("width", "");
            });

            // temporary kludge to get the sizes right
            setTimeout(function() {
                $("#serverlist").setGridWidth($("#listContainer").width());
            }, 150);
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
                data: {
                    activate: true,
                    profileId: "${profile_id}",
                    clientUUID: "${clientUUID}"
                },
                success: function(data) {
                    reloadGrid("#serverlist");
                }
            });
        }

        function loadPath(pathId) {
            if(pathId < 0) {
                $("#tabs").hide();
                return;
            }

            $.ajax({
                type: "GET",
                url: '<c:url value="/api/path/"/>' + pathId,
                data: 'clientUUID=${clientUUID}',
                success: function(data) {
                    // populate Configuration values
                    $("#tabs").show();
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
                    getEnabledResponseOverrides(populateEnabledResponseOverrides());
                    getEnabledRequestOverrides(populateEnabledRequestOverrides());

                    setResponseOverridesActiveIndicator(data.responseEnabled);
                    setRequestOverridesActiveIndicator(data.requestEnabled);

                    // reset informational divs
                    $('#applyPathChangeSuccessDiv').hide();
                    $('#applyPathChangeAlertDiv').hide();
                }
            });
        }

        function pathRequestTypeChanged() {
            var requestType = $("#requestType").val();
            $("#postGeneral").toggle(requestType != 1 && requestType != 4);
        }

        function overrideRemove(type) {
            var defer = $.Deferred();
            defer.resolve();

            $("select#" + type + "OverrideEnabled" + " option:selected").each(function(i, selected) {
                var methodId = $(selected).data("override-id");
                var ordinal = $(selected).data("ordinal");
                defer = defer.pipe(function() {
                    return overrideRemoveRequest(methodId, ordinal);
                });
            });

            defer.done(function() {
                if (type == "response") {
                    selectedResponseOverride = 0;
                    getEnabledResponseOverrides(populateEnabledResponseOverrides());
                } else {
                    selectedRequestOverride = 0;
                    getEnabledRequestOverrides(populateEnabledRequestOverrides());
                }
            });
        }

        function overrideRemoveRequest(methodId, ordinal) {
            $.ajax({
                type: 'POST',
                url: '<c:url value="/api/path/"/>' + currentPathId + '/' + methodId,
                data: {
                    ordinal: ordinal,
                    clientUUID: clientUUID,
                    _method: 'DELETE'
                }
            });
        }

        function overrideMoveUp(type) {
            return overrideMove(type, "Up");
        }

        function overrideMoveDown(type) {
            return overrideMove(type, "Down");
        }

        function overrideMove(type, direction) {
            var defer = $.Deferred();
            defer.resolve();

            var $options = $("select#" + type + "OverrideEnabled" + " option:selected");
            if (direction == "Down") {
                $options = $($options.get().reverse());
            }

            $options.each(function(i, selected) {
                defer = defer.pipe(function() {
                    return overrideMoveRequest(selected.value, direction);
                });
            });

            defer.done(function() {
                if (type == "response") {
                    getEnabledResponseOverrides(refreshSelectedResponseOverride());
                } else {
                    getEnabledRequestOverrides(refreshSelectedRequestOverride());
                }
            });
        }

        function overrideMoveRequest(value, direction) {
            var data = { clientUUID: clientUUID };
            data["enabledMove" + direction] = value;

            return $.ajax({
                type: "POST",
                url: '<c:url value="/api/path/"/>' + currentPathId,
                data: data
            });
        }

        function changeResponseOverrideDiv(autofocusOnForm) {
            var selections = $("select#responseOverrideEnabled option:selected");

            if (selections.length > 1) {
                $("#responseOverrideParameters").html("");
                $("#responseOverrideDetails").hide();
                selectedResponseOverride = 0;
            } else if (selections.length == 1) {
                var id = selections.val();
                selectedResponseOverride = id;
                var methodId = selections.data("override-id");
                var ordinal = selections.data("ordinal");
                populateEditOverrideConfiguration(currentPathId, methodId, ordinal, "response", autofocusOnForm);
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
                var id = selections.val();
                selectedRequestOverride = id;
                var methodId = selections.data("override-id");
                var ordinal = selections.data("ordinal");
                populateEditOverrideConfiguration(currentPathId, methodId, ordinal, "request", autofocusOnForm);
            }
            else {
                // nothing selected
                selectedRequestOverride = 0;
                $("#requestOverrideParameters").empty();
                $("#requestOverrideDetails").hide();
            }
        }

        function setResponseOverridesActiveIndicator(isResponseEnabled) {
            $("#tabs-1 .panel").toggleClass("panel-default", !isResponseEnabled).toggleClass("panel-info", isResponseEnabled);
            $("#tabs-1 .panel-title .label").toggle(!isResponseEnabled);
        }

        function setRequestOverridesActiveIndicator(isRequestEnabled) {
            $("#tabs-2 .panel").toggleClass("panel-default", !isRequestEnabled).toggleClass("panel-info", isRequestEnabled);
            $("#tabs-2 .panel-title .label").toggle(!isRequestEnabled);
        }

        // get the next available ordinal for methodId on a specific path
        function getNextOrdinal(selectId, methodId) {
            return (parseInt($("select#" + selectId + " option[data-override-id=" + methodId + "]").last().data("ordinal"), 10) + 1) || 1;
        }

        var selectedResponseOverride = 0;
        var selectedRequestOverride = 0;

        // Called when a different override is selected from the select box
        function overrideSelectChanged(overrideType) {
            var enabledCount = $("select#" + overrideType + "OverrideEnabled option").length;

            $("select#" + overrideType + "OverrideSelect option:selected").each(function(i, selected) {
                if (selected.value == -999)
                    return true;

                // get the next ordinal so we can pop up the argument dialogue
                var ordinal = getNextOrdinal(overrideType + "OverrideEnabled", selected.value);

                $.ajax({
                    type: "POST",
                    url: '<c:url value="/api/path/"/>' + currentPathId,
                    data: {
                        addOverride: selected.value,
                        clientUUID: clientUUID
                    },
                    success: function() {
                        if (enabledCount == 0) {
                            // automatically enable the response if a first override is added
                            toggleOverride(currentPathId, overrideType, true, function() {
                                var rowId = $("#" + overrideType + "_enabled_" + currentPathId).data("row");
                                var rowData = $("#packages").getLocalRow(rowId);
                                if (!rowData[overrideType + "Enabled"]) {
                                    rowData[overrideType + "Enabled"] = true;
                                    $("#packages").setRowData(rowId, rowData);

                                    if (overrideType == "response") {
                                        setResponseOverridesActiveIndicator(true);
                                    } else {
                                        setRequestOverridesActiveIndicator(true);
                                    }
                                }
                            });
                        }

                        if (overrideType == "response") {
                            getEnabledResponseOverrides(populateEnabledResponseOverrides(true));
                            selectedResponseOverride = selected.value + "," + ordinal;
                            $("#responseOverrideSelect").val(-999).trigger("change");
                        }
                        else {
                            getEnabledRequestOverrides(populateEnabledRequestOverrides(true));
                            selectedRequestOverride = selected.value + "," + ordinal;
                            $("#requestOverrideSelect").val(-999).trigger("change");
                        }
                    }
                });
            });
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
            $.each(args, function(i, methodArg) {
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

        function populateEditOverrideConfiguration(pathId, methodId, ordinal, overrideType, autofocusOnForm) {
            $.ajax({
                type: "GET",
                url: '<c:url value="/api/path/"/>' + pathId + '/' + methodId,
                data: 'ordinal=' + ordinal + '&clientUUID=${clientUUID}',
                success: function(data) {
                    populateEditOverrideConfigurationSuccess(data, pathId, methodId, ordinal, overrideType, autofocusOnForm);
                }
            });
        }

        function populateEditOverrideConfigurationSuccess(data, pathId, methodId, ordinal, overrideType, autofocusOnForm) {
            if (data.enabledEndpoint == null) {
                return;
            }

            $("#" + overrideType + "OverrideDetails .panel-title")
                .text(data.enabledEndpoint.methodInformation.className + " " + data.enabledEndpoint.methodInformation.methodName);

            var $formData = $("<form>");
            var $formDiv = $("<div>").addClass("form-group");
            $.each(data.enabledEndpoint.methodInformation.methodArguments, function(i, el) {
                var inputId = overrideType + "_args_" + i;
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
                            .val(inputValue)
                            .on("input", function(event) {
                                toggleFormSubmitEnabled($(event.target).closest("form"), true);
                            }))
                        .append($("<label>")
                            .attr("for", "setResponseCode")
                            .text("Response Code"))
                        .append($("<input>")
                            .attr({
                                id: "setResponseCode",
                                min: 100,
                                max: 599,
                                default: data.enabledEndpoint.responseCode,
                                class: "form-control",
                                type: "number"
                            })
                            .val(data.enabledEndpoint.responseCode)
                            .on("input", function(event) {
                                toggleFormSubmitEnabled($(event.target).closest("form"), true);
                            }));
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
                                id: inputId,
                                class: "form-control",
                                type: "text",
                            })
                            .val(inputValue)
                            .on("input", function(event) {
                                toggleFormSubmitEnabled($(event.target).closest("form"), true);
                            }));
                }
            });

            $formDiv
                .append($("<label>")
                    .attr("for", "setRepeatNumber")
                    .text("Repeat Count"))
                .append($("<input>")
                    .attr({
                        id: "setRepeatNumber",
                        type: "number",
                        min: -1,
                        default: data.enabledEndpoint.repeatNumber,
                        required: true,
                        class: "form-control"
                    })
                    .val(data.enabledEndpoint.repeatNumber)
                    .on("input", function(event) {
                        toggleFormSubmitEnabled($(event.target).closest("form"), true);
                    }));

            $formData
                .append($formDiv)
                .append($("<button>")
                    .addClass("btn btn-primary")
                    .text("Apply"))
                .append($("<button>")
                    .addClass("btn btn-default")
                    .attr("type", "reset")
                    .text("Clear"))
                .on("reset", function(event) {
                    toggleFormSubmitEnabled($(event.target), true);

                    $(":input", event.target)
                        .not(':button, :submit, :reset, :hidden, [default]')
                        .removeAttr('checked')
                        .removeAttr('selected')
                        .not(':checkbox, :radio, select')
                        .val('');

                    event.preventDefault();
                })
                .submit(function(event) {
                    submitOverrideData(event, overrideType, parseInt(pathId, 10), parseInt(methodId, 10), parseInt(ordinal, 10), data.enabledEndpoint.methodInformation.methodArguments.length);
                });

            $("#" + overrideType + "OverrideParameters").empty().append($formData).show();
            $("#" + overrideType + "OverrideDetails").show();

            if (autofocusOnForm === true) {
                $("#" + overrideType + "OverrideDetails form")
                    .find("input, textarea")
                    .first()
                    .focus();
            }
        }

        function toggleFormSubmitEnabled($formElement, isEnabled) {
            var $submitButton = $formElement.find(":submit")

            if (isEnabled) {
                $submitButton
                    .text("Apply")
                    .attr("class", "btn btn-primary");
            } else {
                $submitButton
                    .text("Saved!")
                    .attr("class", "btn btn-success");
            }
            $submitButton.attr("disabled", !isEnabled)
        }

        function applyGeneralPathChanges() {
            event.preventDefault();
            var pathName = $("#pathName").val();
            var path = $("#pathValue").val();
            var contentType = $("#contentType").val();
            var isGlobal = $("#pathGlobal").is(":checked");
            var requestType = $("#requestType").val();
            var bodyFilter = $("#postBodyFilter").val();
            var repeat = $("#pathRepeatCount").val();
            var code = $("#pathResponseCode").val();

            var groupArray = $("#groupTable").jqGrid("getGridParam", "selarrrow");

            // reset informational divs
            $('#applyPathChangeSuccessDiv').hide();
            $('#applyPathChangeAlertDiv').hide();

            $.ajax({
                type: "POST",
                url: '<c:url value="/api/path/"/>' + currentPathId,
                data: {
                    clientUUID: "${clientUUID}",
                    pathName: pathName,
                    path: path,
                    bodyFilter: bodyFilter,
                    contentType: contentType,
                    repeatNumber: repeat,
                    requestType: requestType,
                    global: isGlobal,
                    responseCode: code,
                    "groups[]": groupArray
                },
                success: function() {
                    $("#applyPathChangeSuccessDiv").show(0, function() {
                        setTimeout(function() {
                            $("#applyPathChangeSuccessDiv").fadeOut(250);
                        }, 3000);
                    });
                },
                error: function(jqXHR) {
                    $("#applyPathChangeAlertTextDiv").text(jqXHR.responseText);
                    $("#applyPathChangeAlertDiv").show(0, function() {
                        setTimeout(function() {
                            $("#applyPathChangeAlertDiv").fadeOut(250);
                        }, 3000);
                    });
                }
            });
        }

        function submitOverrideData(event, overrideType, pathId, methodId, ordinal, numArgs) {
            event.preventDefault();
            var formData = new FormData();
            formData.append("ordinal", ordinal);
            formData.append("clientUUID", clientUUID);
            for (var i = 0; i < numArgs; i++) {
                formData.append('arguments[]', $("#" + overrideType + "_args_" + i).val());
            }

            $.ajax({
                type:"POST",
                url: '<c:url value="/api/path/"/>' + pathId + '/' + methodId,
                data: formData,
                cache: false,
                processData: false,
                contentType: false,
                success: function() {
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
                        success: function() {
                            toggleFormSubmitEnabled($(event.target), false);
                            // TODO: *MANUALLY* change the active overrides select
                            if (overrideType == "response") {
                                getEnabledResponseOverrides(refreshSelectedResponseOverride());
                            } else {
                                getEnabledRequestOverrides(refreshSelectedRequestOverride());
                            }
                        },
                        error: function(err) {
                            debugger;
                            alert("We were unable to save your override. Please try again.");
                        }
                    });
                },
                error: function(err) {
                    debugger;
                    alert("We were unable to save your override. Please try again.");
                }
            });
        }

        function getEnabledResponseOverrides(cb) {
             $.ajax({
                type: "GET",
                url: '<c:url value="/api/path/"/>' + currentPathId + '?profileIdentifier=${profile_id}&typeFilter[]=ResponseOverride&typeFilter[]=ResponseHeaderOverride&clientUUID=${clientUUID}',
                success: cb
            });
        }

        function populateEnabledResponseOverrides(autofocusOnForm) {
            return function(data) {
                enabledOverridesSuccess('response', data);
                if (selectedResponseOverride != 0) {
                    $("#responseOverrideEnabled").val(selectedResponseOverride);
                }
                changeResponseOverrideDiv(autofocusOnForm);
            };
        }

        function refreshSelectedResponseOverride() {
            return function(data) {
                // TODO: Consider changing only the text of the <option> that is selected.
                // Then we do not have to execute .val()
                enabledOverridesSuccess('response', data);
                if (selectedResponseOverride != 0) {
                    $("#responseOverrideEnabled").val(selectedResponseOverride);
                }
            };
        }

        function getEnabledRequestOverrides(cb) {
            $.ajax({
                type: "GET",
                url: '<c:url value="/api/path/"/>' + currentPathId + '?profileIdentifier=${profile_id}&typeFilter[]=RequestOverride&typeFilter[]=RequestHeaderOverride&clientUUID=${clientUUID}',
                success: cb
            });
        }

        function populateEnabledRequestOverrides(autofocusOnForm) {
            return function(data) {
                enabledOverridesSuccess('request', data);
                if (selectedRequestOverride != 0) {
                    $("#requestOverrideEnabled").val(selectedRequestOverride);
                }
                changeRequestOverrideDiv(autofocusOnForm);
            };
        }

        function refreshSelectedRequestOverride() {
            return function(data) {
                // TODO: Consider changing only the text of the <option> that is selected.
                // Then we do not have to execute .val()
                enabledOverridesSuccess('request', data);
                if (selectedRequestOverride != 0) {
                    $("#requestOverrideEnabled").val(selectedRequestOverride);
                }
            };
        }

        function enabledOverridesSuccess(type, data) {
            var usedIndexes = {};

            $("#" + type + "OverrideEnabled").empty();
            $.each(data.enabledEndpoints, function() {
                var name = getEndpointDisplayString(this, data);
                if (!name) return;

                var enabledId = this.overrideId;
                usedIndexes[enabledId] = (usedIndexes[enabledId] || 0) + 1;
                var dataValue = enabledId + ',' + usedIndexes[enabledId];

                $("#" + type + "OverrideEnabled").append($("<option>")
                    .val(dataValue)
                    .attr({
                        "data-override-id": enabledId,
                        "data-ordinal": usedIndexes[enabledId]
                    })
                    .text(name));
            });
        }

        function getEndpointDisplayString(endpoint, data) {
            var enabledId = endpoint.overrideId;
            var repeat = endpoint.repeatNumber >= 0 ? endpoint.repeatNumber + "x " : "";

            // custom response/request
            if (enabledId < 0) {
                return repeat + requestOverrideText(enabledId, endpoint.arguments);
            }

            var method = data.possibleEndpoints.find(function(endpoint) {
                return endpoint.id == enabledId;
            });
            if (!method) { return; }

            var methodName = method.methodName;

            // Add arguments to method name if they exist
            if (method.methodArguments.length > 0) {
                methodName += "(" + getFormattedArguments(endpoint.arguments, method.methodArguments.length) + ")";
            }

            return repeat + methodName;
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
                <input id="configurationUploadFile" class="form-control" type="file" name="fileData" />
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

            <ul class="nav navbar-nav navbar-left">
                <%@ include file="navigation_part.jsp" %>
                <li><a href="#" onClick="navigatePathTester()">Path Tester</a></li>
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

            <div id="details" class="col-xs-7">
                <div id="editDiv">
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
                                    <div class="panel">
                                        <div class="panel-heading">
                                            <h3 class="panel-title">
                                                <span>Response Overrides</span>
                                                <span class="label label-default label-medsmall">Inactive</span>
                                            </h3>
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
                                    <div class="panel">
                                        <div class="panel-heading">
                                            <h3 class="panel-title">
                                                <span>Request Overrides</span>
                                                <span class="label label-default label-medsmall">Inactive</span>
                                            </h3>
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
                                        <input id="pathGlobal" autofocus type="checkbox" class="form-check-input mousetrap" />
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
                                    </div>
                                </div>

                                <div class="form-group row" style="margin-bottom: 0;">
                                    <div class="col-sm-12">
                                        <div id="applyPathChangeSuccessDiv" class="alert alert-success" role="alert" style="display: none;">
                                            <span class="glyphicon glyphicon-info-sign"></span>
                                            <strong>Success:</strong>
                                            Configuration saved.
                                        </div>

                                        <div id="applyPathChangeAlertDiv" class="alert alert-danger" role="alert" style="display: none;">
                                            <span class="glyphicon glyphicon-exclamation-sign"></span>
                                            <strong>Alert:</strong>
                                            <span id="applyPathChangeAlertTextDiv"></span>
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
