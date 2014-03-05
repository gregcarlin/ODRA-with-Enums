package odra.wrapper.config;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * A SWARD configuration class.
 * @author jacenty
 * @version   2007-07-09
 * @since   2007-06-25
 */
public class SwardConfig extends PropertiesConfiguration
{
	/** default config file path */
	public final static String CONFIG_FILE_NAME = "sward.schema.properties";
	/** a property name */
	public static final String SWARD_INSTANCE_DEFAULT = "sward.default";
	
	/** a placehloder string for creating property names */
	private static final String PLACEHOLDER = "xxxxxx";
	
	/** a base property name */
	public static final String SWARD_XXX_NAME = "sward." + PLACEHOLDER + ".name";
	/** a base property name */
	public static final String SWARD_XXX_UPV_NAME = "sward." + PLACEHOLDER + ".upv.name";
	/** a base property name */
	public static final String SWARD_XXX_UPV_URI = "sward." + PLACEHOLDER + ".upv.uri";
	/** a base property name */
	public static final String SWARD_XXX_UPV_SUBJECT = "sward." + PLACEHOLDER + ".upv.subject";
	/** a base property name */
	public static final String SWARD_XXX_UPV_PREDICATE = "sward." + PLACEHOLDER + ".upv.predicate";
	/** a base property name */
	public static final String SWARD_XXX_UPV_OBJECT = "sward." + PLACEHOLDER + ".upv.object";
	/** a base property name */
	public static final String SWARD_XXX_CONNECTION_USER = "sward." + PLACEHOLDER + ".connection.user";
	/** a base property name */
	public static final String SWARD_XXX_CONNECTION_PASSWORD = "sward." + PLACEHOLDER + ".connection.password";
	
	/**
	 * The constructor.
	 * 
	 * @throws ConfigurationException
	 */
	public SwardConfig() throws ConfigurationException
	{
		this(null, new File(CONFIG_FILE_NAME));
	}
	
	/**
	 * The constructor.
	 * 
	 * @param defaultInstanceName default instance
	 * @param configFilePath config file path
	 * @throws ConfigurationException
	 */
	public SwardConfig(String defaultInstanceName, String configFilePath) throws ConfigurationException
	{
		this(defaultInstanceName, new File(configFilePath));
	}
	
	/**
	 * The constructor.
	 * 
	 * @param defaultInstanceName default instance
	 * @param configFile config file
	 * @throws ConfigurationException 
	 */
	public SwardConfig(String defaultInstanceName, File configFile) throws ConfigurationException
	{
		super(configFile);
		
		if(defaultInstanceName != null)
			setProperty(SWARD_INSTANCE_DEFAULT, defaultInstanceName);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param configFilePath config file path
	 * @throws ConfigurationException
	 */
	public SwardConfig(String configFilePath) throws ConfigurationException
	{
		this(null, new File(configFilePath));
	}
	
	/**
	 * The constructor.
	 * 
	 * @param configFile config file
	 * @throws ConfigurationException
	 */
	public SwardConfig(File configFile) throws ConfigurationException
	{
		this(null, configFile);
	}
	
	/**
	 * Returns a property value for a given database.
	 * 
	 * @param basePropertyName base property name
	 * @return property value, <code>null</code> if not set
	 */
	public String getProperty(String basePropName)
	{
		String propName;
		if(basePropName.equals(SWARD_INSTANCE_DEFAULT))
			propName = basePropName;
		else
			propName = createPropertyName(basePropName);
		
		if(containsKey(propName))
			return super.getProperty(propName).toString();
		else
			return null;
	}
	
	/**
	 * Transforms a base property name into a db property name.
	 * 
	 * @param basePropName a base property name
	 * @return db property name
	 */
	private final String createPropertyName(String basePropName)
	{
		return basePropName.replaceAll(PLACEHOLDER, getProperty(SWARD_INSTANCE_DEFAULT).toString());
	}
}
