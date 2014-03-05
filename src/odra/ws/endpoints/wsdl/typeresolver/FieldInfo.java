package odra.ws.endpoints.wsdl.typeresolver;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBVariable;

/** Container for typedef resolving. Allows to keep and access both {@link MBProcedure} and {@link MBVariable}
 *
 * @since 2007-10-20
 * @version 2007-10-20
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 * TODO think of more appropriate name for this class
 */
public class FieldInfo  {


	private String name;
	private OID type;
	private int minCard;
	private int maxCard;

	/** Copying constructor with type substitution
	 * @param info
	 * @param type
	 */
	public FieldInfo(FieldInfo info)
	{
		this.name = info.name;
		this.type = info.type;
		this.minCard = info.minCard;
		this.maxCard = info.maxCard;
	}

	/** Constructor based on meta procedure info
	 * @param mbProc
	 * @throws DatabaseException
	 */
	public FieldInfo(MBProcedure mbProc) throws DatabaseException  {
		this.name = mbProc.getName();
		this.type = mbProc.getType();
		this.minCard = mbProc.getMinCard();
		this.maxCard = mbProc.getMaxCard();

	}

	/** Constructor based on meta class info
	 * @param mbClass
	 * @throws DatabaseException
	 */
	public FieldInfo(MBClass mbClass) throws DatabaseException  {
		this.name = mbClass.getInstanceName();
		this.type = mbClass.getOID();
		this.minCard = mbClass.getMinCard();
		this.maxCard = mbClass.getMaxCard();

	}

	/** Constructor based on meta variable info
	 * @param mbVar
	 * @throws DatabaseException
	 */
	public FieldInfo(MBVariable mbVar) throws DatabaseException {
		this.name = mbVar.getName();
		this.type = mbVar.getType();
		this.minCard = mbVar.getMinCard();
		this.maxCard = mbVar.getMaxCard();

	}

	/** Generic constructor
	 * @param name
	 * @param type
	 * @param minCard
	 * @param maxCard
	 */
	public FieldInfo(String name, OID type, int minCard, int maxCard)
	{
		this.name = name;
		this.type = type;
		this.minCard = minCard;
		this.maxCard = maxCard;

	}

	/** Gets field name
	 * @return
	 * @throws DatabaseException
	 */
	public String getName() throws DatabaseException
	{
		return this.name;
	}

	/** Gets field type
	 * @return
	 * @throws DatabaseException
	 */
	public OID getType() throws DatabaseException {
		return this.type;
	}

	/** Gets field maximal cardinality
	 * @return
	 */
	public int getMaxCard() throws DatabaseException {
		return this.maxCard;
	}

	/** Gets field minimal cardinality
	 * @return
	 */
	public int getMinCard() throws DatabaseException {
		return this.minCard;
	}

	/** Sets field name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** Sets field type
	 * @param type
	 */
	public void setType(OID type) {
		this.type = type;
	}

	/** Sets field minimal cardinality
	 * @param minCard
	 */
	public void setMinCard(int minCard) {
		this.minCard = minCard;
	}

	/** Sets field maximal cardinality
	 * @param maxCard
	 */
	public void setMaxCard(int maxCard) {
		this.maxCard = maxCard;
	}

	/**
	 * Get key part of info object
	 * @return
	 * @throws DatabaseException
	 */
	public FieldKey getKey() throws DatabaseException  {
		return new FieldKey(this);
	}
}
