package odra.wrapper.misc.testschema;

import java.util.Hashtable;

/**
 * Sample car make/model data.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-04-07
 */
public class CarData
{
	public static final Hashtable<String, String[]> CARS = new Hashtable<String, String[]>();
	
	static
	{
		CARS.put("Audi", new String[] {"A3", "A4", "A6", "A08", "TT"});
		CARS.put("Ford", new String[] {"Escort", "Focus", "GT", "Fusion", "Galaxy"});
		CARS.put("Honda", new String[] {"Accord", "Civic", "Prelude"});
		CARS.put("Mitsubishi", new String[] {"3000GT", "Diamante", "Eclipse", "Galant"});
		CARS.put("Toyota", new String[] {"Camry", "Celica", "Corolla", "RAV4", "Yaris"});
		CARS.put("Volkswagen", new String[] {"Golf", "Jetta", "Passat"});
	}
}
