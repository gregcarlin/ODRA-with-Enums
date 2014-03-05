package odra.sbql.results.runtime;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Date result. 
 * @author jacenty
 * @version   2007-03-21
 * @since   2007-03-19
 */
public class DateResult extends ComparableResult
{
	public Date value;

	public DateResult(Date value)
	{
		this.value = value;
	}

	public SingleResult[] fieldsToArray()
	{
		return new SingleResult[] {this};
	}

	public int compareTo(Object sres)
	{
	    assert sres instanceof DateResult: "param instanceof DateResult";
		Date dvalue;
		
		dvalue = ((DateResult)sres).value;		

		return value.compareTo(dvalue);
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof DateResult) {
			return value.equals(((DateResult) arg0).value); 
		}
		
		return false;
	}

	public int hashCode() {
		return value.hashCode();
	}
	
	
	/**
	 * Formats the date string.
	 * 
	 * @return string representation.
	 */
	public String format()
	{
		String base = DateFormat.getDateTimeInstance().format(value);
		try
		{
			String ms = Long.toString(value.getTime() - DateFormat.getDateTimeInstance().parse(base).getTime());
			while(ms.length() < 3)
				ms += "0";
			return base + "." + ms;
		}
		catch(ParseException exc)
		{
				
			return base;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		return format();
	}
	
}
