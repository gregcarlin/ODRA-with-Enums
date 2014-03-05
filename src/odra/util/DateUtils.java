package odra.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UnknownFormatFlagsException;

import odra.sbql.ast.expressions.DateprecissionExpression;

/**
 * Date utility class.
 * 
 * @author jacenty
 * @version 2007-11-26
 * @since 2007-03-21
 */
public class DateUtils
{
	//0000-00-00 00:00:00.000
	private static final int OFFSET_SHORT = 10;
	private static final int OFFSET_MEDIUM = 16;
	private static final int OFFSET_LONG = 19;
	
	private static final String WHITE_SPACE = " +";
	private static final String DOT = "\\.";
	private static final String COLON = ":";
	
	/** OS locale independent date format */
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
	
	/**
	 * Formats a date according to a format givem. Formatting controls a date precission.
	 * 
	 * @param date {@link Date}
	 * @param format format string
	 * @return formatted date
	 * @throws ParseException 
	 */
	public static final Date formatDatePrecission(Date date, String format) throws ParseException
	{
		String dateString = DateFormat.getDateTimeInstance().format(date);
		
		if(format.equals(DateprecissionExpression.PRECISSION_LOW))
			return parseDatetime(dateString.substring(0, OFFSET_SHORT));
		else if(format.equals(DateprecissionExpression.PRECISSION_MEDIUM))
			return parseDatetime(dateString.substring(0, OFFSET_MEDIUM));
		else if(format.equals(DateprecissionExpression.PRECISSION_HIGH))
			return parseDatetime(dateString.substring(0, OFFSET_LONG));
		else if(format.equals(DateprecissionExpression.PRECISSION_FULL))
			;//no formatting needed
		else
			throw new UnknownFormatFlagsException(format);
		
		return date;
	}
	

	/**
	 * Parses a date-time string into a date with milliseconds.
	 * 
	 * @param src date-time string
	 * @return calculated date
	 * @throws ParseException 
	 */
	public static Date parseDatetime(String src) throws ParseException
	{
		return format.parse(normalizeDatetime(src));
	}
	
	
	/**
	 * Formats string representation of a date
	 * @param date
	 * @return string representation of a date
	 */
	public static String format(Date date){
		return format.format(date);
	}
	/**
	 * Normalizes a time string.
	 * 
	 * @param src time string
	 * @return normalized string
	 */
	private static String normalizeTime(String src)
	{
		String hour = "00";
		String min = "00";
		String sec = "00";
		String mili = "000";
		
		String[] split = src.split(COLON);
		if(split.length == 2)
		{
			hour = split[0];
			min = split[1];
		}
		else if(split.length == 3)
		{
			hour = split[0];
			min = split[1];
			String[] dotTest = split[2].split(DOT);
			if(dotTest.length == 1)
				sec = split[2];
			if(dotTest.length == 2)
			{
				sec = dotTest[0];
				mili = dotTest[1];
			}
		}
		
		return hour + ":" + min + ":" + sec + "." + mili;
	}
	
	/**
	 * Normalizes a date-time string.
	 * 
	 * @param src date-time string
	 * @return normalized string
	 */
	private static String normalizeDatetime(String src)
	{
		String date = "";
		String time = "";
		
		String[] dateTime = src.split(WHITE_SPACE);
		if(dateTime.length == 1)
		{
			date = src;
			time = normalizeTime("");
		}
		else if(dateTime.length == 2)
		{
			date = dateTime[0];
			time = normalizeTime(dateTime[1]);
		}
		
		return date + " " + time;
	}
}