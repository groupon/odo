<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page session="false" %>
<html>
    <head>
        <%@ include file="/resources/js/webjars.include" %>

        <title>API Profiles</title>
        <script type="text/javascript">

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

        function navigateConfiguration() {
            window.location ='<c:url value = '/configuration' />';
        }

        var currentProfileId = -1;
        // this just sets the current profile ID so that other formatters can use it
        function idFormatter( cellvalue, options, rowObject ) {
            currentProfileId = cellvalue;
            return cellvalue;
        }

        // formatter for the name column
        function nameFormatter( cellvalue, options, rowObject ) {
            /*var cellContents = '<div class="ui-state-default ui-corner-all" style="float:right" title="Edit Profile" onClick="editProfile(' + currentProfileId + ')">';
            cellContents +=	'<span class="ui-icon ui-icon-carat-1-e"></span></div>';
            cellContents += '<div>' + cellvalue + '</div>';*/
            var cellContents = '<div class="ui-state-default" title="Edit Profile" onClick="editProfile(' + currentProfileId + ')">';
            cellContents += '<div><span class="ui-icon ui-icon-carat-1-e" style="float:right"></span></div>';
            cellContents += '<div>' + cellvalue + '</div></div>'
            return cellContents;
        }

        // formats the active check box
        function activeFormatter( cellvalue, options, rowObject ) {
            var checkedValue = 0;
            if (cellvalue == true) {
                checkedValue = 1;
            }

            var newCellValue = '<input id="active_' + currentProfileId + '" onChange="makeActive(' + currentProfileId + ')" type="checkbox" offval="0" value="' + checkedValue + '"';

            if (checkedValue == 1) {
                newCellValue += 'checked="checked"';
            }

            newCellValue += '>';

            return newCellValue;
        }

        // formatter for the options column
        function optionsFormatter( cellvalue, options, rowObject ) {
            return '<div class="ui-state-default ui-corner-all"><span class="ui-icon ui-icon-folder-open" title="Edit Groups"></span></div>';
        }

        $(document).ready(function () {

            var profileList = jQuery("#profilelist");
            profileList
            .jqGrid({
                url : '<c:url value="/api/profile"/>',
                autowidth : false,
                multiselect: true,
                multiboxonly: true,
                rowList : [], // disable page size dropdown
                pgbuttons : false, // disable page control like next, back button
                pgtext : null,
                cellEdit : true,
                datatype : "json",
                colNames : [ 'ID', 'Profile Name', 'Name'],
                colModel : [ {
                    name : 'id',
                    index : 'id',
                    width : 55,
                    hidden : true,
                    formatter: idFormatter
                }, {
                    // we have this hidden one so the form Add works properly
                    name : 'name',
                    index : 'name',
                    width : 55,
                    editable: true,
                    hidden : true
                }, {
                    name : 'name',
                    index : 'displayProfileName',
                    width : 400,
                    editable : false,
                    formatter: nameFormatter
                }],
                jsonReader : {
                    page : "page",
                    total : "total",
                    records : "records",
                    root : 'profiles',
                    repeatitems : false
                },
                cellurl : '/testproxy/edit/api/server',
                rowList : [],
                pager : '#profilenavGrid',
                sortname : 'id',
                viewrecords : true,
                sortorder : "desc",
                caption : 'Profiles'
            });
            profileList.jqGrid('navGrid', '#profilenavGrid', {
                edit : false,
                add : true,
                del : true
            },
            {},
            {
                jqModal:true,
                url: '<c:url value="/api/profile"/>',
                beforeShowForm: function(form) {
                    $('#tr_name', form).show();
                },
                reloadAfterSubmit: true,
                width: 400
            },
            {
                url: '<c:url value="/api/profile/"/>',
                mtype: 'DELETE',
                reloadAfterSubmit:true,
                onclickSubmit: function(rp_ge, postdata) { /* CODE CHANGED TO ALLOW FOR MULTISELECTION*/
                    /* SPLIT THE DATA INTO EACH THING THAT NEEDS TO BE DELETED.*/
                    var data = postdata.split(",");

                    rp_ge.url = '<c:url value="/api/profile/"/>';

                    /* FOR EVERYTHING THAT NEEDS TO BE DELETED
                        ADD THE CELL DATA INTO THE URL.
                     */
                    for( var i = 0; i < data.length; i++ ) {
                        if( i == data.length - 1 ) {
                            rp_ge.url = rp_ge.url + $('#profilelist').getCell(data[i], 'id');
                        }
                        else
                        {
                            rp_ge.url = rp_ge.url + $('#profilelist').getCell(data[i], 'id') + ",";
                        }
                    }

                  }
            });
            profileList.jqGrid('gridResize');
        });


        function exportConfiguration() {
            downloadFile('<c:url value="/api/backup"/>');
        }

        function importConfiguration() {
            $("#configurationUploadDialog").dialog({
                title: "Upload New Configuration",
                modal: true,
                position:['top',20],
                buttons: {
                  "Submit": function() {
                    // submit form
                    $("#configurationUploadFileButton").click();
                  },
                  "Cancel": function() {
                      $("#configurationUploadDialog").dialog("close");
                  }
                }
            });
        }

        window.onload = function () {
            // Adapted from: http://blog.teamtreehouse.com/uploading-files-ajax
            document.getElementById('configurationUploadForm').onsubmit = function(event) {
                event.preventDefault();

                var file = document.getElementById('configurationUploadFile').files[0];
                var formData = new FormData();
                formData.append('fileData', file, file.name);
                var xhr = new XMLHttpRequest();
                xhr.open('POST', '<c:url value="/api/backup"/>', true);
                xhr.onload = function () {
                  if (xhr.status === 200) {
                    location.reload();
                  } else {
                    $("#statusNotificationStateDiv").removeClass("ui-state-highlight");
                    $("#statusNotificationStateDiv").addClass("ui-state-error");
                    $("#statusNotificationText").html("An error occured while uploading configuration...");

                    // enable form buttons
                    $(":button:contains('Submit')").prop("disabled", false).removeClass("ui-state-disabled");
                    $(":button:contains('Cancel')").prop("disabled", false).removeClass("ui-state-disabled");
                  }
                };

                $("#statusNotificationText").html("Uploading configuration...");
                $("#statusNotificationStateDiv").removeClass("ui-state-error");
                $("#statusNotificationStateDiv").addClass("ui-state-highlight");
                $("#statusNotificationDiv").fadeIn();

                // disable form buttons
                $(":button:contains('Submit')").prop("disabled", true).addClass("ui-state-disabled");
                $(":button:contains('Cancel')").prop("disabled", true).addClass("ui-state-disabled");

                xhr.send(formData);
            }
        }


        </script>
    </head>

    <body>
        <!-- Hidden div for configuration file upload -->
        <div id="configurationUploadDialog" style="display:none;">
            <form id="configurationUploadForm" action="<c:url value="/api/backup"/>" method="POST">
                <input id="configurationUploadFile" type="file" name="fileData" />
                <button id="configurationUploadFileButton" type="submit" style="display: none;"></button>
            </form>

            <!-- div for status notice -->
            <div class="ui-widget" id="statusNotificationDiv" style="display: none;">
                <div class="ui-state-highlight ui-corner-all" id="statusNotificationStateDiv" style="margin-top: 10px;  margin-bottom: 10px; padding: 0 .7em;">
                    <p style="margin-top: 10px; margin-bottom:10px;"><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
                    <span id="statusNotificationText"/>gfdgfd</p>
                </div>
            </div>
        </div>

        <nav class="navbar navbar-default" role="navigation">
            <div class="container-fluid">
                <div class="collapse navbar-collapse">
                    <ul class="nav navbar-nav navbar-left">
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">Options <b class="caret"></b></a>
                            <ul class="dropdown-menu">
                                <li><a href="#" onclick='exportConfiguration()'>Export Configuration</a></li>
                                <li><a href="#" onclick='importConfiguration()'>Import Configuration</a></li>
                            </ul>
                        </li>
                    </ul>
                    <ul class="nav navbar-nav navbar-right">
                        <li>
                            <p class="navbar-text">Odo Version: <c:out value = "${version}"/></p>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

        <div style="width:400px;">
            <table id="profilelist"></table>
            <div id="profilenavGrid"></div>
        </div>
    </body>
</html>