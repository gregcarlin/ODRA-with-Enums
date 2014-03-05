package odra.ws.endpoints.wsdl.typeresolver;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBVariable;
/** Provides key implementation for variables dictionary used in AbstractWSDLBuilder
 * 
 * @since 2007-10-20
 * @version 2007-10-20
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * TODO consider removal 
 */
public class FieldKey {
	
	private String typeName;
	private int minCard;
	private int maxCard;
	

	public FieldKey(FieldInfo field) throws DatabaseException {
		this.typeName = field.getType().getObjectName();
		this.minCard = field.getMinCard();
		this.maxCard = field.getMaxCard();
	}
	
	public FieldKey(String typeName, int minCard, int maxCard) {
		this.typeName = typeName;
		this.minCard = minCard;
		this.maxCard = maxCard;
	}

	public int getMaxCard() {
		return this.maxCard;
	}

	public void setMaxCard(int maxCard) {
		this.maxCard = maxCard;
	}

	public int getMinCard() {
		return this.minCard;
	}

	public void setMinCard(int minCard) {
		this.minCard = minCard;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FieldKey)
		{
			FieldKey varKey = (FieldKey) obj;
			if ( this.typeName.equals(varKey.typeName) && 
					( (this.maxCard > 1 && varKey.maxCard > 1) || (this.maxCard <= 1 && varKey.maxCard <= 1))
				)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
		
	}
	
	@Override
	public int hashCode() {
		return this.typeName.hashCode();
	}
	
}