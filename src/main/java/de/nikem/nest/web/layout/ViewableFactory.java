package de.nikem.nest.web.layout;

import javax.servlet.http.HttpServletRequest;

import org.glassfish.jersey.server.mvc.Viewable;

public class ViewableFactory {
	
	private HttpServletRequest request;
	
	protected HttpServletRequest getRequest() {
		return request;
	}

	public ViewableFactory setRequest(HttpServletRequest request) {
		this.request = request;
		return this;
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
	
	public Object createStandAloneViewable(String templateName) {
		return createStandAloneViewable(templateName, null);
	}
	
	public Object createStandAloneViewable(String templateName, Object model) {
		return new Viewable(templateName, model);
	}
}
