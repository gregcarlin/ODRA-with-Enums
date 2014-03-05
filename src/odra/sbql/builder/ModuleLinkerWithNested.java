package odra.sbql.builder;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObjectFactory;

/** Experimental module linker allowing binding 
 * references also to nested meta-objects 
 * ModuleLinkerWithNested
 * @author Radek Adamus
 *last modified: 2007-04-13
 *2007-04-13 fix for reference levels according local variables
 *@version 1.0
 */
public class ModuleLinkerWithNested extends ModuleLinker {

	protected void linkModule(DBModule mod, HashSet<String> resmods) throws DatabaseException, LinkerException {
		// necessary to get rid of import cycles (module a importing module b importing module a)
		if (resmods.contains(mod.getModuleGlobalName()))
			return;
		
		// bind the names
		bindImports(mod);
		
		Hashtable<Integer, String> partiallyLinked = new Hashtable<Integer, String>();
		bindMetaReferences(mod,partiallyLinked);

		// recursively link dependendent modules
		resmods.add(mod.getModuleGlobalName());

		for (OID i : mod.getImports()) {
			DBModule m = Database.getModuleByName(i.derefString());
			linkModule(m, resmods);
		}
 
		// set the module as linked
		mod.setModuleLinked(true);
		
		//we finished the main linkage stage, all meta-references are linked to  
		//root meta-objects but some of them needs additional binding to sub-objects
		//next we bind nested meta-references
		bindNestedMetaReferences(mod, partiallyLinked);
		
		//finally set meta-variables reference level
//		this.fixReferenceLevels(mod.getMetabaseEntry().derefComplex());
	}

