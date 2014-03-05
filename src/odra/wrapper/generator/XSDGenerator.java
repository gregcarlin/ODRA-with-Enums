package odra.wrapper.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import odra.wrapper.model.Column;
import odra.wrapper.model.Database;
import odra.wrapper.model.Table;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * XSD generator utility class.  
 * <br />
 * The main application is creating a metabase description. 
 * @author jacenty
 * @version   2007-07-23
 * @since   2006-12-13
 */
public class XSDGenerator
{
	private enum SchemaElement
	{
		ELEMENT("element"),
		ATTRIBUTE("attribute"),
		COMPLEX_TYPE("complexType"),
		ALL("all");
		
		private final String name;
		
		private SchemaElement(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
	}
	
	/** namespace prefic */
	public static final String NAMESPACE_PREFIX = "xsd";
	/** namespace URI */
	public static final String NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";
	/** default namespace URI */
	public static final String DEFAULT_NAMESPACE_URI = "http://jacenty.kis.p.lodz.pl";
	/** default namespace prefix */
	public static final String DEFAULT_NAMESPACE_PREFIX = "sql";
	/** target namespace URI */
	public static final String TARGET_NAMESPACE_URI = "http://jacenty.kis.p.lodz.pl";
	/** element form */
	public static final String ELEMENT_FORM_DEFAULT = "qualified";
	
	/** database */
	private final Database database;
	/** document */
	private final Document doc;

	
	/**
	 * The constructor.
	 * 
	 * @param database database 
	 */
	public XSDGenerator(Database database)
	{
		this.database = database;
		
		doc = new Document();
		createSchema();
	}
	
	/**
	 * Writes the schema to an output stream given.
	 * 
	 * @param outputStream <code>OutputStream</code>
	 * @throws IOException 
	 */
	public void writeSchema(OutputStream outputStream) throws IOException
	{
		OutputStreamWriter xmlWriter = new OutputStreamWriter(outputStream, "UTF-8");
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		xmlOutputter.output(doc, xmlWriter);
		xmlWriter.close();
	}
	
	/**
	 * Returns a schema string.
	 * 
	 * @return schema string
	 */
	public String getSchemaString()
	{
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		return xmlOutputter.outputString(doc);
	}
	
	/**
	 * Creates a schema;
	 */
	private void createSchema()
	{
		Element rootElement = new Element("schema");
		doc.setRootElement(rootElement);
		
		rootElement.setNamespace(Namespace.getNamespace(NAMESPACE_PREFIX, NAMESPACE_URI));
		rootElement.setAttribute("targetNamespace", TARGET_NAMESPACE_URI);
		rootElement.setAttribute("elementFormDefault", ELEMENT_FORM_DEFAULT);
		rootElement.addNamespaceDeclaration(Namespace.getNamespace(DEFAULT_NAMESPACE_PREFIX, DEFAULT_NAMESPACE_URI));

		Element databaseElement = createElement(
			SchemaElement.ELEMENT,
			new String[][] {
				{"name", database.getName()}
			});
		Element databaseComplexTypeElement = createElement(
			SchemaElement.COMPLEX_TYPE,
			null);
		Element databaseAllElement = createElement(
			SchemaElement.ALL,
			new String[][] {
				{"minOccurs", "0"}
		});
		databaseComplexTypeElement.addContent(databaseAllElement);
		databaseElement.addContent(databaseComplexTypeElement);
		rootElement.addContent(databaseElement);
		
		Hashtable<String, Table> tables = database.getTables();
		Enumeration<String> tableNames = tables.keys();
		while(tableNames.hasMoreElements())
		{
			String tableName = tableNames.nextElement();
			Table table = tables.get(tableName);
			Element tableElement = createElement(
				SchemaElement.ELEMENT,
				new String[][] {
					{"name", table.getName()},
					{"minOccurs", "0"}
			});
			
			Element tableComplexTypeElement = createElement(
				SchemaElement.COMPLEX_TYPE,
				null);
			Element tableAllElement = createElement(
				SchemaElement.ALL,
				new String[][] {
					{"minOccurs", "0"}
			});
			tableComplexTypeElement.addContent(tableAllElement);
			tableElement.addContent(tableComplexTypeElement);
			databaseAllElement.addContent(tableElement);
			
			Hashtable<String, Column> columns = table.getColumns();
			Enumeration<String> columnNames = columns.keys();
			while(columnNames.hasMoreElements())
			{
				String columnName = columnNames.nextElement();
				Column column = columns.get(columnName);
				Element columnElement = createElement(
					SchemaElement.ELEMENT,
					new String[][] {
						{"name", column.getName()},
						{"type", column.getXsdType()},
						{"minOccurs", "0"},
						{"maxOccurs", "1"}
				});
				tableAllElement.addContent(columnElement);
			}
		}
	}
	
	/**
	 * Creates a schema element.
	 * 
	 * @param name element name (element|attribute)
	 * @param attrs attributes (pairs name:value)
	 * @return element
	 */
	private Element createElement(SchemaElement schemaElement, String[][] attrs)
	{
		Element element = new Element(schemaElement.getName(), NAMESPACE_PREFIX, NAMESPACE_URI);
		
		if(attrs != null)
			for(int i = 0; i < attrs.length; i++)
				if(attrs[i] != null)
					element.setAttribute(attrs[i][0], attrs[i][1]);
		
		return element;
	}
}
