package odra.sbql.builder.views;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.data.DBView;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraVariableSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.sbql.builder.DatabaseManager;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.builder.OrganizerException;

/**
 * ViewOrganizer
 * 
 * @author Radek Adamus last modified: 2006-11-20
 * @version 1.0
 */
public class ViewOrganizer {
    private MetabaseManager mborg;

    private DatabaseManager dborg;

    // private DBModule mod;
    private boolean breakOnConflicts;

    /**
     * @param module
     *                organizer for the module in which the view will be placed
     */
    public ViewOrganizer(MetabaseManager morg, DatabaseManager dorg,
	    boolean breakOnConflicts) {
	this.mborg = morg;
	this.dborg = dorg;
	this.breakOnConflicts = breakOnConflicts;
    }

    /**
     * creates view in the module (with subviews) TODO currently thows an
     * exception when module already have the object with the same name TODO
     * when adding the view check for conflicts on virtual object name
     * 
     * @param viewInfo
     * @throws DatabaseException
     * @throws OrganizerException
     */
    /**
     * @param viewInfo
     * @throws DatabaseException
     * @throws OrganizerException
     */
    public void createView(OdraViewSchema viewInfo) throws OrganizerException {
	try {
	    // check if there is already an object of the same name as the name
	    // of the new view
	    // and the virtual object the same as the name of the new vitual
	    // object
	    OID exmtviewoid = mborg.findRootMetaObjectByName(viewInfo
		    .getViewName());
	    OID exmtvirtobjoid = mborg.findRootMetaObjectByName(viewInfo
		    .getVirtualObjectName());

	    // if such an objects exists, and it's a view,
	    // check if there is a conflict between the new and old version.
	    if (exmtviewoid != null) {
		boolean deleteOld = true;
		// throw new OrganizerException("Module field '" +
		// viewInfo.vName + "' cannot be created");
		MBView oldmbview = new MBView(exmtviewoid);
		if (oldmbview.isValid()) {
		    throw new OrganizerException("View '"
			    + viewInfo.getViewName() + "' already exists");

		} else {
		    if (this.breakOnConflicts) {
			throw new OrganizerException("Module field '"
				+ viewInfo.getViewName()
				+ "' cannot be created");
		    }
		    mborg.deleteMetabaseObject(oldmbview);
		    dborg.deleteRootDatabaseObject(viewInfo.getViewName());
		    dborg.deleteRootDatabaseObject(viewInfo
			    .getVirtualObjectName());

		}

	    }

	    if (exmtvirtobjoid != null) {
		throw new OrganizerException("Module field '"
			+ viewInfo.getVirtualObjectName()
			+ "' cannot be created");

	    }

	    OID nwmtviewoid = mborg.createMetaView(viewInfo);

	    new DBView(dborg.createView(viewInfo));

	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}

    }

    public OID findMetaObject(OID viewid, String[] relativePath, String name)
	    throws OrganizerException {
	try {
	    MBView view = new MBView(viewid);
	    assert view.isValid() : "not a meta view";
	    if (relativePath.length > 0) {
		OID subviewid = mborg.findMetaObjectByName(relativePath[0],
			view.getSubViewsEntry());
		if (subviewid == null)
		    throw new OrganizerException("unable to find subview "
			    + relativePath[0] + " in " + view.getName());

		String[] subPath = new String[relativePath.length - 1];
		for (int i = 1; i < relativePath.length; i++) {
		    subPath[i - 1] = relativePath[i];
		}
		return this.findMetaObject(subviewid, subPath, name);

	    }
	    return this.findMetaObject(viewid, name);
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}

    }

    private OID findMetaObject(OID viewid, String name)
	    throws DatabaseException, OrganizerException {
	MBView view = new MBView(viewid);
	// search generic procedures
	OID found = view.getGenericProc(name);
	if (found == null) {
	    // search view fields
	    found = mborg.findMetaObjectByName(name, view.getViewFieldsEntry());
	}
	if (found == null) {
	    // search virtual fields
	    found = mborg.findMetaObjectByName(name, view
		    .getVirtualFieldsEntry());
	}
	if (found == null) {
	    // check virtual object
	    OID vo = view.getVirtualObject();
	    if (vo.getObjectName().equals(name))
		return vo;
	}
	return found;
    }

    public OID findDataObject(OID viewid, String[] relativePath, String name)
	    throws OrganizerException {
	try {
	    DBView view = new DBView(viewid);
	    assert view.isValid() : "not a meta view";
	    if (relativePath.length > 0) {
		OID subviewid = view.getSubViewByName(relativePath[0]);
		if (subviewid == null)
		    throw new OrganizerException("unable to find subview '"
			    + relativePath[0] + "' in '" + view.getName() + "'");

		String[] subPath = new String[relativePath.length - 1];
		for (int i = 1; i < relativePath.length; i++) {
		    subPath[i - 1] = relativePath[i];
		}
		return this.findDataObject(subviewid, subPath, name);

	    }
	    return this.findDataObject(viewid, name);
	} catch (DatabaseException e) {
	    throw new OrganizerException(e);
	}
    }

    private OID findDataObject(OID viewid, String name)
	    throws DatabaseException, OrganizerException {
	DBView view = new DBView(viewid);
	// search generic procedures
	OID found = view.getGenericProcByName(name);
	if (found == null) {
	    // search view fields
	    found = dborg.findDataObjectByName(name, view.getViewFieldsEntry());
	}
	if (found == null) {
	    // search virtual fields
	    found = dborg.findDataObjectByName(name, view
		    .getVirtualFieldsEntry());
	}
	if (found == null) {
	    // check virtual object
	    OID vo = view.getVirtualObject();
	    if (vo.getObjectName().equals(name))
		return vo;
	}
	return found;
    }
}
