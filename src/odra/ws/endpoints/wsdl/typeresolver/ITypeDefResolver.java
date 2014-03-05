package odra.ws.endpoints.wsdl.typeresolver;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBTypeDef;

/** Interface 
 * @author Marcin Daczkowski
 *
 */
public interface ITypeDefResolver {

	/** Registers new type definition in resolver
	 * @param typeDef
	 * @throws DatabaseException
	 */
	public abstract void addTypeDef(MBTypeDef typeDef) throws DatabaseException;

	/** Registers new type definition in resolver
	 * @param name
	 * @param type
	 * @throws DatabaseException
	 */
	public void addTypeDef(String name, OID type) throws DatabaseException;
	
	
	/** Finds normal version for typedef version
	 * @param typeDefName
	 * @return
	 * @throws DatabaseException
	 */
	public abstract OID resolveName(String typeDefName)
			throws DatabaseException;

	/** Finds normal version for type
	 * @param type
	 * @return
	 * @throws DatabaseException
	 */
	public abstract String resolveType(OID type) throws DatabaseException;

}