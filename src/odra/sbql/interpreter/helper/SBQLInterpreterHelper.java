/**
 * 
 */
package odra.sbql.interpreter.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.indices.Index;
import odra.db.indices.keytypes.KeyType;
import odra.db.links.RemoteDefaultStore;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBIndex;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBView;
import odra.db.objects.data.DBVirtualObjectsProcedure;
import odra.db.objects.data.DataObjectKind;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MetaObjectKind;
import odra.sbql.emiter.OpCodes;
import odra.sbql.interpreter.InterpreterException;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.CollectionResult;
import odra.sbql.results.runtime.ComparableResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.sbql.stack.SBQLStack;
import odra.store.DefaultStore;
import odra.store.TransientStore;
import odra.system.config.ConfigDebug;

/**
 * SBQLInterpreterHelper - utility helper class for SBQL intepreter 
 * @author Radek Adamus 
 * @version 1.0
 */
public final class SBQLInterpreterHelper {
	

	public static final OID createCopy(OID obj, OID where) throws DatabaseException{
		assert where.isComplexObject() || where.isAggregateObject(): "complex object required";
		int objnameid = obj.getObjectNameId();
		OID copy = null;
		switch(obj.getObjectKind()){
		case AGGREGATE_OBJECT:
		    OID[] asubs = obj.derefComplex();
			copy = where.createAggregateChild(objnameid, asubs.length, 
					obj.getAggregateMinCard(), obj.getAggregateMaxCard());
			for(OID sub:asubs){
				createCopy(sub, copy);
			}
		    break;
		case BOOLEAN_OBJECT:
		    copy = where.createBooleanChild(objnameid, obj.derefBoolean());
		    break;
		case BINARY_OBJECT:
		    byte[] val = obj.derefBinary();
			copy = where.createBinaryChild(objnameid, val, val.length);
		    break;
		case COMPLEX_OBJECT:
		    OID[] csubs = obj.derefComplex();
			copy = where.createComplexChild(objnameid, csubs.length);
			for(OID sub: csubs){
				createCopy(sub, copy);
			}
		    break;
		case DATE_OBJECT:
		    copy = where.createDateChild(objnameid, obj.derefDate());
		    break;
		case DOUBLE_OBJECT:
		    copy = where.createDoubleChild(objnameid, obj.derefDouble());
		    break;
		case INTEGER_OBJECT:
		    copy = where.createIntegerChild(objnameid, obj.derefInt());
		    break;
		case POINTER_OBJECT:
		    copy = where.createPointerChild(objnameid, obj.derefReference());
		    break;
		case REFERENCE_OBJECT:
		    copy =where.createReferenceChild(objnameid, obj.derefReference());
		    break;				    
		case STRING_OBJECT:
		    copy = where.createStringChild(objnameid, obj.derefString(),0);
		    break;
		case UNKNOWN_OBJECT:
		case REVERSE_REFERENCE_OBJECT:    
		    
		default:
		    assert false: "unimplemented copy for " + obj.getObjectKind().toString();
		break;
			
		    
		}
				
		return copy; 
	}
	
	public static final BagResult dynamicCreate(OID where, int nameid, Result objvalue)throws DatabaseException{
		BagResult result = new BagResult();
		//for dynamic create we always create aggregate (we have no information about cardinatily so we must assume *)
		//we should also check if 'where' is an aggregate
		OID agg;
		if(!where.isAggregateObject())
		    agg = SBQLInterpreterHelper.findAggObject(where.getStore(), nameid, where, objvalue.elementsCount());
		else
		    agg = where;
		for(SingleResult sinres: objvalue.elementsToArray()){
			if(sinres instanceof IntegerResult){
				result.addElement(new ReferenceResult(agg.createIntegerChild(nameid, ((IntegerResult)sinres).value)));
			}else if(sinres instanceof DoubleResult){
				result.addElement(new ReferenceResult(agg.createDoubleChild(nameid, ((DoubleResult)sinres).value)));
			}else if(sinres instanceof StringResult){
				result.addElement(new ReferenceResult(agg.createStringChild(nameid, ((StringResult)sinres).value, ((StringResult)sinres).value.length())));
			}else if(sinres instanceof BooleanResult){
				result.addElement(new ReferenceResult(agg.createBooleanChild(nameid, ((BooleanResult)sinres).value)));
			}else if(sinres instanceof ReferenceResult){
				result.addElement(new ReferenceResult(agg.createReferenceChild(nameid, ((ReferenceResult)sinres).value)));
			}else if(sinres instanceof DateResult){
				result.addElement(new ReferenceResult(agg.createDateChild(nameid, ((DateResult)sinres).value)));
			}else if(sinres instanceof StructResult){
				StructResult strres = (StructResult)sinres;
				SingleResult[] fields = strres.fieldsToArray();
				BinderResult[] bfields = new BinderResult[fields.length];
				for(int i = 0; i < fields.length; i++){
					if(fields[i] instanceof BinderResult)
						bfields[i] = (BinderResult)fields[i];
					else throw new InterpreterException("field in the structure is unnamed, unable to create unnamed object");						
				}
				OID complex = agg.createComplexChild(nameid, fields.length);
				for(BinderResult bfield: bfields){
					result.addAll(SBQLInterpreterHelper.dynamicCreate(complex, Database.getNameIndex().addName(bfield.getName()), bfield.value));
				}
			}else 
				assert false : "unimplemented result type in dynamic create";
		}
		return result;
	}
	/** Perform dereference of a complex object
	 * @param cpxobid - OID of a complex object
	 * @return dereference result
	 * @throws DatabaseException
	 * @throws InterpreterException
	 */
	public static final StructResult derefComplex(OID cpxobid) throws DatabaseException,
	InterpreterException {
		return DereferenceHelper.derefComplex(cpxobid);
}

