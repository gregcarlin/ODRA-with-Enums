package tests.wrapper;

import java.io.IOException;
import odra.cli.CLIVariable;
import odra.cli.batch.BatchException;
import odra.exceptions.rd.RDException;

/**
 * A local CLI specialization.
 * 
 * @author jacenty
 * @version 2007-11-02
 * @since 2007-01-30
 */
public class CLI extends odra.cli.CLI
{
	/**
	 * The constructor.
	 */
	CLI()
	{

	}
	
	@Override
	protected void execCm(String[] data) throws RDException, IOException
	{
		super.execCm(data);
	}

	@Override
	protected String execAddModuleAsWrapper(String[] data, String parmod) throws RDException, IOException
	{
		return super.execAddModuleAsWrapper(data, parmod);
	}

	@Override
	protected void initialize()
	{
		super.initialize();
		
		try
		{
			super.execSetOptimization(new String[] {Boolean.toString(false), "viewrewrite", "wrapperoptimize"});
			super.execSet(new String[] {CLIVariable.AUTODEREF.getName(), "off"});
			super.execBatch(new String[] {"res/wrapper/batch/init.cli"});
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
