package odra.sbql.builder;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DBProcedure;
import odra.db.objects.data.DBVirtualObjectsProcedure;
import odra.db.objects.data.DataObjectKind;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBSchema;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MBVirtualVariable;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraVariableSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.builder.classes.ClassOrganizer;
import odra.sbql.builder.views.ViewOrganizer;
import odra.system.config.ConfigDebug;
import odra.transactions.ast.IASTTransactionCapabilities;

/**
 * This class is responsible for the creation of module components. Together
 * with ModuleConstructor it is used to construct modules from source code.
 * 
 * @author raist, radamus, edek (transaction support)
 */

public class ModuleOrganizer {

	private MetabaseManager mborg;

	private DatabaseManager dborg;

	private DBModule newmod;

	public boolean breakOnConflicts;

	/**
	 * Initializes the manager
	 * 
	 * @param mod
	 *            a module of which content is to be managed by an object of
	 *            this class
	 * @param conf
	 *            indicates what should happen on conflicts between the source
	 *            code and database objects
	 */
	public ModuleOrganizer(DBModule mod, boolean confl) {
		newmod = mod;
		breakOnConflicts = confl;
		mborg = new MetabaseManager(mod, breakOnConflicts);
		dborg = new DatabaseManager(mod);
	}

	/**
	 * Change the procedure/method body the signature remains unchanged
	 * 
	 * @param mbproc -
	 *            meta-prcedure to change
	 * @param procInfo -
	 *            schema procedure information (only the ast and locals are
	 *            used)
	 * @throws OrganizerException
	 */
	public void alterProcedureBody(OID mbprocid, OdraProcedureSchema procInfo)
			throws OrganizerException {
		try {

			this.mborg.alterProcedureBody(mbprocid, procInfo);
			this.newmod.setModuleLinked(false);
			this.newmod.setModuleCompiled(false);
		} catch (DatabaseException e) {
			throw new OrganizerException(e);
		}
	}

	/**
	 * Creates a new procedure.
	 * 
	 * @param sprocInfo -
	 *            procedure description see SchemaProcedureInfo
	 * @throws OrganizerException
	 */
	public void createProcedure(OdraProcedureSchema sprocInfo)
			throws OrganizerException {
		if (ConfigDebug.ASSERTS) {
			assert sprocInfo != null : " schema procedure info != null ";
		}
		/*
		 * check if there is already an object of the same name as the name of
		 * the new procedure
		 */
		OID exmtprcoid = mborg.findRootMetaObjectByName(sprocInfo.getPname());

		/*
		 * if such an objects exists, and it's a procedure, check if there is a
		 * conflict between the new and old version.
		 */
		boolean deleteOld = true;
		if (exmtprcoid != null) {
			// TODO schema evolution
			try {
				MBProcedure exmtprc = new MBProcedure(exmtprcoid);
				if (!exmtprc.isValid()) {
					deleteOld = false;
					OID[] exargs = exmtprc.getArguments();
					/**
					 * has the result type changed?
					 */
					if (exmtprc.getMinCard() != sprocInfo.getMincard() || exmtprc.getMaxCard() != sprocInfo.getMaxcard() || exmtprc.getRefIndicator() != sprocInfo.getRefs()
								|| !varTypeStructEquiv(this.mborg.getProcedureTypeName(exmtprc), sprocInfo.getTypeName())) {
						deleteOld = true;
					}
					/**
					 * are the parameters the same?
					 */
					ProcArgument[] nwargs = sprocInfo.getArgs();
					if (exargs.length == nwargs.length) {
						for (int i = 0; i < exargs.length; i++) {
							MBVariable mbvar = new MBVariable(exargs[i]);
	
							if (!mbvar.getName().equals(nwargs[i].getName())
										|| !varTypeStructEquiv(this.mborg.getVariableTypeName(mbvar), nwargs[i].getTypeName())
										|| mbvar.getMaxCard() != nwargs[i].getMaxCard()|| mbvar.getMinCard() != nwargs[i].getMinCard()
										|| mbvar.getRefIndicator() != nwargs[i].getRefs()) {
								deleteOld = true;
								break;
							}
						}
					}
					/*
					 * if the old procedure is ok, just replace the source code (ast) in it
					 */
					if (!deleteOld) {
						mborg.alterProcedureBody(exmtprcoid, sprocInfo);
						return;
					}
				}
			} catch (DatabaseException e) {
				throw new OrganizerException(e);
			}
			if (deleteOld) {
				if (breakOnConflicts) {
					throw new OrganizerException("Module field (procedure) '"
							+ sprocInfo.getPname()
							+ "' is not compatible with an older version");

				}
				dborg.deleteRootDatabaseObject(sprocInfo.getPname());
				mborg.deleteMetabaseRootObject(sprocInfo.getPname());
			}
		}
		/**
		 * create metabase and database objects
		 */
		mborg.createMetaProcedure(sprocInfo);
		dborg.createProcedure(sprocInfo);

	}