	public static final CollectionResult createProperCollection(Result colres) {
		if (colres instanceof BagResult || colres instanceof SingleResult)
			return new BagResult();
		else if(colres instanceof SingleResult)
		    return new BagResult();
		// else if(colres instanceof SequenceResult)
		// return new SequenceResult();
		else
			if(ConfigDebug.ASSERTS) assert false : "unknown collection type";
		return null;
	}

	/**
	 * @param svalue
	 * @return 
	 */
	public static final IntegerResult dynamic2Int(SingleResult svalue)
			throws InterpreterException {
		if (svalue instanceof StringResult)
			try {
				return new IntegerResult(Integer
						.parseInt(((StringResult) svalue).value));
			} catch (NumberFormatException e) {
				throw new InterpreterException("cannot coerce '" + ((StringResult) svalue).value + "' to integer");
			}

		else if (svalue instanceof IntegerResult)
			return (IntegerResult) svalue;
		else
			throw new InterpreterException("to integer coerce cannot be applied to " + printFriendyResultType(svalue));
	}

	public static final DoubleResult dynamic2Real(SingleResult svalue)
			throws InterpreterException {

		if (svalue instanceof StringResult)
			try {
				return new DoubleResult(Double
						.parseDouble(((StringResult) svalue).value));
			} catch (NumberFormatException e) {
				throw new InterpreterException("cannot coerce '" + ((StringResult) svalue).value + "' to real");
			}
		else if (svalue instanceof IntegerResult)
			return new DoubleResult(((IntegerResult) svalue).value);
		else if (svalue instanceof DoubleResult)
			return (DoubleResult) svalue;
		else
			throw new InterpreterException(" to real coerce cannot be applied to " + printFriendyResultType(svalue));

	}

	public static final StringResult dynamic2String(SingleResult svalue)
			throws InterpreterException {
		if (svalue instanceof StringResult)
			return (StringResult) svalue;
		else if (svalue instanceof IntegerResult)
			return new StringResult(String
					.valueOf(((IntegerResult) svalue).value));
		else if (svalue instanceof DoubleResult)
			return new StringResult(String
					.valueOf(((DoubleResult) svalue).value));
		else if (svalue instanceof BooleanResult)
			return new StringResult(String
					.valueOf(((BooleanResult) svalue).value));
		else
			throw new InterpreterException("to string coerce cannot be applied to " + printFriendyResultType(svalue));
	}

	public static final BooleanResult dynamic2Bool(SingleResult svalue)
			throws InterpreterException {
		if (svalue instanceof BooleanResult)
			return (BooleanResult) svalue;
		else if (svalue instanceof StringResult)
			return new BooleanResult(Boolean
					.valueOf(((StringResult) svalue).value));
		else
			throw new InterpreterException("to boolean coerce cannot be applied to " + printFriendyResultType(svalue));
	}
	
	public static final DateResult dynamic2Date(SingleResult svalue) throws InterpreterException
	{
		if(svalue instanceof DateResult)
			return (DateResult)svalue;
		else if(svalue instanceof StringResult)
		{
			try
			{
				return new DateResult(DateFormat.getDateInstance().parse(((StringResult)svalue).value));
			}
			catch(ParseException exc)
			{
				throw new InterpreterException("to date coerce cannot be applied to " + printFriendyResultType(svalue) + " (unparsable format)");
			}
		}
		else
			throw new InterpreterException("to date coerce cannot be applied to " + printFriendyResultType(svalue));
	}


