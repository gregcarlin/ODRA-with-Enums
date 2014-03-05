package odra.wrapper.misc.testschema;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Random;

/**
 * Birth date distributor.
 * 
 * @author jacenty
 * @version 2007-02-25
 * @since 2007-02-25
 */
class BirthDateDistributor extends Distributor
{
	private final int retryLimit = 10;
	private final int minYear = 1950;
	private final int maxYear = 1987;
	
	private final Random random = new Random();
	
	BirthDateDistributor()
	{
	}

	@Override
	String getRandomValue()
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
		
		return date;
	}
	
	private final int randomYear()
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
	
	private final int randomMonth()
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
	
	private final int randomDay()
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
