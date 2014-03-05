package odra.sbql.results.compiletime;

import java.util.Iterator;
import java.util.Vector;

/**
 * This class implements variant signatures processed by the type checker.
 *
 * @author raist, stencel
 */

public class VariantSignature extends Signature {
	private Vector<Signature> fields = new Vector();
	
	/**
	 * Initializes an "empty" variant signature. 
	 */	
	public VariantSignature() {
	}
	
	/**
	 * Adds a new field to the variant signature.
	 * @param sig is the signature to be added. 
	 */	
	public void addField(Signature sig) {
		fields.addElement(sig);
	}
	
	/**
	 * Adds a new fields to the variant signature.
	 * @param sig is the array with signatures to be added. 
	 */	
	public void addFields(Signature[] sig) {
		for (Signature s : sig)
			fields.addElement(s);
	}

	/**
	 * Return the fields of the variant signature.
	 * @return is the array of subsignatures of the struct signature. 
	 */	
	public Signature[] getFields() {
		return fields.toArray(new Signature[fields.size()]);
	}

	@Override
	public String dump(String indent) {
		StringBuffer result = new StringBuffer();
		
		for (Signature s : fields)
			result.append("  " + indent + s.dump("  " + indent) + "\n"); 

		return indent + "Variant{\n" + result.toString() + indent + "}" + super.dump("");
	}

	@Override
	public boolean isComparableTo(Signature sig) {
		return super.isComparableTo(sig) && isStructuralTypeCompatible(sig);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.results.compiletime.Signature#isStructuralTypeCompatible(odra.sbql.results.compiletime.Signature)
	 */
	@Override
	public boolean isStructuralTypeCompatible(Signature sig) {
		if (!(sig instanceof VariantSignature))
			return false;

		VariantSignature ssig = (VariantSignature) sig;
		if (fields.size() != ssig.fields.size())
			return false;
		
		Iterator<Signature> iter = fields.iterator();
		
		for (Signature i : ssig.getFields()) {
			if (!iter.next().isComparableTo(i))
				return false;
		}
		
		// every item is comparable, so StructSignatures is comparable
		return true;

	}	
}
