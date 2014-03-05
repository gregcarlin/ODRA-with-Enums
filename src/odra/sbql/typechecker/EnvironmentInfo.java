package odra.sbql.typechecker;


/**
 * This class is used to transfer some meta information about the environment.
 * It is used to inform about the current Environment Stack size and
 * the number of frames opened by the operator
 * @author radamus
 */
public class EnvironmentInfo {    
	public int baseEnvsSize; //base size of the Envs
	public int framesOpened; //number of frames opened by the non-algebraic operator
	public int relativeSection;//relative section number (section can contains more than one frame)

}
