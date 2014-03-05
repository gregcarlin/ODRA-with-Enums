package odra.network.transport;

import java.nio.ByteBuffer;

import odra.system.Sizes;

/**
 * This is a block of memory (array of bytes) with the capability for changing its size when it
 * becomes full.
 * 
 * @author raist
 */

public class AutoextendableBuffer
{
	private ByteBuffer buffer;

	public AutoextendableBuffer()
	{
		buffer = ByteBuffer.allocate(CHUNK_LENGTH);
	}

	/**
	 * Checks if the buffer is big enough. If not, the buffer is expanded.
	 * 
	 * @param min
	 *            minimum desired size of the buffer
	 * @param extend
	 *            if the buffer is not big enough, it is expanded by 'extend' bytes
	 */
	private final void checkBuffer(int min, int extend)
	{
		if (buffer.remaining() < min)
		{
			if (extend < min)
				extend = min;

			ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + extend);
			buffer.flip();

			newBuffer.put(buffer);

			buffer = newBuffer;
		}
	}

	/**
	 * Writes some data into the byte buffer
	 * 
	 * @param val
	 *            array of bytes that which should be written
	 */
	public final void put(byte[] val)
	{
		try
		{
			checkBuffer(val.length, CHUNK_LENGTH);
			buffer.put(val);
		}
		catch (RuntimeException ex)
		{
			System.err.println("Buffer error (" + ex.getMessage() + ")");
		}
	}

	/**
	 * Writes a byte into the byte buffer
	 * 
	 * @param val
	 *            the byte that is to be written
	 */
	public final void put(byte val)
	{
		checkBuffer(Sizes.BYTEVAL_LEN, CHUNK_LENGTH);

		buffer.put(val);
	}

	/**
	 * Writes an integer value into the byte buffer
	 * 
	 * @param val
	 *            a value that should be written
	 */
	public final void putInt(int val)
	{
		checkBuffer(Sizes.INTVAL_LEN, CHUNK_LENGTH);

		buffer.putInt(val);
	}
	
	public final void putLong(long val)
	{
		checkBuffer(Sizes.LONGVAL_LEN, CHUNK_LENGTH);

		buffer.putLong(val);
	}
	
	public final void putDouble(double d)
	{
		checkBuffer(Sizes.DOUBLEVAL_LEN, CHUNK_LENGTH);
		
		buffer.putDouble(d);		
	}
	

	/**
	 * Returns the whole buffer as an array of bytes
	 * 
	 * @return content of the buffer
	 */
	public final byte[] getBytes()
	{
		buffer.flip();

		byte[] buf = new byte[buffer.limit()];
		buffer.get(buf);

		return buf;
	}

	public void rewind()
	{
		this.buffer.rewind();
	}

	public void flip()
	{
		this.buffer.flip();

	}

	public void mark()
	{
		this.buffer.mark();
	}

	public void reset()
	{
		this.buffer.reset();
	}
	
	public int position()
	{
		return buffer.position();
	}
	
	public void position(int p)
	{
		buffer.position(p);
	}


	private final static int CHUNK_LENGTH = 8192;
}
