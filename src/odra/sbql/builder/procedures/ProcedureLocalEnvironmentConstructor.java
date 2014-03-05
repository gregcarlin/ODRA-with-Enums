package odra.sbql.builder.procedures;

import java.util.Stack;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraVariableSchema;
import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.RecordTypeDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.SingleCatchBlock;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.builder.OrganizerException;

/**
 * Create schema info for creating local metabase for the procedure it is responsible for collecting informations about
 * variable declarations and local block naming ProcedureLocalEnvironmentConstructor
 * 
 * @author Radek Adamus last modified: 2007-03-06
 * @version 1.0
 */
public final class ProcedureLocalEnvironmentConstructor extends TraversingASTAdapter {

	private final Stack<String> blocksNames;

	private final Stack<Integer> blocksNumbers;

	private final OdraProcedureSchema procinfo;

	private final DBModule mod;

	public ProcedureLocalEnvironmentConstructor(DBModule mod, OdraProcedureSchema procinfo)  {
		this.blocksNames = new Stack<String>();
		this.blocksNumbers = new Stack<Integer>();
		this.mod = mod;
		try {
		    this.setSourceModuleName(mod.getName());
		} catch (DatabaseException e) {
		    throw new OrganizerException(e);
		}
		this.procinfo = procinfo;
		// procinfo.insertBlock(procinfo.pname);
		this.blocksNames.push(OdraProcedureSchema.MAIN_LOCAL_BLOCK_NAME);
		this.blocksNumbers.push(0);
	}

	public void constructProcedureLocalMetadata(Statement ast) throws SBQLException {
		
			ast.accept(this, null);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.TraversingASTAdapter#visitBlockStatement(odra.sbql.ast.statements.BlockStatement,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitBlockStatement(BlockStatement stmt, Object attr) throws SBQLException {
	    
		int num = this.blocksNumbers.pop();
		this.blocksNumbers.push(num + 1);
		String blockName = this.blocksNames.peek() + "." + (num + 1);
		// this.procinfo.insertBlock(blockName);
		this.blocksNames.push(blockName);
		this.blocksNumbers.push(0);
		stmt.getStatement().accept(this, attr);
		if(attr != null && attr instanceof SingleCatchBlock){ //this is catch block
		    SingleCatchBlock cb = (SingleCatchBlock)attr;
		    OdraVariableSchema excVar = new OdraVariableSchema(cb.getExceptionName(), cb.getExceptionTypeName(), 1, 1, 0);
		    this.procinfo.insertCatchBlockExceptionVariable(blockName, excVar);
		    cb.setCatchBlockName(blockName);
		    }
		this.blocksNames.pop();
		this.blocksNumbers.pop();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.TraversingASTAdapter#visitVariableDeclarationStatement(odra.sbql.ast.statements.VariableDeclarationStatement,
	 *      java.lang.Object)
	 */
	@Override
	public Object visitVariableDeclarationStatement(VariableDeclarationStatement stmt, Object attr) throws SBQLException {
		stmt.getTypeDeclaration().accept(this, attr);		
		stmt.setVariableTypeName(stmt.getTypeDeclaration().getTypeName());

		procinfo.insertLocalVariable(this.blocksNames.peek(), new OdraVariableSchema(stmt.getVariableName(), stmt.getVariableTypeName(), stmt.getMinCard(), stmt.getMaxCard(), stmt.getReflevel()));
		return null;
	}


	/**
	 * When a type is an anonymous structure, we create an MBStruct object in the module metabase with a random name,
	 * inaccesible to the end user.
	 */
	public Object visitRecordTypeDeclaration(RecordTypeDeclaration decl, Object attr) throws SBQLException {
		try {
		    MBStruct mbrec = new MBStruct(new MetabaseManager(mod).createMetaStruct(0));

		    for (SingleFieldDeclaration f : decl.getRecordTypeFields()) {
		    	VariableDeclaration vd = ((VariableFieldDeclaration) f).getVariableDeclaration();
		    	vd.getType().accept(this, null);
		    	mbrec.createField(vd.getName(), vd.getCardinality().getMinCard(), vd.getCardinality().getMaxCard(), vd.getType().getTypeName(), vd.getReflevel());
		    }

		    decl.setTypeName(mbrec.getName());
		} catch (DatabaseException e) {
		    throw new OrganizerException(e, decl, this);
		}

		return null;
	}

	/**
	 * Called when a type is a simple name (that is it not an anonymous structure).
	 */
	public Object visitNamedTypeDeclaration(NamedTypeDeclaration decl, Object attr) throws SBQLException {
		// TODO it might be a reference to local variable!!
		decl.setTypeName(decl.getName().nameAsString());

		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.TraversingASTAdapter#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	@Override
	public Object visitTryCatchFinallyStatement(
		TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	{
	    stmt.getTryStatement().accept(this, attr);
	    for(SingleCatchBlock cb : stmt.getCatchBlocks().flattenCatchBlocks()){
		cb.getCatchVariable().accept(this, attr);
		cb.setExceptionName(cb.getCatchVariable().getName());
		cb.setExceptionTypeName(cb.getCatchVariable().getType().getTypeName());
		cb.getStatement().accept(this, cb);
	    }
	    stmt.getFinallyStatement().accept(this, attr);
	    return null;
	}
	
}