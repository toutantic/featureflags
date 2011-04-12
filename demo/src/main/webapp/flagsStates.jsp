<%@page import="org.featureflags.demo.Flags"%>

<h2>Flag state</h2>
<a href="javascript:parent.frames['state'].window.location.reload()">RELOAD</a>


<hr>
<% if(Flags.ONE.isUp()) { %>
Flag one is up
<%} else {%>
Flag one is down
<%} %>
<hr>
<% if(Flags.TWO.isUp()) { %>
Flag two is up
<%} else {%>
Flag two is down
<%} %>
<hr>
<% if(Flags.THREE.isUp()) { %>
Flag three is up
<%} else {%>
Flag three is down
<%} %>
<hr>