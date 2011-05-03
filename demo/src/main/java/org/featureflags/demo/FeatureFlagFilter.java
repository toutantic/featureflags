package org.featureflags.demo;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.featureflags.FlagManager;

public class FeatureFlagFilter implements Filter {

    private FlagManager flagManager;
    
    public void init(FilterConfig filterConfig) throws ServletException {
	String featureFlagsClassName = filterConfig.getInitParameter("featureFlagsClassName");
	flagManager = FlagManager.get(featureFlagsClassName);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	String username = request.getParameter("username");
	flagManager.setThreadUserName(username);
	chain.doFilter(request, response);
	flagManager.resetThreadUserName();

    }

    public void destroy() {
    }

}
