package de.nikem.nest.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

public class Messages {
	private static final String BASENAMES = "de.nikem.nest.filter.NestFilter.basenames";
	private Logger log = Logger.getLogger(getClass().getName(), "de.nikem.nest.texts");

	private Supplier<ServletRequest> servletRequestSupplier;
	
	public Messages() {
	}
	
	public void initLocalizationContext() {
		final ServletRequest request = getServletRequestSupplier().get();
		final Locale locale = Locale.GERMAN;			//TODO: LocaleChooser
			MultipleResourceBundle multipleResourceBundle = new MultipleResourceBundle();
			multipleResourceBundle.addResourceBundle("de.nikem.nest.texts", locale);
			String raw = request.getServletContext().getInitParameter(BASENAMES);
			if (raw != null) {
				String[] basenames = raw.split(",");
				for (String basename : basenames) {
					multipleResourceBundle.addResourceBundle(basename, locale);
					log.fine("nest.filter.resourcebundle_added");
					log.fine(basename);
				}
			}
			
			LocalizationContext locCtxt = new LocalizationContext(multipleResourceBundle, locale);

			Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, locCtxt);
			Config.set(request, Config.FMT_LOCALE, locale.toString());
	}
	
	public String getMessage(String key, Object...params) {
		final ServletRequest request = getServletRequestSupplier().get();
		LocalizationContext locCtxt = (LocalizationContext) Config.get(((HttpServletRequest) request).getSession(false), Config.FMT_LOCALIZATION_CONTEXT);
		if (locCtxt == null) {
			locCtxt = (LocalizationContext) Config.get(request, Config.FMT_LOCALIZATION_CONTEXT);
		}
		String message = locCtxt.getResourceBundle().getString(key);
		return MessageFormat.format(message, params);
	}

	public Supplier<ServletRequest> getServletRequestSupplier() {
		return servletRequestSupplier;
	}

	public void setServletRequestSupplier(Supplier<ServletRequest> servletRequestSupplier) {
		this.servletRequestSupplier = servletRequestSupplier;
	}
}
