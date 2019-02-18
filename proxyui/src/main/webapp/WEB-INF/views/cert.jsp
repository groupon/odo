<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
    <title>Certificates</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <%@ include file="/resources/js/webjars.include" %>
</head>

<body>
    <nav class="navbar navbar-default" role="navigation">
        <div id="statusBar" class="container-fluid">
            <div class="navbar-header">
                <a class="navbar-brand" href="#">Odo</a>
            </div>

            <ul class="nav navbar-nav navbar-left">
                <%@ include file="navigation_part.jsp" %>
            </ul>
        </div>
    </nav><!-- /.navbar -->

    <form action="<c:url value="/cert/root"/>">
        <button type="submit" class="btn btn-default">Download Root Certificate</button>
    </form>
</body>
</html>
