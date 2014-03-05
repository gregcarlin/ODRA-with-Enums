package odra.sbql.ast.terminals;

import java.util.Hashtable;

/**
 * AST node for simple operators. Objects of this class are used
 * together with SimpleUnaryExpression and SimpleBinaryExpression
 * 
 * @author raist
 */

public class Operator extends Terminal {
	private int opcode;
		
	private Operator(int o) {
		opcode = o;
	}
	
	public String spell() {
		return opstr[opcode];
	}
	
	public int getAsInt() {
		return opcode;
	}
	
	public static Operator get(int opcode) {
		return opers.get(opcode);
	}
	
	public boolean equals(Object O) {
		return ((O instanceof Operator) && (opcode == ((Operator) O).opcode));
	}
	
	private final static String[] opstr = {
		"+",
		"-",
		"*",
		"/",
		"=",
		">",
		"<",
		">=",
		"<=",
		"or",
		"and",
		"not",
		"<>",
		":=",
		"%",
		"~~",
		"~!"
	};
	
	
	public final static int PLUS = 0;
	public final static int MINUS = 1;
	public final static int MULTIPLY = 2;
	public final static int DIVIDE = 3;
	public final static int EQUALS = 4;
	public final static int GREATER = 5;
	public final static int LOWER = 6;
	public final static int GREATEREQUALS = 7;
	public final static int LOWEREQUALS = 8;
	public final static int OR = 9;
	public final static int AND = 10;
	public final static int NOT = 11;
	public final static int DIFFERENT = 12;
	public final static int ASSIGN = 13;
	public final static int MODULO = 14;
	public final static int MATCH_STRING = 15;
	public final static int NOT_MATCH_STRING = 16;

	
	public static Operator opPlus = new Operator(PLUS);
	public static Operator opMinus = new Operator(MINUS);
	public static Operator opMultiply = new Operator(MULTIPLY);
	public static Operator opDivide = new Operator(DIVIDE);
	public static Operator opEquals = new Operator(EQUALS);
	public static Operator opGreater = new Operator(GREATER);
	public static Operator opLower = new Operator(LOWER);
	public static Operator opGreaterEquals = new Operator(GREATEREQUALS);
	public static Operator opLowerEquals = new Operator(LOWEREQUALS);
	public static Operator opOr = new Operator(OR);
	public static Operator opAnd = new Operator(AND);
	public static Operator opNot = new Operator(NOT);
	public static Operator opDifferent = new Operator(DIFFERENT);
	public static Operator opAssign = new Operator(ASSIGN);
	public static Operator opModulo = new Operator(MODULO);
	public static Operator opMatchString = new Operator(MATCH_STRING);
	public static Operator opNotMatchString = new Operator(NOT_MATCH_STRING);
	
	private static Hashtable<Integer, Operator> opers = new Hashtable<Integer, Operator>();  
	
	static{
		opers.put(PLUS, opPlus);
		opers.put(MINUS, opMinus);
		opers.put(MULTIPLY, opMultiply);
		opers.put(DIVIDE,opDivide);
		opers.put(EQUALS,opEquals);
		opers.put(GREATER,opGreater);
		opers.put(LOWER,opLower);
		opers.put(GREATEREQUALS,opGreaterEquals);
		opers.put(LOWEREQUALS,opLowerEquals);
		opers.put(OR, opOr);
		opers.put(AND, opAnd);
		opers.put(NOT, opNot);
		opers.put(DIFFERENT, opDifferent);
		opers.put(ASSIGN, opAssign);
		opers.put(MODULO, opModulo);
		opers.put(MATCH_STRING, opMatchString);
		opers.put(NOT_MATCH_STRING, opNotMatchString);
	}
}
