package odra.db.schema;
/**
* This is an auxilliary class used to convey information about
* type (name, cardinality, reference level) to the Schema Manager.
* 
* @author radamus, raist
* TODO change it - this is not a type schema in the matabase
*/

public class OdraTypeSchema extends OdraObjectSchema {
	private int minCard;
	private int maxCard;
	private int refs;
	
	
	/**
	 * @param tname - name of the type
	 * @param mincard - minimal cardinality of the procedure result
	 * @param maxcard - maximal cardinality of the procedure result
	 * @param refs - reference level
	 */
	public OdraTypeSchema( String tname, int mincard, int maxcard, int refs) {
		
		super(tname);
		this.minCard = mincard;
		this.maxCard = maxcard;
		this.refs = refs;
	}

	/**
	 * @param tname the tname to set
	 */
	public final void setTypeName(String tname) {
	    this.setName(tname);
	}

	/**
	 * @return the tname
	 */
	public final String getTypeName() {
	    return this.getName();
	}

	/**
	 * @param mincard the mincard to set
	 */
	public final void setMinCard(int mincard) {
	    this.minCard = mincard;
	}

	/**
	 * @return the mincard
	 */
	public final int getMinCard() {
	    return minCard;
	}

	/**
	 * @param maxcard the maxcard to set
	 */
	public final void setMaxCard(int maxcard) {
	    this.maxCard = maxcard;
	}

	/**
	 * @return the maxcard
	 */
	public final int getMaxCard() {
	    return maxCard;
	}

	/**
	 * @param refs the refs to set
	 */
	public final void setRefs(int refs) {
	    this.refs = refs;
	}

	/**
	 * @return the refs
	 */
	public final int getRefs() {
	    return refs;
	}
}