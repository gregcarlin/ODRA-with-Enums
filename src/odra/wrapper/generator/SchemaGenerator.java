package odra.wrapper.generator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import odra.wrapper.config.TorqueConfig;
import odra.wrapper.model.Index;
import odra.wrapper.model.IndexColumn;
import odra.wrapper.model.Index.Type;

import org.apache.tools.ant.Project;
import org.apache.torque.engine.database.model.TypeMap;
import org.apache.torque.task.TorqueJDBCTransformTask;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.w3c.dom.Element;


/**
 * Reads relational schema and creates XML schema description file (for local use).
 *  
 * @author jacenty
 * @version   2006-12-11
 * @since   2006-05-20
 */
public class SchemaGenerator extends TorqueJDBCTransformTask
{
	@SuppressWarnings( {"unchecked", "deprecation"})
	@Override
	public void generateXML() throws Exception
	{
		DocumentTypeImpl docType = new DocumentTypeImpl(null, Schema.DATABASE.toString(), null, "http://jacenty.kis.p.lodz.pl/relational-schema.dtd");
		doc = new DocumentImpl(docType);
		doc.appendChild(doc.createComment("SBQL Wrapper - relational database schema"));
		doc.appendChild(doc.createComment("generated at " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date())));
		doc.appendChild(doc.createComment("author: Jacek Wislicki,  jacek.wislicki@gmail.com"));
		
		Class.forName(dbDriver);
		log("DB driver sucessfuly instantiated");

