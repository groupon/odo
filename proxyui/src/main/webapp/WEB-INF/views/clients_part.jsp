<script type="text/javascript">
'use strict';
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


function enabledFormatter(cellvalue, options, rowObject) {
    var checkedValue = (cellvalue == true || cellvalue === 'Yes') ? 1 : 0;

    return $("<div>").append($("<input>")
        .attr({
            id: "enabled_" + id,
            onchange: "enabledChanged(" + id + ", '" + uuid + "')",
            type: "checkbox",
            offval: 0,
            value: checkedValue,
            checked: cellvalue,
        }))
        .html();
}

function enabledChanged(id, uuid) {
    var enabled = $("#enabled_" + id).is(":checked");

    $.ajax({
        type: "POST",
        url: '<c:url value="/api/profile/${profile_id}/clients/"/>' + uuid,
        data: "active=" + enabled,
        success: function() {
            $("#clientlist").trigger("reloadGrid");
        },
        error: function() {
            $("#enabled_" + id).prop("checked", enabled);
            window.alert("Error updating client entry.");
        }
    });
}

function manageClientPopup() {
    $("#changeClientDialog").dialog({
        title: "Manage Clients",
        modal: true,
        resizeable: true,
        width: 'auto',
        height: 'auto',
        buttons: {
            Close: function() {
                $("#changeClientDialog").dialog("close");
            }
        }
    });
}

function saveClientRow(id, active) {
    $("#clientlist").jqGrid("setGridParam", {
        ajaxRowOptions: {
            url: '<c:url value="/api/profile/${profile_id}/clients/"/>' + $("#clientlist").jqGrid("getCell", id, "uuid")
        }
    });

    $("#clientlist").jqGrid("saveRow", id, {
        successfunc : null,
        url: '<c:url value="/api/profile/${profile_id}/clients/"/>' + $("#clientlist").jqGrid("getCell", id, "uuid"),
        extraparam: {
            active: active
        },
        aftersavefunc: null,
        errorfunc: null,
        afterrestorefunc: null,
        restoreAfterError: true,
        mtype: "POST"
    });
}

function editClientRow(id) {
    $("#clientlist").jqGrid("setGridParam", {
        ajaxRowOptions: {
            url: '<c:url value="/api/profile/${profile_id}/clients/"/>' + $("#clientlist").jqGrid("getCell", id, "uuid")
        }
    });

    var checked = isChecked($("#clientlist").getCell(id, "isActive"));
    $("#clientlist").jqGrid("editRow", id, true, {
        oneditfunc: null,
        successfunc: null,
        url: '<c:url value="/api/profile/${profile_id}/clients/"/>' + $("#clientlist").jqGrid("getCell", id, "uuid"),
        extraparam: {},
        aftersavefunc: null,
        errorfunc: null,
        afterrestorefunc: null,
        restoreAfterError: true,
        mtype: "POST"
    });

    $("#" + id + "_isActive").prop("checked", checked)
}

function isChecked(cellValue) {
    if (cellValue === true || cellValue === 'Yes') {
        return true;
    }

    if (cellValue != null && cellValue[0] == '<') {
        return cellValue.indexOf('checked="checked"') != -1;
    }
    return false;
}

function changeClientSubmit(id) {
    var active = $("#" + id + "_isActive").is(":checked");

    saveClientRow(id, active);

    $.removeCookie("UUID", {expires: 10000, path: '/testproxy/'});
    var value = $("#clientlist").jqGrid('getCell', id, "friendlyName");
    if (value === "") {
        value = $("#clientlist").jqGrid('getCell', id, 'uuid');
    }
    var url = '<c:url value="/edit/${profile_id}"/>?clientUUID=' + value;
    window.location.href = url;
}

function getCaption() {
    return "Current client UUID: <span style=\"color: blue;\">${clientUUID}</span>";
}

