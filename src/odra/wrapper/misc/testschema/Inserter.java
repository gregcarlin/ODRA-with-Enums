package odra.wrapper.misc.testschema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Vector;

import odra.wrapper.WrapperException;
import odra.wrapper.config.TorqueConfig;

/**
 * SQL inserter class
 * 
 * @author jacenty
 * @version 2007-07-09
 * @since 2007-02-11
 */
public class Inserter
{
	public enum Mode
	{
		EMPLOYEES,
		CARS
	}
	
	private Parser parser = new Parser();
	private Statement statement;
	
	public static void main(String[] args)
	{
		Mode mode = null;
		int employeeCount = -1;
		try
		{
			mode = Mode.valueOf(args[0].toUpperCase());
			employeeCount = Integer.parseInt(args[1]);
		}
		catch(Exception exc)
		{
			System.out.println("syntax: Inserter <mode> <employee count>");
			System.out.print("available modes: ");
			for(Mode availableMode : Mode.values())
				System.out.print(availableMode.name().toLowerCase() + " ");
			System.out.println();
			System.exit(1);
		}
		
		try
		{
			long start = System.currentTimeMillis();
			System.out.println("Wrapper test data population started...");
			
			Inserter inserter = new Inserter();
			inserter.connect();
			if(mode.equals(Mode.EMPLOYEES))
			{
				inserter.cleanupEmployees();
				inserter.insertEmployees(employeeCount);
			}
			else if(mode.equals(Mode.CARS))
			{
				inserter.cleanupCars();
				inserter.insertCars(employeeCount);
			}
			
			System.out.println("Wrapper test data population finished in " + (System.currentTimeMillis() - start) + " ms...");
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
		}
	}
	
