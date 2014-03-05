package odra.sbql.optimizers;

import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;

public interface ISBQLOptimizer {
	ASTNode optimize(ASTNode query, DBModule module) throws SBQLException;
	void setStaticEval(ASTAdapter staticEval);
	void reset();
}
