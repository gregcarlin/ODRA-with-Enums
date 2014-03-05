/**
 * 
 */
package tests.jobc;

import static org.junit.Assert.*;

import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.SBQLQuery;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * QueryTC
 * @author Radek Adamus
 *@since 2007-12-15
 *last modified: 2007-12-15
 *@version 1.0
 */
public class QueryTC {
    static SBQLQuery query;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	query = new JOBC("admin", "admin", "localhost", 1521).getSBQLQuery("Car where model = {modelname} and name = {modelname} and power = {power} and married = {ismarried} or test < {power} and price = {price}");
	
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
	String result = query.toString();
	assertTrue(result.equals("Car where model = \"model1\" and name = \"model1\" and power = 10 and married = true or test < 10 and price = 10.0;"));
	
    }
    /**
     * Test method for {@link odra.jobc.SBQLQuery#addIntegerParam(int, java.lang.Integer)}.
     */
    @Test
    public final void testAddParamInteger() {
	try {
	    query.addIntegerParam("power", 10);
	} catch (JOBCException e) {
	    fail("param not exists");
	}
    }
    /**
     * Test method for {@link odra.jobc.SBQLQuery#addRealParam(int, java.lang.Integer)}.
     */
    @Test
    public final void testAddParamReal() {
	try {
	    query.addRealParam("price", 10.0);
	} catch (JOBCException e) {
	    fail("param not exists");
	}
    }

    /**
     * Test method for {@link odra.jobc.SBQLQuery#addBooleanParam(int, java.lang.Boolean)}.
     */
    @Test
    public final void testAddParamBoolean() {
	try {
	    query.addBooleanParam("ismarried", true);
	} catch (JOBCException e) {
	    fail("param not exists");
	}
    }

    /**
     * Test method for {@link odra.jobc.SBQLQuery#addIntegerParam(int, java.lang.String)}.
     */
    @Test
    public final void testAddParamString() {
	try {
	    query.addStringParam("modelname", "model1");
	} catch (JOBCException e) {
	    fail("param not exists");
	}	
    }

    

}
