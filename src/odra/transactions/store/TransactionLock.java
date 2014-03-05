package odra.transactions.store;

import java.util.Map;
import java.util.TreeMap;

import odra.store.guid.GUID;

/**
 * Represents a lock set by a transaction. There are only two different locks per each transaction:
 * <li>read lock, and</li>
 * <li>exclusive one.</li>
 * 
 * @author edek
 */
public final class TransactionLock implements ITransactionLock {

	private final static Map<Transaction, TransactionLockPair> lockPairs = new TreeMap<Transaction, TransactionLockPair>();

	private final Transaction transaction;

	private final TransactionLockType lockType;

	private TransactionLock(Transaction transaction, TransactionLockType lockType) {
		this.transaction = transaction;
		this.lockType = lockType;
	}

	/**
	 * This method should be invoked only while instantiating a session associated with a given ServerProcess thread. The
	 * purpose of the method is to create a single read lock object for each session to avoid garbage collecting
	 * 
	 * @param session
	 * @return
	 */
	public static ITransactionLock getReadLock(Transaction transaction) {
		return getTransactionLockPair(transaction).getReadLock();
	}

	/**
	 * This method should be invoked only while instantiating a session associated with a given ServerProcess thread. The
	 * purpose of the method is to create a single exclusive lock object for each session to avoid garbage collecting
	 * 
	 * @param session
	 * @return
	 */
	public static ITransactionLock getExclusiveLock(Transaction transaction) {
		return getTransactionLockPair(transaction).getReadLock();
	}

	public int compareTo(ITransactionLock otherLock) {
		if (this.getLockType() != otherLock.getLockType()) {
			throw new TransactionStoreRuntimeException("locks type do not match");
		}
		Transaction otherTransaction = otherLock.getTransaction();
		return this.getTransaction().compareTo(otherTransaction);
	}

	public Transaction getTransaction() {
		return this.transaction;
	}

	public TransactionLockType getLockType() {
		return this.lockType;
	}

	private static TransactionLockPair getTransactionLockPair(Transaction transaction) {
		TransactionLockPair lockPair = lockPairs.get(transaction);
		if (lockPair == null) {
			lockPair = new TransactionLockPair(transaction);
			lockPairs.put(transaction, lockPair);
		}
		return lockPair;
	}

	private final static class TransactionLockPair {

		private final ITransactionLock readLock;

		private final ITransactionLock exclusiveLock;

		private TransactionLockPair(Transaction transaction) {
			this.readLock = new TransactionLock(transaction, TransactionLockType.READ);
			this.exclusiveLock = new TransactionLock(transaction, TransactionLockType.EXCLUSIVE);
		}

		private ITransactionLock getReadLock() {
			return this.readLock;
		}

		private ITransactionLock getExclusiveLock() {
			return this.exclusiveLock;
		}
	}
}