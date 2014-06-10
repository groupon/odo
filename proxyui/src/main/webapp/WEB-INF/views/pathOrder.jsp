<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" isELIgnored="false" %> 
<%@ page session="false" %>
<html>

<!-- Used a lot of code from: http://jqueryui.com/demos/sortable/default.html -->
<head>
<title>Update Path Order</title>
  <%@ include file="/resources/js/webjars.include" %>
  
  <link rel="stylesheet" type="text/css" media="screen"
	 href="<c:url value="/resources/css/odo.css"/>" />
  
  <style>
	#sortable { list-style-type: none; margin: 1px; padding: 1px; width: 60%; }
	#sortable li { margin: 0 3px 3px 3px; padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; height: 18px; }
	#sortable li span { position: absolute; margin-left: -1.3em; }
  </style>
  
  <script>
  $(document).ready(function() {
    $("#sortable").sortable({
    	update: function(event, ui) {
			var pathOrder = $('#sortable').sortable('toArray').toString();
			//alert(pathOrder);
			$.ajax({
				type: "POST",
				url: "",
				data: ({pathOrder : pathOrder}),
				success: function(){
			    	$('#info').html('Path Order Updated');
			    	$('#info').fadeOut(1).delay(50).fadeIn(150);
				}
			});
		}
    });
  });
  </script>
</head>

<body>

<nav class="navbar navbar-default" role="navigation">
    <div class="container-fluid">
        <div class="collapse navbar-collapse">
            <ul id="status2" class="nav navbar-nav">
                <li><a href="#" onClick="window.location='<c:url value = '/profiles' />'">All Profiles</a></li>
                <li><a href="#" onClick="window.location='<c:url value = '/edit/${profile_id}' />'">${profile_name}</a></li>
            </ul>
        </div>
    </div>
</nav>
<div>
	<div class="ui-widget-header ui-corner-all">
		<h3>Drag items to set priority</h3>
		The ordering of paths impacts how requests are handled.  In general if a higher priority path matches a request then further paths will not be evaluated.  The only exception is Global paths.  In the case that a global path is matched the matcher will continue to search for a non-global matching path.	
	</div>
	<div class="ui-widget-content ui-corner-all">
		<ul id="sortable">
			<c:forEach var="pathname" items="${pathnames}">
				<li class="ui-state-default ui-corner-all reorderbox" id='${pathname.pathId}'><span class="ui-icon ui-icon-arrowthick-2-n-s"></span>
					${pathname.pathName} ==> ${pathname.path}
				</li>
			</c:forEach>
		</ul>
	</div> 
</div>
</body>
</html>