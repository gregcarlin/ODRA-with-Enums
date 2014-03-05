package odra.sbql.ast.utils.patterns;

/**
 * Interface provides method to check if given object matches criteria implemented
 * 
 * @author tkowals
 */

public interface Pattern {
	
	boolean matches(Object obj);
	
}
