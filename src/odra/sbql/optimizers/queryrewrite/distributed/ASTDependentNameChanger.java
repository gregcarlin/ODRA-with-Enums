package odra.sbql.optimizers.queryrewrite.distributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.NameExpression;

/**
 * @author janek
 * 
 */
public class ASTDependentNameChanger extends TraversingASTAdapter
{
	private int nameSuffix = 0;
	private ArrayList<NameExpression> parmDependentNames;
	Map<String, String> nameLookup = new HashMap<String, String>();

	public ASTDependentNameChanger(ArrayList<NameExpression> parmDependentNames)
	{
		super();
		this.parmDependentNames = parmDependentNames;
	}

	@Override
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException
	{

		if (parmDependentNames == null)
			return null;
		
		String newName = null;
		String oldName = expr.name().value();
	
		for (NameExpression nx : parmDependentNames)
		{
			if (nx.name().value().equals(oldName) && oldName.startsWith("$aux"))
			{
				newName = "remote_parm_aux" + "_" + nameSuffix++;

				nameLookup.put(oldName, newName);
				expr.name().setValue(newName);
			}
		}

		return null;
	}

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException
	{

		String oldName = expr.name().value();

		if (oldName.startsWith("$aux") && nameLookup.containsKey(oldName))
			expr.name().setValue(nameLookup.get(oldName));

		return null;
	}

}
