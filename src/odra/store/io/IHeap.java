package odra.store.io;

import java.util.Set;

import odra.db.DatabaseException;
import odra.store.io.IHeapExtension.ExtensionType;
import odra.transactions.store.IHeapTransactionExtension;

/**
 * This interface declares basic heap-related mechanisms, such us: the buffer representing I/O operations, heap's
 * beginning, its size, etc.
 * 
 * @author raist
 * 
 * edek: encapsulating access to the raw byte buffer
 */

public interface IHeap {

	int getUserSpaceLength();

	int getStartOffset();

	/**
	 * Get size of the heap
	 * 
	 * @return size of the heap
	 */
	int getSize();

	void open() throws DatabaseException;

	/**
	 * Close the heap. When overriden, should close the underlying memory structure (e.g. a file)
	 */
	void close();

	void read(int offset, byte[] buf) throws DatabaseException;

	void write(int offset, byte[] data) throws DatabaseException;

	byte[] readBytesWithLength(int offset) throws DatabaseException;

	void writeBytesWithLength(int offset, byte[] data) throws DatabaseException;

	byte readByte(int offset) throws DatabaseException;

	void writeByte(int offset, byte data) throws DatabaseException;

	short readShort(int offset) throws DatabaseException;

	void writeShort(int offset, short data) throws DatabaseException;

	int readInteger(int offset) throws DatabaseException;

	void writeInteger(int offset, int data) throws DatabaseException;

	double readDouble(int offset) throws DatabaseException;

	void writeDouble(int offset, double data) throws DatabaseException;

	long readLong(int offset) throws DatabaseException;

	void writeLong(int offset, long data) throws DatabaseException;

	boolean readBoolean(int offset) throws DatabaseException;

	void writeBoolean(int offset, boolean data) throws DatabaseException;

	String readStringWithLength(int offset) throws DatabaseException;

	void writeStringWithLength(int offset, String data) throws DatabaseException;

	/**
	 * Adds an implementation of an {@link IHeap} extension, such as {@link IHeapTransactionExtension}.<br>
	 * 
	 * NOTE: THERE MAY BE ONLY ONE IMPLEMENTATION OF THE GIVEN EXTENSION ATTACHED TO THE PARTICULAR {@link IHeap}
	 * instance.
	 * 
	 * @param extension
	 * @return added {@link IHeapExtension} implementation
	 */
	IHeapExtension addExtension(IHeapExtension extension);

	/**
	 * Remove an implementation of an {@link IHeap} extension.<br>
	 * 
	 * NOTE: THERE MAY BE ONLY ONE IMPLEMENTATION OF THE GIVEN EXTENSION ATTACHED TO THE PARTICULAR {@link IHeap}
	 * instance.
	 * 
	 * @param typeExtension
	 * @return deleted {@link IHeapExtension} implementation
	 */
	IHeapExtension removeExtension(ExtensionType typeExtension);

	/**
	 * Returns the extenstions attached to the present {@link IHeap} instance.
	 * 
	 * @return the set of {@link IHeapExtension} implementations attached to the given {@link IHeap} instance.
	 */
	Set<IHeapExtension> getExtensions();

	/**
	 * Returns an {@link IHeapExtension} of the given type.
	 * 
	 * @param typeExtension
	 * @return the {@link IHeapExtension} implementation of the given type.
	 */
	IHeapExtension getExtension(ExtensionType typeExtension);

	/**
	 * Returns an {@link IHeapTransactionExtension} associated with the given {@link IHeap} instance.
	 * 
	 * @return the {@link IHeapTransactionExtension} attached to the given {@link IHeap} implementation instance.
	 */
	IHeapTransactionExtension getTransactionExtension();

	/**
	 * Checks whether the present instance has been attached the given {@link ExtensionType} implementation.
	 * 
	 * @param typeExtension
	 * @return
	 */
	boolean hasExtension(ExtensionType typeExtension);
}