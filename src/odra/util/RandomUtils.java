package odra.util;

import java.util.Random;

/**
 * Random value utility class.
 * 
 * @author jacenty
 * @version 2007-03-23
 * @since 2007-03-23
 */
public class RandomUtils
{
	/** random number generator */
	private static final Random RANDOM = new Random();
	/** generation retry limit */
	private static final int RETRY_LIMIT = 10;
	
	/**
	 * Generates a random value between <code>min</code> inclusive and <code>inclusive</code>.
	 * 
	 * @param min min value (inclusive)
	 * @param max max value (inclusive)
	 * @return random value
	 */
	public static int next(int min, int max)
	{
		if(max < min)
			throw new IllegalArgumentException("incorrect min & max values (max >= min), given: min = " + min + ", max = " + max);
		
		int result;
		int sign = 1;
		int retryControl = 0; 
		do
		{
			if(retryControl < RETRY_LIMIT)
			{
				if(min < 0 || max < 0)
					if(RANDOM.nextBoolean())
						sign = -1;

				result = sign * RANDOM.nextInt(Math.max(Math.abs(min) + 1, Math.abs(max) + 1));
			}
			else
			{
				if(RANDOM.nextBoolean())
					result = max;
				else
					result = min;
			}
			
			retryControl++;
		}
		while(result < min || result > max);
		
		return result;
	}
}