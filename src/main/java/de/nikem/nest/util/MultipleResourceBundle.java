package de.nikem.nest.util;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;

public class MultipleResourceBundle extends ResourceBundle {
	private Logger log = Logger.getLogger(getClass().getName(), "de.nikem.nest.texts");

	private final Map<String, ResourceBundle> bundles = new LinkedHashMap<>();
	
	public void addResourceBundle(String basename, Locale locale) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(basename, locale);
		bundles.put(basename, resourceBundle);
	}
	
	@Override
	protected Object handleGetObject(String key) {
		for (ResourceBundle resourceBundle : bundles.values()) {
			try {
				return resourceBundle.getObject(key);
			} catch (MissingResourceException mre) {
				log.finer(key + " not found in " + resourceBundle.getBaseBundleName());
			}
		}
		throw new MissingResourceException("Can't find resource for bundles "
                +bundles.keySet()
                +", key "+key,
                this.getClass().getName(),
                key);
	}

	@Override
	public Enumeration<String> getKeys() {
		Vector<String> keys = new Vector<>();
		for (ResourceBundle resourceBundle : bundles.values()) {
			keys.addAll(resourceBundle.keySet());
		}
		return keys.elements();
	}

}
