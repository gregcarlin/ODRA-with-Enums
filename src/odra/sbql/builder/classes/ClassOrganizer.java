package odra.sbql.builder.classes;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraClassSchema;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.ProcArgument;
import odra.sbql.builder.DatabaseManager;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.builder.OrganizerException;

/**
 * ClassOrganizer
 * 
 * @author Radek Adamus last modified: 2007-02-09
 * @version 1.0
 */
public class ClassOrganizer {
    private MetabaseManager mborg;

    private DatabaseManager dborg;


    boolean breakOnConflicts;

    public ClassOrganizer(MetabaseManager morg, DatabaseManager dorg,
	    boolean breakOnConflicts) {
	this.mborg = morg;
	this.dborg = dorg;
	this.breakOnConflicts = breakOnConflicts;
    }
    public ClassOrganizer(DBModule mod, boolean breakOnConflicts){
	this.mborg = new MetabaseManager(mod, breakOnConflicts);
	this.dborg = new DatabaseManager(mod);
	this.breakOnConflicts = breakOnConflicts;
    }
    
    public ClassOrganizer(DBModule mod){
	this(mod,true);
    }
    
    /**
     * Create class in database and metabase TODO currently always new class is
     * created the existing class with the same name is deleted
     * 
     * @param sci
     * @throws DatabaseException
     * @throws OrganizerException
     */
    public void createClass(OdraClassSchema sci) throws OrganizerException {
	try {
	    OID mbclassid = mborg.findRootMetaObjectByName(sci.getName());
	    MBClass mbclass;
	    DBClass dbclass;
	    if (mbclassid != null) {

		if (new MBClass(mbclassid).isValid()) {
		    // TODO conflict decide unfinished
		    // currently we always delete existing class
		    mborg.deleteMetabaseObject(new MBObject(mbclassid));
		    dborg.deleteRootDatabaseObject(sci.getName());
		} else {
		    throw new OrganizerException(
			    " Field '"
				    + sci.getName()
				    + "' already exists and is not compatible with a new version");
		}
	    }

	   
	    // create metabase class object
	    mborg.createMetaClass(sci);
	    // create database class object
	    dborg.createClass(sci);
	    	
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}

    }

   

    /**
     * @param metaclassid -
     *                meta-class oid
     * @param name -
     *                name of the searched invariant
     * @return oid of the meta-object representing class invariant if found,
     *         <br>
     *         null otherwise
     * @throws DatabaseException
     * @throws OrganizerException
     */
    public OID findMetaClassInvariant(OID metaclassid, String name)
	    throws OrganizerException {
	// search for a method
	OID found;
	try {
	    MBClass cls = new MBClass(metaclassid);
	    assert cls.isValid() : "not a meta class";
	    found = mborg.findMetaObjectByName(name, cls.getMethodsEntry());
	    if (found == null) {
		// search for a variable field
		found = new MBStruct(cls.getType()).findFieldByName(name);
	    }
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
	return found;
    }

    public OID findDataClassInvariant(OID dataclassid, String name)
	    throws OrganizerException {
	try {
	    DBClass cls = new DBClass(dataclassid);
	    assert cls.isValid() : "not a class";
	    return dborg.findDataObjectByName(name, cls.getMethodsEntry());
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}

    }
}
