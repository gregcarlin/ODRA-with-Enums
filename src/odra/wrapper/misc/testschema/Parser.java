package odra.wrapper.misc.testschema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Import data parser for building a test schema.
 * 
 * @author jacenty
 * @version 2007-07-25
 * @since 2007-02-11
 */
class Parser
{
	private final String prefix = "#";
	private final String femaleSuramesDelimiter = "female.surname";
	private final String maleSurnamesDelimiter = "male.surname";
	private final String femaleNamesDelimiter = "female.name";
	private final String maleNamesDelimiter = "male.name";
	private final String departmentDelimiter = "department";
	private final String salaryDelimiter = "salary";
	private final String locationDelimiter = "location";
	
	private Distributor femaleSurnames = new Distributor();
	private Distributor maleSurnames = new Distributor();
	private Distributor femaleNames = new Distributor();
	private Distributor maleNames = new Distributor();
	private Distributor departments = new Distributor();
	private Distributor salaries = new Distributor();
	private Distributor locations = new Distributor();
	
	private final String dataFilePath = "res/wrapper/data.txt";
		
	void parse()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataFilePath)), "utf-8"));
			String line;
			Distributor current = null;
			while((line = reader.readLine()) != null)
			{
				if(line.startsWith(prefix))
				{
					line = line.replaceAll(prefix, "");
					if(line.equals(femaleNamesDelimiter))
						current = femaleNames;
					else if(line.equals(femaleSuramesDelimiter))
						current = femaleSurnames;
					else if(line.equals(maleNamesDelimiter))
						current = maleNames;
					else if(line.equals(maleSurnamesDelimiter))
						current = maleSurnames;
					else if(line.equals(departmentDelimiter))
						current = departments;
					else if(line.equals(salaryDelimiter))
						current = salaries;
					else if(line.equals(locationDelimiter))
						current = locations;
				}
				else
				{
					String[] split = line.split("\t");
					String value = capitalize(split[0]);
					int occurences = Integer.parseInt(split[1].trim());
					if(
							current.equals(femaleNames) ||
							current.equals(femaleSurnames) ||
							current.equals(maleNames) ||
							current.equals(maleSurnames))
						occurences = normalize(occurences);
					
					current.add(value, occurences);
				}
			}
		}
		catch(IOException exc)
		{
			exc.printStackTrace();
		}
	}
	
	private int normalize(int occurences)
	{
		return occurences / 1000;
	}
	
	private String capitalize(String value)
	{
		String result = value.trim();
    return result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase();
	}

	Distributor getFemaleNames()
	{
		return femaleNames;
	}

	Distributor getFemaleSurnames()
	{
		return femaleSurnames;
	}

	Distributor getMaleNames()
	{
		return maleNames;
	}

	Distributor getMaleSurnames()
	{
		return maleSurnames;
	}
	
	Distributor getDepartments()
	{
		return departments;
	}
	
	Distributor getSalaries()
	{
		return salaries;
	}
	
	Distributor getLocations()
	{
		return locations;
	}
}