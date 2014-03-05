package odra.sbql.emiter;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import odra.system.Sizes;
import odra.system.config.ConfigDebug;

public class ConstantPool {
	private Vector<Constant> pool = new Vector();

	// this is used to prevent repetitive constants
	private Hashtable<Constant, Integer> ids = new Hashtable();
	
	public ConstantPool() {
	}
	
	public void clean(){
		pool.clear();
		ids.clear();
	}
	
	// initializes the pool using previously serialized data
	public ConstantPool(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		byte type;
		int len, offset;
		byte[] rawstrval;
		
		int nconst = buffer.getInt();
		for (int i = 0; i < nconst; i++) {
			// read the header
			buffer.position(Sizes.INTVAL_LEN + (Sizes.INTVAL_LEN + Sizes.BYTEVAL_LEN) * i);
			type = buffer.get();
			offset = buffer.getInt();

			// read the body
			buffer.position(offset);
			switch (type) {
				case Constant.STRING_CONSTANT:
					len = buffer.getInt();
					rawstrval = new byte[len];
					buffer.get(rawstrval);					
					addString(new String(rawstrval));
					break;
					
				case Constant.BOOLEAN_CONSTANT:
					len = buffer.get();
					addBoolean(len == 1 ? true : false);
					break;

				case Constant.DOUBLE_CONSTANT:
					addDouble(buffer.getDouble());
					break;
					
				case Constant.INTEGER_CONSTANT:
					addInteger(buffer.getInt());
					break;
					
				case Constant.DATE_CONSTANT:
					addDate(new Date(buffer.getLong()));
					break;
					
				default:
					assert false : "unknwon constant";
			}
			
			ids.put(pool.lastElement(), pool.size() - 1);
		}
	}

	// adds a new constant to the pool and returns its id
	private  int addConstant(Constant cnst) {
		if (ConfigDebug.ASSERTS) assert pool.size() < Integer.MAX_VALUE : "no free space";		
	
		if (!ids.containsKey(cnst)) {
			pool.addElement(cnst);
			ids.put(cnst, pool.size() - 1);
		}

		return ids.get(cnst);
	}

	// adds a new boolean constant to the pool
	public final int addBoolean(boolean val) {		
		return addConstant(new Constant(val));
	}
	
	// adds a new double constant to the pool
	public final int addDouble(double val) {
		return addConstant(new Constant(val));
	}
	
	// adds a new string constant to the pool
	public final int addString(String val) {	
		return addConstant(new Constant(val));
	}
	
	//adds a new string constant to the pool
	public final int addDate(Date val) {	
		return addConstant(new Constant(val));
	}
	
	// adds a new integer constant to the pool
	public final int addInteger(int val) {
		return addConstant(new Constant(val));
	}

	// finds the "index"-th double constant
	public final double lookUpDouble(int index) {
		return pool.get(index).getDouble();
	}

	// finds the "index"-th string constant
	public final String lookUpString(int index) {
		return pool.get(index).getString();
	}
	
	// finds the "index"-th integer constant
	public final int lookUpInteger(int index) {
		return pool.get(index).getInteger();
	}

	// finds the "index"-th boolean constant
	public final boolean lookUpBoolean(int index) {
		return pool.get(index).getBoolean();
	}
	
	// finds the "index"-th date constant
	public final Date lookUpDate(int index) {
		return pool.get(index).getDate();
	}
	
	// serializes the constant pool
	public byte[] getAsBytes() {		
		// compute the length of the final array
		// constant length = 1 byte of kind + 4 bytes of offset + raw length
		int length = Sizes.INTVAL_LEN;
		for (Constant c : pool)
			length += c.getRawLength() + Sizes.BYTEVAL_LEN + Sizes.INTVAL_LEN;
		
		// write the number of values
		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.putInt(pool.size());

		// where we start to write constant values
		int datastart = Sizes.INTVAL_LEN + (Sizes.INTVAL_LEN + Sizes.BYTEVAL_LEN) * pool.size();

		// serialize the pool
		int i = 0;
		for (Constant c : pool) {
			// write the header (kind + offset)
			buffer.position(Sizes.INTVAL_LEN + (Sizes.INTVAL_LEN + Sizes.BYTEVAL_LEN) * i);
			buffer.put(c.getType());
			buffer.putInt(datastart);

			// write the value
			buffer.position(datastart);

			switch (c.getType()) {
				case Constant.STRING_CONSTANT: {
					byte[] str = c.getString().getBytes();
					
					buffer.putInt(str.length);
					buffer.put(str);
					
					break;
				}

				case Constant.INTEGER_CONSTANT:
					buffer.putInt(c.getInteger());
					break;
					
				case Constant.DOUBLE_CONSTANT:
					buffer.putDouble(c.getDouble());
					break;
					
				case Constant.BOOLEAN_CONSTANT:
					buffer.put((byte) (c.getBoolean() ? 1 : 0));
					break;
					
				case Constant.DATE_CONSTANT:
					buffer.putLong(c.getDate().getTime());
					break;
			}

			datastart = buffer.position();
			
			i++;
		}

		return buffer.array();
	}

	// used for debugging purposes
	public String getAsString() {
		Constant[] cons = pool.toArray(new Constant[pool.size()]);

		String str = "";
		
		for (int i = 0; i < cons.length; i++) {
			switch (cons[i].getType()) {
				case Constant.BOOLEAN_CONSTANT:
					str += "\t#" + i + " : " + cons[i].getBoolean() + " (boolean)\n";
					break;
					
				case Constant.INTEGER_CONSTANT:
					str += "\t#" + i + " : " + cons[i].getInteger() + " (integer)\n";
					break;
					
				case Constant.STRING_CONSTANT:
					str += "\t#" + i + " : " + cons[i].getString() + " (string)\n";
					break;
					
				case Constant.DOUBLE_CONSTANT:
					str += "\t#" + i + " : " + cons[i].getDouble() + " (double)\n";
					break;

				default:
					assert false : "unknown constant kind";
			}
		}

		return str;
	}
}

