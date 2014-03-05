package tests.review.demo.objects;

import java.util.Date;
import java.util.Vector;
import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.Result;
import odra.sbql.results.runtime.StructResult;
import tests.review.demo.Database;

/**
 * A citizen container.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2008-01-29
 */
public class Citizen
{
	private Database db;
	
	private final String pesel;
	private final Date birthDate;
	private final String surname;
	private final String name1;
	private final String name2;
	private final String sex;
	private final String fatherPesel;
	private final String motherPesel;
	private final Country nationality;
	
	public Citizen(String pesel, Database db) throws JOBCException
	{
		this.db = db;
		this.pesel = pesel;
		
		Result result = db.execute("citizens where pesel = \"" + pesel + "\"");
		Result fields = result.fields();
		
		birthDate = fields.getByName("birth_date").getDate();
		surname = fields.getByName("surname").getString();
		name1 = fields.getByName("name1").getString();
		name2 = !fields.getByName("name2").isBag() ? fields.getByName("name2").getString() : null;
		sex = fields.getByName("sex").getString();
		fatherPesel = !fields.getByName("father_pesel").isBag() ? fields.getByName("father_pesel").getString() : null;
		motherPesel = !fields.getByName("mother_pesel").isBag() ? fields.getByName("mother_pesel").getString() : null;
		nationality = new Country(fields.getByName("country_code").getString(), db);
	}
	
	public Citizen getMother() throws JOBCException
	{
		return motherPesel != null ? new Citizen(motherPesel, db) : null;
	}
	
	public Citizen getFather() throws JOBCException
	{
		return fatherPesel != null ? new Citizen(fatherPesel, db) : null;
	}
	
	public Vector<Citizen> getChildren() throws JOBCException
	{
		Vector<Citizen> children = new Vector<Citizen>();

		Result result = db.execute(
			"(citizens where father_pesel = \"" + pesel + "\" or mother_pesel = \"" + pesel + "\").pesel");
		Result fields = result.fields();
		for(int i = 0; i < fields.size(); i++)
			children.addElement(new Citizen(fields.get(i).getString(), db));
		
		return children;
	}
	
	public Vector<Object[]> getAddressHistory() throws JOBCException
	{
		Vector<Object[]> addresses = new Vector<Object[]>();

		Result result = db.execute(
			"((history where pesel = \"" + pesel + "\") orderby ordinal).(deref(address_id), deref(moving_date))");
		if(result.isComplex())
		{
			Result row = result.fields();
			addresses.addElement(new Object[] {new Address(row.get(0).getInteger(), db), row.get(1).getDate()});
		}
		else if(result.isBag())
		{
			Result[] rows = result.toArray();
			for(int i = 0; i < rows.length; i++)
			{
				Result row = rows[i].fields();
				addresses.addElement(new Object[] {new Address(row.get(0).getInteger(), db), row.get(1).getDate()});
			}
		}

		return addresses;
	}

	public Object[] getCurrentAddress() throws JOBCException
	{
		Result result = db.execute(
			"(history where pesel = \"" + pesel + "\" and ordinal = max((history where pesel = \"" + pesel + "\").ordinal))" + 
			".(deref(address_id), deref(moving_date))");
		Result fields = result.fields();
		
		return new Object[] {new Address(fields.get(0).getInteger(), db), fields.get(1).getDate()};
	}

	public Date getBirthDate()
	{
		return birthDate;
	}

	public String getName1()
	{
		return name1;
	}

	public String getName2()
	{
		return name2;
	}

	public String getPesel()
	{
		return pesel;
	}

	public String getSex()
	{
		return sex;
	}

	public String getSurname()
	{
		return surname;
	}

	public Country getNationality()
	{
		return nationality;
	}
}
