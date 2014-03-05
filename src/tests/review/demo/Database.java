package tests.review.demo;

import java.io.IOException;
import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.Result;
import odra.sbql.optimizers.Type;

/**
 * A query executor intermadiate class.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2008-01-30
 */
public class Database
{
	private JOBC db = new JOBC("admin", "admin", "localhost", 1521);
	
	Database()
	{
		
	}
	
	void openConnection() throws JOBCException
	{
		db.connect();
    db.setCurrentModule("admin.integration");
    db.setOptimization(new String[] {Type.NONE.getTypeName(), Type.VIEWREWRITE.getTypeName(), Type.AUXNAMES.getTypeName(), Type.WRAPPER_OPTIMIZE.getTypeName()});
	}
	
	void closeConnection() throws IOException
	{
		if(db.isConnected())
			db.close();
	}

	public Result execute(String queryString) throws JOBCException
	{
		System.out.println(queryString);
		return db.execute(queryString);
	}
}
