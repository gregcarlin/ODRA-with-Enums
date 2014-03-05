/**
 * 
 */
package tests.jobc;

import java.io.IOException;

import odra.jobc.JOBC;
import odra.jobc.JOBCException;
import odra.jobc.Result;
import odra.jobc.SBQLQuery;
import odra.jobc.ddl.AddModuleCommand;

/**
 * JOBCTest
 * 
 * @author Radek Adamus
 * @since 2007-12-13 last modified: 2007-12-13
 * @version 1.0
 */
public class JOBCTest {
    
    /**
     * @param args
     */
    public static void main(String[] args) {
	JOBC db = new JOBC("admin", "admin", "localhost", 1521);
	
	String NEW_LINE = System.getProperty("line.separator");
	try {
	    db.connect();
	    db.executeDDLCommand(new AddModuleCommand("module test {a:integer; b:string[0..*];}"));
	    db.setCurrentModule("admin.test");
	} catch (JOBCException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	    System.exit(-1);
	}
	byte[] input = new byte[100];

	while (true) {
	    try {
		System.out.print("query>");
		int read = System.in.read(input);
		if(read < 0)
		    break;
		String query = new String(input, 0, read);
		if (("quit" + NEW_LINE).equals(query)) {
		    break;
		}
		if (NEW_LINE.equals(query)) {
		    continue;
		}
		Result result = db.execute(query);
		System.out.println(result.toString());

	    } catch (IOException e) {
		e.printStackTrace();
		break;
	    } catch (JOBCException e) {

		e.printStackTrace();
	    }
	}

	try {
	    db.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

}
