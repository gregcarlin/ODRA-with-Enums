package odra.wrapper.config;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * A Torque configuration class (no need to use <code>.properties</code> files).
 * @author jacenty
 * @version   2007-07-09
 * @since   2006-05-17
 */
public class TorqueConfig extends PropertiesConfiguration
{
	/** default config file path */
	public final static String CONFIG_FILE_NAME = "connection.properties";
	/** a property name */
	public static final String TORQUE_DATABASE_DEFAULT = "torque.database.default";
	
	/** a placehloder string for creating property names */
	private static final String PLACEHOLDER = "xxxxxx";
	
	/** a base property name */
	public static final String TORQUE_DATABASE_XXX_ADAPTER = "torque.database." + PLACEHOLDER + ".adapter";
	/** a base property name */
	public static final String TORQUE_DSFACTORY_XXX_FACTORY = "torque.dsfactory." + PLACEHOLDER + ".factory";
	/** a base property name */
	public static final String TORQUE_DSFACTORY_XXX_CONNECTION_DRIVER = "torque.dsfactory." + PLACEHOLDER + ".connection.driver";
	/** a base property name */
	public static final String TORQUE_DSFACTORY_XXX_CONNECTION_URL = "torque.dsfactory." + PLACEHOLDER + ".connection.url";
	/** a base property name */
	public static final String TORQUE_DSFACTORY_XXX_CONNECTION_USER = "torque.dsfactory." + PLACEHOLDER + ".connection.user";
	/** a base property name */
	public static final String TORQUE_DSFACTORY_XXX_CONNECTION_PASSWORD = "torque.dsfactory." + PLACEHOLDER + ".connection.password";
	
	/**
	 * The constructor.
	 * 
	 * @throws ConfigurationException
	 */
	public TorqueConfig() throws ConfigurationException
	{
		this(null, new File(CONFIG_FILE_NAME));
	}
	
	/**
	 * The constructor.
	 * 
	 * @param defaultDbName default db
	 * @param configFilePath config file path
	 * @throws ConfigurationException
	 */
	public TorqueConfig(String defaultDbName, String configFilePath) throws ConfigurationException
	{
		this(defaultDbName, new File(configFilePath));
	}
	
	/**
	 * The constructor.
	 * 
	 * @param defaultDbName default db
	 * @param configFile config file
	 * @throws ConfigurationException 
	 */
	public TorqueConfig(String defaultDbName, File configFile) throws ConfigurationException
	{
		super(configFile);
		
		if(defaultDbName != null)
			setProperty(TORQUE_DATABASE_DEFAULT, defaultDbName);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param configFilePath config file path
	 * @throws ConfigurationException
	 */
	public TorqueConfig(String configFilePath) throws ConfigurationException
	{
		this(null, new File(configFilePath));
	}
	
	/**
	 * The constructor.
	 * 
	 * @param configFile config file
	 * @throws ConfigurationException
	 */
	public TorqueConfig(File configFile) throws ConfigurationException
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
		if(basePropName.equals(TORQUE_DATABASE_DEFAULT))
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
		return basePropName.replaceAll(PLACEHOLDER, getProperty(TORQUE_DATABASE_DEFAULT).toString());
	}
}
