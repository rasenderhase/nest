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

@Path("/nest/")
public class LoginResource {
	
	@Context HttpServletRequest request;
	private Messages messages;
	private ViewableFactory viewableFactory;
	
	public LoginResource() {
		setMessages(BeanFactory.get().getMessages());
		setViewableFactory(BeanFactory.get().getViewableFactory());
	}
	
	@GET
	@Path("dologin")
	@Produces("text/html")
	public Object doLogin() {
		Map<String, Object> model = new HashMap<>();
		model.put("title", "Login");
		return getViewableFactory().createViewable("/nest/dologin", model, "login");
	}
	
	@GET
	@Path("login")
	@Produces("text/html")
	public Object login() {
		final URI location;
		location = UriBuilder.fromPath(request.getContextPath()).build();
		return Response.seeOther(location).build();
	}
	
//	@Path("failed")
//	@Produces("text/html")
//	public Object loginfailed() {
//		Map<String, Object> model = new HashMap<>();
//		model.put("title", "Login");
//		request.getSession().setAttribute("snackMessage", getMessages().getMessage("nest-web.login_failed"));
//		return new ViewableFactory(request).createViewable("/nest/dologin", model, "login");
//	}

	public Messages getMessages() {
		return messages;
	}

	public void setMessages(Messages messages) {
		this.messages = messages;
	}
	
	protected ViewableFactory getViewableFactory() {
		return viewableFactory;
	}

	public void setViewableFactory(ViewableFactory viewableFactory) {
		this.viewableFactory = viewableFactory;
	}
}
