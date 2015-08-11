<style type="text/css">
    .ui-widget-content .selectedRow {
        color: blue;
    }
</style>
<script>
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

function manageClientPopup() {
    $("#changeClientDialog").dialog({
        title: "Manage Clients",
        modal: true,
        resizeable: true,
        width:'auto',
        height:'auto',
        close: function() {
            $("#switchClientName").val("");
        },
        buttons: {
            "Close": function () {
                $("#changeClientDialog").dialog("close");
            }
        }
    });
}

function changeClientSubmit(id) {
    $.removeCookie("UUID", { expires: 10000, path: '/testproxy/' });
    var value = $("#clientlist").jqGrid('getCell', id, "friendlyName");
    if( value === "" ) {
        value = $("#clientlist").jqGrid('getCell', id, 'uuid');
    }
    var url = '<c:url value="/edit/${profile_id}"/>?clientUUID=' + value;
    window.location.href = url;
}

$(document).ready(function () {
    var clientList = jQuery("#clientlist");
    clientList
    .jqGrid({
        url : '<c:url value="/api/profile/${profile_id}/clients"/>',
        autowidth : true,
        rowList : [], // disable page size dropdown
        pgbuttons : false, // disable page control like next, back button
        pgtext : null,
        multiselect:true,
        multiboxonly:true,
        cellEdit : true,
        datatype : "json",
        colNames : [ 'ID', 'UUID',
            'Friendly Name', 'Active', 'Last Accessed', 'Change Current' ],
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
            width: '300',
            align: 'left',
            formatter: uuidFormatter
        }, {
            name : 'friendlyName',
            index : 'friendlyName',
            width : 200,
            editable : true
        }, {
            name: 'isActive',
            path: 'isActive',
            width: 36,
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
        }, {
            name: 'selectButton',
            width: 100
        }],
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

            /* ADD A BUTTON TO EACH ROW */
            var rows = clientList.jqGrid("getDataIDs");
            for(var i = 0; i < rows.length; i++) {
                clientList.jqGrid('setRowData', rows[i], {selectButton: "<input type='button' style='width:90px' value='Select' onclick='changeClientSubmit("+rows[i]+")'/>"});
            }
        },
        rowattr: function(rowData, currentObj, rowId) { /* HIGHLIGHTS THE CURRENT CLIENT */
            if( rowData.uuid === clientUUID ) {
                return { "class": "selectedRow" };
            }
        },
        cellurl : '<c:url value="/api/profile/${profile_id}/clients/"/>',
        rowList : [],
        rowNum: 100000,
        pager : '#clientnavGrid',
        sortname : 'id',
        viewrecords : true,
        sortorder : "desc",
        //caption : '<font size="5">Clients: ${profile_name}</font>'
    });
    clientList.jqGrid('navGrid', '#clientnavGrid', {
        edit : false,
        add : true,
        del : true,
        addtext: "Add a new client",
        deltext: "Delete a client"
    },
    {},
    {
        url: '<c:url value="/api/profile/${profile_id}/clients/"/>',
        reloadAfterSubmit: true,
        beforeShowForm: function(form) {
            $('#tr_isActive', form).hide();
        },
        width: 450,
        closeAfterAdd: true,
        errorTextFormat: jsonErrorFormat
    },
    {
        url: '<c:url value="/api/profile/{profileIdentifier}/clients/delete"/>',
        mtype:"POST", reloadAfterSubmit:true, serializeDelData: function (postdata) {
        return ""; // the body MUST be empty in DELETE HTTP requests
    },
        onclickSubmit: function(rp_ge,postdata) {
            /* IDS GIVEN IN AS A STRING SEPARATED BY COMMAS.
             SEPARATE INTO AN ARRAY.
             */
            var rowids = postdata.split(",");
            console.log(postdata);

            /* FOR EVERY ROW ID TO BE DELETED,
             GET THE CORRESPONDING PROFILE ID.
             */
            var params = "profileIdentifier=${profile_id}&";
            for( var i = 0; i < rowids.length; i++) {
                var odoId = $(this).jqGrid('getCell', rowids[i], 'uuid');
                params += "clientUUID=" + odoId + "&";
            }

            rp_ge.url = '<c:url value="/api/profile/{profileIdentifier}/clients/delete"/>?' +
                    params;
            console.log(rp_ge.url);
        },
        reloadAfterSubmit: true
    });
    clientList.jqGrid('gridResize');
});
</script>

<!-- Hidden div for clients -->
<div id="changeClientDialog" style="display:none">
    <div id="clientContainer">
        <b>Current client UUID: <font color="blue">${clientUUID}</font></b><br>
        <table id="clientlist" style="width:100%"></table><br>
        <div id="clientnavGrid"></div>
    </div>
</div>