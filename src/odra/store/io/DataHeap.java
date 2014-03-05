package odra.store.io;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.system.Sizes;
import odra.system.config.ConfigDebug;

/**
 * A class containing definitions of I/O operations performed on byte buffers.
 * 
 * @author raist
 */
public abstract class DataHeap extends AbstractHeap {

	/**
	 * buffer must be initialized before or when open() method is called
	 */
	protected ByteBuffer buffer;

	/**
	 * size must be initialized before or when open() method is called
	 */
	protected int size;

	/**
	 * Reads length bytes from the datafile starting at a specific location
	 */
	public void read(int offset, byte[] buf) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - buf.length : offset;
		}
		buffer.position(offset);
		buffer.get(buf);
	}

	/**
	 * Writes a buffer at a specific location of the underlaying memory structure
	 */
	public void write(int offset, byte[] data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size - data.length : offset;
		}
		buffer.position(offset);
		buffer.put(data, 0, data.length);
	}

	/**
	 * Reads an array of byte values from a specific location of the memory structure. |datalen:4|data:n|
	 */
	public byte[] readBytesWithLength(int offset) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size - Sizes.INTVAL_LEN - buffer.getInt(offset) : offset;
		}
		byte[] buf = new byte[buffer.getInt(offset)];
		read(offset + Sizes.INTVAL_LEN, buf);
		return buf;
	}

	/**
	 * Writes an array of bytes and its length at a specific location of the memory structure. |datalen:4|data:n|
	 */
	public void writeBytesWithLength(int offset, byte[] data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size - Sizes.INTVAL_LEN - data.length : offset;
		}
		buffer.putInt(offset, data.length);
		write(offset + Sizes.INTVAL_LEN, data);
	}

	/**
	 * Reads a byte value from a specific location of the underlaying memory structure
	 */
	public byte readByte(int offset) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size : offset;
		}
		return buffer.get(offset);
	}

	/**
	 * Write a byte value at a specific location of the datafile
	 */
	public final void writeByte(int offset, byte data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size : offset;
		}
		buffer.put(offset, data);
	}

	/**
	 * Reads a short value from a specific location of the datafile
	 */
	public final short readShort(int offset) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size - Sizes.SHORTVAL_LEN : offset;
		}
		return buffer.getShort(offset);
	}

	/**
	 * Writes a short value at a specific location of the datafile
	 */
	public final void writeShort(int offset, short data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size - Sizes.SHORTVAL_LEN : offset;
		}
		buffer.putShort(offset, data);
	}

	/**
	 * Reads an integer value from a specific location of the datafile
	 */
	public final int readInteger(int offset) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - Sizes.INTVAL_LEN : offset;
		}
		return buffer.getInt(offset);
	}

	/**
	 * Writes an integer value at a specific location of the datafile
	 */
	public final void writeInteger(int offset, int data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - Sizes.INTVAL_LEN : offset;
		}
		buffer.putInt(offset, data);
	}

	/**
	 * Reads a double value from a specific location of the datafile
	 */
	public final double readDouble(int offset) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - Sizes.DOUBLEVAL_LEN : offset;
		}
		return buffer.getDouble(offset);
	}

	/**
	 * Writes a double value at a specific location of the datafile
	 */
	public final void writeDouble(int offset, double data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - Sizes.DOUBLEVAL_LEN : offset;
		}
		buffer.putDouble(offset, data);
	}

	/**
	 * Reads a long value from a specific location of the datafile
	 */
	public final long readLong(int offset) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - Sizes.LONGVAL_LEN : offset;
		}
		return buffer.getLong(offset);
	}

	/**
	 * Writes a long value at a specific location of the datafile
	 */
	public final void writeLong(int offset, long data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size - Sizes.LONGVAL_LEN : offset;
		}
		buffer.putLong(offset, data);
	}

	/**
	 * Writes a boolean value at a specific location of the datafile
	 */
	public final void writeBoolean(int offset, boolean data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= size - Sizes.BOOLEAN_LEN : offset;
		}
		writeByte(offset, (byte) ((data) ? 1 : 0));
	}

	/**
	 * Reads a boolean value from a specific location of the datafile
	 */
	public final boolean readBoolean(int offset) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - Sizes.BOOLEAN_LEN : offset;
		}
		return readByte(offset) == 0 ? false : true;
	}

	/**
	 * reads a string value from a specific location of the datafile. |strlen:4|string:n|
	 */
	public final String readStringWithLength(int offset) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - Sizes.INTVAL_LEN : offset;
		}
		byte[] buf = new byte[this.buffer.getInt(offset)];
		read(offset + Sizes.INTVAL_LEN, buf);
		return new String(buf);
	}

	/**
	 * writes a string value and its length at a specific location of the datafile. |strlen:4|string:n|
	 */
	public final void writeStringWithLength(int offset, String data) throws DatabaseException {
		if (ConfigDebug.ASSERTS) {
			assert offset >= 0 && offset <= this.size - data.getBytes().length - Sizes.INTVAL_LEN : offset;
		}
		byte[] bytes = data.getBytes();
		buffer.putInt(offset, bytes.length);
		write(offset + Sizes.INTVAL_LEN, bytes);
	}

	public int getUserSpaceLength() {
		return getSize() - getStartOffset();
	}
}