<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
    <title>Configuration Page</title>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <%@ include file="/resources/js/webjars.include" %>

    <script type="text/javascript">
        function pluginTbodyBuilder(data) {
            $('#pluginPathTable tbody').empty();
            if (data.plugins.length == 0) {
                $('#info')
                    .text("There are no valid plugins setup. Please specify one to continue.")
                    .attr("class", "alert-danger")
                    .show();
                $("#pluginPathTable").hide();
                return;
            }

            $('#info').hide();
            $("#pluginPathTable").show();

            for (var i = 0; i < data.plugins.length; i++) {
                var pluginInfo = data.plugins[i];
                var path = pluginInfo.path;
                var id = pluginInfo.id;
                var status = pluginInfo.status;

                var messageIndicator = "ok";
                var messageDisposition = "success";
                if (status == 1) {
                    messageIndicator = "remove";
                    messageDisposition = "danger";
                }
                messageIndicator = "<span class=\"glyphicon glyphicon-" + messageIndicator + "-sign text-" + messageDisposition + "\"></span> ";
                var message = messageIndicator + pluginInfo.statusMessage;

                var $button = $("<input>")
                    .attr({
                        type: "button",
                        class: "btn btn-default"
                    })
                    .val("Remove Path")
                    .click(function() {
                        removeRow(id);
                    });

                $('#pluginPathTable tbody').append($("<tr>")
                    .append($("<td>").text(id))
                    .append($("<td>").append($("<code>").text(path)))
                    .append($("<td>").html(message))
                    .append($("<td>").append($button)));
            }
        }

        function removeRow(profile_id) {
            if (confirm("Are you sure you want to delete this?")) {
                $.ajax({
                    type: "DELETE",
                    url: 'api/plugins/' + profile_id + '/?requestFromConfiguration=true',
                    success: function(data) {
                        pluginTbodyBuilder(data);
                        $('#info')
                            .text("Removed!")
                            .attr("class", "alert alert-success")
                            .show();
                    }
                });
            }
        }

        $(document).ready(function() {
            $.ajax({
                type: "GET",
                url: 'api/plugins?requestFromConfiguration=true',
                beforeSend: function() {
                    $('#info')
                        .text("Getting data!")
                        .attr("class", "alert alert-info")
                        .show();
                },
                success: function(data) { // array<array<object>>
                    pluginTbodyBuilder(data);
                },
                error: function() {
                    $('#info')
                        .text("Whoops!")
                        .attr("class", "alert alert-danger")
                        .show();
                }
            });
        });
    </script>
</head>
<body>
    <nav class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <a class="navbar-brand" href="#">Odo</a>
            </div>

            <ul class="nav navbar-nav navbar-left">
                <li><a href="<c:url value = '/profiles' />" target="_BLANK">Profiles</a></li>
            </ul>
        </div>
    </nav>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h1 class="panel-title">Plugin Paths</h1>
                    </div>
                    <div class="panel-body">
                        <div id="info" class="alert alert-warning" role="alert" style="display: none;"></div>

                        <table id="pluginPathTable" class="table">
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
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