	public final static BagResult doIntersect(Result res1, Result res2) throws InterpreterException{
		BagResult bagres = new BagResult();
		
		Result derefRes1 = SBQLInterpreterHelper.doDynamicDereference(res1);
		Result derefRes2 = SBQLInterpreterHelper.doDynamicDereference(res2);
		
		HashSet<SingleResult> set = new HashSet<SingleResult>(derefRes2.elementsCount());
		for(SingleResult sinres: derefRes2.elementsToArray())
			set.add(sinres);
		
		for(int i = 0; i < res1.elementsCount(); i++)
			if (set.contains(derefRes1.elementAt(i))) 
				bagres.addElement(res1.elementAt(i));
		
		return bagres;
	}
	
	public final static BagResult doDifference(Result res1, Result res2) throws InterpreterException{
		BagResult bagres = new BagResult();
		
		HashSet<SingleResult> set = new HashSet<SingleResult>(res2.elementsCount());
		for(SingleResult sinres: res2.elementsToArray())
			set.add(sinres);
		
		for(SingleResult sr1: res1.elementsToArray())
			if (!set.contains(sr1))
				bagres.addElement(sr1);
		
		return bagres;
	}
	
	public final static BagResult doUnique(Result res1) throws InterpreterException{
		BagResult bagres = new BagResult();
	
		HashSet<SingleResult> set = new HashSet<SingleResult>(res1.elementsCount());
		for(SingleResult sinres: res1.elementsToArray())
			if (set.add(sinres))
				bagres.addElement(sinres);
		
		return bagres;
	}
	
	public final static BooleanResult doIn(Result res1, Result res2) throws InterpreterException{
		HashSet<SingleResult> set = new HashSet<SingleResult>(res2.elementsCount());
		for(SingleResult sinres: res2.elementsToArray())
			set.add(sinres);
		
		for(SingleResult sinres: res1.elementsToArray())
			if (!set.contains(sinres))
				return new BooleanResult(false);
		
		return new BooleanResult(true);
	}
	
	public final static Result doDynamicDereference( Result val) throws InterpreterException{
		return DereferenceHelper.dereference(val, false);	
	}
	
	public final static Result doDynamicDereferenceMetabase( Result val) throws InterpreterException{
		return DereferenceHelper.dereference(val,true);	
	}
	

	public final static SingleResult[] sort(SingleResult[] sres) throws InterpreterException {
		Arrays.sort(sres, OrderByResultsComparator.comparator);
		return sres;
	}
	
	
	
	public final static BooleanResult doDynamicComparison(SingleResult sr1, SingleResult sr2, OpCodes opcode){
		boolean bres = false;
		if((sr1 instanceof ComparableResult) && (sr2 instanceof ComparableResult)){
			try{
				switch(opcode){
				case dynGr:
					bres = ((ComparableResult)sr1).compareTo((ComparableResult)sr2) > 0;
					break;
				case dynLo:
					bres = ((ComparableResult)sr1).compareTo((ComparableResult)sr2) < 0;
					break;
				case dynGrEq:
					bres = ((ComparableResult)sr1).compareTo((ComparableResult)sr2) >= 0;
					break;
				case dynLoEq:
					bres = ((ComparableResult)sr1).compareTo((ComparableResult)sr2) <= 0;
					break;
				case dynEq:
					bres = ((ComparableResult)sr1).compareTo((ComparableResult)sr2) == 0;
					break;
				case dynNEq:
					bres = ((ComparableResult)sr1).compareTo((ComparableResult)sr2) != 0;
					break;
				default:
					if(ConfigDebug.ASSERTS) assert false: "unknown operaton" + opcode;
				break;
				}
				
			}catch(ClassCastException e){
				throw new InterpreterException("cannot compare " + printFriendyResultType(sr1) + " and " + printFriendyResultType(sr2));
			}
		}else if (!sr1.getClass().equals(sr2.getClass()))
			throw new InterpreterException("cannot compare " + printFriendyResultType(sr1) + " and " + printFriendyResultType(sr2));
		else{
			switch(opcode){
			case dynEq:
				bres = sr1.equals(sr2);
			break;
			case dynNEq:
				bres = !sr1.equals(sr2);
			break;
			}
		}
		return new BooleanResult(bres);
		
	}
	
