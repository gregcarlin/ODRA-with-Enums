/**
 * 
 */
package odra.sbql.interpreter;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBView;
import odra.db.schema.OdraViewSchema;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.sbql.results.runtime.VirtualReferenceResult;
import odra.sbql.stack.AggregateBinder;
import odra.sbql.stack.Binder;
import odra.sbql.stack.Nester;
import odra.sbql.stack.RemoteNester;
import odra.sbql.stack.StackFrame;

/**
 * RuntimeNestingManager
 * 
 * @author Radek Adamus
 * @since 2007-10-01 last modified: 2007-10-01
 * @version 1.0
 */
 class RuntimeNestingManager {
    private ReferenceResult on_navigate = null;
    private Hashtable<OID, Vector<Integer>> inheritedInstanceNames = new Hashtable<OID, Vector<Integer>>();

    /** Performe nested function - the result is a stack of stack frames
     * @param res - parameter for nested
     * @return the result of nesting
     * @throws DatabaseException
     */
    public Stack<StackFrame> nested(SingleResult res) throws DatabaseException{
	reset();
	Stack<StackFrame> environment = new Stack<StackFrame>();
	StackFrame frame = new StackFrame();
	//topmost section at the bottom !!
	environment.push(frame);
	nested(res, frame, environment);
	return environment;
    }
    
    /** Perform nested on virtual reference seed
     * @param vidr - the virtual reference
     * @return - the result of nested
     * @throws DatabaseException
     */
    public Stack<StackFrame> nestedSeed(VirtualReferenceResult vidr) throws DatabaseException{
	Stack<StackFrame> environment = new Stack<StackFrame>();
	this.nestedSeed(vidr, environment);
	return environment;
    }
    /**
     * @param env - the oid of the root (e.g. module entry) environment to be perform nested on
     * @return the stack frame with the result of nested
     * @throws DatabaseException
     */
    public StackFrame nestedRootEnvironment(OID env) throws DatabaseException
    {	
	return this.initializeNestedEnvironment(new StackFrame(), env, null);
    }
    private void reset() { 
	    this.setOn_navigate(null);
	}
    /** 
     * @param res - query result - the nested parameter
     * @param stackframe - main stack frame for nested result
     * @param buildedEnvironment - the whole environment for nesting (for models > M0 there can be more than one frame o)
     * @throws DatabaseException
     */
    protected void nested(SingleResult res, StackFrame stackframe,
	    Stack<StackFrame> buildedEnvironment) throws DatabaseException
    {
	if (res instanceof StructResult)
	{
	    StructResult str = (StructResult) res;
	    for (SingleResult sinres : str.fieldsToArray())
		nested(sinres, stackframe, buildedEnvironment);
	} else if (res instanceof BinderResult)
	{
	    BinderResult bin = (BinderResult) res;
	    stackframe.enter(new Binder(Database.getNameIndex().name2id(
		    bin.getName()), bin.value)); // replace to call Database's nidx
	} else if (res instanceof VirtualReferenceResult)
	{
	    VirtualReferenceResult vidr = (VirtualReferenceResult) res;
	    // we need to smuggle the whole seed for subviews virtual
	    // identifiers
	    DBView viewDef = new DBView(vidr.value);
	    nestedSeed(vidr, buildedEnvironment);
	    this.initializeNestedEnvironment(stackframe, viewDef
		    .getVirtualFieldsEntry(), vidr);
	    // experiment 'self' for virtual object
	    stackframe.enter(new Binder(Database.getStore().addName("self"),
		    vidr));
	    OID navig = viewDef
		    .getGenericProcByName(OdraViewSchema.GenericNames.ON_NAVIGATE_NAME.toString());
	    if (navig != null)
	    {
		if (this.getOn_navigate() != null)
		    throw new InterpreterException(
			    "unsupported nested on struct with more than one virtual pointer ");
		this.setOn_navigate(new ReferenceResult(navig, vidr));
	    }

	} else if (res instanceof ReferenceResult)
	{
		ReferenceResult ref = (ReferenceResult) res;

		if (ref.value.isComplexObject())
		{
			if (new DBLink(ref.value).isValid())
			{
				stackframe.enter(new RemoteNester(ref.value));
			} else if (new DBView(ref.value).isValid())
			{
				stackframe.enter(new Nester(ref.value));
				this.initializeNestedEnvironment(stackframe, new DBView(
						ref.value).getViewFieldsEntry(), ref);
			} else if (new DBModule(ref.value).isValid())
			{
				this.initializeNestedEnvironment(stackframe, ref.value, ref);
				stackframe.enter(new Binder(Database.getStore().addName("self"), ref));
				stackframe.enter(new Binder(ref.value.getObjectNameId(), ref));
			} else
			{
				OID clsid = ref.value.derefInstanceOfReference();
				if (clsid != null)
				{
					stackframe.enter(new Binder(Database.getStore()
							.addName("self"), ref));
					DBClass cls = new DBClass(clsid);
					this.nestedM1Class(ref, cls, buildedEnvironment);
				}
				this.initializeNestedEnvironment(stackframe, ref.value, ref);
			}
		} else if (ref.value.isReferenceObject())
		{
			OID target = ref.value.derefReference();
			ReferenceResult val = new ReferenceResult(target, ref);
			int name_id = target.getObjectNameId();
			OID clsid = target.derefInstanceOfReference();
		if (clsid != null)
		{
		    DBClass cls = new DBClass(clsid);
		    for (OID sclsref : cls.getSuperClassesRefs())
		    {
			DBClass scls = new DBClass(sclsref.derefReference());
			if (scls.hasInstanceName())
			{
			    stackframe.enter(new Binder(scls
				    .getInstanceNameId(), val));
			}
		    }
		    // UML compatibility (we enter the binder with class name)
		    stackframe.enter(new Binder(cls.getOID().getObjectNameId(),
			    val));
		    
		    if(cls.hasInstanceName()){
		    	int instancenameid = cls.getInstanceNameId();
		    	if(instancenameid != name_id)
		    		stackframe.enter(new Binder(instancenameid,val));
		    }
		}
		

		stackframe.enter(new Binder(name_id, val));
	    }
	}

    }

    private final StackFrame initializeNestedEnvironment(StackFrame frame,
	    OID env, ReferenceResult source) throws DatabaseException
    {
	OID clsid;

	assert env.isComplexObject() : "complex object required";
	for (OID id : env.derefComplex())
	{
		if (id.isAggregateObject())
		{
			frame.enter(new AggregateBinder(id.getObjectNameId(),
					new ReferenceResult(id, source)));
			clsid = id.derefInstanceOfReference();
			if (clsid != null)
			{
				DBClass cls = new DBClass(clsid);
				assert cls.isValid() : "instanceof reference leads to not a class object"
					+ clsid.getObjectName();
				if (cls.hasInstanceName())
				{
//					if(cls.getInstanceNameId() != id.getObjectNameId())
//						frame.enter(new AggregateBinder(cls.getInstanceNameId(),
//								new ReferenceResult(id, source)));
					Vector<Integer> names = this.inheritedInstanceNames
					.get(clsid);
					if (names == null)
					{
						names = this.getInheritedInstanceNames(cls);
						this.inheritedInstanceNames.put(clsid, names);
					}
					for (int nameid : names)
					{
						frame.enter(new AggregateBinder(nameid,
								new ReferenceResult(id, source)));
					}
				}
			}
		} 
		else if(id.getObjectName().equals("$enums")){
			for(OID enuid : id.derefComplex()){
				initializeEnum(enuid,frame,source);
			}
		}
		else
		{
			frame.enter(new Binder(id.getObjectNameId(),
					new ReferenceResult(id, source)));
			clsid = id.derefInstanceOfReference();
			if (clsid != null)
			{
				DBClass cls = new DBClass(clsid);
				if (cls.hasInstanceName())
				{
					Vector<Integer> names = this.inheritedInstanceNames
					.get(clsid);
					if (names == null)
					{
						names = this.getInheritedInstanceNames(cls);
						this.inheritedInstanceNames.put(clsid, names);
					}
					for (int nameid : names)
					{
						frame.enter(new Binder(nameid, new ReferenceResult(
								id, source)));
					}
				}
			}
		}
	}
	return frame;
    }

    private void initializeEnum(OID enuid,StackFrame frame,ReferenceResult source) throws DatabaseException{
    	
    	if(enuid.derefComplex().length==0)
    		frame.enter(new Binder(enuid.getObjectNameId(),
					new ReferenceResult(enuid, source)));
    	else{
    	  	//StructResult srFields = new StructResult();
    	  	/*for(OID field : enuid.derefComplex()){
    		switch (field.getObjectKind()){
    			case STRING_OBJECT:
    				srFields.addField(new BinderResult(field.getObjectName(),new StringResult(field.derefString())));
    			case INTEGER_OBJECT:
    				srFields.addField(new BinderResult(field.getObjectName(),new IntegerResult(field.derefInt())));
    			case COMPLEX_OBJECT:
    				StructResult srFieldsStruct = new StructResult();
    				for(OID strField : field.derefComplex()){
    					srFieldsStruct.addField(new BinderResult(strField.getObjectName(),null));
    				}
    				srFields.addField(new BinderResult(field.getObjectName(),srFieldsStruct));
    		}
    	  }
    	  	frame.enter(new Binder(enuid.getObjectNameId(),srFields));*/
    	
    	StructResult srFields = new StructResult(); //for enum values
    	BagResult br = new BagResult();
    	for(OID field : enuid.derefComplex()){
    		SingleResult res = (SingleResult)assignEnumBinder(field);
    		srFields.addField((new BinderResult(field.getObjectName(),res)));
    		br.addElement(res);
    	}
    	
    	frame.enter(new Binder(enuid.getObjectNameId(),srFields));
    	frame.enter(new Binder(Database.getNameIndex().addName("$enum_"+enuid.getObjectName()+"_values"),br));
      }	
    	
    }

	private Result assignEnumBinder(OID field) throws DatabaseException{
		switch (field.getObjectKind()){
		case STRING_OBJECT:
			return (new StringResult(field.derefString()));
		case INTEGER_OBJECT:
			return (new IntegerResult(field.derefInt()));
		case COMPLEX_OBJECT:
			StructResult srFieldsStruct = new StructResult(); //field is complex C_kob
			for(OID strField : field.derefComplex()){
				if(strField.isAggregateObject()){
					for(OID aggField : strField.derefComplex()){
						srFieldsStruct.addField(new BinderResult(aggField.getObjectName(),assignEnumBinder(aggField)));
						
					}
				}
				else				
				  srFieldsStruct.addField(new BinderResult(strField.getObjectName(),assignEnumBinder(strField)));
			}
			return srFieldsStruct;
		case AGGREGATE_OBJECT:
			//StructResult srFieldsAgg = new StructResult(); //dla podpl aggregata
			//for(OID aggField : field.derefComplex()){
				//srFieldsAgg.addField(new BinderResult(aggField.getObjectName(),assignEnumBinder(aggField)));
			//}
			//return srFieldsAgg;
			
	}
		return null;
	}

	private void nestedSeed(VirtualReferenceResult vidr,
	    Stack<StackFrame> buildedEnvironment) throws DatabaseException
    {
	for (SingleResult sr : vidr.getSeed().elementsToArray())
	{
	    StackFrame seedframe = new StackFrame();
	    buildedEnvironment.push(seedframe);
	    nested(sr, seedframe, buildedEnvironment);

	}
    }

    private void nestedM1Class(ReferenceResult source, DBClass cls,
	    Stack<StackFrame> buildedEnvironment) throws DatabaseException
    {
	// add methods to a new section
	StackFrame clsframe = new StackFrame();
	for (OID methid : cls.getMethodsEntry().derefComplex())
	{
	    clsframe.enter(new Binder(methid.getObjectNameId(),
		    new ReferenceResult(methid, source)));
	}
	buildedEnvironment.push(clsframe);
	// superclasses
	for (OID superclsref : cls.getSuperClassesRefs())
	{
	    OID superclsid = superclsref.derefReference();
	    nestedM1Class(source, new DBClass(superclsid), buildedEnvironment);
	}
    }

    private Vector<Integer> getInheritedInstanceNames(DBClass cls)
	    throws DatabaseException
    {
	Vector<Integer> names = new Vector<Integer>();
	Hashtable<OID, OID> fetched = new Hashtable<OID, OID>();
	for (OID sclsid : cls.getSuperClassesRefs())
	{
	    DBClass scls = new DBClass(sclsid.derefReference());
	    this.getInstanceNames(scls, names, fetched);
	}
	return names;
    }

    private void getInstanceNames(DBClass cls, Vector<Integer> names,
	    Hashtable<OID, OID> fetched) throws DatabaseException
    {
	if (cls.hasInstanceName())
	{
	    names.add(cls.getInstanceNameId());
	    for (OID sclsidref : cls.getSuperClassesRefs())
	    {
		OID sclsid = sclsidref.derefReference();
		if (fetched.get(sclsid) != null)
		    continue;
		DBClass scls = new DBClass(sclsid);
		this.getInstanceNames(scls, names, fetched);
		fetched.put(sclsid, sclsid);
	    }
	}

    }

    /**
     * @param on_navigate the on_navigate to set
     */
    void setOn_navigate(ReferenceResult on_navigate)
    {
	this.on_navigate = on_navigate;
    }

    /**
     * @return the on_navigate
     */
    ReferenceResult getOn_navigate()
    {
	return on_navigate;
    }
}
