package tests.wrapper.sward;

import java.util.Vector;
import swardAPI.Sward;
import swardAPI.SwardScan;

/**
 * SWARD tests.
 * 
 * @author jacenty
 * @version 2007-11-28
 * @since 2007-05-30
 */
public class SwardTest
{
	public static void main(String[] args)
	{
		String[] queries = new String[] {
			"SELECT ?s,?p,?val2 FROM <http://udbl.it.uu.se/upv/eGov/> WHERE " + 
			"(?s,<http://www.egov_project.org/GovMLSchema#Subject>,?val1), " + 
			"(?p,<http://www.w3.org/2000/01/rdf-schema#domain>, <http://udbl.it.uu.se/schemas/eGovern#LifeEvent>)," + 
			"(?s,?p,?val2) AND " + 
			"?p != <http://www.egov_project.org/GovMLSchema#Subject> AND " + 
			"?val1 =~ '%married%'", 
			
			"select ?predicate, ?object from <http://udbl.it.uu.se/upv/eGov/> where " + 
			"(?subject, ?predicate, ?object) AND ?object =~ \"%married%\""};
		
		SwardScan res;
		Sward sward = new Sward();
		sward.connect("eGov", "test_login", "12345");
		for(String query : queries)
		{
			System.out.println(query);
			res = sward.query(query);
			while(!res.eof())
			{
				System.out.println("New result row: ");
				System.out.println("=============== ");
				for(Object element : res.next())
				{
					Vector pair = (Vector)element;
					System.out.println("Variable " + (String)pair.elementAt(0) + " has value " + (String)pair.elementAt(1));
				}
			}
			System.out.println();
		}
		sward.disconnect("eGov");
	}
}
