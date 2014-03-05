package odra.sbql.external;

import java.awt.*;
import java.util.Hashtable;
import javax.swing.*;

import java.lang.reflect.*;

import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.runtime.*;

//TW and Pietia (wslib)
public class ExternalRoutines {
	
	//hashtable storing procedure names
	private Hashtable<String, Integer> namesTable = new Hashtable<String, Integer>();
	//internal store for Java objects
	private Hashtable<Integer, JavaObject> objTable = new Hashtable<Integer, JavaObject>();
	private int cntr = 100;
	
	//Pietia - internal store for WS objects
	private static Hashtable<Integer, WSLib> wsTable = new Hashtable<Integer, WSLib>();
	private static Hashtable<Integer, Signature> sigs = new  Hashtable<Integer, Signature>();
	private static int wscntr = 100;
	
	
	public final static int
	LOAD_CLASS = 1,
	NEW_OBJECT = 2,
	INIT_PARAMETERS = 3,
	ADD_PARAMETERS = 4,
	INVOKE_VOID = 5,
	INVOKE_LIBRARY = 6,
	ADD_CALLBACK_PARAMETER = 7,
	INVOKE_STRING = 8,
	INVOKE_BOOLEAN = 9,
	INVOKE_REAL = 10,
	INVOKE_INTEGER = 11,
	//Pietia
	INVOKE_WEBSERVICE = 12,
	RESET_WEBSERVICE = 13,
	FIELD_WEBSERVICE = 14,
	ATTR_WEBSERVICE = 15,
	TXT_WEBSERVICE = 16,
	NEW_WEBSERVICE = 17,
	DELETE_WEBSERVICE = 18;
	
	public ExternalRoutines()
	{
		namesTable.put("load_class", new Integer(LOAD_CLASS));
		namesTable.put("new_object", new Integer(NEW_OBJECT));
		namesTable.put("init_parameters", new Integer(INIT_PARAMETERS));
		namesTable.put("add_parameters", new Integer(ADD_PARAMETERS));
		namesTable.put("add_callback_parameter", new Integer(ADD_CALLBACK_PARAMETER));
		namesTable.put("invoke_void", new Integer(INVOKE_VOID));
		namesTable.put("invoke_string", new Integer(INVOKE_STRING));
		namesTable.put("invoke_integer", new Integer(INVOKE_INTEGER));
		namesTable.put("invoke_boolean", new Integer(INVOKE_BOOLEAN));
		namesTable.put("invoke_library", new Integer(INVOKE_LIBRARY));
		//Pietia
		namesTable.put("invoke_webservice", new Integer(INVOKE_WEBSERVICE));
		namesTable.put("addfield_webservice", new Integer(FIELD_WEBSERVICE));
		namesTable.put("addattr_webservice", new Integer(ATTR_WEBSERVICE));
		namesTable.put("addtxt_webservice", new Integer(TXT_WEBSERVICE));
		namesTable.put("new_webservice", new Integer(NEW_WEBSERVICE));
		namesTable.put("delete_webservice", new Integer(DELETE_WEBSERVICE));
		namesTable.put("reset_webservice", new Integer(RESET_WEBSERVICE));
	}
	
	public static Signature getSignature(int no)
	{
		return sigs.get(new Integer(no));
	}
	
	public Result invoke(String name, Result res) throws Exception
	{
		if (!exists(name))
			throw new Exception("Unknown external procedure '" + name + "'"); //TODO fix Exception
		
		Integer Id = (Integer) namesTable.get(name);
		int id = Id.intValue();
		
		switch (id) {
		
			case ADD_PARAMETERS:
				return addParameters(res);

			case INIT_PARAMETERS:
				return initParameters(res);
		
			case LOAD_CLASS:
				return loadClass(res);
				
			case NEW_OBJECT:
				return newObject(res);
				
//			case INVOKE_VOID:
//				return invokeVoid(par1, par2);
//				
//			case INVOKE_LIBRARY:
//				return invokeLibrary(par1, par2);
//			
			case INVOKE_STRING:
				return invokeString(res);
				
			case INVOKE_INTEGER:
				return invokeInteger(res);
//				
//			case INVOKE_REAL:
//				return invokeReal(par1, par2);
//				
//			case INVOKE_BOOLEAN:
//				return invokeBoolean(par1, par2);
			//Pietia
			case INVOKE_WEBSERVICE:
				return invokeWebService(res);
			case FIELD_WEBSERVICE:
				return addfieldWebService(res);
			case ATTR_WEBSERVICE:
				return addattrWebService(res);
			case TXT_WEBSERVICE:
				return addtxtWebService(res);
			case NEW_WEBSERVICE:
				return newWebService(res);
			case DELETE_WEBSERVICE:
				return delWebService(res);
			case RESET_WEBSERVICE:
				return resetWebService(res);
			default:
				throw new Exception("Unknown external routine"); //TODO fix Exception
		}
	}
	
	

