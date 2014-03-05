package odra.db.schema;

import java.util.List;
import java.util.Vector;

/**
 * OdraClassSchema
 * Transfer object to convey the information about 
 * Class in the store independent format
 * @author radamus
 *last modified: 2008-05-03 renamed & moved
 *@version 1.0
 */
public class OdraClassSchema extends OdraObjectSchema {

    private String[] superClassesNames;

    private String type;

    private String instanceName;

    private List<OdraProcedureSchema> methods = new Vector<OdraProcedureSchema>();

    public final static String NO_INVARIANT_NAME = "$NO_NAME";

    /**
     * @param type
     *                the type to set
     */
    public void setTypeName(String type) {
	this.type = type;
    }

    /**
     * @return the type
     */
    public String getTypeName() {
	return type;
    }

    /**
     * @param invariantName
     *                the invariantName to set
     */
    public void setInstanceName(String invariantName) {
	this.instanceName = invariantName;
    }

    /**
     * @return the invariantName
     */
    public String getInstanceName() {
	return instanceName;
    }

    public void addMethod(OdraProcedureSchema method) {
	this.methods.add(method);
    }

    /**
     * @return the methods
     */
    public OdraProcedureSchema[] getMethods() {
	return methods.toArray(new OdraProcedureSchema[methods.size()]);
    }

    /**
     * @return the superClassesNames
     */
    public String[] getSuperClassesNames() {
	return superClassesNames;
    }

    /**
     * @param superClassesNames
     *                the superClassesNames to set
     */
    public void setSuperClassesNames(String[] superClassesNames) {
	this.superClassesNames = superClassesNames;
    }
}
