package de.nikem.nest.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

public class ConnectionInvocationHandlerTest {

	private ConnectionInvocationHandler subject;
	
	private Statement statementMock;
	private Connection connectionMock;
	private Connection connectionProxy;
	
	@Before
	public void before() throws Exception {
		connectionMock = mock(Connection.class);
		statementMock = mock(Statement.class);
		subject = new ConnectionInvocationHandler(connectionMock);
		
		connectionProxy = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), 
				new Class<?>[] { Connection.class },
				subject);
		
		when(connectionMock.createStatement()).thenReturn(statementMock);
		when(connectionMock.getCatalog()).thenReturn("Memphis");
	}
	
	@Test
	public void testExecute() throws Exception {
		Statement statement = connectionProxy.createStatement();
		
		assertThat(true, is(Proxy.isProxyClass(statement.getClass())));
	}
	
	@Test
	public void testOther() throws Exception {
		Object result = connectionProxy.getCatalog();
		
		assertThat(true, is(not(Proxy.isProxyClass(result.getClass()))));
	}
}
