<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ page session="false"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<%@ include file="/resources/js/webjars.include" %>
	
<style type="text/css">

</style>

<title>Edit Clients for Profile: ${profile_name}</title>
</head>
<body>
	<nav class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="collapse navbar-collapse">
                <ul id="status2" class="nav navbar-nav">
                    <li><a href="#" onClick="window.location='<c:url value = '/profiles' />'">All Profiles</a></li>
                    <li><a href="#" onClick="navigateProfile()">${profile_name}</a></li>
                </ul>
            </div>
        </div>
    </nav>
	<div id="clientContainer"">
        <table id="clientlist"></table>
        <div id="clientnavGrid"></div>
	</div>
<script>

$(document).ready(function() {
    // bind window resize to fix grid width
    $(window).bind('resize', function() {
        $("#clientlist").setGridWidth($("#clientContainer").width());
    });
});

function navigateProfile() {
    window.location = '<c:url value = '/edit/${profile_id}' />';
}

var id = -1;
function idFormatter( cellvalue, options, rowObject ) {
	id = cellvalue;
	return cellvalue;
}

var uuid;
function uuidFormatter( cellvalue, options, rowObject ) {
	uuid = cellvalue;
	return cellvalue;
}

//formats the enable/disable check box
function enabledFormatter( cellvalue, options, rowObject ) {
	var checkedValue = 0;
	if (cellvalue == true) {
		checkedValue = 1;
	}
	
	var newCellValue = '<input id="enabled_' + id + '" onChange="enabledChanged(' + id + ', \'' + uuid + '\')" type="checkbox" offval="0" value="' + checkedValue + '"';
	
	if (checkedValue == 1) {
		newCellValue += 'checked="checked"';
	}
	
	newCellValue += '>';
	
	return newCellValue;
}

//called when an enabled checkbox is changed
function enabledChanged(id, uuid) {
	var enabled = $("#enabled_" + id).is(":checked");

	$.ajax({
   		type:"POST",
    	url: '<c:url value="/api/profile/${profile_id}/clients/"/>' + uuid,
    	data: "active=" + enabled,
    	success: function() {
   			jQuery("#clientlist").trigger("reloadGrid");
   		},
   		error: function(xhr) {
   			document.getElementById("enabled_" + id).checked = origEnabled;
   			alert("Error updating client entry.");
   		}
   	});
}

var clientList = jQuery("#clientlist");
clientList
.jqGrid({
	url : '<c:url value="/api/profile/${profile_id}/clients"/>',
	autowidth : true,
	rowList : [], // disable page size dropdown
	pgbuttons : false, // disable page control like next, back button
	pgtext : null,
	cellEdit : true,
	datatype : "json",
	colNames : [ 'ID', 'UUID',
			'Friendly Name', 'Active', 'Last Accessed' ],
	colModel : [ {
		name : 'id',
		index : 'id',
		width : 55,
		hidden : true,
		formatter: idFormatter
	}, {
		name: 'uuid',
		path: 'uuid',
		editable: false,
		align: 'left',
		formatter: uuidFormatter
	}, {
		name : 'friendlyName',
		index : 'friendlyName',
		width : 100,
		editable : true
	}, {
		name: 'isActive',
		path: 'isActive',
		width: 18,
		editable: true,
		align: 'center',
		edittype: 'checkbox',
		formatter: enabledFormatter, 
		formatoptions: {disabled : false}
	}, {
		name: 'lastAccessedFormatted',
		path: 'lastAccessedFormatted',
		editable: false,
		align: 'left'
	}
	],
	jsonReader : {
		page : "page",
		total : "total",
		records : "records",
		root : 'clients',
		repeatitems : false
	},
	afterEditCell : function(rowid, cellname, value, iRow, iCol) {
		var uuid = clientList.getCell(rowid, 'uuid');
		console.log(rowid);
		if (cellname == "friendlyName") {
			clientList.setGridParam({
				cellurl : '<c:url value="/api/profile/${profile_id}/clients/"/>' + uuid
			});
		}
	},
	afterSaveCell : function() {
		clientList.trigger("reloadGrid");
	},
	gridComplete: function() {
		clientList.jqGrid('setGridHeight', 22 * clientList.jqGrid('getGridParam', 'records'));
	},
	cellurl : '<c:url value="/api/profile/${profile_id}/clients/"/>',
	rowList : [],
	rowNum: 100000,
	pager : '#clientnavGrid',
	sortname : 'id',
	viewrecords : true,
	sortorder : "desc",
	caption : '<font size="5">Clients: ${profile_name}</font>'
});
clientList.jqGrid('navGrid', '#clientnavGrid', {
	edit : false,
	add : true,
	del : true
},
{},
{
	url: '<c:url value="/api/profile/${profile_id}/clients/"/>',
	reloadAfterSubmit: true,
	beforeShowForm: function(form) { 
		$('#tr_isActive', form).hide();
	},
	width: 450,
	closeAfterAdd: true
},
{
	mtype:"DELETE", reloadAfterSubmit:true, serializeDelData: function (postdata) {
		return ""; // the body MUST be empty in DELETE HTTP requests
	}, 
	onclickSubmit: function(rp_ge,postdata) {
		var uuid = jQuery('#clientlist').getCell(postdata, 'uuid');
	    rp_ge.url = '<c:url value="/api/profile/${profile_id}/clients/"/>' +
	    			uuid;
	},
	reloadAfterSubmit: true
});
</script>
</body>
</html>