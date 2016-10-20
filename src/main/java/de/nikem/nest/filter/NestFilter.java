package de.nikem.nest.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import de.nikem.nest.util.LocalizationContextManager;

public class NestFilter implements Filter {
	private Logger log = Logger.getLogger(getClass().getName(), "de.nikem.nest.texts");
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("nest.filter.inititalized");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		new LocalizationContextManager().setLocalizationContext(request);
		
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {

	}

}
