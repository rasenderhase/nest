package de.nikem.nest.resource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import de.nikem.nest.filter.BeanFactory;
import de.nikem.nest.util.Messages;
import de.nikem.nest.web.layout.ViewableFactory;

@Path("/nest")
public class LoginResource {
	
	@Context HttpServletRequest request;
	private Messages messages;
	
	public LoginResource() {
		setMessages(BeanFactory.get().getMessages());
	}
	
	@GET
	@Path("/dologin")
	@Produces("text/html")
	public Object doLogin() {
//		getMessages().initLocalizationContext();
		Map<String, Object> model = new HashMap<>();
		model.put("title", "Login");
		return new ViewableFactory(request).createViewable("/nest/dologin", model, "login");
	}
	
	@GET
	@Path("/login")
	@Produces("text/html")
	public Object login() {
		final URI location;
		location = UriBuilder.fromPath(request.getContextPath() + "/rest/nest/index").build();
		return Response.seeOther(location).build();
	}
	
	@Path("/failed")
	@GET
	@Produces("text/plain")
	public Object loginfailed() {
		return "kein Zugriff";
	}

	public Messages getMessages() {
		return messages;
	}

	public void setMessages(Messages messages) {
		this.messages = messages;
	}
}
