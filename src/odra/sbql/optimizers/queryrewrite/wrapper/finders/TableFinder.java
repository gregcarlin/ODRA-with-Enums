package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import java.util.Hashtable;

import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.TablePattern;
import odra.wrapper.Wrapper;

/**
 * A class for searching for tables.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-03-02
 */
public class TableFinder extends ASTNodeFinder
{
	/**
	 * The constructor.
	 * 
	 * @param wrappers wrappers referenced by a query
	 */
	public TableFinder(Hashtable<String, Wrapper> wrappers)
	{
		super(new TablePattern(wrappers), true);
	}
}
