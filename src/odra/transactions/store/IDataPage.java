package odra.transactions.store;

import odra.store.io.IHeap;

/**
 * Represents physical data page.
 * 
 * @author edek
 */
public interface IDataPage extends Comparable<Integer> {

	boolean setExclusiveLock();

	boolean setReadLock();

	/**
	 * Get underlying {@link IHeap}.
	 * 
	 * @return underlying heap
	 */
	IHeap getUnderlyingHeap();

	void fillBytesFromUnderlyingHeap() throws TransactionStoreException;

	/**
	 * Check whether the page contains data.
	 * 
	 * @return true if page contains data read from the underlying {@link IHeap} --- obviously the data may be modified
	 *         afterwards.
	 */
	boolean containsData();

	/**
	 * Writes bytes starting from the given offset up to the size of the data buffer.
	 * <p>
	 * The proper {@link IDataPage} implementation should ensure that only the data stored on the given page are written.
	 * In other words if the byte buffer holds data that should be written to more than one page, only the part which
	 * corresponds to the present {@link IDataPage} should be written.
	 * 
	 * @param offset
	 *           the absolute offset for the underlying heap
	 * @param data
	 *           the buffer the data that should be stored in the {@link IDataPage} are read from
	 * @throws TransactionStoreException
	 */
	void writeBytes(int offset, byte[] data) throws TransactionStoreException;

	/**
	 * Reads bytes starting from the given offset up to the size fo the data buffer.
	 * <p>
	 * The proper {@link IDataPage} implementation should ensure that only the data stored on the given page are read and
	 * put to the data buffer. In other words if the byte buffer holds data that should be written to more than one page,
	 * only the part which corresponds to the present {@link IDataPage} should be copied to the appropriate segment of
	 * the byte buffer.
	 * 
	 * @param offset
	 *           the absolute offset for the underlying heap
	 * @param buffer
	 *           the buffer the data
	 * @throws TransactionStoreException
	 */
	void readBytes(int offset, byte[] buffer) throws TransactionStoreException;

	/**
	 * Commit changes onto the underlying {@link IHeap}
	 * 
	 * @throws TransactionStoreException
	 */
	void commitChanges() throws TransactionStoreException;

	/**
	 * Get size of the {@link IDataPage}.
	 * 
	 * @return size of the page (should be constant for the given implementation)
	 */
	int getSize();
}