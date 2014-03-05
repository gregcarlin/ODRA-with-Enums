package odra.sessions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import odra.OdraCoreAssemblyInfo;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.links.AsynchronousRemoteQueriesManager;
import odra.db.links.LinkManager;
import odra.db.objects.data.DBModule;
import odra.sbql.external.ExternalRoutines;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.compiletime.util.SignatureInfo;
import odra.sbql.results.runtime.BagResult;
import odra.security.AuthenticationException;
import odra.security.Authenticator;
import odra.security.UserContext;
import odra.store.TransientStore;
import odra.store.guid.GUID;
import odra.store.guid.IGUIDIdentifiableResource;
import odra.store.io.AutoExpandableHeap;
import odra.store.io.AutoExpandablePowerHeap;
import odra.store.io.IHeap;
import odra.store.memorymanagement.AutoExpandableMemManager;
import odra.store.memorymanagement.ConstantSizeObjectsMemManager;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.sbastore.ObjectManager;
import odra.store.sbastore.SpecialReferencesManager;
import odra.store.sbastore.ValuesManager;
import odra.store.transience.DataMemoryBlockHeap;
import odra.system.config.ConfigServer;
import odra.system.log.UniversalLogger;
import odra.transactions.store.ITransactionLock;
import odra.transactions.store.Transaction;
import odra.transactions.store.TransactionStoreException;
import odra.wrapper.Wrapper;

/**
 * Session represents client session in the system. Encapsulate session transient store, provides operations for
 * creating local environments
 * 
 * @author radamus last modified: 2007-03-27
 * @version 1.0
 * 
 * edek: some slight improvements related to transactions
 */
public final class Session implements IGUIDIdentifiableResource {

	private final static UniversalLogger logger = UniversalLogger.getInstance(OdraCoreAssemblyInfo.class, Session.class);

	/**
	 * session extent
	 */
	private final static List<Session> sessions = new LinkedList<Session>();

	/**
	 * current session
	 */
	private static ThreadLocal<Session> current = new ThreadLocal<Session>();

	/**
	 * active transaction
	 */
	private static ThreadLocal<Transaction> trnsActive = new ThreadLocal<Transaction>();

	/**
	 * wrapper container
	 */
	private static Hashtable<String, Wrapper> wrappers = new Hashtable<String, Wrapper>();

	/**
	 * grid contributed modules container
	 */
	private static Hashtable<Session, DBModule> contributedModules = new Hashtable<Session, DBModule>();

	
	private AsynchronousRemoteQueriesManager asynchronousRemoteQueriesManager = new AsynchronousRemoteQueriesManager();

	private ArrayList<SignatureInfo>  parmSigantures;
	
	private Vector<AbstractQueryResult>  remoteResultsStack;
	
	/**
	 * main transaction of the {@link Session}
	 */
	private final Transaction mainTransaction;

	// private Thread sessionThread;
	public UserContext usrctx;

	private IDataStore transientStore;

	long timeout;

	// session's external routines
	public ExternalRoutines extRoutines; // TW

	/**
	 * initialized modules session store container
	 */
	private final Hashtable<String, OID> sessionEnvironments;

	/**
	 * temporary indices container
	 */
	private final Hashtable<OID, OID> temporaryIndices;
	private final Hashtable<OID, BagResult> temporaryIndicesResults;
	
	/** optimization sequence */
	private final OptimizationSequence optimizationSequence;
	/** optimization reference sequence */
	private final OptimizationSequence optimizationReferenceSequence;

	private Session() {
		
		try {
			this.timeout = ConfigServer.SESSION_TIMEOUT;
			this.mainTransaction = Transaction.instantiate(this);
			trnsActive.set(this.mainTransaction);
			this.sessionEnvironments = new Hashtable<String, OID>();
			this.temporaryIndices = new Hashtable<OID, OID>();
			this.temporaryIndicesResults = new Hashtable<OID, BagResult>();
			this.optimizationSequence = new OptimizationSequence();
			this.optimizationReferenceSequence = new OptimizationSequence();
			extRoutines = new ExternalRoutines();// TW
		} catch (TransactionStoreException ex) {
			throw new SessionRuntimeException("instantiating session", ex);
		}
	}

	public static void create() {
		assert current.get() == null : "one server process cannot have more that one session object";
		Session session = new Session();
		current.set(session);
	}

	public static Session getCurrent() {
		assert current.get() != null : "no session object use Session.create() first";
		return current.get();
	}
	
	/**
	 * @return
	 */
	public static boolean exists() {		
		return current.get() != null;
	}
	public static void close() {
	    	logger.info("Session closed");
		Session ses = current.get();
		if (ses != null) {
			ses.closeSession();
			LinkManager.getInstance().closeConnections();
			current.set(null);
		}
	}

