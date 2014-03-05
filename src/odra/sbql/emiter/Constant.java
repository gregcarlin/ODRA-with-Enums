package odra.sbql.emiter;

import java.util.Date;

import odra.system.Sizes;

public class Constant {
	private byte type;
	private Object value;
	
	public Constant(int val) {
		value = val;
		type = INTEGER_CONSTANT;
	}
	
	public Constant(String val) {
		value = val;
		type = STRING_CONSTANT;
	}
	
	public Constant(double val) {
		value = val;
		type = DOUBLE_CONSTANT;
	}
	
	public Constant(boolean val) {
		value = val;
		type = BOOLEAN_CONSTANT;
	}
	
	public Constant(Date val) {
		value = val.getTime();
		type = DATE_CONSTANT;
	}

	public byte getType() {
		return type;
	}
	
	public String getString() {
		return (String) value;
	}
	
	public int getInteger() {
		return (Integer) value;
	}
	
	public double getDouble() {
		return (Double) value;
	}
	
	public boolean getBoolean() {
		return (Boolean) value;
	}
	
	public Date getDate() {
		return new Date((Long)value);
	}
	
	public int getRawLength() {
		switch (type) {
			case STRING_CONSTANT:
				return getString().getBytes().length + Sizes.INTVAL_LEN;
				
			case INTEGER_CONSTANT:
				return Sizes.INTVAL_LEN;
				
			case DOUBLE_CONSTANT:
				return Sizes.DOUBLEVAL_LEN;
				
			case BOOLEAN_CONSTANT:
				return Sizes.BOOLEAN_LEN;
				
			case DATE_CONSTANT:
				return Sizes.LONGVAL_LEN;
		}
		
		assert false : "unknown constant";
		
		return 0;
	}
	
	public boolean equals(Object obj) {
		return obj instanceof Constant && ((Constant) obj).value.equals(value);
	}

	public int hashCode() {
		return value.hashCode();
	}
	
	final static byte STRING_CONSTANT = 1;
	final static byte INTEGER_CONSTANT = 2;
	final static byte DOUBLE_CONSTANT = 3;
	final static byte BOOLEAN_CONSTANT = 4;
	final static byte DATE_CONSTANT = 5;
}
