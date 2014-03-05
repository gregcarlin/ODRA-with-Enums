package odra.transactions.store;

/**
 * Represents lock set on {@link IDataPage} instances to ensure data consistency.
 * 
 * @author edek
 */
public interface ITransactionLock extends Comparable<ITransactionLock> {

	/**
	 * Get the type of present transaction lock
	 * 
	 * @return type of transaction lock
	 */
	TransactionLockType getLockType();

	/**
	 * Get the transaction which set the present lock
	 * 
	 * @return transaction which set the lock
	 */
	Transaction getTransaction();

	/**
	 * The enum representing possible type of transaction locks
	 * 
	 * @author edek
	 */
	public static enum TransactionLockType {
		READ, EXCLUSIVE;
	}
}