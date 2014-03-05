package odra.ws.type.mappers;

import odra.exceptions.OdraException;


/***
 * Represents type mapper errors
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * @version 2007-06-23
 * @since 2007-03-25
 *
 */
public class TypeMapperException extends OdraException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6896154113004674169L;

	public TypeMapperException() {
		super("unknown");
	}
	
	public TypeMapperException(String msg) {
		super(msg);		
	}
	
	public TypeMapperException(String msg, Throwable inner)
	{
		super(msg, inner);
	}
	
	public TypeMapperException(Throwable inner)
	{
		super(inner);
	}

}
