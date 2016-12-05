package de.nikem.nest.web.layout;

import javax.servlet.http.HttpServletRequest;

import org.glassfish.jersey.server.mvc.Viewable;

public class ViewableFactory {
	
	private final HttpServletRequest request;
	
	public ViewableFactory(HttpServletRequest request) {
		super();
		this.request = request;
	}

	public Object createViewable(String templateName) {
		return createViewable(templateName, null, null);
	}
	
	public Object createViewable(String templateName, Object model) {
		return createViewable(templateName, model, null);
	}
	
	public Object createViewable(String templateName, Object model, String mainScript) {
		request.setAttribute("_bodyJsp", "/WEB-INF/jsp" + templateName + ".jsp");
		if (mainScript != null) {
			request.setAttribute("_mainScript", mainScript);
		}
		return new Viewable("/nest/frame", model);
	}
}
