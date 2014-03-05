package odra.sbql.external;

import java.lang.reflect.*;
import java.util.Vector;

public class JavaObject {
	public Class cls;
	public Object val;
	private Vector pars = new Vector();
	
	public JavaObject(Class cls) {
		this.cls = cls;
		initParameters();
	}
	
	public void addParameter(Object val) {
		pars.addElement(val);
	}
	
	public void initParameters() {
		pars = new Vector();
	}
	
	public Object[] getParsObjects() {
		return pars.toArray();
	}
	
	public Class[] getParsClasses() {
		
		Object[] obj = getParsObjects();
		Class[] cls = new Class[obj.length];
		
		for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof Integer)
				cls[i] = Integer.TYPE;
			else if (obj[i] instanceof String)
				cls[i] = String.class;
			else if (obj[i] instanceof Boolean)
				cls[i] = Boolean.TYPE;
			else
				throw new RuntimeException("Unsupported result");
		}

		return cls;
	}
}