	/**
	 * Binds logical names to objects. Although such names are usually registered
	 * when a module references to data stored in other modules,
	 * to unify the access methods, also internal module access is accomplished
	 * in the same way.
	 * @param mod module of which logical references should be processed
	 */
	private void bindMetaReferences(DBModule mod, Hashtable<Integer, String> partiallyLinked) throws DatabaseException, LinkerException {
		mod.removeCompiledMetaReferences(); 
		// get the logical names
		OID[] metarefs = mod.getMetaReferences();
		
		// objects having names of logical references can belong
		// to the current module or to modules imported by the module
		for (int i = 0; i < metarefs.length; i++) {
			String cmpname = metarefs[i].derefString();
			String[] names = cmpname.split("\\.");
			String name = names[0];
			//first search session entry (closer scope than persistent entry)
			OID found = mod.findFirstByName(name, mod.getSessionMetaDataEntry());
			if(found == null)
				found = mod.findFirstByName(name, mod.getMetabaseEntry());
			
			if (found == null) {
				OID[] imports = mod.getCompiledImports();

				for (int j = 0; j < imports.length; j++) {
					DBModule impmod = new DBModule(imports[j].derefReference());

					found = impmod.findFirstByName(name, impmod.getMetabaseEntry());

					if (found != null)
						break;
				}
			}

			if (found != null) {
				mod.addCompiledMetaReference(found);
				if(names.length > 1){
					//the process of final binding  name in the path
					//must be defferend to the next stage
					partiallyLinked.put(i, cmpname.substring(name.length()+1));
				}
				
			}
			else
				throw new LinkerException("Unable to link name '" + name + "'");
		}
	
	}
	private void bindNestedMetaReferences(DBModule mod,
			Hashtable<Integer, String> partiallyLinked)
			throws DatabaseException, LinkerException {

		try {
			for (Enumeration<Integer> e = partiallyLinked.keys(); e
					.hasMoreElements();) {
				int refnum = e.nextElement();
				String[] names = partiallyLinked.get(refnum).split("\\.");
				OID ref = mod.getCompiledMetaReferenceAt(refnum);
				OID[] nmbentries = MBObjectFactory.getTypedMBObject(
						ref.derefReference()).getNestedMetabaseEntries();

				OID found = null;
				for (int j = 0; j < names.length; j++) {
					if (nmbentries.length == 0)
						throw new LinkerException("Unable to link name '"
								+ names[0] + "'");
					for(int i = 0; i < nmbentries.length; i++){
					    found = mod.findFirstByName(names[j], nmbentries[i]);
					    if(found != null)
						break;
					}
					if (found != null) {
						nmbentries = MBObjectFactory.getTypedMBObject(found)
								.getNestedMetabaseEntries();
					} else
						break;
				}
				if (found != null)
					ref.updatePointerObject(found);
				else
					throw new LinkerException("Unable to link name '"
							+ names[0] + "'");
			}
		} catch (Exception e) {
			mod.setModuleLinked(false);
			throw new LinkerException(e.getMessage());

		}

	}

//	private void fixReferenceLevels(OID[] moids) throws DatabaseException,
//			LinkerException {
//		int level;
//		for (OID moid : moids) {
//			String name = moid.getObjectName();
//			switch (new MBObject(moid).getObjectKind().kindAsInteger()) {
//			case MetaObjectKind.VARIABLE_OBJECT:
//				MBVariable mbv = new MBVariable(moid);
//				level = this.calculateReferenceLevel(mbv);
//				mbv.setRefIndicator(level);
//				break;
//			case MetaObjectKind.PROCEDURE_OBJECT:
//				MBProcedure mbp = new MBProcedure(moid);
//				level = this.calculateReferenceLevel(mbp);
//				mbp.setRefIndicator(level);
//				fixReferenceLevels(mbp.getArguments());
//				for(OID localBlock: mbp.getLocalBlocksEntries())
//				{
//					fixReferenceLevels(localBlock.derefComplex());
//				}
//				break;
//			case MetaObjectKind.VIRTUAL_OBJECTS_PROCEDURE_OBJECT:
//				MBVirtualObjectsProcedure mbvop = new MBVirtualObjectsProcedure(
//						moid);
//				level = this.calculateReferenceLevel(mbvop);
//				mbvop.setRefIndicator(level);
//				break;
//			case MetaObjectKind.STRUCT_OBJECT:
//				MBStruct mbs = new MBStruct(moid);
//				fixReferenceLevels(mbs.getFields());
//				break;
//			case MetaObjectKind.ANNOTATED_VARIABLE_OBJECT:
//				MBAnnotatedVariableObject mbav = new MBAnnotatedVariableObject(
//						moid);
//				fixReferenceLevels(mbav.getAnnotations());
//				fixReferenceLevels(new OID[] { mbav.getValueRef() });
//				break;
//
//			case MetaObjectKind.VIEW_OBJECT:
//				MBView view = new MBView(moid);
//				fixReferenceLevels(view.getGenProcs());
//				fixReferenceLevels(view.getVirtualFieldsEntry().derefComplex());
//				fixReferenceLevels(view.getSubViewsEntry().derefComplex());
//				break;
//			case MetaObjectKind.CLASS_OBJECT:
//				MBClass mbc = new MBClass(moid);
//				fixReferenceLevels(mbc.getMethods());
//				fixReferenceLevels(new OID[] { mbc.getType() });
//				break;
//			default: // do nothing
//				break;
//			}
//		}
//	}
//
//	private int calculateReferenceLevel(MBVariable mbv)
//			throws DatabaseException {
//		int level = 0;
//		OID type = mbv.getType();
//		while (new MBObject(type).getObjectKind().kindAsInteger() == MetaObjectKind.VARIABLE_OBJECT) {
//			level++;
//			type = new MBVariable(type).getType();
//		}
//		return level;
//	}
//
//	private int calculateReferenceLevel(MBProcedure mbv)
//			throws DatabaseException {
//		int level = 0;
//		boolean end = false;
//		OID type = mbv.getType();
//
//		while (!end) {
//			
//			switch(new MBObject(type).getObjectKind().kindAsInteger())
//			{
//			case MetaObjectKind.VARIABLE_OBJECT:
//				level++;
//				type = new MBVariable(type).getType();
//				break;
//			case MetaObjectKind.VIRTUAL_OBJECTS_PROCEDURE_OBJECT:
//				level++;
//				type = new MBVirtualObjectsProcedure(type).getType();
//				break;
//			default: end = true;
//				break;
//			}
//		}
//		return level;
//	}

}