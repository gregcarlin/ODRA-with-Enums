package odra.sbql.typechecker;

import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.StdEnvironment;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MBVirtualVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.sbql.ast.expressions.AuxiliaryNameGeneratorExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import odra.sbql.results.compiletime.ValueSignature;
import odra.sbql.results.compiletime.util.ValueSignatureType;
import odra.system.config.ConfigDebug;

public class SBQLTypeCheckerHelper {

	/**
	 * creates a signature for a given type representation of procedure return
	 * type
	 * 
	 * @param mbproc
	 *            - a meta-objects representing procedure
	 * @return calculated signature
	 * @throws DatabaseException
	 */

	static final Signature inferSignature(OID type, int minCard, int maxCard) throws DatabaseException {
		Signature sig;

		sig = inferSignature(type);
		if (sig instanceof StructSignature) {
			sig = flattenStruct(sig);
			sig.setMinCard(Signature.cardinalityMult(sig.getMinCard(), minCard));
			sig.setMaxCard(Signature.cardinalityMult(sig.getMaxCard(), maxCard));
		} else {
			sig.setMinCard(minCard);
			sig.setMaxCard(maxCard);
		}
		return sig;
	}

	/**
	 * creates a signature for a given meta-object
	 * 
	 * @param mbobjectid
	 *            - an oid of meta-objects
	 * @return calculated signature
	 * @throws DatabaseException
	 */
	static final Signature inferSignature(OID type) throws DatabaseException {
		Signature sig = null;
		String[] typeName = { null };

		MetaObjectKind ok = new MBObject(type).getObjectKind();
		if (ok == MetaObjectKind.TYPEDEF_OBJECT) {
			type = enforceExpandTypeDef(type, typeName);
			ok = new MBObject(type).getObjectKind();
		}
		switch (ok) {

		case STRUCT_OBJECT: {
			StructSignature ssig = new StructSignature();
			ssig.setTypeName(typeName[0]);
			MBStruct mbstruct = new MBStruct(type);
			for (OID fld : mbstruct.getFields()) {
				ssig.addField(inferFieldSignature(fld));
			}
			sig = ssig;
		}
			break;
		case ENUM_OBJECT:{
			MBEnum mbenu = new MBEnum(type);
			sig = inferSignature(mbenu.getType());
			if(mbenu.getState())
				sig.setEnumerator(mbenu.getName());
		}
			break;
		case CLASS_OBJECT: {
			StructSignature ssig = new StructSignature();
			MBClass mbclass = new MBClass(type);
			Vector<OID> types = mbclass.getFullType();
			for (OID stype : types) {
				MBStruct mbstruct = new MBStruct(stype);
				for (OID fld : mbstruct.getFields()) {
					ssig.addField(inferFieldSignature(fld));					
				}
			}
			sig = ssig;
		}
			break;
		case PRIMITIVE_TYPE_OBJECT:
			ValueSignature vsig = new ValueSignature(type);
			vsig.setTypeName(typeName[0]);
			sig = vsig;
			break;
		case VARIABLE_OBJECT:
			sig = new ReferenceSignature(type, true);
			break;
		case VIRTUAL_VARIABLE_OBJECT:
			sig = new ReferenceSignature(new MBVirtualVariable(type).getView());
			((ReferenceSignature) sig).setVirtual(true);
			break;
		case PROCEDURE_OBJECT:
			sig = new ReferenceSignature(type);
			break;
		default:
			if (ConfigDebug.ASSERTS)
				assert false : "unknown object type";
			break;
		}

		return sig;

	}

	/**
	 * @param fld
	 * @return
	 * @throws DatabaseException
	 */
	private static Signature inferFieldSignature(OID fld) throws DatabaseException {
		MBVariable fldvar = new MBVariable(fld);
		assert fldvar.isValid() : "fldvar.isValid()";
		Signature val = inferSignature(fldvar.getType());
		val.setCardinality(1, 1);
		val = flattenStruct(val);				
		BinderSignature fldSig = new BinderSignature(fld.getObjectName(), val);
		fldSig.setCardinality(Signature.cardinalityMult(val.getMinCard(), fldvar.getMinCard()), Signature.cardinalityMult(val.getMaxCard(),fldvar.getMaxCard()));

		return fldSig;
	}

	/**
	 * if sig is StructSignature with only one field the field is returned
	 * 
	 * @param sig
	 * @return
	 */
	private static Signature flattenStruct(Signature sig) {
		Signature result = sig;
		while (sig instanceof StructSignature && ((StructSignature) sig).fieldsNumber() == 1) {
			result = ((StructSignature) sig).getFields()[0];
			result.setMinCard(Signature.cardinalityMult(sig.getMinCard(), result.getMinCard()));
			result.setMaxCard(Signature.cardinalityMult(sig.getMaxCard(), result.getMaxCard()));
			sig = result;
		}
		return result;
	}

