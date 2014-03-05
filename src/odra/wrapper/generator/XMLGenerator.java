package odra.wrapper.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import odra.wrapper.model.Table;
import odra.wrapper.sql.result.ColumnResult;
import odra.wrapper.sql.result.Result;
import odra.wrapper.sql.result.Row;
import odra.wrapper.sql.result.TableResult;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * XML generator utility class. 
 * <br />
 * The main application is warppping SQL results to XML. 
 * @author jacenty
 * @version   2008-01-29
 * @since   2007-01-05
 */
public class XMLGenerator
{
	/** tuple element name */
	public static final String TUPLE = "tuple";
	
	/** result */
	private final Result result;
	/** document */
	private final Document doc;
	
	/**
	 * The constructor.
	 * 
	 * @param result result 
	 */
	public XMLGenerator(Result result)
	{
		this.result = result;
		
		doc = new Document();
		createXML();
	}
	
	/**
	 * Writes the XML document to an output stream given.
	 * 
	 * @param outputStream <code>OutputStream</code>
	 * @throws IOException 
	 */
	public void writeXML(OutputStream outputStream) throws IOException
	{
		OutputStreamWriter xmlWriter = new OutputStreamWriter(outputStream, "UTF-8");
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		xmlOutputter.output(doc, xmlWriter);
		xmlWriter.close();
	}
	
	/**
	 * Returns the XML document string.
	 * 
	 * @return XML string
	 */
	public String getXMLString()
	{
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		return xmlOutputter.outputString(doc);
	}
	
	/**
	 * Creates a document;
	 */
	private void createXML()
	{
		Namespace namespace = Namespace.getNamespace(XSDGenerator.DEFAULT_NAMESPACE_PREFIX, XSDGenerator.DEFAULT_NAMESPACE_URI);
		Element resultElement = new Element(result.getDatabase().getName(), namespace);
		doc.setRootElement(resultElement);
		
		for(Row row : result.getRows())
		{
			Element tupleElement = new Element(TUPLE, namespace);
			for(TableResult tableResult : row.getTableResults())
			{
				Table table = tableResult.getTable();
				Element tableElement = new Element(table.getName(), namespace);
				for(ColumnResult columnResult : tableResult.getColumnResults())
				{
					Element columnElement = new Element(columnResult.getColumn().getName(), namespace);
					if(columnResult.getValue() != null)
					{
						columnElement.addContent(String.valueOf(columnResult.getValue()));
						tableElement.addContent(columnElement);
					}
				}
				
				tupleElement.addContent(tableElement);
			}
			resultElement.addContent(tupleElement);
		}
	}
}
