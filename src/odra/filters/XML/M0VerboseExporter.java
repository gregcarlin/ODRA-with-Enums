package odra.filters.XML;

import nu.xom.Attribute;
import nu.xom.Element;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.filters.ShadowObject;
import odra.filters.ShadowObject.Kind;

/**
 * This creator exports internal type information and tries to be a database dumper. However it omits annotations.
 * 
 * @author Krzysztof Kaczmarski
 * 
 */
public class M0VerboseExporter implements XMLNodeExporter {

   public Element createNode(OID oid, ShadowObject.Kind kind, Element parent) throws DatabaseException {
      Element elem = new Element(kind.name());
      elem.addAttribute(new Attribute("name", oid.getObjectName()));
      elem.addAttribute(new Attribute("oid", "" + oid));
      if (parent != null) parent.appendChild(elem);
      return elem;
   }

   public void createNodeValue(Element elem, Element parent, OID oid, ShadowObject.Kind kind) throws DatabaseException {
      elem.appendChild(kind.getValue(oid).toString());
   }

   public Element createRootElement() {
      return new Element("SBQL_Objects");
   }

   public void closeScope() {
   }

   public void openScope(OID oid) {
   }

   public void createReferenceValue(Element elem, Element parentElement, OID valueRef, Kind kind)
            throws DatabaseException {
      createNodeValue(elem, parentElement, valueRef, kind);
   }

   // public void processObjectId(Element elem, OID oid) {
   // }
}