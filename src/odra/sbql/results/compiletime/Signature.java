package odra.sbql.results.compiletime;

import java.util.HashSet;
import java.util.Set;

import odra.db.OID;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.results.AbstractQueryResult;
import odra.system.config.ConfigDebug;

/**
 * This abstract class implements signatures, i.e. abstract values processed by the type checker.
 * Each AST node gets assigned a signature during the type check. 
 *
 * @author raist, stencel, radamus
 */

public abstract class Signature extends AbstractQueryResult implements java.io.Serializable, Cloneable {
	private int minCard = 1;
	private int maxCard = 1;
	private String typeName = null; 
	private boolean mutable = false;
	private CollectionKind collectionKind = new CollectionKind(CollectionKind.NONE_COLLECTION);
	private Expression generator; //radamus: node that generates the signature
	private Expression associator; //radamus: node that is associated with the signature through nested
	private String enumName = null;
		
	public Set<OID> links; //do we deal with reference of remote object?
	
	/**
	 * Initializes an "empty" default signature. 
	 */
	public Signature() {
		links = new HashSet<OID>();
	}

	/**
	 * Performs a shallow (default) copy of the signature.
	 * Uses the default behaviour of the Object method.
	 * @return a shallow copy of the signature.
	 */
	@Override
	public Signature clone()  {
		Signature newSig;
		try {
		    newSig = (Signature)super.clone();
		} catch (CloneNotSupportedException e) {
		    assert false : "signature should be cloneable";
			return null;
		}
		
		newSig.links = new HashSet<OID>();
		for(OID oid : this.links)
			newSig.links.add(oid);		
		
		return newSig;
	}

	/**
	 * @param minCard the minCard to set
	 */
	public void setMinCard(int minCard) {
	    this.minCard = minCard;
	}

	/**
	 * @return the minCard
	 */
	public int getMinCard() {
	    return minCard;
	}

	/**
	 * @param maxCard the maxCard to set
	 */
	public void setMaxCard(int maxCard) {
	    this.maxCard = maxCard;
	    if(maxCard > 1)
		collectionKind = new CollectionKind(CollectionKind.BAG_COLLECTION);
	}
	/**
	 * @param minCard - minimal cardinality
	 * @param maxCard - maximal cardinality
	 */
	public void setCardinality(int minCard, int maxCard){
		this.setMinCard(minCard);
		this.setMaxCard(maxCard);
	}
	/**
	 * @return the maxCard
	 */
	public int getMaxCard() {
	    return maxCard;
	}

	/**
	 * Checks whether the values represented by the signatures can be compared.
	 * @param sig is the signature to be checked against this.
	 * @return true if the two signatures are comparable.
	 */
	public boolean isComparableTo(Signature sig) {
		return (mutable == sig.mutable) && isTypeNameCompatible(sig)
			&& ((sig.minCard <= minCard && minCard <= sig.maxCard) 
				// TODO: verify whether || should be substituted with &&
				||
				(sig.minCard <= maxCard && maxCard <= sig.maxCard)); 
	}

	
	/**
	 * Checks whether the the signatures are compatible according 
	 * to the distinct type naming rules.
	 * @param sig is the signature to be checked against this.
	 * @return true if the two signatures are distinct type name comparable.
	 */
	public boolean isTypeNameCompatible(Signature sig) {
		if (typeName != null && sig.typeName != null )
			return typeName.equals(sig.typeName);
		if (typeName == null && sig.typeName == null )
			return true;

		return false;
	}
	public abstract boolean isStructuralTypeCompatible(Signature sig);
	/**
	 * Dumps the signature for the sake of debugging. 
	 * @return the string representing the signature.
	 */
	public String dump(String indent) {
		String dCard = "card=" + minCard + ".." + ((maxCard == Integer.MAX_VALUE) ? "*" : maxCard);
		String dTypeName = "typeName=" + typeName;
		String dMutable = "mutable=" + mutable;
		String dCollection = "colKind=" + collectionKind.kindAsString();
		
		return " [" + dCard    + "," + dTypeName   + "," 
				   + dMutable + "," + dCollection + "]";
	}

	/*********************** STATIC METHODS ******************************/
	
	/**
	 * Performs "safe" cardinality multiplication. 
	 * If the product of arguments is greater than Integer.MAX_VALUE, Integer.MAX_VALUE is returned.
	 * This causes the cardinality * to be handled well. * times * is still *.
	 * @param a first cardinality to be multiplied. 
	 * @param b second cardinality to be multiplied. 
	 * @return is the product of a and b, however it is greater than Integer.MAX_VALUE, Integer.MAX_VALUE is returned
	 */
	public static int cardinalityMult(int a, int b) {
		long product = (long)a * (long)b;

		if (ConfigDebug.ASSERTS) assert a >= 0 && b >= 0; 
		
		if (product > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		
		return (int) product;
	}
	
	/**
	 * Performs "safe" cardinality addition. 
	 * If the sum of arguments is greater than Integer.MAX_VALUE, Integer.MAX_VALUE is returned.
	 * This causes the cardinality * to be handled well. * plus * is still *.
	 * @param a first cardinality to be added. 
	 * @param b second cardinality to be added. 
	 * @return is the sum of a and b, however it is greater than Integer.MAX_VALUE, Integer.MAX_VALUE is returned
	 */
	public static int cardinalityAdd(int a, int b) {
		long sum = (long)a + (long)b;

		if (ConfigDebug.ASSERTS) assert a >= 0 && b >= 0; 
		
		if (sum > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		
		return (int) sum;
	}
	
	public void addLinkDecoration(OID oid)
	{
		links.add(oid);
	}

	/**
	 * @param generator the generator to set
	 */
	public void setOwnerExpression(Expression generator) {
	    this.generator = generator;
	}

	/**
	 * @return the generator
	 */
	public Expression getOwnerExpression() {
	    return generator;
	}

	/**
	 * @param associator the associator to set
	 */
	public void setAssociatedExpression(Expression associator) {
	    this.associator = associator;
	}

	/**
	 * @return the associator
	 */
	public Expression getAssociatedExpression() {
	    return associator;
	}

	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * @param mutable the mutable to set
	 */
	public void setMutable(boolean mutable) {
		this.mutable = mutable;
	}

	/**
	 * @return the mutable
	 */
	public boolean isMutable() {
		return mutable;
	}

	/**
	 * @param collectionKind the collectionKind to set
	 */
	public void setCollectionKind(CollectionKind collectionKind) {
		this.collectionKind = collectionKind;
	}

	/**
	 * @return the collectionKind
	 */
	public CollectionKind getCollectionKind() {
		return collectionKind;
	}
	
	public void setEnumerator(String enumName) {
		this.enumName = enumName;
	}

	public String getEnumerator() {
		return enumName;
	}
		
	
}
