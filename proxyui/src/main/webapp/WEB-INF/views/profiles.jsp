<!DOCTYPE html>
<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page session="false" %>
    <head>
        <title>API Profiles</title>

        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <%@ include file="/resources/js/webjars.include" %>
        <style type="text/css">
            .ui-jqgrid tr.jqgrow td,
            #jqgh_profilelist_name { /* MAKE PROFILE NAMES BIGGER */
                font-size: medium;
            }

            ul {
                list-style-type: none;
            }
        </style>
        <script type="text/javascript">

        function navigateHelp() {
            window.open("https://github.com/groupon/odo#readme", "help");
        }

        //makes the specific profile active, goes to the database column
        function makeActive(profile_id){
            $.ajax({
                type: "POST",
                url: '<c:url value="/api/profile/' + profile_id + '/clients/-1"/>',
                data: "active=" + 'true',
                success: function(data) {
                    jQuery("#profilelist").trigger("reloadGrid");
                }
            });
        }

        //for now just opens up a new window. dont know if we will want more in the future
        /* THIS IS CALLED TO GO TO THE CORRECT "EDIT PROFILE" PAGE.*/
        function editProfile(profile_id){
            window.location = "edit/" + profile_id;
        }

        var currentProfileId = -1;
        // this just sets the current profile ID so that other formatters can use it
        function idFormatter(cellvalue, options, rowObject) {
            currentProfileId = cellvalue;
            return cellvalue;
        }

        // formatter for the name column
        function nameFormatter(cellvalue, options, rowObject) {
            return '<div class="ui-state-default" title="Edit Profile" onClick="editProfile(' + currentProfileId + ')">' + cellvalue + '</div>'
        }

        $(document).ready(function() {
            $("#helpButton").tooltip();

            var profileList = jQuery("#profilelist");
            profileList
            .jqGrid({
                url : '<c:url value="/api/profile"/>',
                autowidth: false,
                sortable: true,
                sorttext: true,
                multiselect: true,
                multiboxonly: true,
                rowList: [], // disable page size dropdown
                pgbuttons: false, // disable page control like next, back button
                pgtext: null,
                cellEdit: true,
                datatype: "json",
                colNames: ['ID', 'Profile Name', 'Name'],
                colModel: [ {
                    name: 'id',
                    index: 'id',
                    width: 55,
                    hidden: true,
                    formatter: idFormatter
                }, {
                    // we have this hidden one so the form Add works properly
                    name: 'name',
                    index: 'name',
                    width: 55,
                    editable: true,
                    hidden: true
                }, {
                    name: 'name',
                    index: 'displayProfileName',
                    width: 400,
                    editable: false,
                    formatter: nameFormatter,
                    sortable: true
                }],
                jsonReader : {
                    page: "page",
                    total: "total",
                    records: "records",
                    root: 'profiles',
                    repeatitems: false
                },
                cellurl: '/testproxy/edit/api/server',
                rowList: [],
                pager: '#profilenavGrid',
                sortname: 'id',
                viewrecords: true,
                sortorder: "desc",
                caption: 'Profiles'
            });
            profileList.jqGrid('navGrid', '#profilenavGrid', {
                edit : false,
                add : true,
                del : true
            },
            {},
            {
                jqModal: true,
                url: '<c:url value="/api/profile"/>',
                beforeShowForm: function(form) {
                    $('#tr_name', form).show();
                },
                reloadAfterSubmit: true,
                closeAfterAdd: true,
                closeAfterEdit: true,
                width: 400
            },
            {
                url: '<c:url value="/api/profile/delete"/>',
                mtype: 'POST',
                reloadAfterSubmit:true,
                onclickSubmit: function(rp_ge, postdata) { /* CODE CHANGED TO ALLOW FOR MULTISELECTION*/
                    /* IDS GIVEN IN AS A STRING SEPARATED BY COMMAS.
                     SEPARATE INTO AN ARRAY.
                     */
                    var rowids = postdata.split(",");

                    /* FOR EVERY ROW ID TO BE DELETED,
                     GET THE CORRESPONDING PROFILE ID.
                     */
                    var params = "";
                    for( var i = 0; i < rowids.length; i++) {
                        var odoId = $(this).jqGrid('getCell', rowids[i], 'id');
                        params += "profileIdentifier=" + odoId + "&";
                    }

                    rp_ge.url = '<c:url value="/api/profile/delete"/>?' +
                            params;

                  }
            });
            profileList.jqGrid('gridResize');

            $('#configurationUploadForm').submit(function(event) {
                event.preventDefault();

                var file = $('#configurationUploadFile').get(0).files[0];
                var formData = new FormData();
                formData.append('fileData', file, file.name);

                $.ajax({
                    type: "POST",
                    url: '<c:url value="/api/backup"/>',
                    data: formData,
                    processData: false,
                    contentType: false,
                    beforeSend: function() {
                        $("#statusNotificationText").text("Uploading configuration...");
                        $("#statusNotificationStateDiv")
                            .removeClass("ui-state-error")
                            .addClass("ui-state-highlight");
                        $("#statusNotificationDiv").fadeIn();

                        // disable form buttons
                        $("#configurationUploadDialog ~ .ui-dialog-buttonpane button")
                            .prop("disabled", true)
                            .addClass("ui-state-disabled");
                    },
                    success: function() {
                        window.location.reload();
                    },
                    error: function() {
                        $("#statusNotificationStateDiv")
                            .removeClass("ui-state-highlight")
                            .addClass("ui-state-error");
                        $("#statusNotificationText").text("An error occurred while uploading configuration...");

                        // enable form buttons
                        $("#configurationUploadDialog ~ .ui-dialog-buttonpane button")
                            .prop("disabled", false)
                            .removeClass("ui-state-disabled");
                    }
                });
            });
        });


        function exportConfiguration() {
            download('<c:url value="/api/backup"/>');
        }

        function importConfiguration() {
            $("#configurationUploadDialog").dialog({
                title: "Upload New Configuration",
                modal: true,
                buttons: {
                  "Submit": function() {
                    // submit form
                    $("#configurationUploadForm").submit();
                  },
                  "Cancel": function() {
                      $("#configurationUploadDialog").dialog("close");
                  }
                }
            });
        }
        </script>
    </head>

    <body>
        <!-- Hidden div for configuration file upload -->
        <div id="configurationUploadDialog" style="display:none;">
            <form id="configurationUploadForm" action='<c:url value="/api/backup"/>' method="post">
                <div class="form-group">
                    <label for="configurationUploadFile">Configuration file:</label>
                    <input id="configurationUploadFile" class="form-control" type="file" name="fileData" />
                </div>
            </form>

            <!-- div for status notice -->
            <div class="ui-widget" id="statusNotificationDiv" style="display: none;">
                <div class="ui-state-highlight ui-corner-all" id="statusNotificationStateDiv" style="margin-top: 10px;  margin-bottom: 10px; padding: 0 .7em;">
                    <p style="margin-top: 10px; margin-bottom:10px;"><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
                    <span id="statusNotificationText"/></p>
                </div>
            </div>
        </div>

        <nav class="navbar navbar-default" role="navigation">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="#">Odo</a>
                </div>

                <ul class="nav navbar-nav navbar-left">
                    <li><a href="#" onclick='exportConfiguration()'>Export Configuration</a></li>
                    <li><a href="#" onclick='importConfiguration()'>Import Configuration</a></li>
                </ul>

                <div class="form-group navbar-form navbar-left">
                    <button id="helpButton" class="btn btn-info" onclick="navigateHelp()"
                            target="_blank" data-toggle="tooltip" data-placement="bottom" title="Click here to read the readme.">Need Help?</button>
                </div>

                <ul class="nav navbar-nav navbar-right">
                    <li>
                        <p class="navbar-text">Odo Version: <c:out value = "${version}"/></p>
                    </li>
                </ul>
            </div>
        </nav>

        <div>
            <table id="profilelist"></table>
            <div id="profilenavGrid"></div>
        </div>
    </body>
</html>
