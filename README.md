Feature flags
=======

* Turn feature up or down live by using feature flag *
Inspired by [Flickr](http://code.flickr.com/blog/2009/12/02/flipping-out/) and [Launchpad](https://dev.launchpad.net/LEP/FeatureFlags).
The main goal is to decouple code delivery in production from feature activation.

Requirements
------------

* Java SE 5 (for enum)
* slf4j 1.6.1/logback 0.9.27 (for logging)
* Servlet API 2.4

How To Use
----------

** First: dependencies **
Add feature flag jar and dependencies to your classpath
* featureflags-1.0.0.jar
* logback-core-0.9.27.jar
* logback-classic-0.9.27.jar
* slf4-api-1.6.1.jar

Feature flag is build with maven but not yet available in a maven repository. 

** Second: create a FeatureFlags enum **
Create an enum like the one below to host all your feature flags.

	package org.featureflags;
	
	import org.featureflags.FeatureFlags;
	
	public enum Flags implements FeatureFlags {
	
	    ONE("First Feature Flag"),
	    TWO("Second Feature Flag", true),
	    THREE("Third Feature Flag");
	    
	    private boolean on = false;
	    private String description;
	    
	    private Flags(String description) {
		this.description = description;
	    }
	
	    private Flags(String description, boolean state) {
		this.description = description;
		this.on = state;
	    }
	    
	    public boolean isUp() {
		return on;
	    }
	    
	    public void up() {
		this.on = true;
	    }
	    
	    public void down() {
		this.on = false;
	    }
	    
	    public String getDescription() {
	        return description;
	    }
	    
	}

** Third:Use Feature Flag in your code **


	if(Flags.ONE.isUp()) { 
		// Do the new stuff
	} else {
		// Keep doing the old stuff
	}
	
** FOURTH: Configure FeatureFlag servlet**

Add this to your web.xml

		<servlet>
			<servlet-name>featureFlags</servlet-name>
			<servlet-class>org.featureflags.FlagServlet</servlet-class>
			<init-param>
				<param-name>featureFlagsClassName</param-name>
				<param-value>org.featureflags.Flags</param-value>
				<description>Full class name of your Feature flags class. REQUIRED</description>
			</init-param>
		</servlet>
	
		<servlet-mapping>
			<servlet-name>featureFlags</servlet-name>
			<url-pattern>/flags/*</url-pattern>
		</servlet-mapping>