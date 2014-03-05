package odra.cli;

import java.nio.charset.Charset;

/**
 * CLI variables' definition.
 * 
 * @author jacenty
 * @version 2007-07-05
 * @since 2007-02-16
 */
public enum CLIVariable
{
	AUTODEREF("autoderef", new String[] {"off", "on"}, "on"),
	TEST("test", new String[] {"off", "plain", "plaintimes", "compare", "comparesimple"}, "off"),
	OUTPUT("output", new String[] {"default", "xml"}, "xml"),
	SYNTAX(CLI.SYNTAX_CLI_VAR, new String[] {CLI.SYNTAX_TYPE_SBQL, CLI.SYNTAX_TYPE_OCL}, CLI.SYNTAX_TYPE_SBQL),
	ENCODING("encoding", new String[] {Charset.defaultCharset().displayName().toLowerCase(), "utf-8", "iso-8859-1", "iso-8859-2", "cp1250", "cp1251", "Cp1252"}, Charset.defaultCharset().displayName().toLowerCase());
		
	private final String name;
	private final String[] states;
	private final String defaultState;
	
	CLIVariable(String name, String[] states, String defaultState)
	{
		this.name = name;
		this.states = states;
		
		if(!isStateValid(defaultState))
			throw new AssertionError("A default state '" + defaultState + "' is not defined for variable '" + name + "'.");
		
		this.defaultState = defaultState;
	}
	
	public static CLIVariable getVariableForName(String name)
	{
		for(CLIVariable variable : values())
			if(variable.name.equals(name))
				return variable;
		
		throw new AssertionError("Unknown variable name: '" + name + "'.");
	}
	
	public String getName()
	{
		return name;
	}
	
	public String[] getStates()
	{
		return states;
	}
	
	public String getDefaultState()
	{
		return defaultState;
	}
	
	public boolean isStateValid(String state)
	{
		for(String s : states)
			if(state.equals(s))
				return true;
		
		return false;
	}
}
