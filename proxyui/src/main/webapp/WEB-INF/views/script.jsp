<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8" isELIgnored="false"%>
<%@ page session="false"%>
<!DOCTYPE html>
<html>
<head>
    <title>Scripts</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <%@ include file="/resources/js/webjars.include" %>

    <style type="text/css">
        #editScript {
            font-family: monospace;
        }
    </style>
</head>
<body>

    <nav class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <a class="navbar-brand" href="#">Odo</a>
            </div>

            <ul id="status2" class="nav navbar-nav navbar-left">
                <li><a target="_BLANK" href="<c:url value='/profiles' />">All Profiles</a></li>
            </div>
        </div>
    </nav>

    <!-- Hidden div for editing -->
    <div id="editDialog">
    </div>

    <table id="scriptlist"></table>
    <div id="scriptnavGrid"></div>

    <script type="text/javascript">
        'use strict';
        function scriptDivVal(origstr) {
            if (!origstr) return "Click here to edit";
            var lines = origstr.split(/\n/);

            if (lines.length <= 4) { return origstr; }

            lines[3] = lines[3].trim();
            return lines.splice(0, 4).join("\n") + "\u2026";
        }

        // from http://stackoverflow.com/questions/24816/escaping-html-strings-with-jquery
        var __entityMap = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': '&quot;',
            "'": '&#39;',
            "/": '&#x2F;'
        };

        function customResponseFormatter( cellvalue, options, rowObject ) {
            var shortenedCell = scriptDivVal(cellvalue);
            var base_id = "script_" + currentScriptId;

            return '<div id="' + base_id + '" onClick="customClick(' + currentScriptId + ')">'
                + shortenedCell.escapeHTML()
                + '</div>';
        }

        function customClick(scriptId) {
            $.ajax({
                type:"GET",
                url: '<c:url value="/api/scripts/"/>' + scriptId,
                success: function(data){
                    var content = '<div><textarea id="editScript" ROWS=10 COLS=70>' + data.script + '</textarea></div>';

                    $("#editDialog").html(content);
                    $("#editDialog").dialog({
                        title: "Edit Script: " + data.name,
                        modal: true,
                        width: 'auto',
                        buttons: {
                          "Submit": function() {
                              customResponseChanged(scriptId);
                          },
                          "Cancel": function() {
                              $("#editDialog").dialog("close");
                          }
                        }
                    });
                }
            });

            return false;
        }

        function customResponseChanged(scriptId) {
            var newVal = $("#editScript").val();
            var data = "script=";

            data += encodeURIComponent(newVal);

            $.ajax({
                type:"POST",
                url: '<c:url value="/api/scripts/"/>' + scriptId,
                data: data,
                success: function(){
                    $("#scriptlist").trigger("reloadGrid");
                    $("#editDialog").dialog("close");
                }
            });
        }

        var currentScriptId = -1;

        // this just sets the current path ID so that other formatters can use it
        function idFormatter( cellvalue, options, rowObject ) {
            currentScriptId = cellvalue;
            return cellvalue;
        }

        $(document).ready(function() {
            String.prototype.escapeHTML = function() {
                return String(this).replace(/[&<>"'\/]/g, function (s) {
                    return __entityMap[s];
                });
            };
            $("#scriptlist").jqGrid({
                url : '<c:url value="/api/scripts?type=0"/>',
                height: "auto",
                autowidth : true,
                pgbuttons : true, // disable page control like next, back button
                pgtext : null,
                cellEdit : true,
                shrinkToFit : true,
                datatype : "json",
                colNames : [ 'ID', 'Name', 'Script', 'Script' ],
                colModel : [ {
                    name : 'id',
                    index : 'id',
                    hidden : true,
                    formatter: idFormatter
                }, {
                    name : 'name',
                    index : 'name',
                    width : 200,
                    editable : true,
                    align: 'center'
                }, {
                    name : 'script',
                    index : 'displayScript',
                    width : 500,
                    editable : false,
                    align: 'left',
                    formatter: customResponseFormatter,
                    cellattr: function() {
                        return 'style="font-family: monospace;"'
                    }
                }, {
                    name : 'script',
                    index : 'displayScript',
                    width : 500,
                    editable : true,
                    hidden: true,
                    type: 'textarea'
                }],
                jsonReader : {
                    page : "page",
                    total : "total",
                    records : "records",
                    root : 'scripts',
                    repeatitems : false
                },
                resizeStop: function () {
                    $(this.bDiv).find('>div:first>table.ui-jqgrid-btable:first')
                                .jqGrid('setGridWidth', this.newWidth);
                },
                afterEditCell : function(rowid, cellname, value, iRow, iCol) {
                    var id = $("#scriptlist").getCell(rowid, 'id');
                    if (cellname == "name") {
                        $("#scriptlist").setGridParam({
                            cellurl : '<c:url value="/api/scripts/"/>' + id
                        });
                    }
                },
                cellurl : '<c:url value="/api/scripts"/>',
                rowList : [],
                rownum: 10000,
                pager : '#scriptnavGrid',
                sortname : 'id',
                viewrecords : true,
                sortorder : "desc",
                caption : '<h2>Scripts</h2>'
            });
            $("#scriptlist").jqGrid('navGrid', '#scriptnavGrid', {
                edit : false,
                add : true,
                del : true
            },
            {},
            {
                url: '<c:url value="/api/scripts"/>',
                reloadAfterSubmit: true,
                width: 400,
                beforeShowForm: function(form) {
                    $('#tr_name', form).show();
                    $('#tr_script', form).show();
                }
            },
            {
                url: '<c:url value="/api/scripts/"/>',
                mtype: 'DELETE',
                reloadAfterSubmit:true,
                onclickSubmit: function(rp_ge, postdata) {
                      rp_ge.url = '<c:url value="/api/scripts/"/>' +
                                  $('#scriptlist').getCell (postdata, 'id');
                  }
            });
        });
    </script>
</body>
</html>
