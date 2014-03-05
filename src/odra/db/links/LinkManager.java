package odra.db.links;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.links.encoders.RemoteQueryParameterEncoder;
import odra.db.links.encoders.RemoteQueryResultDecoder;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBSchema;
import odra.db.objects.meta.MBLink;
import odra.exceptions.rd.RDDatabaseException;
import odra.exceptions.rd.RDException;
import odra.exceptions.rd.RDNetworkException;
import odra.exceptions.rd.RDStaleMetaBaseException;
import odra.network.encoders.messages.MessageDecoder;
import odra.network.encoders.messages.MessageEncoder;
import odra.network.encoders.metabase.MetaDecoder;
import odra.network.encoders.signatures.SBQLPrimitiveSignatureEncoder;
import odra.network.encoders.stack.SBQLStackEncoder;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.compiletime.util.ValueSignatureInfo;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.stack.ISBQLStackSerializer;
import odra.sbql.stack.SBQLStack;
import odra.security.UserContext;
import odra.system.Names;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.GridException;
import odra.virtualnetwork.RemoteP2PStore;
import odra.virtualnetwork.cmu.CMUnit;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.pu.Repository;

/**
 * This class is responsible for managing database links. Database links represent connections
 * between distributed databases. Each database link is an object containing the following
 * information: - target schema - compound name of the target module (e.g. raist, raist.mymodule) -
 * password - password of the user who owns the target module - host - host name of the target
 * database - port - port at which the target database listens for incoming connections
 * 
 * Each user module contains a list of pointers to all database links created by that user. When a
 * remote reference is dereferenced, the list of all database links is searched. During the search
 * information from the currently analyzed database link is compared to its counterparts from the
 * remote reference (i.e. target host, target schema, target port, etc.). If the data matches, the
 * link is taken as the base of a connection to the target database.
 * 
 * @author raist
 */
 
public class LinkManager
{
	private static ThreadLocal<LinkManager> current = new ThreadLocal<LinkManager>();
	private Hashtable<Integer, DBConnection> connections = new Hashtable<Integer, DBConnection>();
	
	private LinkManager()
	{
	}

	public static LinkManager getInstance()
	{
		if (current.get() == null)
			current.set(new LinkManager());
		return current.get();
	}

	/**
	 * Creates a new DBLink object in the database
	 * 
	 * @param name
	 *            name of the database link object
	 * @param mod
	 *            module in which the object should be created
	 * @param host
	 *            target host name
	 * @param port
	 *            target port name
	 * @param schema
	 *            compound name of the target module (e.g. raist, raist.mymodule)
	 * @param password
	 *            password of the user who owns the target module
	 * @return the newly create DBLink object
	 */
	public DBLink createLink(String name, DBModule mod, String host, int port, String schema, String password)
			throws DatabaseException, RDException
	{
		DBLink dbl = null;
		OID mbLink = null;

		try
		{
			OID data = mod.getDatabaseEntry();
			IDataStore store = data.getStore();
			DBModule module = Database.getModuleByName(mod.getSchema());

			// check if link with a given name exists
			if (module.findFirstByName(name, data) != null)
			{
				throw new RDDatabaseException("Link with this name already exists - link name : " + name);
			}

			// creates meta database link
			mbLink = createMetaLink(name, host, port, schema, mod);

			OID dbs = store.createComplexObject(store.addName(name), data, DBLink.FIELD_COUNT);

			dbl = new DBLink(dbs);
			dbl.initialize(host, port, schema, password, mbLink);

			OID prvlnksoid = module.findFirstByNameId(Names.S_PRVLINKS_ID, module.getDatabaseEntry());

			if (prvlnksoid == null)
				throw new DatabaseException("Invalid user account configuration");

			module.createPointerObject(name, prvlnksoid, dbs);
			// fetch remote metabase
			refreshLinkMetadata(dbl);

		}
		catch (Exception e)
		{
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during metadata refresh", e);
			if (ConfigServer.DEBUG_EXCEPTIONS)
				e.printStackTrace();

			// if any exception arises , delete the link
			removeLink(name, mod.getName() );

			throw new RDDatabaseException(e.getMessage());
		}

		return dbl;
	}

	private OID createMetaLink(String name, String host, int port, String schema, DBModule module) throws DatabaseException
	{
		OID linkid = module.createComplexObject(name, module.getMetabaseEntry(), MBLink.FIELD_COUNT);
		new MBLink(linkid).initialize(host, port, schema);

		return linkid;
	}

