package odra.wrapper.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import odra.wrapper.WrapperException;
import odra.wrapper.generator.Schema;
import odra.wrapper.generator.XSDGenerator;
import odra.wrapper.model.Index.Type;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.helpers.DefaultHandler;


/**
 * A database representation.
 * 
 * @author jacenty
 * @version 2007-06-25
 * @since 2006-05-21
 */
public class Database extends DefaultHandler implements Serializable
{
	/** schema file */
	protected File schemaFile;
	
	/** database name */
	protected String name;
	/** tables */
	protected Hashtable<String, Table> tables = new Hashtable<String, Table>();

	/**
	 * Constructor.
	 * 
	 * @param schemaFile
	 *          schema file path
	 * @throws WrapperException
	 */
	public Database(String schemaFile) throws WrapperException
	{
		this(new File(schemaFile));
	}

	/**
	 * Constructor.
	 * 
	 * @param schemaFile
	 *          schema file
	 * @throws WrapperException
	 */
	public Database(File schemaFile) throws WrapperException
	{
		if(!schemaFile.exists())
			throw new WrapperException("File '" + schemaFile + "' nor found!", WrapperException.Error.XML_SCHEMA_NOT_FOUND);

		this.schemaFile = schemaFile;
		
		build();
	}
	
	/**
	 * No-argument constructor (mainly for inheritance).
	 *
	 */
	protected Database()
	{
		
	}