	/**
	 * creates view in the module (with subviews) TODO currently throws an
	 * exception when module already have the object with the same name TODO
	 * when adding the view check for conflicts on virtual object name
	 * 
	 * @param viewInfo
	 * @throws OrganizerException
	 */
	public void createView(OdraViewSchema viewInfo) throws OrganizerException {

		ViewOrganizer viewcruder = new ViewOrganizer(this.mborg, this.dborg,
				this.breakOnConflicts);
		viewcruder.createView(viewInfo);
	}

	/**
	 * Declares a new variable by inserting a new record into the metabase.
	 * Definition takes place during module compilation.
	 * 
	 * @param vname
	 *            name of the variable
	 * @param tname
	 *            name of the type
	 * @param mincard
	 *            the minimal cardinality
	 * @param maxcard
	 *            the maximal cardinality
	 * @param ref
	 *            reference indicator
	 */
	public void createVariable(OdraVariableSchema svi)
			throws OrganizerException {
		/*
		 * check if an object of the name vname already exists in the database
		 */
		try {
			OID exmtvaroid = mborg.findRootMetaObjectByName(svi.getName());
			OID nwmtvaroid = mborg.createMetaVariable(svi);

			if (exmtvaroid == null)
				dborg.deleteRootDatabaseObject(svi.getName());
			else {
				MBVariable exmtvar = new MBVariable(exmtvaroid);
				MBVariable nwmtvar = new MBVariable(nwmtvaroid);

				/*
				 * check if we have a conflict between the new source code and
				 * the database
				 */
				if (exmtvar.isValid()
						&& varTypeStructEquiv(mborg
								.getVariableTypeName(exmtvar), mborg
								.getVariableTypeName(nwmtvar)))
					mborg.deleteMetabaseObject(nwmtvar);
				else {
					if (breakOnConflicts)
						throw new OrganizerException("Module field '"
								+ svi.getName()
								+ "' is not compatible with an older version");
					deleteMetabaseObject(exmtvar);
					deleteDatabaseObject(svi.getName());

				}
			}
		} catch (DatabaseException e) {
			throw new OrganizerException(e);
		}
	}

	/**
	 * Declares a new session variable by inserting a new record into the
	 * session metabase. Definition takes place during module compilation.
	 * 
	 * @param vname
	 *            name of the variable
	 * @param tname
	 *            name of the type
	 * @param mincard
	 *            the minimal cardinality
	 * @param maxcard
	 *            the maximal cardinality
	 * @param ref
	 *            reference indicator
	 */
	public void createSessionVariable(String vname, String tname, int mincard,
			int maxcard, int ref, Expression init) throws OrganizerException {

		try {
			OID exmtvaroid = newmod.findFirstByName(vname, newmod
					.getSessionMetaDataEntry());
			OID nwmtvaroid = newmod.createSessionMetaVariable(vname, mincard,
					maxcard, tname, ref);

			if (exmtvaroid != null) {
				MBVariable exmtvar = new MBVariable(exmtvaroid);
				MBVariable nwmtvar = new MBVariable(nwmtvaroid);

				/*
				 * check if we have a conflict between the new source code and
				 * the database
				 */
				if (exmtvar.isValid()
						&& varTypeStructEquiv(mborg
								.getVariableTypeName(exmtvar), mborg
								.getVariableTypeName(nwmtvar)))
					mborg.deleteMetaVariable(nwmtvar);
				else {
					if (breakOnConflicts)
						throw new OrganizerException("Module session field '"
								+ vname
								+ "' is not compatible with an older version");

					mborg.deleteMetaVariable(exmtvar);

				}
			}
		} catch (DatabaseException e) {
			throw new OrganizerException(e);
		}
	}

