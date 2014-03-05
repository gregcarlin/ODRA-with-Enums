package odra.cli.ast;

import odra.cli.CLISyntaxErrorException;
import odra.cli.parser.CLIASTVisitor;

/**
 * Conditional command
 * 
 * @author jacenty
 * @version 2007-07-04
 * @since 2007-07-04
 */
public class ConditionalCommand extends Command
{
	/** checked condition */
	private final SimpleCommand condition;
	/** command to execute */
	private final Command execution;
	
	/**
	 * The constructor.
	 * 
	 * @param condition checked condition
	 * @param execution command to execute
	 * @throws CLISyntaxErrorException 
	 */
	public ConditionalCommand(SimpleCommand condition, Command execution) throws CLISyntaxErrorException
	{
		if(condition.cmdid != SimpleCommand.EXISTS_CMD)
			throw new CLISyntaxErrorException("Provide logical condition for the first argument.");
		
		this.condition = condition;
		this.execution = execution;
	}

	@Override
	public Object accept(CLIASTVisitor vis, Object attr) throws Exception
	{
		return vis.visitConditionalCommand(this, attr);
	}

	/**
	 * Returns the condition.
	 * 
	 * @return condtion
	 */
	public SimpleCommand getCondition()
	{
		return condition;
	}

	/**
	 * Returns the command to execute.
	 * 
	 * @return command to execute
	 */
	public Command getExecution()
	{
		return execution;
	}
}
