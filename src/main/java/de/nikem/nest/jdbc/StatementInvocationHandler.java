package de.nikem.nest.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.logging.Logger;

public class StatementInvocationHandler implements InvocationHandler {
	private final Logger log = Logger.getLogger("de.nikem.nest.jdbc.Statement");
	
	private final Statement delegate;
	
	public StatementInvocationHandler(Statement delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			Object result = method.invoke(delegate, args);
			if (isExecuteMethod(method, args)) {
				log.fine(method.getName() + ": " + args[0]);
			}
			return result;
		} catch (Throwable t) {
			if (isExecuteMethod(method, args)) {
				log.severe(method.getName() + ": " + args[0]);
			}
			throw t;
		}
		
	}

	private boolean isExecuteMethod(Method method, Object[] args) {
		return method.getName().startsWith("execute") && args.length > 0 && args[0] != null && args[0] instanceof CharSequence;
	}
}