$(document).ready(function() {
    var lastSelected = -2;
    $("#clientlist").jqGrid({
        url: '<c:url value="/api/profile/${profile_id}/clients"/>',
        autowidth: true,
        caption: getCaption(),
        rowList: [], // disable page size dropdown
        pgbuttons: false, // disable page control like next, back button
        pgtext: null,
        multiselect: true,
        multiboxonly: true,
        datatype: "json",
        colNames: ["ID", "UUID", "Friendly Name", "Active", "Last Accessed", "Change Current"],
        colModel : [ {
            name : "id",
            index : "id",
            width : 55,
            hidden : true,
            formatter: idFormatter
        }, {
            name: "uuid",
            path: "uuid",
            editable: false,
            width: "200",
            align: "left",
            formatter: uuidFormatter
        }, {
            name : "friendlyName",
            index : "friendlyName",
            width : 200,
            editable : true
        }, {
            name: "isActive",
            path: "isActive",
            width: 36,
            editable: true,
            align: "center",
            edittype: "checkbox",
            formatter: enabledFormatter,
            formatoptions: {disabled: false}
        }, {
            name: "lastAccessedFormatted",
            path: "lastAccessedFormatted",
            editable: false,
            align: "left"
        }, {
            name: "selectButton",
            width: 100
        }],
        jsonReader : {
            page: "page",
            total: "total",
            records: "records",
            root: "clients",
            repeatitems: false
        },
        onCellSelect: function(id, iCol, cellcontent, e) {
            // if the user has another row currently selected
            if (id != lastSelected) {
                // keep track of whether or not the checkbox for isActive is checked
                // and if there exists a last selected row, save the info edited in that row first
                if (lastSelected != -2) {
                    var active = isChecked($("#clientlist").getCell(lastSelected, 'isActive'));
                    saveClientRow(lastSelected, active);
                }

                // now edit the row that we wish to edit
                editClientRow(id);

                // update that our last selected row is this one
                lastSelected = id;
            }
        },
        afterSaveCell: function() {
            $("#clientlist").trigger("reloadGrid");
        },
        gridComplete: function() {
            $("#clientlist").jqGrid('setGridHeight', 22 * $("#clientlist").jqGrid('getGridParam', 'records'));

            /* ADD A BUTTON TO EACH ROW */
            var rows = $("#clientlist").jqGrid("getDataIDs");
            for(var i = 0; i < rows.length; i++) {
                $("#clientlist").jqGrid('setRowData', rows[i], {
                    selectButton: "<input type='button' style='width:90px' value='Select' onclick='changeClientSubmit(" + rows[i] + ")'/>"
                });
            }
        },
        rowattr: function(rowData, currentObj, rowId) { /* HIGHLIGHTS THE CURRENT CLIENT */
            if( rowData.uuid === clientUUID ) {
                return {class: "selectedRow"};
            }
        },
        ajaxRowOptions: {
            url: '<c:url value="/api/profile/${profile_id}/clients/"/>'
        },
        cellurl: '<c:url value="/api/profile/${profile_id}/clients/"/>',
        rowList: [],
        rowNum: 100000,
        pager: "#clientnavGrid",
        sortname : "id",
        viewrecords : true,
        sortorder : "desc",
    });
    $("#clientlist").jqGrid("navGrid", '#clientnavGrid', {
        edit: false,
        add: true,
        del: true,
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
        mtype: "POST",
        reloadAfterSubmit: true,
        serializeDelData: function(postdata) {
            return ""; // the body MUST be empty in DELETE HTTP requests
        },
        onclickSubmit: function(rp_ge,postdata) {
            /* IDS GIVEN IN AS A STRING SEPARATED BY COMMAS.
             SEPARATE INTO AN ARRAY.
             */
            var rowids = postdata.split(",");

            /* FOR EVERY ROW ID TO BE DELETED,
             GET THE CORRESPONDING PROFILE ID.
             */
            var params = "profileIdentifier=${profile_id}&";
            for (var i = 0; i < rowids.length; i++) {
                var odoId = $(this).jqGrid("getCell", rowids[i], "uuid");
                params += "clientUUID=" + odoId + "&";
            }

            rp_ge.url = '<c:url value="/api/profile/{profileIdentifier}/clients/delete"/>?' + params;
        },
        reloadAfterSubmit: true
    });
    $("#clientlist").jqGrid('gridResize');
});
</script>

<!-- Hidden div for clients -->
<div id="changeClientDialog" style="display: none;">
    <div id="clientContainer">
        <table id="clientlist"></table>

        <div id="clientnavGrid"></div>
    </div>
</div>
