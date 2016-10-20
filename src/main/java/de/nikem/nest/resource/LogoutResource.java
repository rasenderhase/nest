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

import de.nikem.nest.util.LocalizationContextManager;

@Path("/logout")
public class LogoutResource {
	@Context HttpServletRequest request;
	@Context HttpHeaders httpHeaders;
	
	@GET
	@Produces("text/html")
	public Object logout() {
		final HttpServletRequest request = getRequest();
		
		final HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		new LocalizationContextManager().setLocalizationContext(request);
		
		String referer = httpHeaders.getHeaderString("referer");
		
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
}
