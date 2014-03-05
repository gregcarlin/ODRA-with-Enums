package odra.sbql.optimizers.queryrewrite.weakly.dependentquery;

import java.util.ArrayList;
import java.util.Set;
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
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.system.config.ConfigDebug;

public class SBQLWeaklyDependentQueryOptimizer extends TraversingASTAdapter implements ISBQLOptimizer{
	
	private int nameSuffix = 0;
	//TODO - possibly we should create and work on the copy of the original tree 
	private ASTNode root;
	
	//TODO - it should be better expressed!!
	private ASTAdapter staticEval;
	
	private WeaklyDependentSubQuerySearcher searcher ;
	private StandaloneWeaklyDependentSubQuerySearcher standAloneSearcher ;
	
	private String globalModuleName;
	
	private DBModule module;
	
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
		applyWeaklyDependentSubQueryMethod(expr, attr);
		return null;
	}

	public Object visitForAllExpression(ForAllExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryMethod(expr, attr);
		return null;
	}

	public Object visitForSomeExpression(ForSomeExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryMethod(expr, attr);
		return null;
	}

	public Object visitJoinExpression(JoinExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryMethod(expr, attr);
		return null;
	}

	

	public Object visitOrderByExpression(OrderByExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryMethod(expr, attr);
		return null;
	}

	public Object visitWhereExpression(WhereExpression expr, Object attr)
			throws SBQLException {
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		applyWeaklyDependentSubQueryMethod(expr, attr);
		return null;
	}


