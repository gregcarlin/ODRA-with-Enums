package tests.review.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

/**
 * Random date generator.
 * 
 * @author jacenty
 * @version 2008-01-28
 * @since 2008-01-28
 */
class DateGenerator
{
	private int retryLimit = 1000;
	private int minYear = 1900;
	private int maxYear = 2008;
	
	private Random random = new Random();
	
	Date getRandomValue() throws ParseException
	{
		int month = randomMonth();
		int day = randomDay();
		
		String monthString = Integer.toString(month);
		while(monthString.length() < 2)
			monthString = "0" + monthString;
		
		String dayString = Integer.toString(day);
		while(dayString.length() < 2)
			dayString = "0" + dayString;
		
		String date = randomYear() + "-" + monthString + "-" + dayString;
		
		try
		{
			date = DateFormat.getDateInstance().format(DateFormat.getDateInstance().parse(date));
		}
		catch(ParseException exc)
		{
			exc.printStackTrace();
		}
		
		return DateFormat.getDateInstance().parse(date);
	}
	
	private int randomYear()
	{
		int year;
		int retryCount = 0;
		do
		{
			if(retryCount < retryLimit)
				year = random.nextInt(maxYear + 1);
			else
			{
				if(random.nextBoolean())
					year = minYear;
				else
					year = maxYear;
			}
			
			retryCount++;
		}
		while(year < minYear || year > maxYear);
		
		return year;
	}
	
	private int randomMonth()
	{
		int month;
		int retryCount = 0;
		do
		{
			if(retryCount < retryLimit)
				month = random.nextInt(13);
			else
			{
				if(random.nextBoolean())
					month = 12;
				else
					month = 1;
			}
			
			retryCount++;
		}
		while(month < 1 || month > 12);
		
		return month;
	}
	
	private int randomDay()
	{
		int day;
		int retryCount = 0;
		do
		{
			if(retryCount < retryLimit)
				day = random.nextInt(31);
			else
			{
				if(random.nextBoolean())
					day = 31;
				else
					day = 1;
			}
			
			retryCount++;
		}
		while(day < 1 || day > 31);
		
		return day;
	}
}
