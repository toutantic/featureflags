package org.featureflags;

public class StringTemplate {
    private String htmlTemplate;
    private String linkTemplate;
    private String inputTemplate;
    private String divTemplate;
    private String servletUri;
    private FlagManager flagManager;
    
    public void setServletUri(String servletUri) {
        this.servletUri = servletUri;
    }

    public StringTemplate(FlagManager flagManager) {
	this.htmlTemplate = Utils.readRessource(this, "index.html");
	this.linkTemplate = Utils.readRessource(this, "link.template");
	this.inputTemplate = Utils.readRessource(this, "input.template");
	this.divTemplate = Utils.readRessource(this, "div.template");
	this.flagManager = flagManager;
    }
    
    public String getHtmlPage(String body) {
	return String.format(htmlTemplate, body);
    }

    public String getInput(String uri) {
	return String.format(inputTemplate, uri);
    }

    public String getDiv(String content, String inputString) {
	return String.format(divTemplate, content, inputString);
    }

    public String getLink(String uri, String description, String divClass, String linkText) {
	return String.format(linkTemplate, uri, description, divClass, linkText);
    }
    
    public String displayAllFlags() {
	StringBuilder builder = new StringBuilder();

	for (FeatureFlags flag : flagManager.getFlags()) {
	    builder.append(getHtmlForFlag(flag, null));
	}
	return builder.toString();
    }
    
    
    public String getHtmlForFlag(FeatureFlags flag, String userName) {
	StringBuilder content = new StringBuilder();
	if(userName == null) {
	    content.append(getHtmlForUserFlag(flag, userName,null));
	}
	String[] userNames = flagManager.getUsersForFlag(flag);
	if (userNames.length > 0) {
	    for (String user : userNames) {
		if (userName == null || user.equals(userName)) {
		    content.append(getHtmlForUserFlag(flag, userName, user));
		}
	    }
	}
	String inputString = "";
	if(userName == null) {
	    inputString = getInput(servletUri + "/" + flag);
	}
	return getDiv(content.toString(), inputString);
    }

    public String getHtmlForUserFlag(FeatureFlags flag, String userName, String threadUserName) {
	flagManager.setThreadUserName(threadUserName);
	String divClass = flag.isUp() ? "flagUp" : "flagDown";
	flagManager.resetThreadUserName();

	String uri = servletUri + "/" + flag;

	if (threadUserName != null) {
	    uri += "/" + threadUserName;
	} else if (userName != null) {
	    uri += "/" + userName;
	}
	
	String linkText = threadUserName;
	if(linkText == null) {
	    linkText = flag.toString();
	}
	
	return getLink(uri, flag.getDescription(), divClass, linkText);
    }
    
    
}
