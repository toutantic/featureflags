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
    private StringTemplate stringTemplate;
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
	stringTemplate = new StringTemplate(flagManager);
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
		if (userDoesNotExist(flag, userName)) {
		    result = Result.NOT_FOUND;
		} else {
		    switch (format) {
		    case txt:
			responseString = flag.isUp() ? FlagState.UP.toString() : FlagState.DOWN.toString();
			break;
		    default:
			responseString += stringTemplate.getHtmlForFlag(flag, userName);
			break;
		    }
		}
	    }
	} else {
	    responseString += stringTemplate.displayAllFlags();
	}
	flagManager.resetThreadUserName();

	sendResponse(response, flagName, result, responseString, null);
    }

    private boolean userDoesNotExist(FeatureFlags flag,String userName) {
	if(userName == null) {
	    return false;
	}
	String[] userNames = flagManager.getUsersForFlag(flag);
	if (userNames.length > 0) {
	    for (String user : userNames) {
		if (user.equals(userName)) {
		    return false;
		}
	    }
	}
	return true;
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
	if (userNameInParam != null) {
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
	    result = flagManager.setFlagStateToAndPersist(flagName, newFlagState);
	}

	sendResponse(response, flagName, result, null, null);
    }

    private String parseFlagName(HttpServletRequest request) {
	String flagName = parsePathInfo(request);

	int slashIndex = flagName.indexOf("/");
	if (slashIndex != -1) {
	    flagName = flagName.substring(0, slashIndex);
	}

	// In the servlet API 2.4 we can't get this information in the init method without a Request object
	servletUri = request.getContextPath() + request.getServletPath();
	stringTemplate.setServletUri(servletUri);
	return flagName;
    }

    private String parseUserName(HttpServletRequest request) {
	String userName = parsePathInfo(request);

	int slashIndex = userName.indexOf("/");
	if (slashIndex != -1) {
	    userName = userName.substring(slashIndex + 1);
	    if (userName.length() == 0) {
		userName = null;
	    }
	} else {
	    userName = null;
	}

	return userName;
    }

    private String parsePathInfo(HttpServletRequest request) {
	String flagName = request.getPathInfo();
	if (flagName != null && flagName.length() > 0) {
	    return flagName.substring(1);
	}

	return "";

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
	response.setContentType("text/html; charset=UTF-8");
	switch (result) {
	case NOT_FOUND:
	    response.sendError(404, "Can't find " + flagName);
	    break;
	default:
	    if (body != null) {
		String content = stringTemplate.getHtmlPage(body);
		response.getWriter().print(content);
	    } else {
		if (refererPage != null) {
		    response.sendRedirect(refererPage);
		} else {
		    response.sendRedirect(servletUri);
		}
	    }
	    break;
	}
    }

}
