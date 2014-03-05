package odra.sbql.ast.declarations;

import java.util.Vector;

import odra.sbql.ast.terminals.Name;

public class CompoundName {
	public CompoundName N1;
	public Name N2;

	public CompoundName(Name n) {
		N2 = n;
	}
	
	public CompoundName(CompoundName n1, Name n2) {
		N1 = n1;
		N2 = n2;
	}
	
	public String nameAsString() {
		StringBuffer buf = new StringBuffer();
		
		for (String s : nameAsArray())
			buf.append(s).append(".");
		
		buf.deleteCharAt(buf.lastIndexOf("."));

		return buf.toString();
	}
	
	public String[] nameAsArray() {
		if (N1 == null)
			return new String[] { N2.value() };

		String[] n1arr = N1.nameAsArray();
		
		Vector<String> names = new Vector();
		for (String s : n1arr)
			names.addElement(s);
		
		names.addElement(N2.value());
		
		return (String[]) names.toArray(new String[names.size()]);
	}
}
