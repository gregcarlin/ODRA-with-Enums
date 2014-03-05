package odra.sbql.ast.declarations;

import odra.sbql.ast.ParserException;
import odra.sbql.ast.statements.Statement;

/**
 * @author ksmialowicz
 * modifications radamus
 * 
 */
public class OnRetrieveViewBodySection extends ViewBodySection {
    	 
	private Statement statement;

	public OnRetrieveViewBodySection( Statement statement) {
		this.statement = statement;
	}

	@Override
	public void putSelfInSection(ViewBody vb) throws ParserException {
		vb.addOnRetrieve(this);

	}

	


	/**
	 * @return the statement
	 */
	public Statement getStatement() {
	    return statement;
	}

}
