package tests.wrapper;

import java.io.IOException;
import odra.cli.CLIVariable;
import odra.exceptions.rd.RDException;

/**
 * A local CLI specialization.
 * 
 * @author jacenty
 * @version 2007-11-02
 * @since 2007-11-02
 */
public class SwardCLI extends CLI
{
	@Override
	protected void initialize()
	{
		super.initialize();
		
		try
		{
			super.execSetOptimization(new String[] {Boolean.toString(false), "viewrewrite", "wrapperoptimize"});
			super.execSet(new String[] {CLIVariable.AUTODEREF.getName(), "off"});
		}
		catch (IOException exc)
		{
			exc.printStackTrace();
		}
		catch (RDException exc)
		{
			exc.printStackTrace();
		}
	}
	
}
