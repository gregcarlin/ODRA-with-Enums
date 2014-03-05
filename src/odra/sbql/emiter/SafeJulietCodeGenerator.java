package odra.sbql.emiter;

import java.util.HashMap;
import java.util.Map;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.StdEnvironment;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBBinaryOperator;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBUnaryOperator;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.PrimitiveTypeKind;
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
import odra.sbql.ast.expressions.DeserializeOidExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ExternalNameExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.InsertCopyExpression;
import odra.sbql.ast.expressions.InsertExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.RemoteQueryExpression;
import odra.sbql.ast.expressions.RenameExpression;
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
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.SingleCatchBlock;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import odra.sbql.results.compiletime.ValueSignature;
import odra.system.Names;
import odra.system.config.ConfigDebug;

public class SafeJulietCodeGenerator extends JulietCodeGenerator {

	StdEnvironment env = StdEnvironment.getStdEnvironment();

	SafeJulietCodeGenerator(DBModule mod) {
		super(mod);
	}
	
	SafeJulietCodeGenerator(DBModule mod, MBProcedure proc) {
		super(mod, proc);
	}

	protected JulietCode postProcessExpressionCodeGeneration(Expression expr) {			

    	// Added by TK for Volatile Index
		Signature subsig = expr.getSubstitutedSignature();
		if (subsig != null)
			if (subsig instanceof ReferenceSignature) {
				ReferenceSignature refsig = (ReferenceSignature) subsig;
				if (refsig.isVirtual()) {
					try {
						expr.getJulietCode().append(JulietGen.genCreateVirtualReferenceCode(new MBView(refsig.value).getVirtualObject().getObjectNameId()));
					} catch (Exception e) {
						
					}
				}				
			}
		
		return super.postProcessExpressionCodeGeneration(expr);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitAsExpression(odra.sbql.ast.expressions.AsExpression, java.lang.Object)
	 */

	public JulietCode visitAsExpression(AsExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}

		super.visitAsExpression(expr, attr);
		if (expr.getExpression().getSignature().getMinCard() == 1 && expr.getExpression().getSignature().getMaxCard() == 1) {
			expr.setJulietCode(JulietGen.genAsExpression(expr.getExpression().getJulietCode(), this.name2Id(expr.name().value())));
		} else {
			expr.setJulietCode(JulietGen.genColAsExpression(expr.getExpression().getJulietCode(), this.name2Id(expr.name().value())));
		}

		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitNameExpression(odra.sbql.ast.expressions.NameExpression, java.lang.Object)
	 */
	public JulietCode visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitNameExpression(expr, attr);
		expr.setJulietCode(JulietGen.genNameExpression(this.name2Id(expr.name().value())));
		if (expr.getSignature() instanceof ReferenceSignature && ((ReferenceSignature) expr.getSignature()).isVirtual()) {
			if (!expr.virtualBind) {
			    //if the name come from the remote server perform remote call instead of virtual call
			    try {
				if(expr.links.size() == 1 && ((OID)expr.links.toArray()[0]).getObjectNameId() != Names.LOCALHOST_LINK){			
					expr.setJulietCode(expr.getJulietCode().append(JulietGen.genRemoteCall(0)));
				}else
					expr.setJulietCode(expr.getJulietCode().append(JulietGen.genCallVirtualObjects()));
			    } catch (DatabaseException e) {
			    	throw new EmiterException(e, expr,this);
			    }
			}
		}
		//if(expr.getSignature().getEnumerator()!=null){
			//expr.setJulietCode(expr.getJulietCode().append(JulietGen.genDerefStringExpression()));
		//}
		return postProcessExpressionCodeGeneration(expr);
	}

	public JulietCode visitExternalNameExpression(ExternalNameExpression expr, Object attr) throws SBQLException {
//		if (ConfigDebug.ASSERTS) {
//			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
//		}
		super.visitExternalNameExpression(expr, attr);
		expr.setJulietCode(JulietGen.genNameExpression(this.name2Id(expr.name().value())));
		
		return postProcessExpressionCodeGeneration(expr);
	}

