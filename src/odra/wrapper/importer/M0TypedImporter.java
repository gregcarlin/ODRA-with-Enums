package odra.wrapper.importer;

import nu.xom.Element;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBAnnotatedObject;
import odra.db.objects.data.DBModule;
import odra.filters.ShadowObjectException;
import odra.filters.XML.XMLImportFilter;
import odra.wrapper.model.Name;

/**
 * {@link odra.filters.XML.M0TypedImporter} local specialization.
 * 
 * @author jacenty
 * @version   2007-07-23
 * @since   2007-02-24
 */
public class M0TypedImporter extends odra.filters.XML.M0TypedImporter
{
	public M0TypedImporter(DBModule module) throws DatabaseException
	{
		super(module);
	}

	@SuppressWarnings("unused")
	@Override
	public final void createMainAnnotationObject(Element node, OID oid) throws DatabaseException, ShadowObjectException
	{
		//nothing happens
	}

	@SuppressWarnings("unused")
	@Override
	protected final void handleNamespace(OID parent, Element node, DBAnnotatedObject annotation) throws DatabaseException, ShadowObjectException
	{
		//nothing happens
	}

	@Override
	protected String determineName(String elementName)
	{
		String name = super.determineName(elementName);
		if(!name.equals(XMLImportFilter.PCDATA))
			name = Name.r2o(name);
		
		return name;
	}
}
