package de.nikem.nest.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class NestFilter implements Filter {
	private Logger log = Logger.getLogger(getClass().getName(), "de.nikem.nest.texts");
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("nest.filter.inititalized");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (path.matches("/static/\\d.*") || path.startsWith("/static/@version@/")) {
			log.finest("nest.filter.original_static_content");
			log.finest(path);
			int beginVersion = path.indexOf('/', 1);
			int endVersion = path.indexOf('/', beginVersion + 1);
			path = path.substring(0, beginVersion) + path.substring(endVersion);
			log.finest("nest.filter.forward_static_content");
			log.finest(path);
			request.getRequestDispatcher(path).forward(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {

	}

}
