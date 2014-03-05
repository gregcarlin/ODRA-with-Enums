package odra.store.persistence;

import java.io.*;
import java.nio.*;

import odra.db.DatabaseException;
import odra.store.io.DataHeap;
import odra.store.io.IHeap;
import odra.system.*;

/**
 * Data files store database content. Each data file consists of two main components:
 * header and user's space.
 * 
 * @author raist, tkowals
 */

public class DataFileHeap extends DataHeap {
	
	MappedFile mappedfile;
	
	int header_extension; //used to store additonal information before user space 

	/**
	 * Initializes the object with a path of a data file
	 */
	public DataFileHeap(String path) throws DatabaseException {
		this(path, 0);
	}

	public DataFileHeap(String path, int header_extension) throws DatabaseException {
		mappedfile = new MappedFile(path);
		this.header_extension = header_extension;
	}
		
	public ByteBuffer getBuffer() throws DatabaseException {
		return buffer;
	}

	public int getSize() {
		return mappedfile.getSize();
	}
	
	/**
	 * @return offset of the first byte after the header of the file
	 */
	public int getStartOffset() {
		return DF_HEADER_LENGTH + header_extension;
	}
	
	/**
	 * Opens the datafile and checks its header
	 */
	public void open() throws DatabaseException {
		mappedfile.open();
		size = mappedfile.getSize();
		buffer = mappedfile.getBuffer();
		
		if (buffer.capacity() < DF_HEADER_LENGTH ||
				buffer.get(MAGIC1_POS) != (byte) 'O' ||
				buffer.get(MAGIC2_POS) != (byte) 'D' ||
				buffer.get(MAGIC3_POS) != (byte) 'B' ||
				buffer.get(MAGIC4_POS) != (byte) 'F')
			throw new DatabaseException("Invalid format of the database file");
	}
	
	/**
	 * Closes the datafile 
	 */
	public void close() {
		mappedfile.close();
	}
	
	/**
	 * Formats the datafile (sets the proper size, installs the file header, etc.)
	 */
	public synchronized void format(int size) throws DatabaseException {

		mappedfile.format(size);
		mappedfile.open();

		mappedfile.getBuffer()
			.put(MAGIC1_POS, (byte) 'O')
			.put(MAGIC2_POS, (byte) 'D')
			.put(MAGIC3_POS, (byte) 'B')
			.put(MAGIC4_POS, (byte) 'F')
			.putInt(DF_SIZE_POS, size);
		
		mappedfile.close();

	}

	/**
	 * @return true if the file is open
	 */
	public boolean isMapped() {
		return mappedfile.isMapped();
	}
	
	// positions of various components of the data file header
	private final static int MAGIC1_POS = 0; // O
	private final static int MAGIC2_POS = 1; // D
	private final static int MAGIC3_POS = 2; // B
	private final static int MAGIC4_POS = 3; // F
	private final static int DF_SIZE_POS = 4; // Datafile size
	
	public final static int DF_HEADER_LENGTH = DF_SIZE_POS + Sizes.INTVAL_LEN; // Datafile header length

}
