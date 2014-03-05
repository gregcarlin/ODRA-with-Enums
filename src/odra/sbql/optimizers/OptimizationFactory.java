package odra.sbql.optimizers;

import odra.sbql.optimizers.queryrewrite.auxiliaryNames.AuxiliaryNamesRemover;
import odra.sbql.optimizers.queryrewrite.deadquery.SBQLDeadQueryOptimizer;
import odra.sbql.optimizers.queryrewrite.distributed.DistributedQueryOptimizer;
import odra.sbql.optimizers.queryrewrite.distributed.parallel.DistributedParallelQueryOptimizer;
import odra.sbql.optimizers.queryrewrite.independentquery.SBQLIndependentQueryOptimizer;
import odra.sbql.optimizers.queryrewrite.index.IndexOptimizer;
import odra.sbql.optimizers.queryrewrite.procedures.ProcedureRewriter;
import odra.sbql.optimizers.queryrewrite.unionquery.SBQLUnionDistributiveOptimizer;
import odra.sbql.optimizers.queryrewrite.viewrewrite.UnstrictViewRewriter;
import odra.sbql.optimizers.queryrewrite.viewrewrite.ViewRewriter;
import odra.sbql.optimizers.queryrewrite.weakly.ROPMethod.SBQLRemovingObviousPartsQueryOptimizer;
import odra.sbql.optimizers.queryrewrite.weakly.dependentquery.SBQLWeaklyDependentQueryOptimizer;
import odra.sbql.optimizers.queryrewrite.weakly.dependentquery.LSCMethod.SBQLWeaklyDependentQueryLSCOptimizer;
import odra.sbql.optimizers.queryrewrite.wrapper.WrapperOptimizer;
import odra.sbql.optimizers.queryrewrite.wrapper.WrapperRewriter;

/**
 * OptimizersFactory
 * @author radamus
 *last modified: 2007-02-19
 *@version 1.0
 */
public class OptimizationFactory { 
	public static ISBQLOptimizer getOptimizer(Type type) throws OptimizationException {
		if(type.equals(Type.INDEPENDENT_SUB_QUERY))
			return new SBQLIndependentQueryOptimizer();
		else if(type.equals(Type.WEAKLY_DEPENDENT_SUB_QUERY))
			return new SBQLWeaklyDependentQueryOptimizer();
		else if(type.equals(Type.WEAKLY_DEPENDENT_INVOLVING_LSC))
			return new SBQLWeaklyDependentQueryLSCOptimizer();
		else if(type.equals(Type.OBVIOUS_PARTS))
			return new SBQLRemovingObviousPartsQueryOptimizer();
		else if(type.equals(Type.DEAD_SUB_QUERY))
			return new SBQLDeadQueryOptimizer();
		else if(type.equals(Type.UNION_DISTRIBUTIVE))
			return new SBQLUnionDistributiveOptimizer();
		else if(type.equals(Type.WRAPPER_REWRITE))
			return new WrapperRewriter();
		else if(type.equals(Type.WRAPPER_OPTIMIZE))
			return new WrapperOptimizer();
		else if(type.equals(Type.REWRITE))
			return new ProcedureRewriter();
		else if(type.equals(Type.INDEX)) 
			return new IndexOptimizer();
		else if(type.equals(Type.VIEWREWRITE)) 
			return new ViewRewriter();
		else if(type.equals(Type.UNSTRICTVIEWREWRITE)) 
			return new UnstrictViewRewriter();
		else if(type.equals(Type.DISTRIBUTED))
			return new DistributedQueryOptimizer();
		else if(type.equals(Type.PARALLEL))
			return new DistributedParallelQueryOptimizer();
		else if(type.equals(Type.AUXNAMES))
			return new AuxiliaryNamesRemover();
		
		return null;
	}
}
