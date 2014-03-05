package tests.dates;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class Regexp
{
	private final String WHITE_SPACE = " +";
	private final String DOT = "\\.";
	private final String COLON = ":";
	
	public static void main(String[] args)
	{
		new Regexp().test();
	}
	
	private void test()
	{
		String regexp = "[0-9]{4}[-](0[1-9]|1[012])[-](0[1-9]|[12][0-9]|3[01])( +(0[0-9]|1[0-9]|2[0-3])[:](0[0-9]|[1-5][0-9])([:](0[0-9]|[1-5][0-9])([.][0-9]{1,3})?)?)?";
		String[] test = new String[] {
			"1977-11-03",
			
			"1977-11-03 00:00",
			"1977-11-03 10:09",
			"1977-11-03 19:10",
			"1977-11-03 23:59",
			
			"1977-11-03 00:00:00",
			"1977-11-03 10:09:09",
			"1977-11-03 19:10:10",
			"1977-11-03 23:59:59",
			
			"1977-11-03 23:59:59.0",
			"1977-11-03 23:59:59.00",
			"1977-11-03 23:59:59.999",
		};
		
		for(int i = 0; i < test.length; i++)
		{
			boolean matches = test[i].matches(regexp);
			System.out.println(i + ": " + test[i] + " " + matches);
			if(matches)
			{
				String formatted = format(test[i]);
				System.out.println(formatted);
				try
				{
					long time = parse(formatted);
					Date date = new Date(time);
					
					System.out.println(date);
				}
				catch(ParseException exc)
				{
					exc.printStackTrace();
				}
			}
			System.out.println();
		}
	}
	
	/**
	 * Parses a formatted date-time string into time in milliseconds.
	 * 
	 * @param src date-time string
	 * @return calculated time in ms
	 * @throws ParseException 
	 */
	private long parse(String src) throws ParseException
	{
		String[] dotSplit = src.split(DOT);
		return DateFormat.getDateTimeInstance().parse(dotSplit[0]).getTime() + Long.parseLong(dotSplit[1]);
	}
	
	/**
	 * Formats a time string.
	 * 
	 * @param src time string
	 * @return formatted string
	 */
	private String formatTime(String src)
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
	 * Formats a date-time string.
	 * 
	 * @param src date-time string
	 * @return formatted string
	 */
	private String format(String src)
	{
		String date = "";
		String time = "";
		
		String[] dateTime = src.split(WHITE_SPACE);
		if(dateTime.length == 1)
		{
			date = src;
			time = formatTime("");
		}
		else if(dateTime.length == 2)
		{
			date = dateTime[0];
			time = formatTime(dateTime[1]);
		}
		
		return date + " " + time;
	}
}
