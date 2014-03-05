package odra.sbql.optimizers;

/**
 * Optimizer types.
 * 
 * @author jacenty
 * @version 2007-02-19
 * @since 2007-02-12
 */
public enum Type
{
	NONE("none"),
	
	INDEPENDENT_SUB_QUERY("independent"),
	WEAKLY_DEPENDENT_INVOLVING_LSC("lscweaklydependent"),
	WEAKLY_DEPENDENT_SUB_QUERY("weaklydependent"),
	OBVIOUS_PARTS("obviousparts"),
	DEAD_SUB_QUERY("dead"),
	UNION_DISTRIBUTIVE("union"),
	WRAPPER_REWRITE("wrapperrewrite"),
	WRAPPER_OPTIMIZE("wrapperoptimize"),
	REWRITE("rewrite"),
	INDEX("index"),
	UNSTRICTVIEWREWRITE("unstrictviewrewrite"),
	VIEWREWRITE("viewrewrite"),
	PARALLEL("parallel"),
	DISTRIBUTED("distributed"),
	AUXNAMES("auxnames");
	
	private final String type;
	
	Type(String type)
	{
		this.type = type;
	}
	
	public String getTypeName()
	{
		return type;
	}
	
	public static Type getTypeForString(String type)
	{
		if(type.equals(NONE.getTypeName()))
			return NONE;
		else if(type.equals(WEAKLY_DEPENDENT_SUB_QUERY.getTypeName()))
			return WEAKLY_DEPENDENT_SUB_QUERY;
		else if(type.equals(WEAKLY_DEPENDENT_INVOLVING_LSC.getTypeName()))
			return WEAKLY_DEPENDENT_INVOLVING_LSC;
		else if(type.equals(INDEPENDENT_SUB_QUERY.getTypeName()))
			return INDEPENDENT_SUB_QUERY;
		else if(type.equals(OBVIOUS_PARTS.getTypeName()))
			return OBVIOUS_PARTS;
		else if(type.equals(DEAD_SUB_QUERY.getTypeName()))
			return DEAD_SUB_QUERY;
		else if(type.equals(UNION_DISTRIBUTIVE.getTypeName()))
			return UNION_DISTRIBUTIVE;
		else if(type.equals(WRAPPER_REWRITE.getTypeName()))
			return WRAPPER_REWRITE;
		else if(type.equals(WRAPPER_OPTIMIZE.getTypeName()))
			return WRAPPER_OPTIMIZE;
		else if(type.equals(REWRITE.getTypeName()))
			return REWRITE;
		else if(type.equals(INDEX.getTypeName()))
			return INDEX;
		else if(type.equals(VIEWREWRITE.getTypeName()))
			return VIEWREWRITE;
		else if (type.equals(UNSTRICTVIEWREWRITE.getTypeName()))
			return UNSTRICTVIEWREWRITE;
		else if (type.equals(DISTRIBUTED.getTypeName()))
			return DISTRIBUTED;
		else if (type.equals(PARALLEL.getTypeName()))
			return PARALLEL;
		else if (type.equals(AUXNAMES.getTypeName()))
			return AUXNAMES;
		else
			throw new AssertionError("Unknown optimization type '" + type + "'.");
	}
}
