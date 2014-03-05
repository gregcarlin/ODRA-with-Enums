/**
 * 
 */
package tests.jobc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.Result;
import odra.jobc.SBQLQuery;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SBQLResultTC
 * 
 * @author Radek Adamus
 * @since 2007-12-13 last modified: 2007-12-13
 * @version 1.0
 */
public class ResultTC {

    static Result namedresult;

    static Result unnamedresult;

    static Result oneresult;

    static Result countresult;

    static final String QUERY = "((Car as c join c.owner.Person as o).(deref(c) as c, deref(o) as o) groupas co) groupas cco;";

    static final String QUERY_NO_NAME = "(Car as c join c.owner.Person as o).(deref(c), deref(o));";

    static final String COUNT_QUERY_NO_NAME = "count (Car as c join c.owner.Person as o);";

    static final String QUERY_ONE_RESULT = "((Car as c join c.owner.Person as o).(deref(c), deref(o)))[1];";
    static final String QUERY_WHERE = "Car where model = \"model0\"  ";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	JOBC db = new JOBC("admin", "admin", "localhost", 1521);
	db.connect();
	db.setCurrentModule("admin.test");
	namedresult = db.execute(QUERY);
	unnamedresult = db.execute(QUERY_NO_NAME);
	countresult = db.execute(COUNT_QUERY_NO_NAME);
	oneresult = db.execute(QUERY_ONE_RESULT);
	Result res = db.execute(QUERY_WHERE);
	assertTrue(countresult.size() == 1);
	assertTrue(namedresult.size() == countresult.getInteger());
	assertTrue(unnamedresult.size() == countresult.getInteger());
	assertTrue(oneresult.size() == 1);
	db.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    /**
     * Test method for {@link odra.jobc.Result#getName()}.
     */
    @Test
    public final void testGetName() {
	String name = namedresult.getName();
	assertEquals("cco", name);

    }

    /**
     * Test method for {@link odra.jobc.Result#getString()}.
     */
    @Test
    public final void testGetString() {
	Result res = namedresult.getByName("cco").getByName("co").getByName(
		"c").getByName("model");
	assertTrue("size > 0", res.size() > 0);
	for (Result ires : res.toArray()) {
	    try {
		ires.getString();
	    } catch (JOBCException e) {
		assertFalse("result must be of type string", false);
	    }
	}
    }

    /**
     * Test method for {@link odra.jobc.Result#getInteger()}.
     */
    @Test
    public final void testGetInteger() {
	Result res = namedresult.getByName("cco").getByName("co").getByName(
		"c").getByName("power");
	
	assertTrue("size > 0", res.size() > 0);
	
	for (Result ires : res.toArray()) {
	    try {
		ires.getInteger();		
	    } catch (JOBCException e) {
		assertFalse("result must be of type integer", false);
	    }
	}
	
    }

    /**
     * Test method for {@link odra.jobc.Result#getReal()}.
     */
    @Test
    public final void testGetReal() {
//	fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link odra.jobc.Result#getBoolean()}.
     */
    @Test
    public final void testGetBoolean() {
	Result res = namedresult.getByName("cco").getByName("co").getByName(
		"o");
	res = res.getByName("married");
	assertTrue("size > 0", res.size() > 0);
	for (Result ires : res.toArray()) {
	    try {
		ires.getBoolean();
	    } catch (JOBCException e) {
		assertFalse("result must be of type boolean", false);
	    }
	}
    }

    /**
     * Test method for {@link odra.jobc.Result#getDate()}.
     */
    @Test
    public final void testGetDate() {
	Result res = namedresult.getByName("cco").getByName("co").getByName(
		"c").getByName("year");
	assertTrue("size > 0", res.size() > 0);
	for (Result ires : res.toArray()) {
	    try {
		ires.getDate();
	    } catch (JOBCException e) {
		assertFalse("result must be of type date", false);
	    }
	}
    }

    /**
     * Test method for {@link odra.jobc.Result#isPrimitive()}.
     */
    @Test
    public final void testIsPrimitive() {
	Result res = namedresult.getByName("cco").getByName("co").getByName(
		"c").getByName("power");
	assertTrue("size > 0", res.size() > 0);
	for (Result ires : res.toArray()) {
	    assertTrue("result must be primitive", ires.isPrimitive());
	}
    }

    /**
     * Test method for {@link odra.jobc.Result#isComplex()}.
     */
    @Test
    public final void testIsComplex() {
	Result res = namedresult.getByName("cco").getByName("co").getByName(
		"c");
	assertTrue("size > 0", res.size() > 0);
	for (Result ires : res.toArray()) {
	    assertTrue("result must be complex", ires.isComplex());
	}
	for (Result cres : unnamedresult.toArray()) {
	    assertTrue("result must be complex", cres.isComplex());
	}

	for (Result cres : oneresult.toArray()) {
	    assertTrue("result must be complex", cres.isComplex());
	}
    }

    /**
     * Test method for {@link odra.jobc.Result#getByName(java.lang.String)}.
     */
    @Test
    public final void testGetByName() {
	Result res = namedresult.getByName("cco");
	assertTrue(res.isNamed());
	res = res.getByName("co");
	assertFalse("result must not be named", res.isNamed());

    }
    /**
     * Test method for {@link odra.jobc.Result#fields()}.
     */
    @Test
    public final void testFields() {
	Result res = namedresult.getByName("cco").getByName("co").getByName(
	"c");
	for(Result cres : res.toArray()){
	    assertFalse("unnamed struct", cres.isNamed());
	    assertTrue(cres.isComplex());
	    for(Result elem : cres.fields().toArray()){
		assertTrue(elem.isNamed());
		String name = elem.getName();
		if(name.equals("power")){
		    try {
			elem.getInteger();
		    } catch (JOBCException e) {
			fail("power is an integer value");
		    }
		}else if(name.equals("model")){
		    try {
			elem.getString();
		    } catch (JOBCException e) {
			fail("power is an integer value");
		    }
		}
	    }
	}
	

    }
}
