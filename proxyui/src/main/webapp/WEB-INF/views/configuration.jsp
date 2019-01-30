<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Configuration Page</title>

        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <link href="<c:url value="/resources/css/profiles.css" />" rel="stylesheet" type="text/css" media="screen" />
        <%@ include file="/resources/js/webjars.include" %>

        <script type="text/javascript">

        function doAjaxPost() {
            var name = $('#path').val();
            if(name != ""){
              $.ajax({
                type: "POST",
                url: 'api/plugins?requestFromConfiguration=true',
                data: ({path : name}),
                success: function(data) { //this data is what is coming back from the server
                  $('#path').val('');
                  pluginTbodyBuilder(data);
                  $('#info').html("Added!");
                }
              });
            } else {
                alert("You can't create a profile without a name");
            }
        }

        //builds the table with nodata, goes and gets the data first and passes it into the other fct
        function pluginTbodyBuilderNodata() {
            $('#info').html("Getting data!");
            $.ajax({
                type: "GET",
                url: 'api/plugins?requestFromConfiguration=true',
                success: function(data) { //this is the data that comes back from the server (the array<array<object>>)
                    pluginTbodyBuilder(data); //now i pass this data into the tbodybuilder
                },
                error:function (xhr, ajaxOptions, thrownError){
                    $('#info').html("Whoops!");
                }
              });
        }

        function rowBuilder(pluginInfo) {
            var name = pluginInfo.path;
            var id = pluginInfo.id;
            var message = pluginInfo.statusMessage;
            var status = pluginInfo.status;

            if (status == 1) {
                message = '<font color=\"red\">' + message + '</font>';
            }

            return '<tr>'
                  + '<td>' + id + '</td>'
                  + '<td>' + name + '</td>'
                  + '<td>' + message + '</td>'
                  //the remove button
                  + '<td><input type="button" onclick="removeRow(' + id + ')" value="Remove Path" /></td>'
            + '</tr>';
        }

        //builds the table by appending all of the rows
        function pluginTbodyBuilder(data) {
            $('#pluginPathTable tbody').html('');
            for(var i = 0; i < data.plugins.length; i++)
                $('#pluginPathTable tbody').append(rowBuilder(data.plugins[i]));

            if (data.plugins.length == 0) {
                $('#info').html("There are no valid plugins setup.  Please specify one to continue.");
                $('#info').css('color', 'red');
            }
            else {
                $('#info').html("");
            }
        }

        function removeRow(profile_id) {
            if(confirm("Are you sure you want to delete this?")){
                $.ajax({
                type: "DELETE",
                url: 'api/plugins/' + profile_id + '/?requestFromConfiguration=true',
                success: function(data) {
                    pluginTbodyBuilder(data);
                    $('#info').html("Removed!");
                }
              });
            }
        }

        function navigateProfiles() {
            window.location = '<c:url value = '/profiles' />';
        }

        function noenter() {
              return !(window.event && window.event.keyCode == 13); }

        //builds the table when the page loads
        window.onload=function() {
            pluginTbodyBuilderNodata();
        };

        </script>
    </head>
    <body>
        <nav class="navbar navbar-default" role="navigation">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="#">Odo</a>
                </div>

                <ul id="status2" class="nav navbar-nav navbar-left">
                    <li><a href="#" onClick="navigateProfiles()">Profiles</a></li>
                </ul>
            </div>
        </nav>

        <div class="ui-widget-content ui-corner-all" style="width: 90%;">
            <h1>Plugin Paths</h1>
            <div id="info" style="color: green;margin: 50px;">

            </div>
            <table id="pluginPathTable" class="normal-table" style="width:95%;">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Path</th>
                        <th>Status</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>

                </tbody>
            </table>
            <br>
        </div>
    </body>
</html>
