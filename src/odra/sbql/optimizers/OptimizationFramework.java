package odra.sbql.optimizers;

import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;

public class OptimizationFramework {
	private OptimizationSequence sequence = new OptimizationSequence();
	ASTAdapter staticEval;
	
	public OptimizationFramework(ASTAdapter staticEval) {
		this.staticEval = staticEval;
	}
	
	/**
	 * Adds a new optimization type to the sequence
	 * 
	 * @param typeName optimization type name
	 * @throws OptimizationException type name not recognized or conflict detected
	 * 
	 * @author jacenty
	 */
	public void add(String typeName) throws OptimizationException {
		try
		{
			Type type = Type.getTypeForString(typeName);
			sequence.addType(type);
		}
		catch(AssertionError err)
		{
			throw new OptimizationException("Unknown optimization type: " + typeName);
		}
	}
	
	/**
	 * Adds a new optimization type to the sequence
	 * 
	 * @param type optimization type
	 * @throws OptimizationException conflict detected
	 * 
	 * @author jacenty
	 */
	public void add(Type type) throws OptimizationException{
		sequence.addType(type);
	}
	
	/**
	 * Sets the current optimization sequence.
	 * 
	 * @param sequence optimizer sequence
	 * 
	 * @author jacenty
	 */
	public void setOptimizationSequence(OptimizationSequence sequence) {
		this.sequence = sequence;
	}
	
	public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException {
		for(Type type : sequence) 
		{
			ISBQLOptimizer optimizer = OptimizationFactory.getOptimizer(type);
			optimizer.setStaticEval(staticEval);
			query = optimizer.optimize(query, module);
		}
		return query;
	}
}
