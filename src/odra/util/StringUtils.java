package odra.util;

import java.util.Random;

/**
 * String utility class.
 * 
 * @author jacenty
 * @version   2007-09-21
 * @since   2007-01-07
 */
public class StringUtils
{
	/** pseudo-random number generaor */
	private static Random random = new Random();

	/**
	 * Generates a random string of a given length. 
	 * 
	 * @param length length
	 * @param lowerCase lower case?
	 * @return random string
	 */
	public static String randomString(int length, boolean lowerCase)
	{
		String randString = "";
		for(int i = 0; i < length; i++)
			randString += randomChar();

		if(lowerCase)
			randString = randString.toLowerCase();
		return randString;
	}

	/**
	 * Returns a random character within 0-9 and A-Z.
	 * 
	 * @return random character
	 */
	public static char randomChar()
	{
		int c = random.nextInt(91);
		while(c < 48 || (c >= 58 && c <= 64))
			c = random.nextInt(91);

		return (char)c;
	}
	
	/**
	 * Rewrites the raw pattern string, i.e. escapes special regex characters so that they are 
	 * seen as literals and replaces query wildcards with regex wildcards.
	 * 
	 * @param rawPattern raw pattern string
	 * @return regex pattern
	 */
	public static String rewritePatternToRegEx(String rawPattern)
	{
		String pattern = rawPattern;

		pattern = 
			pattern.
			//escape all special regex characters to be used as literals
			replaceAll("\\.", "\\\\.").
			replaceAll("\\?", "\\\\?").
			replaceAll("\\*", "\\\\*").
			replaceAll("\\)", "\\\\)").
			replaceAll("\\(", "\\\\(").
			replaceAll("\\[", "\\\\[").
			replaceAll("\\]", "\\\\]").
			replaceAll("\\{", "\\\\{").
			replaceAll("\\}", "\\\\}").
			replaceAll("\\^", "\\\\^").
			replaceAll("\\+", "\\\\+").
			replaceAll("\\|", "\\\\|").
			//replace wildcards
			replaceAll("_", ".").
			replaceAll("%", ".*");
	
		return pattern;
	}
	
	/**
	 * Removes trailing white spaces from the input string.
	 * 
	 * @param src input string
	 * @return trimmed string
	 */
	public static String trimTrailing(String src)
	{
		char[] chars = src.toCharArray();
		int i;
		for(i = chars.length - 1; i >= 0; i--)
			if(!Character.isWhitespace(chars[i]))
				break;
		
		return src.substring(0, i + 1);
	}
}
