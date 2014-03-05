package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.ParserException;
import odra.sbql.ast.terminals.Name;

/**
 * @author ksmialowicz
 * modifications radamus
 * 
 */
public class ViewDeclaration extends Declaration {

    	private String viewName;
    	
	private ViewBody body;

	public ViewDeclaration(Name name, ViewBody body) throws ParserException {
			
		if(name.equals(Name.EMPTY_NAME))
		    viewName = body.getSeedProcedure().getName() + "Def";
		else
		    viewName = name.value();
		this.body = body;

	}
	
	
	
	
	
	/**
	 * @return the viewName
	 */
	public String getViewName() {
	    return viewName;
	}




	/**
	 * @return the body
	 */
	public ViewBody getBody() {
	    return body;
	}




	/**
	 * <p>
	 * <u>(non-Javadoc)</u>
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "name: " + viewName + "\nbody:\n" + body.toString();
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitViewDeclaration(this, attr);
	}
}
