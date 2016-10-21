package de.nikem.nest.filter;

import java.util.function.Function;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import de.nikem.nest.util.Messages;

public class BeanFactory implements ServletRequestListener, ServletContextListener {
	static ThreadLocal<ServletRequest> SERVLET_REQUESTS = new ThreadLocal<>();
	private static Logger log = Logger.getLogger(BeanFactory.class.getName(), "de.nikem.nest.texts");
	
	@SuppressWarnings("unchecked")
	public static <T extends BeanFactory> T get() {
		log.fine("nest.filter.retrieve_beanfactory");
		return (T) SERVLET_REQUESTS.get().getServletContext().getAttribute(BeanFactory.class.getName());
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		BeanFactory.SERVLET_REQUESTS.remove();
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		log.fine("nest.filter.set_request_thread_local");
		BeanFactory.SERVLET_REQUESTS.set(sre.getServletRequest());
		BeanFactory.get().getMessages().initLocalizationContext();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		sce.getServletContext().setAttribute(BeanFactory.class.getName(), this);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
	
	protected <T> T retrieveRequestScopedBean(Class<T> clazz, Function<T, T> initialization) {
		ServletRequest request = SERVLET_REQUESTS.get();
		String name = BeanFactory.class.getName() + "." + clazz.getName();
		@SuppressWarnings("unchecked")
		T bean = (T) request.getAttribute(name);
		if (bean == null) {
			try {
				bean = clazz.newInstance();
				bean = initialization.apply(bean);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
			request.setAttribute(name, bean);
		}
		return bean;
	}
	
	public Messages getMessages() {
		ServletRequest request = SERVLET_REQUESTS.get();
		return retrieveRequestScopedBean(Messages.class, t -> t.setRequest(request));
	}


}
