<li class="dropdown">
    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><strong>Navigation</strong> <span class="caret"></span></a>
    <ul class="dropdown-menu">
        <li><a href="<c:url value='/profiles'/>">All Profiles</a></li>
        <li><a href="<c:url value='/edit/${profile_id}'/>">Current Profile</a></li>
        <li><a href="<c:url value='/history/${profile_id}'/>?clientUUID=${clientUUID}">Request History</a></li>
        <li><a href="<c:url value='/group'/>">Edit Groups</a></li>
        <li><a href="<c:url value='/pathorder/${profile_id}'/>">Reorder Paths</a></li>
        <li><a href="<c:url value='/scripts'/>">Scripts</a></li>
        <li><a href="<c:url value='/configuration'/>">Plugins</a></li>
    </ul>
</li>
