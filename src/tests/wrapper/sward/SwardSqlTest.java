package tests.wrapper.sward;

import odra.wrapper.Wrapper;
import odra.wrapper.WrapperException;
import odra.wrapper.model.SwardDatabase;
import odra.wrapper.sql.Query;
import org.apache.commons.configuration.ConfigurationException;

/**
 * SWARD query transformation test.
 * 
 * @author jacenty
 * @version 2007-11-29
 * @since 2007-07-26
 */
public class SwardSqlTest
{
	private SwardDatabase model;
	private Query query;
	private String rdql;
	
	private String[] sql = new String[] {
		"select egov.subject, egov.predicate, egov.object from egov",
		"select egov.subject, egov.predicate, egov.object from egov where egov.object = 'x'",
		"select egov.subject, egov.predicate, egov.object from egov where egov.object like '%x%'",
		"select egov.subject, egov.predicate, egov.object from egov where egov.object = 'x' or egov.subject = 'y'",
		"select egov.subject, egov.predicate, egov.object from egov where egov.object = 2 or egov.subject = 'y'",
		"select egov.subject, egov.predicate, egov.object from egov where egov.object = 2 or egov.subject = 'y' and egov.subject <> 'z'",
		"select egov.subject, egov.predicate, egov.object from egov where (egov.object = 2 or egov.subject like '%y%') and egov.subject <> 'z'",
		"select egov.subject, egov.predicate, egov.object from egov where (egov.object = 2 or egov.subject not like '%y%') and egov.subject <> 'z'",
	};
	
	public static void main(String[] args)
	{
		try
		{
			new SwardSqlTest().test();
		}
		catch (WrapperException exc)
		{
			exc.printStackTrace();
		}
		catch (ConfigurationException exc)
		{
			exc.printStackTrace();
		}
	}
	
	private void test() throws WrapperException, ConfigurationException
	{
		model = new SwardDatabase("./conf/sward.schema.properties");
		for(String sql : this.sql)
		{
			query = new Query(sql, model);
			rdql = query.getQueryStringForMode(Wrapper.MODE_SWARD);
			System.out.println(rdql);
		}
	}
}