	/**
	 * Creates a new type definition object.
	 * 
	 * @param name
	 *            name of the new type
	 * @param tname
	 *            name of the base type
	 */
	public void createTypeDef(String name, String tname, boolean distinct)
			throws OrganizerException {
		try {
			/*
			 * check if an object of the name 'name' already exists in the
			 * metabase
			 */
			OID exmttdfoid = mborg.findRootMetaObjectByName(name);

			if (exmttdfoid != null) {
				MBTypeDef exmttdf = new MBTypeDef(exmttdfoid);

				if (exmttdf.isValid()
						&& varTypeStructEquiv(
								mborg.getTypeDefTypeName(exmttdf), tname)) {
					mborg.deleteTypeIfAnonStruct(tname);
					return;
				}

				if (breakOnConflicts)
					throw new OrganizerException("Module field '" + name
							+ "' is not compatible with an older version");

				deleteMetabaseObject(name);
			}

			deleteDatabaseObject(name);

			mborg.createMetaTypeDef(name, tname, distinct);
		} catch (DatabaseException e) {
			throw new OrganizerException(e);
		}
	}

	public void createExternalSchamaDef(String name) throws OrganizerException {
		try {
			/*
			 * check if an object of the name 'name' already exists in the
			 * metabase
			 */
			OID exmtsdfoid = mborg.findRootMetaObjectByName(name);

			if (exmtsdfoid != null) {
				MBSchema exmtsdf = new MBSchema(exmtsdfoid);

				// TODO:
				/*
				 * if (exmtsdf.isValid() && varTypeStructEquiv(mborg
				 * .getTypeDefTypeName(exmttdf), tname)) {
				 * mborg.deleteTypeIfAnonStruct(tname); return; }
				 */

				if (breakOnConflicts)
					throw new OrganizerException("Module field '" + name
							+ "' is not compatible with an older version");

				deleteMetabaseObject(name);
			}

			deleteDatabaseObject(name);

			mborg.createMetaExternalSchemaDef(name);
		} catch (DatabaseException e) {
			throw new OrganizerException(e);
		}
	}

	/**
	 * Deletes an object from the database.
	 * 
	 * @param name
	 *            name of the object
	 */
	public void deleteDatabaseObject(String name) throws OrganizerException {
		dborg.deleteRootDatabaseObject(name);
	}

	/**
	 * Deletes a view from the database.
	 * 
	 * @param name
	 *            name of the object
	 */
	public void deleteDatabaseView(String virtualObjectName)
			throws OrganizerException {
		try {
			OID exrtoid = dborg.findRootDataObjectByName(virtualObjectName);

			DBVirtualObjectsProcedure vop = new DBVirtualObjectsProcedure(
					exrtoid);
			if (exrtoid != null
					&& new DBVirtualObjectsProcedure(exrtoid).isValid()) {
				vop.getView().delete();
				exrtoid.delete();
			}
		} catch (DatabaseException e) {
			throw new OrganizerException(e);
		}

	}

	/**
	 * Deletes a view from the database.
	 * 
	 * @param name
	 *            name of the object
	 */
	public void deleteView(String viewName) throws OrganizerException {
		try {
			OID metaviewid = mborg.findRootMetaObjectByName(viewName);

			if (metaviewid != null) {
				MBView mbview = new MBView(metaviewid);
				if (mbview.isValid()) {
					String virtualObjectName = mbview.getVirtualObject()
							.getObjectName();
					mborg.deleteMetabaseObject(mbview);
					deleteDatabaseView(virtualObjectName);
					newmod.setModuleLinked(false);
					newmod.setModuleCompiled(false);
					return;
				}
				throw new OrganizerException("'" + viewName + "' is not a view");
			}
			throw new OrganizerException("'" + viewName
					+ "' cannot be found in module '"
					+ newmod.getModuleGlobalName() + "'");
		} catch (DatabaseException e) {
			throw new OrganizerException(e);
		}

	}

	/**
	 * Deletes a metabase object using its name.
	 * 
	 * @param name
	 *            name of the metabase object.
	 */
	void deleteMetabaseObject(String name) throws OrganizerException {
		mborg.deleteMetabaseRootObject(name);
	}

	/**
	 * @return the newmod
	 */
	public DBModule getModule() {
		return newmod;
	}

