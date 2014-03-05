package odra.wrapper.misc.testschema;

/**
 * Car colour distributor.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-04-07
 */
class ColourDistributor extends Distributor
{
	private String[] colours = new String[] {
		"red",
		"green",
		"blue",
		"white",
		"black",
		"yellow",
		"orange",
		};
	
	ColourDistributor()
	{
		for(String colour : colours)
			super.add(colour, 1);
	}
}