	/**
	 * returns OIDs representing fields in the given type
	 * 
	 * @param the
	 *            type
	 * @return OIDs table (if complex type) otherwise OID[0]
	 */
	static final OID[] getTypeFields(OID type) throws DatabaseException {
		switch (new MBObject(type).getObjectKind()) {
		case STRUCT_OBJECT:
			return new MBStruct(type).getFields();
		case CLASS_OBJECT:

			Vector<OID> types = new Vector<OID>();
			for (OID strid : new MBClass(type).getFullType()) {
				for (OID field : new MBStruct(strid).getFields()) {
					types.add(field);
				}
			}
			return types.toArray(new OID[types.size()]);

		default:
			return new OID[0];
		}
	}

	/**
	 * If the argument is a metabase entry of a typedef, expand it. Otherwise
	 * just return the argument unexpanded.
	 * 
	 * @param type
	 *            is the metabase node of the type to be possibly expanded.
	 * @param typeName
	 *            is the output parameter to return the typeName if the type is
	 *            distinct.
	 * @return the metabase node after possible expansion.
	 */
	static final OID enforceExpandTypeDef(OID type, String[] typeName) throws DatabaseException {
		MBTypeDef mbtype = new MBTypeDef(type);
		// if the type of the variable is a typeDef, expand it
		if (mbtype.isValid()) {
			if (mbtype.isDistinct())
				typeName[0] = mbtype.getName();
			return mbtype.getType();
		}
		// otherwise leave the type unexpanded

		return type;
	}

	/**
	 * Performs the static (recursive) derefence of a metabase object.
	 * 
	 * @param type
	 *            is the metabase node to be dereferenced.
	 * @return the signature of the result of the dereference.
	 */
	static final Signature performDeref(OID type) throws DatabaseException {
		StdEnvironment env = StdEnvironment.getStdEnvironment();
		MBObject mbobj = new MBObject(type);
		OID forcedType = null;
		String[] typeName = { null };
		Signature rsig = null;
		// check the kind of the object
		// only metavariables can be referenced
		switch (mbobj.getObjectKind()) {
		case VARIABLE_OBJECT:
			MBVariable mbvar = new MBVariable(mbobj.getOID());
			
			//if(mbvar.isTypeEnum()){
				//return new ReferenceSignature(mbvar.getType());
				
			//}

			// if the type is a typedef, expand it
			forcedType = enforceExpandTypeDef(mbvar.getType(), typeName);

			// if it is a dereference of a pointer object
			if (mbvar.isTypeReference()) {
				// return the signature of the referenced variable
				return new ReferenceSignature(forcedType);
			}

			break;

		case TYPEDEF_OBJECT:
		case PRIMITIVE_TYPE_OBJECT:
		case STRUCT_OBJECT:
		case CLASS_OBJECT:
			forcedType = enforceExpandTypeDef(type, typeName);
			break;
		case PROCEDURE_OBJECT:
			forcedType = type;
			break;

		default:
			throw new TypeCheckerException("Cannot dereference metaobject " + type.getObjectName());
		}

		MBObject mbtype = new MBObject(forcedType);

		switch (mbtype.getObjectKind()) {
		case PRIMITIVE_TYPE_OBJECT:
			// get the type of the deferenced variable
			if (forcedType.equals(env.stringType)) {
				rsig = new ValueSignature(env.stringType);
				((ValueSignature) rsig).setType(ValueSignatureType.STRING_TYPE);
			} else if (forcedType.equals(env.integerType)) {
				rsig = new ValueSignature(env.integerType);
				((ValueSignature) rsig).setType(ValueSignatureType.INTEGER_TYPE);
			} else if (forcedType.equals(env.realType)) {
				rsig = new ValueSignature(env.realType);
				((ValueSignature) rsig).setType(ValueSignatureType.REAL_TYPE);
			} else if (forcedType.equals(env.booleanType)) {
				rsig = new ValueSignature(env.booleanType);
				((ValueSignature) rsig).setType(ValueSignatureType.BOOLEAN_TYPE);
			} else if (forcedType.equals(env.dateType)) {
				rsig = new ValueSignature(env.dateType);
				((ValueSignature) rsig).setType(ValueSignatureType.DATE_TYPE);
			} else
				assert false : "unexpected type " + forcedType.getObjectName();
			break;

		case STRUCT_OBJECT: {
			MBStruct mbstruct = new MBStruct(forcedType);
			OID[] flds = mbstruct.getFields();
			StructSignature ssig = new StructSignature();

			for (OID fld : flds) {
				MBObject mbfld = new MBObject(fld);
				// TODO bug: compiletime dereference of recursive type result in
				// java stack overflow
				BinderSignature bsig = new BinderSignature(mbfld.getName(), performDeref(fld));
				bsig.setMinCard(mbfld.getMinCard());
				bsig.setMaxCard(mbfld.getMaxCard());
				ssig.addField(bsig);
			}
			if (ssig.fieldsNumber() == 1)
				rsig = ssig.getFields()[0];
			else
				rsig = ssig;
		}
			break;
		case ENUM_OBJECT: {
			MBEnum mbenum = new MBEnum(forcedType);
			//rsig = new ValueSignature(mbenum.getType());
			rsig = performDeref(mbenum.getType());
			//here rsig.setEnumerator(mbenum.getName());
		}
			break;
		case CLASS_OBJECT: {
			StructSignature ssig = new StructSignature();
			MBClass mbclass = new MBClass(forcedType);
			Vector<OID> types = mbclass.getFullType();
			for (OID stype : types) {
				MBStruct mbstruct = new MBStruct(stype);
				for (OID fld : mbstruct.getFields()) {
					ssig.addField(new BinderSignature(fld.getObjectName(), performDeref(fld)));
				}
			}
			rsig = ssig;
		}
			break;
		case PROCEDURE_OBJECT:
			// cannot dereference procedure - simply return reference
			rsig = new ReferenceSignature(type);
			break;
		default:
			throw new TypeCheckerException("Unable to dereference object " + type.getObjectName());
		}

		rsig.setTypeName(typeName[0]);
		rsig.setMinCard(mbobj.getMinCard());
		rsig.setMaxCard(mbobj.getMaxCard());
		return rsig;
	}

