package odra.wrapper;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.schema.OdraViewSchema;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.emiter.JulietGen;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.sessions.Session;
import odra.system.config.ConfigDebug;
import odra.wrapper.importer.XMLImportFilter;
import odra.wrapper.importer.XSDImportFilter;
import odra.wrapper.model.Database;
import odra.wrapper.model.Name;
import odra.wrapper.model.Table;
import odra.wrapper.net.Client;
import odra.wrapper.resultpattern.ResultPattern;
import odra.wrapper.resultpattern.ResultPattern.Deref;
import odra.wrapper.resultpattern.ResultPattern.Type;
import odra.wrapper.viewgenerator.ViewGenerator;
import org.apache.tools.ant.filters.StringInputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Relational resource wrapper instantiation class.
 * 
 * @author jacenty
 * @version 2008-01-29
 * @since 2007-01-18
 */
public class Wrapper
{
	/** mode for SQL (standard) */
	public static final int MODE_SQL = 1;
	/** mode for SD-SQL */
	public static final int MODE_SD = 2;
	/** mode for SWARD */
	public static final int MODE_SWARD = 3;
	
	/** module */
	private final DBModule module;
	/** database model */
	private Database model;
	/** client */
	private Client client;
	
	/** session related wrapper stores */
	private Hashtable<String, WrapperStore> stores = new Hashtable<String, WrapperStore>();
	/** session related current results */
	private Hashtable<String, Vector<OID>> currentResults = new Hashtable<String, Vector<OID>>();

	/**
	 * The constructor
	 * 
	 * @param identity identity string
	 * @param host server host
	 * @param port server port
	 * @param mode mode
	 * @param module module
	 * @throws Exception
	 */
	public Wrapper(String identity, String host, int port, int mode, DBModule module) throws Exception
	{
		this.module = module;
		
		client = new Client(mode, identity, host, port);

		synchronized(identity)
		{
			testCommunication();
			model = loadModel();
			loadMetabase();
			createViews();
			module.setModuleLinked(false);
			module.setModuleCompiled(false);
		}
	}

	/**
	 * Performs a simple communication test.
	 * 
	 * @throws WrapperException
	 */
	public void testCommunication() throws WrapperException
	{
		client.go(Client.Target.COMMUNICATION_TEST, null);
	}

	/**
	 * Creates a metabase.
	 * 
	 * @return OIDs of created metaobjects
	 * @throws WrapperException
	 * @throws DatabaseException
	 * @throws ShadowObjectException
	 * @throws FilterException
	 */
	private OID[] loadMetabase() throws WrapperException, DatabaseException, FilterException, ShadowObjectException
	{
		client.go(Client.Target.METABASE, null);
		String xsd = (String)client.getReceivedObject();
//		System.err.println(xsd);
		XSDImportFilter importer = new XSDImportFilter(module, xsd);

		OID[] result = importer.importSchema(module);

		return result;
	}

	/**
	 * Creates views corresponding to the metabase.
	 * 
	 * @throws Exception
	 */
	private void createViews() throws Exception
	{
		OID moduleOid = module.getMetabaseEntry();
		Hashtable<String, Table> tables = model.getTables();
		// get Tables names == mbvariables names
		String[] tableNames = tables.keySet().toArray(new String[tables.size()]);

		ModuleOrganizer org = new ModuleOrganizer(module, true);
		ViewGenerator vg = ViewGenerator.getDefaultViewGenerator();
		// foreach table name find corresponding mbvariable
		for(String tableName : tableNames)
		{
			OID metaTableOid = module.findFirstByName(Name.r2o(tableName), moduleOid);
			if(metaTableOid != null)
			{
				MBVariable metaTable = new MBVariable(metaTableOid);
				assert metaTable.isValid() : "table name should be name of the meta-variable";

				OdraViewSchema viewInfo = vg.generateViewForRelationalTable(module, metaTable, tables.get(tableName));
				org.createView(viewInfo);
			}
		}
	}

