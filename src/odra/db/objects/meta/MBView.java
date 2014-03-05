package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.ModuleDumper;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This object provides API for metabase objects describing views.
 * 
 * @author raist
 */

public class MBView extends MBObject {
   /**
    * Initializes a new MBView object
    * 
    * @param oid
    *           OID of an existing structure or an empty complex object
    */
   public MBView(OID oid) throws DatabaseException {
      super(oid);

      if (ConfigDebug.ASSERTS) {
	   assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
      }
   }

   
   /**
    * Initializes mbview's structure by creating some system-level sub-objects
    * @param vobj - oid of the virtual variable object
    * @param votypenameid - virtual object type name id
    * @param mincard - minimal cardinality
    * @param maxcard - maximal cardinality
    * @param seedtypeid - seed (virtual references generator) procedure type name id
    * @param ref - reference indicator
    * @param seedast - seed abstract syntax tree
    * @throws DatabaseException
    */
   public void initialize(OID vobj,  int mincard, int maxcard, int ref, int seedtypeid, byte[] seedast) throws DatabaseException {
      assert vobj != null & seedast != null;
       store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.VIEW_OBJECT.kindAsInt());
      store.createComplexObject(store.addName(Names.namesstr[Names.GENPROCS_ID]), oid, GENERIC_PROC_COUNT);
      store.createComplexObject(store.addName(Names.namesstr[Names.VIRTFLDS_ID]), oid, 0);
      store.createComplexObject(store.addName(Names.namesstr[Names.FIELDS_ID]), oid, 0);
      store.createComplexObject(store.addName(Names.namesstr[Names.SUBVIEWS_ID]), oid, 0);
      store.createPointerObject(store.addName(Names.namesstr[Names.VIRTOBJREF_ID]), oid, vobj);
      this.createSeed(mincard, maxcard, seedtypeid, ref, 0, seedast);

   }

   /**
    * @return true if the oid represents a valid view
    */
   public boolean isValid() throws DatabaseException {
      return getObjectKind() == MetaObjectKind.VIEW_OBJECT;
   }

   /**
    * Creates a new subview declaration
    * 
    * @param name
    *           of the new view
    * @return OID of the new MBView object
    */
   OID createSubView(String viewname, String vobjname, String votypename,    int mincard,
            int maxcard, int refind, String seedtypename, byte[] seedast) throws DatabaseException {
      MetaBase metaBase = getMetaBase();
      
      OID viewid = store.createComplexObject(store.addName(viewname), getSubviewsRef(), MBView.FIELD_COUNT);
      OID vobjid = store.createComplexObject(store.addName(vobjname), getVirtFieldsRef(),
               MBVirtualVariable.FIELD_COUNT);

     // int viewnameid = metaBase.addMetaReference(viewname);
      int votypenameid = metaBase.addMetaReference(votypename);
      int seedtypenameid = metaBase.addMetaReference(seedtypename);
      new MBView(viewid).initialize(vobjid,  mincard, maxcard, refind, seedtypenameid, seedast);
      new MBVirtualVariable(vobjid).initialize(votypenameid, mincard, maxcard, refind, viewid);

      return viewid;
   }

   /**
    * Creates a new procedure declaration inside the view
    * 
    * @return OID of the new MBProcedure object
    */
   OID createProcedureField(String name, int mincard, int maxcard, String type, int ref, int argbuf,
            byte[] ast) throws DatabaseException {
      MetaBase metaBase = getMetaBase();

      int typeid = metaBase.addMetaReference(type);

      OID strid = store.createComplexObject(store.addName(name), getViewFieldsRef(), MBProcedure.FIELD_COUNT);
      new MBProcedure(strid).initialize(typeid, mincard, maxcard, ref, argbuf, ast);

      return strid;
   }

   /**
    * Creates a new variable declaration inside the view
    * 
    * @return OID of the MBVariable object
    */
   OID createVariableField(String name, int mincard, int maxcard, String type, int ref)
            throws DatabaseException {
      MetaBase metaBase = getMetaBase();

      int typeid = metaBase.addMetaReference(type);

      OID strid = store.createComplexObject(store.addName(name), getViewFieldsRef(), MBVariable.FIELD_COUNT);
      new MBVariable(strid).initialize(typeid, mincard, maxcard, ref);

      return strid;
   }

   /**
    * Creates a new "generic procedure", e.g. "on create", "on delete" and so on
    * 
    * @return OID of the new MBProcedure object
    */
   public OID createGenericProcedure(String name, int mincard, int maxcard, String type, int ref, int argbuf,
            byte[] ast) throws DatabaseException {
      MetaBase metaBase = getMetaBase();

      int typeid = metaBase.addMetaReference(type);

      OID strid = store.createComplexObject(store.addName(name), getGenProcsRef(), MBProcedure.FIELD_COUNT);
      new MBProcedure(strid).initialize(typeid, mincard, maxcard, ref, argbuf, ast);

      return strid;
   }

   /**
    * @return OID of the object being root of view virtual fields (virtual
    *         objects state)
    */
   public OID getVirtualFieldsEntry() throws DatabaseException {
      return getVirtFieldsRef();
   }

   /**
    * @return OID of the object being root of view fields (view state)
    */
   public OID getViewFieldsEntry() throws DatabaseException {
      return getViewFieldsRef();
   }

   /**
    * @return OID of the object being root of all generic procedures of the view
    */
   public OID getGenProcsEntry() throws DatabaseException {
      return getGenProcsRef();
   }

   /**
    * @return OIDs of the objects of all generic procedures of the view
    */
   public OID[] getGenProcs() throws DatabaseException {
      return getGenProcsRef().derefComplex();
   }
   /**
    * @return OIDs of the objects of all generic procedures of the view
    */
   public OID getSeedProc() throws DatabaseException {
      return getSeedRef();
   }
   public OID getGenericProc(String name) throws DatabaseException {
      OID[] procs = this.getGenProcsRef().derefComplex();
      for (OID proc : procs) {
         if (name.compareTo(proc.getObjectName()) == 0)
            return proc;
      }
      return null;
   }

   /**
    * @return OID of the object being root of all view's sub-views
    */
   public OID getSubViewsEntry() throws DatabaseException {
      return getSubviewsRef();
   }

   /**
    * @return OID of the virtual variable 
    */
   public OID getVirtualObject() throws DatabaseException {
      return getVirtualObjectRef().derefReference();
   }
   /**
    * @return name of the virtual variable 
    */
   public String getVirtualObjectName() throws DatabaseException {
      return getVirtualObjectRef().derefReference().getObjectName();
   }
   
   /**
    * @return OID of the virtual variable type 
    */
   public OID getVirtualObjectType() throws DatabaseException {
      return new MBVirtualVariable(getVirtualObjectRef().derefReference()).getType();
   }
   /*
    * (non-Javadoc)
    * 
    * @see odra.db.objects.meta.MBObject#getNestedMetabaseEntry()
    */
   @Override
   public OID[] getNestedMetabaseEntries() throws DatabaseException {

      return new OID[] { this.getViewFieldsEntry() };
   }

   /****************************************************************************
    * debugging
    */

   public String dump(String indend) throws DatabaseException {
      DBModule module = getModule();

      int mobjnameid = oid.getObjectNameId();
      String mobjname = oid.getObjectName();

      String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname
               + ") [view]\n";

      ModuleDumper dumper = new ModuleDumper(module);

      OID[] prcs = getGenProcsRef().derefComplex();
      for (int j = 0; j < prcs.length; j++)
         metastr += dumper.dumpMetadata(module, prcs[j], indend + " gpr. " + j + ": ");

      OID[] virtflds = getVirtFieldsRef().derefComplex();
      for (int j = 0; j < virtflds.length; j++)
         metastr += dumper.dumpMetadata(module, virtflds[j], indend + " virt fld. " + j + ": ");

      OID[] viewflds = getViewFieldsRef().derefComplex();
      for (int j = 0; j < viewflds.length; j++)
         metastr += dumper.dumpMetadata(module, viewflds[j], indend + " view fld. " + j + ": ");

      OID[] sbvs = getSubviewsRef().derefComplex();
      for (int j = 0; j < sbvs.length; j++)
         metastr += dumper.dumpMetadata(module, sbvs[j], indend + " sbv. " + j + ": ");

      return metastr;
   }

   
   /**
    * Creates a new "generic procedure", e.g. "on create", "on delete" and so on
    * 
    * @return OID of the new MBProcedure object
    */
   private OID createSeed( int mincard, int maxcard, int seedtypeid, int ref, int argbuf,
            byte[] ast) throws DatabaseException {
      OID strid = store.createComplexObject(store.addName(Names.namesstr[Names.SEED_ID]), this.oid, MBProcedure.FIELD_COUNT);
      new MBProcedure(strid).initialize(seedtypeid, mincard, maxcard, ref, argbuf, ast);

      return strid;
   }
   /****************************************************************************
    * access to subobjects describing the declaration
    */

   private final OID getGenProcsRef() throws DatabaseException {
      return oid.getChildAt(GENPROCS_POS);
   }

   private final OID getVirtFieldsRef() throws DatabaseException {
      return oid.getChildAt(VIRTUAL_FIELDS_POS);
   }

   private final OID getViewFieldsRef() throws DatabaseException {
      return oid.getChildAt(VIEW_FIELDS_POS);
   }

   private final OID getSubviewsRef() throws DatabaseException {
      return oid.getChildAt(SUBVIEWS_POS);
   }

   private final OID getVirtualObjectRef() throws DatabaseException {
      return oid.getChildAt(VIRTUAL_OBJECT_POS);
   }
   
   private final OID getSeedRef() throws DatabaseException {
       return oid.getChildAt(SEED_POS);
    }
//   private OID getTypeNameIdRef() throws DatabaseException
//   {
//		return oid.getChildAt(TYPENAMEID_POS);
//   }

   private final static int GENPROCS_POS = 1;

   private final static int VIRTUAL_FIELDS_POS = 2;

   private final static int VIEW_FIELDS_POS = 3;

   private final static int SUBVIEWS_POS = 4;

   private final static int VIRTUAL_OBJECT_POS = 5;
   
   private final static int SEED_POS = 6;
   
   //private final static int TYPENAMEID_POS = 7;

   public final static int FIELD_COUNT = 7;
   
   private final static int GENERIC_PROC_COUNT = 5; 
   
}