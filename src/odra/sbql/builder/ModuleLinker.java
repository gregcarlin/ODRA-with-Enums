package odra.sbql.builder;

import java.util.HashSet;
import java.util.StringTokenizer;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.sessions.Session;

/**
 * This class is responsible for binding logical references registered in
 * modules. Logical references are used to allow communication between modules.
 * For example, during the process of linking the logical reference 'integer' is
 * bound to the real database object representing the type integer, created in
 * the module system.
 * 
 * @author raist
 */

public class ModuleLinker {

    ModuleLinker() {
    }

    /**
     * Initiates the process of linking.
     * 
     * @param mod
     *                module that should be linked to other modules
     */

    public void linkModule(DBModule mod) throws LinkerException {
	try {
	    linkModule(mod, new HashSet<String>());
	} catch (DatabaseException e) {
	    throw new LinkerException(e);
	}
    }

    /**
     * Links a module. It binds names of import lists and change logical
     * references to oids of objects representing them.
     * 
     * @param module
     *                that should be linked to other modules
     * @param a
     *                hash set keeping track on what modules are being linked
     *                (ensures there are no import cycles)
     */
    protected void linkModule(DBModule mod, HashSet<String> resmods)
	    throws DatabaseException, LinkerException {
	// necessary to get rid of import cycles (module a importing module b
	// importing module a)
	if (resmods.contains(mod.getModuleGlobalName()))
	    return;

	// bind the names
	bindImports(mod);
	bindMetaReferences(mod);

	// recursively link dependendent modules
	resmods.add(mod.getModuleGlobalName());

	for (OID i : mod.getImports()) {
	    DBModule m = Database.getModuleByName(i.derefString());
	    linkModule(m, resmods);
	}

	// set the module as linked
	mod.setModuleLinked(true);
    }

    /**
     * Binds the names of modules imported by a module and stores the oids of
     * the modules for later use.
     * 
     * @param mod
     *                module of which import list should be analyzed
     */
    protected void bindImports(DBModule mod) throws DatabaseException,
	    LinkerException {
	OID[] imports = mod.getImports();

	mod.removeCompiledImports();
	String rootModuleName = determineRootName(mod);
	for (int i = 0; i < imports.length; i++) {
	    String modname = imports[i].derefString();
	    DBModule compMod;
	    try {
		compMod = Database.getModuleByName(modname);
	    } catch (DatabaseException e) {
		try {
		    compMod = Database.getModuleByName(rootModuleName + "."
			    + modname);
		    // fix import name
		    imports[i].updateStringObject(rootModuleName + "."
			    + modname);
		} catch (DatabaseException e1) {
		    throw new LinkerException("Unable to find module '"
			    + modname + "'");
		}
	    }

	    if (mod.getOID().equals(compMod.getOID()))
		throw new LinkerException("Recursive module import of '"
			+ modname + "'");

	    mod.addCompiledImport(compMod.getOID());
	}
    }

    /**
     * @param mod
     * @return
     * @throws DatabaseException
     */
    private String determineRootName(DBModule mod) throws DatabaseException {
	String globalName = mod.getModuleGlobalName();

	StringTokenizer tokenizer = new StringTokenizer(globalName, ".");
	return tokenizer.nextToken();
    }

    /**
     * Binds logical names to objects. Although such names are usually
     * registered when a module references to data stored in other modules, to
     * unify the access methods, also internal module access is accomplished in
     * the same way.
     * 
     * @param mod
     *                module of which logical references should be processed
     */
    private void bindMetaReferences(DBModule mod) throws DatabaseException,
	    LinkerException {
	mod.removeCompiledMetaReferences();

	// get the logical names
	OID[] metarefs = mod.getMetaReferences();

	// objects having names of logical references can belong
	// to the current module or to modules imported by the module
	for (int i = 0; i < metarefs.length; i++) {
	    String name = metarefs[i].derefString();

	    // first search session entry (closer scope than persistent entry)
	    OID found = mod
		    .findFirstByName(name, mod.getSessionMetaDataEntry());
	    if (found == null)
		found = mod.findFirstByName(name, mod.getMetabaseEntry());

	    if (found == null) {
		OID[] imports = mod.getCompiledImports();

		for (int j = 0; j < imports.length; j++) {
		    DBModule impmod = new DBModule(imports[j].derefReference());

		    found = impmod.findFirstByName(name, impmod
			    .getMetabaseEntry());

		    if (found != null)
			break;
		}
	    }

	    if (found != null)
		mod.addCompiledMetaReference(found);
	    else
		throw new LinkerException("Unknown name '" + name + "'");
	}
    }

}
