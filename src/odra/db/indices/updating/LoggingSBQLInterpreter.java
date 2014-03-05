package odra.db.indices.updating;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DataObjectKind;
import odra.sbql.interpreter.RuntimeBindingManager;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.stack.AggregateBinder;
import odra.sbql.stack.Binder;
import odra.sbql.stack.EnvsElement;
import odra.security.UserContext;
import odra.sessions.Session;
import odra.store.DefaultStoreOID;

public class LoggingSBQLInterpreter extends SBQLInterpreter {

	LoggingRuntimeBindingManager bindingManager;
	
	private LoggingSBQLInterpreter(DBModule module, LoggingRuntimeBindingManager bindingManager) throws DatabaseException {
		super(module, ExecutionMode.Data, bindingManager);
		this.bindingManager = bindingManager;
	}

	public Collection<OID> getOidlog() {
		return bindingManager.getOidlog();
	}
	
	public static LoggingSBQLInterpreter getLoggingSBQLInterpreterInstance(DBModule module) throws DatabaseException {
		return new LoggingSBQLInterpreter(module, new LoggingRuntimeBindingManager(Session.getUserContext()));
	}
	
	public static LoggingSBQLInterpreter getLoggingSBQLInterpreterInstance(DBModule module, DefaultStoreOID bindRoot) throws DatabaseException {
		if (bindRoot == null)
			return getLoggingSBQLInterpreterInstance(module);
		return new LoggingSBQLInterpreter(module, new BindingLimitingLoggingRuntimeBindingManager(Session.getUserContext(), bindRoot));
	}	
	
}

// TODO: All changes in RuntimeBindingManager has to be manually reflected in LoggingRuntimeBindingManager (can be fixed?)
class BindingLimitingLoggingRuntimeBindingManager extends LoggingRuntimeBindingManager {
	  
	DefaultStoreOID bindRoot; // if null - bind all!

	BindingLimitingLoggingRuntimeBindingManager(UserContext usrctx, DefaultStoreOID bindRoot) throws DatabaseException {
		super(usrctx);
		this.bindRoot = bindRoot;
	}

	public AbstractQueryResult[] bind(int name_id) throws DatabaseException {
		if (bindRoot == null)
			return super.bind(name_id);
		
		bag = new BagResult();
		
		// look for binders
		boolean found = findBinders(name_id);
		
		// look for nesters
		if (bindRoot != null)
			found = OIDs2Result(nesters2OIDs(name_id)) || found;	
					
		// ignoring remote nesters		
		
		if (!found)
			return new AbstractQueryResult[0];

		if (bindRoot != null) {
			for (SingleResult r : bag.elementsToArray()) 
				if ((r instanceof ReferenceResult) && ((ReferenceResult) r).value.equals(bindRoot)) {
					bag = new BagResult();
					bag.addElement(r);
					this.log(((ReferenceResult) r).value);
					bindRoot = null;
					break;
				}				
		} else 
			for (SingleResult r : bag.elementsToArray()) 
				if (r instanceof ReferenceResult)
					this.log(((ReferenceResult) r).value);
		
		return bagToReturnResult(bag);
	}
	
	@Override
	protected boolean binders2Result(Vector<EnvsElement> chain)
			throws DatabaseException {

		if (bindRoot != null) {
			Vector<EnvsElement> chainclone = (Vector<EnvsElement>) chain.clone();
			for (EnvsElement e : chainclone)
				if (e instanceof AggregateBinder) 
					if (((ReferenceResult) ((Binder) e).val).value.equals(bindRoot)) {
						chain = new Vector<EnvsElement>();
						chain.add(e);
						bindRoot = null;
						break;
					} else 
						chain.remove(e);
		}
			
		return super.binders2Result(chain);
	}

	@Override
	protected boolean OIDs2Result(Vector<OID> oidchain)
			throws DatabaseException {

		if (bindRoot != null) {
			Vector<OID> oidchainclone = (Vector<OID>) oidchain.clone();
			for (OID res : oidchain)
				if (res.isAggregateObject()) 
					if (res.equals(bindRoot)) {
						oidchain = new Vector<OID>();
						oidchain.add(res);
						bindRoot = null;
						break;
					} else 
						oidchain.remove(res);
		}
			
		return super.OIDs2Result(oidchain);
	}
	
}

//TODO: All changes in RuntimeBindingManager has to be manually reflected in LoggingRuntimeBindingManager (can be fixed?)
class LoggingRuntimeBindingManager extends RuntimeBindingManager {
	
	HashSet<OID> oidlog = new HashSet<OID>();  

	LoggingRuntimeBindingManager(UserContext usrctx) throws DatabaseException {
		super(usrctx);
	}
	
	public Collection<OID> getOidlog() {
		return oidlog;
	}
	
	protected void log(OID oid) throws DatabaseException {
		int kind = new DBObject(oid).getObjectKind().getKindAsInt();
		if (kind == DataObjectKind.DATA_OBJECT || kind >= DataObjectKind.ANNOTATED_STRING_OBJECT)
			oidlog.add(oid);
	}
	
	public AbstractQueryResult[] bind(int name_id) throws DatabaseException {
		bag = new BagResult();
		
		// look for binders
		boolean found = findBinders(name_id);
		
		// look for nesters
		found =	OIDs2Result(nesters2OIDs(name_id)) || found;	
					
		// ignoring remote nesters		
		
		if (!found)
			return new AbstractQueryResult[0];

		for (SingleResult r : bag.elementsToArray()) 
			if (r instanceof ReferenceResult)
				this.log(((ReferenceResult) r).value);
		
		return bagToReturnResult(bag);
	}

	@Override
	protected boolean binders2Result(Vector<EnvsElement> chain)
			throws DatabaseException {
		
		for (EnvsElement e : chain) {
			if (e instanceof AggregateBinder)
				this.log(((ReferenceResult) ((Binder) e).val).value);
		}
		
		return super.binders2Result(chain);
	}

	@Override
	protected boolean OIDs2Result(Vector<OID> oidchain)
			throws DatabaseException {
	
		for (OID res : oidchain)
			if (res.isAggregateObject())
				this.log(res);
		
		return super.OIDs2Result(oidchain);
	}	
	
}