	private void connect() throws Exception
	{
		String configFilePath = "./conf/" + TorqueConfig.CONFIG_FILE_NAME;
		TorqueConfig config = new TorqueConfig(configFilePath);
		
		try
		{
			Class.forName(config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_DRIVER));
		}
		catch(ClassNotFoundException exc)
		{
			throw new WrapperException("JDBC driver class ('" + config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_DRIVER) + "') not found", exc, WrapperException.Error.DRIVER_CLASS_NOT_FOUND);
		}

		Connection connection;
		try
		{
			connection = DriverManager.getConnection(
				config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_URL), 
				config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_USER), 
				config.getProperty(TorqueConfig.TORQUE_DSFACTORY_XXX_CONNECTION_PASSWORD));
			statement = connection.createStatement();
		}
		catch(SQLException exc)
		{
			throw new WrapperException("Connecting to database", exc, WrapperException.Error.SQL_CONNECTION);
		}
		System.out.println("\tconnected");
	}
	
	private void cleanupEmployees() throws SQLException
	{
		System.out.println("\t" + statement.executeUpdate("delete from employees") + " records deleted from employees");
		System.out.println("\t" + statement.executeUpdate("delete from departments") + " records deleted from departments");
		System.out.println("\t" + statement.executeUpdate("delete from locations") + " records deleted from locations");
	}
	
	private void cleanupCars() throws SQLException
	{
		System.out.println("\t" + statement.executeUpdate("delete from cars") + " records deleted from cars");
		System.out.println("\t" + statement.executeUpdate("delete from models") + " records deleted from models");
		System.out.println("\t" + statement.executeUpdate("delete from makes") + " records deleted from makes");
	}
	
	private void insertEmployees(int employeeCount) throws SQLException
	{
		parser.parse();
		
		Distributor femaleNames = parser.getFemaleNames();
		Distributor femaleSurnames = parser.getFemaleSurnames();
		Distributor maleNames = parser.getMaleNames();
		Distributor maleSurnames = parser.getMaleSurnames();
		Distributor departments = parser.getDepartments();
		Distributor salaries = parser.getSalaries();
		Distributor locations = parser.getLocations();
		Distributor infos = new InfoDistributor();
		
		Vector<String> uniqueLocations = locations.getUniqueValues();
		for(int i = 0; i < uniqueLocations.size(); i++)
			statement.executeUpdate(
				"insert into locations (id, name) values (" +
				"'" + (i + 1) + "', " +
				"'" + uniqueLocations.get(i) + "')");
		System.out.println("\t" + uniqueLocations.size() + " locations created");
		
		Vector<String> uniqueDepartments = departments.getUniqueValues();
		for(int i = 0; i < uniqueDepartments.size(); i++)
		{
			int locationId = -1;
			ResultSet rs = statement.executeQuery(
				"select id from locations where " +
				"name = '" + locations.getRandomValue() + "'");
			if(rs.next())
				locationId = rs.getInt(1);
			
			statement.executeUpdate(
				"insert into departments (id, name, location_id) values (" +
				"'" + (i + 1) + "', " +
				"'" + uniqueDepartments.get(i) + "', " +
				"'" + locationId + "' " +
				")");
		}
		System.out.println("\t" + uniqueDepartments.size() + " departments created");
		
		Random random = new Random();
		Distributor birthDates = new BirthDateDistributor(); 
		for(int i = 0; i < employeeCount; i++)
		{
			int departmentId = -1;
			ResultSet rs = statement.executeQuery(
				"select id from departments where " +
				"name = '" + departments.getRandomValue() + "'");
			if(rs.next())
				departmentId = rs.getInt(1);
			
			boolean male = random.nextBoolean();
			Distributor names = femaleNames;
			Distributor surnames = femaleSurnames;
			String sex = "F";
			if(male)
			{
				names = maleNames;
				surnames = maleSurnames;
				sex = "M";
			}
			
			statement.executeUpdate(
				"insert into employees (id, name, surname, sex, salary, info, birth_date, department_id) values (" +
				"'" + (i + 1) + "', " +
				"'" + names.getRandomValue() + "', " +
				"'" + surnames.getRandomValue() + "', " +
				"'" + sex + "', " +
				"'" + salaries.getRandomValue() + "', " +
				"'" + infos.getRandomValue() + "', " +
				"'" + birthDates.getRandomValue() + "', " +
				"'" + departmentId + "')");
		}
		System.out.println("\t" + employeeCount + " employees created");
	}
	
	private void insertCars(int employeeCount) throws SQLException
	{
		Distributor makeDistributor = new MakeDistributor();
		Distributor yearDistributor = new YearDistributor();
		Distributor colourDistributor = new ColourDistributor();
		
		Vector<String> uniqueMakes = makeDistributor.getUniqueValues();
		for(int i = 0; i < uniqueMakes.size(); i++)
			statement.executeUpdate(
				"insert into makes (id, name) values (" +
				"'" + (i + 1) + "', " +
				"'" + uniqueMakes.get(i) + "')");
		System.out.println("\t" + uniqueMakes.size() + " makes created");
		
		int counter = 0;
		for(String make : uniqueMakes)
		{
			Distributor modelDistributor = new ModelDistributor(make);
			int makeId = -1;
			ResultSet rs = statement.executeQuery(
				"select id from makes where " +
				"name = '" + make + "'");
			if(rs.next())
				makeId = rs.getInt("id");
			Vector<String> uniqueModels = modelDistributor.getUniqueValues();
				for(int i = 0; i < uniqueModels.size(); i++)
				statement.executeUpdate(
					"insert into models (id, make_id, name) values (" +
					"'" + (++counter) + "', " +
					"'" + makeId + "', " +
					"'" + uniqueModels.get(i) + "')");
		}
		System.out.println("\t" + counter + " models created");
		
		counter = 0;
		for(int i = 0; i < employeeCount; i++)
		{
			String make = makeDistributor.getRandomValue();
			ModelDistributor modelDistributor = new ModelDistributor(make);
			
			int makeId = -1;
			ResultSet rs = statement.executeQuery(
				"select id from makes where " +
				"name = '" + make + "'");
			if(rs.next())
				makeId = rs.getInt(1);
			
			int modelId = -1;
			rs = statement.executeQuery(
				"select id from models where " +
				"make_id = '" + makeId + "' and " +
				"name = '" + modelDistributor.getRandomValue() + "'");
			if(rs.next())
				modelId = rs.getInt(1);
			
			statement.executeUpdate(
				"insert into cars (id, owner_id, model_id, colour, year) values (" +
				"'" + (++counter) + "', " +
				"'" + (i + 1) + "', " +
				"'" + modelId + "', " +
				"'" + colourDistributor.getRandomValue() + "', " +
				"'" + yearDistributor.getRandomValue() + "')");
		}
		System.out.println("\t" + counter + " cars created");
	}
}
