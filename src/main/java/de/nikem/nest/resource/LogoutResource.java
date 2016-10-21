package de.nikem.nest.resource;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import de.nikem.nest.filter.BeanFactory;
import de.nikem.nest.util.Messages;

@Path("/logout")
public class LogoutResource {
	@Context HttpServletRequest request;
	@Context HttpHeaders httpHeaders;
	private Messages messages;
	
	public LogoutResource() {
		setMessages(BeanFactory.get().getMessages());
	}
	
	@GET
	@Produces("text/html")
	public Object logout() {
		final HttpServletRequest request = getRequest();
		
		final HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		getMessages().initLocalizationContext();
		
		String referer = getHttpHeaders().getHeaderString("referer");
		
		final URI location;
		if (referer == null) {
			location = UriBuilder.fromPath(request.getContextPath()).build();
		} else {
			location = UriBuilder.fromUri(referer).build();
		}
		
		return Response.seeOther(location).build();
	}

	private HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public Messages getMessages() {
		return messages;
	}

	public void setMessages(Messages messages) {
		this.messages = messages;
	}

	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(HttpHeaders httpHeaders) {
		this.httpHeaders = httpHeaders;
	}
}
