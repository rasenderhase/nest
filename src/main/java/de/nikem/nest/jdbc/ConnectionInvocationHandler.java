package de.nikem.nest.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class ConnectionInvocationHandler implements InvocationHandler {
	private final Connection delegate;

	public ConnectionInvocationHandler(Connection delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = method.invoke(delegate, args);
		if (result instanceof PreparedStatement) {
			String query = (String) args[0];
			InvocationHandler handler = new PreparedStatementInvocationHandler((PreparedStatement) result, query);
			result = Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), 
					new Class<?>[] { PreparedStatement.class },
					handler);
		} else if (result instanceof Statement) {
			InvocationHandler handler = new StatementInvocationHandler((Statement) result);
			result = Proxy.newProxyInstance(Statement.class.getClassLoader(), 
					new Class<?>[] { Statement.class },
					handler);
		}

		return result;
	}

}
