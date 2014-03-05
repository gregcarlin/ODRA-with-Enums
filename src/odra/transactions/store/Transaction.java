package odra.transactions.store;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import odra.sessions.Session;
import odra.store.guid.GUID;
import odra.store.guid.GUIDException;
import odra.store.guid.IGUIDIdentifiableResource;

public final class Transaction implements IGUIDIdentifiableResource {

	private final static List<Transaction> transactions = new LinkedList<Transaction>();

	private final GUID guid;

	private final Session session;

	private final ITransactionLock readLock;

	private final ITransactionLock exclusiveLock;

	private final Set<IDataPage> exclusivelyLockedPages;

	private Transaction nestedTransaction;

	private Transaction(Session session) {
		this.guid = GUID.generateGUID(transactions);
		this.session = session;
		this.readLock = TransactionLock.getExclusiveLock(this);
		this.exclusiveLock = TransactionLock.getExclusiveLock(this);
		this.exclusivelyLockedPages = new TreeSet<IDataPage>();
		this.nestedTransaction = null;
	}

	public static synchronized Transaction instantiate(Session session) throws TransactionStoreException {
		try {
			Transaction transaction = new Transaction(session);
			int insertionIndex = GUID.getInsertionIndex(transactions, transaction.getGUID());
			transactions.add(insertionIndex, transaction);
			return transaction;
		} catch (GUIDException ex) {
			throw new TransactionStoreException("transaction instantiation", ex);
		}
	}

	public static synchronized void terminate(Transaction transaction) {
		transactions.remove(transaction);
	}

	public int compareTo(IGUIDIdentifiableResource otherResource) {
		return this.guid.compareTo(otherResource.getGUID());
	}

	public GUID getGUID() {
		return this.guid;
	}

	public Transaction getMostNestedTransaction() {
		Transaction activeTransaction = this;
		while (activeTransaction.nestedTransaction != null) {
			activeTransaction = activeTransaction.nestedTransaction;
		}
		return activeTransaction;
	}

	public Transaction instantiateNested() {
		Transaction trnsActive = this.getMostNestedTransaction();
		trnsActive.nestedTransaction = new Transaction(this.session);
		return trnsActive.nestedTransaction;
	}

	public ITransactionLock getExclusiveLock() {
		return this.exclusiveLock;
	}

	public ITransactionLock getReadLock() {
		return this.readLock;
	}
	
	public void commit() {
		
	}

	private final static class ExclusivelyLockedPage implements Comparable<ExclusivelyLockedPage> {
		public int compareTo(ExclusivelyLockedPage o) {
			return 0;
		}
	}
}