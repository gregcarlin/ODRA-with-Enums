/**
 * 
 */
package odra.db.schema;

import java.util.List;
import java.util.Vector;

/**
 * SchemaInterfaceInfo TODO!!
 * Transfer object to convey the information about 
 * Variable in the store independent format
 * @author radamus
 *	last modified: 2008-05-03 renamed & moved
 * @since 2008-04-30 
 * @version 1.0
 */
public class OdraInterfaceSchema extends OdraObjectSchema {
    private List<OdraProcedureHeaderSchema> procHeaders = new Vector<OdraProcedureHeaderSchema>();
    private List<OdraVariableSchema> variables = new Vector<OdraVariableSchema>();
}
