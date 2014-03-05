/**
 * 
 */
package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;

/**
 * MBVirtualVariable
 * @author Radek Adamus
 *@since 2007-11-15
 *last modified: 2007-11-15
 *@version 1.0
 */
public class MBVirtualVariable extends MBVariable {

    /**
     * @param oid
     * @throws DatabaseException
     */
    public MBVirtualVariable(OID oid) throws DatabaseException {
	super(oid);
	
	
    }
    
    /* (non-Javadoc)
     * @see odra.db.objects.meta.MBVariable#isValid()
     */
    @Override
    public boolean isValid() throws DatabaseException {
	return getObjectKind() == MetaObjectKind.VIRTUAL_VARIABLE_OBJECT;
    }

    /**
	 * @return an oid of the metabase object that defines the virtual object (currently only a MBView) 
	 * @throws DatabaseException
	 */
	public final OID getView() throws DatabaseException {
		return getViewRef().derefReference();
	}
	
	public void initialize(int typenameid, int mincard, int maxcard, int ref, OID view) throws DatabaseException
	{
	    	store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.VIRTUAL_VARIABLE_OBJECT.kindAsInt());
		store.createIntegerObject(store.addName(Names.namesstr[Names.MIN_CARD_ID]), oid, mincard);
		store.createIntegerObject(store.addName(Names.namesstr[Names.MAX_CARD_ID]), oid, maxcard);
		store.createIntegerObject(store.addName(Names.namesstr[Names.TYPEID_ID]), oid, typenameid);
		store.createIntegerObject(store.addName(Names.namesstr[Names.REFERENCE_ID]), oid, ref);
		store.createIntegerObject(store.addName(Names.namesstr[Names.REVERSEID_ID]), oid, NO_REVERSE_NAME);
		store.createPointerObject(store.addName(Names.namesstr[Names.VIEW_REF_ID]), oid, view);
	}
	
    /* (non-Javadoc)
	 * @see odra.db.objects.meta.MBProcedure#getNestedMetabaseEntry()
	 */
	@Override
	public OID[] getNestedMetabaseEntries() throws DatabaseException {
	    return new OID[] {new MBView(this.getView()).getVirtualFieldsEntry()};
	}
    /*******************************************
	 * access to subobjects representing fields of the procedure
	 **/

	private final OID getViewRef() throws DatabaseException {
		return oid.getChildAt(VIEW_POS);
	}
	
	private final static int VIEW_POS = 6;

	public final static int FIELD_COUNT = 7;

}
