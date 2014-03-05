/**
 * 
 */
package odra.sbql.builder;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBClass;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBView;
import odra.db.schema.OdraClassSchema;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraViewSchema;

/**
 * DatabaseManager
 * manages database objects
 * @author Radek Adamus
 * @since 2008-04-29 last modified: 2008-04-29
 * @version 1.0
 */
public class DatabaseManager {
    DBModule mod;

    /**
     * @param mod
     * @param breakOnConflicts
     */
    public DatabaseManager(DBModule mod) {
	this.mod = mod;
    }

    /**
     * @param pname
     */
    public void deleteDatabaseObject(OID parent, String name)
	    throws OrganizerException {
	try {
	    OID exrtoid = mod.findFirstByName(name, parent);

	    if (exrtoid != null)
		exrtoid.delete();
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}

    }

    /**
     * @param pname
     */
    public void deleteRootDatabaseObject(String name) throws OrganizerException {
	try {
	    deleteDatabaseObject(mod.getDatabaseEntry(), name);
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}

    }

    public OID findDataObjectByName(String name, OID parent)
	    throws OrganizerException {
	try {
	    return mod.findFirstByName(name, parent);
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
    }

    public OID findRootDataObjectByName(String name) throws OrganizerException {
	try {
	    return mod.findFirstByName(name, mod.getDatabaseEntry());
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
    }

    /**
     * @param viewName
     * @param virtualObjectName
     * @param debug
     * @param binary
     * @param constants
     * @param catches
     * @return
     */
    public OID createView(OdraViewSchema svi)
	    throws OrganizerException {

	try {
	    OdraProcedureSchema seed = svi.getSeed();
	    DBView dataView = new DBView(mod.createView(svi.getViewName(), svi.getVirtualObjectName(), seed.getDebug(), seed.getBinary(),
		    seed.getConstants(), seed.getCatches()));
	    
	    initView(dataView, svi);
	    
	    return dataView.getOID(); 
	    
	    
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
    }

    /**
     * @param dataView
     * @param svi
     * @throws DatabaseException 
     */
    private void initView(DBView dataView, OdraViewSchema svi) throws DatabaseException {
	for(OdraProcedureSchema genericProc: svi.getGenericProcedures()){
		createGenericViewOperator(dataView,genericProc);
	    }
	    
	    for(OdraViewSchema subView: svi.getSubViews()){
		createSubViews(dataView, subView);
	    }
	
    }

    /**
     * @param dataView
     * @param subView
     * @throws DatabaseException 
     */
    private void createSubViews(DBView dataView, OdraViewSchema viewInfo) throws DatabaseException {
	OdraProcedureSchema seedProc = viewInfo.getSeed();
	DBView dbsview = new DBView(dataView.createSubView(viewInfo.getViewName(), viewInfo.getVirtualObjectName(), seedProc.getDebug(), seedProc.getBinary(), seedProc.getConstants(), seedProc.getCatches()));
	initView(dbsview, viewInfo);
	
    }

    /**
     * @param dataView
     * @param genericProc
     * @throws DatabaseException 
     */
    private void createGenericViewOperator(DBView dataView,
	    OdraProcedureSchema genericProc) throws DatabaseException {
	dataView.createGenericProcedure(genericProc.getPname(), genericProc.getDebug(), genericProc.getBinary(), genericProc.getConstants(), genericProc.getCatches());
	
    }

    /**
     * @param name
     * @param i
     * @param instanceName
     * @return
     */
    public OID createClass(OdraClassSchema sci) throws OrganizerException {
	DBClass dbclass;
	String[] superclasses = sci.getSuperClassesNames();
	try {
	    if (sci.getInstanceName().compareTo(
		    OdraClassSchema.NO_INVARIANT_NAME) == 0) {

		dbclass = new DBClass(mod.createClass(sci.getName(), 0));
	    } else {

		dbclass = new DBClass(mod.createClass(sci.getName(), 0, sci
			.getInstanceName()));
	    }

	    for (OdraProcedureSchema methinfo : sci.getMethods()) {
		this.createClassMethod(dbclass, methinfo);
	    }

	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
	return dbclass.getOID();
    }

    private OID createClassMethod(DBClass dbclass, OdraProcedureSchema methinfo)
	    throws OrganizerException {
	try {
	    return dbclass.createMethod(methinfo.getPname(), methinfo
		    .getDebug(), methinfo.getBinary(), methinfo.getConstants(),
		    methinfo.getCatches());
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
    }

    /**
     * @param sprocInfo
     */
    public OID createProcedure(OdraProcedureSchema pi)
	    throws OrganizerException {
	try {
	    return mod.createProcedure(pi.getPname(), pi.getDebug(), pi
		    .getBinary(), pi.getConstants(), pi.getCatches());
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}

    }
    
    
    /**
     * @param name
     * @param field buf
     */
	public OID createEnum(String name, int i)
	 throws OrganizerException {
		try {
		    return mod.createEnum(name,i);
		} catch (DatabaseException e) {
		    throw new OrganizerException(e);
		}

	}

	
}
