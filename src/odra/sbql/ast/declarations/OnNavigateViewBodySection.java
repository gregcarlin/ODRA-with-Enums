package odra.sbql.ast.declarations;

import odra.sbql.ast.ParserException;
import odra.sbql.ast.statements.Statement;

public class OnNavigateViewBodySection extends ViewBodySection {

	private Statement statement;
	
	public OnNavigateViewBodySection(Statement statement) {		
		this.statement = statement;
	}
	@Override
	public void putSelfInSection(ViewBody vb) throws ParserException {
		vb.addOnNavigate(this);

	}
	/**
	 * @return the statement
	 */
	public Statement getStatement() {
	    return statement;
	}
}