	/**
	 * I think this is only a stub
	 * 
	 * @param expected
	 *            primitive type
	 * @param current
	 *            primitive type
	 * @return
	 */
	static final OID findPrimitiveCoerce(OID expected, OID current) {
		StdEnvironment env = StdEnvironment.getStdEnvironment();
		if (expected.equals(env.realType)) {
			if (current.equals(env.integerType))
				return env.realType;
		}
		if (expected.equals(env.stringType)) {
			return env.stringType;
		}
		return null;
	}

	static final OID findGlobalMetaVariable(String name, DBModule mod, boolean withVirtual, boolean withImports) throws DatabaseException {
		OID found = mod.findFirstByName(name, mod.getMetabaseEntry());
		if (found == null && withImports) {
			for (OID importm : mod.getCompiledImports()) {
				DBModule impmod = new DBModule(importm.derefReference());
				found = impmod.findFirstByName(name, impmod.getMetabaseEntry());
				if (found != null)
					break;
			}
		}
		if (found != null) {
			if (withVirtual) {
				if (new MBVirtualVariable(found).isValid())
					return found;
			}
			if (new MBVariable(found).isValid())
				return found;

		}
		return null;

	}

	static final OID findTemporalMetaVariable(String name, DBModule mod, boolean withImports) throws DatabaseException {
		OID found = mod.findFirstByName(name, mod.getSessionMetaDataEntry());
		if (found == null && withImports) {
			for (OID importm : mod.getCompiledImports()) {
				DBModule impmod = new DBModule(importm.derefReference());
				found = impmod.findFirstByName(name, impmod.getSessionMetaDataEntry());
				if (found != null)
					break;
			}
		}
		if (found != null) {
			if (new MBVariable(found).isValid())
				return found;
		}
		return null;
	}

	/**
	 * Seach for a sub view definition by name of virtual objects
	 * 
	 * @param view
	 * @param name
	 * @return
	 * @throws DatabaseException
	 */
	static final OID findSubViewByVirtualObjectName(MBView view, int name) throws DatabaseException {

		for (OID subvirtualobject : view.getVirtualFieldsEntry().derefComplex()) {
			// if(new
			// MBView(subv).getVirtualObject().getObjectName().equals(name))
			if (subvirtualobject.getObjectNameId() == name)
				return new MBVirtualVariable(subvirtualobject).getView();

		}
		return null;
	}

	/**
	 * Returns the variable that is pointed by the pointer object (pass the
	 * pointer - to - pointer) always return the variable that is not a pointer
	 * object
	 * 
	 * @param varid
	 * @return
	 * @throws DatabaseException
	 */
	static final OID getPointedVariable(OID varid) throws DatabaseException {
		MBVariable var = new MBVariable(varid);
		assert var.isValid() : "not a MBVariable " + new MBObject(varid).getObjectKind();
		while (var.isTypeReference()) {
			var = new MBVariable(var.getType());
		}
		return var.getOID();
	}

