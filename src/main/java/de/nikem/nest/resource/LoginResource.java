package de.nikem.nest.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import de.nikem.nest.web.layout.ViewableFactory;

@Path("/login")
public class LoginResource {
	
	@Context HttpServletRequest request;
	
	@GET
	@Produces("text/html")
	public Object login() {
		return new ViewableFactory(request).createViewable("/nest/login");
	}
	
	@Path("/failed")
	@GET
	@Produces("text/plain")
	public Object loginfailed() {
		return "kein Zugriff";
	}
}
