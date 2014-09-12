<style type="text/css">
.paddedtable td {
	padding: 10px
}
</style>
<script type="text/javascript">
function pathTesterSubmit() {
	var url = $('#pathTesterURL').val();
	var encoded = encodeURIComponent(url);
	
	$.ajax({
        type:"GET",
        url: '<c:url value="/api/path/test"/>',
        data: 'profileIdentifier=${profile_id}&url=' + encoded,
        success: function(data) {
            // build up grid
            // $("#friendlyNameError").html(json.error.message);
            var grid = "<table id=\"pathTesterTable\" class=\"paddedtable\"><tr><td class=\"ui-widget-header\">#</td>";
            grid = grid + "<td class=\"ui-widget-header\">Path Name</td>";
            grid = grid + "<td class=\"ui-widget-header\">Path</td>";
            grid = grid + "<td class=\"ui-widget-header\">Global</td></tr>";
            data = $.parseJSON(data);
            var x = 1;
            jQuery.each(data.paths, function(index, value) {
            	grid = grid + "<tr>";
                grid = grid + "<td class=\"ui-widget-content\">" + x + "</td>";
                grid = grid + "<td class=\"ui-widget-content\">" + value.pathName + "</td>"
                grid = grid + "<td class=\"ui-widget-content\">" + value.path + "</td>"
                grid = grid + "<td class=\"ui-widget-content\">" + value.global + "</td>"
                grid = grid + "</tr>"
            	x = x + 1;
            });
            
            grid = grid + "</table>"
            
            $("#pathTesterResults").html(grid);
        },
        error: function(xhr) {
        }
    });
}

function navigatePathTester() {
	$("#pathTesterDialog").dialog({
        title: "Path Tester",
        width: 650,
        modal: true,
        position:['top',20],
        buttons: {
          "Close": function() {
              $("#pathTesterDialog").dialog("close");
          }
        }
    });
}
</script>

<!-- Hidden div for path tester -->
<div id="pathTesterDialog" style="display:none;">
    URL to Test: <input id="pathTesterURL" size=45/>
    <button class="btn btn-primary" onclick="pathTesterSubmit()">Test</button>
    <div class="ui-widget">
		<div class="ui-state-highlight ui-corner-all">
			<p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
			<strong>NOTE:</strong> POST body filters are not taken into account during this test.</p>
		</div>
	</div>
    <div id="pathTesterResults"></div>
</div>