	public JulietCode visitAssignExpression(AssignExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitAssignExpression(expr, attr);
		ReferenceSignature refLeftSign = ((ReferenceSignature) expr.getLeftExpression().getSignature());
		if (refLeftSign.isVirtual()) {
			expr.setJulietCode(JulietGen.genVirtualAssignExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		} else { 	
	    	JulietCode assignCode = this.getAssignCode(expr.getLeftExpression().getSignature(),expr.getRightExpression().getSignature());
	    	expr.setJulietCode(JulietGen.genAssignExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(), assignCode));		
		}
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitInsertCopyExpression(odra.sbql.ast.expressions.InsertCopyExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitInsertCopyExpression(InsertCopyExpression expr, Object attr) throws SBQLException {

		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		ReferenceSignature rsig = (ReferenceSignature) expr.getSignature();
		super.visitInsertCopyExpression(expr, attr);
		JulietCode jtc = expr.getRightExpression().getJulietCode();
		
		if (rsig.isVirtual()) {
			// inserting an object into the virtual environment is really calling on_new
			return expr.setJulietCode(JulietGen.genVirtualInsert(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(), this.name2Id(expr.name().value())));
		} 
		try {
		    MBVariable mbvar = new MBVariable(rsig.value);
		    jtc.append(expr.getLeftExpression().getJulietCode());
		    jtc.append(JulietGen.genInsertCopyExpression(mbvar, expr.getRightExpression().getSignature().getMinCard(), expr.getRightExpression().getSignature().getMaxCard(), true));
		} catch (DatabaseException e) {
		    throw new EmiterException(e, expr,this);
		}
		
		expr.setJulietCode(jtc);
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitInsertExpression(odra.sbql.ast.expressions.InsertExpression, java.lang.Object)
	 */
	@Override
	public Object visitInsertExpression(InsertExpression expr, Object attr) throws SBQLException {		
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitInsertExpression(expr, attr);
		boolean binder = false;
		if(expr.getRightExpression().getSignature() instanceof BinderSignature){
			binder = true;
			
		}
		ReferenceSignature refsig = (ReferenceSignature) expr.getSignature();
		

		if (refsig.isVirtual()) {
			// inserting an object into the virtual environment is really calling on_new
			try {
			    expr.setJulietCode(JulietGen.genVirtualInsert(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(), new MBView(
			    			refsig.value).getVirtualObject().getObjectNameId()));
			} catch (DatabaseException e) {
			    throw new EmiterException(e,expr,this);
			}
		}else if(isCollection(expr.getSignature())){
			expr.setJulietCode(JulietGen.genColInsertExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(),binder));
		}else 
			expr.setJulietCode(JulietGen.genInsertExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(),binder));
		return postProcessExpressionCodeGeneration(expr);
		
	}

	/**
	 * @param signature
	 * @return
	 */
	private boolean isCollection(Signature signature) {
		if (signature.getMinCard() == 1 && signature.getMaxCard() == 1) 
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitSimpleBinaryExpression(odra.sbql.ast.expressions.SimpleBinaryExpression,
	 *      java.lang.Object)
	 */
	public JulietCode visitSimpleBinaryExpression(SimpleBinaryExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}

		super.visitSimpleBinaryExpression(expr, attr);
		try {
		    expr.setJulietCode(JulietGen.genSimpleBinaryExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(),
		    			OpCodes.getOpCode(new MBBinaryOperator(expr.operator).getOpCode())));
		} catch (DatabaseException e) {
		    throw new EmiterException(e,expr,this);
		}
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTVisitor#visitSimpleUnaryExpression(odra.sbql.ast.expressions.SimpleUnaryExpression,
	 *      java.lang.Object)
	 */
	public JulietCode visitSimpleUnaryExpression(SimpleUnaryExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitSimpleUnaryExpression(expr, attr);
		try {
		    expr.setJulietCode(JulietGen.genSimpleUnaryExpression(expr.getExpression().getJulietCode(), OpCodes.getOpCode(new MBUnaryOperator(expr.operator)
		    			.getOpCode())));
		} catch (DatabaseException e) {
		    throw new EmiterException(e,expr,this);
		}
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
		expr.setJulietCode(JulietGen.genAvgExpression(expr.getExpression().getJulietCode()));
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
		expr.setJulietCode(JulietGen.genUniqueExpression(expr.getExpression().getJulietCode()));
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
		expr.setJulietCode(JulietGen.genMaxExpression(expr.getExpression().getJulietCode()));
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
		expr.setJulietCode(JulietGen.genMinExpression(expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	@Override
	public JulietCode visitSumExpression(SumExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}

		super.visitSumExpression(expr, attr);
		if (((ValueSignature) expr.getExpression().getSignature()).value.equals(env.integerType)) {
			expr.setJulietCode(JulietGen.genSumIntExpression(expr.getExpression().getJulietCode()));
		} else if (((ValueSignature) expr.getExpression().getSignature()).value.equals(env.realType)) {
			expr.setJulietCode(JulietGen.genSumRealExpression(expr.getExpression().getJulietCode()));
		} else {
			expr.setJulietCode(JulietGen.genDynSumExpression(expr.getExpression().getJulietCode()));
		}
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitRefExpression(odra.sbql.ast.expressions.RefExpression, java.lang.Object)
	 */
	@Override
	public Object visitRefExpression(RefExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitRefExpression(expr, attr);
		if (isCollection(expr.getSignature())) {
			expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.genColRefExpression()));
		}else
			expr.setJulietCode(expr.getExpression().getJulietCode().append(JulietGen.genRefExpression()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitDerefExpression(odra.sbql.ast.expressions.DerefExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitDerefExpression(DerefExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitDerefExpression(expr, attr);
		Expression paramExpr = expr.getExpression();
		Signature paramSig = paramExpr.getSignature();//c_ip
		Signature thisSig = expr.getSignature();//c_integer
		JulietCode jtExpr = paramExpr.getJulietCode();
		if ((paramSig instanceof ReferenceSignature) && (((ReferenceSignature) paramSig).hasRefFlag())) {
			// ref flag we do not perform dereference
			//expr.setJulietCode(jtExpr);
		}
		else if ((paramSig instanceof ReferenceSignature) && (((ReferenceSignature) paramSig).isVirtual())) {
			jtExpr = JulietGen.genDynDeref(jtExpr);
		}
		else if (thisSig instanceof ValueSignature) {
			ValueSignature vsig = (ValueSignature) thisSig;
			PrimitiveTypeKind kind;
			    try {
				    MBPrimitiveType mbpt = new MBPrimitiveType(((ValueSignature) thisSig).value);
				    kind = mbpt.getTypeKind();
				} catch (DatabaseException e) {
				    throw new EmiterException(e, expr,this);
				}
			if (vsig.getMinCard() == 1 && vsig.getMaxCard() == 1) {
				switch (kind) {
					case BOOLEAN_TYPE:
						jtExpr.append(JulietGen.genDerefBooleanExpression());
						break;
					case INTEGER_TYPE:
						jtExpr.append(JulietGen.genDerefIntExpression());
						break;
					case REAL_TYPE:
						jtExpr.append(JulietGen.genDerefRealExpression());
						break;
					case STRING_TYPE:
						jtExpr.append(JulietGen.genDerefStringExpression());
						break;
					case DATE_TYPE:
						jtExpr.append(JulietGen.genDerefDateExpression());
						break;
					default:
						assert false : "dereference simple object of unknown type";
				}

			} else {
				switch (kind) {
					case BOOLEAN_TYPE:
						jtExpr.append(JulietGen.genDerefColBooleanExpression());
						break;
					case INTEGER_TYPE:
						jtExpr.append(JulietGen.genDerefColIntExpression());
						break;
					case REAL_TYPE:
						jtExpr.append(JulietGen.genDerefColRealExpression());
						break;
					case STRING_TYPE:
						jtExpr.append(JulietGen.genDerefColStringExpression());
						break;
					case DATE_TYPE:
						jtExpr.append(JulietGen.genDerefColDateExpression());
						break;
					default:
						assert false : "dereference simple object of unknown type";
				}
			}
		} else if (thisSig instanceof ReferenceSignature) { // dereference of reference object
			if (thisSig.getMinCard() == 1 && thisSig.getMaxCard() == 1) {
				jtExpr.append(JulietGen.genDerefRefExpression());
			} else {
				jtExpr.append(JulietGen.genDerefColRefExpression());
			}
		} else if (thisSig instanceof StructSignature || thisSig instanceof BinderSignature) {
						
			if (thisSig.getMinCard() == 1 && thisSig.getMaxCard() == 1) {
				jtExpr.append(JulietGen.genDerefComplexExpression());
			} else {
				jtExpr.append(JulietGen.genDerefColComplexExpression());
			}
		} else {
			assert false : "wrong dereference output signature";
		}

		expr.setJulietCode(jtExpr);
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
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitEqualityExpression(expr, attr);
		OpCodes opcode = this.getEqualityOpCode(expr.getLeftExpression().getSignature(), expr.O.getAsInt() == Operator.EQUALS);
		expr.setJulietCode(JulietGen.genSimpleBinaryExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode(), opcode));
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
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitToIntegerExpression(expr, attr);
		JulietCode res = expr.getExpression().getJulietCode();

		if (expr.getExpression().getSignature() instanceof ValueSignature) {
		    PrimitiveTypeKind kind;
		    try {
			    MBPrimitiveType mbpt = new MBPrimitiveType(((ValueSignature) expr.getExpression().getSignature()).value);
			    kind = mbpt.getTypeKind();
			} catch (DatabaseException e) {
			    throw new EmiterException(e, expr,this);
			}
	
			switch (kind) {
				case REAL_TYPE:
					res.append(JulietGen.genR2I());
					break;
				case STRING_TYPE:
					res.append(JulietGen.genS2I());
					break;

				case INTEGER_TYPE:
					break;

				default:
					throw new EmiterException("forbidden coerce to integer",expr,this);
			}

		} else {
			throw new EmiterException("to Integer coerce of non value",expr,this);
		}

		expr.setJulietCode(res);
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitToRealExpression(odra.sbql.ast.expressions.ToRealExpression, java.lang.Object)
	 */
	@Override
	public JulietCode visitToRealExpression(ToRealExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitToRealExpression(expr, attr);
		JulietCode res = expr.getExpression().getJulietCode();

		if (expr.getExpression().getSignature() instanceof ValueSignature) {
		    PrimitiveTypeKind kind;
		    try {
			    MBPrimitiveType mbpt = new MBPrimitiveType(((ValueSignature) expr.getExpression().getSignature()).value);
			    kind = mbpt.getTypeKind();
			} catch (DatabaseException e) {
			    throw new EmiterException(e, expr,this);
			}
			switch (kind) {
				case INTEGER_TYPE:
					res.append(JulietGen.genI2R());
					break;
				case STRING_TYPE:
					res.append(JulietGen.genS2R());
					break;
				case REAL_TYPE:
					break;
				default:
					throw new EmiterException("forbidden coerce to real", expr,this);
			}
		} else {
			throw new EmiterException("to Real coerce of non value", expr,this);
		}

		expr.setJulietCode(res);
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
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitToStringExpression(expr, attr);
		JulietCode res = expr.getExpression().getJulietCode();
		if (expr.getExpression().getSignature() instanceof ValueSignature) {
		    PrimitiveTypeKind kind;
		    try {
			    MBPrimitiveType mbpt = new MBPrimitiveType(((ValueSignature) expr.getExpression().getSignature()).value);
			    kind = mbpt.getTypeKind();
			} catch (DatabaseException e) {
			    throw new EmiterException(e, expr,this);
			}
			switch (kind) {
				case INTEGER_TYPE:
					res.append(JulietGen.genI2S());
					break;
				case BOOLEAN_TYPE:
					res.append(JulietGen.genB2S());
					break;
				case REAL_TYPE:
					res.append(JulietGen.genR2S());
					break;
				case DATE_TYPE:
					res.append(JulietGen.genD2S());
					break;
				case STRING_TYPE:
					break;
				default:
					throw new EmiterException("forbidden coerce to string", expr,this);
			}

		} else {
			throw new EmiterException("to String coerce of non value", expr,this);
		}

		expr.setJulietCode(res);
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
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitToBooleanExpression(expr, attr);
		JulietCode res = expr.getExpression().getJulietCode();
		if (expr.getExpression().getSignature() instanceof ValueSignature) {
			PrimitiveTypeKind kind;
			try {
			    MBPrimitiveType mbpt = new MBPrimitiveType(((ValueSignature) expr.getExpression().getSignature()).value);
			    kind = mbpt.getTypeKind();
			} catch (DatabaseException e) {
			    throw new EmiterException(e, expr,this);
			}
			switch (kind) {
				case STRING_TYPE:
					res.append(JulietGen.genS2B());
					break;
				case BOOLEAN_TYPE:
					break;
				default:
					throw new EmiterException("forbidden coerce to Boolean", expr,this);
			}
		} else {
			throw new EmiterException("to Boolean coerce of non value", expr,this);
		}
		expr.setJulietCode(res);
		return postProcessExpressionCodeGeneration(expr);
	}

	@Override
	public JulietCode visitToDateExpression(ToDateExpression expr, Object attr) throws SBQLException {
		if (ConfigDebug.ASSERTS) {
			assert expr.getSignature() != null : "no signature, the AST is not typechecked";
		}
		super.visitToDateExpression(expr, attr);
		JulietCode res = expr.getExpression().getJulietCode();

		if (expr.getExpression().getSignature() instanceof ValueSignature) {
		    PrimitiveTypeKind kind;
		    try {
			    MBPrimitiveType mbpt = new MBPrimitiveType(((ValueSignature) expr.getExpression().getSignature()).value);
			    kind = mbpt.getTypeKind();
			} catch (DatabaseException e) {
			    throw new EmiterException(e, expr,this);
			}
			switch (kind) {
				case STRING_TYPE:
					res.append(JulietGen.genS2D());
					break;

				case DATE_TYPE:
					break;

				default:
					throw new EmiterException("forbidden coerce to date", expr,this);
			}

		} else {
			throw new EmiterException("to date coerce of non value", expr,this);
		}

		expr.setJulietCode(res);
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
		expr.setJulietCode(JulietGen.genInExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
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
		expr.setJulietCode(JulietGen.genRangeExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCreateExpression(odra.sbql.ast.expressions.CreateExpression, java.lang.Object)
	 */
	@Override
	public Object visitCreateExpression(CreateExpression expr, Object attr) throws SBQLException {
		ReferenceSignature rsig = (ReferenceSignature) expr.getSignature();
		super.visitCreateExpression(expr, attr);
		JulietCode jtc = expr.getExpression().getJulietCode();
		try {
		    if (rsig.isVirtual()) {
		    	// jtc = JulietGen.genDynamicPermanentCreate(this.name2Id(expr.N.V), jtc, new
		    	// MBView(rsig.value).getModule().getOID());
			if(proc == null) {
		    		jtc = JulietGen.genVirtualCreate(this.name2Id(expr.name().value()), jtc, new MBView(rsig.value)
		    				.getModule().getOID());
			} else {
			    jtc = JulietGen.genDynamicPermanentCreate(this.name2Id(expr.name().value()), jtc,
	    				new MBView(rsig.value).getModule().getOID());
			}			    
		    } else {
			MBVariable mbvar = new MBVariable(rsig.value);
			if(expr.getExpression() instanceof EmptyExpression)
    	    {
				jtc.append(JulietGen.genCreateExpressionDefault(mbvar));
    	    }else 
    	    {
    	    	jtc.append(JulietGen.genCreateExpressionWithInit(mbvar, rsig.getMinCard(), rsig.getMaxCard()));
    	    }
		    }
		} catch (DatabaseException e) {
		    throw new EmiterException(e, expr,this);
		}
		expr.setJulietCode(jtc);
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCreateLocalExpression(odra.sbql.ast.expressions.CreateLocalExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitCreateLocalExpression(CreateLocalExpression expr, Object attr) throws SBQLException {
		ReferenceSignature rsig = (ReferenceSignature) expr.getSignature();
		super.visitCreateLocalExpression(expr, attr);
		JulietCode jtc = expr.getExpression().getJulietCode();
		if (rsig.isVirtual()) {
			jtc = JulietGen.genDynamicLocalCreate(this.name2Id(expr.name().value()), jtc);
		} else {
			try {
			    MBVariable mbvar = new MBVariable(rsig.value);
			    if (expr.declaration_environment != CreateExpression.LOCAL || !expr.getBlockName().equals(this.currentBlockName)) {
			    	if (mbvar.getMinCard() != 1 || mbvar.getMaxCard() != 1) {
			    		init.append(JulietGen.genLoadLocalEnvironment());
			    		init.append(JulietGen.genInitVariable(mbvar, 0 , mbvar.getMaxCard()));
			    		init.append(JulietGen.genPopQRES());
			    	}
			    }
			    if(expr.getExpression() instanceof EmptyExpression)
			    {
			        jtc.append(JulietGen.genCreateDefaultLocalExpression(mbvar));
			    }else 
			    {
			        jtc.append(JulietGen.genCreateLocalExpression(mbvar, rsig.getMinCard(), rsig.getMaxCard(), true));
			    }
			} catch (DatabaseException e) {
			    throw new EmiterException(e,expr,this);
			}

		}

		expr.setJulietCode(jtc);
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
		ReferenceSignature rsig = (ReferenceSignature) expr.getSignature();
		super.visitCreatePermanentExpression(expr, attr);
		JulietCode jtc = expr.getExpression().getJulietCode();
		try {
		    if (rsig.isVirtual()) {
		    	// jtc = JulietGen.genDynamicPermanentCreate(this.name2Id(expr.N.V), jtc, new
		    	// MBView(rsig.value).getModule().getOID());
		    	jtc = JulietGen.genVirtualCreate(this.name2Id(expr.name().value()), jtc, new MBView(rsig.value)
		    				.getModule().getOID());
		    } else {
		    	MBVariable mbvar = new MBVariable(rsig.value);
		    	if(expr.getExpression() instanceof EmptyExpression)
		    	{
		    	    jtc.append(JulietGen.genCreateDefaultPermanentExpression(mbvar,  expr.importModuleRef == CreateExpression.CURRENT_MODULE));
		    	}else 
		    	{
		    	    jtc.append(JulietGen.genCreatePermanentExpression(mbvar, rsig.getMinCard(), rsig.getMaxCard(),
		    				expr.importModuleRef == CreateExpression.CURRENT_MODULE, true));
		    	}

		    }
		} catch (DatabaseException e) {
		    throw new EmiterException(e,expr,this);
		}

		expr.setJulietCode(jtc);
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCreateTemporalExpression(odra.sbql.ast.expressions.CreateTemporalExpression,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, Object attr) throws SBQLException {
		ReferenceSignature rsig = (ReferenceSignature) expr.getSignature();
		super.visitCreateTemporalExpression(expr, attr);
		JulietCode jtc = expr.getExpression().getJulietCode();
		try {
		    if (rsig.isVirtual()) {
		    	// assert false :"unimplemented";
		    	jtc = JulietGen.genVirtualCreate(this.name2Id(expr.name().value()), jtc);
		    } else {
		    	MBVariable mbvar = new MBVariable(rsig.value);
		    	if(expr.getExpression() instanceof EmptyExpression)
		    	{
		    	    jtc.append(JulietGen.genCreateDefaultTemporalExpression(mbvar,  expr.importModuleRef == CreateExpression.CURRENT_MODULE));
		    	}else
		    	{
		    	    jtc.append(JulietGen.genCreateTemporalExpression(mbvar, rsig.getMinCard(), rsig.getMaxCard(),
		    				expr.importModuleRef == CreateExpression.CURRENT_MODULE, true));
		    	}

		    }
		} catch (DatabaseException e) {
		    throw new EmiterException(e,expr,this);
		}

		expr.setJulietCode(jtc);
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitVariableDeclarationStatement(odra.sbql.ast.statements.VariableDeclarationStatement,
	 *      java.lang.Object)
	 */
	@Override
	public JulietCode visitVariableDeclarationStatement(VariableDeclarationStatement node, Object attr) throws SBQLException {
		super.visitVariableDeclarationStatement(node, attr);
		this.localVariableDeclaration.put(node.getVariableName(), node);
		if(!(node.getInitExpression() instanceof EmptyExpression))
		    node.setJulietCode(node.getInitExpression().getJulietCode().append(JulietGen.genEndStatement()));
		else
			node.setJulietCode(node.getInitExpression().getJulietCode());
		return postProcessStatementCodeGeneration(node);
		    
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitBreakStatement(odra.sbql.ast.statements.BreakStatement, java.lang.Object)
	 */
	@Override
	public Object visitBreakStatement(BreakStatement stmt, Object attr) throws SBQLException {
		super.visitBreakStatement(stmt, attr);
		stmt.setJulietCode(JulietGen.genBreak(this.blockLevel - this.loopBlockLevel));
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitContinueStatement(odra.sbql.ast.statements.ContinueStatement, java.lang.Object)
	 */
	@Override
	public Object visitContinueStatement(ContinueStatement stmt, Object attr) throws SBQLException {
		super.visitContinueStatement(stmt, attr);
		stmt.setJulietCode(JulietGen.genContinue(this.blockLevel - this.loopBlockLevel));
		return postProcessStatementCodeGeneration(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitCastExpression(odra.sbql.ast.expressions.CastExpression, java.lang.Object)
	 */
	@Override
	public Object visitCastExpression(CastExpression expr, Object attr) throws SBQLException {
		if(expr.getSignature().getEnumerator()!=null){
			expr.getRightExpression().accept(this, attr);
			
			try {
				JulietCode jc = JulietGen.genAsExpression(expr.getRightExpression().getJulietCode(), Database.getNameIndex().addName("$value"));
				
				JulietCode jc1 = JulietGen.genInExpression(JulietGen.genNameExpression(Database.getNameIndex().addName("$value")), JulietGen.genNameExpression(Database.getNameIndex().addName("$enum_"+expr.getSignature().getEnumerator()+"_values")));
				
				JulietCode jc2 = JulietGen.genConditionalExpression(jc1, JulietGen.genNameExpression(Database.getNameIndex().addName("$value")),JulietGen
						.genEmptyBag());
				
				JulietCode jc3 = JulietGen.genDotExpression(jc, jc2).emit(OpCodes.single);
				
				expr.setJulietCode(jc3);
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else{

			super.visitCastExpression(expr, attr);
			expr.setJulietCode(JulietGen.genClassCastExpression(expr.getLeftExpression().getJulietCode(), expr.getRightExpression().getJulietCode()));
				
		}
		
		return postProcessExpressionCodeGeneration(expr);
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitDeserializeOidExpression(odra.sbql.ast.expressions.DeserializeOidExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeserializeOidExpression(DeserializeOidExpression expr,
			Object attr) throws SBQLException {
		super.visitDeserializeOidExpression(expr, attr);		
		expr.setJulietCode(JulietGen.genDeserializeOID(expr.getRightExpression().getJulietCode(), expr.getLeftExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.ASTAdapter#visitBlockStatement(odra.sbql.ast.statements.BlockStatement, java.lang.Object)
	 */
	@Override
	public JulietCode visitBlockStatement(BlockStatement node, Object attr) throws SBQLException {

		JulietCode prevBlockInitCode = init;
		init = new JulietCode();
		
		Map<String, VariableDeclarationStatement> prevLocalVariableDeclaration = this.localVariableDeclaration;
		this.localVariableDeclaration = new HashMap<String, VariableDeclarationStatement>();
		if (ConfigDebug.ASSERTS) {
			assert node.hasBlockInfo() : "no block info in the block statement";
		}

		JulietCode blckCode = JulietGen.genCreateLocalEnvironment();

		
		this.blockLevel++;
		String previousBlockName = this.currentBlockName;
		this.currentBlockName = node.getBlockName();
		node.getStatement().accept(this, attr);
		this.currentBlockName = previousBlockName;
		this.blockLevel--;
		JulietCode inBlockCode = node.getStatement().getJulietCode();
		
		try {
		    if (node.hasLocalData()) {
		    	if (ConfigDebug.ASSERTS) {
		    		assert this.proc != null : "code generator for procedure must have MBProcedure instance";
		    	}
		    	OID locData = proc.getLocalBlockEntry(node.getBlockName());

		    	if (ConfigDebug.ASSERTS) {
		    		assert locData != null : node.getBlockName()
		    					+ " block has local data but there is no block entry in MBProcedure";
		    	}
		    	for (OID locvar : locData.derefComplex()) {
		    		MBVariable mbvar = new MBVariable(locvar);
		    		if (mbvar.isValid()) {
		    			blckCode.append(initializeLocalVariable(mbvar));
		    		}
		    	}
		    }
		} catch (DatabaseException e) {
		    throw new EmiterException(e,node,this);
		}
		blckCode.append(init);
		blckCode.append(JulietGen.genInitLocalEnvironment());
		// blckCode.append((JulietCode)node.S.accept(this, attr));
		blckCode.append(inBlockCode);
		blckCode.append(JulietGen.genDestroyLocalEnvironment());

		init = prevBlockInitCode;
		this.localVariableDeclaration = prevLocalVariableDeclaration;
		super.commonVisitStatement(node, attr);
		node.setJulietCode(blckCode);
		return postProcessStatementCodeGeneration(node);
	}

	private OpCodes getEqualityOpCode(Signature sig, boolean equal) {

		if (sig instanceof ValueSignature) {
			OID val = ((ValueSignature) sig).value;
			if (val.equals(env.integerType)) {
				return equal ? OpCodes.eqI : OpCodes.nEqI;
			} else if (val.equals(env.stringType)) {
				return equal ? OpCodes.eqS : OpCodes.nEqS;
			} else if (val.equals(env.booleanType)) {
				return equal ? OpCodes.eqB : OpCodes.nEqB;
			} else if (val.equals(env.realType)) {
				return equal ? OpCodes.eqR : OpCodes.nEqR;
			} else if (val.equals(env.dateType)) {
				return equal ? OpCodes.eqD : OpCodes.nEqD;
			} else {
				assert false : "unknown simple type";
			}
		} else if (sig instanceof ReferenceSignature) {
			return equal ? OpCodes.eqRef : OpCodes.nEqRef;
		} else if (sig instanceof StructSignature) {
			return equal ? OpCodes.eqStruct : OpCodes.nEqStruct;
		} else {
			assert false : "unknown signature type";
		}
		return OpCodes.nop;
	}

	private JulietCode getAssignCode(Signature lsig, Signature rsig) {
		
		if (rsig instanceof ValueSignature) {
			OpCodes opcode;
			OID val = ((ValueSignature) rsig).value;
			if (val.equals(env.integerType)) {
				opcode = OpCodes.storeI;
			} else if (val.equals(env.stringType)) {
				opcode = OpCodes.storeS;
			} else if (val.equals(env.booleanType)) {
				opcode = OpCodes.storeB;
			} else if (val.equals(env.realType)) {
				opcode = OpCodes.storeR;
			} else if (val.equals(env.dateType)) {
				opcode = OpCodes.storeD;
			} else {
				assert false : "unknown simple type";
				opcode = OpCodes.nop;
			}
			return JulietGen.getJulietForOpCode(opcode);
		} else if (rsig instanceof ReferenceSignature) {
		    try {
			MBVariable var = new MBVariable(((ReferenceSignature)lsig).value);
			if(var.isValid() && var.hasReverseReference()){
				return JulietGen.genAssignReverseReference(var.getReverseNameId());
			}
			else if(var.isValid() && var.isTypeEnum()){
				//return JulietGen.genAssignEnumCode();
			}
		    } catch (DatabaseException e) {
		    	throw new EmiterException(e);
		    }
		    return  JulietGen.getJulietForOpCode(OpCodes.storeRef);
		} else if (rsig instanceof StructSignature || rsig instanceof BinderSignature) {
			try {
				MBVariable var = new MBVariable(((ReferenceSignature)lsig).value);
				return JulietGen.genAssignComplexOperatorCode(var);
			} catch (DatabaseException e) {
				throw new EmiterException(e);
			}
		} else {
			assert false : "unknown signature type";
			return new JulietCode();
		}
		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	@Override
	public Object visitTryCatchFinallyStatement(
		TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	{
	    stmt.setLevel(this.blockLevel);
	    stmt.getTryStatement().accept(this, attr);
	    JulietCode tryStatementCode = stmt.getTryStatement().getJulietCode();
	    this.catchTable.insertTryBlock(tryStatementCode, this.blockLevel);
	    //TODO catch
	    SingleCatchBlock[] cbs = stmt.getCatchBlocks().flattenCatchBlocks();
	    JulietCode[] catchBlocksCode = new JulietCode[cbs.length];
	    
	    this.blockLevel ++;
	    for(int i = 0 ; i < cbs.length; i++){	
			cbs[i].getStatement().accept(this, attr);
			int exceptionNameId = this.name2Id(cbs[i].getExceptionName());
			int exceptionTypeNameId = this.name2Id(cbs[i].getExceptionTypeName());
			catchBlocksCode[i] = JulietGen.genCatchBlock(exceptionNameId, cbs[i].getStatement().getJulietCode());
			this.catchTable.insertCatchForException(tryStatementCode, catchBlocksCode[i], exceptionTypeNameId);
	    }
	    this.blockLevel --;
	    
	    stmt.getFinallyStatement().accept(this, attr);
	    if(!(stmt.getFinallyStatement() instanceof EmptyStatement)){
			this.catchTable.insertFinally(tryStatementCode, stmt.getFinallyStatement().getJulietCode());
	    }
	    
	    stmt.setJulietCode(JulietGen.genTryCatchFinally(stmt.getTryStatement().getJulietCode(), catchBlocksCode, stmt.getFinallyStatement().getJulietCode()));
	    return postProcessStatementCodeGeneration(stmt);
	}

	@Override
	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr)
			throws SBQLException {
			    //super.visitRemoteQueryExpression(expr, attr);
				expr.setJulietCode(generateRemoteQueryExpression(expr));
				return postProcessExpressionCodeGeneration(expr);
			}

	/**
	 * @param expr
	 * @param parmList
	 */
	private JulietCode generateRemoteQueryExpression(RemoteQueryExpression expr			) {
		JulietCode code = new JulietCode(); 
		try {
			DBLink link = new DBLink(expr.getRemoteLink());						
			int rquery =  pool.addString(expr.getRemoteQueryAsString());
			int linkName = pool.addString(link.getName());
			int hostName = pool.addString(link.getHost());
			int schemaName = pool.addString(link.getSchema());
			int user = pool.addString(link.getUser());
			
			if (expr.isParmDependent())
				// bind parms on the stack
				
				// put no. of parms on the stack
				// name of the dependent parameter
				// type of the parameter's signature - must be of Primitive Type

				for (NameExpression ne : expr.getParmDependentNames())
				{
					if ( ! (ne.getSignature() instanceof ValueSignature) )
						throw new EmiterException("Remote query parameter require value");
					int typeid =  ((ValueSignature) ne.getSignature()).getType().kindAsInt();
					int nameid = Database.getStore().addName(ne.name().value());
					int stringid = pool.addString(ne.name().value());
					code.append(JulietGen.genParamDependent(typeid, nameid, stringid));
				}
			code.append(JulietGen.genRemoteQuery(rquery, linkName, hostName, schemaName, link.getPort(), user, expr.getParmDependentNames().size(), expr.isAsynchronous() ? asynchronous_id : -1));
			return code;
		} catch (DatabaseException e) {
			throw new EmiterException(e);
		}
		
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitRenameExpression(odra.sbql.ast.expressions.RenameExpression, java.lang.Object)
	 */
	@Override
	public Object visitRenameExpression(RenameExpression expr, Object attr)
			throws SBQLException {		
		super.visitRenameExpression(expr, attr);
		if(isCollection(expr.getSignature()))
			expr.setJulietCode(JulietGen.genColRename(name2Id(expr.name().value()), expr.getExpression().getJulietCode()));
		else
			expr.setJulietCode(JulietGen.genRename(name2Id(expr.name().value()), expr.getExpression().getJulietCode()));
		return postProcessExpressionCodeGeneration(expr);
	}
	
	
}