package odra.sbql.builder.interfaces;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBInterface;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.declarations.InterfaceDeclaration;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.ast.declarations.VariableInterfaceFieldDeclaration;
import odra.sbql.builder.ModuleConstructor;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.builder.OrganizerException;

public class InterfaceConstructor extends ASTAdapter {
	private ModuleConstructor modctr;
	private ModuleOrganizer modorg;
	private DBModule mod;
	
	public InterfaceConstructor(ModuleConstructor modconstr, ModuleOrganizer modorg) {
		this.modctr = modconstr;
		this.modorg = modorg;
		this.mod = modctr.getConstructedModule();
	}

	public Object visitInterfaceDeclaration(InterfaceDeclaration decl, Object attr) throws SBQLException {
		String intname = decl.getInterfaceName();
		String insname = decl.getInstanceName();
		
		SingleFieldDeclaration[] fields = decl.getInterfaceBody().getFields();				
		String[] sintarr = decl.getExtends();		
		try {
		    createMetaInterface(intname, insname, fields, sintarr);
		    createDataInterface(intname, insname);
		} catch (DatabaseException e) {
		    throw new OrganizerException(e, decl, this);
		}
		

		return null;
	}

	private OID createMetaInterface(String intname, String insname, SingleFieldDeclaration[] fields, String[] sinterfaces) throws DatabaseException {
		OID ioid = mod.getMetaBase().createMetaInterface(intname, insname, sinterfaces);

		MBInterface mbint = new MBInterface(ioid);
		
		for (int i = 0; i < fields.length; i++) {
			if (fields[i] instanceof VariableInterfaceFieldDeclaration) {
				VariableInterfaceFieldDeclaration f = (VariableInterfaceFieldDeclaration) fields[i];
				
				assert f.getVariableDeclaration().getType() instanceof NamedTypeDeclaration : "unexpected interface field type";

				NamedTypeDeclaration ntd = (NamedTypeDeclaration) f.getVariableDeclaration().getType();

				mbint.addField(f.getVariableDeclaration().getName(), ntd.getName().nameAsString(), f.getVariableDeclaration().getCardinality().getMinCard(), f.getVariableDeclaration().getCardinality().getMaxCard(), f.getVariableDeclaration().getReflevel(), f.D2.encodeFlag());
			}
			else if (fields[i] instanceof ProcedureHeaderFieldDeclaration) {
				ProcedureHeaderDeclaration ph = ((ProcedureHeaderFieldDeclaration) fields[i]).getProcedureHeaderDeclaration();
				
				assert ph.getProcedureResult().getResultType() instanceof NamedTypeDeclaration : "unexpected interface field type";

				NamedTypeDeclaration ntd = (NamedTypeDeclaration) ph.getProcedureResult().getResultType();

				mbint.addProcedure(ph.getName(), ntd.getName().nameAsString(), ph.getProcedureResult().getResultMinCard(), ph.getProcedureResult().getResultMaxCard(), ph.getProcedureResult().getReflevel());
			}
			else
				assert false : "unexpected interface field";
		}

		return ioid;
	}

	private OID createDataInterface(String intname, String insname) throws DatabaseException {
		OID ioid = mod.createDataInterface(intname, insname);

		return ioid;
	}
}
