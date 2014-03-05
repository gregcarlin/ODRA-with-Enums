package odra.ws.facade;

/**
 * WS configuration - some features cannot be in common configuration files as they refer to WS classes 
 * not available in all distributions.
 * 
 * @author jacenty
 * @version 2007-07-03
 * @since 2007-07-03
 */
public class Config
{
	/** registered bindings */
	public final static WSBindingType[] WS_BINDINGS = {WSBindingType.SOAP11};
}
