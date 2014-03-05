package odra.ws.type.mappers;


import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSFunction;

/** Wrapps bare intetface for xsd infoset tree visiting function
 * 
 * @version 2007-06-24
 * @since 2006-06-17
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class AbstractXSFunction<T> implements XSFunction<T>{

	public T annotation(XSAnnotation ann) {
		throw new NotImplementedException();
	}

	public T attGroupDecl(XSAttGroupDecl decl) {
		throw new NotImplementedException();
	}

	public T attributeDecl(XSAttributeDecl decl) {
		throw new NotImplementedException();
	}

	public T attributeUse(XSAttributeUse use) {
		throw new NotImplementedException();
	}

	public T complexType(XSComplexType type) {
		throw new NotImplementedException();
	}

	public T facet(XSFacet facet) {
		throw new NotImplementedException();
	}

	public T identityConstraint(XSIdentityConstraint decl) {
		throw new NotImplementedException();
	}

	public T notation(XSNotation notation) {
		throw new NotImplementedException();
	}

	public T schema(XSSchema schema) {
		throw new NotImplementedException();
	}

	public T xpath(XSXPath xpath) {
		throw new NotImplementedException();
	}

	public T empty(XSContentType empty) {
		throw new NotImplementedException();
	}

	public T particle(XSParticle particle) {
		throw new NotImplementedException();
	}

	public T simpleType(XSSimpleType simpleType) {
		throw new NotImplementedException();
	}

	public T elementDecl(XSElementDecl decl) {
		throw new NotImplementedException();
	}

	public T modelGroup(XSModelGroup group) {
		throw new NotImplementedException();
	}

	public T modelGroupDecl(XSModelGroupDecl decl) {
		throw new NotImplementedException();
	}

	public T wildcard(XSWildcard wc) {
		throw new NotImplementedException();
	}

}
