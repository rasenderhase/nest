package de.nikem.nest.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PreparedStatementInvocationHandler implements InvocationHandler {
	private final Logger log = Logger.getLogger("de.nikem.nest.jdbc.Statement");

	private final PreparedStatement delegate;
	private final String query;

	private final Map<Integer, String> parameters = new HashMap<>();

	public PreparedStatementInvocationHandler(PreparedStatement delegate, String query) {
		super();
		this.delegate = delegate;
		this.query = query;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String query = "";
		boolean executeMethod = false;
		if (isParameterSetterMethod(method, args)) {
			//Record parameters
			parameters.put((int) args[0], toDynamicSqlParameterString(args[1]));
		} else if (method.getName().equals("registerOurParameter")
				&& args[0].getClass().equals(Integer.class)) { 
			//Record out parameters
			parameters.put((int) args[0], "<out>");
		} else if (executeMethod = isExecuteMethod(method)) {
			query = buildQuery(this.query);
		}

		try {
			Object result = method.invoke(delegate, args);
			if (executeMethod) {
				log.fine(method.getName() + ": " + query);
			}
			return result;
		} catch (Throwable t) {
			if (executeMethod) {
				log.severe(method.getName() + ": " + query);
			}
			throw t;
		}
	}

	boolean isExecuteMethod(Method method) {
		return method.getName().startsWith("execute");
	}

	String buildQuery(String query) throws IOException {
		StringBuilder queryBuilder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new StringReader(query));) {
			String line;
			int parameterCount = 1;
			while ((line = reader.readLine()) != null) {
				int length = line.length();
				int commentCount = 0;

				if (queryBuilder.length() > 0) {
					queryBuilder.append(System.getProperty("line.separator"));
				}

				for (int i = 0; i < length; i++) {
					char c = line.charAt(i);
					if (c == '-') {
						commentCount++;
					}

					//stop replacing ? in the line after two dashes
					if (c == '?' && commentCount < 2) {
						queryBuilder.append(parameters.get(parameterCount++));
						continue;
					}

					queryBuilder.append(c);
				}
			}
		}
		return queryBuilder.toString();
	}

	boolean isParameterSetterMethod(Method method, Object[] args) {
		return method.getName().startsWith("set") && args.length == 2 
				&& args[0] != null && args[1] != null
				&& args[0].getClass().equals(Integer.class);
	}

	private String toDynamicSqlParameterString(Object object) {
		if (object == null) {
			return "null";
		}

		if (object instanceof CharSequence
				|| object instanceof Date
				|| object instanceof Timestamp) {
			return quote(object);
		}

		if (object instanceof java.util.Date) {
			return quote(new Timestamp(((java.util.Date) object).getTime()));
		}

		return object.toString();
	}

	private static String quote(Object s) {
		return "'" + s.toString().replaceAll("'", "''") + "'";
	}
}
