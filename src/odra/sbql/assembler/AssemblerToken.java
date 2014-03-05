package odra.sbql.assembler;

/**
 * This class represents tokens returned by the AssemblerLexer class.
 * @author raist, tkowals
 *
 */

public class AssemblerToken {
	public byte kind;
	public String value;
	
	public AssemblerToken(byte kind, String value) {
		this.kind = kind;
		this.value = value;
	}

	public static String spell(byte kind) {
		return kindstr[kind];
	}

	public final static byte
		ERROR = 0,
		EOF = 1,
		EOL = 2,
		OPERATION = 3,
		INTEGER_LITERAL = 4,
		LABEL = 5;
	
	public static String[] kindstr = {
			"<error>",
			"<eof>",
			"<eol>",
			"<operation>",
			"<integer_literal>",
			"<label>"
	};
}