	public void removeLink(String lnkname, String modname) throws RDNetworkException
	{
		DBModule mod;
		try
		{
			mod = Database.getModuleByName(modname);
			OID linkOID = mod.findFirstByName(lnkname, mod.getDatabaseEntry());

			if (linkOID != null)
			{
				DBLink dbl = new DBLink(linkOID);
				DBConnection conn = this.getConnectionForLink(dbl);
				if (conn.isConnected())
				{
					conn.close();
					this.connections.remove(dbl.getOID().internalOID());
				}
				
				OID prvlnksoid = mod.findFirstByNameId(Names.S_PRVLINKS_ID, mod.getDatabaseEntry());
				if (prvlnksoid != null)
				{
					for( OID oidPrv : prvlnksoid.derefComplex() )
					{
						if ( oidPrv.getObjectName().equals(lnkname) )
							oidPrv.delete();
					}
				}
				
				// remove metabase
				dbl.getMBLink().getOID().deleteAllChildren();
				dbl.getMBLink().getOID().delete();
				
				// remove link
				linkOID.deleteAllChildren();
				linkOID.delete();				
			}				
		}
		catch (DatabaseException e)
		{
			if (ConfigServer.DEBUG_EXCEPTIONS)
				e.printStackTrace();

			throw new RDNetworkException("exception when removing link");
		}
		catch (Exception e)
		{
			if (ConfigServer.DEBUG_EXCEPTIONS)
				e.printStackTrace();

			throw new RDNetworkException("exception when removing link");
		}
	}
	
	
	/**
	 * Creates a new DBLink object in the database (grid variant)
	 * 
	 * @param name
	 *            name of the database link object
	 * @param mod
	 *            module in which the object should be created
	 * @param peer
	 *            target peer name
	 * @param schema
	 *            compound name of the target module (e.g. raist, raist.mymodule)
	 * @param password
	 *            password of the user who owns the target module
	 * 
	 * @return the newly create DBLink object
	 */
	public DBLink createGridLink(String name, DBModule mod, String peerName, String schema, String password) throws DatabaseException,
			RDException
	{
		DBLink dbl = null;
		OID mbLink = null;

		try
		{

			OID data = mod.getDatabaseEntry();
			IDataStore store = data.getStore();
			DBModule module = Database.getModuleByName(mod.getSchema());
	
			// check if link with a given name exists
			if (module.findFirstByName(name, data) != null)
			{
				//silently go away
				//throw new RDDatabaseException("Link with this name already exists - link name : " + name);
				return null;
			}
			
			if (Repository.getInstance().getPeer(peerName) == null)
				throw new DatabaseException("Peer discovery failed");
	
			// creates meta database link
			mbLink = createMetaLink(name, peerName, 0, schema, mod);
	
			OID dbs = store.createComplexObject(store.addName(name), data, DBLink.FIELD_COUNT);
	
			dbl = new DBLink(dbs);
			dbl.initialize(peerName, schema, password, mbLink);

			OID prvlnksoid = module.findFirstByNameId(Names.S_PRVLINKS_ID, module.getDatabaseEntry());
	
			if (prvlnksoid == null)
				throw new DatabaseException("Invalid user account configuration");
	
			module.createPointerObject(name, prvlnksoid, dbs);
	
			// fetch metabase
			refreshLinkMetadata(dbl);

		}
		catch (Exception e)
		{
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during metadata refresh", e);
			if (ConfigServer.DEBUG_EXCEPTIONS)
				e.printStackTrace();
		
			// if any exception arises, be quiet (for test) 
			//removeGridLink(name, mod.getName() );
		
			throw new RDDatabaseException(e.getMessage());
		}
	
	return dbl;
	}
	
	


