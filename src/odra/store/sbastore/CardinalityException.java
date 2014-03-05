/**
 * 
 */
package odra.store.sbastore;

import odra.db.DatabaseException;



/**
 * CardinalityException
 * @author Radek Adamus
 *@since 2008-09-09
 *last modified: 2008-09-09
 *@version 1.0
 */
@SuppressWarnings("serial")
public class CardinalityException extends DatabaseException {

	private final int objectNameId;
	private final int minCard;
	private final int maxCard;
	/**
	 * @return the minCard
	 */
	public int getMinCard() {
		return minCard;
	}
	/**
	 * @return the maxCard
	 */
	public int getMaxCard() {
		return maxCard;
	}
	/**
	 * @return the objectNameid
	 */
	public int getObjectNameId() {
		return objectNameId;
	}
	/**
	 * @param message
	 */
	public CardinalityException(String message, int objnameid, int minCard, int maxCard) {
		super(message);
		objectNameId = objnameid;
		this.minCard = minCard;
		this.maxCard = maxCard;
	}

}
