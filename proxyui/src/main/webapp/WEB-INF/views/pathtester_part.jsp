<script type="text/javascript">
'use strict';
$(document).ready(function() {
    $("#pathTesterDialog form").on("submit", function(e) {
        e.preventDefault();

        var url = $('#pathTesterURL').val();
        var requestType = $('#pathTesterRequestType ~ .dropdown-menu li[data-active=1] a').attr("data-value");
        var encoded = encodeURIComponent(url);

        $.ajax({
            type:"GET",
            url: '<c:url value="/api/path/test"/>',
            data: 'profileIdentifier=${profile_id}&requestType=' + requestType + '&url=' + encoded,
            success: function(data) {
                $("#pathTesterResults").empty();
                data = $.parseJSON(data);

                if (data.paths.length === 0) {
                    $("#pathTesterResults").text("No matching path found.");
                    return;
                }

                var $pathTable = $("<table>")
                    .addClass("table table-striped table-bordered table-hover")
                    .attr("id", "pathTesterTable")
                        .append($("<thead>")
                            .append($("<tr>")
                                .append($("<th>").text("#"))
                                .append($("<th>").text("Path Name"))
                                .append($("<th>").text("Path"))
                                .append($("<th>").text("Global?"))));

                var $pathTableBody = $("<tbody>");
                jQuery.each(data.paths, function(index, value) {
                    $pathTableBody.append($("<tr>")
                        .append($("<td>").text(index + 1))
                        .append($("<td>").text(value.pathName))
                        .append($("<td>").addClass("preformatted").text(value.path))
                        .append($("<td>").text(value.global ? "Yes" : "No")));
                });

                $pathTable.append($pathTableBody);

                $("#pathTesterResults").append($pathTable);
                $("#pathTesterAlert").alert("close");
            },
            error: function(xhr) {
                $("#pathTesterResults").empty().text("An error occurred.");
                $("#pathTesterAlert").alert("close");
            }
        });
    });

    $("#pathTesterRequestType ~ .dropdown-menu a").click(function(event) {
        event.preventDefault();
        let $target = $(event.target);
        $target.closest("ul").children("li").attr("data-active", "0");
        $target.closest("li").attr("data-active", "1");

        $("#pathTesterRequestType span.requestType").text($target.text());
    });
});

function navigatePathTester(onOpen) {
    $("#pathTesterDialog").dialog({
        title: "Path Tester",
        width: 750,
        minHeight: 320,
        modal: true,
        buttons: {
            "Close": function() {
                $("#pathTesterDialog").dialog("close");
            }
        },
        open: onOpen
    });
}

function pathTesterSubmit() {
    $("#pathTesterDialog form").submit();
}
</script>

<!-- Hidden div for path tester -->
<div id="pathTesterDialog" style="display:none;">
    <form style="margin-bottom: 1em;">
        <div class="input-group">
            <div class="input-group-btn">
                <button id="pathTesterRequestType" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <span class="requestType">GET</span> <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                    <li data-active="0"><a href="#" data-value="0">ALL</a></li>
                    <li data-active="1"><a href="#" data-value="1">GET</a></li>
                    <li data-active="0"><a href="#" data-value="2">PUT</a></li>
                    <li data-active="0"><a href="#" data-value="3">POST</a></li>
                    <li data-active="0"><a href="#" data-value="4">DELETE</a></li>
                </ul>
            </div><!-- /btn-group -->
            <input id="pathTesterURL" autofocus required type="text" class="form-control" />
            <div class="input-group-btn">
                <button class="btn btn-primary">Test</button>
            </div>
        </div><!-- /input-group -->
    </form>

    <div id="pathTesterAlert" class="alert alert-warning alert-dismissible" role="alert">
        <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <strong>NOTE:</strong> POST body filters are not taken into account during this test.
    </div>

    <div id="pathTesterResults"></div>
</div><!-- /#pathTesterDialog -->
