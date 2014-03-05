package odra.sbql.optimizers.queryrewrite.weakly.ROPMethod;

import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBEnum;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.queryrewrite.weakly.dependentquery.WeaklyDependentSubQuerySearcher;
import odra.sbql.results.compiletime.ValueSignature;
import odra.system.config.ConfigDebug;

public class SBQLRemovingObviousPartsQueryOptimizer extends TraversingASTAdapter implements ISBQLOptimizer{
	
	private int nameSuffix = 0;
	//TODO - possibly we should create and work on the copy of the original tree 
	private ASTNode root;
	
	//TODO - it should be better expressed!!
	private ASTAdapter staticEval;
	
	private WeaklyDependentSubQuerySearcher searcher ;
	
	public void setStaticEval(ASTAdapter staticEval) {
		this.staticEval = staticEval;
		
	}
	public void reset(){
		nameSuffix = 0; 
	}
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#optimize(odra.sbql.ast.ASTNode)
	 */
	public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException{
		if(ConfigDebug.ASSERTS) assert query != null: "query != null";
		this.root = query;
		try {
		    this.setSourceModuleName(module.getName());
		} catch (DatabaseException e) {
		    throw new OptimizationException(e,query,this);
		}
		query.accept(this, null);
		return root;
	}
	
	
	public Object visitEqualityExpression(EqualityExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyRemovingObviousPartsMethod(expr, attr);
		return null;
	}
	
	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyRemovingObviousPartsMethod(expr, attr);
		return null;
	}
	
	/** Perform optimization: 
	 * 1. calculate comparison of literals and change them into true or false
	 * 2. rewrite the query (change the AST) by removing obvious parts
	 * @param expr - simple binary AST node - context for the optimization process
	 * @param attr - visitor parameter - unused
 * @throws DatabaseException 
	 * @throws Exception
	 */
	private void applyRemovingObviousPartsMethod(BinaryExpression expr,
			Object attr) {
		if(expr instanceof EqualityExpression){
			EqualityExpression ee = (EqualityExpression)expr;
			checkEqualityExpression(ee);
			
		}
		else if(expr instanceof SimpleBinaryExpression){
			SimpleBinaryExpression sbe = (SimpleBinaryExpression)expr;
			checkSimpleBinaryExpression(sbe);
			
		}
		
	}
	

	private void checkSimpleBinaryExpression(SimpleBinaryExpression sbe) {
		
		boolean b=false;
		switch(sbe.O.getAsInt()){
			case Operator.AND:
				if((sbe.getLeftExpression() instanceof BooleanExpression && !((BooleanExpression)(sbe.getLeftExpression())).getLiteral().value())
						|| (sbe.getRightExpression() instanceof BooleanExpression && !((BooleanExpression)(sbe.getRightExpression())).getLiteral().value()))
					b=false;
				else if(sbe.getLeftExpression() instanceof BooleanExpression && sbe.getRightExpression() instanceof BooleanExpression){
					b=((BooleanExpression)(sbe.getLeftExpression())).getLiteral().value() && ((BooleanExpression)(sbe.getRightExpression())).getLiteral().value();
				}
				break;
			case Operator.OR:
				if((sbe.getLeftExpression() instanceof BooleanExpression && ((BooleanExpression)(sbe.getLeftExpression())).getLiteral().value())
						|| (sbe.getRightExpression() instanceof BooleanExpression && ((BooleanExpression)(sbe.getRightExpression())).getLiteral().value()))
					b=false;
				else if(sbe.getLeftExpression() instanceof BooleanExpression && sbe.getRightExpression() instanceof BooleanExpression){
					b=((BooleanExpression)(sbe.getLeftExpression())).getLiteral().value() || ((BooleanExpression)(sbe.getRightExpression())).getLiteral().value();
				}
				break;
		}
		BooleanLiteral bl = new BooleanLiteral(b);
		if(sbe.getParentExpression () != null){
			sbe.getParentExpression ().replaceSubexpr(sbe, new BooleanExpression(bl));
		}else {
			root = new BooleanExpression(bl);
		}
		
	}
	private void checkEqualityExpression(EqualityExpression ee) {
		
		if(ee.getLeftExpression().getSignature() instanceof ValueSignature){
			boolean b=false;
			if(ee.getLeftExpression() instanceof StringExpression && ee.getRightExpression() instanceof StringExpression){
				StringExpression sel = (StringExpression)ee.getLeftExpression();
				StringExpression ser = (StringExpression)ee.getRightExpression();
				b = sel.getLiteral().value().equals(ser.getLiteral().value());
			}
			else if(ee.getLeftExpression() instanceof IntegerExpression && ee.getRightExpression() instanceof IntegerExpression){
				IntegerExpression iel = (IntegerExpression)ee.getLeftExpression();
				IntegerExpression ier = (IntegerExpression)ee.getRightExpression();
				b = (iel.getLiteral().value()==ier.getLiteral().value());
			}
			BooleanLiteral bl = new BooleanLiteral(b);
			if(ee.getParentExpression () != null){
				ee.getParentExpression ().replaceSubexpr(ee, new BooleanExpression(bl));
			}else {
				root = new BooleanExpression(bl);
			}
		}
		
	}
