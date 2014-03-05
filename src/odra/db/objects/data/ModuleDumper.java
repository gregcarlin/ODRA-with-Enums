package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.objects.meta.*;
import odra.util.DateUtils;

/**
 * This class dumps internal information of a module
 * and returns it as a string. Used for debugging purposes.
 * 
 * @author raist
 */
public class ModuleDumper {
	private DBModule module;
	private IDataStore store;
	
	public ModuleDumper(DBModule module) {
		this.module = module;
		this.store = module.getOID().getStore();
	}

	public String dump() throws DatabaseException {	
		String mod = "";

		mod += "Global name: " + module.getModuleGlobalName() + "\n";
		mod += "OID: &" + module.getOID().toString() + "\n"; 
		mod += "Linked: " + module.getLinkedRef().derefBoolean() + "\n";
		mod += "Compiled: " + module.getCompiledRef().derefBoolean() + "\n";

		mod += "Imports:\n";
		OID[] imports = module.getImportsRef().derefComplex();
		for (int i = 0; i < imports.length; i++)
			mod += "\t" + i + ":\t" + imports[i].derefString() +"\n";

		mod += "Compiled imports:\n";
		OID[] cimports = module.getCompiledImportsRef().derefComplex();
		for (int i = 0; i < cimports.length; i++)
			mod += "\t" + i + ":\t&" + cimports[i].derefReference().toString() +"\n";
		
		mod += "Implements:\n";
		OID[] impl = module.getImplementsRef().derefComplex();
		for (int i = 0; i < impl.length; i++)
			mod += "\t" + i + ":\t" + impl[i].derefString() +"\n";

		mod += "Metareferences:\n";
		OID[] metarefs = module.getMetaReferencesRef().derefComplex();
		for (int i = 0; i < metarefs.length; i++)
			mod += "\t" + i + ":\t" + metarefs[i].derefString() + "\n";

		mod += "Compiled metareferences:\n";
		OID[] cmetarefs = module.getCompiledMetaReferencesRef().derefComplex();
		for (int i = 0; i < cmetarefs.length; i++)
			mod += "\t" + i + ":\t&" + cmetarefs[i].derefReference().toString() +"\n";

		mod += "Metadata:\n";
		mod += dumpMetadata(module, module.getMetaRef());

		mod += "Data:\n";
		mod += dumpData(module, module.getDataRef());

		mod += "Submodules:\n";
		mod += dumpSubmodules();
		
		return mod;
	}
		
	public String dumpData(DBModule mod, OID parent) throws DatabaseException {
		String datastr = "";
		
		OID[] children = parent.derefComplex();
		for (int i = 0; i < children.length; i++)			
			datastr += dumpData(mod, children[i], "");
		
		return datastr;
	}

	public String dumpMetadata(DBModule mod, OID parent) throws DatabaseException {
		String metastr = "";

		for (OID c : parent.derefComplex()) {
			if (c.isComplexObject())
				metastr += dumpMetadata(mod, c, "");
			else if (c.isAggregateObject()) {
				for (OID ac : c.derefComplex())
					metastr += dumpMetadata(mod, ac, "");
			}
			else
				assert false : "not a complex/aggregate object";
		}

		return metastr;
	}

	public String dumpData(DBModule mod, OID parent, String indend) throws DatabaseException {
		String datastr = "";

		if (parent.isIntegerObject())
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") = " + parent.derefInt() + " (integer object)\n";
		else if (parent.isStringObject())
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") = \"" + removeNewLines(parent.derefString()) + "\" (string object)\n";
		else if (parent.isBooleanObject())
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") = " + parent.derefBoolean() + " (boolean object)\n";
		else if (parent.isDoubleObject())
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") = " + parent.derefDouble() + " (double object)\n";
		else if (parent.isDateObject())
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") = " + DateUtils.format(parent.derefDate()) + " (date object)\n";
		else if (parent.isReferenceObject())
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") = &" + parent.derefReference().toString() + " (reference object)\n";
		else if (parent.isPointerObject())
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") = &" + parent.derefReference().toString() + " (pointer object)\n";
		else if (parent.isBinaryObject())
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") = <unprintable> (binary object)\n";
		else if (parent.isAggregateObject()) {
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") (aggregate object)\n";

			OID[] children = parent.derefComplex();

			for (int i = 0; i < children.length; i++)
				datastr += dumpData(mod, children[i], indend + " ");			
		}
		else if (parent.isComplexObject()) {
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") (complex object)\n";

			OID[] children = parent.derefComplex();

			for (int i = 0; i < children.length; i++)
				datastr += dumpData(mod, children[i], indend + " ");
		}
		else
			datastr += "\t" + parent.toString() + "\t\t" + indend + "#" + parent.getObjectNameId() + " (" + parent.getObjectName() + ") (unknown object)\n";

		return datastr;
	}

	public String dumpMetadata(DBModule mod, OID parent, String indend) throws DatabaseException {
		String metastr = "";

		MBObject mbo = new MBObject(parent);

		switch (mbo.getObjectKind()) {
			case PRIMITIVE_TYPE_OBJECT:
				metastr += new MBPrimitiveType(parent).dump(indend);
				break;

			case TYPEDEF_OBJECT:
				metastr += new MBTypeDef(parent).dump(indend);
				break;

			case STRUCT_OBJECT:
				metastr += new MBStruct(parent).dump(indend);
				break;

			case VARIABLE_OBJECT:
				metastr += new MBVariable(parent).dump(indend);
				break;
				
			case ENUM_OBJECT:
				metastr += new MBEnum(parent).dump(indend);
				break;	

			case CLASS_OBJECT:
				metastr += new MBClass(parent).dump(indend);
				break;
				
			case INTERFACE_OBJECT:
				metastr += new MBInterface(parent).dump(indend);
				break;

			case PROCEDURE_OBJECT:
				metastr += new MBProcedure(parent).dump(indend);
				break;

			case BINARY_OPERATOR_OBJECT:
				metastr += new MBBinaryOperator(parent).dump(indend);
				break;

			case UNARY_OPERATOR_OBJECT:
				metastr += new MBUnaryOperator(parent).dump(indend);
				break;

			case ANNOTATED_VARIABLE_OBJECT:
				metastr += new MBAnnotatedVariableObject(parent).dump(indend);
				break;
				
			case VIEW_OBJECT:
				metastr += new MBView(parent).dump(indend);
				break;
				
			case VIRTUAL_VARIABLE_OBJECT:
				metastr += new MBVirtualVariable(parent).dump(indend);
				break;
				
			case LINK_OBJECT:
				metastr += new MBLink(parent).dump(indend);
				break;

			default:
				metastr += "\t" + parent.toString() + "\t\t" + indend + parent.getObjectName() + " (unknown object " + mbo.getObjectKind() + ")\n";
		}

		return metastr;
	}

	private String dumpSubmodules() throws DatabaseException {
		String substr = "\t";

		OID[] suboid = module.getSubmodules();

		for (int i = 0; i < suboid.length; i++) {
			substr += new DBModule(suboid[i]).getName();

			if (i < suboid.length - 1)
				substr += ", ";
		}
		
		return substr;
	}

	private final String removeNewLines(String str) {
		return str.replaceAll("\n", " ");
	}
}
