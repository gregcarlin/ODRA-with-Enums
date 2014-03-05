package odra.store.io;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.store.persistence.DataFileHeap;
import odra.store.transience.DataMemoryBlockHeap;
import odra.system.Sizes;

public abstract class AutoExpandableHeap extends AbstractHeap {

	Vector<IHeap> heaps;

	Vector<Integer> startOffsets;

	String path_prefix;

	int heap_kind; // DATAFILE_HEAP or DATAMEMORYBLOCK_HEAP;

	protected int init_size; // initial heap size

	/**
	 * Constructor of transient auto-expandable heap
	 */
	protected AutoExpandableHeap(int size) {
		this.heap_kind = DATAMEMORYBLOCK_HEAP;
		this.init_size = size;
		heaps = new Vector<IHeap>();
		startOffsets = new Vector<Integer>();
		heaps.add(new DataMemoryBlockHeap(init_size));
		startOffsets.add(calculateStartOffset(0));
	}

	/**
	 * Constructor of persistant auto-expandable heap
	 */
	protected AutoExpandableHeap(String path_prefix) {
		this.heap_kind = DATAFILE_HEAP;
		this.path_prefix = path_prefix;
	}

	public Collection<IHeap> getHeapsCollection() {
		return heaps;
	}

	public void open() throws DatabaseException {
		heaps = new Vector<IHeap>();
		startOffsets = new Vector<Integer>();

		if (heap_kind == DATAFILE_HEAP) {
			DataFileHeap startFileHeap = new DataFileHeap(getHeapFileName(0), Sizes.INTVAL_LEN);
			startFileHeap.open();
			int heap_count = startFileHeap.readInteger(HEAPSCOUNTER_POS);
			this.init_size = startFileHeap.getSize();
			startFileHeap.close();

			heaps.add(startFileHeap);
			startOffsets.add(calculateStartOffset(0));
			for (int i = 1; i < heap_count; i++) {
				heaps.add(new DataFileHeap(getHeapFileName(i)));
				startOffsets.add(calculateStartOffset(i));
			}

		} else throw new DatabaseException("Heap kind unsupported by auto-expandable heap");

		for (IHeap heap : heaps)
			heap.open();

	}

	public void close() {
		for (IHeap heap : heaps)
			heap.close();

		heaps.clear();
	}

	/**
	 * Expands the database. Works only when heap is opened.
	 * 
	 * @throws DatabaseException
	 */
	public IHeap expand() throws DatabaseException {
		int heap_count = heaps.size();

		if (heap_kind == DATAFILE_HEAP) {
			DataFileHeap newFileHeap = new DataFileHeap(getHeapFileName(heap_count));
			newFileHeap.format(getHeapSize(heap_count));
			heaps.add(newFileHeap);

			heaps.firstElement().writeInteger(HEAPSCOUNTER_POS, heap_count + 1);

		} else if (heap_kind == DATAMEMORYBLOCK_HEAP) {
			heaps.add(new DataMemoryBlockHeap(getHeapSize(heap_count)));
		} else throw new DatabaseException("Heap kind unsupported by auto-expandable heap");

		heaps.lastElement().open();
		startOffsets.add(calculateStartOffset(heap_count));

		return heaps.lastElement();
	}

	/**
	 * Formats the first datafile (sets the proper size, installs the file header, etc.)
	 */
	public synchronized void format(int size) throws DatabaseException {
		DataFileHeap startFileHeap = new DataFileHeap(getHeapFileName(0), Sizes.INTVAL_LEN);

		startFileHeap.format(size);
		startFileHeap.open();
		startFileHeap.writeInteger(HEAPSCOUNTER_POS, 1);
		startFileHeap.close();

		this.init_size = size;
	}

	public ByteBuffer getBuffer() throws DatabaseException {
		throw new DatabaseException("Auto-expandable heap buffer cannot be accessed directly");
	}

	public int getSize() {
		int summary_size = 0;
		for (IHeap heap : heaps)
			summary_size += heap.getSize();
		return 0;
	}

	public int getStartOffset() {
		return heaps.firstElement().getStartOffset();
	}

	public int getUserSpaceLength() {
		int summary_header = 0;
		for (IHeap heap : heaps)
			summary_header += heap.getStartOffset();
		return getSize() - summary_header;
	}

	public int getStartOffset(int heap_num) {
		return startOffsets.elementAt(heap_num);
	}

	public int relativeOffset(int heap_num, int offset) {
		return offset - getStartOffset(heap_num);
	}

	public void read(int offset, byte[] buf) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).read(relativeOffset(heap_num, offset), buf);
	}

	public boolean readBoolean(int offset) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		return heaps.elementAt(heap_num).readBoolean(relativeOffset(heap_num, offset));
	}

	public byte readByte(int offset) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		return heaps.elementAt(heap_num).readByte(relativeOffset(heap_num, offset));
	}

	public byte[] readBytesWithLength(int offset) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		return heaps.elementAt(heap_num).readBytesWithLength(relativeOffset(heap_num, offset));
	}

	public double readDouble(int offset) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		return heaps.elementAt(heap_num).readDouble(relativeOffset(heap_num, offset));
	}

	public long readLong(int offset) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		return heaps.elementAt(heap_num).readLong(relativeOffset(heap_num, offset));
	}

	public int readInteger(int offset) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		return heaps.elementAt(heap_num).readInteger(relativeOffset(heap_num, offset));
	}

	public short readShort(int offset) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		return heaps.elementAt(heap_num).readShort(relativeOffset(heap_num, offset));
	}

	public String readStringWithLength(int offset) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		return heaps.elementAt(heap_num).readStringWithLength(relativeOffset(heap_num, offset));
	}

	public void write(int offset, byte[] data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).write(relativeOffset(heap_num, offset), data);
	}

	public void writeBoolean(int offset, boolean data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).writeBoolean(relativeOffset(heap_num, offset), data);
	}

	public void writeByte(int offset, byte data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).writeByte(relativeOffset(heap_num, offset), data);
	}

	public void writeBytesWithLength(int offset, byte[] data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).writeBytesWithLength(relativeOffset(heap_num, offset), data);
	}

	public void writeDouble(int offset, double data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).writeDouble(relativeOffset(heap_num, offset), data);
	}

	public void writeLong(int offset, long data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).writeLong(relativeOffset(heap_num, offset), data);
	}

	public void writeInteger(int offset, int data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).writeInteger(relativeOffset(heap_num, offset), data);
	}

	public void writeShort(int offset, short data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).writeShort(relativeOffset(heap_num, offset), data);
	}

	public void writeStringWithLength(int offset, String data) throws DatabaseException {
		int heap_num = findHeapNum(offset);
		heaps.elementAt(heap_num).writeStringWithLength(relativeOffset(heap_num, offset), data);
	}

	protected abstract int getHeapSize(int heap_num);

	protected abstract int calculateStartOffset(int heap_num);

	public abstract int findHeapNum(int offset);

	private String getHeapFileName(int heap_num) {
		return path_prefix + "_" + heap_num + ".dbf";
	}

	public static int DATAFILE_HEAP = 1;

	public static int DATAMEMORYBLOCK_HEAP = 2;

	private static int HEAPSCOUNTER_POS = DataFileHeap.DF_HEADER_LENGTH;

}
