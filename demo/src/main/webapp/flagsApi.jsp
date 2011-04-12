
<h2>Restful APi</h2>
Try these urls and click RELOAD on the left iframe<br/>

Display all the flags in html, clicking on a flag flip it. (green is up, red down)
<% String url= "flags";%>
<a href="<%= url%>"><%= url%></a>

<hr>
Display just one flag in Html
<% url= "flags/ONE";%>
<a href="<%= url%>"><%= url%></a>

<hr/>
or display just one flag state in plaintext
<% url= "flags/ONE?format=txt";%>
<a href="<%= url%>"><%= url%></a>

<hr>
HTTP POST to flip a flag
<form method="POST" action="flags/ONE" > 
	<button type="submit">Flip flag ONE</button>
</form> 

<form method="POST" action="flags/TWO" > 
	<button type="submit">Flip flag TWO</button>
</form>
<form method="POST" action="flags/THREE" > 
	<button type="submit">Flip flag THREE</button>
</form>


<hr>
HTTP PUT with UP or DOWN as content to set flag state
<br/> You have to try this out of the browser, with curl or poster extension for <a href="https://chrome.google.com/extensions/detail/cdjfedloinmbppobahmonnjigpmlajcd">Chrome</a> or <a href="https://addons.mozilla.org/en-us/firefox/addon/poster/">Firefox</a>
