/**
 * 
 */
package odra.sbql.interpreter.helper;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBLink;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBObjectFactory;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MBVirtualVariable;
import odra.db.objects.meta.MetaObjectKind;
import odra.sbql.interpreter.InterpreterException;
import odra.sbql.interpreter.metabase.MetaNames;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;

/**
 * SBQLInterpreterMetaDereferenceHelper
 * 
 * @author Radek Adamus
 * @since 2007-10-02 last modified: 2007-10-02
 * @version 1.0
 */
public class MetaDereferenceHelper {


    static final SingleResult derefMetabaseObject(OID cpxobid, boolean compact)
	    throws DatabaseException, InterpreterException
    {
	StructResult strres = new StructResult();
	MBObject mbo = MBObjectFactory.getTypedMBObject(cpxobid);
	MetaObjectKind kind = mbo.getObjectKind();
	try
	{
	    strres.addField(binderResult(MetaNames.NAME.toString(), mbo.getName()));
	    strres.addField(binderResult(MetaNames.KIND.toString(), kind.toString()));
	    if(compact)
		return strres;
	    switch (kind) {
	    case VARIABLE_OBJECT:
		MBVariable mbvar = (MBVariable) mbo;
		strres.addField(binderResult(MetaNames.MIN_CARD.toString(), mbvar.getMinCard()));
		strres.addField(binderResult(MetaNames.MAX_CARD.toString(), mbvar.getMaxCard()));
		strres.addField(binderResult(MetaNames.TYPE.toString(), derefMetabaseObject(mbvar.getType(), true)));
		strres.addField(binderResult(MetaNames.REFERENCE.toString(), mbvar.getRefIndicator()));
		
		if (mbvar.hasReverseReference())
		{
		    strres.addField(binderResult(MetaNames.REVERSE.toString(), mbvar.getReversePointer()));		    
		}
		break;
	    case PRIMITIVE_TYPE_OBJECT:		
		return result(((MBPrimitiveType)mbo).getTypeKind().toString());
		
	    case TYPEDEF_OBJECT:
		MBTypeDef mbtd = (MBTypeDef) mbo;
		strres.addField(binderResult(MetaNames.TYPE.toString(), derefMetabaseObject(mbtd.getType(), true)));

		break;
	    case STRUCT_OBJECT:
		MBStruct mbstr = (MBStruct) mbo;
		for(OID fld : mbstr.getFields()){
		    strres.addField(binderResult(MetaNames.FIELD.toString(), derefMetabaseObject(fld,true)));
		}
		

		break;
	    case PROCEDURE_OBJECT:
		MBProcedure mbproc = (MBProcedure) mbo;
		strres.addField(binderResult(MetaNames.MIN_CARD.toString(), mbproc.getMinCard()));
		strres.addField(binderResult(MetaNames.MAX_CARD.toString(), mbproc.getMaxCard()));
		strres.addField(binderResult(MetaNames.TYPE.toString(), mbproc.getType()));
		strres.addField(binderResult(MetaNames.REFERENCE.toString(), mbproc.getRefIndicator()));
		strres.addField(binderResult(MetaNames.AST.toString(), "unprintable"));
		
		for(OID arg : mbproc.getArguments()){
		    strres.addField(binderResult(MetaNames.ARGUMENT.toString(), derefMetabaseObject(arg, true)));
		}
		
		

		break;
	    case VIRTUAL_VARIABLE_OBJECT:
		MBVirtualVariable mbvproc = (MBVirtualVariable) mbo;
		strres.addField(binderResult(MetaNames.MIN_CARD.toString(), mbvproc.getMinCard()));
		strres.addField(binderResult(MetaNames.MAX_CARD.toString(), mbvproc.getMaxCard()));
		strres.addField(binderResult(MetaNames.TYPE.toString(), derefMetabaseObject(mbvproc.getType(), true)));
		strres.addField(binderResult(MetaNames.REFERENCE.toString(), mbvproc.getRefIndicator()));
		strres.addField(binderResult(MetaNames.VIEW.toString(), derefMetabaseObject(mbvproc.getView(), true)));
		
		break;
	    case CLASS_OBJECT:
		MBClass cls = (MBClass) mbo;
		if(cls.hasInstanceName())
		    strres.addField(binderResult(MetaNames.INSTANCE.toString(), cls.getInstanceName()));
		
		
		for(OID fld : new MBStruct(cls.getType()).getFields()){
		    strres.addField(binderResult(MetaNames.FIELD.toString(), derefMetabaseObject(fld, true)));
		}
		
		
		for(OID mth : cls.getMethods()){
		    strres.addField(binderResult(MetaNames.METHOD.toString(), derefMetabaseObject(mth, true)));
		}		
		
		for(OID scls : cls.getDirectSuperClasses()){
		    strres.addField(binderResult(MetaNames.SUPERCLASS.toString(), derefMetabaseObject(scls,true)));
		}

		break;
	    case VIEW_OBJECT:
		MBView mbview = (MBView) mbo;
		
		for(OID prc : mbview.getGenProcs()){
		    strres.addField(binderResult(MetaNames.GENPROC.toString(), derefMetabaseObject(prc,true)));
		}
				
		for(OID sv : mbview.getSubViewsEntry().derefComplex()){
		    strres.addField(binderResult(MetaNames.SUBVIEW.toString(), derefMetabaseObject(sv, true)));
		}
		
		strres.addField(binderResult(MetaNames.VIRTUAL_OBJECT.toString(), derefMetabaseObject(mbview.getVirtualObject(),true)));
		for(OID fld : mbview.getViewFieldsEntry().derefComplex()){
		    strres.addField(binderResult(MetaNames.VIEW_FIELD.toString(), derefMetabaseObject(fld, true)));
		}

		break;
	    case LINK_OBJECT:
		MBLink mblink = (MBLink)mbo;
		
		strres.addField(binderResult(MetaNames.HOST.toString(), mblink.getHost()));
		strres.addField(binderResult(MetaNames.PORT.toString(), mblink.getPort()));
		strres.addField(binderResult(MetaNames.SCHEMA.toString(), mblink.getSchema()));
		
		break;

	    default:
		assert false : "unimplemented";
	    }

	} catch (Exception e)
	{
	    throw new InterpreterException(e.getMessage(), e);
	}

	return strres;

    }

    private static final SingleResult result(Object value)throws DatabaseException{
	if(value instanceof Integer){
	    return integerResult((Integer)value);
	}else if(value instanceof String){
	    return stringResult((String)value);
	}else if(value instanceof OID){
	    if(new MBPrimitiveType((OID)value).isValid())
		return result(new MBPrimitiveType((OID)value).getTypeKind().toString());
	    return referenceResult((OID)value);
	}
	assert false : "unimplemented";
	    return null;
    }
    private static final BinderResult binderResult(String name, Result value) 
    {
	return new BinderResult(name, value);
    }
    
    private static final BinderResult binderResult(String name, Object value) throws DatabaseException
    {
	return new BinderResult(name, result(value));
    }
    private static final StringResult stringResult(String value) 
    {
	return new StringResult(value);
    }
    
    private static final IntegerResult integerResult(int value) 
    {
	return new IntegerResult(value);
    }
    
    private static final ReferenceResult referenceResult(OID value) 
    {
	return new ReferenceResult(value);
    }
       
}
