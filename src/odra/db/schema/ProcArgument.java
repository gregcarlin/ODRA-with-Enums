package odra.db.schema;

/**
 * This is an auxiliary class used to convey information about
 * procedure parameters to the Schema Manager.
 * 
 * @author raist
 */

public class ProcArgument extends OdraObjectSchema{
    
    private OdraTypeSchema typeDescriptor;	
    

	public ProcArgument(String aname, String tname, int mincard, int maxcard, int refs) {
	    	super(aname);
		this.typeDescriptor = new OdraTypeSchema(tname, mincard, maxcard, refs);
	}

	/**
	 * @param readVariable
	 */
	public ProcArgument(OdraVariableSchema variableSchema) {
	    this(variableSchema.getTName(), variableSchema.getTName(), variableSchema.getMinCard(), variableSchema.getMaxCard(), variableSchema.getRefLevel());
	    
	}

	/**
	 * @return the tname
	 */
	public String getTypeName()
	{
	    return typeDescriptor.getTypeName();
	}


	/**
	 * @return the mincard
	 */
	public int getMinCard()
	{
	    return this.typeDescriptor.getMinCard();
	}



	/**
	 * @return the maxcard
	 */
	public int getMaxCard()
	{
	    return this.typeDescriptor.getMaxCard();
	}


	/**
	 * @return the refs
	 */
	public int getRefs()
	{
	    return this.typeDescriptor.getRefs();
	}
}
