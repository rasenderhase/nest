package de.nikem.nest.jdbc;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class JdbcUtilTest {
	
	private interface OtherInterface {
		
	}

	private JdbcUtil subject;
	private DataSource dataSourceMock;
	private Connection connectionMock;
	private Statement statementMock;
	private PreparedStatement preparedStatementMock;
	
	@Before
	public void beforeMethod() throws Exception {
		dataSourceMock = mock(DataSource.class);
		connectionMock = mock(Connection.class);
		statementMock = mock(Statement.class, withSettings().extraInterfaces(OtherInterface.class));
		preparedStatementMock = mock(PreparedStatement.class);
		subject = JdbcUtil.createDataSourceInstance(dataSourceMock);
		
		when(dataSourceMock.getConnection()).thenReturn(connectionMock);
		when(connectionMock.createStatement()).thenReturn(statementMock);
		when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
		when(statementMock.unwrap(any())).thenReturn(statementMock);
	}
	
	@Test
	public void testDoWithoutTransaction() {
		Object thing = new Object();
		
		Object result = subject.doWithoutTransaction(con -> {
			assertThat("Not a proxy: " + con.getClass(), Proxy.isProxyClass(con.getClass()), is(true));
			
			return thing;
		});
		assertThat("lambda result is returned", result, equalTo(thing));
	}
	
	@Test
	public void testCreateStatement() throws Exception {
		subject.doWithoutTransaction(con -> {
			Statement stmt = con.createStatement();
			assertThat("Not a proxy: " + stmt.getClass(), Proxy.isProxyClass(stmt.getClass()), is(true));
			
			stmt.executeQuery("hallo");
			Object other = stmt.unwrap(OtherInterface.class);
			assertThat("proxied object is not returned", other, equalTo(statementMock));
			
			return null;
		});
		
		verify(statementMock).executeQuery("hallo");
	}
	
	@Test
	public void testPrepareStatement() throws Exception {
		subject.doWithoutTransaction(con -> {
			PreparedStatement stmt = con.prepareStatement("select name,\n vorname\r\nfrom\nsome_table\nwhere name = ? --Ist name hier korrekt?\r\nor vorname = ?");
			assertThat("Not a proxy: " + stmt.getClass(), Proxy.isProxyClass(stmt.getClass()), is(true));
			
			stmt.setString(1, "Andi");
			stmt.setString(2, "O'Connor");
			stmt.executeQuery();
			return null;
		});
	}
}
