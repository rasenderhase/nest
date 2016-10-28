package de.nikem.nest.resource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import de.nikem.nest.web.layout.ViewableFactory;

@Path("/nest/demo")
public class DemoResource {
	@Context HttpServletRequest request;

	@GET
	@Produces("text/html")
	public Object index() {
		Map<String, Object> model = new HashMap<>();
		model.put("datetime", new Date());
		model.put("title", "Demo");
		return new ViewableFactory(request).createViewable("/nest/demo/demo", model, "demo");
	}
}
