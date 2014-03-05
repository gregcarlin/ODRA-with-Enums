/**
 * 
 */
package odra.db.schema;

/**
 * OdraProcedureHeaderSchema
 * Transfer object to convey the information about 
 * Procedure header in the store independent format
 * @author Radek Adamus
 * @since 2008-04-30 last modified: 2008-04-30
 * @version 1.0
 */
public class OdraProcedureHeaderSchema extends OdraObjectSchema {

    private ProcArgument[] arguments;

    private OdraTypeSchema result;

    /**
     * @param args
     * @param res
     */
    OdraProcedureHeaderSchema(String name, ProcArgument[] args, OdraTypeSchema res) {
	super(name);
	this.arguments = args;
	this.result = res;
    }

    /**
     * @return the arguments
     */
    public ProcArgument[] getArguments() {
	return arguments;
    }

    /**
     * @param arguments
     *                the arguments to set
     */
    public void setArguments(ProcArgument[] arguments) {
	this.arguments = arguments;
    }

    /**
     * @return the result
     */
    public OdraTypeSchema getResult() {
	return result;
    }

    /**
     * @param result
     *                the result to set
     */
    public void setResult(OdraTypeSchema result) {
	this.result = result;
    }

}
