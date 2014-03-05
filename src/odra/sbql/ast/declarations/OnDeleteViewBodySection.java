package odra.sbql.ast.declarations;

import odra.sbql.ast.ParserException;
import odra.sbql.ast.statements.Statement;

/**
 * @author ksmialowicz
 * 
 */
public class OnDeleteViewBodySection extends ViewBodySection {

	private Statement statement;

	public OnDeleteViewBodySection(Statement statement) {
		this.statement = statement;
	}

	@Override
	public void putSelfInSection(ViewBody vb) throws ParserException {
		vb.addOnDelete(this);

	}
	

	/**
	 * @return the statement
	 */
	public Statement getStatement() {
	    return statement;
	}

}
