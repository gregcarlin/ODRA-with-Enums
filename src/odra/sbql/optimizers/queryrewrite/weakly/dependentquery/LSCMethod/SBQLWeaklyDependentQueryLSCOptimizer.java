package odra.sbql.optimizers.queryrewrite.weakly.dependentquery.LSCMethod;

import java.util.Enumeration;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBEnum;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.LazyFailureExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.queryrewrite.weakly.dependentquery.WeaklyDependentSubQuerySearcher;
import odra.system.config.ConfigDebug;


/**
 * SBQLWeaklyDependentQueryLSCOptimizer - class implementing 
 * optimization method involving large and small collection
 * @author blejam
 */

public class SBQLWeaklyDependentQueryLSCOptimizer extends TraversingASTAdapter implements ISBQLOptimizer{
	
	private int nameSuffix = 0;
	//TODO - possibly we should create and work on the copy of the original tree 
	private ASTNode root;
	
	private String globalModuleName;
	
	private DBModule module;
	
	//TODO - it should be better expressed!!
	private ASTAdapter staticEval;
	
	private WeaklyDependentSubQueryLSCSearcher searcher ;
	
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
		    this.globalModuleName = module.getModuleGlobalName();
		    this.module = module;
		} catch (DatabaseException e) {
		    throw new OptimizationException(e,query,this);
		}
		query.accept(this, null);
		return root;
	}
	
	public Object visitDotExpression(DotExpression expr, Object attr)
	throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryLSCMethod(expr, attr);
		return null;
	}

	public Object visitForAllExpression(ForAllExpression expr, Object attr)
	throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryLSCMethod(expr, attr);
		return null;
	}

	public Object visitForSomeExpression(ForSomeExpression expr, Object attr)
	throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryLSCMethod(expr, attr);
		return null;
	}

	public Object visitJoinExpression(JoinExpression expr, Object attr)
	throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryLSCMethod(expr, attr);
		return null;
	}



	public Object visitOrderByExpression(OrderByExpression expr, Object attr)
	throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryLSCMethod(expr, attr);
		return null;
	}

	public Object visitWhereExpression(WhereExpression expr, Object attr)
	throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryLSCMethod(expr, attr);
		return null;
	}


