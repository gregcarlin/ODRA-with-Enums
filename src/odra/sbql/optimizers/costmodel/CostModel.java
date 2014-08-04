package odra.sbql.optimizers.costmodel;

import java.util.Vector;

import odra.db.objects.data.DBModule;
import odra.sbql.ast.ASTNode;
import odra.sbql.optimizers.queryrewrite.index.SingleIndexFitter;

/** 
 * 
 * A cost model for optimizations and a "best" index selecting method.
 * 
 * @author tkowals, Greg Carlin
 * 
 */


public class CostModel {

	private CostModel() {
		
	}
	
	public static CostModel getCostModel() {
		return new CostModel();
	}
	
	/**
	 * Estimates the running time of a given query. Note that estimates are not absolute, but they can be compared.
	 * 
	 * @param query
	 * @param module
	 * @return
	 */
	public double estimate(ASTNode query, DBModule module) {
	    // TODO
	    return 0.0;
	}
	
	public double indexSelectivity(SingleIndexFitter index, boolean[] combination) {
		double selectivity = 1;
		for(Integer cardOfEqualCondKey: index.getEqualCondKeysCard(combination)) {
			if (cardOfEqualCondKey == Integer.MAX_VALUE)
				selectivity *= 0.02;
			else 
				selectivity *= 1.0 / (cardOfEqualCondKey);
		}
		for(Integer cardOfInCondKey: index.getInCondKeysCard(combination)) {
			if (cardOfInCondKey == Integer.MAX_VALUE)
				selectivity *= 0.1;
			else 
				selectivity *= 1.0 / (cardOfInCondKey); 
		}
		return selectivity * Math.pow(0.5, index.countRangeConditions(combination))
				* Math.pow(0.25, index.countLimitedRangeConditions(combination));
	}
	
	public int indexSelector(Vector<SingleIndexFitter> indices, boolean[] combination) {
		if (indices.size() == 1)
			return 0;
		
		// table of approximate indices selectivity 
		double[] idxsel = new double[indices.size()];
		
		int minIS = 0;
		
		for(int i = 0; i < indices.size(); i++) {
			idxsel[i] = indexSelectivity(indices.elementAt(i), combination);
			if (idxsel[i] < idxsel[minIS])
				minIS = i;
		}
		
		return minIS;
	}

	public double indicesAlternativeSelectivity(double selectivity1, double selectivity2) {
		return selectivity1 + selectivity2;
	}
	
	public static final double INDEXING_THRESHOLD_SELECTIVITY = 0.51;
	
}
