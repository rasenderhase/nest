package de.nikem.nest.jdbc;

public class NikemJdbcException extends RuntimeException {
	private static final long serialVersionUID = -6247574473964131602L;

	public NikemJdbcException()
	{
		super();
	}

	public NikemJdbcException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NikemJdbcException(String message)
	{
		super(message);
	}

	public NikemJdbcException(Throwable cause)
	{
		super(cause);
	}

}
