package de.nikem.nest.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class NestFilterTest  {
	
	private NestFilter nestFilter;
	private FilterChain filterChain;
	private String contextPath;
	private String requestURI;
	private HttpServletRequest servletRequest;
	
	@Before
	public void beforeMethod() {
		nestFilter = new NestFilter();
		filterChain = mock(FilterChain.class);
		servletRequest = mock(HttpServletRequest.class);
		
		contextPath = "/nest";
		requestURI = contextPath + "/static/1.0-SNAPSHOT/scripts/nest/nest.js";
		
		when(servletRequest.getRequestURI()).thenReturn(requestURI);
		when(servletRequest.getContextPath()).thenReturn(contextPath);
		when(servletRequest.getRequestDispatcher(any(String.class))).thenReturn(mock(RequestDispatcher.class));
	}
	
	@Test
	public void testDoFilterRemoveVersion() throws IOException, ServletException {
		nestFilter.doFilter(servletRequest, null, filterChain);
		
		ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
		verify(servletRequest).getRequestDispatcher(pathCaptor.capture());
		
		assertThat(pathCaptor.getValue(), is("/static/scripts/nest/nest.js"));
	}
	
	@Test
	public void testDoFilterRemoveVersionPlaceholder() throws IOException, ServletException {
		requestURI = contextPath + "/static/@version@/scripts/nest/nest.js";
		when(servletRequest.getRequestURI()).thenReturn(requestURI);
		
		nestFilter.doFilter(servletRequest, null, filterChain);
		
		ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
		verify(servletRequest).getRequestDispatcher(pathCaptor.capture());
		
		assertThat(pathCaptor.getValue(), is("/static/scripts/nest/nest.js"));
	}
	
	@Test
	public void testDoFilterNoVersion() throws IOException, ServletException {
		requestURI = contextPath + "/static/scripts/nest/nest.js";
		when(servletRequest.getRequestURI()).thenReturn(requestURI);
		
		nestFilter.doFilter(servletRequest, null, filterChain);
		
		verify(servletRequest, never()).getRequestDispatcher(any(String.class));
	}
}
