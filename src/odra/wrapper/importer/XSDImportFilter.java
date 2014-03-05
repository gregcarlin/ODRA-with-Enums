package odra.wrapper.importer;

import java.io.StringReader;

import odra.db.objects.data.DBModule;

/**
 * {@link odra.filters.XSD.XSDImportFilter} local specialization.
 * 
 * @author jacenty
 * @version   2007-02-28
 * @since   2007-01-17
 */
public class XSDImportFilter extends odra.filters.XSD.XSDImportFilter
{
	/**
	 * The constructor.
	 * 
	 * @param module module
	 * @param xsd XSD schema string
	 */
	public XSDImportFilter(DBModule module, String xsd)
	{
		interpreter = new XSDSchemaInterpreter(module);	
		xmlImportFilter = new XMLImportFilter(interpreter, new StringReader(xsd));
	}
}
