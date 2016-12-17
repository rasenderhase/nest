package de.nikem.nest.jdbc;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import javax.sql.DataSource;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PreparedStatementInvocationHandlerTest {

	@Parameters(name = "{0} - {1}")
	public static Collection<Object[]> data() {
		Calendar date = Calendar.getInstance();
		date.clear();
		date.set(YEAR, 2016);
		date.set(MONTH, DECEMBER);
		date.set(DAY_OF_MONTH, 17);
		date.set(HOUR_OF_DAY, 20);
		
		return Arrays.asList(new Object[][] {
            { "test1Result1", "ben" },
            { "test1Result2", null },
            { "test1Result3", new Date(date.getTime().getTime()) },
            { "test1Result4", new Timestamp(date.getTime().getTime()) },
            { "test1Result5", 1234 },
            { "test1Result6", "O'Connor" }
      });
	}

	private DataSource dataSourceMock;
	private Connection connectionMock;
	private PreparedStatement preparedStatementMock;

	@Parameter(value = 0) public String resultQueryName;
	@Parameter(value = 1) public Object queryParameter;
	
	@Before
	public void beforeMethod() throws Exception {
		dataSourceMock = mock(DataSource.class);
		connectionMock = mock(Connection.class);
		preparedStatementMock = mock(PreparedStatement.class);
		
		when(dataSourceMock.getConnection()).thenReturn(connectionMock);
		when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
	}

	@Test
	public void test() {
		JdbcUtil jdbcUtil = JdbcUtil.createDataSourceInstance(dataSourceMock);
		jdbcUtil.addQueries("/de/nikem/nest/jdbc/testQueries.xml");
		String query = jdbcUtil.getNamedQuery("test1");
		
		jdbcUtil.doWithoutTransaction(con -> {
			PreparedStatement stmt = con.prepareStatement(query);
			
			stmt.addBatch();	//darf keine Auswirkung auf Darstellung haben
			
			if (queryParameter instanceof String) {
				stmt.setString(1, (String) queryParameter);
			} else if (queryParameter instanceof Integer) {
				stmt.setInt(1, (Integer) queryParameter);
			} else if (queryParameter instanceof Date) {
				stmt.setDate(1, (Date) queryParameter);
			} else if (queryParameter instanceof Timestamp) {
				stmt.setTimestamp(1, (Timestamp) queryParameter);
			}  else {
				stmt.setNull(1, Types.CHAR);
			}
			String resultingQuery = ((PreparedStatementInvocationHandler) Proxy.getInvocationHandler(stmt)).buildQuery(query);
			Assert.assertThat(resultingQuery, CoreMatchers.is(jdbcUtil.getNamedQuery(resultQueryName)));
			
			return null;
		});
	}
}