	static final boolean isSimpleType(OID val) {
		StdEnvironment env = StdEnvironment.getStdEnvironment();
		if (val.equals(env.stringType) || val.equals(env.integerType) || val.equals(env.realType) || val.equals(env.booleanType)
				|| val.equals(env.dateType))
			return true;
		return false;
	}

	static final boolean isVirtualObject(OID val) throws DatabaseException {
		MBVirtualVariable virtarg = new MBVirtualVariable(val);
		return virtarg.isValid();
	}

	static final String printCardinality(int min, int max) {
		return "[" + min + ".." + (max == Integer.MAX_VALUE ? "*" : max) + "]";
	}

	static final boolean isAuxiliaryNameGeneratorExpression(Expression e) {
		return (e instanceof AuxiliaryNameGeneratorExpression);
	}

	static final boolean isStructConstructorExpression(Expression e) {
		return (e instanceof CommaExpression);// PROBLEM with Join &
												// dereference|| e instanceof
												// JoinExpression);
	}

	static final boolean isBagConstructorExpression(Expression e) {
		return (e instanceof UnionExpression);
	}

	static final String getObjectName(OID objId) {
		try {
			return objId.getObjectName();
		} catch (DatabaseException e) {
			throw new TypeCheckerException(e);
		}
	}

	static final String getObjectName(MBObject obj) {
		try {
			return obj.getName();
		} catch (DatabaseException e) {
			throw new TypeCheckerException(e);
		}
	}

	static final int name2id(String name) {
		try {
			return Database.getNameIndex().addName(name);
		} catch (DatabaseException e) {
			throw new TypeCheckerException(e);
		}
	}

	static final NameExpression findAssociatedNameExpression(Signature sig, String name, boolean[] isAuxiliary) {
		assert isAuxiliary.length == 1 : "isAuxiliary.length == 1";
		boolean checkAuxiliary = true;
		Expression found = sig.getAssociatedExpression();
		while (found != null) {
			if (found instanceof NameExpression) {
				if (!((NameExpression) found).isAuxiliaryName())
					return (NameExpression) found;
				// else
				// checkAuxiliary = false;
			} else if (found instanceof AuxiliaryNameGeneratorExpression && ((AuxiliaryNameGeneratorExpression) found).name().value().equals(name)) {
				isAuxiliary[0] = true;
			}
			found = found.getSignature().getAssociatedExpression();

		}
		return null;
	}

	/**
	 * @param sig1
	 * @param sig2
	 * @return
	 */
	public static StructSignature createStructSignature(Signature sig1, Signature sig2) {
		// build the signature of the result of the visited expression
		StructSignature ssig = new StructSignature();

		// we clone the added signatures, because their cardinalities are to be
		// reset to [1..1]
		if (!(sig1 instanceof StructSignature))
			ssig.addField(sig1.clone());
		else {
			// flatten the signature on the fly (SBA does not allow nested
			// structs)
			Signature[] fields = ((StructSignature) sig1).getFields();
			for (Signature i : fields)
				ssig.addField(i.clone());
		}

		if (!(sig2 instanceof StructSignature))
			ssig.addField(sig2.clone());
		else {
			// flatten the signature on the fly
			Signature[] fields = ((StructSignature) sig2).getFields();
			for (Signature i : fields)
				ssig.addField(i.clone());
		}

		// reset the cardinalities of component signatures to [1..1]
		for (Signature i : ssig.getFields()) {
			i.setCardinality(1, 1);

		}

		// set the cardinality of result signature
		ssig.setMinCard(Signature.cardinalityMult(sig1.getMinCard(), sig2.getMinCard()));
		ssig.setMaxCard(Signature.cardinalityMult(sig1.getMaxCard(), sig2.getMaxCard()));
		return ssig;
	}
	
	public static Signature getSignature(OID type) throws DatabaseException {
		return inferSignature(type);
	}

	public static void checkEnumVariable(MBVariable mv, Signature sig2) throws DatabaseException{
		if(mv.isTypeEnum()){
			if(sig2 instanceof ReferenceSignature){
				ReferenceSignature rsig2 = (ReferenceSignature)sig2;
				if((new MBObject(rsig2.value).getObjectKind())==MetaObjectKind.VARIABLE_OBJECT){
					MBVariable mbvenu = new MBVariable(rsig2.value);
					if(mv.getType().equals(mbvenu.getType())) return;
				}
			}
		 throw new TypeCheckerException("Incorrect datatype for enum object "+ mv.getName());
	}
   }
}	
