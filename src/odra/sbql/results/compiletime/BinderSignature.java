package odra.sbql.results.compiletime;


/**
 * This class implements binder signatures, i.e. abstract binders processed by the type checker.
 *
 * @author raist, stencel
 */

public class BinderSignature extends Signature {
	public String name;
	public Signature value;
	public boolean isAuxiliaryName; //true if the name is auxiliary

	/**
	 * Initializes a binder signature.
	 * @param name  is the name held by the binder signature.
	 * @param value is the argument signature of the binder signature. 
	 */
	public BinderSignature(String name, Signature value) {
		this.name = name;
		this.value = value;
		this.isAuxiliaryName = false;
	}
	/**
	 * Initializes a binder signature.
	 * @param name  is the name held by the binder signature.
	 * @param value is the argument signature of the binder signature. 
	 * @param isAuxiliary - true if the binder name is auxiliary name created with 'as'/'groupas'
	 */
	public BinderSignature(String name, Signature value, boolean isAuxiliary) {
		this.name = name;
		this.value = value;
		this.isAuxiliaryName = isAuxiliary;
	}
	@Override
	public String dump(String indent) {
		return "Binder-" + name 
			+ "(\n  " + indent + value.dump("  "+indent) + "\n" + indent +")" 
			+ super.dump("");
	}

	@Override
	public boolean isComparableTo(Signature sig) {
		if (!(sig instanceof BinderSignature))
			return false;
		BinderSignature bsig = (BinderSignature) sig;		
		return super.isComparableTo(sig) && name.equals(bsig.name) && this.value.isComparableTo(bsig.value); 
	}
	/* (non-Javadoc)
	 * @see odra.sbql.results.compiletime.Signature#isStructuralTypeCompatible(odra.sbql.results.compiletime.Signature)
	 */
	@Override
	public boolean isStructuralTypeCompatible(Signature sig) {
		if (!(sig instanceof BinderSignature))
			return false;
		BinderSignature bsig = (BinderSignature) sig;
		return name.equals(bsig.name) && value.isStructuralTypeCompatible(bsig.value);
	}


}