	public static void resetTime() {
		getCurrent().timeout = ConfigServer.SESSION_TIMEOUT;
	}

	public static boolean isInitialized() {
		return getCurrent().usrctx != null;
	}

	public static UserContext getUserContext() {
		return getCurrent().usrctx;
	}

	public static void initialize(String user, String password) throws AuthenticationException, DatabaseException {
		Session ses = getCurrent();		
		ses.usrctx = Authenticator.userPasswordAuthentication(user, password);
		// ses.initSessionTransientStore(1024 * 1024);
		// ses.initSessionTransientOptimizedStore(1024 * 1024);
		ses.initSessionTransientExpandableStore(1024 * 128);
		getModuleSessionEntry(user);
		logger.info("Session");
	}

	public static OID createLocalEnvironment() throws DatabaseException {
		assert isInitialized() : "session is not initialized";
		Session ses = getCurrent();
		return ses.transientStore.createComplexObject(Database.getStore().addName("$localEnv"), ses.transientStore
					.getEntry(), 0);
	}

	public static OID getModuleSessionEntry(String modglobalname) throws DatabaseException {
		assert isInitialized() : "session is not initialized";
		Session ses = getCurrent();
		OID sentry = ses.sessionEnvironments.get(modglobalname);
		if (sentry != null) {
			return sentry;
		} 
		/**
		 * lazy initialization
		 */
		sentry = ses.transientStore.createComplexObject(Database.getStore().addName("$sessionEnv" + modglobalname),
					ses.transientStore.getEntry(), 0);
		ses.sessionEnvironments.put(modglobalname, sentry);
		ses.initModuleSessionEnvironment(Database.getModuleByName(modglobalname));
		return sentry;
		
	}

	public static OID getModuleSessionEntry(DBModule mod) throws DatabaseException {
		assert isInitialized() : "session is not initialized";
		String modglobalname = mod.getModuleGlobalName();
		Session ses = getCurrent();
		OID sentry = ses.sessionEnvironments.get(modglobalname);
		if (sentry != null) {
			return sentry;
		} 
		// lazy initialization
		sentry = ses.transientStore.createComplexObject(Database.getStore().addName("$sessionEnv" + modglobalname),
					ses.transientStore.getEntry(), 0);
		
		ses.sessionEnvironments.put(modglobalname, sentry);
		ses.initModuleSessionEnvironment(mod);
		return sentry;
	}

	public static OID getTemporaryIndex(OID dbidxoid) throws DatabaseException {
		assert isInitialized() : "session is not initialized";
		Session ses = getCurrent();
		return ses.temporaryIndices.get(dbidxoid);
	}

	public static BagResult getTemporaryIndexResult(OID dbidxoid) throws DatabaseException {
		assert isInitialized() : "session is not initialized";
		Session ses = getCurrent();
		return ses.temporaryIndicesResults.get(dbidxoid);
	}
	
	public static OID addTemporaryIndex(OID dbidxoid, BagResult bres) throws DatabaseException {
		assert isInitialized() : "session is not initialized";
		Session ses = getCurrent();
		OID idxoid = ses.transientStore.createComplexObject(Database.getStore().addName("$tmpindex"),
						ses.transientStore.getEntry(), 0);
		ses.temporaryIndices.put(dbidxoid, idxoid);
		ses.temporaryIndicesResults.put(dbidxoid, bres);		
		return idxoid;
	}

	public static void removeTemporaryIndex(OID dbidxoid) throws DatabaseException {
		assert isInitialized() : "session is not initialized";
		Session ses = getCurrent();
		OID tmpidxoid = ses.temporaryIndices.get(dbidxoid);
		if (tmpidxoid != null) {
			tmpidxoid.delete();
			ses.temporaryIndices.remove(dbidxoid);
			ses.temporaryIndicesResults.remove(dbidxoid);
		}
	}

	public static void removeTemporaryIndices() throws DatabaseException {
		assert isInitialized() : "session is not initialized";
		Session ses = getCurrent();
		for(OID tmpidxoid : ses.temporaryIndices.values())
			tmpidxoid.delete();
		ses.temporaryIndices.clear();
		ses.temporaryIndicesResults.clear();
	}
	
	public static void removeModuleFromSession(String modglobalname) throws DatabaseException {
		Session ses = getCurrent();
		OID sentry = ses.sessionEnvironments.remove(modglobalname);
		if (sentry != null) {
			sentry.delete();
			wrappers.remove(modglobalname);
			contributedModules.remove(modglobalname);
		}
	}

	public static IDataStore getTransientStore() {
		assert isInitialized() : "session is not initialized";
		return getCurrent().transientStore;
	}