	/**
	 * @param relativePath -
	 *            path to the object relative to the module
	 * @param name -
	 *            name of the searched object
	 * @return oid of the meta object if found, null otherwise
	 * @throws DatabaseException
	 */
	public OID findMetaObject(String[] relativePath, String name)
			throws OrganizerException {
		try {

			if (relativePath.length > 0) {
				OID parent = mborg.getMetaBaseEntry();
				parent = mborg.findMetaObjectByName(relativePath[0], parent);
				if (parent == null)
					throw new OrganizerException("unable to find "
							+ relativePath[0] + " in module "
							+ newmod.getName());
				MBObject obj = new MBObject(parent);
				switch (obj.getObjectKind()) {
				case CLASS_OBJECT:
					return new ClassOrganizer(this.mborg, this.dborg,
							this.breakOnConflicts).findMetaClassInvariant(
							parent, name);

				case VIEW_OBJECT: {
					String[] subPath = new String[relativePath.length - 1];
					for (int i = 1; i < relativePath.length; i++) {
						subPath[i - 1] = relativePath[i];
					}
					return new ViewOrganizer(this.mborg, this.dborg,
							this.breakOnConflicts).findMetaObject(parent,
							subPath, name);
				}
				default:
					return null;
				}
			}
			return mborg.findRootMetaObjectByName(name);
		} catch (DatabaseException e) {
			throw new OrganizerException(e);
		}

	}

	OID findDataObject(String[] relativePath, String name)
			throws DatabaseException {
		OID dataEntry = this.newmod.getDatabaseEntry();
		OID parent = dataEntry;
		if (relativePath.length > 0) {
			parent = newmod.findFirstByName(relativePath[0], parent);
			if (parent == null)
				throw new OrganizerException("unable to find '"
						+ relativePath[0] + "' in module '" + newmod.getName()
						+ "'");
			DBObject obj = new DBObject(parent);
			switch (obj.getObjectKind().getKindAsInt()) {
			case (DataObjectKind.CLASS_OBJECT):
				return new ClassOrganizer(this.mborg, this.dborg,
						this.breakOnConflicts).findDataClassInvariant(parent,
						name);

			case (DataObjectKind.VIEW_OBJECT): {
				String[] subPath = new String[relativePath.length - 1];
				for (int i = 1; i < relativePath.length; i++) {
					subPath[i - 1] = relativePath[i];
				}
				return new ViewOrganizer(this.mborg, this.dborg,
						this.breakOnConflicts).findDataObject(parent, subPath,
						name);
			}
			default:
				return null;
			}
		}
		return this.newmod.findFirstByName(name, parent);
	}

	/**
	 * @param mbo
	 */
	public void deleteMetabaseObject(MBObject mbo) {
		mborg.deleteMetabaseObject(mbo);

	}

	/**
	 * Checks if two types are structurally equivalent. The method is used to
	 * help decide whether the old variable is equivalent to the new variable or
	 * not. Structural equivalence is performed only on anonymous structures.
	 * Name type equivalence is used otherwise. This is slightly different from
	 * the comparison performed during type checking.
	 * 
	 * @param tstr1
	 *            name of the first type
	 * @param tstr2
	 *            name of the second type
	 * @return equivalent (true) or not (false)
	 */
	boolean varTypeStructEquiv(String tstr1, String tstr2)
			throws DatabaseException {
		// if we are dealing with anonymous structures, compare them
		// structurally.
		// if we're dealing with other types, just compare their names.
		if (!tstr1.startsWith("$"))
			return tstr1.equals(tstr2);

		if (!tstr2.startsWith("$"))
			return false;

		OID str1oid = mborg.findRootMetaObjectByName(tstr1);
		OID str2oid = mborg.findRootMetaObjectByName(tstr2);

		if (ConfigDebug.ASSERTS)
			assert (str1oid != null || str2oid != null) : "lost one or two structures";

		MBStruct str1 = new MBStruct(str1oid);
		MBStruct str2 = new MBStruct(str2oid);

		if (ConfigDebug.ASSERTS)
			assert str1.isValid() && str2.isValid() : "invalid one or two structures";

		OID[] fields1 = str1.getFields();
		OID[] fields2 = str2.getFields();

		if (fields1.length != fields2.length)
			return false;
		for (int i = 0; i < fields1.length; i++)
			if (!varTypeStructEquiv(mborg.getVariableTypeName(new MBVariable(
					fields1[i])), mborg.getVariableTypeName(new MBVariable(
					fields2[i]))))
				return false;

		return true;

	}

}
