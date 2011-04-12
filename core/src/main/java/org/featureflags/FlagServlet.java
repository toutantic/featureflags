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
	AcceptedFormat format = parseFormat(request);
	Result result = Result.OK;
	String responseString = "";
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
		    responseString += getHtmlForFlag(flag);
		    break;
		}
	    }
	} else {
	    responseString += displayAllFlags();
	}
	sendResponse(response, flagName, result, responseString);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String flagName = parseFlagName(request);

	Result result = flagManager.flipFlag(flagName);

	sendResponse(response, flagName, result, null);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String flagName = parseFlagName(request);
	FlagState newFlagState = parseFlagState(request);

	Result result = flagManager.setFlagStateTo(flagName, newFlagState);

	sendResponse(response, flagName, result, null);
    }

    private String parseFlagName(HttpServletRequest request) {
	String flagName = request.getPathInfo();
	if (flagName != null && flagName.length() > 0) {
	    flagName = flagName.substring(1);
	}

	// In the servlet API 2.4 we can't get this information in the init method without a Request object
	servletUri = request.getContextPath() + request.getServletPath();
	return flagName;
    }
    
    private AcceptedFormat parseFormat(HttpServletRequest request) {
	String formatString = request.getParameter(AcceptedParameter.format.toString());
	//HTML is the default format
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
    
    private void sendResponse(HttpServletResponse response, String flagName, Result result, String body) throws IOException {
	switch (result) {
	case NOT_FOUND:
	    response.sendError(404, "Can't find " + flagName);
	    break;
	default:
	    if (body != null) {
		String content = String.format(html, body);
		response.getWriter().print(content);
	    } else {
		response.sendRedirect(servletUri);
	    }
	    break;
	}
    }

    private String displayAllFlags() {
	StringBuilder builder = new StringBuilder();

	for (FeatureFlags flag : flagManager.getFlags()) {
	    builder.append(getHtmlForFlag(flag));
	}
	return builder.toString();
    }
    
    
    private String getHtmlForFlag(FeatureFlags flag) {
	String divClass = flag.isUp() ? "flagUp" : "flagDown";
	return String.format(linkTemplate, servletUri + "/" + flag, flag.getDescription(), divClass, flag);
    }

}