	private void closeSession() {
		usrctx = null;
		transientStore.close();
		transientStore = null;
		
		for (Wrapper wrapper : wrappers.values()) {
			wrapper.removeSessionData(this);
		}
		Transaction.terminate(this.mainTransaction);
	}

	/**
	 * Initializes transient store for the session Executed during sesssion initializaton process
	 */

	private void initSessionTransientStore(int size) throws DatabaseException {
		ObjectManager manager = null;

		DataMemoryBlockHeap dmbHeap = new DataMemoryBlockHeap(size);

		RevSeqFitMemManager allocator;

		allocator = new RevSeqFitMemManager(dmbHeap);
		allocator.initialize();

		manager = new ObjectManager(allocator);
		manager.initialize(100);
		transientStore = new TransientStore(manager);
	}

	/**
	 * Initializes transient store for the session Executed during sesssion initializaton process
	 */

	private void initSessionTransientOptimizedStore(int size) throws DatabaseException {
		IHeap dmbHeap = new DataMemoryBlockHeap(size);

		ConstantSizeObjectsMemManager allocator;

		allocator = new ConstantSizeObjectsMemManager(dmbHeap, ObjectManager.MAX_OBJECT_LEN);
		allocator.initialize();

		DataMemoryBlockHeap valuesdmbHeap = new DataMemoryBlockHeap(size);
		RevSeqFitMemManager valuesAllocator = new RevSeqFitMemManager(valuesdmbHeap);
		valuesAllocator.initialize();
		ValuesManager valuesManager = new ValuesManager(valuesAllocator);

		DataMemoryBlockHeap specdmbHeap = new DataMemoryBlockHeap(size);
		ConstantSizeObjectsMemManager specAllocator = new ConstantSizeObjectsMemManager(specdmbHeap,
					SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
		specAllocator.initialize();
		SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);

		ObjectManager manager = new ObjectManager(allocator, valuesManager, specManager);
		manager.initialize(100);
		transientStore = new TransientStore(manager);
	}

	/**
	 * Initializes transient store for the session. Executed during session initializaton process
	 */

	private void initSessionTransientExpandableStore(int size) throws DatabaseException {
		ObjectManager manager = null;

		AutoExpandableHeap dmbHeap = AutoExpandablePowerHeap.initializeTransientHeap(size);

		AutoExpandableMemManager allocator;

		allocator = AutoExpandableMemManager.startAutoExpandableConstantSizeObjectsMemManager(dmbHeap,
					ObjectManager.MAX_OBJECT_LEN);
		allocator.initialize();

		AutoExpandableHeap valuesdmbHeap = AutoExpandablePowerHeap.initializeTransientHeap(size);
		AutoExpandableMemManager valuesAllocator = AutoExpandableMemManager
					.startAutoExpandableRevSeqFitMemManager(valuesdmbHeap);
		valuesAllocator.initialize();
		ValuesManager valuesManager = new ValuesManager(valuesAllocator);

		AutoExpandableHeap specdmbHeap = AutoExpandablePowerHeap.initializeTransientHeap(size);
		AutoExpandableMemManager specAllocator = AutoExpandableMemManager
					.startAutoExpandableConstantSizeObjectsMemManager(specdmbHeap,
								SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
		specAllocator.initialize();
		SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);

		manager = new ObjectManager(allocator, valuesManager, specManager);
		manager.initialize(100);
		transientStore = new TransientStore(manager);
	}

	private void initModuleSessionEnvironment(DBModule mod) throws DatabaseException {
		byte[] init = mod.getSessionInitalizationCode();
		if (init.length > 0) {
			SBQLInterpreter inter = new SBQLInterpreter(mod);
			inter.runCode(init, null);
		}
	}

	/**
	 * Returns the optimization sequence.
	 * 
	 * @return optimization sequence
	 * 
	 * @author jacenty
	 */
	public OptimizationSequence getOptimizationSequence() {
		return optimizationSequence;
	}
	
	/**
	 * Returns the reference optimization sequence.
	 * 
	 * @return optimization sequence
	 * 
	 * @author jacenty
	 */
	public OptimizationSequence getOptimizationReferenceSequence() {
		return optimizationReferenceSequence;
	}

	/**
	 * Returns this session unique ID.
	 * 
	 * @return ID
	 * 
	 * @author jacenty
	 */
	public String getId() {
		return this.getGUID().toString();
	}

	public GUID getGUID() {
		return this.mainTransaction.getGUID();
	}

	/**
	 * Returns a wrapper instance associated with a module given.
	 * 
	 * @param module
	 *           {@link DBModule}
	 * @return {@link Wrapper}
	 * @throws DatabaseException
	 * 
	 * @author jacenty
	 */
	public static Wrapper getWrapper(DBModule module) throws DatabaseException {
		return wrappers.get(module.getModuleGlobalName());
	}

