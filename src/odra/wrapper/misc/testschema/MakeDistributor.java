package odra.wrapper.misc.testschema;

/**
 * Car make distributor.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-04-07
 */
class MakeDistributor extends Distributor
{
	MakeDistributor()
	{
		for(String make : CarData.CARS.keySet())
			super.add(make, 1);
	}
}