	/**
	 * Builds a database model according to the schema file.
	 * 
	 * @throws WrapperException
	 */
	@SuppressWarnings("unchecked")
	private void build() throws WrapperException
	{
		try
		{
			SAXBuilder builder = new SAXBuilder(true);
			Document doc = builder.build(schemaFile);
			
			Element root = doc.getRootElement();
			name = root.getAttributeValue(Schema.NAME.toString());
			List<Element> tables = root.getChildren(Schema.TABLE.toString());
			
			//ensure that all the tables (with their columns) are added to database before defining indices, foreign keys, etc.
			for(int i = 0; i < tables.size(); i++)
			{
				Element tableElement = tables.get(i); 
				String tableName = tableElement.getAttributeValue(Schema.NAME.toString());
				
				Table table = addTable(new Table(this, tableName));

				List<Element> columns = tableElement.getChildren(Schema.COLUMN.toString());
				for(int j = 0; j < columns.size(); j++)
				{
					Element columnElement = columns.get(j);
					String columnName = columnElement.getAttributeValue(Schema.NAME.toString());
					String type = columnElement.getAttributeValue(Schema.TYPE.toString());
					boolean nullable = new Boolean(columnElement.getAttributeValue(Schema.NULLABLE.toString()));
					
					table.addColumn(new Column(table, columnName, type, nullable));
				}
			}
				
			for(int i = 0; i < tables.size(); i++)
			{
				Element tableElement = tables.get(i);
				String tableName = tableElement.getAttributeValue(Schema.NAME.toString());
				
				Table table = getTable(tableName);
				
				Element bestRowId = tableElement.getChild(Schema.BEST_ROW_ID.toString());
				if(bestRowId != null)
				{
					List<Element> bestRowIdColumns = bestRowId.getChildren(Schema.BEST_ROW_ID_COLUMN.toString());
					for(int j = 0; j < bestRowIdColumns.size(); j++)
					{
						Element bestRowIdColumnElement = bestRowIdColumns.get(j);
						String columnName = bestRowIdColumnElement.getAttributeValue(Schema.NAME.toString());
						
						table.addBestRowIdColumn(table.getColumn(columnName));
					}
				}
				
				List<Element> indices = tableElement.getChildren(Schema.INDEX.toString());
				for(int j = 0; j < indices.size(); j++)
				{
					Element indexElement = indices.get(j);
					String indexName = indexElement.getAttributeValue(Schema.NAME.toString());
					boolean unique = new Boolean(indexElement.getAttributeValue(Schema.UNIQUE.toString()));
					Type type = Type.getTypeForShort(new Short(indexElement.getAttributeValue(Schema.TYPE.toString())));
					int cardinality = new Integer(indexElement.getAttributeValue(Schema.CARDINALITY.toString()));
					int pages = new Integer(indexElement.getAttributeValue(Schema.PAGES.toString()));
					String filterCondition = indexElement.getAttributeValue(Schema.FILTER_CONDITION.toString());
					
					Index index = new Index(indexName, unique, type, pages, cardinality, filterCondition);
					
					List<Element> indexColumns = indexElement.getChildren(Schema.INDEX_COLUMN.toString());
					for(int k = 0; k < indexColumns.size(); k++)
					{
						Element indexColumnElement = indexColumns.get(k);
						String indexColumnName = indexColumnElement.getAttributeValue(Schema.NAME.toString());
						
						index.addColumn(new IndexColumn(indexColumnName, (short)k));
					}
					
					table.addIndex(index);
				}

				List<Element> foreignKeys = tableElement.getChildren(Schema.FOREIGN_KEY.toString());
				for(int j = 0; j < foreignKeys.size(); j++)
				{
					Element foreignKeyElement = foreignKeys.get(j);
					String refTableName = foreignKeyElement.getAttributeValue(Schema.FOREIGN_TABLE.toString());
					
					Table refTable = getTable(refTableName);
					
					Vector<Column> localColumns = new Vector<Column>();
					Vector<Column> refColumns = new Vector<Column>();
					
					List<Element> references = foreignKeyElement.getChildren(Schema.REFERENCE.toString());
					for(int k = 0; k < references.size(); k++)
					{
						Element reference = references.get(k);
						String localColumnName = reference.getAttributeValue(Schema.LOCAL.toString());
						String refColumnName = reference.getAttributeValue(Schema.FOREIGN.toString());
						
						Column localColumn = table.getColumn(localColumnName);
						Column refColumn = refTable.getColumn(refColumnName);
						
						localColumns.add(localColumn);
						refColumns.add(refColumn);
					}
					
					ForeignKey foreignKey = new ForeignKey(table, localColumns, refTable, refColumns);
					table.addForeignKey(foreignKey);
				}
			}
		}
		catch(IOException exc)
		{
			throw new WrapperException("IOException", exc, WrapperException.Error.XML_SCHEMA_PARSE);
		}
		catch(JDOMException exc)
		{
			throw new WrapperException("JDOMException", exc, WrapperException.Error.XML_SCHEMA_PARSE);
		}
	}

	/**
	 * Adds a table (if not added before).
	 * 
	 * @param table table
	 * @return table
	 */
	private Table addTable(Table table)
	{
		if(!tables.containsKey(table.getName()))
			tables.put(table.getName(), table);
		
		return getTable(table.getName());
	}
	
	/**
	 * Returns a table with a given name, <code>null</code> if table not found.
	 * 
	 * @param name table name
	 * @return <code>Table</code>
	 */
	public Table getTable(String name)
	{
		return tables.get(name.toLowerCase());
	}

	/**
	 * Return all tables.
	 * 
	 * @return tables
	 */
	public Hashtable<String, Table> getTables()
	{
		return tables;
	}
	
	@Override
	public String toString()
	{
		return "[database based on " + schemaFile.getName() + "]";
	}
	
	/**
	 * Returns a database name.
	 * 
	 * @return name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Creates and returns a metabase XSD.
	 * 
	 * @return metabase XSD
	 */
	public String createMetabaseXSD()
	{
		return new XSDGenerator(this).getSchemaString();
	}

	/**
	 * Returns if this database contains a table with a given name.
	 * 
	 * @param name table name
	 * @return contains the tab;e?
	 */
	public boolean containsTable(String name)
	{
		return tables.containsKey(name.toLowerCase());
	}
}