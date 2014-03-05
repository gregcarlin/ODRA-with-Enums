package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import java.util.Hashtable;

import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.utils.patterns.Pattern;
import odra.wrapper.Wrapper;
import odra.wrapper.model.Name;

/**
 * A local pattern implementation for tables finding.
 * 
 * @author jacenty
 * @version 2007-07-23
 * @since 2007-03-02
 */
public class TablePattern implements Pattern
{
	/** wrappers of a query */
	private Hashtable<String, Wrapper> wrappers;
	
	/**
	 * The constructor.
	 * 
	 * @param wrappers wrappers of a query
	 */
	public TablePattern(Hashtable<String, Wrapper> wrappers)
	{
		this.wrappers = wrappers;
	}
	
	public boolean matches(Object obj)
	{
		if(obj instanceof NameExpression)
		{
			for(Wrapper wrapper : wrappers.values())
				if(wrapper.getModel().containsTable(Name.o2r(((NameExpression)obj).name().value())))
					return true;
		}
		
		return false;
	}
}
