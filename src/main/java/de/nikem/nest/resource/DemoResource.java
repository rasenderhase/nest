package de.nikem.nest.resource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.nikem.nest.filter.BeanFactory;
import de.nikem.nest.web.layout.ViewableFactory;

@Path("/nest/demo")
public class DemoResource {

	private ViewableFactory viewableFactory;
	
	public DemoResource() {
		setViewableFactory(BeanFactory.get().getViewableFactory());
	}
	
	@GET
	@Produces("text/html")
	@RolesAllowed({ "info", "admin" })
	public Object index() {
		Map<String, Object> model = new HashMap<>();
		model.put("datetime", new Date());
		model.put("title", "Demo");
		return getViewableFactory().createViewable("/nest/demo/demo", model, "demo");
	}
	
	protected ViewableFactory getViewableFactory() {
		return viewableFactory;
	}

	public void setViewableFactory(ViewableFactory viewableFactory) {
		this.viewableFactory = viewableFactory;
	}
}
