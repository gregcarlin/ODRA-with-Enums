package odra.sbql.optimizers.queryrewrite.weakly.dependentquery.LSCMethod;

import java.util.Hashtable;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBSystemModule;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBVariable;
import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.system.config.ConfigDebug;

public class LSCDependencyChecker extends TraversingASTAdapter {
	
	private NonAlgebraicExpression context; //non algebraic operator for which we check
											//whether the name is weakly dependent
	private String globalModuleName;
	private Hashtable<String,Vector<Expression>> dependentParts = new Hashtable<String,Vector<Expression>>();
	public boolean isLSCWeaklyDependent = true;
	
	

	/**
	 * @param context - non-algebraic operator - the context for dependency 
	 * checking process
	 */
	public LSCDependencyChecker(NonAlgebraicExpression context,String modName) {
		if(ConfigDebug.ASSERTS) assert context.getEnvsInfo() != null : "the ENVS binding levels are not present in the AST";
		this.context = context;
		this.globalModuleName=modName;
		isLSCWeaklyDependent = true;
	}
	
	public Object visitNameExpression(NameExpression expr, Object attr)
			throws SBQLException {
		try {
			DBModule mod = Database.getModuleByName(this.globalModuleName);
		
		boolean itWasAdded = false;

		if ((expr.getBindingInfo().boundat >= context.getEnvsInfo().baseEnvsSize) && (expr.getBindingInfo().boundat < context.getEnvsInfo().baseEnvsSize + context.getEnvsInfo().framesOpened)){
			Expression e = expr;
			/*ReferenceSignature rr=(ReferenceSignature)e.getSignature();
			MBVariable var=new MBVariable(rr.value);if(var.isTypeReference()){String i="ok";}else{String j="ok";}//e nie jest typu ref, oid do C_Emp
			while(var.isTypeReference()){
			    var = new MBVariable(var.getType());
			    String n=var.getName();String tttt=var.getTypeName();
			    if(!var.isValid())
				break;
			}
			ReferenceSignature rs1=(ReferenceSignature)e.getSignature();
			if(new MBVariable(rs1.value).isValid()){
				MBVariable mmm=new MBVariable(rs1.value);
				if(mmm.isTypeReference()){
					while(mmm.isTypeReference()){
					    mmm = new MBVariable(mmm.getType());
					    String n=mmm.getName();String tttt=mmm.getTypeName();
					    if(!mmm.isValid())
						break;
					}
				}
				else if(mmm.isTypeClass()){
					String vb="ccc";
				}
				
			}*/
			
			
			
			
			while(e.getParentExpression()!=null){// && e.getParentExpression() instanceof BinaryExpression){
				
				Expression parentExpr = (Expression)e.getParentExpression();
				//Expression rSubq = parentExpr.getRightExpression();
				
				Signature sigL=context.getLeftExpression().getSignature();
				MBVariable mvL=null;
				
				if(sigL instanceof ReferenceSignature){
					mvL = new MBVariable(((ReferenceSignature)sigL).value);
				}
				else if(sigL instanceof BinderSignature){
					BinderSignature bs = (BinderSignature)sigL;
					if(bs.value instanceof ReferenceSignature)
						mvL = new MBVariable(((ReferenceSignature)(bs.value)).value);
					//mvL = new MBVariable( ((ReferenceSignature)(((BinderSignature)sigL).value)).value );
					else {this.isLSCWeaklyDependent=false;break;}
				}
				else {this.isLSCWeaklyDependent=false;break;}
				
				
				
				if(!(e.getSignature() instanceof ReferenceSignature)) {this.isLSCWeaklyDependent=false;break;}
				ReferenceSignature eSig=(ReferenceSignature)e.getSignature();
				if(new MBVariable(eSig.value).isValid()){
					MBVariable mveSig = new MBVariable(eSig.value);//create MBVar on basis of expr sig
					if((parentExpr instanceof DotExpression || this.dependentParts.containsKey(mveSig.getTypeName()))  && mveSig.isTypeReference()){
						//if(context.getLeftExpression().getSignature().getMaxCard()>=(new MBVariable(mveSig.getType())).getMaxCard() || true){
						//Signature sigL=context.getLeftExpression().getSignature();
						//if(sigL instanceof ReferenceSignature){
							//MBVariable mvL = new MBVariable(((ReferenceSignature)sigL).value);
							if(mvL.getMaxCard()>=(new MBVariable(mveSig.getType())).getMaxCard()){
							//if(sigL.getMaxCard()>=(new MBVariable(mveSig.getType())).getMaxCard()){	
								if(!this.dependentParts.containsKey(mveSig.getTypeName())){
									Vector<Expression> vecExpr=new Vector<Expression>();
									vecExpr.add(parentExpr);
									this.dependentParts.put(mveSig.getTypeName(), vecExpr);
								}
								else{
									this.dependentParts.get(mveSig.getTypeName()).add(parentExpr);
								}
								itWasAdded=true;
								break;
								
							}
							else {this.isLSCWeaklyDependent=false;break;}
						//}
						
							
					}
					else if(mveSig.isTypeEnum()){
						if(this.isUnderExtNonAlgOp(e)){
						if(mvL.getMaxCard()>(new MBEnum(mveSig.getType())).getFields().length){
							if(!this.dependentParts.containsKey(mveSig.getTypeName())){
								Vector<Expression> vecExpr=new Vector<Expression>();
								vecExpr.add(e);
								this.dependentParts.put(mveSig.getTypeName(), vecExpr);
							}
							else{
								this.dependentParts.get(mveSig.getTypeName()).add(e);
							}
							itWasAdded=true;
							break;
						}
						else {this.isLSCWeaklyDependent=false;break;}
					}
						else {this.isLSCWeaklyDependent=false;break;}
					}
					else if(mveSig.isTypeClass()){
						
						e=parentExpr;
						if(e==context || e.getParentExpression()==null) {this.isLSCWeaklyDependent=false;break;}
						
						/*else
							if(e.getParentExpression() instanceof UnaryExpression){
								//e.getParentExpression().replaceSubexpr(e.getParentExpression(), ((UnaryExpression)(e.getParentExpression())).getExpression());
								//e.setParentExpression(((UnaryExpression)(e.getParentExpression())).getExpression());
								
								e.setParentExpression(e.getParentExpression().getParentExpression());
								
								if(e.getParentExpression()==null) {this.isLSCWeaklyDependent=false;break;}
							}*/
						
					}
					else {this.isLSCWeaklyDependent=false;break;}
					
				}
				else {this.isLSCWeaklyDependent=false;break;}
					

			
				
			}
			
			if(!itWasAdded) this.isLSCWeaklyDependent=false;
		}
			
			
				
		
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		return null;

	}

	public Hashtable<String, Vector<Expression>> getDependentParts() {
		return dependentParts;
	}
	
	private boolean isUnderExtNonAlgOp(Expression expr){
		Expression e = expr.getParentExpression ();
		while(!(e instanceof NonAlgebraicExpression)){
			e = e.getParentExpression ();
		}
		return e!=context;
	}


}
