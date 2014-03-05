package odra.sbql.results.compiletime;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBObject;
import odra.sbql.results.compiletime.util.ValueSignatureType;

/**
 * This class implements value signatures, i.e. abstract primitive values processed by the type checker.
 *
 * @author raist, stencel
 */

public class ValueSignature extends Signature {
	public OID value; // reference to a primitive type declaration in the module system
	
	private ValueSignatureType type;
	
			
	
	/**
	 * Initializes a value signature.
	 * @param OID is the reference to a primitive type declaration in the module system. 
	 */
	public ValueSignature(OID value) {
		this.value = value;
	}

	@Override
	public String dump(String indent) {
		MBObject mbobj = new MBObject(value);
		
		try {
			return "Value-" + mbobj.getName() + super.dump("");
		} catch (DatabaseException ex) {
			return "Value-???" + super.dump("");
		}
	}

	@Override
	public boolean isComparableTo(Signature sig) {		
		return isStructuralTypeCompatible(sig) && super.isComparableTo(sig) ;
	}

	public ValueSignatureType getType()
	{
		return type;
	}

	public void setType(ValueSignatureType type)
	{
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.results.compiletime.Signature#isStructuralTypeCompatible(odra.sbql.results.compiletime.Signature)
	 */
	@Override
	public boolean isStructuralTypeCompatible(Signature sig) {
		if (sig instanceof ValueSignature) {
			ValueSignature vsig = (ValueSignature) sig;
			return vsig.value.equals(this.value);
		}
		return false;
	}

	
	
}
