package odra.filters.XML;

import java.io.IOException;
import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;

public class XMLTransformer {

	private Document stylesheet;
	XSLTransform transformation;

	public XMLTransformer(InputStream stylesheetInput) throws ValidityException, ParsingException, IOException, XSLException
	{
		Builder builder = new Builder();
		stylesheet = builder.build(stylesheetInput);
		transformation = new XSLTransform(stylesheet);
	}
	
	public Nodes transform( Document document ) throws XSLException
	{
		return transformation.transform(document);
	}

	public Nodes transform( Nodes nodes ) throws XSLException
	{
		return transformation.transform(nodes);
	}
}
