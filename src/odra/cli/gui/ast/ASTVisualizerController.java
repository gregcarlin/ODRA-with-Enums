package odra.cli.gui.ast;

import odra.cli.CLI;
import odra.cli.CLIVariable;
import odra.sbql.optimizers.OptimizationSequence;

/**
 * AST visualizer controller.
 *
 * @author jacenty
 * @version 2007-10-09
 * @since 2007-06-08
 */
public class ASTVisualizerController
{
	/** AST visualizer frame */
	private ASTVisualizerFrame frame;
	/** referring CLI instance */
	private CLI cli;
	
	/**
	 * Opens the AST visualizer.
	 */
	public void openVisualizer(CLI cli)
	{
		this.cli = cli;
		
		if(frame == null)
			frame = new ASTVisualizerFrame(this);
		
		frame.setVisible(true);
	}
	
	/**
	 * Returns an associated CLI instance reference.
	 * 
	 * @return CLI
	 */
	CLI getCLI()
	{
		return cli;
	}
	
	/**
	 * Typechecks the given query and presents the resulting tree.
	 * 
	 * @param query query string
	 * @throws Exception 
	 */
	public void parseQuery(String query) throws Exception
	{
		frame.setParsedQuery(cli.execParseOnly(new String[] {cli.getVar(CLIVariable.SYNTAX), query}));
	}
	
	/**
	 * Typechecks the given query and presents the resulting tree.
	 * 
	 * @param query query string
	 * @throws Exception 
	 */
	public void typecheckQuery(String query) throws Exception
	{
		frame.setTypecheckedQuery(cli.execTypecheckOnly(new String[] {cli.getVar(CLIVariable.SYNTAX), cli.getVar(CLIVariable.AUTODEREF), query}));
	}
	
	/**
	 * Optmizes the given query and presents the resulting tree.
	 * 
	 * @param query query string
	 * @param sequence optimization sequence to be used
	 * @throws Exception 
	 */
	public void optimizeQuery(String query, OptimizationSequence sequence) throws Exception
	{
		String[] params = new String[sequence.size() + 3];
		params[0] = cli.getVar(CLIVariable.SYNTAX);
		params[1] = cli.getVar(CLIVariable.AUTODEREF);
		params[2] = query;
		for(int i = 0; i < sequence.size(); i++)
			params[i + 3] = sequence.get(i).getTypeName();
		
		frame.setOptimizedQuery(cli.execOptimizeOnly(params));
	}
}
