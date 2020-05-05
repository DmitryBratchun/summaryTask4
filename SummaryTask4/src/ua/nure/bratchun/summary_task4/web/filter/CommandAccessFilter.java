package ua.nure.bratchun.summary_task4.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ua.nure.bratchun.summary_task4.Path;
import ua.nure.bratchun.summary_task4.db.Role;

/**
 * Security filter. Disabled by default. Uncomment Security filter
 * section in web.xml to enable.
 * 
 * @author D.Bratchun
 * 
 */
public class CommandAccessFilter implements Filter {
	
	private static final Logger LOG = Logger.getLogger(CommandAccessFilter.class);

	// commands access	
	private EnumMap<Role, List<String>> accessMap = new EnumMap<>(Role.class);
	private List<String> commons = new ArrayList<>();	
	private List<String> outOfControl = new ArrayList<>();
	
	@Override
	public void destroy() {
		LOG.debug("Filter destruction starts");
		// do nothing
		LOG.debug("Filter destruction finished");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		LOG.debug("Filter starts");
		
		if (accessAllowed(request)) {
			LOG.debug("Filter finished");
			chain.doFilter(request, response);
		} else if(((HttpServletRequest) request).getSession().getAttribute("user")!=null){
			String errorMessasge = "You do not have permission to access the requested resource " + request.getParameter("command");
			
			request.setAttribute("errorMessage", errorMessasge);
			LOG.trace("Set the request attribute: errorMessage --> " + errorMessasge);
			((HttpServletRequest) request).getSession().setAttribute("wrongfulActErrorMessage", "error_page_jsp.wrongful_act");
			request.getRequestDispatcher(Path.PAGE_ERROR)
					.forward(request, response);
		} else {
			((HttpServletRequest) request).getSession().setAttribute("loginErrorMessage", "login_jsp.error.not_authorized");
			request.getRequestDispatcher(Path.PAGE_LOGIN)
			.forward(request, response);
		}
	}
	/**
	 * Checks access to commands.
	 * @param request
	 * @return boolean result
	 */
	private boolean accessAllowed(ServletRequest request) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;

		String commandName = request.getParameter("command");
		if (commandName == null || commandName.isEmpty()) {
			return false;
		}
		
		if (outOfControl.contains(commandName)) {
			return true;
		}
		
		HttpSession session = httpRequest.getSession(false);
		if (session == null) { 
			return false;
		}
		
		Role userRole = (Role)session.getAttribute("userRole");
		if (userRole == null) {
			return false;
		}
		return accessMap.get(userRole).contains(commandName)
				|| commons.contains(commandName);
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		LOG.debug("Filter initialization starts");
		
		// roles
		accessMap.put(Role.ADMIN, asList(fConfig.getInitParameter("admin")));
		accessMap.put(Role.CLIENT, asList(fConfig.getInitParameter("client")));
		LOG.trace("Access map --> " + accessMap);

		// commons
		commons = asList(fConfig.getInitParameter("common"));
		LOG.trace("Common commands --> " + commons);

		// out of control
		outOfControl = asList(fConfig.getInitParameter("out-of-control"));
		LOG.trace("Out of control commands --> " + outOfControl);
		
		LOG.debug("Filter initialization finished");
	}
	
	/**
	 * Extracts parameter values from string.
	 * 
	 * @param str
	 *            parameter values string.
	 * @return list of parameter values.
	 */
	private List<String> asList(String str) {
		List<String> list = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(str);
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		return list;		
	}
	
}