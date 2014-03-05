package odra.cli.parser;

import odra.cli.CLISyntaxErrorException;


/**
 * This class represents a simple lexer used by jodra's cli tool.
 * It is used only during parsing of cli commands. SBQL has its
 * own lexer/parser generated using jflex/cup.
 * 
 * @author raist
 */

public class CLILexer {
	private String src;
	private SourceBuffer srcbuf;

	private char currChar;
	private StringBuffer currSpell;
	
	public CLILexer(String str) {
		this.src = str;
		this.srcbuf = new SourceBuffer();
		this.currChar = (char) srcbuf.nextChar();
	}

	public String getSource() {
		return src;
	}
	
	public String getRemainingSource() {
		return src.substring(srcbuf.currPos);
	}

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isSpecSymbol(char c) {
		return (c == '_');
	}

	public void rewind(int nbytes) {
		srcbuf.currPos -= nbytes;
	}

	public CLIToken nextToken() throws CLISyntaxErrorException {
	    currSpell = new StringBuffer("");
		
	    while (currChar == ' ' || currChar == '\n' || 
	    		currChar == '\r' || currChar == '\t')
	    	scanSeparator();
	    
	    byte kind = scanToken();

		return new CLIToken(kind, currSpell.toString());
	}

	private byte scanToken() throws CLISyntaxErrorException {
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
		    case '_':
		    	accept();
		        while (isLetter(currChar) || isDigit(currChar)|| isSpecSymbol(currChar))
		            accept();
		    	
				return CLIToken.NAME;
				
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				accept();
			      while (isDigit(currChar))
			          accept();
			
				return CLIToken.INTEGER_LITERAL;
			
			case '\0':
				return CLIToken.EOF;
			
			case ':':
				accept();
				return CLIToken.COLON;
			
			case '/':
				accept();
				return CLIToken.SLASH;
			
			case '@':
				accept();
				return CLIToken.AT;
				
			case '.':
				accept();
				return CLIToken.DOT;
			case '|':
				accept();
				return CLIToken.BAR;
			case '=':
				accept();
				return CLIToken.EQ;
			case ',':
				accept();
				return CLIToken.COMMA;				
			case '"':	
				omit();

				while (currChar != '"') {
					scanEscapes();
					
					if (currChar == '\0')
						throw new CLISyntaxErrorException("String literal is not properly closed by a double-quote");

					accept();
				}
				omit();
				return CLIToken.STRING_LITERAL;

		    default:
		        accept();
		        return CLIToken.ERROR;
		}
	}

	private void scanEscapes() {
		if (currChar == '\\') {
			omit();
			
			switch (currChar) {
				case '"':
					accept();
					break;

				default:
					append('\\');
			}
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
	    currChar = srcbuf.nextChar();
	}

	private final void append(char appChar) {
		currSpell.append(appChar);
	}
	
	private final void omit() {
	    currChar = srcbuf.nextChar();
	}

	private class SourceBuffer {
		int currPos = 0;

		public char nextChar() {
			return currPos < src.length() ? src.charAt(currPos++) : '\0';
		}
	}
}