/** Perform optimization: 
	 * 1. calculate comparison of literals and change them into true or false
	 * 2. rewrite the query (change the AST) by removing obvious parts
	 * @param expr - non-algebraic AST node - context for the optimization process
	 * @param attr - visitor parameter - unused
 * @throws DatabaseException 
	 * @throws Exception
	 */
	private void applyWeaklyDependentSubQueryMethod(NonAlgebraicExpression expr, Object attr) throws SBQLException{
		if(ConfigDebug.ASSERTS) assert expr.getEnvsInfo() != null : "the ENVS binding levels are not present in the AST";
		searcher = new WeaklyDependentSubQuerySearcher(expr); 
		Vector<Expression> exprParts = null;
		Expression subExpr = (Expression)expr.getRightExpression();
		subExpr.accept(searcher, attr);
		
		if(searcher.getVecEnums()!=null){
			Expression enu=searcher.getVecEnums().get(0);
			try {
				DBModule mod = Database.getModuleByName("admin.testm1");
				
				OID enuid = mod.findFirstByName(searcher.getStrEnum(), mod.getMetabaseEntry());
				MBEnum mbenu = new MBEnum(enuid);
				OID[] fields = mbenu.getFields();
				Expression newExpr=null;
				
				exprParts = new Vector<Expression>();
			
			IfThenElseExpression newExpr1=null;
			IfThenElseExpression newExpr2=null;
			IfThenElseExpression exprFinal=null;
			Expression exprElse=null;
			
			int cntr=0;
			
			for(int i=0;i<fields.length;i++)	{
				for(int j=0;j<searcher.getVecEnums().size();j++){
						Expression enumExpr = (Expression)BuilderUtils.deserializeAST(fields[i].derefBinary());
						searcher.getVecEnums().get(j).getParentExpression().replaceSubexpr(searcher.getVecEnums().get(j), enumExpr);
						searcher.getVecEnums().set(j,enumExpr);
					}
					if(cntr==0){
					
					newExpr1=new IfThenElseExpression(new EqualityExpression(enu,(Expression)BuilderUtils.deserializeAST(fields[i].derefBinary()),Operator.opEquals),(Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()),new BooleanExpression(new BooleanLiteral(false)));
					exprFinal=newExpr1;
					cntr++;
					}
					else{
						if(cntr==fields.length-1){
							newExpr1.setElseExpression((Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()));
							
						}
						else{
						
						newExpr2=new IfThenElseExpression(new EqualityExpression(enu,(Expression)BuilderUtils.deserializeAST(fields[i].derefBinary()),Operator.opEquals),(Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()),new BooleanExpression(new BooleanLiteral(false)));
						newExpr1.setElseExpression(newExpr2);
						newExpr1=newExpr2;newExpr2=null;
						}
						cntr++;
						}
						
					}
				
			
				//exprFinal = (Expression)DeepCopyAST.copyWithoutSign(exprFinal);
				expr.setRightExpression((Expression)DeepCopyAST.copyWithoutSign(exprFinal));
				
				root.accept(staticEval, null);
				
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	
		 
		
	}

	
	
	
}
