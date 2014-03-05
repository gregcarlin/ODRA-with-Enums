package odra.store.persistence;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import odra.db.DatabaseException;
import odra.system.config.ConfigDebug;

/**
 * A class representing memory-mapped files.
 * 
 * @author raist
 */

public class MappedFile {
	protected String path;
	protected RandomAccessFile file;
	protected FileChannel channel;
	protected MappedByteBuffer buffer;
	protected int fsize;
	
	/**
	 * Initializes a new MappedFile object using a file path
	 */
	public MappedFile(String filePath) {
		path = filePath;
	}

	public String getPath() {
		return path;
	}

	/**
	 * Opens the file for read/write operations
	 */
	public void open() throws DatabaseException {	
		if (isMapped())
			throw new DatabaseException("File already opened");

		File fi = new File(path);
		if (!fi.exists() || !fi.canRead() || !fi.canWrite() || fi.isDirectory())
			throw new DatabaseException("Cannot open database file '" + path + "'");

		try {
			file = new RandomAccessFile(path, "rw");
			channel = file.getChannel();

			fsize = (int) channel.size();
			buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fsize);

			channel.close();
		}
		catch (Exception ex) {
			throw new DatabaseException(ex.getMessage());
		}
	}

	/**
	 * @return size of the datafile
	 */
	public int getSize() {
		return fsize;
	}

	/**
	 * Closes the file
	 */
	public void close() {
		buffer = null;
	}

	/**
	 * @return buffer representing the content of the file
	 */
	public MappedByteBuffer getBuffer() throws DatabaseException {
		if (!isMapped())
			throw new DatabaseException("File not opened");
				
		return buffer;
	}

	/**
	 * @return true if the file is open
	 */
	public boolean isMapped() {
		return buffer != null;
	}
	
	/**
	 * Formats the datafile (sets the proper size, installs the file header, etc.)
	 */
	public synchronized void format(int size) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert size >= 1024 && size <= Integer.MAX_VALUE : "Data files cannot be smaller than 1024B and bigger than " + Integer.MAX_VALUE;

		try {
			// file setup
			File f = new File(path);
			if (f.length() > size)
				f.delete();

			file = new RandomAccessFile(path, "rw");

			channel = file.getChannel();
			ByteBuffer newBuf = ByteBuffer.allocate(size);
			channel.write(newBuf);
			channel.close();
			
		}
		catch (IOException ex) {
			throw new DatabaseException(ex.getMessage());
		}
	}

	
}
