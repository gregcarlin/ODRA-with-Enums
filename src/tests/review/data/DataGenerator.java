package tests.review.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * An internal utility class for generating data for the eGov-Bus review demo. Output of the class is 
 * displayed in stdout, prepared relational inserts are included in appropriate SQL scripts in res/review/sql.
 * <br />
 * Connection parameters are hardcoded, hence the code is not portable.
 * 
 * @author jacenty
 * @version 2008-01-28
 * @since 2008-01-28
 */
public class DataGenerator
{
	private final Table table;
	private final int recordCount;
	
	private final Random random = new Random();
	private final DateGenerator dateGenerator = new DateGenerator();
	
	private final Date minDate;
	private final Date maxDate;
	
	private final String MALE = "M";
	private final String FEMALE = "F";
	
	public static final long HOUR = 1000 * 60 * 60;
	public static final long DAY = HOUR * 24;
	
	private enum Table
	{
		CITIZENS("CITIZENS", "review.citizens"),
		CITIES("CITIES", "review.addresses"),
		STREETS("STREETS", "review.addresses"),
		ADDRESSES("ADDRESSES", "review.addresses"),
		HISTORY("HISTORY", "review.addresses"),
		;
		
		private final String name;
		private final String database;
		
		Table(String name, String database)
		{
			this.name = name;
			this.database = database;
		}
		
		static Table getForName(String name)
		{
			for(Table table : values())
				if(table.name.equalsIgnoreCase(name))
					return table;
			
			throw new RuntimeException("Unknow table '" + name + "'");
		}
	}
	
	public DataGenerator(Table table, int recordCount) throws ParseException
	{
		this.table = table;
		this.recordCount = recordCount;
		
		minDate = DateFormat.getDateInstance().parse("1900-01-01");
		maxDate = new Date();
	}
	