		Connection con = null;
		try
		{
			con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			log("DB connection established");

			DatabaseMetaData dbMetaData = con.getMetaData();
			List tableList = getTableNames(dbMetaData);
			
			databaseNode = doc.createElement(Schema.DATABASE.toString());
			databaseNode.setAttribute(Schema.NAME.toString(), dbUser);

			columnTableMap = new Hashtable();

			log("Building column/table map...");
			for(int i = 0; i < tableList.size(); i++)
			{
				String curTable = (String)tableList.get(i);
				List columns = getColumns(dbMetaData, curTable);

				for(int j = 0; j < columns.size(); j++)
				{
					List col = (List)columns.get(j);
					String name = (String)col.get(0);

					columnTableMap.put(name, curTable);
				}
			}

			for(int i = 0; i < tableList.size(); i++)
			{
				String curTable = (String)tableList.get(i);
				log("Processing table: " + curTable);

				Element table = doc.createElement(Schema.TABLE.toString());
				table.setAttribute(Schema.NAME.toString(), curTable);

				List columns = getColumns(dbMetaData, curTable);
				Collection forgnKeys = getForeignKeys(dbMetaData, curTable);
				
				for(int j = 0; j < columns.size(); j++)
				{
					List col = (List)columns.get(j);
					String name = (String)col.get(0);
					Integer type = ((Integer)col.get(1));
					int size = ((Integer)col.get(2)).intValue();
					int scale = ((Integer)col.get(5)).intValue();

					Integer nullType = (Integer)col.get(3);
					String defValue = (String)col.get(4);

					Element column = doc.createElement(Schema.COLUMN.toString());
					column.setAttribute(Schema.NAME.toString(), name);

					column.setAttribute(Schema.TYPE.toString(), TypeMap.getTorqueType(type).getName());

					if(size > 0 && (type.intValue() == Types.CHAR || type.intValue() == Types.VARCHAR || type.intValue() == Types.LONGVARCHAR || type.intValue() == Types.DECIMAL || type.intValue() == Types.NUMERIC))
						column.setAttribute(Schema.SIZE.toString(), String.valueOf(size));

					if(scale > 0 && (type.intValue() == Types.DECIMAL || type.intValue() == Types.NUMERIC))
						column.setAttribute(Schema.SCALE.toString(), String.valueOf(scale));

					String nullable;
					if(nullType.intValue() == 0)
						nullable = "false";
					else
						nullable = "true";
					column.setAttribute(Schema.NULLABLE.toString(), nullable);

					if(defValue != null)
					{
						if(defValue.startsWith("(") && defValue.endsWith(")"))
							defValue = defValue.substring(1, defValue.length() - 1);

						if(defValue.startsWith("'") && defValue.endsWith("'"))
							defValue = defValue.substring(1, defValue.length() - 1);

						column.setAttribute(Schema.DEFAULT.toString(), defValue);
					}
					table.appendChild(column);
				}
				
				ResultSet rs = dbMetaData.getBestRowIdentifier(null, dbSchema, curTable, 0, false);
				if(rs.next())
				{
					Element bestRowId = doc.createElement(Schema.BEST_ROW_ID.toString());
					rs = dbMetaData.getBestRowIdentifier(null, dbSchema, curTable, 0, false);
					while(rs.next())
					{
						Element bestRowIdColumn = doc.createElement(Schema.BEST_ROW_ID_COLUMN.toString());
						bestRowIdColumn.setAttribute(Schema.NAME.toString(), rs.getString("COLUMN_NAME"));
						bestRowId.appendChild(bestRowIdColumn);
					}
					table.appendChild(bestRowId);
				}
				
				for(Iterator l = forgnKeys.iterator(); l.hasNext();)
				{
					Object[] forKey = (Object[])l.next();
					String foreignKeyTable = (String)forKey[0];
					List refs = (List)forKey[1];
					Element fk = doc.createElement(Schema.FOREIGN_KEY.toString());
					fk.setAttribute(Schema.FOREIGN_TABLE.toString(), foreignKeyTable);
					for(int m = 0; m < refs.size(); m++)
					{
						Element ref = doc.createElement(Schema.REFERENCE.toString());
						String[] refData = (String[])refs.get(m);
						ref.setAttribute(Schema.LOCAL.toString(), refData[0]);
						ref.setAttribute(Schema.FOREIGN.toString(), refData[1]);
						fk.appendChild(ref);
					}
					table.appendChild(fk);
				}

				Hashtable<String, Index> indices = getIndices(dbMetaData, curTable, false);
				Enumeration<String> indexNames = indices.keys();
				while(indexNames.hasMoreElements())
				{
					String name = indexNames.nextElement();
					Index index = indices.get(name);
					IndexColumn[] indexColumns = index.getOrderedColumns();

					Element indexElement = doc.createElement(Schema.INDEX.toString());
					indexElement.setAttribute(Schema.NAME.toString(), name);
					indexElement.setAttribute(Schema.UNIQUE.toString(), new Boolean(index.isUnique()).toString());
					indexElement.setAttribute(Schema.TYPE.toString(), new Short(index.getType().getType()).toString());
					indexElement.setAttribute(Schema.PAGES.toString(), new Integer(index.getPages()).toString());
					indexElement.setAttribute(Schema.CARDINALITY.toString(), new Integer(index.getCardinality()).toString());
					if(index.getFilterCondition() != null)
						indexElement.setAttribute(Schema.FILTER_CONDITION.toString(), index.getFilterCondition());

					for(int k = 0; k < indexColumns.length; k++)
					{
						Element colElement = doc.createElement(Schema.INDEX_COLUMN.toString());
						colElement.setAttribute(Schema.NAME.toString(), indexColumns[k].getName());

						indexElement.appendChild(colElement);
					}

					table.appendChild(indexElement);
				}

				databaseNode.appendChild(table);
			}
			doc.appendChild(databaseNode);
		}
		finally
		{
			if(con != null)
			{
				con.close();
				con = null;
			}
		}
	}

	/**
	 * Returns a <code>Hashtable</code> of table indices.
	 * 
	 * @param dbMeta <code>DatabaseMetaData</code>
	 * @param tableName table name
	 * @param onlyUnique inly unique indices?
	 * @return indices
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	private Hashtable<String, Index> getIndices(DatabaseMetaData dbMeta, String tableName, boolean onlyUnique) throws SQLException
	{
		List indexList = new ArrayList();
		Hashtable<String, Index> indices = new Hashtable();
		ResultSet rs = null;
		try
		{
			rs = dbMeta.getIndexInfo(null, dbSchema, tableName, onlyUnique, true);
			while(rs.next())
			{
				String name = rs.getString("INDEX_NAME");
				boolean nonUnique = rs.getBoolean("NON_UNIQUE");
				String colName = rs.getString("COLUMN_NAME").replaceAll("\"", "");
				short colPosition = rs.getShort("ORDINAL_POSITION");
				short type = rs.getShort("TYPE");
				int cardinality = rs.getInt("CARDINALITY");
				int pages = rs.getInt("PAGES");
				String filterCondition = rs.getString("FILTER_CONDITION");
				
				List index = new ArrayList(8);
				index.add(name);
				index.add(nonUnique);
				index.add(colName);
				index.add(colPosition);
				index.add(type);
				index.add(cardinality);
				index.add(pages);
				index.add(filterCondition);

				indexList.add(index);
			}
			
			for(int k = 0; k < indexList.size(); k++)
			{
				List indexData = (List)indexList.get(k);
				String name = (String)indexData.get(0);
				boolean unique = !(Boolean)indexData.get(1);
				String colName = (String)indexData.get(2);
				short colPosition = (Short)indexData.get(3);
				Type type = Type.getTypeForShort(((Short)indexData.get(4)));
				int cardinality = (Integer)indexData.get(5);
				int pages = (Integer)indexData.get(6);
				String filterCondition = (String)indexData.get(7);

				if(!indices.containsKey(name))
					indices.put(name, new Index(name, unique, type, cardinality, pages, filterCondition));

				Index index = indices.get(name);
				index.addColumn(new IndexColumn(colName, colPosition));
			}
		}
		finally
		{
			if(rs != null)
			{
				rs.close();
			}
		}
		return indices;
	}
	
	/**
	 * Generates a XML schema file.
	 * 
	 * @param outputDir output directory
	 * @param config database connection configuration
	 */
	public void generateXMLSchemaFile(String outputDir, TorqueConfig config)
	{
		setProject(new Project());
		
		String dbName = config.getProperty(TorqueConfig.TORQUE_DATABASE_DEFAULT);
		setOutputFile(outputDir + "/" + dbName + "-schema.generated.xml");
		setDbDriver(config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_DRIVER));
		setDbUrl(config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_URL));
		setDbUser(config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_USER));
		setDbPassword(config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_PASSWORD));
		execute();
	}
}