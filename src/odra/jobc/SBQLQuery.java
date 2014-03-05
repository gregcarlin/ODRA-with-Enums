/**
 * 
 */
package odra.jobc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SBQLQuery
 * 
 * @author Radek Adamus
 * @since 2007-12-15  
 * @version 2007-12-16
 */
public class SBQLQuery {
    private String rawquery;

    private Map params = new HashMap();


     /**
      * The constructor
     * @param query the query string
     */
    SBQLQuery(String query) {
	this.rawquery = query;
	
    }

    /**
     * Adds integer param value
     * @param name - param name
     * @param param - param value
     * @throws JOBCException
     */
    public void addIntegerParam(String name , int param) throws JOBCException{
	if(existsParam(name))
	{	    
	    params.put(name,Integer.toString(param));
	}else throw new JOBCException(WRONG_PARAM_NAME + name, JOBCException.QUERY_PARAMETER_ERROR);
    }

    /**
     * Adds boolean param value
     * @param name - param name
     * @param param - param value
     * @throws JOBCException
     */
    public void addBooleanParam(String name , boolean param) throws JOBCException{
	if(existsParam(name))
	{	    
	    params.put(name,Boolean.toString(param));
	}else throw new JOBCException(WRONG_PARAM_NAME + name, JOBCException.QUERY_PARAMETER_ERROR);
	
    }

    /**
     * Adds string param value
     * @param name - param name
     * @param param - param value
     * @throws JOBCException
     */
    public void addStringParam(String name , String param) throws JOBCException{
	if(existsParam(name))
	{	    
	    params.put(name,DQUOTES + param + DQUOTES);
	}else throw new JOBCException(WRONG_PARAM_NAME + name, JOBCException.QUERY_PARAMETER_ERROR);
	
    }
    
    /**
     * Adds real param value
     * @param name - param name
     * @param param - param value
     * @throws JOBCException
     */
    public void addRealParam(String name , double param) throws JOBCException{
	if(existsParam(name))
	{	    
	    params.put(name,Double.toString(param));
	}else throw new JOBCException(WRONG_PARAM_NAME + name, JOBCException.QUERY_PARAMETER_ERROR);
	
    }
    
    /**
     * Adds Date param value
     * @param name - param name
     * @param param - param value
     * @throws JOBCException
     */
    public void addDateParam(String name , Date param) throws JOBCException{
	if(existsParam(name))
	{	   
	    
	    String strdate = Utils.date2Str(param); 
	    params.put(name, strdate);
	}else throw new JOBCException(WRONG_PARAM_NAME + name, JOBCException.QUERY_PARAMETER_ERROR);
	
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
	
	return prepare();
    }

    private boolean existsParam(String name) {
	String match = PARAM_MATCH_PREFIX + name + PARAM_MATCH_SUFFIX;
	if(rawquery.matches(match))
	    return true;
	return false;
    }
    
    public String prepare() {
		
	String query = this.rawquery.trim();
	if (!query.endsWith(END_STATEMENT)) {
	    query = query.concat(END_STATEMENT);
	}
	for(Iterator i = this.params.keySet().iterator(); i.hasNext();){
	    String name = (String) i.next();
	     
	    String match = PARAM_PREFIX + name + PARAM_SUFFIX;
	    query = query.replaceAll(match, (String)this.params.get(name) );
	}
	return query;
    }

   
    private static String END_STATEMENT = ";";
    private static String PARAM_PREFIX = "\\{";
    private static String PARAM_SUFFIX = "\\}";
    private static String DQUOTES = "\"";
    private static String PARAM_MATCH_PREFIX = ".*" + PARAM_PREFIX;
    private static String PARAM_MATCH_SUFFIX = PARAM_SUFFIX + ".*";
    
    private static String WRONG_PARAM_NAME = "Query does not have parameter named: "; 
    
}
