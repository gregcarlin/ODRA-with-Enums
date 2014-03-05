package odra.sbql.ast.declarations;

import odra.sbql.ast.ParserException;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.Name;

/**
 * @author ksmialowicz
 * changes radamus
 * 
 */
public class OnUpdateViewBodySection extends ViewBodySection {
    
	private Statement statement;
	private Name param;


	public OnUpdateViewBodySection(Name paramName, Statement statement) {
		this.statement = statement;
		this.param = paramName;

	}

	@Override
	public void putSelfInSection(ViewBody vb) throws ParserException {
		vb.addOnUpdate(this);

	}

	

	/**
	 * @return the statement
	 */
	public Statement getStatement() {
	    return statement;
	}

	/**
	 * @return the parameter name
	 */
	public Name getParamName() {
	    return param;
	}

}
