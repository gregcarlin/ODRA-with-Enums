package odra.db;

import java.util.Hashtable;
import java.util.StringTokenizer;

import odra.db.objects.data.DBModule;
import odra.system.config.ConfigDebug;

/**
 * This class is used to speed up the process of finding modules
 * using their global names. Normally, to find the OID of a module having
 * only its global name, the whole tree of modules must be searched.
 * By caching the oids in a hashtable, we can accelerate subsequent searches.
 * 
 * @author raist
 */

public class ModuleFinder {
	private Hashtable<String, DBModule> hash = new Hashtable(); // cache
	private Object monitor = new Object(); // synchronizator

	/**
	 * Finds a module using its global name.
	 * @param modpath global name of the module
	 * @return module which has been found
	 */
	private DBModule findModule(String modpath) throws DatabaseException {
		if (ConfigDebug.ASSERTS)
			assert modpath != null : "modpath == null";

		if (modpath.equals("system"))
			return Database.getSystemModule();
		if(modpath.endsWith("."))
			throw new DatabaseException("Improper module name '" + modpath + "'");
		StringTokenizer s = new StringTokenizer(modpath, ".");
		String[] tokens = new String[s.countTokens()];

		int i = 0;
		while (s.hasMoreTokens())
			tokens[i++] = s.nextToken();

		DBModule mod = Database.getSystemModule();

		synchronized (monitor) {
			for (i = 0; i < tokens.length; i++) {
				OID modid = mod.getSubmodule(tokens[i]);

				if (modid == null)
					throw new DatabaseException("Cannot find module '" + modpath + "'");

				mod = new DBModule(modid);
			}
			hash.put(modpath, mod);
		}

		return mod;
	}

	/**
	 * Finds a module having the name given as a parameter.
	 * First the cache is checked, then the database.
	 * @param name global name of the module
	 */
	public DBModule getModuleByName(String name) throws DatabaseException {
		synchronized (monitor) {
			DBModule mod = hash.get(name);

			if (mod == null)
				mod = findModule(name);

			return mod;
		}
	}

	/**
	 * Removes the record of a module from the cache
	 * @param name global name of the module
	 */
	public void unregisterModule(String name) {
		synchronized (monitor) {
			hash.remove(name);
		}
	}
}
