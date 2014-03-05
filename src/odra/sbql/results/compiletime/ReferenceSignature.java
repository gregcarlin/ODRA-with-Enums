package odra.sbql.results.compiletime;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBVirtualVariable;

/**
 * This class implements reference signatures, i.e. abstract references
 * processed by the type checker.
 * 
 * @author raist, stencel
 */

public class ReferenceSignature extends Signature {
	public OID value; // reference to a object declaration in a metabase

	public int reflevel; // optional: integer => reflevel = 0,

	// ref integer => reflevel = 1,
	// ref ref integer => reflevel = 2
	private boolean virtual = false; // R.A. do we deal with virtual
	// reference?

	private boolean refFlag = false; // should deref be performed on this

	/**
	 * Initializes a reference signature.
	 * 
	 * @param OID
	 *            is reference to a object declaration in a metabase.
	 */
	public ReferenceSignature(OID value) {
		this(value, false);
	}

	/**
	 * Initializes a reference signature and possibly copy the attribute values
	 * from metabase.
	 * 
	 * @param OID
	 *            is reference to a object declaration in a metabase.
	 * @param copyAttrib
	 *            indicated whether to copy attributes from the metabase object.
	 */
	public ReferenceSignature(OID value, boolean copyAttrib) {		
		this.value = value;
		this.setMutable(true);
		if (copyAttrib) {

			try {
				MBObject mo = new MBObject(value);
				this.setMinCard(mo.getMinCard());
				this.setMaxCard(mo.getMaxCard());
				if (new MBVariable(value).isValid() || new MBVirtualVariable(value).isValid()) {
					
					this.reflevel = new MBVariable(value).getRefIndicator();
				}
				// if(new MBVirtualVariable(value).isValid())
			} catch (DatabaseException ex) {
				// something went wrong
				this.setMinCard(-1);
				this.setMaxCard(-1);
			}
		}
	}

	@Override
	public String dump(String indent) {
		MBObject mbobj = new MBObject(value);

		try {
			return "Reference-" + mbobj.getName() + super.dump("");
		} catch (DatabaseException ex) {
			return "Reference-???" + super.dump("");
		}
	}

	public boolean isRemote() {
		assert false : "not implemented";

		return links != null;
	}

	@Override
	public boolean isComparableTo(Signature sig) {	
		return isStructuralTypeCompatible(sig)&& super.isComparableTo(sig);
	}

	private boolean isSubClassInstance(ReferenceSignature paramsig) {
		try {
			MBVariable thisvar = new MBVariable(this.value);
			MBVariable paramvar = new MBVariable(paramsig.value);
			if (thisvar.isValid() && paramvar.isValid()) {
				MBClass thisclass = new MBClass(thisvar.getType());
				MBClass paramclass = new MBClass(paramvar.getType());
				if (thisclass.isValid() && paramclass.isValid()) {
					return paramclass.isSubClassOf(thisclass.getOID());
				}
			}
		} catch (DatabaseException e) {
			return false;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.results.compiletime.Signature#isStructuralTypeCompatible(odra.sbql.results.compiletime.Signature)
	 */
	@Override
	public boolean isStructuralTypeCompatible(Signature sig) {
		if (!(sig instanceof ReferenceSignature))
			return false;
		ReferenceSignature rsig = (ReferenceSignature) sig;
		
		return (value.equals(rsig.value) || isSubClassInstance(rsig));
	}
	/**
	 * @param virtual the virtual to set
	 */
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	/**
	 * @return the virtual
	 */
	public boolean isVirtual() {
		return virtual;
	}

	/**
	 * @param refFlag the refFlag to set
	 */
	public void setRefFlag(boolean refFlag) {
		this.refFlag = refFlag;
	}

	/**
	 * @return the refFlag
	 */
	public boolean hasRefFlag() {
		return refFlag;
	}
}
