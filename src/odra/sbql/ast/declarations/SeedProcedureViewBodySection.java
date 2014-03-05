package odra.sbql.ast.declarations;

import odra.db.schema.OdraViewSchema;
import odra.sbql.ast.ParserException;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.Name;

/**
 * @author ksmialowicz
 * modifications radamus
 * 
 */
public class SeedProcedureViewBodySection extends ViewBodySection {

	private Statement statement;

	private Name name = new Name(OdraViewSchema.SEED_PROCEDURE_NAME);
	private ProcedureResult result;

	public SeedProcedureViewBodySection(ProcedureResult result, Statement statement) {
		this.statement = statement;
		this.result = result;

	}

	@Override
	public void putSelfInSection(ViewBody vb) throws ParserException {
		vb.addSeedProcedure(this);

	}

	/**
	 * @return the statement
	 */
	Statement getStatement() {
	    return statement;
	}

	/**
	 * @return the name
	 */
	Name getName() {
	    return name;
	}
	
	/**
	 * @return the result
	 */
	public ProcedureResult getResult() {
	    return result;
	}

	

	

}
