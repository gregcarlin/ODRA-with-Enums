package odra.transactions.store;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import odra.db.DatabaseException;
import odra.store.io.AbstractHeapExtension;
import odra.store.io.IHeap;
import odra.store.io.IHeapExtension;
import odra.store.io.IHeapExtension.ExtensionType;
import odra.system.Sizes;
import odra.system.log.UniversalLogger;
import odra.transactions.TransactionsAssemblyInfo;

public final class TransactionCapableHeap extends AbstractHeapExtension implements ITransactionCapableHeap {

	private final static UniversalLogger logger = UniversalLogger.getInstance(TransactionsAssemblyInfo.class,
				TransactionCapableHeap.class);

	private final IHeap heapImpl;

	private final List<IDataPage> pages = new ArrayList<IDataPage>();

	private boolean enabled;

	private TransactionCapableHeap(IHeap heapImpl) {
		super(ExtensionType.TRANSACTIONS);
		this.heapImpl = heapImpl;
		this.heapImpl.addExtension(this);
		this.enabled = false;
	}

	public static ITransactionCapableHeap getInstance(IHeap heapImpl) {
		return new TransactionCapableHeap(heapImpl);
	}

	private List<IDataPage> getDataPages(int offset, byte[] buffer) {
		int[] pageOffsets = this.getDataPageOffsets(offset, buffer);
		List<IDataPage> pages = new ArrayList<IDataPage>();
		for (int i = 0; i < pageOffsets.length; i++) {
			pages.add(this.getDataPage(pageOffsets[i]));
		}
		return pages;
	}

	private int[] getDataPageOffsets(int offset, byte[] buffer) {
		int pageSize = DataPage.getDataPageSize();
		int pageCount = (buffer.length / pageSize) + 1;
		int[] offsets = new int[pageCount];
		int pageOffset = offset - (offset % pageSize);
		for (int i = 0; i < pageCount;) {
			offsets[i] = pageOffset;
			pageOffset += pageSize;
			i++;
		}
		return offsets;
	}

	private IDataPage getDataPage(int dataPageOffset) {
		int index = Collections.binarySearch(this.pages, dataPageOffset);
		IDataPage page = null;
		if (index < 0) {
			page = DataPage.getInstance(this.heapImpl, dataPageOffset);
			int insertionIndex = Math.abs(index + 1);
			this.pages.add(insertionIndex, page);
		} else {
			page = this.pages.get(index);
		}
		return page;
	}

	private synchronized void setExclusiveLocks(List<IDataPage> pages) {
		Iterator<IDataPage> i = pages.iterator();
		while (i.hasNext()) {
			IDataPage page = i.next();
			if (!page.setExclusiveLock()) {
				throw new TransactionStoreRuntimeException("exclusive lock could not be set (timeout elapsed)");
			}
		}
	}

	private final static String ERROR_SET_READ_LOCKS = "ERROR_SET_READ_LOCKS";

	private synchronized void setReadLocks(List<IDataPage> pages) {
		Iterator<IDataPage> i = pages.iterator();
		while (i.hasNext()) {
			IDataPage page = i.next();
			if (!page.setReadLock()) {
				throw new TransactionStoreRuntimeException(TransactionCapableHeap.class, ERROR_SET_READ_LOCKS,
							"most probable reason: timeout elapsed");
			}
		}
	}

	public void close() {
		logger.debug("close");
		this.heapImpl.close();
	}

	public int getSize() {
		logger.debug("getSize");
		return this.heapImpl.getSize();
	}

	public int getStartOffset() {
		logger.debug("getStartOffset");
		return this.heapImpl.getStartOffset();
	}

	public int getUserSpaceLength() {
		logger.debug("getUserSpaceLength");
		return this.heapImpl.getUserSpaceLength();
	}

	public void open() throws DatabaseException {
		logger.debug("open");
		this.heapImpl.open();
	}

	private final static String ERROR_READ = "ERROR_READ";

	public void read(int offset, byte[] buffer) throws DatabaseException {
		logger.debug("read");
		if (this.enabled) {
			try {
				List<IDataPage> pages = this.getDataPages(offset, buffer);
				this.setReadLocks(pages);
				Iterator<IDataPage> i = pages.iterator();
				while (i.hasNext()) {
					IDataPage page = i.next();
					page.readBytes(offset, buffer);
				}
			} catch (TransactionStoreException ex) {
				throw new DatabaseException(TransactionCapableHeap.class, ERROR_READ, ex);
			}
		} else {
			this.heapImpl.read(offset, buffer);
		}
	}

	public boolean readBoolean(int offset) throws DatabaseException {
		logger.debug("readBoolean");
		return (this.readByte(offset) == 0) ? false : true;
	}

	private final static int SINGLE_BYTE_LEN = 1;
	
	private final static int FIRST_BYTE_INDEX = 0;

	public byte readByte(int offset) throws DatabaseException {
		logger.debug("readByte");
		final byte[] buffer = new byte[SINGLE_BYTE_LEN];
		this.read(offset, buffer);
		return buffer[FIRST_BYTE_INDEX];
	}

	public byte[] readBytesWithLength(int offset) throws DatabaseException {
		logger.debug("readBytesWithLength");
		int length = this.readInteger(offset);
		byte[] buffer = new byte[length];
		this.read(offset + Sizes.INTVAL_LEN, buffer);
		return buffer;
	}