	public final static BooleanResult doDynamicLogicalOperator(SingleResult sr1, SingleResult sr2, OpCodes opcode){
		boolean bres = false;
	
		if((sr1 instanceof BooleanResult) && (sr2 instanceof BooleanResult)){
			
				switch(opcode){
				case dynOr:
					bres = ((BooleanResult)sr1).value || ((BooleanResult)sr2).value;
					break;
				case dynAnd:
					bres = ((BooleanResult)sr1).value && ((BooleanResult)sr2).value;
					break;
				
				default:
					if(ConfigDebug.ASSERTS) assert false: "unknown runtime operator" + opcode;
				break;
				}
		}else 
			throw new InterpreterException("logical operator requires boolean operands: left is " + printFriendyResultType(sr1) + ", rigth is " + printFriendyResultType(sr2));
			return new BooleanResult(bres);
		
	}
	
	
	public final static SingleResult doDynamicBinaryArithmetic(SingleResult sr1, SingleResult sr2, OpCodes opcode){
		if(!(((sr1 instanceof IntegerResult) || (sr1 instanceof DoubleResult)) && ((sr2 instanceof IntegerResult)  || (sr2 instanceof DoubleResult))))
			throw new InterpreterException("arithmetic operator cannot be applied to " + printFriendyResultType(sr1) + " and " + printFriendyResultType(sr2));
		if(sr1 instanceof IntegerResult && sr2 instanceof IntegerResult)
		{
			int ir1 = ((IntegerResult)sr1).value;
			int ir2 = ((IntegerResult)sr2).value;
			switch(opcode){
			case dynAdd:
				return new IntegerResult(ir1 + ir2); 
			case dynSub:
				return new IntegerResult(ir1 - ir2);
			case dynMul:
				return new IntegerResult(ir1 * ir2);
			case dynDiv:
				if (ir2 == 0)
					throw new InterpreterException("Divide by zero.");
				return new IntegerResult(ir1 / ir2);
			case dynRem:
				return new IntegerResult(ir1 % ir2);
			default:
				if(ConfigDebug.ASSERTS) assert false: "unknown dynamic opcode";
				return null;
			}
		}
		double dr1 = SBQLInterpreterHelper.dynamic2Real(sr1).value;
		double dr2 = SBQLInterpreterHelper.dynamic2Real(sr2).value;
		switch(opcode){
		case dynAdd:
			return new DoubleResult(dr1 + dr2); 
		case dynSub:
			return new DoubleResult(dr1 - dr2);
		case dynMul:
			return new DoubleResult(dr1 * dr2);
		case dynDiv:
			if (dr2 == 0)
				throw new InterpreterException("Divide by zero.");
			return new DoubleResult(dr1 / dr2);
		case dynRem:
			return new DoubleResult(dr1 % dr2);
		default:
			if(ConfigDebug.ASSERTS) assert false: "unknown dynamic opcode";
			return null;	
		}	
	}
	
	public final static ReferenceResult doDynamicUpdate(ReferenceResult rres, SingleResult sres){
		try{
			if(rres.value.isBooleanObject())
				rres.value.updateBooleanObject((SBQLInterpreterHelper.dynamic2Bool(sres)).value);
			else if(rres.value.isIntegerObject())
				rres.value.updateIntegerObject((SBQLInterpreterHelper.dynamic2Int(sres)).value);
			else if(rres.value.isDoubleObject())
				rres.value.updateDoubleObject((SBQLInterpreterHelper.dynamic2Real(sres)).value);
			else if(rres.value.isStringObject())
				rres.value.updateStringObject((SBQLInterpreterHelper.dynamic2String(sres)).value);
			else if(rres.value.isDateObject())
				rres.value.updateDateObject((SBQLInterpreterHelper.dynamic2Date(sres)).value);
			else if(rres.value.isReferenceObject()){
				if(sres instanceof ReferenceResult)
					rres.value.updateReferenceObject(((ReferenceResult)sres).value);
				else throw new InterpreterException("Cannot update reference object with non-reference: " + printFriendyResultType(sres));
					
			}else if(rres.value.isComplexObject()){
			    if(isSpecialObject(rres.value))
				throw new InterpreterException("Unable to update object '" + new DBObject(rres.value).getObjectKind().getKindAsString() + "'");
				SBQLInterpreterHelper.doComplexUpdate( rres, (StructResult)sres, true);
			}else throw new InterpreterException("Improper object type. Object name:" + rres.value.getObjectName());
		}catch (DatabaseException e){
			throw new InterpreterException("Database error during runtime dynamic update operation: " + e.getMessage(), e);
		}
		return rres;
	}
	
	public static final Result doMax(Result res) {

		SingleResult[] sres = res.elementsToArray();
		ComparableResult max = (ComparableResult)sres[0];
		for(SingleResult s : sres){
			ComparableResult cs = (ComparableResult)s;
			if( cs.compareTo(max) > 0)
				max = cs;
		}
		return max;
	}
	
