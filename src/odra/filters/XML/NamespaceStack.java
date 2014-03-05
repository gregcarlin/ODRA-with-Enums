package odra.filters.XML;

import java.util.Stack;

import odra.db.OID;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;

@SuppressWarnings("serial")
public class NamespaceStack extends Stack<NamespaceStack.NamespaceDef> {

	public static class NamespaceDef{
		String uri;
		String prefix;
		OID oid;
		
		public NamespaceDef(String prefix, String uri, OID oid) {
			super();
			this.uri = uri;
			this.prefix = prefix;
			this.oid = oid;
		}
	
		public NamespaceDef(StructResult def)
		{
			prefix = ((StringResult)((BinderResult)((StructResult)def).fieldAt(0)).value).value;
			uri = ((StringResult)((BinderResult)((StructResult)def).fieldAt(1)).value).value;
			oid = null;
		}
	
	}
	
	public NamespaceStack() {
		super();
	}
	
	public OID getSourceOID(String prefix)
	{
		for( int i=this.elementCount-1; i>=0; i-- )
			if ( this.elementAt(i).prefix.equals(prefix) )
				return elementAt(i).oid;
		return null;
	}
	
	public String getSourceURI(String prefix)
	{
		for( int i=this.elementCount-1; i>=0; i-- )
			if ( this.elementAt(i).prefix.equals(prefix) )
				return elementAt(i).uri;
		return null;
	}
}
