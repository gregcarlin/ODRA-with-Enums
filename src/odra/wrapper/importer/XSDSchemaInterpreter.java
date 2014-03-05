package odra.wrapper.importer;

import odra.db.objects.data.DBModule;
import odra.wrapper.model.Name;

/**
 * {@link odra.filters.XSD.XSDSchemaInterpreter} local specialization.
 * 
 * @author jacenty
 * @version   2007-07-23
 * @since   2007-02-28
 */
public class XSDSchemaInterpreter extends odra.filters.XSD.XSDSchemaInterpreter
{
	public XSDSchemaInterpreter(DBModule module)
	{
		super(module, true, false, true);
	}

	@Override
	protected String determineName(String nameAttribValue)
	{
		return Name.r2o(super.determineName(nameAttribValue));
	}
}
