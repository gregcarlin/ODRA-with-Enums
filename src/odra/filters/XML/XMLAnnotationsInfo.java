package odra.filters.XML;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;

public class XMLAnnotationsInfo {

	public boolean isCorrect = false;
	public boolean isAttribute = false;
	public String prefix = "";
	public String URI = "";
	public NamespaceStack namespaceDefs = new NamespaceStack();
	
	public XMLAnnotationsInfo( StructResult res ) throws DatabaseException
	{
		if (res==null)
			return;
		for(int i=0; i<res.fieldsCount(); i++)
		{
			isCorrect = true;
			BinderResult ann = (BinderResult)res.fieldAt(i);
			if (ann.getName().equals(XMLImportFilter.ATTRIBUTE))
			{	if (((BooleanResult)ann.value).value == true )
					isAttribute = true;
			}
			else if (ann.getName().equals(XMLImportFilter.NAMESPACE_DEF))
			{
				if (ann.value instanceof BagResult)
				{	BagResult bag = (BagResult) ann.value;
					for ( Result def:bag.elementsToArray())
						namespaceDefs.add( new NamespaceStack.NamespaceDef( (StructResult)def ));
				}
				else
					namespaceDefs.add( new NamespaceStack.NamespaceDef( (StructResult)ann.value ));
			}
			else if (ann.getName().equals(XMLImportFilter.NAMESPACE_REF))
			{
				StructResult str = (StructResult)ann.value;
				this.prefix = ((StringResult)((BinderResult)str.fieldAt(0)).value).value;
				this.URI =  ((StringResult)((BinderResult)str.fieldAt(1)).value).value;
			}
		}
	}
}
