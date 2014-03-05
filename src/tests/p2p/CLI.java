package tests.p2p;

import java.io.IOException;
import odra.cli.CLIVariable;
import odra.cli.batch.BatchException;
import odra.exceptions.rd.RDException;

/**
 * A local CLI specialization.
 * 
 * @author kamil
 * @version 2007-08-20
 * @since 2007-01-30
 */
public class CLI extends odra.cli.CLI
{
	@Override
	protected void execCm(String[] data) throws RDException, IOException
	{
		super.execCm(data);
	}


	@Override
	protected void initialize()
	{
		super.initialize();
		
		try
		{
			super.execSetOptimization(new String[] {Boolean.toString(false), "viewrewrite", "wrapperoptimize"});
			super.execSet(new String[] {CLIVariable.AUTODEREF.getName(), "on"});
			super.execBatch(new String[] {"res/p2p/cmu.cli"});
		}
		catch (IOException exc)
		{
			exc.printStackTrace();
		}
		catch (RDException exc)
		{
			exc.printStackTrace();
		}
		catch (BatchException exc)
		{
			exc.printStackTrace();
		}
	}
	
}
