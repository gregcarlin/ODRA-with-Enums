package odra.wrapper.importer;

import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import nu.xom.Element;
import nu.xom.Node;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.filters.FilterException;
import odra.filters.ShadowObjectException;
import odra.filters.XML.XMLNodeImporter;
import odra.system.config.ConfigServer;
import odra.wrapper.generator.XSDGenerator;

/**
 * {@link odra.filters.XML.XMLImportFilter} local specialization.
 * 
 * @author jacenty
 * @version   2007-02-24
 * @since   2007-01-17
 */
public class XMLImportFilter extends odra.filters.XML.XMLImportFilter
{
	/**
	 * The constructor.
	 * 
	 * @param importer XML importer
	 * @param reader input reader
	 */
	public XMLImportFilter(XMLNodeImporter importer, Reader reader)
	{
		super(importer, reader);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param module module
	 * @param xml XML document string
	 * @throws DatabaseException 
	 */
	public XMLImportFilter(DBModule module, String xml) throws DatabaseException
	{
		this(new M0TypedImporter(module), new StringReader(xml));
	}
	
	@Override
	public OID[] importInto(OID parent) throws DatabaseException, FilterException, ShadowObjectException
	{
		OID result[] = null;
		if(nodes != null)
			throw new FilterException("importInto method may be called only once. Create a new Filter.", null);

		initialize();
		
		Node rootNode = nodes.get(0);
		if(((Element)rootNode).getNamespacePrefix().equals(XSDGenerator.NAMESPACE_PREFIX))//XSD
		{
			for(int i = 0; i < rootNode.getChildCount(); i++)
				if(rootNode.getChild(i) instanceof Element)
				{
					Element complexNode = (Element)rootNode.getChild(i);
					for(int j = 0; j < complexNode.getChildCount(); j++)
						if(complexNode.getChild(j) instanceof Element)
						{
							Element allNode = (Element)complexNode.getChild(j);
							{
								result = new OID[allNode.getChildCount()];
								for(int k = 0; k < allNode.getChildCount(); k++)
									if(allNode.getChild(k) instanceof Element)
										result[i] = importObjectNode(allNode.getChild(k), parent, false);
							}
						}
				}
		}
		else if(((Element)rootNode).getNamespacePrefix().equals(XSDGenerator.DEFAULT_NAMESPACE_PREFIX))//XML
		{
			Vector<OID> tempResult = new Vector<OID>();
			
			for(int i = 0; i < rootNode.getChildCount(); i++)
				if(rootNode.getChild(i) instanceof Element)
					tempResult.addElement(importObjectNode(rootNode.getChild(i), parent, false));

			result = new OID[tempResult.size()];
			tempResult.copyInto(result);
		}
		interpreter.finalizeUnknownIdrefs();

		ConfigServer.getLogWriter().getLogger().finest("Finished parsing of " + input);
		return result;
	}
}