package tests.ws;

import static org.junit.Assert.fail;

import javax.xml.parsers.DocumentBuilderFactory;

import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.literal.LiteralTypeMapper;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * Tests type mappers
 * 
 * @since 2007-04-24
 * @version 2007-06-23
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */

public class TypeMapperTest {
	/**
	 * Simple test of {@link LiteralTypeMapper}
	 */
	@Test 
	public void Simple() {
		ITypeMapper typeMapper = new LiteralTypeMapper();
		
		StringResult name = new StringResult("Marcin");
		StringResult surname = new StringResult("Daczkowski");
		
		BinderResult b1 = new BinderResult("name", name);
		BinderResult b2 = new BinderResult("surname", surname);
		
		StructResult s = new StructResult();
		s.addField(b1);
		s.addField(b2);
		
		BinderResult b3 = new BinderResult("student", s);
		
		try {
			Document context = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			NodeList nodelist = typeMapper.mapOdraResultToXML(context, b3, null, "http://ns.org/");
			
			while (nodelist.getLength() != 0) {
				context.appendChild(nodelist.item(0));
			}
			
			
		}
		catch (Exception ex) {
			fail(ex.getMessage());
		}
		
	
	}
}