	public void removeGridLink(String lnkname, String modname) throws RDNetworkException
	{
		DBModule mod;
		try
		{
			mod = Database.getModuleByName(modname);
			OID linkOID = mod.findFirstByName(lnkname, mod.getDatabaseEntry());

			if (linkOID != null)
			{
				DBLink dbl = new DBLink(linkOID);

				//it needs to be checked!!!
/*				DBConnection conn = this.getConnectionForGridLink(dbl);
				if (conn.isConnected())
				conn.close();
*/
				
				OID prvlnksoid = mod.findFirstByNameId(Names.S_PRVLINKS_ID, mod.getDatabaseEntry());
				if (prvlnksoid != null)
				{
					for( OID oidPrv : prvlnksoid.derefComplex() )
					{
						if ( oidPrv.getObjectName().equals(lnkname) )
							oidPrv.delete();
					}
				}
				
				// remove metabase
				dbl.getMBLink().getOID().deleteAllChildren();
				dbl.getMBLink().getOID().delete();
				
				// remove link
				linkOID.deleteAllChildren();
				linkOID.delete();				
			}				
		}
		catch (DatabaseException e)
		{
			if (ConfigServer.DEBUG_EXCEPTIONS)
				e.printStackTrace();

			throw new RDNetworkException("exception when removing link");
		}
		catch (Exception e)
		{
			if (ConfigServer.DEBUG_EXCEPTIONS)
				e.printStackTrace();

			throw new RDNetworkException("exception when removing link");
		}
	}


	/**
	 * Prepares a new DBConnection object using the information extracted from a DBLink object.
	 * 
	 * @param link
	 *            the database link
	 * @return DBConnection the newly created connection object
	 */
	private DBConnection prepareConnection(DBLink link) throws DatabaseException, RDException, IOException
	{
		DBConnection conn = new DBConnection(link.getHost(), link.getPort());
		conn.connect();

		String user = link.getSchema();

		StringTokenizer tokenizer = new StringTokenizer(link.getSchema(), ".");
		if (tokenizer.hasMoreTokens())
			user = tokenizer.nextToken();

		DBRequest lreq = new DBRequest(DBRequest.LOGIN_RQST, new String[] { user, link.getPassword() });
		DBReply lrep = conn.sendRequest(lreq);

		return conn;
	}

	/**
	 * Sends a query through a database link, and puts result on the stack
	 * 
	 * @param link
	 *            database link which used as a communication channel
	 * @param query
	 *            query being sent
	 * @throws RDStaleMetaBaseException 
	 */
	public void evaluateRemoteQuery(DBLink link, String query, int parmCount,  SBQLStack stack, ArrayList<ValueSignatureInfo> parmSignatures, UserContext usrctx, boolean asynchronic, int rquery_id) throws RDException, RDStaleMetaBaseException
	{
		try
		{
			DBConnection conn = this.getConnectionForLink(link);			

			if ( !isLinkMetaBaseUptoDate(conn, link.getSchema() , link.getMBLink().getMetaBase().getSerial()) )
				throw new RDStaleMetaBaseException("Stale link metabase. Refresh link's metabase");	
			
			// get parms. from stack
			ISBQLStackSerializer encoder = new SBQLStackEncoder(new RemoteQueryParameterEncoder(link));
			String rawStack = new String(stack.getAsBytes(encoder, parmCount));
			for (int i = 0; i < parmCount; i++)
			{
				stack.pop();
			}
			
			SBQLPrimitiveSignatureEncoder signEncoder = new SBQLPrimitiveSignatureEncoder();
			String rawSignatures = new String( signEncoder.encode(parmSignatures) );

			DBRequest qreq = new DBRequest(DBRequest.EXECUTE_REMOTE_SBQL_RQST, new String[] { query, link.getSchema(), "off", "off" , rawStack, rawSignatures});
			if (asynchronic) {

				int qresindex = stack.pushEmptyResultFrame();
				AsynchronousRemoteQueriesManager asynchronousRemoteQueriesManager = AsynchronousRemoteQueriesManager.getCurrent();
				asynchronousRemoteQueriesManager.registerAsynchronousRemoteQuery(rquery_id);
				new AsynchronousRemoteQueryEvaluator(qreq, conn, usrctx, link, stack, qresindex, rquery_id, asynchronousRemoteQueriesManager).start();
							
			} else {

				DBReply qrep = conn.sendRequest(qreq);

				byte[] rawres = qrep.getRawResult();

				RemoteQueryResultDecoder decoder = new RemoteQueryResultDecoder(usrctx, link);
				Result res = decoder.decodeResult(rawres);

				stack.push(res);								
			}
			
			
		}
		catch (IOException ex)
		{
			throw new RDNetworkException(ex.getMessage());
		}
		catch (Exception ex)
		{
			if (ConfigServer.DEBUG_EXCEPTIONS)
				ex.printStackTrace();
			
			throw new RDNetworkException(ex.getMessage());
		}
	}
	