/** Perform optimization: 
	 * 1. search for the weakly dependent query
	 * 2. rewrite the query (change the AST)
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
		
		//new code
		if(!searcher.getHMEnums().isEmpty()){
			try {
			   
			    Object[] arrayOfEnums=searcher.getHMEnums().keySet().toArray();
			    Object[] arrayOfEnumsOccurrences=searcher.getHMEnums().values().toArray();
			    
			    ArrayList<Vector> valuesOfEnums = new ArrayList<Vector>();
			    
			    String firstEnum=arrayOfEnums[0].toString();
			    OID firstEnumOID = module.findFirstByName(firstEnum, module.getMetabaseEntry());
			    MBEnum firstMBEnum = new MBEnum(firstEnumOID);
			    OID[] fieldsOfFirstEnum = firstMBEnum.getFields();
			    
			    
			    for(OID oid:fieldsOfFirstEnum){
			    	Vector<Expression> ve = new Vector<Expression>();
			    	ve.add((Expression)BuilderUtils.deserializeAST(oid.derefBinary()));
			    	valuesOfEnums.add(ve);
			    }
			    
			    for(int i=1;i<arrayOfEnums.length;i++){
			    	ArrayList<Vector> tmpValuesOfEnums = new ArrayList<Vector>();
			    	MBEnum ithMBEnum = new MBEnum(module.findFirstByName(arrayOfEnums[i].toString(), module.getMetabaseEntry()));
				    OID[] fieldsOfIthEnum = ithMBEnum.getFields();
				    for(Vector<Expression> veofEnums:valuesOfEnums){
				    	for(OID oid:fieldsOfIthEnum){
				    		Vector<Expression> nVE= new Vector<Expression>();
				    		for(Expression e:veofEnums)
				    			nVE.add(e);
				    		nVE.add((Expression)BuilderUtils.deserializeAST(oid.derefBinary()));
				    		tmpValuesOfEnums.add(nVE);
				    	}
				    }
				    valuesOfEnums=tmpValuesOfEnums;
				    tmpValuesOfEnums=null;
			    }
			    
			    Vector<Expression> subqueriesForIf = new Vector<Expression>();
			    
			    
			    
			    /*for(Vector<Expression> valueOfEnum:valuesOfEnums){//male,analyst
			    	for(int i=0;i<valueOfEnum.size();i++){//male
			    		int j=0;
			    		for(Expression e:(Vector<Expression>)(arrayOfEnumsOccurrences[i])){
			    			e.getParentExpression().replaceSubexpr(e, valueOfEnum.get(i));
			    			((Vector<Expression>)(arrayOfEnumsOccurrences[i])).set(j, valueOfEnum.get(i));j++;
			    			//e=valueOfEnum.get(i);
			    		}
			    					    		
			    	}
			    	
			    	
			    
			    	subqueriesForIf.add((Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()));
			    
			    }*/
			    //nc2
			    
			    IfThenElseExpression newExpr1=null;
				IfThenElseExpression newExpr2=null;
				IfThenElseExpression exprFinal=null;
				Expression exprElse=null;
				Expression condExpr=null;
				Expression tmpValueOfEnum=null;
				
				Vector<Expression> enumExpr = new Vector<Expression>();
				for(int i=0;i<arrayOfEnumsOccurrences.length;i++){
					enumExpr.add(((Vector<Expression>)(arrayOfEnumsOccurrences[i])).firstElement());
				}
			    
			    for(int k=0;k<valuesOfEnums.size();k++){	
			    	Vector<Expression> valueOfEnum=valuesOfEnums.get(k);//male,analyst
			    	for(int i=0;i<valueOfEnum.size();i++){//male valueofEnum to Vector
			    		int j=0;
			    		//Vector<Expression> ve=(Vector<Expression>)(arrayOfEnumsOccurrences[i]);
			    		for(Expression e:(Vector<Expression>)(arrayOfEnumsOccurrences[i])){
			    			//e za 2 razem pod femalem jest
			    			//e.getParentExpression().replaceSubexpr(e, valueOfEnum.get(i));
			    			//((Vector<Expression>)(arrayOfEnumsOccurrences[i])).set(j, valueOfEnum.get(i));j++;
			    			Expression voe = (Expression)DeepCopyAST.copyWithoutSign(valueOfEnum.get(i));
			    			e.getParentExpression().replaceSubexpr(e, voe);
			    			((Vector<Expression>)(arrayOfEnumsOccurrences[i])).set(j, voe);j++;
			    			
			    			
			    		}
			    		//tmpValueOfEnum=valueOfEnum.get(i);
			    		if(i==0){
			    			
			    			condExpr=new EqualityExpression(enumExpr.get(i),(Expression)DeepCopyAST.copyWithoutSign(valueOfEnum.get(i)),Operator.opEquals);
			    			
			    		}
			    		else{
			    			condExpr=new IntersectExpression(condExpr,new EqualityExpression(enumExpr.get(i),(Expression)DeepCopyAST.copyWithoutSign(valueOfEnum.get(i)),Operator.opEquals));
			    		}
			    	}
			    	//tmpValueOfEnum=null;
			    	if(k==0){
			    		newExpr1=new IfThenElseExpression(condExpr,(Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()),new BooleanExpression(new BooleanLiteral(false)));
						exprFinal=newExpr1;
						
			    	}
			    	else{
						if(k==valuesOfEnums.size()-1){
							newExpr1.setElseExpression((Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()));
							
						}
						else{
						
						newExpr2=new IfThenElseExpression(condExpr,(Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()),new BooleanExpression(new BooleanLiteral(false)));
						newExpr1.setElseExpression(newExpr2);
						newExpr1=newExpr2;newExpr2=null;
						}
					}
			    	condExpr=null;
			    	//subqueriesForIf.add((Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()));
			    
			    }
			    //nc2
			    
			    expr.setRightExpression((Expression)DeepCopyAST.copyWithoutSign(exprFinal));
				
				root.accept(staticEval, null);
			    
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		//
		
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

	
	private void applyStandaloneWeaklyDependentSubQueryMethod(NonAlgebraicExpression expr, Object attr) throws SBQLException{
		if(ConfigDebug.ASSERTS) assert expr.getEnvsInfo() != null : "the ENVS binding levels are not present in the AST";
		standAloneSearcher = new StandaloneWeaklyDependentSubQuerySearcher(expr); 
		Vector<Expression> exprParts = null;
		//Expression subExpr = (Expression)expr.getRightExpression();
		//subExpr.accept(standAloneSearcher, attr);
		Expression subExpr = (Expression)expr.getRightExpression().accept(standAloneSearcher, attr);
		//new code
		if(subExpr!=null && !standAloneSearcher.hmEnums.isEmpty()){
			try {
			   
			    Object[] arrayOfEnums=standAloneSearcher.hmEnums.keySet().toArray();//nazwy enumów
			    Object[] arrayOfEnumsOccurrences=standAloneSearcher.hmEnums.values().toArray();//wystąpienia enumów
			    
			    ArrayList<Vector> valuesOfEnums = new ArrayList<Vector>();
			    
			    String firstEnum=arrayOfEnums[0].toString();
			    OID firstEnumOID = module.findFirstByName(firstEnum, module.getMetabaseEntry());
			    MBEnum firstMBEnum = new MBEnum(firstEnumOID);
			    OID[] fieldsOfFirstEnum = firstMBEnum.getFields();
			    
			    
			    for(OID oid:fieldsOfFirstEnum){
			    	Vector<Expression> ve = new Vector<Expression>();
			    	ve.add((Expression)BuilderUtils.deserializeAST(oid.derefBinary()));
			    	valuesOfEnums.add(ve);
			    }
			    
			    for(int i=1;i<arrayOfEnums.length;i++){
			    	ArrayList<Vector> tmpValuesOfEnums = new ArrayList<Vector>();
			    	MBEnum ithMBEnum = new MBEnum(module.findFirstByName(arrayOfEnums[i].toString(), module.getMetabaseEntry()));
				    OID[] fieldsOfIthEnum = ithMBEnum.getFields();
				    for(Vector<Expression> veofEnums:valuesOfEnums){
				    	for(OID oid:fieldsOfIthEnum){
				    		Vector<Expression> nVE= new Vector<Expression>();
				    		for(Expression e:veofEnums)
				    			nVE.add(e);
				    		nVE.add((Expression)BuilderUtils.deserializeAST(oid.derefBinary()));
				    		tmpValuesOfEnums.add(nVE);
				    	}
				    }
				    valuesOfEnums=tmpValuesOfEnums;
				    tmpValuesOfEnums=null;
			    } //dotąd kartezjan wartości enumów, 
			    
			    Vector<Expression> subqueriesForIf = new Vector<Expression>();
			    
			    
			    
			
			    //nc2
			    
			    IfThenElseExpression newExpr1=null;
				IfThenElseExpression newExpr2=null;
				IfThenElseExpression exprFinal=null;
				Expression exprElse=null;
				Expression condExpr=null;
				Expression tmpValueOfEnum=null;
				Expression newExpr3=null;
				Expression commaExpr=null;
				Name auxName;
				
				Vector<Expression> enumExpr = new Vector<Expression>();
				for(int i=0;i<arrayOfEnumsOccurrences.length;i++){
					enumExpr.add(((Vector<Expression>)(arrayOfEnumsOccurrences[i])).firstElement());
				}
			    
			    for(int k=0;k<valuesOfEnums.size();k++){	
			    	Vector<Expression> valueOfEnum=valuesOfEnums.get(k);//male,analyst
			    	for(int i=0;i<valueOfEnum.size();i++){//male
			    		int j=0;
			    		//Vector<Expression> ve=(Vector<Expression>)(arrayOfEnumsOccurrences[i]);
			    		for(Expression e:(Vector<Expression>)(arrayOfEnumsOccurrences[i])){
			    			
			    			e.getParentExpression().replaceSubexpr(e, valueOfEnum.get(i));
			    			((Vector<Expression>)(arrayOfEnumsOccurrences[i])).set(j, valueOfEnum.get(i));j++;
			    			
			    		} //w tym forze zamiana np. wszystkich e.sex na male
			    		tmpValueOfEnum=valueOfEnum.get(i);
			    		if(i==0){
			    			
			    			condExpr=new EqualityExpression(enumExpr.get(i),(Expression)DeepCopyAST.copyWithoutSign(valueOfEnum.get(i)),Operator.opEquals);
			    			
			    		}
			    		else{
			    			condExpr=new IntersectExpression(condExpr,new EqualityExpression(enumExpr.get(i),(Expression)DeepCopyAST.copyWithoutSign(valueOfEnum.get(i)),Operator.opEquals));
			    		}
			    	}// po tym np. (e.sex=male and e.job=analyst) 
			    	tmpValueOfEnum=null;
			    	if(k==0){
			    		auxName=new Name("aux"+k);
			    		commaExpr=new GroupAsExpression((Expression)DeepCopyAST.copyWithoutSign(subExpr),auxName);
			    		
			    		
			    		//newExpr1=new IfThenElseExpression(condExpr,(Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()),new BooleanExpression(new BooleanLiteral(false)));
			    		newExpr1=new IfThenElseExpression(condExpr,new NameExpression(auxName),new BooleanExpression(new BooleanLiteral(false)));
						
			    		exprFinal=newExpr1;
						
						
						
			    	}
			    	else{
						if(k==valuesOfEnums.size()-1){
							auxName=new Name("aux"+k);
							commaExpr=new CommaExpression(commaExpr,new GroupAsExpression((Expression)DeepCopyAST.copyWithoutSign(subExpr),auxName));
							newExpr1.setElseExpression(new NameExpression(auxName));
							
						}
						else{
							auxName=new Name("aux"+k);
							commaExpr=new CommaExpression(commaExpr,new GroupAsExpression((Expression)DeepCopyAST.copyWithoutSign(subExpr),auxName));
							
						newExpr2=new IfThenElseExpression(condExpr,new NameExpression(auxName),new BooleanExpression(new BooleanLiteral(false)));
						newExpr1.setElseExpression(newExpr2);
						newExpr1=newExpr2;newExpr2=null;
						}
					}
			    	condExpr=null;
			    	//subqueriesForIf.add((Expression)DeepCopyAST.copyWithoutSign(expr.getRightExpression()));
			    
			    }
			    //nc2
			    subExpr.getParentExpression().replaceSubexpr(subExpr, exprFinal);
			    
			    //expr.setRightExpression((Expression)DeepCopyAST.copyWithoutSign(exprFinal));
			    if(expr.getParentExpression () != null){
					expr.getParentExpression ().replaceSubexpr(expr, new DotExpression(commaExpr ,(Expression)DeepCopyAST.copyWithoutSign(expr)));
				}else {
					root = new DotExpression(commaExpr ,(Expression)DeepCopyAST.copyWithoutSign(expr));
				}
				
				root.accept(staticEval, null);
			    
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		//
		
		
		/*if(searcher.getVecEnums()!=null){
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
			
		}*/

	
		 
		
	}
	
	
}
