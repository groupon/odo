<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<!-- Used a lot of code from: http://jqueryui.com/demos/sortable/default.html -->
<head>
    <title>Update Path Order</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <%@ include file="/resources/js/webjars.include" %>

    <style type="text/css">
        #sortable {
            list-style-type: none;
            padding: 0;
            margin: 0;
        }

        #sortable li {
            font-size: 1em;
            padding: .5em;
            margin-bottom: .5em;
        }

        .ui-state-highlight {
            height: 2.5em;
        }
    </style>

    <script type="text/javascript">
        $(document).ready(function() {
            $("#sortable").sortable({
                axis: "y",
                cursor: "move",
                revert: 100,
                update: function(event, ui) {
                    var pathOrder = $('#sortable').sortable('toArray').toString();
                    $.ajax({
                        type: "POST",
                        url: "",
                        data: {pathOrder : pathOrder},
                        success: function() {
                            $('#info')
                                .text('Path Order Updated')
                                .fadeOut(1)
                                .delay(50)
                                .fadeIn(150);
                        }
                    });
                },
                placeholder: "ui-state-highlight"
            });
        });
    </script>
</head>
<body>
    <%@ include file="pathtester_part.jsp" %>

    <nav class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <a class="navbar-brand" href="#">Odo</a>
            </div>

            <ul id="status2" class="nav navbar-nav navbar-left">
                <li><a href="<c:url value='/profiles' />">All Profiles</a></li>
                <li><a href="<c:url value='/edit/${profile_id}' />">${profile_name}</a></li>
                <li><a href="#" onClick="navigatePathTester();">Path Tester</a></li>
            </ul>
        </div>
    </nav>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Drag items to set priority</h3>
                    </div>
                    <div class="panel-body">
                        <div class="alert alert-warning alert-dismissible" role="alert">
                            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                            The ordering of paths impacts how requests are handled.
                            In general if a higher priority path matches a request then further paths will not be evaluated.
                            The only exception is Global paths. In the case that a global path is matched the matcher will continue to search for a non-global matching path.
                        </div>

                        <ul id="sortable">
                            <c:forEach var="pathname" items="${pathnames}">
                                <li class="ui-state-default ui-corner-all reorderbox" id='${pathname.pathId}'>
                                    <span class="ui-icon ui-icon-arrowthick-2-n-s"></span>
                                    <strong>${pathname.pathName}</strong>&nbsp;&nbsp;&nbsp;<code>${pathname.path}</code>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
