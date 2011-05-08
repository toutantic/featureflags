Feature flags
=======

*Turn feature up or down live by using feature flag also known as feature flipping or[feature toggle](http://martinfowler.com/bliki/FeatureToggle.html)*  
Inspired by [Flickr](http://code.flickr.com/blog/2009/12/02/flipping-out/) and [Launchpad](https://dev.launchpad.net/LEP/FeatureFlags).  
The main goal is to decouple code delivery in production from feature activation.  

Requirements
=======

* Java SE 5 (for enum)
* slf4j 1.6.1 (for logging)
* Servlet API 2.4

Demo
=======
[http://featureflagsdemo.toutantic.cloudbees.net](http://featureflagsdemo.toutantic.cloudbees.net)  

Roadmap
=======
- Persist flags state in case of reboot
- Distribute flags state across servers 
- Manage different state per server
- *Manage different state per user or user group* (available in 1.1.0)
- ...

How To Use
=======

**First: dependencies**  
Add feature flag jar and dependencies to your classpath
* [featureflags-1.1.0.jar](http://dl.dropbox.com/u/24652695/featureflags-1.1.0.jar)
* [slf4-api-1.6.1.jar](http://search.maven.org/remotecontent?filepath=org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar)
* [slf4-simple-1.6.1.jar](http://search.maven.org/remotecontent?filepath=org/slf4j/slf4j-simple/1.6.1/slf4j-simple-1.6.1.jar)

**Second: create a FeatureFlags enum**  
Create an enum like the one below to host all your feature flags.

	package org.featureflags.demo;
	
	import org.featureflags.FeatureFlags;
	import org.featureflags.FlagManager.FlagState;
	
	public enum Flags implements FeatureFlags {
	
		//Create your own feature flags
	    ONE("First Feature Flag"),
	    TWO("Second Feature Flag", FlagState.UP),
	    THREE("Third Feature Flag");
	    
	    //Don't change anything below
	    private FlagState flagState = FlagState.DOWN;
	    private String description;
	    private FlagManager flagManager;
	    
	    private Flags(String description) {
			initFlag(description, flagState);
	    }
	
	    private Flags(String description, FlagState flagState) {
			this.flagState = flagState;
			initFlag(description, flagState);
	    }
	
	    public void initFlag(String description, FlagState flagState) {
			this.description = description;
			this.flagManager = FlagManager.get(this, flagState);    
	    }
	    
	    public boolean isUp() {
		    return flagManager.isUp(this);
	    }
	
	    public String getDescription() {
	        return description;
	    }
	    
	}

**Third: Use Feature Flags in your code**  

	if(Flags.ONE.isUp()) { 
		// Do the new stuff
	} else {
		// Keep doing the old stuff
	}
	
**Fourth: Configure FeatureFlag servlet**  

Add this to your web.xml (replace filter-class and param-value with your own)

		<servlet>
			<servlet-name>featureFlags</servlet-name>
			<servlet-class>org.featureflags.FlagServlet</servlet-class>
			<init-param>
				<param-name>featureFlagsClassName</param-name>
				<param-value>org.featureflags.demo.Flags</param-value>
				<description>Full class name of your Feature flags class. REQUIRED</description>
			</init-param>
		</servlet>
	
		<servlet-mapping>
			<servlet-name>featureFlags</servlet-name>
			<url-pattern>/flags/*</url-pattern>
		</servlet-mapping>
		
Optional: add a servlet filter to enable flag per user/group

Create a servletFilter:

	public class FeatureFlagFilter implements Filter {
	
	    private FlagManager flagManager;
	    
	    public void init(FilterConfig filterConfig) throws ServletException {
			String featureFlagsClassName = filterConfig.getInitParameter("featureFlagsClassName");
			flagManager = FlagManager.get(featureFlagsClassName);
	    }
	
	    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			// Replace this your code to get the current user or user group name
			String username = request.getParameter("username");
			flagManager.setThreadUserName(username);
			chain.doFilter(request, response);
			flagManager.resetThreadUserName();
	
	    }
	
	    public void destroy() {
	    }
	
	}
	
Add this to your web.xml (replace filter-class and param-value with your own)

	<filter>
		<filter-name>featureFlagsFilter</filter-name>
		<filter-class>org.featureflags.demo.FeatureFlagFilter</filter-class>
		<init-param>
			<param-name>featureFlagsClassName</param-name>
			<param-value>org.featureflags.demo.Flags</param-value>
			<description>Full class name of your Feature flags class. REQUIRED</description>
		</init-param>
	</filter>

	<filter-mapping>
		<filter-name>featureFlagsFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

Feature flipping elsewhere
=======
* [grails](http://www.grails.org/plugin/feature-flipper)
* [in django](http://djangopackages.com/grids/g/feature-flip/)

		
