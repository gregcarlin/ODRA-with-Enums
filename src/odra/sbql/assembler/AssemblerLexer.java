package odra.sbql.assembler;



/**
 * This class represents a simple lexer used by jodra's assembler tool.
 * It is used only during parsing of assembler commands. SBQL has its
 * own lexer/parser generated using jflex/cup.
 * @author raist, tkowals
 *
 */

public class AssemblerLexer {
	private SourceBuffer src;

	private char currChar;
	private StringBuffer currSpell;
	
	public AssemblerLexer(String str) {
		this.src = new SourceBuffer(str);
		this.currChar = src.nextChar();
	}

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	public AssemblerToken nextToken() throws AssemblerSyntaxErrorException {
	    currSpell = new StringBuffer("");
		
	    while (currChar == '!' || currChar == ' ' || 
	    		currChar == '\r' || currChar == '\t')
	    	scanSeparator();
	    
	    byte kind = scanToken();

		return new AssemblerToken(kind, currSpell.toString());
	}

	private byte scanToken() throws AssemblerSyntaxErrorException {
		
		boolean zero = false;
		switch (currChar) {
		    case 'a':  case 'b':  case 'c':  case 'd':  case 'e':
		    case 'f':  case 'g':  case 'h':  case 'i':  case 'j':
		    case 'k':  case 'l':  case 'm':  case 'n':  case 'o':
		    case 'p':  case 'q':  case 'r':  case 's':  case 't':
		    case 'u':  case 'v':  case 'w':  case 'x':  case 'y':
		    case 'z':
		    case 'A':  case 'B':  case 'C':  case 'D':  case 'E':
		    case 'F':  case 'G':  case 'H':  case 'I':  case 'J':
		    case 'K':  case 'L':  case 'M':  case 'N':  case 'O':
		    case 'P':  case 'Q':  case 'R':  case 'S':  case 'T':
		    case 'U':  case 'V':  case 'W':  case 'X':  case 'Y':
		    case 'Z':
		    	accept();
		        while (isLetter(currChar) || isDigit(currChar))
		            accept();
		    	
				return AssemblerToken.OPERATION;
				
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':			    
				while (isDigit(currChar))
			          accept();
				return AssemblerToken.INTEGER_LITERAL;
			
			case '\0':
				return AssemblerToken.EOF;
			
			case '\n':
				accept();
				return AssemblerToken.EOL;
			
			case ':':
				omit();
		        while (isLetter(currChar) || isDigit(currChar))
		            accept();
		    	
				return AssemblerToken.LABEL;
				
		    default:
		        accept();
		        return AssemblerToken.ERROR;
		}
	}
	
	private void scanSeparator() {
		switch (currChar) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				omit();
				break;
		}
	}
	
	private final void accept() {
		currSpell.append(currChar);
	    currChar = src.nextChar();
	}

	private final void omit() {
	    currChar = src.nextChar();
	}
	
	private class SourceBuffer {
		private String src;
		private int currPos = 0;

		public SourceBuffer(String src) {
			this.src = src;
		}
		
		public char nextChar() {
			if (currPos < src.length())
				return src.charAt(currPos++);
			
			return '\0';
		}
	}
}
