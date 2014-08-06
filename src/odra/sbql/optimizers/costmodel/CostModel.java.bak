package odra.sbql.optimizers.costmodel;

import java.util.Vector;

import odra.sbql.optimizers.queryrewrite.index.SingleIndexFitter;

/** 
 * 
 * This class is just a stub. Should be extended as a cost model for optimizations.
 * 
 * Now it provides simple "best" index selecting method
 * 
 * @author tkowals
 * 
 */


public class CostModel {

	private CostModel() {
		
	}
	
	public static CostModel getCostModel() {
		return new CostModel();
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
