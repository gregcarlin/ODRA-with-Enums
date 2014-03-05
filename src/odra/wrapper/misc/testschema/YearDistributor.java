package odra.wrapper.misc.testschema;

import java.util.Random;

/**
 * Car year distributor.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-04-07
 */
class YearDistributor extends Distributor
{
	private final int retryLimit = 10;
	private final int minYear = 1990;
	private final int maxYear = 2007;
	
	private final Random random = new Random();
	
	YearDistributor()
	{
	}
	
	@Override
	String getRandomValue()
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
		
		return Integer.toString(year);
	}
}
