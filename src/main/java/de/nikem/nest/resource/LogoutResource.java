package de.nikem.nest.resource;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/logout")
public class LogoutResource {
	@Context HttpServletRequest request;
	
	@GET
	@Produces("text/html")
	public Object logout() {
		final HttpServletRequest request = getRequest();
		
		final HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		
		final URI location = UriBuilder.fromPath(request.getContextPath()).build();
		return Response.seeOther(location).build();
	}

	private HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
}
