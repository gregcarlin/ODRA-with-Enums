package tests.review.demo.objects;

import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.Result;
import tests.review.demo.Database;

/**
 * An address container.
 * 
 * @author jacenty
 * @version 2008-01-30
 * @since 2008-01-29
 */
public class Address
{
	private final int id;

	private final String street;
	private final String houseNumber;
	private final String city;
	private final String country;
	
	public Address(int id, Database db) throws JOBCException
	{
		this.id = id;
		
		Result result = db.execute(
			"((addresses where id = " + id + ") as a join " + 
			"(streets where id = a.street_id) as s join " + 
			"(cities where id = s.city_id) as c join " + 
			"(countries where code = c.country_code) as cc)" + 
			".(deref(s.name), deref(a.house_number), deref(c.name), deref(cc.name))");
		Result fields = result.fields();
		street = fields.get(0).getString();
		houseNumber = fields.get(1).getString();
		city = fields.get(2).getString();
		country = fields.get(3).getString();
	}

	public String getCity()
	{
		return city;
	}

	public String getCountry()
	{
		return country;
	}

	public String getHouseNumber()
	{
		return houseNumber;
	}

	public int getId()
	{
		return id;
	}

	public String getStreet()
	{
		return street;
	}

	@Override
	public String toString()
	{
		return country + ", " + city + ", " + street + " " + houseNumber;
	}
}
