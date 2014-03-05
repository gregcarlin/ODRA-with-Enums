package odra.wrapper.misc.testschema;

import java.util.Random;
import java.util.Vector;

/**
 * Data distribution utility class.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-02-11
 */
class Distributor extends Vector<String>
{
	private Vector<String> uniqueValues = new Vector<String>();
	private Random random = new Random();
	
	/**
	 * Adds a next value with a given frequency with an sssumption the data is sorted already.
	 * 
	 * @param value value
	 * @param occurences occurences
	 */
	void add(String value, int occurences)
	{
		uniqueValues.addElement(value);
		
		for(int i = 0; i < occurences; i++)
			addElement(value);
	}
	
	@Override
	public synchronized boolean add(@SuppressWarnings("unused") String arg0)
	{
		throw new RuntimeException("do not use this method");
	}

	String getRandomValue()
	{
		return get(random.nextInt(size()));
	}
	
	Vector<String> getUniqueValues()
	{
		return uniqueValues;
	}
}