	/**
	 * Loads a relational database model.
	 * 
	 * @return database model
	 * @throws WrapperException
	 */
	private Database loadModel() throws WrapperException
	{
		client.go(Client.Target.DATABASE, null);
		return (Database)client.getReceivedObject();
	}

	/**
	 * Returns a database model.
	 * 
	 * @return database model
	 */
	public Database getModel()
	{
		if(ConfigDebug.ASSERTS)
			assert model != null : "model should be retrieved before this call";

		return model;
	}

	/**
	 * Builds a result for tuple OIDs basing on a result pattern.
	 * 
	 * @param pattern {@link ResultPattern}
	 * @param oids tuple {@link OID}s
	 * @param interpreter {@link SBQLIntepreter}
	 * @return {@link SingleResult}
	 * @throws DatabaseException 
	 */
	private SingleResult buildResult(ResultPattern pattern, OID[] oids, SBQLInterpreter interpreter) throws DatabaseException
	{
		SingleResult result = null;
		
		OID match = findOidForResultPattern(pattern, oids);
		if(pattern.getType().equals(Type.BINDER))
		{
			if (pattern.size() == 1) {
		    	// Added by TK for Volatile Index
				Result internalResult = buildResult(pattern.firstElement(), oids, interpreter);
				result = new BinderResult(pattern.getAlias(), internalResult);
			} else {
				ResultPattern copy = (ResultPattern)pattern.clone();
				copy.setAlias(null);
				if(!pattern.getDeref().equals(Deref.NONE))
					copy.setType(Type.VALUE);
				else
					copy.setType(Type.REF);
				Result internalResult = buildResult(copy, oids, interpreter);
				result = new BinderResult(pattern.getAlias(), internalResult);
			}
		}
		else if(pattern.getType().equals(Type.REF))
			result = new ReferenceResult(match);
		else if(pattern.getType().equals(Type.STRUCT))
		{
			result = new StructResult();
			for(ResultPattern childPattern : pattern)
			{
				try
				{
					((StructResult)result).addField(buildResult(childPattern, oids, interpreter));
				}
				catch(NullPointerException exc) {}//relational null values are not present in the result
			}
		}
		else if(pattern.getType().equals(Type.VALUE))
		{
		    	if(match.countChildren() == 0)
		    	    throw new NullPointerException();
			match = match.getChildAt(0);//_VALUE
			
			if(pattern.getDeref().equals(Deref.BOOLEAN))
				result = new BooleanResult(match.derefBoolean());
			else if(pattern.getDeref().equals(Deref.STRING))
				result = new StringResult(match.derefString());
			else if(pattern.getDeref().equals(Deref.INTEGER))
				result = new IntegerResult(match.derefInt());
			else if(pattern.getDeref().equals(Deref.REAL))
				result = new DoubleResult(match.derefDouble());
			else if(pattern.getDeref().equals(Deref.DATE))
				result = new DateResult(match.derefDate());
		} else if(pattern.getType().equals(Type.VIRTREF))
		{  // Added by TK for Volatile Index
			if (pattern.size() == 1) {
				result = buildResult(pattern.firstElement(), oids, interpreter);
				interpreter.setResult(result);
				interpreter.runCode(
						JulietGen.genCreateVirtualReferenceCode(odra.db.Database.getStore().getNameId(pattern.getAlias())).getByteCode(),
								null);
				result = (SingleResult) interpreter.getResult();
			} else 
				assert false : "unimplemented";
		}
		
		return result;
	}
	
	/**
	 * Searches parent OIDs and its children for the one matching the pattern.
	 * 
	 * @param pattern {@link ResultPattern}
	 * @param parents parent {@link OID}s
	 * @return matching {@link OID}
	 * @throws DatabaseException 
	 */
	private OID findOidForResultPattern(ResultPattern pattern, OID[] parents) throws DatabaseException
	{
		String tableName = pattern.getTableName();
		String columnName = pattern.getColumnName();
		
		for(OID parent : parents)
		{
			if(parent.getObjectName().equals(tableName))
			{
				if(columnName == null)
					return parent;
				else
					for(int j = 0; j < parent.countChildren(); j++)
						if(parent.getChildAt(j).getObjectName().equals(columnName))
							return parent.getChildAt(j).getChildAt(0);
			}
		}
		
		return null;
		
	}
	
