package odra.transactions.store;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import odra.db.DatabaseException;
import odra.sessions.Session;
import odra.store.io.IHeap;
import odra.system.config.ConfigServer;
import odra.transactions.store.ITransactionLock.TransactionLockType;

public final class DataPage implements IDataPage {

	private final static int DATA_PAGE_SIZE = ConfigServer.TRANSACTIONS_DATA_PAGE_SIZE;

	private final static long LOCK_WAIT_TIMEOUT = ConfigServer.TRANSACTIONS_LOCK_WAIT_TIMEOUT;

	private final IHeap heap;

	private final int offset;

	private final Set<ITransactionLock> lcksRead;

	private ITransactionLock lckExclusive;

	private byte[] bytes;

	private DataPage(IHeap heap, int offset) {
		this.heap = heap;
		this.offset = offset;
		this.lcksRead = new TreeSet<ITransactionLock>();
		this.lckExclusive = null;
	}

	public static IDataPage getInstance(IHeap heap, int offset) {
		return new DataPage(heap, offset);
	}

	public int compareTo(Integer otherPageOffset) {
		return this.offset - otherPageOffset.intValue();
	}

	private static long getCurrentTimeInMillis() {
		return (new Date()).getTime();
	}

	private boolean setLock(TransactionLockType type) {
		try {
			ITransactionLock lckRead = Session.getReadLock();
			ITransactionLock lckExclusive = Session.getExclusiveLock();
			long waitTimeout = LOCK_WAIT_TIMEOUT;
			long elapsedTime = 0;
			long previousTime, currentTime;
			previousTime = currentTime = getCurrentTimeInMillis();
			while (true) {
				switch (type) {
					case READ:
						if (this.checkAndSetExclusiveLock(lckRead, lckExclusive)) {
							return true;
						}
						break;
					case EXCLUSIVE:
						if (this.checkAndSetReadLock(lckRead, lckExclusive)) {
							return true;
						}
						break;
				}
				currentTime = getCurrentTimeInMillis();
				elapsedTime = currentTime - previousTime;
				previousTime = currentTime;
				waitTimeout -= elapsedTime;
				if (waitTimeout > 0) {
					wait(waitTimeout);
				} else {
					return false;
				}
			}
		} catch (InterruptedException ex) {
			throw new TransactionStoreRuntimeException("exception occurred while setting exclusive lock", ex);
		}
	}

	private boolean checkAndSetExclusiveLock(ITransactionLock lckRead, ITransactionLock lckExclusive) {
		if (this.lckExclusive == lckExclusive || (this.lckExclusive == null && this.mayBeExcusivelyLocked(lckRead))) {
			this.lckExclusive = lckExclusive;
			this.lcksRead.add(lckRead);
			return true;
		}
		return false;
	}

	private boolean checkAndSetReadLock(ITransactionLock lckRead, ITransactionLock lckExclusive) {
		if (this.lckExclusive == null || this.lckExclusive == lckExclusive) {
			this.lcksRead.add(lckRead);
			return true;
		}
		return false;
	}

	public synchronized boolean setExclusiveLock() {
		return this.setLock(TransactionLockType.EXCLUSIVE);
	}

	public synchronized boolean setReadLock() {
		return this.setLock(TransactionLockType.READ);
	}

	public void fillBytesFromUnderlyingHeap() throws TransactionStoreException {
		try {
			if (!this.containsData()) {
				this.bytes = new byte[DATA_PAGE_SIZE];
				this.heap.read(this.offset, this.bytes);
			}
		} catch (DatabaseException ex) {
			throw new TransactionStoreException("error while filling bytes", ex);
		}
	}

	public boolean containsData() {
		return this.bytes != null;
	}

	public void writeBytes(int offset, byte[] data) throws TransactionStoreException {
		try {
			int exclLastIndex = offset + data.length;
			assert this.offset < exclLastIndex && (this.offset + DATA_PAGE_SIZE > offset) : "invalid offset or data array size";
			if (this.bytes == null) {
				this.bytes = new byte[DATA_PAGE_SIZE];
			}
			int dataFromIndex, pageFromIndex;
			pageFromIndex = dataFromIndex = this.offset - offset;
			pageFromIndex = (pageFromIndex < 0) ? Math.abs(pageFromIndex) : 0;

			if (dataFromIndex < 0) {
				dataFromIndex = 0;
			}
			int length = exclLastIndex - this.offset;
			if (length > DATA_PAGE_SIZE) {
				length = DATA_PAGE_SIZE;
			}
			System.arraycopy(data, dataFromIndex, this.bytes, pageFromIndex, length);
		} catch (Exception ex) {
			throw new TransactionStoreException("exception occurred while writing bytes", ex);
		}
	}

	public void readBytes(int offset, byte[] buffer) throws TransactionStoreException {
		try {
			int exclLastIndex = offset + buffer.length;
			assert this.offset < exclLastIndex && (this.offset + DATA_PAGE_SIZE > offset) : "invalid offset or array size";

			int dataFromIndex, pageFromIndex;
			pageFromIndex = dataFromIndex = this.offset - offset;
			pageFromIndex = (pageFromIndex < 0) ? Math.abs(pageFromIndex) : 0;

			if (dataFromIndex < 0) {
				dataFromIndex = 0;
			}
			int length = exclLastIndex - this.offset;
			if (length > DATA_PAGE_SIZE) {
				length = DATA_PAGE_SIZE;
			}

			final byte[] bytes = new byte[DATA_PAGE_SIZE];
			byte[] source = null;
			if (this.containsData()) {
				source = this.bytes;
			} else {
				this.heap.read(this.offset, bytes);
				source = bytes;
			}
			System.arraycopy(source, pageFromIndex, buffer, dataFromIndex, length);
		} catch (Exception ex) {
			throw new TransactionStoreException("exception occurred while reading bytes", ex);
		}
	}

	private synchronized boolean releaseLock() {
		ITransactionLock lckExclusive = Session.getExclusiveLock();
		ITransactionLock lckRead = Session.getReadLock();
		if (this.lckExclusive == lckExclusive) {
			this.lckExclusive = null;
		}
		return this.lcksRead.remove(lckRead);
	}

	private final static int SINGLE_LOCK_SIZE = 1;

	private boolean mayBeExcusivelyLocked(ITransactionLock lckRead) {
		return this.lcksRead.isEmpty() || (this.lcksRead.size() == SINGLE_LOCK_SIZE && this.lcksRead.contains(lckRead));
	}

	public int getSize() {
		return getDataPageSize();
	}

	public static int getDataPageSize() {
		return DATA_PAGE_SIZE;
	}

	public IHeap getUnderlyingHeap() {
		return this.heap;
	}

	public void commitChanges() throws TransactionStoreException {
		try {
			this.heap.write(this.offset, this.bytes);
		} catch (DatabaseException ex) {
			throw new TransactionStoreException("exception while commiting changes", ex);
		}
	}
}