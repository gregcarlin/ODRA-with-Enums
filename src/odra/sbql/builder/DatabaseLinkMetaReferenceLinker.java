package odra.sbql.builder;

import java.util.Enumeration;
import java.util.Hashtable;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObjectFactory;
import odra.db.objects.meta.MetaBase;

/**
 * DatabaseLinkMetaReferenceLinker
 * linker for meta references in database links
 * link meta-name only inside the link metabase and 
 * system module
 * @author Janek Murlewski
 *@since ...
 *last modified: 2007-11-11
 *@version 1.0
 * modifications Radek Adamus
 * 
 *  @TODO refactorize (duplicated code from superclass) 
 */
public class DatabaseLinkMetaReferenceLinker extends ModuleLinkerWithNested
{

	/**
	 * Binds logical names to objects.
	 * 
	 * @param mb
	 *            MetaBase of which logical references should be processed
	 */
	public void bindMetaBaseReferences(MetaBase mb) throws DatabaseException, LinkerException
	{

		Hashtable<Integer, String> partiallyLinked = new Hashtable<Integer, String>();
		bindMetaReferences(mb, partiallyLinked);

		bindNestedMetaReferences(mb, partiallyLinked);

	}

	private void bindMetaReferences(MetaBase mb, Hashtable<Integer, String> partiallyLinked) throws DatabaseException,
			LinkerException
	{
		mb.removeCompiledMetaReferences();
		// get the logical names
		OID[] metarefs = mb.getMetaReferences();

		// objects having names of logical references can belong
		// to the current module or to modules imported by the module
		for (int i = 0; i < metarefs.length; i++)
		{
			String cmpname = metarefs[i].derefString();
			String[] names = cmpname.split("\\.");
			String name = names[0];
			OID found = null;
			found = mb.findFirstByName(name, mb.getMetabaseEntry());

			if (found == null)
			{
			    DBModule sysmod = Database.getSystemModule();
			
			    found = sysmod.findFirstByName(name, sysmod.getMetabaseEntry());
			    
			}

			if (found != null)
			{
				mb.addCompiledMetaReference(found);
				if (names.length > 1)
				{
					// the process of final binding name in the path
					// must be deffered to the next stage
					partiallyLinked.put(i, cmpname.substring(name.length() + 1));
				}

			}
			else
				throw new LinkerException("Unable to link name '" + name + "'");
		}

	}

	private void bindNestedMetaReferences(MetaBase mb, Hashtable<Integer, String> partiallyLinked) throws DatabaseException,
			LinkerException
	{

		try
		{
			for (Enumeration<Integer> e = partiallyLinked.keys(); e.hasMoreElements();)
			{
				int refnum = e.nextElement();
				String[] names = partiallyLinked.get(refnum).split("\\.");
				OID ref = mb.getCompiledMetaReferenceAt(refnum);
				OID[] nmbentries = MBObjectFactory.getTypedMBObject(ref.derefReference()).getNestedMetabaseEntries();

				OID found = null;
				for (int j = 0; j < names.length; j++)
				{
					if (nmbentries.length == 0)
						throw new LinkerException("Unable to link name '" + names[0] + "'");
					for (int i = 0; i < nmbentries.length; i++)
					{
						found = mb.findFirstByName(names[j], nmbentries[i]);
						if (found != null)
							break;
					}
					if (found != null)
					{
						nmbentries = MBObjectFactory.getTypedMBObject(found).getNestedMetabaseEntries();
					}
					else
						break;
				}
				if (found != null)
					ref.updatePointerObject(found);
				else
					throw new LinkerException("Unable to link name '" + names[0] + "'");
			}
		}
		catch (Exception e)
		{

			throw new LinkerException(e.getMessage());

		}

	}
}
