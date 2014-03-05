package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

public class MBInterface extends MBObject {
	public MBInterface(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}

	public void initialize(OID insvarid, OID instypid, int[] superids) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), this.oid, MetaObjectKind.INTERFACE_OBJECT.kindAsInt());
		store.createPointerObject(store.addName(Names.namesstr[Names.VALUE_ID]), this.oid, insvarid);
		store.createPointerObject(store.addName(Names.namesstr[Names.TYPEID_ID]), this.oid, instypid);
		store.createComplexObject(store.addName(Names.namesstr[Names.FLAGS_ID]), this.oid, 0);
		store.createComplexObject(store.addName(Names.namesstr[Names.EXTENDS_ID]), this.oid, 0);

		for(int superid : superids)
			store.createIntegerObject(store.addName(Names.namesstr[Names.SUPER_ID]), getExtendsRef(), superid);
	}

	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.INTERFACE_OBJECT;
	}

	public OID[] getFields() throws DatabaseException {
		MBStruct str = new MBStruct(this.getTypeRef().derefReference());

		return str.getFields();
	}
	
	public OID[] getProcedures() throws DatabaseException {
		MBStruct str = new MBStruct(this.getTypeRef().derefReference());

		return str.getProcedures();
	}
	
	public OID[] getSuperInterfaces() throws DatabaseException {
		return getExtendsRef().derefComplex();
	}

	public int getFieldFlag(OID fieldid) throws DatabaseException {
		int pos = 0;
		
		for (OID oid : getFields()) {
			if (oid.equals(fieldid))
				return getFieldFlag(pos);
			
			pos++;
		}

		throw new DatabaseException("This is not an interface field");
	}

	public int getFieldFlag(String fieldname) throws DatabaseException {
		int pos = 0;

		for (OID oid : getFields()) {
			if (oid.getObjectName().equals(fieldname))
				return getFieldFlag(pos);

			pos++;
		}

		throw new DatabaseException("This is not an interface field");
	}
	
	public int getFieldFlag(int fieldpos) throws DatabaseException {
		return getFlagsRef().derefInt();
	}

	public String getInstanceName() throws DatabaseException {
		return this.getValueRef().derefReference().getObjectName();
	}
	
	public OID addField(String varname, String typename, int mincard, int maxcard, int ref, int flags) throws DatabaseException {
		MBStruct str = new MBStruct(this.getTypeRef().derefReference());
		
		store.createIntegerObject(Names.FLAGS_ID, getFlagsRef(), flags);
		
		return str.createField(varname, mincard, maxcard, typename, 0);
	}
	
	public OID addProcedure(String procname, String typename, int mincard, int maxcard, int ref) throws DatabaseException {		
		MBStruct str = new MBStruct(this.getTypeRef().derefReference());
		
		return str.createProcedure(procname, typename, mincard, maxcard, ref);
	}

	public OID addSuperInterface(int sint) throws DatabaseException {
		return store.createIntegerObject(store.addName(Names.namesstr[Names.SUPER_ID]), getExtendsRef(), sint);
	}
	
	public String dump(String indend) throws DatabaseException {
		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();
		MetaBase metaBase = getMetaBase();

		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ")";

		metastr += " iname " + this.getInstanceName();

		// extends
		OID[] extend = getExtendsRef().derefComplex();
		if (extend.length > 0) {
			metastr += ", extends: ";
			for (int i = 0; i < extend.length; i++) {
				metastr += getMetaBase().getMetaReferenceAt(extend[i].derefInt()).derefString();
				
				if (i < extend.length - 1)
					metastr += ", ";
			}
		}
		metastr += " [interface]\n";

		// variables
		OID[] fields = this.getFields();
		OID[] flags = getFlagsRef().derefComplex();
		
		assert fields.length == flags.length : "unexpected number of fields and flags";

		for (int i = 0; i < fields.length; i++) {
			char[] flarr = { 'C', 'R', 'U', 'D' };

			if ((flags[i].derefInt() & FLAG_CREATABLE) == 0)
				flarr[0] = ' ';

			if ((flags[i].derefInt() & FLAG_RETRIEVABLE) == 0)
				flarr[1] = ' ';

			if ((flags[i].derefInt() & FLAG_UPDATEABLE) == 0)
				flarr[2] = ' ';

			if ((flags[i].derefInt() & FLAG_DELETABLE) == 0)
				flarr[3] = ' ';

			metastr += indend + "\t\t\t\t fld. " + i + " " + new String(flarr) + "\n";
		}

		return metastr;
	}

	/********************************************************************************************************************
	 * access to subobjects representing fields of the procedure
	 */

	private final OID getValueRef() throws DatabaseException {
		return oid.getChildAt(VALUE_POS);
	}
	
	private final OID getTypeRef() throws DatabaseException {
		return oid.getChildAt(TYPE_POS);
	}

	private final OID getFlagsRef() throws DatabaseException {
		return oid.getChildAt(FLAGS_POS);
	}

	private final OID getExtendsRef() throws DatabaseException {
		return oid.getChildAt(EXTENDS_POS);
	}

	protected final static int VALUE_POS = 1;
	protected final static int TYPE_POS = 2;
	protected final static int FLAGS_POS = 3;
	protected final static int EXTENDS_POS = 4;

	public final static int FIELD_COUNT = 5;

	public int FLAG_CREATABLE = 1;
	public int FLAG_RETRIEVABLE = 2;
	public int FLAG_UPDATEABLE = 4;
	public int FLAG_DELETABLE = 8;
}

