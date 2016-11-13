package de.nikem.nest.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <b>JdbcUtil</b> provides methods to conveniently access and use data base data. <b>JdbcUtil</b> uses either a {@link DataSource} or {@link DriverManager} to retrieve JDBC connections. You may use <b>JdbcUtil</b> as a singleton object.
 * <h3>Sample Usage:</h3>
 * <pre style="border: 1px solid gray; background: rgb(255,255,150)">
 * JdbcUtil jdbcUtil = JdbcUtil.createDataSourceInstance(getDataSource());
 * jdbcUtil.addQueries("/de/nikem/Queries.xml");
 * jdbcUtil.executeNamedQuery("selectSomething", null, new QueryParam("firstName", "Homer"), new QueryParam("lastName", "Simpson"));
 * </pre>
 * @author andreas
 * 
 */
public abstract class JdbcUtil {
	public static class ConnectionInfo {
		private final Connection con;
		private boolean transactionActive = false;
		public ConnectionInfo(Connection con) {
			this.con = con;
		}
		public Connection getCon() {
			return con;
		}
		public boolean isTransactionActive() {
			return transactionActive;
		}
		public void setTransactionActive(boolean transactionActive) {
			this.transactionActive = transactionActive;
		}
	}

	private static class DataSourceJdbcUtil extends JdbcUtil {
		private final DataSource dataSource;
		private DataSourceJdbcUtil(DataSource dataSource) {
			this.dataSource = dataSource;
		}
		@Override
		protected Connection getConnection() throws SQLException {
			return dataSource.getConnection();
		}
		public DataSource getDataSource() {
			return dataSource;
		}
	}

	private static class DriverJdbcUtil extends JdbcUtil {
		private class DriverManagerDataSource implements DataSource {
			private PrintWriter out;
			
			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				throw new UnsupportedOperationException("unwrap is not supported.");
			}
			
			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				throw new UnsupportedOperationException("unwrap is not supported.");
			}
			
			@Override
			public void setLoginTimeout(int seconds) throws SQLException {
				throw new UnsupportedOperationException("Login timeout is not supported.");
			}
			
			@Override
			public void setLogWriter(PrintWriter out) throws SQLException {
				this.out = out;
			}
			
			@Override
			public int getLoginTimeout() throws SQLException {
				throw new UnsupportedOperationException("Login timeout is not supported.");
			}
			
			@Override
			public PrintWriter getLogWriter() throws SQLException {
				return out;
			}
			
			@Override
			public Connection getConnection(String username, String password) throws SQLException {
				return DriverJdbcUtil.this.getConnection();
			}
			
			@Override
			public Connection getConnection() throws SQLException {
				return DriverJdbcUtil.this.getConnection();
			}

