package odra.sbql.optimizers.queryrewrite.weakly.dependentquery;

import java.awt.Color;
import java.util.HashMap;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBVariable;
import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.StructExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBagExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.ToRealExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.ToStringExpression;
import odra.sbql.ast.expressions.TransitiveClosureExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.system.config.ConfigDebug;


public class WeaklyDependentSubQuerySearcher extends TraversingASTAdapter {
	
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
	public WeaklyDependentSubQuerySearcher(NonAlgebraicExpression context) {
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
		//if ((expr.getBindingInfo().boundat >= context.getEnvsInfo().baseEnvsSize) && (expr.getBindingInfo().boundat < context.getEnvsInfo().baseEnvsSize + context.getEnvsInfo().framesOpened))
			if(expr.getSignature() instanceof ReferenceSignature){
				ReferenceSignature rsig=(ReferenceSignature)expr.getSignature();
				
					if(new MBVariable(rsig.value).isValid() && new MBVariable(rsig.value).isTypeEnum()){ //do tego elsa i booleana na false
					 	
						
							Expression enumExpr=getWholeEnumExpr(expr);
							if(isUnderNonAlgebraic(enumExpr) && isDependent(enumExpr)){
								String enumName = new MBEnum(new MBVariable(rsig.value).getType()).getName();
								if(hmEnums.containsKey(enumName))
									hmEnums.get(enumName).add(enumExpr);
								else{
									Vector<Expression> ve = new Vector<Expression>();
									ve.add(enumExpr);
									hmEnums.put(enumName, ve);
								}
			                }		
								
					}
														
			
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

	public Vector<Expression> getVecEnums(){
		return vecEnums;
	}
	
	public boolean isAnotherEnum(){
		return isAnotherEnum;
	}
	
	public boolean isWeaklyDependent(){
		return isWeaklyDependent;
	}
	
	public String getStrEnum(){
		return strEnum;
	}
	
	public HashMap<String,Vector<Expression>> getHMEnums(){
		return hmEnums;
	}

	
}