package org.featureflags;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.featureflags.FlagManager.FlagState;
import org.featureflags.FlagManager.Result;

/**
 * Servlet implementation class FlagServlet
 */
public class FlagServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private FlagManager flagManager;
    private String html;
    private String linkTemplate;
    private String servletUri;

    private enum AcceptedParameter {
	// used to get a different representation of a flag
	format
    }

    private enum AcceptedFormat {
	// just give a simple txt version of flag
	txt,
	// html for human interaction
	html
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
    public FlagServlet() {
	super();
    }

    @Override
    public void init() throws ServletException {
	String featureFlagsClassName = getServletConfig().getInitParameter("featureFlagsClassName");
	flagManager = FlagManager.get(featureFlagsClassName);
	flagManager.initFlags();
	html = Utils.readRessource(flagManager, "index.html");
	linkTemplate = Utils.readRessource(flagManager, "link.template");

    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String flagName = parseFlagName(request);
	String userName = parseUserName(request);
	AcceptedFormat format = parseFormat(request);
	Result result = Result.OK;
	String responseString = "";
	flagManager.setThreadUserName(userName);
	if (flagName != null && flagName.length() > 0) {
	    FeatureFlags flag = flagManager.getFlag(flagName);
	    if (flag == null) {
		result = Result.NOT_FOUND;
	    } else {
		switch (format) {
		case txt:
		    responseString = flag.isUp() ? FlagState.UP.toString() : FlagState.DOWN.toString();
		    break;
		default:
		    responseString += getHtmlForFlag(flag, userName);
		    break;
		}
	    }
	} else {
	    responseString += displayAllFlags();
	}
	flagManager.resetThreadUserName();

	sendResponse(response, flagName, result, responseString, null);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String refererPage = request.getHeader("referer");
	System.out.println(refererPage);
	String flagName = parseFlagName(request);
	String userName = parseUserName(request);
	String userNameInParam = request.getParameter("username");
	if(userNameInParam != null) {
	    userName = userNameInParam;
	}
	Result result = null;
	if (userName != null) {
	    result = flagManager.flipFlagForUser(userName, flagName);
	} else {
	    result = flagManager.flipFlag(flagName);
	}

	sendResponse(response, flagName, result, null, refererPage);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String flagName = parseFlagName(request);
	String userName = parseUserName(request);

	FlagState newFlagState = parseFlagState(request);

	Result result = null;
	if (userName != null) {
	    result = flagManager.setFlagStateForUserTo(userName, flagName, newFlagState);
	} else {
	    result = flagManager.setFlagStateTo(flagName, newFlagState);
	}

	sendResponse(response, flagName, result, null, null);
    }

    private String parseFlagName(HttpServletRequest request) {
	String flagName = parseUserOrFlagName(request, true);

	// In the servlet API 2.4 we can't get this information in the init method without a Request object
	servletUri = request.getContextPath() + request.getServletPath();
	return flagName;
    }

    private String parseUserName(HttpServletRequest request) {
	String userName = parseUserOrFlagName(request, false);

	return userName;
    }

    private String parseUserOrFlagName(HttpServletRequest request, boolean parseFlag) {
	String flagName = request.getPathInfo();
	if (flagName != null && flagName.length() > 0) {
	    flagName = flagName.substring(1);

	    int slashIndex = flagName.indexOf("/");
	    if (slashIndex != -1) {
		if (parseFlag) {
		    return flagName.substring(0, slashIndex);
		} else {
		    return flagName.substring(slashIndex + 1);
		}
	    } else {
		if (parseFlag) {
		    return flagName;
		} else {
		    return null;
		}
	    }
	}

	return null;

    }

    private AcceptedFormat parseFormat(HttpServletRequest request) {
	String formatString = request.getParameter(AcceptedParameter.format.toString());
	// HTML is the default format
	if (formatString == null || formatString.length() == 0) {
	    return AcceptedFormat.html;
	}
	return AcceptedFormat.valueOf(formatString);
    }

    private FlagState parseFlagState(HttpServletRequest request) throws IOException {
	String flagNewStateString = Utils.readerToString(request.getReader());
	FlagState newFlagState = FlagState.valueOf(flagNewStateString);
	return newFlagState;
    }

    private void sendResponse(HttpServletResponse response, String flagName, Result result, String body, String refererPage) throws IOException {
	switch (result) {
	case NOT_FOUND:
	    response.sendError(404, "Can't find " + flagName);
	    break;
	default:
	    if (body != null) {
		String content = String.format(html, body);
		response.getWriter().print(content);
	    } else {
		if(refererPage != null) {
		    response.sendRedirect(refererPage);
		} else {
		    response.sendRedirect(servletUri);
		}
	    }
	    break;
	}
    }

    private String displayAllFlags() {
	StringBuilder builder = new StringBuilder();

	for (FeatureFlags flag : flagManager.getFlags()) {
	    builder.append(getHtmlForFlag(flag, null));
	}
	return builder.toString();
    }

    private String getHtmlForFlag(FeatureFlags flag, String userName) {
	String html = "<div class=\"flagDiv\">"; 
	    html+= getHtmlForFlag(flag, userName, null);
	String[] userNames = flagManager.getUsersForFlag(flag);
	if (userNames.length > 0) {
	    for (String user : userNames) {
		html += getHtmlForUserFlag(flag, userName, user);
	    }
	}
	html+= "<form class=\"flagForm\" method=\"POST\" action=\"/ff/flags/";
	html+= flag;
	html+="\" >";
	html+= "<input type=\"text\" name=\"username\" title=\"add a user\" value=\"username\" />";
	//html+= "<div title=\"First Feature Flag\" onclick=\"javascript:this.parentNode.submit();\" class=\"flagUp\">ONE</div>"; 
	html+= "</form>";
	return html;
    }

    private String getHtmlForFlag(FeatureFlags flag, String userName, String threadUserName) {
	flagManager.setThreadUserName(threadUserName);
	String divClass = flag.isUp() ? "flagUp" : "flagDown";
	String uri = servletUri + "/" + flag;
	if (userName != null) {
	    uri += "/" + userName;
	}
	flagManager.resetThreadUserName();
	return String.format(linkTemplate, uri, flag.getDescription(), divClass, flag);
    }
    
    private String getHtmlForUserFlag(FeatureFlags flag, String userName, String threadUserName) {
	flagManager.setThreadUserName(threadUserName);
	String divClass = flag.isUp() ? "flagUp" : "flagDown";
	String uri = servletUri + "/" + flag;

	if (threadUserName != null) {
	    uri += "/" + threadUserName;
	} else if (userName != null) {
	    uri += "/" + userName;
	}
	
	
	flagManager.resetThreadUserName();
	return String.format(linkTemplate, uri, flag.getDescription(), divClass, threadUserName);
    }

}
