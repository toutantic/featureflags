<%@page import="org.featureflags.demo.Flags"%>

<html>
<body>
<h3>Feature flag demo</h3>
This is a very basic page to demonstrate how Feature Flags let you to
turn feature up or down at runtime for everyone or just one user or
group of user.
<br />
The <a href="flags">feature flags dashboard</a> in the Iframe below is dynamically generated from your Feature Flag enumeration used in your code.
<br />
<table width="1000">
	<tr>
		<td><pre>public enum Flags implements FeatureFlags {

    ONE("First Feature Flag"),
    TWO("Second Feature Flag", FlagState.UP),
    THREE("Third Feature Flag");
</pre></td>
		<td><iframe name="api" src="flags" width="600" height="150"></iframe>
		</td>
	</tr>

	<tr>
		<td>To use a Feature flag in your code simply call: <pre>if(Flags.ONE.isUp()) { 
	Do the new stuff
} else {
	Do the old stuff
}
</pre></td>
		<td><b>Try to switch the flags and reload the page</b> <br />
		<form class="flagForm" method="POST" action="flags/ONE/bob">
		<button type="submit">switch flag ONE for
		user bob</button>	(it does a POST to the URL flags/ONE/bob)
		</form>

		Reload this <a href="index.jsp?username=bob"> page with the
		username parameter set to bob</a>
		<form class="flagForm" method="POST" action="flags/TWO">
		<button type="submit">switch flag TWO for everyone (it does a POST to the URL flags/TWO)
		</form>
		<form class="flagForm" method="POST" action="flags/THREE">
		<button type="submit">switch flag THREE for everyone (it does a POST to the URL flags/THREE)
		</form>
		</td>
	</tr>

<tr>

<td width="600px">
<table>
	<tr>
		<td bgcolor="#000000"><font color="#ffffff"><pre> 

<% if(Flags.THREE.isUp()) { %>
                  __ooooooooo__
                 oOOOOOOOOOOOOOOOOOOOOOo
             oOOOOOOOOOOOOOOOOOOOOOOOOOOOOOo
          oOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOo
        oOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOo
      oOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOo
     oOOOOOOOOOOO*  *OOOOOOOOOOOOOO*  *OOOOOOOOOOOOo
    oOOOOOOOOOOO      OOOOOOOOOOOO      OOOOOOOOOOOOo
    oOOOOOOOOOOOOo  oOOOOOOOOOOOOOOo  oOOOOOOOOOOOOOo
   oOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOo
   oOOOO     OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO     OOOOo
   oOOOOOO OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO OOOOOOo
    *OOOOO  OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO  OOOOO*
    *OOOOOO  *OOOOOOOOOOOOOOOOOOOOOOOOOOOOO*  OOOOOO*
     *OOOOOO  *OOOOOOOOOOOOOOOOOOOOOOOOOOO*  OOOOOO*
      *OOOOOOo  *OOOOOOOOOOOOOOOOOOOOOOO*  oOOOOOO*
        *OOOOOOOo  *OOOOOOOOOOOOOOOOO*  oOOOOOOO*
          *OOOOOOOOo  *OOOOOOOOOOO*  oOOOOOOOO*    
             *OOOOOOOOo           oOOOOOOOO*      
                 *OOOOOOOOOOOOOOOOOOOOO*
                      ""ooooooooo""

<%} else {%>
                         oooo$$$$$$$$$$$$oooo
                      oo$$$$$$$$$$$$$$$$$$$$$$$$o
                   oo$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$o         o$   $$ o$
   o $ oo        o$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$o       $$ $$ $$o$
oo $ $ "$      o$$$$$$$$$    $$$$$$$$$$$$$    $$$$$$$$$o       $$$o$$o$
"$$$$$$o$     o$$$$$$$$$      $$$$$$$$$$$      $$$$$$$$$$o    $$$$$$$$
  $$$$$$$    $$$$$$$$$$$      $$$$$$$$$$$      $$$$$$$$$$$$$$$$$$$$$$$
  $$$$$$$$$$$$$$$$$$$$$$$    $$$$$$$$$$$$$    $$$$$$$$$$$$$$  """$$$
   "$$$""""$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$     "$$$
    $$$   o$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$     "$$$o
   o$$"   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$       $$$o
   $$$    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" "$$$$$$ooooo$$$$o
  o$$$oooo$$$$$  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   o$$$$$$$$$$$$$$$$$
  $$$$$$$$"$$$$   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$     $$$$""""""""
 """"       $$$$    "$$$$$$$$$$$$$$$$$$$$$$$$$$$$"      o$$$
            "$$$o     """$$$$$$$$$$$$$$$$$$"$$"         $$$
              $$$o          "$$""$$$$$$""""           o$$$
               $$$$o                                o$$$"
                "$$$$o      o$$$$$$o"$$$$o        o$$$$
                  "$$$$$oo     ""$$$$o$$$$$o   o$$$$""
                     ""$$$$$oooo  "$$$o$$$$$$$$$"""
                        ""$$$$$$$oo $$$$$$$$$$
                                """"$$$$$$$$$$$
                                    $$$$$$$$$$$$
                                     $$$$$$$$$$"
                                      "$$$""  

<%} %>
</pre></font></td>
	</tr>
</table>
</td>
<td>
<pre>
<% if(Flags.ONE.isUp()) { %>
   |          
   |.===.     
   {}o o{}    
ooO--(_)--Ooo-
<%} else {%>
    ((__))     
     (00)      
nn--(o__o)--nn-
<%} %>
</pre>
<table>
	<tr>
		<td bgcolor="#000000"><font color="#ffffff"><pre> 
<% if(Flags.TWO.isUp()) { %>_/_/_/_/_/  _/          _/    _/_/        _/    _/  _/_/_/    
   _/      _/          _/  _/    _/      _/    _/  _/    _/   
  _/      _/    _/    _/  _/    _/      _/    _/  _/_/_/      
 _/        _/  _/  _/    _/    _/      _/    _/  _/           
_/          _/  _/        _/_/          _/_/    _/<%} else {%>_/_/_/_/_/  _/          _/    _/_/        _/_/_/      _/_/    _/          _/  _/      _/   
   _/      _/          _/  _/    _/      _/    _/  _/    _/  _/          _/  _/_/    _/    
  _/      _/    _/    _/  _/    _/      _/    _/  _/    _/  _/    _/    _/  _/  _/  _/     
 _/        _/  _/  _/    _/    _/      _/    _/  _/    _/    _/  _/  _/    _/    _/_/      
_/          _/  _/        _/_/        _/_/_/      _/_/        _/  _/      _/      _/<%} %>
</pre></font></td>
	</tr>
</table>
</td>

</tr>
</table>
</body>
</html>