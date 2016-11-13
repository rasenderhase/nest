package de.nikem.nest.jdbc;

/**
 * named query parameter
 * 
 * @author andreas
 * 
 */
public class QueryParam {

	private final String name;
	private final Object value;

	/**
	 * Constructs a named query parameter
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 */
	public QueryParam(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "QueryParam [name=" + name + ", value=" + value + "]";
	}
}
