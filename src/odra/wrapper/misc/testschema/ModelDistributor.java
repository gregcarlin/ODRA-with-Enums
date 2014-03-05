package odra.wrapper.misc.testschema;

/**
 * Car model distributor.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-04-07
 */
class ModelDistributor extends Distributor
{
	ModelDistributor(String make)
	{
		for(String model : CarData.CARS.get(make))
			super.add(model, 1);
	}
}