	public double readDouble(int offset) throws DatabaseException {
		logger.debug("readDouble");
		final ByteBuffer buffer = ByteBuffer.allocate(Sizes.DOUBLEVAL_LEN);
		final byte[] bytes = buffer.array();
		buffer.clear();
		this.read(offset, bytes);
		return buffer.getDouble();
	}

	public int readInteger(int offset) throws DatabaseException {
		logger.debug("readInteger");
		final ByteBuffer buffer = ByteBuffer.allocate(Sizes.INTVAL_LEN);
		final byte[] bytes = buffer.array();
		buffer.clear();
		this.read(offset, bytes);
		return buffer.getInt();
	}

	public long readLong(int offset) throws DatabaseException {
		logger.debug("readLong");
		final ByteBuffer buffer = ByteBuffer.allocate(Sizes.LONGVAL_LEN);
		final byte[] bytes = buffer.array();
		buffer.clear();
		this.read(offset, bytes);
		return buffer.getLong();
	}

	public short readShort(int offset) throws DatabaseException {
		logger.debug("readShort");
		final ByteBuffer buffer = ByteBuffer.allocate(Sizes.SHORTVAL_LEN);
		final byte[] bytes = buffer.array();
		buffer.clear();
		this.read(offset, bytes);
		return buffer.getShort();
	}

	public String readStringWithLength(int offset) throws DatabaseException {
		logger.debug("readStringWithLength");
		byte[] bytes = this.readBytesWithLength(offset);
		return new String(bytes);
	}

	private final static String ERROR_WRITE = "ERROR_WRITE";

	private final static int FIRST_PAGE_INDEX = 0;

	public void write(int offset, byte[] data) throws DatabaseException {
		logger.debug("write");
		if (this.enabled) {
			try {
				List<IDataPage> pages = this.getDataPages(offset, data);
				this.setExclusiveLocks(pages);
				IDataPage firstPage = pages.get(FIRST_PAGE_INDEX);
				IDataPage lastPage = pages.get(pages.size() - 1);
				firstPage.fillBytesFromUnderlyingHeap();
				lastPage.fillBytesFromUnderlyingHeap();
				Iterator<IDataPage> i = pages.iterator();
				while (i.hasNext()) {
					final byte[] buffer = new byte[DataPage.getDataPageSize()];
					IDataPage page = i.next();
					page.writeBytes(offset, data);
				}
			} catch (TransactionStoreException ex) {
				throw new DatabaseException(TransactionCapableHeap.class, ERROR_WRITE, ex);
			}
		} else {
			this.heapImpl.write(offset, data);
		}
	}

	public void writeBoolean(int offset, boolean data) throws DatabaseException {
		logger.debug("writeBoolean");
		this.writeByte(offset, (byte) ((data) ? 1 : 0));
	}

	public void writeByte(int offset, byte data) throws DatabaseException {
		logger.debug("writeByte");
		final byte[] buffer = new byte[SINGLE_BYTE_LEN];
		this.write(offset, buffer);
	}

	public void writeBytesWithLength(int offset, byte[] data) throws DatabaseException {
		logger.debug("writeBytesWithLength");
		this.writeInteger(offset, data.length);
		this.write(offset + Sizes.INTVAL_LEN, data);
	}

	public void writeDouble(int offset, double data) throws DatabaseException {
		logger.debug("writeDouble");
		final ByteBuffer buffer = ByteBuffer.allocate(Sizes.DOUBLEVAL_LEN);
		buffer.clear();
		buffer.putDouble(data);
		this.write(offset, buffer.array());
	}

	public void writeInteger(int offset, int data) throws DatabaseException {
		logger.debug("writeInteger");
		final ByteBuffer buffer = ByteBuffer.allocate(Sizes.INTVAL_LEN);
		buffer.clear();
		buffer.putInt(data);
		this.write(offset, buffer.array());
	}

	public void writeLong(int offset, long data) throws DatabaseException {
		logger.debug("writeLong");
		final ByteBuffer buffer = ByteBuffer.allocate(Sizes.LONGVAL_LEN);
		buffer.clear();
		buffer.putLong(data);
		this.write(offset, buffer.array());
	}

	public void writeShort(int offset, short data) throws DatabaseException {
		logger.debug("writeShort");
		final ByteBuffer buffer = ByteBuffer.allocate(Sizes.SHORTVAL_LEN);
		buffer.clear();
		buffer.putShort(data);
		this.write(offset, buffer.array());
	}

	public void writeStringWithLength(int offset, String data) throws DatabaseException {
		logger.debug("writeStringWithLength");
		this.writeBytesWithLength(offset, data.getBytes());
	}

	public void commitChanges() throws TransactionStoreException {
		logger.debug("commitChanges");
		Iterator<IDataPage> i = this.pages.iterator();
		while (i.hasNext()) {
			IDataPage page = i.next();
			page.commitChanges();
		}
	}

	public boolean isTransactionCapable() {
		return true;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public IHeapExtension addExtension(IHeapExtension extension) {
		return this.heapImpl.addExtension(extension);
	}

	public IHeapExtension removeExtension(ExtensionType typeExtension) {
		return this.heapImpl.removeExtension(typeExtension);
	}

	public Set<IHeapExtension> getExtensions() {
		return this.heapImpl.getExtensions();
	}

	public IHeapExtension getExtension(ExtensionType typeExtension) {
		return this.heapImpl.getExtension(typeExtension);
	}

	public IHeapTransactionExtension getTransactionExtension() {
		return this.heapImpl.getTransactionExtension();
	}

	public boolean hasExtension(ExtensionType typeExtension) {
		return this.heapImpl.hasExtension(typeExtension);
	}
}