	/**
	 * Returns if there is a wrapper defined for a module given.
	 * 
	 * @param module
	 *           {@link DBModule}
	 * @return contains a wrapper?
	 * @throws DatabaseException
	 * 
	 * @author jacenty
	 */
	public static boolean hasWrapperForModule(DBModule module) throws DatabaseException {
		return wrappers.containsKey(module.getModuleGlobalName());
	}

	/**
	 * Adds a new wrapper definition associated with a module given.
	 * 
	 * @param module
	 *           {@link DBModule}
	 * @param wrapper
	 *           {@link Wrapper}
	 * @throws DatabaseException
	 * 
	 * @author jacenty
	 */
	public static void addWrapper(DBModule module, Wrapper wrapper) throws DatabaseException {
		if (hasWrapperForModule(module)) throw new DatabaseException("There is already a wrapper defined for module '"
					+ module.getModuleGlobalName() + "'.");

		wrappers.put(module.getModuleGlobalName(), wrapper);
	}

	/**
	 * Removes a wrapper definition for a module given.
	 * 
	 * @param module
	 *           {@link DBModule}
	 * @throws DatabaseException
	 * 
	 * @author jacenty
	 */
	public static void removeWrapper(DBModule module) throws DatabaseException {
		if (!hasWrapperForModule(module)) throw new DatabaseException("There is no wrapper defined for module '"
					+ module.getModuleGlobalName() + "'.");

		wrappers.remove(module.getModuleGlobalName());
	}
	
	/**
	 * Adds a grid contributed module to a session..
	 *
	 * @param module
	 *           {@link DBModule}
	 * 
	 * @author kamil
	 */
	public static void addGridContributedModule(DBModule contribModule) {
		Session currses = getCurrent();
		contributedModules.put(currses, contribModule);
	}

	/**
	 * Removes a grid contributed module from a session..
	 * 
	 * @param module
	 *           {@link DBModule}
	 * 
	 * @author kamil
	 */
	public static void removeGridContributedModule(DBModule contribModule) {
		contributedModules.remove(contribModule);
	}
	/**
	 * Returns a session with given grid contributed module.
	 * 
	 * @param module
	 *           {@link DBModule}
	 * @return {@link Session}
	 * 
	 * @author kamil
	 */
	public static Session getSessionForGridContributedModule(DBModule contribModule) {
		Session currses = getCurrent();
		if(currses.sessionEnvironments.containsKey(contribModule))
			return currses;
		else
			return null;
	}

	public int compareTo(IGUIDIdentifiableResource otherGUID) {
		return this.getGUID().compareTo(otherGUID.getGUID());
	}

	/**
	 * Instantiate a new {@link Transaction} for the current session
	 * 
	 * @return transaction
	 */
	public static Transaction instantiateTransaction() {
		Session sessCurrent = getCurrent();
		Transaction trnsNested = sessCurrent.mainTransaction.instantiateNested();
		trnsActive.set(trnsNested);
		return trnsActive.get();
	}

	/**
	 * Get active {@link Transaction} for the current session.
	 * 
	 * @return active transaction
	 */
	public static Transaction getActiveTransaction() {
		return trnsActive.get();
	}

	/**
	 * Get exclusive {@link ITransactionLock} of the active {@link Transaction} for the current session
	 * 
	 * @return exclusive lock
	 */
	public static ITransactionLock getExclusiveLock() {
		Transaction trnsActive = getActiveTransaction();
		return trnsActive.getExclusiveLock();
	}

	/**
	 * Get read {@link ITransactionLock} of the active {@link Transaction} for the current session
	 * 
	 * @return read lock
	 */
	public static ITransactionLock getReadLock() {
		Transaction trnsActive = getActiveTransaction();
		return trnsActive.getReadLock();
	}

	public static void commitActiveTransaction() {
		getActiveTransaction();
	}

	public ArrayList<SignatureInfo> getParmSigantures()
	{
		return parmSigantures;
	}

	public void setParmSigantures(ArrayList<SignatureInfo> parmsigantures)
	{
		this.parmSigantures = parmsigantures;
	}

	public Vector<AbstractQueryResult> getRemoteResultsStack()
	{
		return remoteResultsStack;
	}

	public void setRemoteResultsStack(Vector<AbstractQueryResult> remoteResultsStack)
	{
		this.remoteResultsStack = remoteResultsStack;
	}

	public boolean isParmDependent()
	{
		if (remoteResultsStack == null)
			return false;
		else if (remoteResultsStack.size() > 0)
			return true;
		else
			return false;
	}


	public static AsynchronousRemoteQueriesManager getAsynchronousRemoteQueriesManager() {
		assert isInitialized() : "session is not initialized";
		Session ses = getCurrent();
		
		return ses.asynchronousRemoteQueriesManager ;
	}
	
}