	/**
	 * Retrieves remote metabase and reconstructs it at local node.
	 * 
	 * @param link
	 *            database link pointing to remote node from which the metabase is to be gathered.
	 * @throws DatabaseException
	 * @throws RDException
	 */
	public void refreshLinkMetadata(DBLink link) throws DatabaseException, RDException
	{
		try
		{
			if (link.getGrid())
			{
				String peerName = link.getHost();
				String peerUser = link.getUser();
				String schema = link.getSchema();
				
				DBRequest mReq = new DBRequest(DBRequest.GET_METADATA_RQST, new String[] { schema});				
				byte[] rawrply = Repository.getInstance().putRequest(peerName, peerUser, 
						new MessageEncoder().encodeRequestMessage(new DBRequest[] { mReq }));
				
				if (Config.peerType == Config.PEER_TYPE.PEER_CMU){
					CMUnit.getInstance().cmu_handler.peerChangedMetabase(peerName, schema, peerUser);
				}
				
				DBReply[] dbreps = new MessageDecoder().decodeReplyMessage(rawrply);
				new MetaDecoder(link.getMBLink().getMetaBase()).decodeMeta(dbreps[0].getRawResult());			
			}
			else
			{
			    	ConfigServer.getLogWriter().getLogger().log(Level.FINER, "Getting metadata for link '" + link.getName() +"' for: '" + link.getSchema() + "@" + link.getHost() + link.getPort() + "'");
				DBConnection conn = this.getConnectionForLink(link);

				DBRequest mReq = new DBRequest(DBRequest.GET_METADATA_RQST, new String[] { link.getSchema() });
				DBReply mRepl = conn.sendRequest(mReq);

				byte[] rawRes = mRepl.getRawResult();
				new MetaDecoder(link.getMBLink().getMetaBase()).decodeMeta(rawRes);
				ConfigServer.getLogWriter().getLogger().log(Level.FINER, "Metadata for link '" + link.getName() +"' created");
			}
		}
		catch (IOException e)
		{
			// TODO if any exception arises dispose the metabase
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during metadata refresh", e);
			if (ConfigServer.DEBUG_EXCEPTIONS)
				e.printStackTrace();

			throw new RDNetworkException(e.getMessage());
		} catch (GridException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Result remoteBind(DBLink link, String name, UserContext usrctx) throws DatabaseException, RDException, RDStaleMetaBaseException
	{
		MessageEncoder encoder = new MessageEncoder();
		MessageDecoder decoder = new MessageDecoder();

		if (link.getGrid())
		{
			DBRequest qreq = new DBRequest(DBRequest.REMOTE_GLOBAL_BIND_RQST, new String[] { link.getSchema(), name });
			try
			{
				String peerName = link.getHost();	
				byte[] veryrawrply = Repository.getInstance().putRequest( peerName, link.getUser(),
						encoder.encodeRequestMessage(new DBRequest[] { qreq } ));
				DBReply[] dbreps = decoder.decodeReplyMessage(veryrawrply);
				
				return new RemoteQueryResultDecoder(usrctx, link).decodeResult((dbreps[0]).getRawResult());

			}
			catch (IOException e)
			{
				throw new RDNetworkException(e.getMessage());
			} catch (GridException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try
		{
			DBConnection conn = this.getConnectionForLink(link);
			
			if ( !isLinkMetaBaseUptoDate(conn, link.getSchema() , link.getMBLink().getMetaBase().getSerial()) )
				throw new RDStaleMetaBaseException("Stale link metabase. Refresh link's metabase");
				
			RemoteQueryResultDecoder qrDecoder = new RemoteQueryResultDecoder(usrctx, link);

			DBRequest qreq = new DBRequest(DBRequest.REMOTE_GLOBAL_BIND_RQST, new String[] { link.getSchema(), name });
			DBReply qrep = conn.sendRequest(qreq);

			byte[] rawres = qrep.getRawResult();

			return qrDecoder.decodeResult(rawres);
		}
		catch (IOException ex)
		{
			throw new RDNetworkException(ex.getMessage());
		}
	}

	public Result remoteCall(ReferenceResult rres, SBQLStack stack, int sectionsNumber, UserContext usrctx)
			throws DatabaseException, RDException
	{
	    assert rres.value instanceof RemoteDefaultStoreOID: "remote refrence required";
		RemoteDefaultStoreOID remoteProcOid = (RemoteDefaultStoreOID)rres.value;
		try
		{
			DBLink link = findLink(remoteProcOid, usrctx);
			ISBQLStackSerializer encoder = new SBQLStackEncoder(new RemoteQueryParameterEncoder(link));
				
			String rawStack = new String(stack.getAsBytes(encoder, sectionsNumber));
			for (int i = 0; i < sectionsNumber; i++)
			{
				stack.pop();
			}

			String rawProcRef = new String(new RemoteQueryParameterEncoder(link, true).encodeResult(rres));
			DBRequest qreq = new DBRequest(DBRequest.REMOTE_PROCEDURE_CALL_RQST, new String[] { link.getSchema(),
					rawProcRef, rawStack });
			
			byte[] rawres = null; 
			
			if (link.getGrid()){
				byte [] veryrawrply = Repository.getInstance().putRequest(link.getHost(),link.getUser(), 
						new MessageEncoder().encodeRequestMessage( new DBRequest[] {qreq}));
				 
				rawres = new MessageDecoder().decodeReplyMessage(veryrawrply)[0].getRawResult();
			} else {
				DBConnection conn = this.getConnectionForLink(link);
				DBReply qrep = conn.sendRequest(qreq);
				
				rawres = qrep.getRawResult();
			}
			
		
			RemoteQueryResultDecoder decoder = new RemoteQueryResultDecoder(usrctx, link);
			return decoder.decodeResult(rawres);

		}
		catch (IOException ex)
		{
			throw new RDNetworkException(ex.getMessage());
		}
		catch (Exception ex)
		{
			throw new RDNetworkException(ex.getMessage());
		}
	}

	/**
	 * Finds a database link in current user's main module
	 * 
	 * @param rres
	 *            remote reference which is a base of the search for the link
	 * @param usrctx
	 *            session information about the current user executing the operation
	 * @return the database link matching the information stored in the remote reference
	 */
	public DBLink findLink(String host, String schema, int port, UserContext usrctx) throws DatabaseException, RDException
	{
		return this.findLink(null, host, schema, port, usrctx);
	}
	
	public DBLink findLink(String peerName, String schema, UserContext usrctx) throws DatabaseException, RDNetworkException{
		DBModule module = Database.getModuleByName(usrctx.getUserName());
		OID prvlnksoid = module.findFirstByNameId(Names.S_PRVLINKS_ID, module.getDatabaseEntry());
		if (prvlnksoid == null)
			throw new DatabaseException("Invalid user account configuration");

		OID[] links = prvlnksoid.derefComplex();

		for (OID oid : links)
		{
			DBLink link = new DBLink(oid.derefReference());
			//change laster getHost -->> getPeerName
			if (link.getHost().equals(peerName))
				return link;
		}

		throw new RDNetworkException("Database gridlink for " + schema+ "@" + peerName + " has not been defined.");
	}
	 
	public DBLink findLink(String linkName, String host, String schema, int port, UserContext usrctx) throws DatabaseException, RDException
	{
		DBModule module = Database.getModuleByName(usrctx.getUserName());

		OID prvlnksoid = module.findFirstByNameId(Names.S_PRVLINKS_ID, module.getDatabaseEntry());

		if (prvlnksoid == null)
			throw new DatabaseException("Invalid user account configuration");

		OID[] links = prvlnksoid.derefComplex();

		for (OID oid : links)
		{
			DBLink link = new DBLink(oid.derefReference());

			if (linkName != null)
			{
				if (oid.getObjectName().equals(linkName) && link.getHost().equals(host) && link.getPort() == port
						&& link.getSchema().equals(schema))
					return link;
			}
			else if (link.getHost().equals(host) && link.getPort() == port && link.getSchema().equals(schema))
				return link;
		}

		throw new RDNetworkException("Database link for " + schema + "@" + host + ":" + port + " has not been defined.");
	}

	DBLink findLink(RemoteDefaultStoreOID oid, UserContext ctx) throws DatabaseException, RDException
	{
		if (oid.getStore().getClass().equals(RemoteDefaultStore.class)){ 
			RemoteDefaultStore store = (RemoteDefaultStore) oid.getStore();
			return findLink(store.host, store.schema, store.port, ctx);
		} else {
			RemoteP2PStore store = (RemoteP2PStore) oid.getStore();
			return findLink(store.peerName, store.schema, ctx);
		}	
	}

	
	/**
	 * Validates if current link's metabase is uptodate.
	 * If the link's metabase is not up to date an exception is risen.
	 * 
	 * @param conn
	 * @param schema
	 * @param serial
	 * @return 
	 * 		true if valid
	 * 		false if stale 
	 * @throws IOException
	 * @throws RDException
	 */
	private boolean isLinkMetaBaseUptoDate(DBConnection conn, String schema, long serial ) throws IOException, RDException
	{		
		try
		{
			DBRequest qreq = new DBRequest(DBRequest.VALIDATE_METABASE_SERIAL_RQST, new String[] { schema, String.valueOf(serial) });
			DBReply qrep = conn.sendRequest(qreq);
		}
		catch (RDStaleMetaBaseException e)
		{		
			return false;
		}

		return true;
	}
	
	public boolean isLinkMetaBaseUptoDate(DBLink link ) throws IOException, RDException, DatabaseException 
	{		
		if (link.getGrid()) {
			MessageEncoder encoder = new MessageEncoder();
			MessageDecoder decoder = new MessageDecoder();
			DBRequest qreq = new DBRequest(DBRequest.VALIDATE_METABASE_SERIAL_RQST, new String[] { link.getSchema(), String.valueOf(link.getMBLink().getMetaBase().getSerial()) });
			try {
				byte [] rply = Repository.getInstance().putRequest(
							link.getHost(),link.getUser(), encoder.encodeRequestMessage(new DBRequest[] { qreq } ));
				if (decoder.decodeReplyMessage(rply)[0].isErrorReply())
					return false;
			} catch (GridException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();				
			}
			return true;
		}
		DBConnection conn = this.getConnectionForLink(link);		
		return isLinkMetaBaseUptoDate(conn, link.getSchema(), link.getMBLink().getMetaBase().getSerial() );
	}
	
	/**
	 * @param link
	 * @return connection for a given link
	 * @throws DatabaseException
	 * @throws RDException
	 * @throws IOException
	 */
	DBConnection getConnectionForLink(DBLink link) throws DatabaseException, RDException, IOException
	{
		DBConnection conn = this.connections.get(link.getOID().internalOID());
		if (conn == null || !conn.isConnected())
		{
			// ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "connection for " + link.getOID().getObjectName());
			conn = this.prepareConnection(link);
			this.connections.put(link.getOID().internalOID(), conn);
		}
		return conn;
	}

	DBConnection getConnectionForGridLink(DBLink link) throws DatabaseException, RDException, IOException
	{
		DBConnection conn = this.connections.get(link.getOID().internalOID());
		return conn;
	}

	
	public void closeConnections()
	{
		for (Enumeration<DBConnection> e = this.connections.elements(); e.hasMoreElements();)
		{
			try
			{
				e.nextElement().close();
			}
			catch (IOException e1)
			{
				if (ConfigServer.DEBUG_EXCEPTIONS)
					e1.printStackTrace();
			}
		}
		this.connections.clear();
	}

	public void addLinkToSchema(String lnkname, OID schemaoid, DBModule mod) throws RDDatabaseException {

		try
		{

			DBSchema schema = new DBSchema(schemaoid);
			
			if (!schema.isValid())
				throw new RDDatabaseException("valid schema not found");

			OID linkOID = mod.findFirstByName(lnkname, mod.getDatabaseEntry());

			if (linkOID != null)
			{
				DBLink dbl = new DBLink(linkOID);
				
				if (dbl.getMBLink().getMetaBase().
						comformsTo(schema.getMBSchema().getMetaBase()))
					schema.addLink(dbl);
				else 
					throw new RDDatabaseException("Link " + lnkname + " does not comform to the given schema");
			} else 
				throw new RDDatabaseException("Link with this name does not exists - link name : " + lnkname);
			
		}
		catch (Exception e)
		{
			throw new RDDatabaseException(e.getMessage());
		}
		
	}

	public void removeLinkFromSchema(String lnkname, OID schemaoid, DBModule mod) throws RDDatabaseException {

		try
		{

			DBSchema schema = new DBSchema(schemaoid);
			
			if (!schema.isValid())
				throw new RDDatabaseException("valid schema not found");

			OID linkOID = mod.findFirstByName(lnkname, mod.getDatabaseEntry());

			if (linkOID != null)
			{
				DBLink dbl = new DBLink(linkOID);
				if (schema.containsLink(dbl))
					schema.removeLink(dbl);
				else 
					throw new RDDatabaseException("Given schema does not contain link " + lnkname);				
			} else 
				throw new RDDatabaseException("Link with this name does not exists - link name : " + lnkname);
			
		}
		catch (Exception e)
		{
			throw new RDDatabaseException(e.getMessage());
		}
	}
}
