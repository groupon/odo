<li class="dropdown">
    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
        <span class="glyphicon glyphicon-globe"></span>
        <strong>Navigation</strong>
        <span class="caret"></span>
    </a>
    <ul class="dropdown-menu">
        <li>
            <a href="<c:url value='/profiles'/>">
                <span class="glyphicon glyphicon-book"></span>&nbsp;&nbsp;All Profiles
            </a>
        </li>
        <li>
            <a href="<c:url value='/edit/${profile_id}'/>">
                <span class="glyphicon glyphicon-briefcase"></span>&nbsp;&nbsp;Current Profile
            </a>
        </li>
        <li>
            <a href="<c:url value='/history/${profile_id}'/>?clientUUID=${clientUUID}">
                <span class="glyphicon glyphicon-time"></span>&nbsp;&nbsp;Request History
            </a>
        </li>
        <li>
            <a href="<c:url value='/group'/>">
                <span class="glyphicon glyphicon-blackboard"></span>&nbsp;&nbsp;Edit Groups
            </a>
        </li>
        <li>
            <a href="<c:url value='/pathorder/${profile_id}'/>">
                <span class="glyphicon glyphicon-th-list"></span>&nbsp;&nbsp;Reorder Paths
            </a>
        </li>
        <li>
            <a href="<c:url value='/scripts'/>">
                <span class="glyphicon glyphicon-list-alt"></span>&nbsp;&nbsp;Scripts
            </a>
        </li>
        <li>
            <a href="<c:url value='/configuration'/>">
                <span class="glyphicon glyphicon-fire"></span>&nbsp;&nbsp;Plugins
            </a>
        </li>
    </ul>
</li>