	private Result resetWebService(Result res) {
		int nlib = ((IntegerResult)res).value;
		WSLib lib = wsTable.get(new Integer(nlib));
		lib.resetParams();	
		return new IntegerResult(nlib);
	}

	private Result addfieldWebService(Result res) {
		StructResult struct = (StructResult) res;
		int nlib = ((IntegerResult)struct.fieldAt(0)).value;
		int fnum = ((IntegerResult)struct.fieldAt(1)).value;
		String fname = ((StringResult)struct.fieldAt(2)).value;
		WSLib lib = wsTable.get(new Integer(nlib));
		Result ret = lib.addField(fnum, fname);
		return ret;
	}

	private Result delWebService(Result res) {
		int nlib = ((IntegerResult)res).value;
		wsTable.remove(new Integer(nlib));
		return new IntegerResult(nlib);
	}

	private Result newWebService(Result res) {
		WSLib lib = new WSLib();
		wsTable.put(new Integer(wscntr), lib);
		Result ret = new IntegerResult(wscntr);
		StructResult struct = (StructResult) res;
		String namespace = ((StringResult)struct.fieldAt(0)).value;
		String endpoint = ((StringResult)struct.fieldAt(1)).value;
		String wsdlloc = ((StringResult)struct.fieldAt(2)).value;
		String operationName = ((StringResult)struct.fieldAt(3)).value;	
		Signature sig = lib.config(namespace,endpoint, wsdlloc,operationName);
		sigs.put(new Integer(wscntr), sig);
		System.out.println("signature: "+sig);
		wscntr++;
		return ret;
	}

	private Result addtxtWebService(Result res) {
		StructResult struct = (StructResult) res;
		int nlib = ((IntegerResult)struct.fieldAt(0)).value;
		int fnum = ((IntegerResult)struct.fieldAt(1)).value;
		String text = ((StringResult)struct.fieldAt(2)).value;
		WSLib lib = wsTable.get(new Integer(nlib));
		Result ret = lib.addText(fnum, text);
		return ret;
	}

	private Result addattrWebService(Result res) {
		StructResult struct = (StructResult) res;
		int nlib = ((IntegerResult)struct.fieldAt(0)).value;
		int fnum = ((IntegerResult)struct.fieldAt(1)).value;
		String attname = ((StringResult)struct.fieldAt(2)).value;
		String attvalue = ((StringResult)struct.fieldAt(3)).value;
		WSLib lib = wsTable.get(new Integer(nlib));
		Result ret = lib.addAttr(fnum, attname,attvalue);
		return ret;
	}





	//Pietia
	private Result invokeWebService(Result res) throws Exception  {
		int nlib = ((IntegerResult)res).value;
		WSLib lib = wsTable.get(new Integer(nlib));
		Result str = lib.callService();
		return str;
	}

	//checks if name is in Hashtable with ext_proc names
	private boolean exists(String name)
	{
		return namesTable.containsKey(name);
	}
	
