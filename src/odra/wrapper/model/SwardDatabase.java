package odra.wrapper.model;

import java.io.File;

import odra.wrapper.WrapperException;
import odra.wrapper.config.SwardConfig;

import org.apache.commons.configuration.ConfigurationException;

/**
 * SWARD database model.
 * 
 * @author jacenty
 * @version 2007-06-25
 * @since 2007-06-05
 */
public class SwardDatabase extends Database
{
	/** UPV name */
	private final String upvName;
	/** UPV URI */
	private final String upvUri;
	/** subject */
	private final String subject;
	/** predicate */
	private final String predicate;
	/** object */
	private final String object;
	
	/**
	 * Constructor.
	 * 
	 * @param schemaFile schema file
	 * @throws ConfigurationException 
	 */
	public SwardDatabase(File schemaFile) throws ConfigurationException
	{
		this(schemaFile, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param schemaFile schema file path
	 * @throws ConfigurationException 
	 */
	public SwardDatabase(String schemaFile) throws ConfigurationException
	{
		this(new File(schemaFile));
	}
	
	/**
	 * Constructor.
	 * 
	 * @param schemaFile schema file
	 * @param instanceName instance name
	 * @throws WrapperException
	 * @throws ConfigurationException 
	 */
	public SwardDatabase(File schemaFile, String instanceName) throws ConfigurationException
	{
		SwardConfig config = new SwardConfig(instanceName, schemaFile);

		name = config.getProperty(SwardConfig.SWARD_XXX_NAME);
		upvName = config.getProperty(SwardConfig.SWARD_XXX_UPV_NAME);
		upvUri = config.getProperty(SwardConfig.SWARD_XXX_UPV_URI);
		subject = config.getProperty(SwardConfig.SWARD_XXX_UPV_SUBJECT);
		predicate = config.getProperty(SwardConfig.SWARD_XXX_UPV_PREDICATE);
		object = config.getProperty(SwardConfig.SWARD_XXX_UPV_OBJECT);

		Table table = new Table(this, upvName);
		table.addColumn(new Column(table, subject, "varchar", true));
		table.addColumn(new Column(table, predicate, "varchar", true));
		table.addColumn(new Column(table, object, "varchar", true));
		tables.put(table.getName(), table);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param schemaFile schema file path
	 * @param instanceName instance name
	 * @throws ConfigurationException 
	 */
	public SwardDatabase(String schemaFile, String instanceName) throws ConfigurationException
	{
		this(new File(schemaFile), instanceName);
	}
	
	@Override
	public String toString()
	{
		return "[SWARD database based on " + schemaFile.getName() + "]";
	}

	/**
	 * Returns the 'object' field name.
	 * 
	 * @return 'object' field name
	 */
	public String getObject()
	{
		return object;
	}

	/**
	 * Returns the 'predicate' field name.
	 * 
	 * @return 'predicate' field name
	 */
	public String getPredicate()
	{
		return predicate;
	}

	/**
	 * Returns the 'subject' field name.
	 * 
	 * @return 'subject' field name
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * Returns the UPV name.
	 * 
	 * @return UPV name
	 */
	public String getUpvName()
	{
		return upvName;
	}

	/**
	 * Returns the UPV URI.
	 * 
	 * @return UPV URI
	 */
	public String getUpvUri()
	{
		return upvUri;
	}
}