	public static final Result doMin(Result res) {
		
		SingleResult[] sres = res.elementsToArray();
		ComparableResult min = (ComparableResult)sres[0];
		for(SingleResult s : sres){
			ComparableResult cs = (ComparableResult)s;
			if( cs.compareTo(min) < 0)
				min = cs;
		}
		return min;
	}
	public static final Result doAvg(Result res) {
		SingleResult[] sres = res.elementsToArray();
		double result = 0;
		for(SingleResult s : sres){
			if(s instanceof IntegerResult){
				result += ((IntegerResult)s).value;
			}else if(s instanceof DoubleResult){
				result += ((DoubleResult)s).value;
			}else throw new InterpreterException("Avg operator wrong type" );
		}
		result /= sres.length;
		return new DoubleResult(result);
	}
	public static final void doComplexUpdate(ReferenceResult refres, StructResult strres, boolean withRuntimeCheck){
		try{
			
			if(ConfigDebug.ASSERTS) assert refres.value.isComplexObject() : "update complex object requires complex object";
			if(withRuntimeCheck)
			    strres = checkStructUpdateCompatibility(strres);
			
			Vector<Integer> aggnameids = new Vector<Integer>();
			//first we delete old sub-objects
			for(OID subid : refres.value.derefComplex()){
				if(subid.isAggregateObject()){
				    aggnameids.add(subid.getObjectNameId());
					for(OID asuid : subid.derefComplex()){
						asuid.delete();
					}
				}else 
					subid.delete();
			}
			//then we create new
			
			for(SingleResult sres: strres.fieldsToArray()){
				BinderResult bres = (BinderResult)sres;
				int nameid = Database.getStore().addName(bres.getName());
				OID agg = refres.value.findFirstChildByNameId(nameid);
				OID parent = agg != null ? agg : refres.value;
				if(withRuntimeCheck)
					dynamicCreate(parent, nameid, bres.value);
				else {
				    if(bres.value instanceof CollectionResult){				    
//					OID parent = store.createAggregateObject(nameid, refres.value, ((CollectionResult)bres.value).elementsCount());			
					for(SingleResult sr: ((CollectionResult)bres.value).elementsToArray()){
						safeCreate(parent, nameid, sr);
					}
				    }else
					safeCreate(parent, nameid, (SingleResult)bres.value);
				}
			}
		}catch (DatabaseException e){
			throw new InterpreterException("Database error during update operation: " + e.getMessage(), e);
		}
		
	}
	
	
	public static final BagResult callIndex(OID idxoid, SBQLStack stack) throws DatabaseException {
		 
		DBIndex idxdb = new DBIndex(idxoid);
		Index idx = idxdb.getIndex();
		
		AbstractQueryResult res;

		int params = ((IntegerResult) (stack.pop())).value;
	
		if (idx.recordType.keyCount() != params)
			throw new InterpreterException("Wrong number of parametrs in "+idxdb.getName()+" index call");
		
		if (idx.recordType.keyCount() == 1) { 	
			res = popIndexParam(stack);
		} else {
			res = new BagResult();
			for (int i = 0; i < idx.recordType.keyCount(); i++)
				((BagResult) res).addElement(popIndexParam(stack));
		}
		
		Object keyValue;
		try {
			keyValue = idx.dataAccess.key2keyValue(res);
			if (!idx.recordType.isProperQuery(keyValue))
				throw new InterpreterException("Wrong parameters in "+idxdb.getName()+" index call");
		} catch (DatabaseException E) {
			throw new InterpreterException("Wrong parameters type in "+idxdb.getName()+" index call");
		}
		
		BagResult bagres = new BagResult();
		
		if (idx.recordType.supportRangeQueries() && idx.recordType.isRangeQuery(keyValue)) {
			for (Object refres : idx.lookupItemsInRange(keyValue))
				bagres.addElement((ReferenceResult) refres);
		} else if (idx.recordType.isInQuery(keyValue)) {
			for (Object refres : idx.lookupItemsInRange(keyValue))
				bagres.addElement((ReferenceResult) refres);
		} else {
			for (Object refres : idx.lookupItemsEqualTo(keyValue))
				bagres.addElement((ReferenceResult) refres);
		}
		return bagres;
	}
	
	private static SingleResult popIndexParam(SBQLStack stack) {
		Result res = (Result) stack.pop();

		if (res instanceof BagResult)
			return new BinderResult(KeyType.IN_KEY_LABEL , res);
		
		if (res instanceof StructResult)
			return new BinderResult(KeyType.RANGE_KEY_LABEL , res);
		
		if (res instanceof BinderResult)
			return (BinderResult) res;
		
		return new BinderResult(KeyType.EQUAL_KEY_LABEL , res);
	}

