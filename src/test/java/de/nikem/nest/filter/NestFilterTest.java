package de.nikem.nest.filter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
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
		requestURI = contextPath + "/scripts/1.0-SNAPSHOT/nest/nest.js";
		
		when(servletRequest.getRequestURI()).thenReturn(requestURI);
		when(servletRequest.getContextPath()).thenReturn(contextPath);
		when(servletRequest.getRequestDispatcher(any(String.class))).thenReturn(mock(RequestDispatcher.class));
	}
	
	@Test
	public void testDoFilter1() throws IOException, ServletException {
		nestFilter.doFilter(servletRequest, null, filterChain);
		
		ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
		verify(servletRequest).getRequestDispatcher(pathCaptor.capture());
		
		assertEquals("/scripts/nest/nest.js", pathCaptor.getValue());
	}
	
	@Test
	public void testDoFilter2() throws IOException, ServletException {
		requestURI = contextPath + "/scripts/nest/nest.js";
		
		nestFilter.doFilter(servletRequest, null, filterChain);
		
		ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
		verify(servletRequest).getRequestDispatcher(pathCaptor.capture());
		
		assertEquals("/scripts/nest/nest.js", pathCaptor.getValue());
	}
}
