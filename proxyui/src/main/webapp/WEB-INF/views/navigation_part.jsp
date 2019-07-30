<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<li class="dropdown" id="global-nav">
    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
        <span class="glyphicon glyphicon-globe"></span>
        <strong>Navigation</strong>
        <kbd>N</kbd>
        <span class="caret"></span>
    </a>
    <ul class="dropdown-menu">
        <li>
            <a href="<c:url value='/profiles'/>" target="_BLANK">
                <span class="glyphicon glyphicon-book"></span>&nbsp;&nbsp;All Profiles
            </a>
        </li>
        <li role="separator" class="divider"></li>
        <c:choose>
            <c:when test="${not empty profile_id}">
                <li>
                    <a href="<c:url value='/edit/${profile_id}'/>" target="_BLANK">
                        <span class="glyphicon glyphicon-briefcase"></span>&nbsp;&nbsp;Current Profile
                    </a>
                </li>
                <c:choose>
                    <c:when test="${not empty clientUUID}">
                        <li>
                            <a href="<c:url value='/history/${profile_id}'/>?clientUUID=${clientUUID}" target="_BLANK">
                                <span class="glyphicon glyphicon-time"></span>&nbsp;&nbsp;Request History
                            </a>
                        </li>
                    </c:when>
                </c:choose>
                <li>
                    <a href="<c:url value='/pathorder/${profile_id}'/>" target="_BLANK">
                        <span class="glyphicon glyphicon-th-list"></span>&nbsp;&nbsp;Paths
                    </a>
                </li>
                <li role="separator" class="divider"></li>
            </c:when>
        </c:choose>
        <li>
            <a href="<c:url value='/group'/>" target="_BLANK">
                <span class="glyphicon glyphicon-blackboard"></span>&nbsp;&nbsp;Groups
            </a>
        </li>
        <li>
            <a href="<c:url value='/scripts'/>" target="_BLANK">
                <span class="glyphicon glyphicon-list-alt"></span>&nbsp;&nbsp;Scripts
            </a>
        </li>
        <li>
            <a href="<c:url value='/configuration'/>" target="_BLANK">
                <span class="glyphicon glyphicon-fire"></span>&nbsp;&nbsp;Plugins
            </a>
        </li>
    </ul>
</li>

<script type="text/javascript">
    $(document).ready(function() {
        Mousetrap.bind('n', function() {
            $("#global-nav > a").click();
        });
    });
</script>