	public static final String printFriendyResultType(Result res){
		if(res.elementsCount() == 0) return "empty result";
		return res.getClass().getSimpleName().toLowerCase().replaceAll("result", "");
	}
	
	private static final void safeCreate( OID parent, int nameid, SingleResult value) throws DatabaseException{
	 	    
		if(!parent.isComplexObject() && !parent.isAggregateObject())
			throw new InterpreterException("Cannot create sub-object in: '" + parent.getObjectName() + "' " + parent.getObjectKind().toString());
		if(value instanceof StringResult){
		    parent.createStringChild(nameid, ((StringResult)value).value, 0);
		}else if(value instanceof IntegerResult){
		    parent.createIntegerChild(nameid, ((IntegerResult)value).value);
		}else if(value instanceof BooleanResult){
		    parent.createBooleanChild(nameid, ((BooleanResult)value).value);
		}else if(value instanceof DoubleResult){
			parent.createDoubleChild(nameid, ((DoubleResult)value).value);
		}else if(value instanceof ReferenceResult){
			parent.createReferenceChild(nameid, ((ReferenceResult)value).value);
		}else if(value instanceof StructResult){
			OID strparent = parent.createComplexChild(nameid, ((StructResult)value).fieldsCount());
			for(SingleResult sr : ((StructResult)value).fieldsToArray()){
				if(!(sr instanceof BinderResult))
					throw new InterpreterException("Cannot create object with use of non-named structure element");
				BinderResult bres = ((BinderResult)sr);
				int snameid = Database.getStore().addName(bres.getName());
				if(bres.value instanceof CollectionResult){
					OID aggparent = strparent.createAggregateChild(Database.getStore().addName(((BinderResult)sr).getName()), ((CollectionResult)bres.value).elementsCount());
					for(SingleResult sres: ((CollectionResult)((BinderResult)sr).value).elementsToArray()){

						safeCreate( aggparent, snameid, sres);
					}
					
				}else
				safeCreate(strparent, snameid, (SingleResult)bres.value); // FIXME: res/sampledata/batch/reverseReferencesAndAggregatesTest.cli runs incorrectly
				}
			}else if(value instanceof BinderResult){ 
				throw new InterpreterException("Object creation error: required name: '" + Database.getNameIndex().id2name(nameid) + "', found binder with name '" + ((BinderResult)value).getName() + "'");
				
			}
			
			
		}
	
	private static final StructResult checkStructUpdateCompatibility(StructResult strres){
		SingleResult[] sresa = strres.fieldsToArray();
		StructResult bresa = new StructResult();
		Hashtable<String, Integer> results = new Hashtable<String, Integer>();
		Integer j;
		int currPos = 0;
		//check if all struct fields are binders and join binders with the same name
		for(int i = 0; i < sresa.length; i++){
			BinderResult field = null;
			if(!(sresa[i] instanceof BinderResult)) {
				throw new InterpreterException("Cannot update complex object with non-named structure element"); }
			if(((BinderResult)sresa[i]).value instanceof CollectionResult){
				CollectionResult cres = createProperCollection(((BinderResult)sresa[i]).value);
				for(SingleResult bsr : ((BinderResult)sresa[i]).value.elementsToArray()){
					if(bsr instanceof BinderResult){
						throw new InterpreterException("Grouped elements cannot be named for update operation: '" + ((BinderResult)bsr).getName() +"' inside '" + ((BinderResult)sresa[i]).getName()); 
					}if(bsr instanceof StructResult){
						cres.addElement(checkStructUpdateCompatibility((StructResult)bsr));
					}else
						cres.addElement(bsr);
				}
				((BinderResult)sresa[i]).value = cres;
			}else if(((BinderResult)sresa[i]).value instanceof StructResult){
				((BinderResult)sresa[i]).value = checkStructUpdateCompatibility((StructResult)((BinderResult)sresa[i]).value);
			}
			if(((j = results.get(((BinderResult)sresa[i]).getName())) != null)) {
				if(j != (i - 1)){ 
					throw new InterpreterException("Improper order of fields in the structure: '" + ((BinderResult)sresa[i]).getName() + "'"); 
					}
					
					currPos ++;
					BinderResult br = (BinderResult)bresa.fieldAt(i - currPos);//get(i - currPos);
					if(br.value instanceof CollectionResult)
					{
						((CollectionResult)br.value).addElement((SingleResult)((BinderResult)sresa[i]).value);
						continue;
					}
					
					bresa.removeField(br);
					CollectionResult cr = new BagResult();
					cr.addElement((SingleResult)br.value);
					cr.addElement((SingleResult)((BinderResult)sresa[i]).value);
					field = new BinderResult(br.getName(),cr);
					
			}else {
				field = (BinderResult)sresa[i];
				
			}
			bresa.addFieldAt(i - currPos, field);
			results.put(((BinderResult)sresa[i]).getName(), i);
		}
		
		return bresa;
		
	}
	
