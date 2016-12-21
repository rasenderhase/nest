package de.nikem.nest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.nikem.nest.filter.BeanFactory;
import de.nikem.nest.web.layout.ViewableFactory;

@Path("/nest/")
public class IndexResource {
	
	private ViewableFactory viewableFactory;
	
	public IndexResource() {
		setViewableFactory(BeanFactory.get().getViewableFactory());
	}
	
	@GET
	@Path("index")
	@Produces("text/html")
	public Object index() {
		return getViewableFactory().createViewable("/nest/index");
	}
	
	protected ViewableFactory getViewableFactory() {
		return viewableFactory;
	}

	public void setViewableFactory(ViewableFactory viewableFactory) {
		this.viewableFactory = viewableFactory;
	}
}
