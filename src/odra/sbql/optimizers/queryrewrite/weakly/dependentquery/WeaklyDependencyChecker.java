package odra.sbql.optimizers.queryrewrite.weakly.dependentquery;

import java.util.HashMap;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBVariable;
import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.system.config.ConfigDebug;

public class WeaklyDependencyChecker extends TraversingASTAdapter {

	private NonAlgebraicExpression context; //non algebraic operator for which we check
											//whether the name is weakly dependent
	private Vector<Expression> vecEnums=null;
	private Expression enumExpr=null;
	private String strEnum=null;
	private boolean isWeaklyDependent;
	private boolean isAnotherEnum;
	
	
	private HashMap<String, Vector<Expression>> hmEnums;

	/**
	 * @param context - non-algebraic operator - the context for dependency 
	 * checking process
	 */
	public WeaklyDependencyChecker(NonAlgebraicExpression context) {
		if(ConfigDebug.ASSERTS) assert context.getEnvsInfo() != null : "the ENVS binding levels are not present in the AST";
		this.context = context;
		this.isWeaklyDependent=true;
		this.isAnotherEnum=false;
		
		this.hmEnums = new HashMap<String,Vector<Expression>>();

	}
	
	public Object visitDerefExpression(DerefExpression expr, Object attr)
		throws SBQLException {
		return commonVisitUnaryExpression(expr, attr);
	}
	
	public Object visitNameExpression(NameExpression expr, Object attr)
	throws SBQLException {

	//if name was bound in the environment created by the 'context' non-algebraic operator
	//the name is not independent
	try {
	  int cntrParent=0;
	  boolean itWasAdded=false;
	 if ((expr.getBindingInfo().boundat >= context.getEnvsInfo().baseEnvsSize) && (expr.getBindingInfo().boundat < context.getEnvsInfo().baseEnvsSize + context.getEnvsInfo().framesOpened)){
		 
		 Expression wholeExpr = getWholeEnumExpr(expr);
		 
		if(wholeExpr.getSignature() instanceof ReferenceSignature){
		ReferenceSignature rsig=(ReferenceSignature)wholeExpr.getSignature();
		
			if(new MBVariable(rsig.value).isValid() && new MBVariable(rsig.value).isTypeEnum()){ 
			 	
				
					//Expression enumExpr=getWholeEnumExpr(expr);
					if(isUnderNonAlgebraic(wholeExpr)){
						String enumName = new MBEnum(new MBVariable(rsig.value).getType()).getName();
						if(hmEnums.containsKey(enumName))
							hmEnums.get(enumName).add(wholeExpr);
						else{
							Vector<Expression> ve = new Vector<Expression>();
							ve.add(wholeExpr);
							hmEnums.put(enumName, ve);
						}
						itWasAdded=true;
	                }
					else this.isWeaklyDependent=false;
						
			}
			else this.isWeaklyDependent=false;								
	
	}
		
	 // if(!itWasAdded) this.isWeaklyDependent=false;
	}
		
	} catch (DatabaseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}return null;
}
	
	private boolean isDependent(Expression expr) {
		Expression ne=expr;
		while(ne instanceof DotExpression){
			ne=((DotExpression)ne).getLeftExpression();
		}	
		//if(ne instanceof DotExpression){
			//ne=((DotExpression)ne).getLeftExpression();
		//Expression[] e=ne.flatten();	
		//}
		NameExpression enu=(NameExpression)ne;
		if((enu.getBindingInfo().boundat >= context.getEnvsInfo().baseEnvsSize) && (enu.getBindingInfo().boundat < context.getEnvsInfo().baseEnvsSize + context.getEnvsInfo().framesOpened))
			return true;
		
		return false;
	}

	private boolean isUnderNonAlgebraic(Expression expr) {
		Expression nonAlgebraicOperator=expr;
		while(nonAlgebraicOperator.getParentExpression()!=context){
			if(nonAlgebraicOperator.getParentExpression() instanceof NonAlgebraicExpression && nonAlgebraicOperator.getParentExpression().getSignature().getMaxCard()>1){
				return true;
			}
			nonAlgebraicOperator=nonAlgebraicOperator.getParentExpression();
		}
		
		return false;
	}
	
	private Expression getWholeEnumExpr(NameExpression expr){
		Expression e=expr;
		while(e.getParentExpression() instanceof DotExpression)
			e=e.getParentExpression();
		return e;
	}


	public boolean isWeaklyDependent()
	{
		return isWeaklyDependent;
	}
	
	public HashMap<String, Vector<Expression>> getHmEnums(){
		return this.hmEnums;
	}
}