	public static final OID findAggObject(IDataStore store, int name, OID parent, int childnum) throws DatabaseException {
	    assert parent.isComplexObject() : "complex object required " + parent.getObjectName() + " : "+parent.getObjectKind().toString();
	    OID agg = store.findFirstByNameId(name, parent);

		if (agg == null)
			agg = store.createAggregateObject(name, parent, childnum);
		else
		    assert agg.isAggregateObject(): agg.getObjectName() + "is not an aggregate object";
		return agg;
	}
	public static final OID findVirtualObject(IDataStore store, int name, OID parent) throws DatabaseException {
		DBView view = new DBView(parent);
		OID vop;
		if(view.isValid()){
			vop = store.findFirstByNameId(name, view.getVirtualFieldsEntry());
			
		}else{
			vop = store.findFirstByNameId(name, parent);
		}
		if (vop != null){
			if(vop.isComplexObject())	
			    if(new DBVirtualObjectsProcedure(vop).isValid())
				return vop;
		}
		
		return null;
	}
	static final boolean isSpecialObject(OID oid) throws DatabaseException {
	    DataObjectKind kind = new DBObject(oid).getObjectKind();
	   switch(kind.getKindAsInt()){
	   case DataObjectKind.CLASS_OBJECT:
	   case DataObjectKind.ENDPOINT_OBJECT:
	   case DataObjectKind.INDEX_OBJECT:
	   case DataObjectKind.LINK_OBJECT:
	   case DataObjectKind.META_BASE_OBJECT:
	   case DataObjectKind.MODULE_OBJECT:
	   case DataObjectKind.PROCEDURE_OBJECT:
	   case DataObjectKind.PROXY_OBJECT:
	   case DataObjectKind.VIEW_OBJECT:
	   case DataObjectKind.VIRTUAL_OBJECTS_PROCEDURE_OBJECT:
	       return true;
	   default:
	      return false;
	   }
	       
	    
	}
	static final boolean isMetabaseObject(OID oid) throws DatabaseException {
	    return new MBObject(oid).getObjectKind() != MetaObjectKind.UNKNOWN_OBJECT;
	}
	public static final Result doDynamicCast(Result castTo, Result value)throws DatabaseException {
	    if(!(castTo instanceof ReferenceResult)){
		throw new InterpreterException("Unable to cast to :" + SBQLInterpreterHelper.printFriendyResultType(castTo) );
	    }
	    
	    if(!((ReferenceResult)castTo).value.isComplexObject())
		    return doDynamicCoerce((ReferenceResult)castTo, value);
	    
	    DBClass castToClass = new DBClass(((ReferenceResult)castTo).value);
	    if(!castToClass.isValid())
		    throw new InterpreterException("Unable to cast to not a class: " + ((ReferenceResult)castTo).value.getObjectName());
		
	    BagResult bagres = new BagResult();
	    for(SingleResult sinres: value.elementsToArray()){
		if(sinres instanceof ReferenceResult){
		    ReferenceResult refres = (ReferenceResult)sinres;
		    OID directClassid = refres.value.derefInstanceOfReference();
		    if(directClassid != null){
			DBClass directClass = new DBClass(directClassid);
			if(directClassid.equals(castToClass.getOID()) || directClass.isSubClassOf(castToClass.getOID())){
			    bagres.addElement(refres);
			}
		    }
		}
	    }
	    return bagres;
	}
	
	private static final Result doDynamicCoerce(ReferenceResult castTo, Result value)throws DatabaseException {
	    BagResult bagres = new BagResult();
	    OID casttoid = castTo.value;
	    for(SingleResult sinres:value.elementsToArray()){
		if(casttoid.isIntegerObject()){
		    bagres.addElement(SBQLInterpreterHelper.dynamic2Int(sinres));
		}else if(casttoid.isDoubleObject()){
		    bagres.addElement(SBQLInterpreterHelper.dynamic2Real(sinres));
		}else if(casttoid.isBooleanObject()){
		    bagres.addElement(SBQLInterpreterHelper.dynamic2Bool(sinres));
		}else if(casttoid.isStringObject()){
		    bagres.addElement(SBQLInterpreterHelper.dynamic2String(sinres));
		}else if(casttoid.isDateObject()){
		    bagres.addElement(SBQLInterpreterHelper.dynamic2Date(sinres));
		}else
		    assert false : "unimplemented simple type in dynamic coerce";
	    }
	    return bagres;
	}
	
