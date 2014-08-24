package odra.sbql.optimizers;

import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.optimizers.costmodel.CostModel;

public class OptimizationFramework {
    private static final boolean FORCE_OPTIMIZATION = false; // ignore cost model and always use 'optimized' version of query
    
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
	    ASTNode oldQuery = DeepCopyAST.copy(query);
	    
		for(Type type : sequence) 
		{
			ISBQLOptimizer optimizer = OptimizationFactory.getOptimizer(type);
			optimizer.setStaticEval(staticEval);
			query = optimizer.optimize(query, module);
		}
		
		CostModel costModel = CostModel.getCostModel();
		double newEstimate = costModel.estimate(query, module);
		double oldEstimate = costModel.estimate(oldQuery, module, false);
		// TODO remove prints when finished
		System.out.println("OLD: " + oldQuery);
		System.out.println("NEW: " + query);
		System.out.printf("%f => %f%n", oldEstimate, newEstimate);
		// if the new query is faster, return it, otherwise return the old query
		if(FORCE_OPTIMIZATION || newEstimate <= oldEstimate) {
		    System.out.println("Using optimized version.");
		    return query;
		} else {
		    System.out.println("Using unoptimized version.");
		    return oldQuery;
		}
	}
}
