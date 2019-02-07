<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
    <title>Create/Edit Override Group </title>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <%@ include file="/resources/js/webjars.include" %>

    <script type="text/javascript">
        var selectedGroupId = "";

        // common function for grid reload
        function reloadGrid(gridId) {
            $(gridId).setGridParam({datatype:'json', page: 1}).trigger("reloadGrid");
        }

        var enabledMethods = {};
        var selectingRows = false;
        function selectRows(groupId) {
            $.ajax({
                type: "GET",
                url: '<c:url value="/api/group/"/>' + groupId,
                success: function(data){
                    selectingRows = true;
                    enabledMethods = {};
                    $.each(data.methods, function(index, value) {
                        enabledMethods[value.idString] = value.id;
                    });

                    var grid = $("#overrideList");
                    grid.resetSelection();

                    var ids = grid.getDataIDs();
                    for (var i = 0, il = ids.length; i < il; i++) {
                        if(enabledMethods[ids[i]] != null) {
                            grid.setSelection(ids[i], true);
                        }
                    }
                    selectingRows = false;
                }
            });
        }

        $(document).ready(function() {
            // bind window resize to fix grid width
            $(window).bind('resize', function() {
                $("#groupsList").setGridWidth($("#groupsDiv").width());
                $("#overrideList").setGridWidth($("#groupOverridesDiv").width());
            });

            $("#groupsList").jqGrid({
                url: '<c:url value="/api/group"/>',
                autowidth: true,
                pgbuttons: false, // disable page control like next, back button
                pgtext: null,
                datatype: "json",
                colNames: [ 'ID', 'Group Name'],
                colModel: [ {
                    name: 'id',
                    index: 'id',
                    width: 55,
                    hidden: true
                }, {
                    name: 'name',
                    path: 'name',
                    width: 200,
                    editable: true,
                    align: 'left'
                }],
                jsonReader: {
                    page: "page",
                    total: "total",
                    records: "records",
                    root: 'groups',
                    repeatitems: false
                },
                loadonce: true,
                cellurl: '/testproxy/api/group',
                gridComplete: function() {
                    if ($("#groupsList").length > 0){
                        $("#groupsList").setSelection($("#groupsList").getDataIDs()[0], true);
                    }
                },
                onSelectRow: function(id) {
                    if (id) {
                        var ret = $("#groupsList").jqGrid('getRowData',id);
                        var groupName = ret.name;
                        $("#overrideList").jqGrid("setCaption", ret.name + " Methods");
                        selectedGroupId = id;
                    }

                    selectRows(id);
                },
                editurl: '<c:url value="/api/group/" />',
                rowList: [],
                rowNum: 10000,
                pager: '#groupNavGrid',
                sortname: 'name',
                viewrecords: true,
                sortorder: "asc",
                caption: 'Groups'
            });

            $("#groupsList").jqGrid('navGrid', '#groupNavGrid',
                { add: true, edit: false, del: true, search: false },
                {},
                {
                    url: '<c:url value="/api/group"/>',
                    reloadAfterSubmit: true,
                    closeAfterAdd: true,
                    afterSubmit: function () {
                        reloadGrid("#groupsList");
                        return [true];
                    }
                 },
                {
                    mtype: 'DELETE',
                    reloadAfterSubmit: false,
                    afterSubmit: function() {
                        reloadGrid("#groupsList");
                        return [true];
                    },
                    onclickSubmit: function(rp_ge, postdata) {
                        rp_ge.url = '<c:url value="/api/group/" />' + selectedGroupId;
                    }
                },
                {
                }
            );
            $("#groupsList").jqGrid('gridResize');

            $("#overrideList").jqGrid({
                url: '<c:url value="/api/methods"/>',
                autowidth: true,
                pgbuttons: false, // disable page control like next, back button
                pgtext: null,
                datatype: "json",
                colNames: ['ID', 'Class', 'Method'],
                colModel: [
                {
                    name: 'idString',
                    index: 'id',
                    key: true,
                    width: 400,
                    hidden: true
                }, {
                    name: 'className',
                    index: 'className',
                    width: 300,
                    editable: false
                }, {
                    name: 'methodName',
                    index: 'methodName',
                    width: 300,
                    editable: false
                }],
                jsonReader: {
                    page: "page",
                    total: "total",
                    records: "records",
                    root: 'methods',
                    repeatitems: false
                },
                height: "auto",
                loadonce: true,
                multiselect: true,
                rowNum: 10000,
                sortname: 'idString',
                sortorder: 'asc',
                viewrecords: true,
                onSelectRow: function(id, status){
                    if (selectedGroupId === "" || selectingRows)
                        return;

                    if (status) {
                        $.ajax({
                            type:"POST",
                            url:"<c:url value='/api/group/'/>" + selectedGroupId,
                            data: ({'addOverride': id})
                        });
                    } else {
                        $.ajax({
                            type:"POST",
                            url:"<c:url value='/api/group/'/>" + selectedGroupId,
                            data: ({'removeOverride': id})
                        });
                    }
                 }

            });
            $("#overrideList").jqGrid('navGrid', '#overrideNavGrid',
                { add: false, edit: false, del: false, search: false },
                {},
                {},
                {},
                {}
            );
            $("#overrideList").jqGrid('gridResize');
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
                <li><a href="<c:url value='/profiles' />" target="_BLANK">Profiles</a></li>
            </ul>
        </div>
    </nav>

    <div class="container-fluid">
        <div class="row">
            <div id="groupsDiv" class="col-xs-4">
                <table id="groupsList"></table>
                <div id="groupNavGrid"></div>
            </div>

            <div id="groupOverridesDiv" class="col-xs-8">
                <div>
                    <table id="overrideList"></table>
                    <div id="overrideNavGrid"></div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
