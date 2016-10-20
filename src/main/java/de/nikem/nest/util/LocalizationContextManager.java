package de.nikem.nest.util;

import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

public class LocalizationContextManager {
	private static final String BASENAMES = "de.nikem.nest.filter.NestFilter.basenames";
	private Logger log = Logger.getLogger(getClass().getName(), "de.nikem.nest.texts");

	/**
	 * @param request
	 */
	public void setLocalizationContext(ServletRequest request) {
		final Locale locale = Locale.GERMAN;			//TODO: LocaleChooser
		HttpSession session = ((HttpServletRequest) request).getSession(false);
		
		if (Config.get(session, Config.FMT_LOCALIZATION_CONTEXT) == null || Config.get(session, Config.FMT_LOCALE) == null) {
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
			
			LocalizationContext locCtxt = new LocalizationContext(multipleResourceBundle);

			if (session != null) {
				Config.set(session, Config.FMT_LOCALIZATION_CONTEXT, locCtxt);
				Config.set(session, Config.FMT_LOCALE, locale.toString());			
			}
			Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, locCtxt);
			Config.set(request, Config.FMT_LOCALE, locale.toString());
		}
	}
}
