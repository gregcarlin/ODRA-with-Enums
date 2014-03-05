/**
 * 
 */
package odra.sbql.interpreter;

import java.util.Stack;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
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
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTNode;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.interpreter.metabase.MetaNames;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.sbql.results.runtime.VirtualReferenceResult;
import odra.sbql.stack.Binder;
import odra.sbql.stack.StackFrame;

/**
 * RuntimeMetabaseNestingManager
 * @author Radek Adamus
 *@since 2007-10-01
 *last modified: 2007-10-01
 *@version 1.0
 */
public class RuntimeMetabaseNestingManager extends RuntimeNestingManager {

    
    public final StackFrame nestedRootEnvironment(OID env) throws DatabaseException {
	StackFrame frame = new StackFrame();
	this.nestedMetabaseRoot(frame, env);
	return frame;
    }
    
    /* (non-Javadoc)
     * @see odra.sbql.interpreter.RuntimeNestingManager#nested(odra.sbql.results.runtime.SingleResult, odra.sbql.stack.StackFrame, java.util.Stack)
     */
    @Override
    protected void nested(SingleResult res, StackFrame stackframe,
	    Stack<StackFrame> buildedEnvironment) throws InterpreterException
    {
	try
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
	    }
	    else if (res instanceof ReferenceResult)
	    {
	        ReferenceResult ref = (ReferenceResult) res;
	        assert ref.value.isComplexObject(): "metadata are complex objects";
	        if(new DBModule(ref.value).isValid()){
	            this.nestedMetabaseRoot(stackframe, new DBModule(ref.value).getMetabaseEntry());
	            return;
	        }
	        MBObject mbo = MBObjectFactory.getTypedMBObject(ref.value);
	        MetaObjectKind kind = mbo.getObjectKind();
	        pushName(stackframe, mbo.getName());
	        pushKind(stackframe,kind);
	        switch(kind){
	        case VARIABLE_OBJECT: 
        	    	MBVariable mbvar = (MBVariable)mbo;
        	    	pushCardinality(stackframe, mbvar.getMinCard(), mbvar.getMaxCard());
        	    	pushType(stackframe, mbvar.getType());
        	    	pushReferenceLevel(stackframe, mbvar.getRefIndicator());
        	    	if(mbvar.hasReverseReference()){
        	    	    pushReversePointer(stackframe, mbvar.getReversePointer());
        	    	}
	        break;
	        case PRIMITIVE_TYPE_OBJECT:
	            
	            break;
	        case TYPEDEF_OBJECT:
	            MBTypeDef mbtd = (MBTypeDef)mbo;
	            pushType(stackframe, mbtd.getType());
	            break;
	        case STRUCT_OBJECT:
	            MBStruct mbstr = (MBStruct)mbo;
	            pushFields(stackframe, mbstr.getFields());	            
	            break;
	        case PROCEDURE_OBJECT:
        	    	MBProcedure mbproc = (MBProcedure)mbo;
        	    	pushCardinality(stackframe, mbproc.getMinCard(), mbproc.getMaxCard());
        	    	pushType(stackframe, mbproc.getType());
        	    	pushReferenceLevel(stackframe, mbproc.getRefIndicator());
        	    	pushAST(stackframe, mbproc.getAST());
        	    	pushArguments(stackframe, mbproc.getArguments());	    	
	    	break;
	        case VIRTUAL_VARIABLE_OBJECT:
	            MBVirtualVariable mbvproc = (MBVirtualVariable)mbo;
	            pushCardinality(stackframe, mbvproc.getMinCard(), mbvproc.getMaxCard());
	            pushType(stackframe, mbvproc.getType());
	            pushReferenceLevel(stackframe, mbvproc.getRefIndicator());
	            pushView(stackframe, mbvproc.getView());
	            pushVirtualFields(stackframe, new MBView(mbvproc.getView()).getVirtualFieldsEntry().derefComplex());
	            break;
	        case CLASS_OBJECT:
	            MBClass cls = (MBClass)mbo;
	            if(cls.hasInstanceName())
	        	pushInstanceName(stackframe, cls.getInstanceName());
	            pushFields(stackframe, new MBStruct(cls.getType()).getFields());
	            pushMethods(stackframe, cls.getMethods());	            
	            pushSuperClasses(stackframe, cls.getDirectSuperClasses());	            	            	            
	    	break;
	        case VIEW_OBJECT:
	            MBView mbview = (MBView)mbo;
	            pushGenericViewProcedures(stackframe, mbview.getGenProcs());
	            pushSubViews(stackframe, mbview.getSubViewsEntry().derefComplex());
	            pushVirtualObject(stackframe, mbview.getVirtualObject());
	            pushViewFields(stackframe, mbview.getViewFieldsEntry().derefComplex());
	            break;
	        case LINK_OBJECT:	     
	            MBLink mblink = (MBLink)mbo;
	            pushLinkInfo(stackframe, mblink);
	            this.nestedMetabaseRoot(stackframe, mblink.getMetaBase().getMetabaseEntry());
	            break;
	        
	    	default:
	    	    assert false: "unimplemented";
	        }
	    }
	} catch (DatabaseException e)
	{
	    throw new InterpreterException(e);
	}
    }

    /* (non-Javadoc)
     * @see odra.sbql.interpreter.RuntimeNestingManager#nestedSeed(odra.sbql.results.runtime.VirtualReferenceResult)
     */
    @Override
    public Stack<StackFrame> nestedSeed(VirtualReferenceResult vidr)
	    throws DatabaseException
    {
	assert false: "operation unimplemented for nesting metabase";
    	return null;
    }
    
    private void pushCardinality(StackFrame frame, int min, int max) throws DatabaseException{
	frame.enter(binder( MetaNames.MIN_CARD.toString(), new IntegerResult(min))); 
	frame.enter(binder(MetaNames.MAX_CARD.toString(), new IntegerResult(max)));
    }
    
    private void pushKind(StackFrame frame, MetaObjectKind kind) throws DatabaseException{
	frame.enter(binder(MetaNames.KIND.toString(), new StringResult(kind.toString())));
    }
    
    private void pushName(StackFrame frame, String name) throws DatabaseException{
	frame.enter(binder(MetaNames.NAME.toString(), new StringResult(name)));
    }
    
    private void pushType(StackFrame frame, OID type) throws DatabaseException{
	if(new MBObject(type).getObjectKind() == MetaObjectKind.PRIMITIVE_TYPE_OBJECT){
	    frame.enter(binder(MetaNames.TYPE.toString(), new StringResult(new MBPrimitiveType(type).getTypeKind().toString())));	   
	}else
	frame.enter(binder(MetaNames.TYPE.toString(), new ReferenceResult(type)));
    }
    
    private void pushAST(StackFrame frame, byte[] ast) throws DatabaseException{
	ASTNode node = BuilderUtils.deserializeAST(ast);
	String body = new AST2TextQueryDumper().dumpAST(node);
	frame.enter(binder(MetaNames.AST.toString(), new StringResult(body)));
    }
    
    private void pushReferenceLevel(StackFrame frame, int ref) throws DatabaseException{
	
	pushInteger(frame, MetaNames.REFERENCE.toString(), ref);
    }
    
    private void pushReversePointer(StackFrame frame, OID reverse) throws DatabaseException{
	pushOID(frame, MetaNames.REVERSE.toString(), reverse);
	pushOID(frame, reverse);	
    }
    
    private void pushArguments(StackFrame frame, OID[] args) throws DatabaseException{
	
	for(OID argument : args){
	    pushOID(frame, argument);	    
	    pushOID(frame, MetaNames.ARGUMENT.toString(), argument);	    
	}
    }
    
    private void pushInstanceName(StackFrame frame, String name) throws DatabaseException{	
	frame.enter(binder(MetaNames.INSTANCE.toString(), new StringResult(name)));
    }
    
    private void pushSuperClasses(StackFrame frame, OID[] scls) throws DatabaseException{
	
	for(OID cls : scls){
	    pushOID(frame, cls);	    
	    pushOID(frame, MetaNames.SUPERCLASS.toString(), cls);	    
	}
    }
    
    private void pushMethods(StackFrame frame, OID[] mths) throws DatabaseException{
	
	for(OID mth : mths){
	    pushOID(frame, mth);	    
	    pushOID(frame, MetaNames.METHOD.toString(), mth);	    
	}
    }
    
    private void pushView(StackFrame frame, OID view) throws DatabaseException{
	
	frame.enter(binder(MetaNames.VIEW.toString(), new ReferenceResult(view)));
    }
    
    private void pushGenericViewProcedures(StackFrame frame, OID[] prcs) throws DatabaseException{
	
	for(OID prc : prcs) {
	    frame.enter(binder(prc.getObjectNameId(), new ReferenceResult(prc)));
	    frame.enter(binder(MetaNames.GENPROC.toString(), new ReferenceResult(prc)));
	}
    }
    
    private void pushSubViews(StackFrame frame, OID[] sbvs) throws DatabaseException{
	
	for(OID sbv : sbvs){
	    frame.enter(binder(sbv.getObjectNameId(), new ReferenceResult(sbv)));
	    frame.enter(binder(MetaNames.SUBVIEW.toString(), new ReferenceResult(sbv)));
	}
    }
    private void pushViewFields(StackFrame frame, OID[] flds) throws DatabaseException{
	
	for(OID fld : flds){
	    frame.enter(binder(fld.getObjectNameId(), new ReferenceResult(fld)));
	    frame.enter(binder(MetaNames.VIEW_FIELD.toString(), new ReferenceResult(fld)));
	}
    }
    
    private void pushVirtualFields(StackFrame frame, OID[] flds) throws DatabaseException{
	
	for(OID fld : flds){
	    frame.enter(binder(fld.getObjectNameId(), new ReferenceResult(fld)));
	    frame.enter(binder(MetaNames.VIRTUAL_FIELD.toString(), new ReferenceResult(fld)));
	}
    }
    
    private void pushFields(StackFrame frame, OID[] flds) throws DatabaseException{
	
	for(OID fld : flds){
	    frame.enter(binder(fld.getObjectNameId(), new ReferenceResult(fld)));
	    frame.enter(binder(MetaNames.FIELD.toString(), new ReferenceResult(fld)));
	}
    }
    private void pushVirtualObject(StackFrame frame, OID vrtobj) throws DatabaseException{	
	    frame.enter(binder(vrtobj.getObjectNameId(), new ReferenceResult(vrtobj)));
	    frame.enter(binder(MetaNames.VIRTUAL_OBJECT.toString(), new ReferenceResult(vrtobj)));
    }

    private final void pushLinkInfo(StackFrame frame, MBLink link) throws DatabaseException{
	frame.enter(binder(MetaNames.HOST.toString(), new StringResult(link.getHost())));
	frame.enter(binder(MetaNames.PORT.toString(), new IntegerResult(link.getPort())));
	frame.enter(binder(MetaNames.SCHEMA.toString(), new StringResult(link.getSchema())));
    }
    private void nestedMetabaseRoot(StackFrame frame, OID root) throws DatabaseException{
	for(OID obj: root.derefComplex()){
	    frame.enter(binder(obj.getObjectNameId(), new ReferenceResult(obj)));
	    MetaObjectKind kind = new MBObject(obj).getObjectKind();
	    
	    frame.enter(binder(kind.toString(), new ReferenceResult(obj)));
			    					   
	}
    }
    
    private final void pushOID(StackFrame frame, OID oid)throws DatabaseException{
	frame.enter(binder(oid.getObjectNameId(), new ReferenceResult(oid)));
    }
    private final void pushOID(StackFrame frame, String name,OID oid)throws DatabaseException{
	frame.enter(binder(name, new ReferenceResult(oid)));
    }
    
    
    private void pushInteger(StackFrame frame, String name, int value)throws DatabaseException{
	frame.enter(binder(name, new IntegerResult(value)));
    }
    
    
    
    private final int name2id(String name)throws DatabaseException{
	return Database.getNameIndex().addName(name);
    }
    
    private final Binder binder(int nameid, SingleResult result)throws DatabaseException{
	return new Binder(nameid,result);
    }
    private final Binder binder(String name, SingleResult result)throws DatabaseException{
	return new Binder(name2id(name),result);
    }
    

}