/** Perform optimization: 
* 1. search for the right-hand subqueries of non-algebraic operator that depend on it
     only on expression returning a collection whose size is small in comparison to the 
     collection size returned by the left-hand subquery of the operator
* 2. rewrite the query (change the AST)
* @param expr - non-algebraic AST node - context for the optimization process
* @param attr - visitor parameter - unused
* @throws DatabaseException 
* @throws Exception
*/
private void applyWeaklyDependentSubQueryLSCMethod(NonAlgebraicExpression expr, Object attr) throws SBQLException{
	
	try{
		
		if(ConfigDebug.ASSERTS) assert expr.getEnvsInfo() != null : "the ENVS binding levels are not present in the AST";
		searcher = new WeaklyDependentSubQueryLSCSearcher(expr,globalModuleName); 
		Expression wds = (Expression)expr.getRightExpression().accept(searcher, attr);
		//int i=1;
		int cntrJ=0;
		boolean wasEnum=false;
		Expression expr1,etmp,joinExpr,rWhereExpr = null,rWhereTmp,lJoinExpr = null;
		Expression lExpr=null,rExpr=null;
		Name name1,name2,name3;
		
		if(wds!=null && !searcher.dependentParts.isEmpty()){
			Enumeration keys = searcher.dependentParts.keys();
			while(keys.hasMoreElements()){
				String key = keys.nextElement().toString();
				name1=new Name("$aux"+nameSuffix++);
					OID oid = module.findFirstByName(key, module.getMetabaseEntry());
					if(new MBEnum(oid).isValid()){
						MBEnum mbenum = new MBEnum(oid);
						OID[] efields=mbenum.getFields();
						etmp=null;
						for(int j=0;j<efields.length;j++){
							Expression enumExpr = (Expression)BuilderUtils.deserializeAST(efields[j].derefBinary());
							if(j==0){
								etmp=enumExpr;
							}
							else{
									enumExpr=new CommaExpression(etmp,enumExpr); 
									etmp=enumExpr;
									
								}
						}
							
						expr1=new AsExpression(new BagExpression(etmp),name1); //bag(....) as aux1
						wasEnum=true;
						
						
					}
					else{
						expr1=new AsExpression(new NameExpression(new Name(key.toString())),name1);//Dept as aux1
					}
					//i++;
					
					if(wasEnum){
						lExpr=searcher.dependentParts.get(key).elementAt(0);
						rExpr=new NameExpression(name1);
					}
					else{
						lExpr=((DotExpression)(searcher.dependentParts.get(key).elementAt(0))).getLeftExpression();
						rExpr=new RefExpression(new NameExpression(name1));
					}
					
					wasEnum=false;
					
					if(cntrJ==0){
						lJoinExpr=expr1;
						//rWhereTmp=new EqualityExpression((Expression)(DeepCopyAST.copyWithoutSign(searcher.dependentParts.get(key).elementAt(0))),new NameExpression(name1),Operator.opEquals);
						rWhereTmp=new EqualityExpression((Expression)(DeepCopyAST.copyWithoutSign(lExpr)),rExpr,Operator.opEquals);
						
						rWhereExpr=rWhereTmp; //e.worksIn.Dept=aux1
						cntrJ++;
					}
					else{
						expr1=new JoinExpression(lJoinExpr,expr1);
						lJoinExpr=expr1;
						
						rWhereTmp=new EqualityExpression((Expression)(DeepCopyAST.copyWithoutSign(lExpr)),rExpr,Operator.opEquals);
						//rWhereExpr=new IntersectExpression(rWhereExpr,rWhereTmp);
						rWhereExpr=new SimpleBinaryExpression(rWhereExpr,rWhereTmp, Operator.opAnd);
						
						
					}
					
					for(Expression e1:searcher.dependentParts.get(key)){
						e1.getParentExpression().replaceSubexpr(e1,new NameExpression(name1));
						
					}
					
					
				}
				name2=new Name("$rjaux"+nameSuffix++);
				//joinExpr=new JoinExpression(lJoinExpr,new AsExpression(new LazyFailureExpression((Expression)(DeepCopyAST.copyWithoutSign(wds))),name2));
				joinExpr=new JoinExpression(lJoinExpr,new AsExpression((Expression)(DeepCopyAST.copyWithoutSign(wds)),name2));
				
				name3=new Name("$jaux"+nameSuffix++);
				wds.getParentExpression().replaceSubexpr(wds, new DotExpression(new WhereExpression(new NameExpression(name3),rWhereExpr),new NameExpression(name2)));
				
				if(expr.getParentExpression () != null){
					expr.getParentExpression ().replaceSubexpr(expr, new DotExpression(new GroupAsExpression(joinExpr,name3) ,expr));
				}else {
					root = new DotExpression(new GroupAsExpression(joinExpr,name3) ,expr);
				}
				
				root.accept(staticEval, null);
				//DotExpression dotExpr=new DotExpression(new GroupAsExpression(joinExpr,name3),expr);
				String vv="ll";
				
				
				//NameExpression ne=new NameExpression(new Name(keys.nextElement().toString()));
				//AsExpression ase=new AsExpression(ne,new Name("aux "+i));
				
				
			}
		
		
			//NameExpression sc=new NameExpression(new Name(searcher.dependentParts.keys().nextElement()));
			//AsExpression asc=new AsExpression(sc,new Name("d"));
			//JoinExpression jExpr=new JoinExpression(asc,);
		
		//dependent part from searcher
		//while(subExpr != null){ //TODO recurrent optimization 
		//}
		
	} catch (SBQLException se) {
		// TODO Auto-generated catch block
		se.printStackTrace();
	}
	catch (DatabaseException de) {
		// TODO Auto-generated catch block
		de.printStackTrace();
	}
	
}
	



}
