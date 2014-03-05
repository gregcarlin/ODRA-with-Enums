package tests.filters.XML;

import java.net.URISyntaxException;

import odra.db.OID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NamespaceStack {

	odra.filters.XML.NamespaceStack nsStack;
	@Before
	public void setUp() throws Exception {
		nsStack = new odra.filters.XML.NamespaceStack();
		OID oid = null; //not testing with real database objects 
		nsStack.push( new odra.filters.XML.NamespaceStack.NamespaceDef( "bottom", "dno", oid ));
		nsStack.push( new odra.filters.XML.NamespaceStack.NamespaceDef( "first", "pierwsza", oid ));
		nsStack.push( new odra.filters.XML.NamespaceStack.NamespaceDef( "", "pusta", oid ));
		nsStack.push( new odra.filters.XML.NamespaceStack.NamespaceDef( "first", "first.druga", oid ));
		nsStack.push( new odra.filters.XML.NamespaceStack.NamespaceDef( "second", "druga", oid ));
		nsStack.push( new odra.filters.XML.NamespaceStack.NamespaceDef( "first", "first.trzecia", oid ));
	}

	@Test
	public void testGetSourceURI() throws URISyntaxException {
		OID oid = null; //not testing with real database objects 
		
		Assert.assertNull(nsStack.getSourceURI("unknown"));
		Assert.assertNull(nsStack.getSourceOID("unknown"));

		Assert.assertEquals(nsStack.getSourceURI("bottom").toString(), "dno");
		Assert.assertEquals(nsStack.getSourceURI("").toString(), "pusta");
		Assert.assertEquals(nsStack.getSourceURI("first").toString(), "first.trzecia");
		nsStack.pop();
		Assert.assertEquals(nsStack.getSourceURI("bottom").toString(), "dno");
		Assert.assertEquals(nsStack.getSourceURI("first").toString(), "first.druga");
		Assert.assertEquals(nsStack.getSourceURI("second").toString(), "druga");
		nsStack.pop();
		Assert.assertEquals(nsStack.getSourceURI("bottom").toString(), "dno");
		Assert.assertEquals(nsStack.getSourceURI("first").toString(), "first.druga");
		nsStack.pop();
		Assert.assertEquals(nsStack.getSourceURI("bottom").toString(), "dno");
		Assert.assertEquals(nsStack.getSourceURI("first").toString(), "pierwsza");
		nsStack.pop();
		Assert.assertEquals(nsStack.getSourceURI("bottom").toString(), "dno");
		nsStack.push( new odra.filters.XML.NamespaceStack.NamespaceDef( "bottom", "dno.druga", oid ));
		Assert.assertEquals(nsStack.getSourceURI("bottom").toString(), "dno.druga");
		
		Assert.assertNull(nsStack.getSourceURI("unknown"));
		Assert.assertNull(nsStack.getSourceOID("unknown"));
	}
}