	/**
	 * Exetcutes a SQL query.
	 * 
	 * @param query sql query
	 * @param resultPatternString result pattern string
	 * @param interpreter 
	 * @return {@link Result}
	 * @throws Exception
	 */
	public Result executeSqlQuery(String query, String resultPatternString, SBQLInterpreter interpreter) throws Exception
	{
		client.go(Client.Target.QUERY, query);
		
		Object response = client.getReceivedObject();
		OID parent = getStore(Session.getCurrent()).allocate();
		
		ResultPattern resultPattern = ResultPattern.parse(resultPatternString);
		
		BagResult bagResult = new BagResult();
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		if(response instanceof String)
		{
			String xml = (String)response;
			
			Document document = new SAXBuilder().build(new StringInputStream(xml, "UTF-8"));
			List<Element> tuples = document.getRootElement().getChildren();
			for(Element tuple : tuples)
			{
				Document tupleDocument = new Document();
				tupleDocument.addContent((Element)tuple.clone());
				
				String tupleXml = xmlOutputter.outputString(tupleDocument).trim();
				XMLImportFilter importFilter = new XMLImportFilter(module, tupleXml);
				OID[] tupleOids = importFilter.importInto(parent);

				for(OID oid : tupleOids)
					getCurrentResult(Session.getCurrent()).addElement(oid);
				
				SingleResult tupleResult = buildResult(resultPattern, tupleOids, interpreter);
				bagResult.addElement(tupleResult);
			}
			return bagResult;
		}
		else if(response instanceof Integer)
		{
			int result = ((Integer)response).intValue();
			return new IntegerResult(result);
		}
		else if(response instanceof Double)
		{
			double result = ((Double)response).doubleValue();
			return new DoubleResult(result);
		}
		else
			throw new AssertionError("Unsupported (yet) wrapper result type (" + response.getClass().getCanonicalName() + ").");
	}

	/**
	 * Returns the module.
	 * 
	 * @return module
	 */
	public DBModule getModule()
	{
		return module;
	}
	
	/**
	 * Clears temporary result.
	 * 
	 * @param session {@link Session}
	 * @throws DatabaseException
	 */
	public void clearResult(Session session) throws DatabaseException
	{
		Vector<OID> currentResult = getCurrentResult(session);
		for(OID oid : currentResult)
			oid.delete();
		
		currentResult.removeAllElements();
	}
	
	/**
	 * Returns a current result buffer for a session.
	 * 
	 * @param session {@link Session}
	 * @return current result
	 */
	private Vector<OID> getCurrentResult(Session session)
	{
		if(currentResults.containsKey(session.getId()))
			return currentResults.get(session.getId());
		
		currentResults.put(session.getId(), new Vector<OID>());
		return getCurrentResult(session);
	}
	
	/**
	 * Returns a local store.
	 * 
	 * @param session {@link Session}
	 * @return {@link WrapperStore}
	 * @throws DatabaseException 
	 */
	public WrapperStore getStore(Session session) throws DatabaseException
	{
		if(stores.containsKey(session.getId()))
			return stores.get(session.getId());
		
		stores.put(session.getId(), new WrapperStore(10 * 1024 * 1024, 20 * 1024 * 1024, 1024 * 1024));
		return getStore(session);
	}
	
	/**
	 * Removes a store and results associated with a session given.
	 * <br />
	 * The method should be called on session destroy.
	 * 
	 * @param session {@link Session}
	 */
	public void removeSessionData(Session session)
	{
		stores.remove(session.getId());
		currentResults.remove(session.getId());
	}
}