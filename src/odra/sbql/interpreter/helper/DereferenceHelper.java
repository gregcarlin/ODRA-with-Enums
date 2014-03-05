/**
 * 
 */
package odra.sbql.interpreter.helper;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBObject;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MetaObjectKind;
import odra.sbql.interpreter.InterpreterException;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.CollectionResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.system.config.ConfigDebug;

/**
 * SBQLInterpreterDereferenceHelper
 * 
 * @author Radek Adamus
 * @since 2007-10-02 last modified: 2007-10-02
 * @version 1.0
 */
public class DereferenceHelper {
    public final static Result dereference(Result val, boolean meta)
	    throws InterpreterException {
	CollectionResult res;
	if (val instanceof CollectionResult)
	    res = SBQLInterpreterHelper.createProperCollection(val);
	else
	    res = new BagResult();
	try {
	    for (SingleResult sres : val.elementsToArray()) {
		if (sres instanceof ReferenceResult
			&& !((ReferenceResult) sres).refFlag) {
		    OID ref = ((ReferenceResult) sres).value;
		    if (ref.isComplexObject()) {
			if (!meta && SBQLInterpreterHelper.isSpecialObject(ref)) {
			    throw new InterpreterException(
				    "Unable to dereference object "
					    + ref.getObjectName()
					    + " '"
					    + new DBObject(ref).getObjectKind()
						    .getKindAsString() + "'");
			} else if (meta && isMetabaseObject(ref)) {
			    res.addElement(MetaDereferenceHelper
				    .derefMetabaseObject(ref, false));
			} else {
			    StructResult strres = SBQLInterpreterHelper
				    .derefComplex(ref);
			    if (strres.fieldsCount() != 0)
				res.addElement(strres);
			}
		    } else
			res.addElement(derefSimple(ref));
		} else
		    res.addElement(sres);
	    }
	} catch (DatabaseException e) {
	    throw new InterpreterException(
		    "Database error during runtime dereference operation: "
			    + e.getMessage(), e);
	}
	return res.elementsCount() == 1 ? res.elementAt(0) : res;
    }

    private final static SingleResult derefSimple(OID soId)
	    throws InterpreterException, DatabaseException {
	switch (soId.getObjectKind()) {
	case BOOLEAN_OBJECT:
	    return new BooleanResult(soId.derefBoolean());
	case BINARY_OBJECT:
	    throw new InterpreterException(
		    "unable to dereference binary object ("
			    + soId.getObjectName() + ")");

	case DOUBLE_OBJECT:
	    return new DoubleResult(soId.derefDouble());

	case INTEGER_OBJECT:
	    return new IntegerResult(soId.derefInt());
	case STRING_OBJECT:
	    return new StringResult(soId.derefString());

	case DATE_OBJECT:
	    return new DateResult(soId.derefDate());
	case REFERENCE_OBJECT:
	case REVERSE_REFERENCE_OBJECT: {
	    OID target = soId.derefReference();
	    if (target == null)
		throw new InterpreterException("'" + soId.getObjectName()
			+ "' is a dangling pointer");
	    return new ReferenceResult(target);
	}

	case POINTER_OBJECT: {
	    OID ptgt = soId.derefReference();
	    if (ptgt.isComplexObject()) {
		return derefComplex(ptgt);
	    }
	    return derefSimple(ptgt);
	}
	case AGGREGATE_OBJECT:
	case COMPLEX_OBJECT:
	    assert false : "should be simple object";
	default:
	    throw new InterpreterException(
		    "unable to dereference object with name:"
			    + soId.getObjectName());

	}

    }

    /**
     * Perform dereference of a complex object
     * 
     * @param cpxobid -
     *                OID of a complex object
     * @return dereference result
     * @throws DatabaseException
     * @throws InterpreterException
     */
    static final StructResult derefComplex(OID cpxobid)
	    throws DatabaseException, InterpreterException {
	if (ConfigDebug.ASSERTS)
	    assert cpxobid.isComplexObject() : "oid of the complex object required";
	StructResult strres = new StructResult();
	for (OID obid : cpxobid.derefComplex()) {
	    String name = obid.getObjectName();

	    if (obid.isComplexObject()) {
		StructResult cmpxstr = derefComplex(obid);
		if (cmpxstr.fieldsCount() > 0)
		    strres.addField(new BinderResult(name, derefComplex(obid)));
	    } else if (obid.isAggregateObject()) {
		for (OID oi : obid.derefComplex()) {
		    if (oi.isComplexObject()) {
			StructResult aggstr = derefComplex(oi);
			if (aggstr.fieldsCount() > 0)
			    strres.addField(new BinderResult(name, aggstr));
		    } else {
			strres.addField(new BinderResult(oi.getObjectName(),
				derefSimple(oi)));
		    }
		}
	    } else
		strres.addField(new BinderResult(name, derefSimple(obid)));

	}
	return strres;
    }

    private static final boolean isMetabaseObject(OID oid)
	    throws DatabaseException {
	return new MBObject(oid).getObjectKind() != MetaObjectKind.UNKNOWN_OBJECT;
    }
}
