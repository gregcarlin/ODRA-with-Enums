package odra.ws.type.mappers.literal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.namespace.QName;

import odra.ws.type.constructors.ForwardingTypeConstructor;
import odra.ws.type.constructors.ModuleTypeConstructor;
import odra.ws.type.constructors.NamedTypeConstructor;
import odra.ws.type.constructors.PrimitiveTypeConstructor;
import odra.ws.type.constructors.RecordTypeConstructor;
import odra.ws.type.constructors.TypeConstructor;
import odra.ws.type.constructors.TypeConstructorFactory;
import odra.ws.type.mappers.AbstractXSFunction;
import odra.ws.type.mappers.ITypeMapper;
import odra.ws.type.mappers.NotImplementedException;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;

/** Literal XML services visiting function implementation 
 * 
 * @since 2007-06-17
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 * 
 */
public class LiteralXSFunction extends AbstractXSFunction<TypeConstructor> {

	
	public HashMap<String, TypeConstructor> generatedTypes = new HashMap<String, TypeConstructor>();

	public Queue<String> typesLeft = new LinkedList<String>();
	public Queue<String> elementsLeft = new LinkedList<String>();
	public Queue<String> typesMissed = new LinkedList<String>();
	public Queue<String> elementsMissed = new LinkedList<String>();
	public ITypeMapper typeMapper = new LiteralTypeMapper();
	
	/** Adds root type
	 * @param name
	 */
	public void addType(QName name) {		

		if (!this.typesLeft.contains(name.getLocalPart())){
			if (this.elementsLeft.contains(name.getLocalPart())) {
				throw new RuntimeException("Name already used for an element. Cannot proceed. ");
			}
			this.typesLeft.add(name.getLocalPart());
		}
	}
	
	/** Adds root element
	 * @param name
	 */
	public void addElement(QName name) {
		// FIXME QNames handling
		if (!this.elementsLeft.contains(name.getLocalPart())){
			if (this.typesLeft.contains(name.getLocalPart())) {
				throw new RuntimeException("Name already used for a type. Cannot proceed. ");
			}
			this.elementsLeft.add(name.getLocalPart());
		}
		
	}
	
	/** Indicates, whether all required root types are done 
	 * @return
	 */
	public boolean isComplete() {
		return this.typesLeft.size() == 0 && this.elementsLeft.size() == 0;
	}

	@Override
	public TypeConstructor schema(XSSchema schema) {

		ModuleTypeConstructor moduleDef = TypeConstructorFactory.createModuleTypeConstructor(this.typeMapper);
		moduleDef.setName(schema.getTargetNamespace());
		
		// initialize with root elements and types
		while (!this.elementsLeft.isEmpty()) {
			String name = this.elementsLeft.poll();
			// process
			XSElementDecl el = schema.getElementDecl(name);
			if (el != null) {
				TypeConstructor type = el.apply(this);
				moduleDef.addType(type);
			} else {
				this.elementsMissed.add(name);
			}
			
		}
		
		while (!this.typesLeft.isEmpty()) {
			String name = this.typesLeft.poll();
			
			ForwardingTypeConstructor forward = TypeConstructorFactory.createForwardingTypeConstructor(this.typeMapper);
			this.generatedTypes.put(name, forward);
			
			// process
			XSType t = schema.getType(name);
			if (t != null) {
				TypeConstructor type = t.apply(this);
				// needed because of all dependent objects
				forward.setTarget(type);			
				
				moduleDef.addType(type);
			} else {
				this.typesMissed.add(name);
			}
		}
		
		// invariant elementLeft.size == 0 && typesLeft.size == 0
		this.elementsLeft = this.elementsMissed;
		this.typesLeft = this.typesMissed;
		this.elementsMissed = new LinkedList<String>();
		this.typesMissed = new LinkedList<String>();
		
		return moduleDef;
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.AbstractXSFunction#simpleType(com.sun.xml.xsom.XSSimpleType)
	 */
	@Override
	public TypeConstructor simpleType(XSSimpleType simpleType) {
		if (simpleType.isPrimitive() || simpleType.isRestriction()) {
			PrimitiveTypeConstructor def = TypeConstructorFactory.createPrimitiveTypeConstructor(this.typeMapper);
			String primitiveTypeName = simpleType.getName();
			
			def.setName(this.typeMapper.mapXML(primitiveTypeName));
			
			return def;
		}
		
		throw new NotImplementedException("Unknown xsd simple type kind. ");
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.AbstractXSFunction#complexType(com.sun.xml.xsom.XSComplexType)
	 */
	@Override
	public TypeConstructor complexType(XSComplexType type) {
		
		if (type.isGlobal()) {
			
			if (this.generatedTypes.containsKey(type.getName())) {
				return this.generatedTypes.get(type.getName());
				
			} else {
				NamedTypeConstructor def = TypeConstructorFactory.createNamedTypeConstructor(this.typeMapper);
				def.setName(this.typeMapper.mapTypeDefName(type.getName()));
				TypeConstructor target = type.getContentType().apply(this);
				
				def.setTarget(target);
				return def;
			}
		} else {
			return type.getContentType().apply(this);
		}
	}


	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.AbstractXSFunction#particle(com.sun.xml.xsom.XSParticle)
	 */
	@Override
	public TypeConstructor particle(XSParticle particle) {
		return particle.getTerm().asModelGroup().apply(this);
		
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.AbstractXSFunction#empty(com.sun.xml.xsom.XSContentType)
	 */
	@Override
	public TypeConstructor empty(XSContentType empty) {
		return new RecordTypeConstructor();
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.AbstractXSFunction#modelGroup(com.sun.xml.xsom.XSModelGroup)
	 */
	@Override
	public TypeConstructor modelGroup(XSModelGroup group) {
		
		switch (group.getCompositor()) {
			case SEQUENCE:
			case ALL:
				RecordTypeConstructor def = new RecordTypeConstructor();
				
				for (XSParticle p : group.getChildren()) {
					String pName = p.getTerm().asElementDecl().getName();
					TypeConstructor pType = p.getTerm().asElementDecl().getType().apply(this);
				
					int pMinCard = p.getMinOccurs();
					int pMaxCard = p.getMaxOccurs();
					def.addParameter(pName, pMinCard, pMaxCard, pType);
					
				}
				return def;
			case CHOICE:
				throw new RuntimeException("Choice compositor support not implemented yet.");
			default:
				throw new RuntimeException("Unknown compositor type.");
		}
		
	}
	
	/* (non-Javadoc)
	 * @see odra.ws.type.mappers.AbstractXSFunction#elementDecl(com.sun.xml.xsom.XSElementDecl)
	 */
	@Override
	public TypeConstructor elementDecl(XSElementDecl decl) {
		NamedTypeConstructor def = TypeConstructorFactory.createNamedTypeConstructor(this.typeMapper);
		def.setName(this.typeMapper.mapTypeDefName(decl.getName()));
		TypeConstructor targetType = decl.getType().apply(this);
		def.setTarget(targetType);
		
		return def;
		
	}




}