			@Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				throw new SQLFeatureNotSupportedException("parent logger is not supported.");
			}
		};
		
		
		private final Properties info;
		private final String password;
		private final String url;
		private final String user;
		
		private final DataSource dataSource;

		private DriverJdbcUtil(String url, String user, String password, Properties info) {
			this.url = url;
			this.user = user;
			this.password = password;
			this.info = info;
			this.dataSource = new DriverManagerDataSource();
		}
		@Override
		protected Connection getConnection() throws SQLException {
			if (user == null && info == null) {
				return DriverManager.getConnection(url);
			}
			if (info == null) {
				return DriverManager.getConnection(url, user, password);
			}
			return DriverManager.getConnection(url, info);
		}
		
		@Override
		public DataSource getDataSource() {
			return dataSource; 
		}
	}

	private class QueryParser extends DefaultHandler {
		private StringBuilder query = new StringBuilder();
		private String queryName = null;
		private final Stack<String> tagStack = new Stack<String>();

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if ("query".equals(tagStack.peek())) {
				query.append(new String(ch, start, length));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("query".equals(tagStack.pop())) {
				String queryString = query.toString().trim();
				log.fine(queryName + ": " + queryString);
				queryString = queryMap.put(queryName, queryString);
				if (queryString != null) {
					log.warning("duplicate queries for name " + queryName + ". Query replaced: " + queryString);
				}
				query.setLength(0);
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("query".equals(tagStack.push(qName))) {
				queryName = attributes.getValue("name");
			}
		}
	}

	private static class TransactionWork<T> implements Work<T> {
		private final Work<T> delegate;

		public TransactionWork(Work<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T doWork(Connection con) throws SQLException {
			ConnectionInfo info = threadConnection.get();
			boolean myTransaction = false;
			T result;
			try {

				if (!info.isTransactionActive()) {
					con.setAutoCommit(false);
					info.setTransactionActive(true);
					myTransaction = true;
				}

				result = delegate.doWork(con);

				if (myTransaction) {
					log.fine("Commit Connection for Thread " + Thread.currentThread().getName());
					con.commit();
				}
			} catch (SQLException e) {
				if (myTransaction) {
					log.fine("Rollback Connection for Thread " + Thread.currentThread().getName());
					rollback(con);
				}
				NikemJdbcException ex = new NikemJdbcException(e);
				log.throwing(getClass().getName(), "doInTransaction", ex);
				throw ex;
			} catch (RuntimeException e) {
				if (myTransaction) {
					log.fine("Rollback Connection for Thread " + Thread.currentThread().getName());
					rollback(con);
				}
				log.throwing(getClass().getName(), "doInTransaction", e);
				throw e;
			}
			return result;
		}
	}

	private static final Logger log = Logger.getLogger(JdbcUtil.class.getName());

	private static final Pattern SQL_PARAM_PATTERN = Pattern.compile("(?i):[a-zA-z0-9_]+");

	private static final ThreadLocal<ConnectionInfo> threadConnection = new ThreadLocal<ConnectionInfo>() {
		protected ConnectionInfo initialValue() {
			return null;
		};
	};

	/**
	 * Close a connection safely without throwing any exception.
	 * 
	 * @param con
	 *            the connection to close (may be <code>null</code>)
	 */
	public static void close(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Connection cannot be closed.", e);
			}
		}
	}

	/**
	 * Close a result set safely without throwing any exception.
	 * 
	 * @param resultSet
	 *            the result set to close (may be <code>null</code>)
	 */
	public static void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				log.log(Level.SEVERE, "ResultSet cannot be closed.", e);
			}
		}
	}

	/**
	 * Close a statement safely without throwing any exception.
	 * 
	 * @param statement
	 *            the statement to close (may be <code>null</code>)
	 */
	public static void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Statement cannot be closed.", e);
			}
		}
	}

	/**
	 * Create a <code>JdbcUtil</code> that uses a DataSource to create / retrieve the connections
	 * 
	 * @param dataSource
	 *            the <code>DataSource</code> to use for connection creation
	 * @return new <code>JdbcUtil</code> instance
	 * @see DataSource
	 */
	public static JdbcUtil createDataSourceInstance(DataSource dataSource) {
		return new DataSourceJdbcUtil(dataSource);
	}

	/**
	 * Create a <code>JdbcUtil</code> that uses the DriverManager to create / retrieve the connections
	 * 
	 * @param url
	 *            a database url of the form <code>jdbc:subprotocol:subname</code>
	 * @return new <code>JdbcUtil</code> instance
	 * @see DriverManager
	 */
	public static JdbcUtil createDriverInstance(String url) {
		return new DriverJdbcUtil(url, null, null, null);
	}

	/**
	 * Create a <code>JdbcUtil</code> that uses the DriverManager to create / retrieve the connections
	 * 
	 * @param url
	 *            a database url of the form <code>jdbc:subprotocol:subname</code>
	 * @param info
	 *            list of arbitrary string tag/value pairs as connection arguments; normally at least a "user" and "password" property should be included
	 * @return new <code>JdbcUtil</code> instance
	 * @see DriverManager
	 */
	public static JdbcUtil createDriverInstance(String url, Properties info) {
		return new DriverJdbcUtil(url, null, null, info);
	}

	/**
	 * Create a <code>JdbcUtil</code> that uses the DriverManager to create / retrieve the connections
	 * 
	 * @param url
	 *            a database url of the form <code>jdbc:subprotocol:subname</code>
	 * @param user
	 *            the database user on whose behalf the connection is being made
	 * @param password
	 *            the user's password
	 * @return new <code>JdbcUtil</code> instance
	 * @see DriverManager
	 */
	public static JdbcUtil createDriverInstance(String url, String user, String password) {
		return new DriverJdbcUtil(url, user, password, null);
	}

	public static void rollback(Connection con) {
		if (con != null) {
			try {
				con.rollback();
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Connection cannot be rolled back.", e);
			}
		}
	}

	private Map<String, String> queryMap = new HashMap<String, String>();

	private JdbcUtil() {
	}

	public void addQueries(String resourceName) {
		InputStream is = getClass().getResourceAsStream(resourceName);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			saxParser.parse(is, new QueryParser());
		} catch (ParserConfigurationException e) {
			throw new NikemJdbcException("Bad parser configuration", e);
		} catch (SAXException e) {
			throw new NikemJdbcException("Bad XML input stream", e);
		} catch (IOException e) {
			throw new NikemJdbcException("Exception accessing resource " + resourceName, e);
		}
	}

	protected Object changeToSqlDatatype(QueryParam param) {
		log.fine(param.toString());
		Object result = param.getValue();
		if (result instanceof Date && !(result instanceof java.sql.Date) && !(result instanceof java.sql.Time)
				&& !(result instanceof java.sql.Timestamp)) {
			log.fine(param + ": replace java.util.Date by java.sql.Timestamp");
			result = new Timestamp(((Date) result).getTime());
		}
		return result;
	}

	/**
	 * Execute a piece of work using a transaction. If there is an existing transaction associated with the current thread use the existing transaction. If not create one and bind it to the current thred. The <code>Connection</code> object
	 * is provided to the {@link Work#doWork(Connection)} method.
	 * 
	 * @param <T> Type of the result
	 * @param work
	 *            piece of work to be executed in an transaction
	 * @return result of the work execution
	 */
	public <T> T doInTransaction(Work<T> work) {
		return doWithoutTransaction(new TransactionWork<T>(work));
	}

	/**
	 * Execute a piece of work. The <code>Connection</code> object is provided to the {@link Work#doWork(Connection)} method.
	 * 
	 * @param <T> Type of the result
	 * @param work
	 *            piece of work to be executed
	 * @return result of the work execution
	 */
	public <T> T doWithoutTransaction(Work<T> work) {
		ConnectionInfo info = threadConnection.get();
		Connection con = null;
		boolean myConnection = false;
		T result;
		try {
			if (info == null) {
				log.fine("Retrieve Connection for Thread " + Thread.currentThread().getName());
				con = getConnection();
				info = new ConnectionInfo(con);
				threadConnection.set(info);
				myConnection = true;
			} else {
				log.fine("Reuse Connection for Thread " + Thread.currentThread().getName());
				con = info.getCon();
			}

			result = work.doWork(con);

		} catch (SQLException e) {
			NikemJdbcException ex = new NikemJdbcException(e);
			log.throwing(getClass().getName(), "doWithoutTransaction", ex);
			throw ex;
		} catch (RuntimeException e) {
			log.throwing(getClass().getName(), "doWithoutTransaction", e);
			throw e;
		} finally {
			if (myConnection) {
				log.fine("Close Connection for Thread " + Thread.currentThread().getName());
				threadConnection.remove();
				close(con);
			}
		}
		return result;
	}

	/**
	 * Execute a named query and return the complete result of the query execution as a list of row maps.
	 * 
	 * @param queryName
	 *            name of the query in the query xml file.
	 * @param preprocessQueryParams Query parameters that will be <b>replaced</b> in the query source string
	 * @param queryParams
	 *            named parameters for the query execution
	 * @return result as a list of &lt;uppercase ColumnName, ColumnValue&gt; maps per each row.
	 */
	public List<Map<String, ?>> executeNamedQuery(final String queryName, final QueryParam[] preprocessQueryParams, final QueryParam... queryParams) {
		final String queryString = getNamedQuery(queryName);
		return doWithoutTransaction(new Work<List<Map<String, ?>>>() {
			@Override
			public List<Map<String, ?>> doWork(Connection con) throws SQLException {
				PreparedStatement stmt = null;
				ResultSet resultSet = null;
				List<Map<String, ?>> result = new ArrayList<Map<String, ?>>();
				try {
					stmt = prepareStatement(con, queryString, preprocessQueryParams, queryParams);
					resultSet = stmt.executeQuery();
					while (resultSet.next()) {
						result.add(resultSetRowToMap(resultSet));
					}
				} finally {
					close(resultSet);
					close(stmt);
				}
				return result;
			}
		});
	}

	/**
	 * Execute a named query that does not return a result set. (E.g. INSERT, UPDATE, DELETE)
	 * 
	 * @param queryName
	 *            name of the query in the query xml file.
	 * @param preprocessQueryParams Query parameters that will be <b>replaced</b> in the query source string
	 * @param queryParams
	 *            named parameters for the query execution.
	 * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
	 * @see PreparedStatement#executeUpdate()
	 */
	public int executeUpdateNamedQuery(final String queryName, final QueryParam[] preprocessQueryParams, final QueryParam... queryParams) {
		final String queryString = getNamedQuery(queryName);

		return doInTransaction(new Work<Integer>() {
			@Override
			public Integer doWork(Connection con) throws SQLException {
				PreparedStatement stmt = null;
				try {
					stmt = prepareStatement(con, queryString, preprocessQueryParams, queryParams);
					return stmt.executeUpdate();
				} finally {
					close(stmt);
				}
			}
		});
	}

	private String replacePreprocessQueryParams(String queryString, final QueryParam[] preprocessQueryParams) {
		Map<String, QueryParam> preprocessQueryParamMap = new HashMap<String, QueryParam>();

		if (preprocessQueryParams != null) {
			for (QueryParam p : preprocessQueryParams) {
				preprocessQueryParamMap.put(p.getName(), p);
			}
		}

		int[] recursionCounter = new int[] {0};
		String resultQueryString = replacePreprocessQueryParams(queryString,
				preprocessQueryParamMap, recursionCounter);
		return resultQueryString;
	}

	protected String replacePreprocessQueryParams(String queryString, Map<String, QueryParam> preprocessQueryParamMap, int[] recursionCounter) {
		recursionCounter[0] += 1;
		if (recursionCounter[0] > 10) {
			throw new NikemJdbcException("Zu viele Preprocess-Rekursionen in Query-String: " + queryString);
		}
		
		Matcher matcher = SQL_PARAM_PATTERN.matcher(queryString);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String paramName = matcher.group().substring(1); // cut colon
			QueryParam queryParam = preprocessQueryParamMap.get(paramName);
			if (queryParam != null) {
				String valueString = "";
				Object value = queryParam.getValue();
				if (value != null) {
					valueString = value.toString().trim();
				}
				
				matcher.appendReplacement(sb, valueString);
			} else {
				//dann ist es wohl ein echter Query-Param -> stehen lassen
				matcher.appendReplacement(sb, matcher.group());
			}

		}
		matcher.appendTail(sb);
		String resultQueryString = sb.toString();
		if (!queryString.equals(resultQueryString)) {
			resultQueryString = replacePreprocessQueryParams(resultQueryString, preprocessQueryParamMap, recursionCounter);
		}
		return resultQueryString;
	}
	
	protected abstract Connection getConnection() throws SQLException;
	public abstract DataSource getDataSource();

	protected String parameterMarkers(int size) {
		StringBuffer sb = new StringBuffer(size * 2);
		for (int i = 0; i < size; i++) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append('?');
		}
		return sb.toString();
	}

	/**
	 * Create a <code>PreparedStatement</code> object using a connection and named query parameters
	 * 
	 * @param con
	 *            the connection to use for the statement
	 * @param queryString
	 *            query string containing named parameters (e.g. <code>:id</code>)
	 * @param preprocessQueryParams Query parameters that will be <b>replaced</b> in the query source string
	 * @param queryParams
	 *            parameter name and value objects
	 * @return a prepared statement ready to be executed
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public PreparedStatement prepareStatement(Connection con, String queryString, QueryParam[] preprocessQueryParams, QueryParam... queryParams) throws SQLException {
		final Map<String, QueryParam> queryParamMap = new LinkedHashMap<String, QueryParam>();
		for (QueryParam param : queryParams) {
			queryParamMap.put(param.getName(), param);
		}
		return prepareStatementMap(con, queryString, preprocessQueryParams, queryParamMap);
	}

	protected PreparedStatement prepareStatementMap(Connection con, String queryString, QueryParam[] preprocessQueryParams, Map<String, QueryParam> queryParams) throws SQLException {
		String tmpString = replacePreprocessQueryParams(queryString, preprocessQueryParams);
		List<QueryParam> paramList = new ArrayList<QueryParam>();

		Matcher matcher = SQL_PARAM_PATTERN.matcher(tmpString);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String paramName = matcher.group().substring(1); // cut colon
			QueryParam queryParam = queryParams.get(paramName);
			if (queryParam == null)
				throw new NikemJdbcException("Missing parameter for " + paramName);
			paramList.add(queryParam); // put parameters in the sequence of occurence in query
			Object value = queryParam.getValue();
			if (value != null) {
				if (value.getClass().isArray()) {
					if (value.getClass().getComponentType().isPrimitive()) {
						throw new UnsupportedOperationException("Please do not use arrays of primitives in de.nikem.jdbc.QueryParam");
					} else {
						Object[] coll = (Object[]) value;
						matcher.appendReplacement(sb, parameterMarkers(coll.length));
					}
				} else if (value instanceof Collection<?>) {
					Collection<?> coll = (Collection<?>) value;
					matcher.appendReplacement(sb, parameterMarkers(coll.size()));
				} else {
					matcher.appendReplacement(sb, "?");
				}
			} else {
				matcher.appendReplacement(sb, "?");
			}
		}
		matcher.appendTail(sb);
		tmpString = sb.toString();
		log.fine(tmpString);
		PreparedStatement stmt = con.prepareStatement(tmpString);
		Iterator<QueryParam> it = paramList.iterator();
		for (int i = 0; it.hasNext(); i++) {
			boolean isParameterList = false;
			Object value = changeToSqlDatatype(it.next());
			if (value != null) {
				if (value.getClass().isArray()) {
					if (value.getClass().getComponentType().isPrimitive()) {
						throw new UnsupportedOperationException("Please do not use primitive Arrays in de.nikem.jdbc.QueryParam");
					} else {
						for (Object item : (Object[]) value) {
							if (isParameterList)
								i++;
							stmt.setObject(i + 1, item);
							isParameterList = true;
						}
					}
				} else if (value instanceof Iterable) {
					Iterable<?> iterable = (Iterable<?>) value;
					for (Object item : iterable) {
						if (isParameterList)
							i++;
						stmt.setObject(i + 1, item);
						isParameterList = true;
					}
				} else {
					stmt.setObject(i + 1, value);
				}
			} else {
				stmt.setObject(i + 1, value);
			}
		}
		return stmt;
	}

	/**
	 * Create a &lt;uppercase ColumnName, ColumnValue&gt; map of the current result set row.
	 * 
	 * @param resultSet
	 *            result set pointing to the current row.
	 * @return created row map
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public Map<String, ?> resultSetRowToMap(ResultSet resultSet) throws SQLException {
		Map<String, Object> row = new LinkedHashMap<String, Object>();
		ResultSetMetaData metaData = resultSet.getMetaData();
		for (int index = 1; index <= metaData.getColumnCount(); index++) {
			row.put(metaData.getColumnName(index).toUpperCase(), resultSet.getObject(index));
		}
		return row;
	}

	/**
	 * Retrieve the query String from the SQL queries file
	 * 
	 * @param name
	 *            name of the query
	 * @return query string
	 */
	public String getNamedQuery(String name) {
		String queryString = queryMap.get(name);
		if (queryString == null)
			throw new NikemJdbcException("No query found for name " + name);
		return queryString;
	}
}
