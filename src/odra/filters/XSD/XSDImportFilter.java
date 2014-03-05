package odra.filters.XSD;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.EnumSet;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.ModuleDumper;
import odra.filters.DataImporter;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;
import odra.filters.XML.M0AnnotatedImporter;
import odra.filters.XML.M0DefaultImporter;
import odra.filters.XML.XMLImportFilter;
import odra.filters.XML.XMLNodeImporter;
import odra.filters.XML.XMLTransformer;
import odra.filters.XML.XMLImportFilter.XMLImportParameter;
import odra.filters.XSD.XSDSchemaInterpreter.TypeConstructor;
import odra.system.config.ConfigServer;
import odra.system.log.LogWriter;

public class XSDImportFilter implements DataImporter{
	protected XMLImportFilter xmlImportFilter;
	protected XSDSchemaInterpreter interpreter;

	public XSDImportFilter(){}

	public XSDImportFilter( XSDSchemaInterpreter interpreter, URI baseURI )
	{
		xmlImportFilter = new XMLImportFilter( interpreter, baseURI );
		this.interpreter = interpreter;
	}

	public XSDImportFilter( XSDSchemaInterpreter interpreter, Reader input )
	{
		xmlImportFilter = new XMLImportFilter( interpreter, input );
		this.interpreter = interpreter;		
	}

	public OID[] importSchema(DBModule module) throws DatabaseException, FilterException, ShadowObjectException 
	{
		return importSchema(module, false);
	}
	
	public OID[] importSchema(DBModule module, boolean typeDefsOnly) throws DatabaseException, FilterException, ShadowObjectException 
	{
		System.out.flush();
		xmlImportFilter.importInto(null);
		return fillMetaBase(module, typeDefsOnly);
	}

	public OID[] fillMetaBase(DBModule module, boolean typeDefsOnly) throws DatabaseException, XSDParsingException
	{
		int rootStructCounter=0;
		Vector<OID> result = new Vector<OID>();
		Collection<TypeConstructor> structures = interpreter.getResults();
		for( TypeConstructor type:structures )
			if (type.parent==null)
			{
				OID metaObject =  type.produceMetaObject(module, null, typeDefsOnly);
				if (metaObject!=null)
				{
					result.add( metaObject );
					//System.out.println("\n----- OID :" + metaObject.internalOID() +"\n" + type.name);
					rootStructCounter++;
				}
			}
		//System.out.println("All structures created!.");
		interpreter.rebuildModule(module);
		return result.toArray( new OID[rootStructCounter]);
		
	}

	public enum XSDImportParameter
	{
		M0, noXML, safe;
		
		static public EnumSet<XSDImportParameter> createParamsInfo(String params)
		{
			EnumSet<XSDImportParameter> paramsSet = EnumSet.noneOf(XSDImportParameter.class);
			
				StringTokenizer tokenizer = new StringTokenizer(params, " \n\r\t\f,;");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					try {
					     paramsSet.add( Enum.valueOf(XSDImportParameter.class, token) );
					}
					catch (IllegalArgumentException e) {
						ConfigServer.getLogWriter().getLogger().severe("Unknown import parameter: " + token);
					}
				}
			return paramsSet;
		}
	}

	
	public void importData(String modname, String data, String params) throws FilterException {
		ConfigServer.getLogWriter().getLogger().info("XSD Import plugin started.");
		long start = System.currentTimeMillis();
		ConfigServer.getLogWriter().flushConsole();
		StringReader fileInput = new StringReader( data ); 
		try {
			DBModule mod = Database.getModuleByName(modname);
			XSDSchemaInterpreter nodeInterpreter;
			EnumSet<XSDImportParameter> paramSet = XSDImportParameter.createParamsInfo(params);
			ConfigServer.getLogWriter().getLogger().fine("Import parameters: " + paramSet);
			nodeInterpreter = new XSDSchemaInterpreter(mod, 
									paramSet.contains(XSDImportParameter.M0),
									paramSet.contains(XSDImportParameter.noXML),
									paramSet.contains(XSDImportParameter.safe)	);
				
			XSDImportFilter importFilter = new XSDImportFilter( nodeInterpreter, fileInput );
			OID result[] = importFilter.importSchema(mod);

			float time = (System.currentTimeMillis() - start) / 1000F;
			ConfigServer.getLogWriter().getLogger().info("XSD Import finished (" +nodeInterpreter.getProcessedNodesCount()+" nodes imported in " + time + "s.)");
			ConfigServer.getLogWriter().flushConsole();

		} catch (Exception e) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Error while XSD import plugin job.", e);
			ConfigServer.getLogWriter().flushConsole();
			throw new FilterException("Error while XSD import plugin job.", e);
		}	
	}

	public OID[] importInto(OID parent) throws DatabaseException, FilterException, ShadowObjectException {
		return null;
	}
}
