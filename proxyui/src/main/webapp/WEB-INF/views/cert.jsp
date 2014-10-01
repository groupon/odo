<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page session="false" %>
<html>
    <head>
        <%@ include file="/resources/js/webjars.include" %>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Certificates</title>
    </head>

    <body>
        <form action="<c:url value="/cert/root"/>">
            <button type="submit" class="btn btn-default">Download Root Certificate</button>
        </form>
    </body>
</html>