	private Result addParameters(Object res) throws Exception 
	{
		StructResult struct = (StructResult) res;
		
		Integer objRef = new IntegerResult( ((ReferenceResult)struct.fieldAt(0)).value.derefInt() ).value;
		JavaObject jobj = objTable.get(objRef);
		
		for(int i=1; i<struct.fieldsCount(); i++)
		{

			if(struct.fieldAt(i) instanceof IntegerResult)
			{
				jobj.addParameter(((IntegerResult)struct.fieldAt(i)).value);
			}
			else if(struct.fieldAt(i) instanceof BooleanResult)
			{
				jobj.addParameter(((BooleanResult)struct.fieldAt(i)).value);
			}
			else if(struct.fieldAt(i) instanceof StringResult)
			{
				jobj.addParameter(((StringResult)struct.fieldAt(i)).value);
			}
		}
		
		return new IntegerResult(objRef);
	}
	
	
	private Result initParameters(Object res) throws Exception
	{
		IntegerResult jobjRef = new IntegerResult( ((ReferenceResult)res).value.derefInt() );
		
		objTable.get(jobjRef.value).initParameters();
		
		return jobjRef;
	}
	
	private Result invokeString(Object res) throws Exception {

		StructResult struct = (StructResult) res;
		
		Integer objRef = new IntegerResult( ((ReferenceResult)struct.fieldAt(0)).value.derefInt() ).value;
		String mname = ((StringResult)struct.fieldAt(1)).value;
		JavaObject jobj = objTable.get(objRef);
		String val;
		
		try {
			if (jobj.val instanceof Class)  {
				throw new Exception("static");
			}
			else {
				Method method = findMethod(mname, jobj.val, jobj.getParsClasses());
				Object resObj = method.invoke(jobj.val, jobj.getParsObjects());
				
				if (!(resObj instanceof String))
					throw new Exception("String expected");
				
				val = (String)resObj;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();

			throw new Exception("External procedure '" + mname + "' cannot be invoked. Reason: " + ex.getMessage());
		}
		
		return new StringResult(val);
	}
	
	private Result invokeInteger(Object res) throws Exception {

		StructResult struct = (StructResult) res;
		
		Integer objRef = new IntegerResult( ((ReferenceResult)struct.fieldAt(0)).value.derefInt() ).value;
		String mname = ((StringResult)struct.fieldAt(1)).value;
		JavaObject jobj = objTable.get(objRef);
		int val;
		
		try {
			if (jobj.val instanceof Class)  {
				throw new Exception("static");
			}
			else {
				Method method = findMethod(mname, jobj.val, jobj.getParsClasses());
				Object resObj = method.invoke(jobj.val, jobj.getParsObjects());
				
				if (!(resObj instanceof Integer))
					throw new Exception("Integer expected");
				
				val = ((Integer) resObj).intValue();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();

			throw new Exception("External procedure '" + mname + "' cannot be invoked. Reason: " + ex.getMessage());
		}
		
		return new IntegerResult(val);
	}
	
	private Result newObject(Object ref) throws Exception
	{	
		Integer jobjRef = new IntegerResult( ((ReferenceResult)ref).value.derefInt() ).value;
		
		JavaObject jobj = objTable.get(jobjRef);
	
		try {
			Constructor cstr = jobj.cls.getConstructor(jobj.getParsClasses());
			jobj.val = cstr.newInstance(jobj.getParsObjects());
			
			return new IntegerResult(jobjRef);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		
			throw new Exception("Class '" + jobj.cls.getName() + "' cannot be instantiated. Reason: " + ex.getMessage());
		}		
	}
	
	private Result loadClass(Object par1) throws Exception
	{	
		String clsname = ((StringResult)par1).value;
		
		if (!(clsname instanceof String))
			throw new Exception("String expected");
		
		try {
			JavaObject jobj = new JavaObject(Class.forName((String)clsname));
			objTable.put(new Integer(cntr), jobj);
			
			return new IntegerResult(cntr++);
		}
		catch (ClassNotFoundException ex) {
			throw new Exception("Class '" + (String)clsname + "' not found");
		}
	}
	
	
	private Method findMethod(String name, Object obj, Class[] params) throws NoSuchMethodException {
		Class cls = obj.getClass();
		
		Method[] m = cls.getMethods();
		
		methods:
		for (int i = 0; i < m.length; i++) {
			if (m[i].getName().equals(name)) {
				Class[] mpars = m[i].getParameterTypes();
				 
				if (mpars.length != params.length)
					continue;
									
				for (int j = 0; j < params.length; j++)
					if (!(mpars[j].isAssignableFrom(params[j])))
						continue methods;

				return m[i];
			}
		}

		throw new NoSuchMethodException(name);
	} 
}