	public static final String printFriendyResult(Result res) {
	    StringBuffer buffer = new StringBuffer(); 
		if(res.elementsCount() == 0) return "empty bag";
		for(SingleResult sinres: res.elementsToArray()){
		    buffer.append("\n type: " + printFriendyResultType(sinres));
		    buffer.append("\nvalue: ");
			if(sinres instanceof IntegerResult){
				buffer.append(((IntegerResult)sinres).value);
			}else if(sinres instanceof DoubleResult){
			    buffer.append(((DoubleResult)sinres).value);
			}else if(sinres instanceof StringResult){
			    buffer.append(((StringResult)sinres).value);
			}else if(sinres instanceof BooleanResult){
			    buffer.append(((BooleanResult)sinres).value);
			}else if(sinres instanceof ReferenceResult){
			    try {
				buffer.append(((ReferenceResult)sinres).value.getObjectName() + " : ");
				buffer.append(((ReferenceResult)sinres).value.getObjectKind().toString());
			    } catch (DatabaseException e) {
				buffer.append("error getting reference info");
			    }
			}else if(sinres instanceof DateResult){
			    buffer.append(((DateResult)sinres).value);
			}else if(sinres instanceof StructResult){
			    buffer.append("{");
				StructResult strres = (StructResult)sinres;
				for(SingleResult fres: strres.fieldsToArray()){
				    buffer.append(printFriendyResultType(fres));
				}
				buffer.append("} ");
			}else 
				assert false : "unimplemented result type";
		}
		return buffer.toString();
	}

	/**
	 * Experimental oid serialization
	 * TODO re-think and re-factor
	 * @return
	 */
	public static SingleResult serializeOID(OID value) {
		StringBuffer result = new StringBuffer();
		int oid = value.internalOID();
		IDataStore store = value.getStore();
		if(store instanceof RemoteDefaultStore){
			RemoteDefaultStore rstore = (RemoteDefaultStore)value.getStore();
			result.append(rstore.host);
			result.append("@");
			result.append(Integer.toString(rstore.port));
			result.append("@");
			
		}else if(store instanceof TransientStore)
		{
			throw new InterpreterException("unable to serialize transient object identifier");
		}else if(store instanceof DefaultStore)
		{
			
		} else {
			assert false : "unknown store type";
		}
		result.append(Integer.toString(oid));
		return new StringResult(result.toString());
	}

	/**
	 * @param sres
	 * @return
	 */
	public static ReferenceResult deserializeOID(SingleResult sres) {
		if(sres instanceof StringResult){
			String[] serOidParts = ((StringResult)sres).value.split("\\@");
			if(serOidParts.length == 1){
				return new ReferenceResult(Database.getStore().offset2OID(Integer.parseInt(serOidParts[0])));
			}else assert false :"unimplemented remote store OID deserialization";
		}else throw new InterpreterException("unable to deserialize object identifier - wrong result type : '" + sres.getClass().getSimpleName() +"'");
		return null;
	}

	/**
	 * @param value - identifier of an object to move
	 * @param where - new parent 
	 * @throws DatabaseException 
	 */
	public static void move(OID value, OID where) throws DatabaseException {
		assert where.isComplexObject() || where.isAggregateObject(): "complex object required";
		if(where.getStore() == value.getStore())
		{	if(!where.equals(value.getParent()))
				value.move(where);
		}else{
			OID copy = SBQLInterpreterHelper.createCopy(value, where);
			value.deleteSafe();
		}		
	}
}



class OrderByResultsComparator implements Comparator<SingleResult> {

	static OrderByResultsComparator comparator = new OrderByResultsComparator();
	
	public int compare(SingleResult sinres1, SingleResult sinres2) {
		
		SingleResult[] fres1 = ((StructResult) sinres1).fieldsToArray();
		SingleResult[] fres2 = ((StructResult) sinres2).fieldsToArray();
		
		for(int field = 1; field < fres1.length; field++) {
			
			if(!(fres1[field] instanceof ComparableResult)) 
				throw new InterpreterException("unable to sort elements with type " + SBQLInterpreterHelper.printFriendyResultType(fres1[field]));
			if(!(fres2[field] instanceof ComparableResult))
				throw new InterpreterException("unable to sort elements with type " + SBQLInterpreterHelper.printFriendyResultType(fres2[field]));

			int comparision = ((ComparableResult)fres1[field]).compareTo((ComparableResult)fres2[field]);
			if (comparision != 0)
				return comparision;
				
		}
		
		return 0;
	}
	
	
}
