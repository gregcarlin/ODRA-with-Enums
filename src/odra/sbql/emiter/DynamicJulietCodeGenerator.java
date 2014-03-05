package odra.sbql.emiter;

import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBProcedure;
import odra.sbql.SBQLException;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.CastExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.CreateLocalExpression;
import odra.sbql.ast.expressions.CreatePermanentExpression;
import odra.sbql.ast.expressions.CreateTemporalExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExternalNameExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.InsertCopyExpression;
import odra.sbql.ast.expressions.InsertExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.ToDateExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.ToRealExpression;
import odra.sbql.ast.expressions.ToStringExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.BreakStatement;
import odra.sbql.ast.statements.ContinueStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;

/**
 * DynamicJulietCodeGenerator
 * codegenerator for a non-typechecked AST 
 * @author Radek Adamus
 *@since 2006-09-27
 *last modified: 2007-06-17
 *@version 1.0
 */
public class DynamicJulietCodeGenerator extends JulietCodeGenerator {
    
	DynamicJulietCodeGenerator(DBModule mod) {
		super(mod);
	}

	DynamicJulietCodeGenerator(DBModule mod, MBProcedure proc) {
		super(mod, proc);
	}

	public JulietCode visitAsExpression(AsExpression expr, Object attr) throws SBQLException {
		super.visitAsExpression(expr, attr);
		expr.setJulietCode(JulietGen.genColAsExpression(expr.getExpression().getJulietCode(), this.name2Id(expr.name().value())));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitNameExpression(odra.sbql.ast.expressions.NameExpression, java.lang.Object)
	 */
	public JulietCode visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
		super.visitNameExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynNameExpression(this.name2Id(expr.name().value())));
		return postProcessExpressionCodeGeneration(expr);

	}

	// TW
	public JulietCode visitExternalNameExpression(ExternalNameExpression expr, Object attr) throws SBQLException {
		super.visitExternalNameExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynNameExpression(this.name2Id(expr.name().value())));
		return postProcessExpressionCodeGeneration(expr);
	}

	public JulietCode visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		super.visitAssignExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynAssignExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitSimpleBinaryExpression(odra.sbql.ast.expressions.SimpleBinaryExpression,
	 *      java.lang.Object)
	 */
	public JulietCode visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException {
		super.visitSimpleBinaryExpression(expr, attr);

		expr.setJulietCode(JulietGen.genDynSimpleBinaryExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(), expr.O
					.getAsInt()));
		return postProcessExpressionCodeGeneration(expr);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitEqualityExpression(odra.sbql.ast.expressions.EqualityExpression,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitEqualityExpression(EqualityExpression expr, Object attr) throws SBQLException {
		super.visitEqualityExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynSimpleBinaryExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(), expr.O
					.getAsInt()));
		return postProcessExpressionCodeGeneration(expr);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitSimpleUnaryExpression(odra.sbql.ast.expressions.SimpleUnaryExpression,
	 *      java.lang.Object)
	 */
	public JulietCode visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException {
		super.visitSimpleUnaryExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynSimpleUnaryExpression(expr.getExpression().getJulietCode(), expr.O.getAsInt()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitRefExpression(odra.sbql.ast.expressions.RefExpression, java.lang.Object)
	 */
	@Override
	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
		super.visitRefExpression(expr, attr);
		expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.genDynRefExpression()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitDerefExpression(odra.sbql.ast.expressions.DerefExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
		super.visitDerefExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynDeref(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitToIntegerExpression(odra.sbql.ast.expressions.ToIntegerExpression,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitToIntegerExpression(ToIntegerExpression expr, Object attr) throws SBQLException {
		super.visitToIntegerExpression(expr, attr);
		expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.genDyn2I()));
		return postProcessExpressionCodeGeneration(expr);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitToRealExpression(odra.sbql.ast.expressions.ToRealExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
		super.visitToRealExpression(expr, attr);
		expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.genDyn2R()));
		return postProcessExpressionCodeGeneration(expr);

	}

	@Override
	public JulietCode visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException {
		super.visitToDateExpression(expr, attr);
		expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.genDyn2D()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitToStringExpression(odra.sbql.ast.expressions.ToStringExpression,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitToStringExpression(ToStringExpression expr, Object attr) throws SBQLException {
		super.visitToStringExpression(expr, attr);
		expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.genDyn2S()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitToBooleanExpression(odra.sbql.ast.expressions.ToBooleanExpression,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitToBooleanExpression(ToBooleanExpression expr, Object attr) throws SBQLException {
		super.visitToBooleanExpression(expr, attr);
		expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.genDyn2B()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitAvgExpression(odra.sbql.ast.expressions.AvgExpression, java.lang.Object)
	 */
	@Override
	public Object visitAvgExpression(AvgExpression expr, Object attr) throws SBQLException {
		super.visitAvgExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynAvgExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitUniqueExpression(odra.sbql.ast.expressions.UniqueExpression, java.lang.Object)
	 */
	@Override
	public Object visitUniqueExpression(UniqueExpression expr, Object attr) throws SBQLException {
		super.visitUniqueExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynUniqueExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitMaxExpression(odra.sbql.ast.expressions.MaxExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException {
		super.visitMaxExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynMaxExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitMinExpression(odra.sbql.ast.expressions.MinExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitMinExpression(MinExpression expr, Object attr) throws SBQLException {
		super.visitMinExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynMinExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	@Override
	public JulietCode visitSumExpression(SumExpression expr, Object attr) throws SBQLException {
		super.visitSumExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynSumExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitInExpression(odra.sbql.ast.expressions.InExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitInExpression(InExpression expr, Object attr) throws SBQLException {
		super.visitInExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynInExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitRangeExpression(odra.sbql.ast.expressions.RangeExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeExpression(RangeExpression expr, Object attr) throws SBQLException {
		super.visitRangeExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynRangeExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitBlockStatement(odra.sbql.ast.statements.BlockStatement, java.lang.Object)
	 */
	@Override
	public JulietCode visitBlockStatement(BlockStatement stmt, Object attr) throws SBQLException {
		this.blockLevel++;
		stmt.getStatement().accept(this, attr);
		this.blockLevel--;
		super.commonVisitStatement(stmt, attr);
		stmt.setJulietCode(stmt.getStatement().getJulietCode());
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitVariableDeclarationStatement(odra.sbql.ast.statements.VariableDeclarationStatement,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitVariableDeclarationStatement(VariableDeclarationStatement stmt, Object attr) throws SBQLException {
		super.visitVariableDeclarationStatement(stmt, attr);
		// in dynamic environment decalarations are skipped
		stmt.setJulietCode(new JulietCode());
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCreateExpression(odra.sbql.ast.expressions.CreateExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
		super.visitCreateExpression(expr, attr);
		if (this.proc == null) return expr.setJulietCode(JulietGen.genDynamicPermanentCreate(this.name2Id(
					expr.name().value()), expr.getExpression().getJulietCode(), null));
		expr.setJulietCode(JulietGen.genDynamicLocalCreate(this.name2Id(expr.name().value()),
					expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCreateLocalExpression(odra.sbql.ast.expressions.CreateLocalExpression,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
		super.visitCreateLocalExpression(expr, attr);
		expr.setJulietCode(JulietGen
					.genDynamicLocalCreate(this.name2Id(expr.name().value()), expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCreatePermanentExpression(odra.sbql.ast.expressions.CreatePermanentExpression,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitCreatePermanentExpression(CreatePermanentExpression expr, Object attr) throws SBQLException {
		super.visitCreatePermanentExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynamicPermanentCreate(this.name2Id(expr.name().value()),
					expr.getExpression().getJulietCode(), null));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCreateTemporalExpression(odra.sbql.ast.expressions.CreateTemporalExpression,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
		super.visitCreateTemporalExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynamicTemporalCreate(this.name2Id(expr.name().value()),
					expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitInsertExpression(odra.sbql.ast.expressions.InsertExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {
		super.visitInsertExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynInsertExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitInsertCopyExpression(odra.sbql.ast.expressions.InsertCopyExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertCopyExpression(InsertCopyExpression expr,
		Object attr) throws SBQLException {
	    super.visitInsertCopyExpression(expr, attr);
	
	    expr.setJulietCode(JulietGen.genDynInsertCopyExpression(this.name2Id(expr.name().value()), expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
	    return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitBreakStatement(odra.sbql.ast.statements.BreakStatement, java.lang.Object)
	 */
	@Override
	public JulietCode visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
		super.visitBreakStatement(stmt, attr);
		if (isInLoop) return stmt.setJulietCode(JulietGen.genBreak(this.blockLevel - this.loopBlockLevel));

		stmt.setJulietCode(new JulietCode());
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCastExpression(odra.sbql.ast.expressions.CastExpression, java.lang.Object)
	 */
	@Override
	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException {
		super.visitCastExpression(expr, attr);
		expr.setJulietCode(JulietGen.genDynCastExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitContinueStatement(odra.sbql.ast.statements.ContinueStatement, java.lang.Object)
	 */
	@Override
	public JulietCode visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
		super.visitContinueStatement(stmt, attr);
		if (isInLoop) return stmt.setJulietCode(JulietGen.genContinue(this.blockLevel - this.loopBlockLevel));
		
		stmt.setJulietCode(new JulietCode());
		return postProcessStatementCodeGeneration(stmt);
	}
	

}