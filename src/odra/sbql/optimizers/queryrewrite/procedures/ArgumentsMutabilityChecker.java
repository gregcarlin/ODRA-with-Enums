package odra.sbql.optimizers.queryrewrite.procedures;

import java.util.Vector;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;

public class ArgumentsMutabilityChecker extends TraversingASTAdapter {
	String[] argNames; //argument names
	Vector<String> auxnames = new Vector<String>(); //for auxiliary names that can be used on argument 
	boolean readOnlyArgs = true; //true by default
	boolean evalLValue = false; //do we visit lvalue of assign operator
	boolean evalAuxNameExpression = false; //do we visit param expression for as/groupas
	boolean agrumentNameUsed = false; //was an argument name used in the context of as/groupas expression?
	
	ArgumentsMutabilityChecker(String[] argNames){
		this.argNames = argNames;
	}
	
	public boolean check(ASTNode node) throws SBQLException{
		node.accept(this, null);
		
		return readOnlyArgs;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAssignExpression(odra.sbql.ast.expressions.AssignExpression, java.lang.Object)
	 */
	@Override
	public Object visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		boolean nestedAssign = evalLValue;
		evalLValue = true;
		expr.getLeftExpression().accept(this, attr);
		evalLValue = nestedAssign;
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitNameExpression(odra.sbql.ast.expressions.NameExpression, java.lang.Object)
	 */
	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
		if(evalLValue){
			for(String name: argNames){
				if(name.compareTo(expr.name().value()) == 0){
					this.readOnlyArgs = false;
					return null;
				}
			}for(String name: this.auxnames){
				if(name.compareTo(expr.name().value()) == 0){
					this.readOnlyArgs = false;
					return null;
				}
			}
		} if(evalAuxNameExpression){
			this.agrumentNameUsed = true;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitAsExpression(odra.sbql.ast.expressions.AsExpression, java.lang.Object)
	 */
	@Override
	public Object visitAsExpression(AsExpression expr, Object attr) throws SBQLException {
		boolean nestedAs = evalAuxNameExpression;
		evalAuxNameExpression = true;
		expr.getExpression().accept(this, attr);
		if(this.agrumentNameUsed)
			this.auxnames.add(expr.name().value());
		agrumentNameUsed = nestedAs;
		evalAuxNameExpression = nestedAs;
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitGroupAsExpression(odra.sbql.ast.expressions.GroupAsExpression, java.lang.Object)
	 */
	@Override
	public Object visitGroupAsExpression(GroupAsExpression expr, Object attr) throws SBQLException {
		boolean nestedAs = evalAuxNameExpression;
		evalAuxNameExpression = true;
		expr.getExpression().accept(this, attr);
		if(this.agrumentNameUsed)
			this.auxnames.add(expr.name().value());
		agrumentNameUsed = nestedAs;
		evalAuxNameExpression = nestedAs;
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitProcedureCallExpression(odra.sbql.ast.expressions.ProcedureCallExpression, java.lang.Object)
	 */
	@Override
	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
		//it might also be recursive call!
		//TODO ??
		this.readOnlyArgs = false;
		return null;
	}
	
}
