package tests.review.demo.objects;

import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.Result;
import tests.review.demo.Database;

/**
 * A country container.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2008-01-29
 */
public class Country
{
	private final String code;
	
	private final String name;
	
	public Country(String code, Database db) throws JOBCException
	{
		this.code = code;
		
		Result result = db.execute("countries where code = \"" + code + "\"");
		Result fields = result.fields();
		name = fields.getByName("name").getString();
	}

	public String getCode()
	{
		return code;
	}

	public String getName()
	{
		return name;
	}
}