	public static void main(String[] args)
	{
		try
		{
			String tableName = args[0];
			int recordCount = Integer.parseInt(args[1]);
			
			DataGenerator generator = new DataGenerator(Table.getForName(tableName), recordCount);
			generator.generate();
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
	}
	
	private Statement connect() throws SQLException, ClassNotFoundException
	{
		Class.forName("org.postgresql.Driver");
		Connection connection = DriverManager.getConnection(
			"jdbc:postgresql://localhost:5432/" + table.database, 
			"postgres", 
			"postgresik2005");
		return connection.createStatement();
	}
	
	private void generate() throws SQLException, ClassNotFoundException, ParseException
	{
		if(table == Table.CITIZENS)
			generateCitizens();
		else if(table == Table.CITIES)
			generateCities();
		else if(table == Table.STREETS)
			generateStreets();
		else if(table == Table.ADDRESSES)
			generateAddresses();
		else if(table == Table.HISTORY)
			generateHistory();
	}
	
	private Object[][] getCitizens() throws ParseException, SQLException, ClassNotFoundException
	{
		Object[][] citizens = new Object[][] {};
		
		DataGenerator generator = new DataGenerator(Table.CITIZENS, 0);
		Statement db = generator.connect();
		
		ResultSet rs = db.executeQuery(
			"select count(*) from CITIZENS");
		if(rs.next())
			citizens = new Object[rs.getInt(1)][];
		
		rs = db.executeQuery(
			"select PESEL, BIRTH_DATE from CITIZENS");
		int i = 0;
		while(rs.next())
			citizens[i++] = new Object[] {rs.getString("PESEL"), new Date(rs.getDate("BIRTH_DATE").getTime())};
		
		db.getConnection().close();
		
		return citizens;
	}
	
	private void generateHistory() throws ParseException, SQLException, ClassNotFoundException
	{
		Statement db = connect();
		
		String[] addresses = new String[] {};
		ResultSet rs = db.executeQuery(
			"select count(*) from ADDRESSES");
		if(rs.next())
			addresses = new String[rs.getInt(1)];
		rs = db.executeQuery(
			"select ID from ADDRESSES");
		int i = 0;
		while(rs.next())
			addresses[i++] = rs.getString("ID");
		
		int historyCount;
		Object[][] citizens = getCitizens();
		for(historyCount = 0; historyCount < citizens.length; historyCount++)
		{
			Object[] citizen = citizens[historyCount];
			String address = (String)randomValue(addresses);
			
			System.out.println(
				"insert into HISTORY (ID, ADDRESS_ID, PESEL, MOVING_DATE, ORDINAL) values (" +
				"'" + historyCount + "', " +
				"'" + address + "', " +
				"'" + (String)citizen[0] + "', " +
				"'" + DateFormat.getDateInstance(DateFormat.MEDIUM).format((Date)citizen[1]) + "', " +
				"'1'" +
				");");
		}
		
		for(Object[] citizen : citizens)
		{
			if(random.nextBoolean())
			{
				int movingCount = random.nextInt(5);
				Date movingDate = (Date)citizen[1];
				
				for(i = 0; i < movingCount; i++)
				{
					String address = (String)randomValue(addresses);
					movingDate = nextMovingDate(movingDate);
					if(movingDate.after(maxDate))
						break;
					
					System.out.println(
						"insert into HISTORY (ID, ADDRESS_ID, PESEL, MOVING_DATE, ORDINAL) values (" +
						"'" + (++historyCount) + "', " +
						"'" + address + "', " +
						"'" + (String)citizen[0] + "', " +
						"'" + DateFormat.getDateInstance(DateFormat.MEDIUM).format(movingDate) + "', " +
						"'" + (i + 2) + "'" +
						");");
				}
			}
		}
		
		db.getConnection().close();
	}
	
	private Date nextMovingDate(Date prevMovingDate)
	{
		long time = prevMovingDate.getTime();
		int days = random.nextInt(1825);//5 years
		for(int i = 0; i < days; i++)
			time += DAY;
		
		return new Date(time);
	}
	
	private void generateAddresses() throws SQLException, ClassNotFoundException
	{
		Statement db = connect();
		
		String[] streets = new String[] {};
		ResultSet rs = db.executeQuery(
			"select count(*) from STREETS");
		if(rs.next())
			streets = new String[rs.getInt(1)];
		rs = db.executeQuery(
			"select ID from STREETS");
		int i = 0;
		while(rs.next())
			streets[i++] = rs.getString("ID");
		
		for(i = 0; i < recordCount; i++)
		{
			String street = (String)randomValue(streets);
			System.out.println(
				"insert into ADDRESSES (ID, STREET_ID, NUMBER) values (" +
				"'" + i + "', " +
				"'" + street + "', " +
				"'" + (i + 1) + "'" +
				");");
		}
		
		db.getConnection().close();
	}
	
	private void generateStreets() throws SQLException, ClassNotFoundException
	{
		Statement db = connect();
		
		String[] cities = new String[] {};
		ResultSet rs = db.executeQuery(
			"select count(*) from CITIES");
		if(rs.next())
			cities = new String[rs.getInt(1)];
		rs = db.executeQuery(
			"select ID from CITIES");
		int i = 0;
		while(rs.next())
			cities[i++] = rs.getString("ID");
		
		for(i = 0; i < recordCount; i++)
		{
			String city = (String)randomValue(cities);
			System.out.println(
				"insert into STREETS (ID, CITY_ID, NAME) values (" +
				"'" + i + "', " +
				"'" + city + "', " +
				"'street_" + i + "'" +
				");");
		}
		
		db.getConnection().close();
	}
	
	private void generateCities() throws SQLException, ClassNotFoundException
	{
		Statement db = connect();
		
		String[] countries = new String[] {};
		ResultSet rs = db.executeQuery(
			"select count(*) from COUNTRIES");
		if(rs.next())
			countries = new String[rs.getInt(1)];
		rs = db.executeQuery(
			"select CODE from COUNTRIES");
		int i = 0;
		while(rs.next())
			countries[i++] = rs.getString("CODE");
		
		for(i = 0; i < recordCount; i++)
		{
			String country = (String)randomValue(countries);
			System.out.println(
				"insert into CITIES (ID, COUNTRY_CODE, NAME) values (" +
				"'" + i + "', " +
				"'" + country + "', " +
				"'city_" + country + "_" + i + "'" +
				");");
		}
		
		db.getConnection().close();
	}
	
	private void generateCitizens() throws SQLException, ClassNotFoundException, ParseException
	{
		Statement db = connect();
		
		String[] countries = new String[] {};
		ResultSet rs = db.executeQuery(
			"select count(*) from COUNTRIES");
		if(rs.next())
			countries = new String[rs.getInt(1)];
		rs = db.executeQuery(
			"select CODE from COUNTRIES");
		int i = 0;
		while(rs.next())
			countries[i++] = rs.getString("CODE");
		
		Date[] birthDates = new Date[recordCount];
		for(i = 0; i < recordCount; i++)
			birthDates[i] = dateGenerator.getRandomValue();
		Arrays.sort(birthDates);
		
		String[] sexes = new String[recordCount];
		for(i = 0; i < recordCount; i++)
			sexes[i] = randomSex();
		
		String[] pesels = new String[recordCount];
		for(i = 0; i < recordCount; i++)
			pesels[i] = randomPesel(birthDates[i], sexes[i]);
		
		for(i = 0; i < recordCount; i++)
		{
			Date birthDate = birthDates[i];
			String motherPesel = parentPesel(MALE, birthDate, pesels, birthDates);
			String fatherPesel = parentPesel(FEMALE, birthDate, pesels, birthDates);
			
			System.out.println(
				"insert into CITIZENS (PESEL, NAME1, NAME2, SURNAME, BIRTH_DATE, SEX, COUNTRY_CODE, FATHER_PESEL, MOTHER_PESEL) values (" +
				"'" + pesels[i] + "', " +
				"'name1_" + i + "', " +
				(random.nextBoolean() ? "'name2_" + i + "', " : "null, ") +
				"'surname_" + i + "', " +
				"'" + DateFormat.getDateInstance(DateFormat.MEDIUM).format(birthDate) + "', " +
				"'" + sexes[i] + "', " +
				"'" + randomValue(countries) + "', " +
				(fatherPesel != null ? ("'" + fatherPesel + "', ") : "null, ") +
				(motherPesel != null ? ("'" + motherPesel + "'") : "null") + 
				");");
		}
		
		db.getConnection().close();
	}
	
	private String parentPesel(String parentSex, Date birthDate, String[] pesels, Date[] birthDates)
	{
		int birthYear = Integer.parseInt(DateFormat.getDateInstance(DateFormat.MEDIUM).format(birthDate).split("-")[0]);
		
		int retryCount = 10;
		for(int i = 0; i < retryCount; i++)
		{
			int index = random.nextInt(pesels.length);
			
			Date parentBirthDate = birthDates[index];
			int parentBirthYear = Integer.parseInt(DateFormat.getDateInstance(DateFormat.MEDIUM).format(parentBirthDate).split("-")[0]);
			
			int ageDifference = birthYear - parentBirthYear;
			if(ageDifference > 15 && ageDifference < 50)
			{
				String pesel = pesels[index];
				int sexDigit = Integer.parseInt(pesel.substring(9, 10));
				if(parentSex.equals(MALE) && sexDigit % 2 == 1 || parentSex.equals(FEMALE) && sexDigit % 2 == 0)
					return pesel;
			}
		}
		
		return null;
	}
	
	private String randomPesel(Date date, String sex)
	{
		int sexDigit;
		do
		{
			sexDigit = randomDigit();
		}
		while(sex.equals(MALE) ? sexDigit % 2 == 0 : sexDigit % 2 == 1);
		
		String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
		String[] split = dateString.split("-");
		int year = Integer.parseInt(split[0]);
		int month = Integer.parseInt(split[1]);
		
		if(year >= 2000)
			month += 20;
		
		String yearString = Integer.toString(year).substring(2);
		String monthString = Integer.toString(month);
		if(monthString.length() < 2)
			monthString = "0" + monthString;
		String dayString = split[2];
		
		String pesel = yearString + monthString + dayString;
		for(int i = 0; i < 3; i++)
			pesel += randomDigit();
		pesel += sexDigit;
		pesel += randomDigit();
		
		return pesel;
	}
	
	private int randomDigit()
	{
		return random.nextInt(10);
	}
	
	private String randomSex()
	{
		return random.nextBoolean() ? MALE : FEMALE;
	}
	
	private Object randomValue(Object[] values)
	{
		return values[random.nextInt(values.length)];
	}
}
