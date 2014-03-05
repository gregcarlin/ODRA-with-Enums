package odra.db.schema;

/**
 * OdraVariableSchema
 * Transfer object to convey the information about 
 * Variable in the store independent format
 * @author radamus
 *last modified: 2008-05-03 renamed & moved
 *@version 1.0
 */
public class OdraVariableSchema extends OdraObjectSchema{

	private OdraTypeSchema typeDescriptor;
	
		
	/**
	 * @param vname
	 * @param tname
	 * @param mincard
	 * @param maxcard
	 * @param ref
	 */
	public OdraVariableSchema(String vname, String tname, int mincard, int maxcard, int ref) {
		super(vname);
		this.typeDescriptor = new OdraTypeSchema(tname, mincard, maxcard, ref);
	}

	

	/**
	 * @param tname the tname to set
	 */
	public void setTName(String tname) {
	    this.typeDescriptor.setTypeName(tname);
	}

	/**
	 * @return the tname
	 */
	public String getTName() {
	    return this.typeDescriptor.getTypeName();
	}

	/**
	 * @param mincard the mincard to set
	 */
	public void setMinCard(int mincard) {
	    this.typeDescriptor.setMinCard(mincard);
	}

	/**
	 * @return the mincard
	 */
	public int getMinCard() {
	    return this.typeDescriptor.getMinCard();
	}

	/**
	 * @param maxcard the maxcard to set
	 */
	public void setMaxCard(int maxcard) {
	    this.typeDescriptor.setMaxCard(maxcard);
	}

	/**
	 * @return the maxcard
	 */
	public int getMaxCard() {
	    return this.typeDescriptor.getMaxCard();
	}

	/**
	 * @param ref the ref to set
	 */
	public void setRefLevel(int ref) {
	    this.typeDescriptor.setRefs(ref);
	}

	/**
	 * @return the ref
	 */
	public int getRefLevel() {
	    return this.typeDescriptor.getRefs();
	}
	
}
