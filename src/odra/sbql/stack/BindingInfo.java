package odra.sbql.stack;

/**
 * This class is used to transfer some meta information about binding.
 * For example, it may be used to inform the static type checker
 * at what envs section a name was bound.
 * 
 * @author raist
 */
public class BindingInfo
{
	/**
	 * the envs frame number the name was bound
	 */
	public int boundat;
	
	/**
	 * The number indicates relative section number 
	 * (one section can contains more than one frame)
	 */
	public int relativeSection;
	public BindingInfo(int boundat)
	{
		this.boundat = boundat;
	}
	
	public BindingInfo()
	{
	}
	
}
