package de.nikem.nest.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import de.nikem.nest.web.layout.ViewableFactory;

@Path("/nest")
public class IndexResource {
	
	@Context HttpServletRequest request;
	
	@GET
	@Path("/index")
	@Produces("text/html")
	public Object index() {
		return new ViewableFactory(request).createViewable("/nest/index");
	}
}
