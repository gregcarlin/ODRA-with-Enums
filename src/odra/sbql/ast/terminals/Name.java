package odra.sbql.ast.terminals;

/**
 * AST node for names in the source code (e.g. john, cat).
 * 
 * @author raist
 */

public class Name extends Terminal {
    private String V;

    public Name(String v) {
	V = v;
    }

    /**
     * @param v
     *                the v to set
     */
    public final void setValue(String v)
    {
	V = v;
    }

    /**
     * @return the string valu
     */
    public final String value()
    {
	return V;
    }
    
    public static final Name EMPTY_NAME = new Name("");
    public static final Name DEFAULT_ARGUMENT_NAME = new Name("value");
}