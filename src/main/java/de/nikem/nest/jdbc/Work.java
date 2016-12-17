package de.nikem.nest.jdbc;

import java.sql.Connection;

/**
 * Piece of work to be executed by {@link JdbcUtil}. Use this interface to operate on the JDBC {@link Connection} object.<br>
 * Please note that you are still responsible for closing statements and result sets.
 * 
 * @author andreas
 * 
 * @param <T>
 *            Type of return object
 */
public interface Work<T> {

	/**
	 * This method is invoked inside a JdbcUtil execution.
	 * 
	 * @param con
	 *            fully managed <code>Connection</code> object. Do not change autoCommit mode, commit, close nor rollback it.
	 * @return result of execution
	 * @throws Exception
	 *             if an error occurs
	 */
	T doWork(Connection con) throws Exception;
}
