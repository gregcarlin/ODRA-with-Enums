/**
 * 
 */
package odra.jobc;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.CollectionResult;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.RemoteReferenceResult;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;

/**
 * Result - wraps and flatten  ODRA native query result hierarchy
 * @see odra.sbql.results.runtime.Result
 * @author Radek Adamus
 * @since 2007-12-14  
 * @version 2007-12-14
 */
public class Result  implements Iterable{
    private odra.sbql.results.runtime.Result result;

    /**
     * @param result -
     *                ODRA native result
     * @see odra.sbql.results.runtime.Result
     * @throws JOBCException
     */
    Result(odra.sbql.results.runtime.Result result) {
	this.result = result;
    }

    /**
     * @return empty bag result
     */
    public static Result emptyResult(){
	return new Result(new BagResult());
    }
    
    /**
     * @param name - required name
     * @return named empty bag result
     */
    public static Result emptyResult(String name){
	return new Result(new BinderResult(name, new BagResult()));
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator iterator() {
	Result unnamed = this.skipNames();
	Vector results = new Vector();
	odra.sbql.results.runtime.Result[] resarr = unnamed.result.elementsToArray();
	for(int i = 0; i < resarr.length; i++)
	{
        	    results.add(new Result(resarr[i]));
        }
	
	return results.iterator();
    }
    
    /**
     * @return if the result is complex returns result representing the collection of complex result fields
     * otherwise empty result
     */
    public Result fields(){
	BagResult bagres = new BagResult();
	Result unnamed = this.skipNames();
	if(unnamed.isComplex()){
	    SingleResult[] sresarr = ((StructResult)unnamed.result).fieldsToArray();
	    for(int i = 0 ; i < sresarr.length; i++){
		bagres.addElement(sresarr[i]);
	    }
	}
	return new Result(bagres);
    }
    
    /**
     * Check if the result is empty
     * @return true if the result is an empty bag 
     */
    public boolean isEmpty(){
	Result unnamed = this.skipNames();
	return unnamed.result.elementsCount() == 0;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
	// TODO Auto-generated method stub
	return dump(0);
    }
    
    /**
     * Get the name of the result
     * @return the name of the result or String("") if unnamed
     */
    public String getName() {
	if (isNamed())
	    return ((BinderResult) this.result).getName();
	return "";
    }

    /**
     * @return the string result value
     * @throws JOBCException -
     *                 if the result type <> string
     */
    public String getString() throws JOBCException {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof StringResult)
	    return ((StringResult) unnamed.result).value;

	throw new JOBCException("Cannot cast " + result + " to string.",
		JOBCException.RESULT_TYPE_ERROR);
    }
    
    /**
     * @return the integer result value
     * @throws JOBCException -
     *                 if the result type <> integer
     */
    public int getInteger() throws JOBCException {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof IntegerResult)
	    return ((IntegerResult) unnamed.result).value;

	throw new JOBCException("Cannot cast " + result + " to integer.",
		JOBCException.RESULT_TYPE_ERROR);
    }

    /**
     * @return the real result value
     * @throws JOBCException -
     *                 if the result type <> real
     */
    public double getReal() throws JOBCException {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof DoubleResult)
	    return ((DoubleResult) unnamed.result).value;
	throw new JOBCException("Cannot cast " + result + " to real.",
		JOBCException.RESULT_TYPE_ERROR);
    }

    // private Result flattenOneElemBag(){
    // if(this.isBag() && size() == 1){
    // return firstElement();
    // }else return this;
    // }
    /**
     * @return the boolean result value
     * @throws JOBCException -
     *                 if the result type <> boolean
     */
    public boolean getBoolean() throws JOBCException {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof BooleanResult)
	    return ((BooleanResult) unnamed.result).value;

	throw new JOBCException("Cannot cast " + result + " to boolean.",
		JOBCException.RESULT_TYPE_ERROR);
    }

    /**
     * @return the date result value
     * @throws JOBCException -
     *                 if the result type <> date
     */
    public Date getDate() throws JOBCException {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof DateResult)
	    return ((DateResult) unnamed.result).value;

	throw new JOBCException("Cannot cast " + result + " to date.",
		JOBCException.RESULT_TYPE_ERROR);
    }

    /**
     * @return true if the value is of primitive type
     */
    public boolean isPrimitive() {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof CollectionResult
		|| unnamed.result instanceof StructResult)
	    return false;
	return true;
    }

    /**
     * @return true if the result is a bag (collection)
     */
    public boolean isBag() {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof CollectionResult)
	    return true;

	return false;
    }

    /**
     * @return true if the result is of complex type
     */
    public boolean isComplex() {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof StructResult)
	    return true;

	return false;
    }

    /**
     * @return true if the result is an object reference
     */
    public boolean isObjectReference() {
	Result unnamed = this.skipNames();
	if (unnamed.result instanceof RemoteReferenceResult)
	    return true;

	return false;
    }

    /**
     * @return true if the result is named
     */
    public boolean isNamed() {
	return this.result instanceof BinderResult;
    }

    /**
     * Search the sub-result by name
     * @param name
     *                the name of searched results
     * @return Result named with a given name the search is performed to the
     *         first encountered named results
     */
    public Result getByName(String name) {
	odra.sbql.results.runtime.Result result = this.getByName(this.result, name);
	return new Result(result.elementsCount() == 1 ? result.elementAt(0)
		: result);

    }

    /**
     * @return the number of results
     */
    public int size() {
	Result unnamed = this.skipNames();
	return unnamed.result.elementsCount();
    }

    /**
     * @param index
     * @return the result at the specified position
     * @throws IndexOutOfBoundsException
     */
    public Result get(int index) {
	Result unnamed = this.skipNames();
	return new Result(unnamed.result.elementAt(index));
    }

    /**
     * @return first result from the results (equivalent to get(0))
     */
    public Result firstElement() {
	return get(0);
    }

    private odra.sbql.results.runtime.Result getByName(odra.sbql.results.runtime.Result param, String name) {
	BagResult result = new BagResult();
	if (param instanceof BinderResult) {
	    result.addAll(this.getByName((BinderResult) param, name));

	} else if (param instanceof CollectionResult) {
	    BagResult bres = (BagResult) param;
	    SingleResult[] sinresarr = bres.elementsToArray();
	    for (int i = 0; i < sinresarr.length; i++) {
		result.addAll(getByName(sinresarr[i], name));
	    }

	} else if (param instanceof StructResult) {
	    StructResult sres = (StructResult) param;
	    SingleResult[] sinresarr = sres.fieldsToArray();
	    for (int i = 0; i < sinresarr.length; i++) {
		result.addAll(getByName(sinresarr[i], name));
	    }
	}

	return result;
    }

    private odra.sbql.results.runtime.Result getByName(BinderResult bres, String name) {
	BagResult result = new BagResult();
	if (bres.getName().equals(name)) {
	    if (bres.value instanceof SingleResult)
		result.addElement((SingleResult) bres.value);
	    else
		result.addAll(bres.value);
	}
	return result;
    }

    protected String dump(int level) {
	StringBuffer descr = new StringBuffer();
	String NEW_LINE = System.getProperty("line.separator");
	String indentunit = "   ";
	String indent = "";
	odra.sbql.results.runtime.Result current = this.result;
	for (int i = 1; i < level + 1; i++) {
	    indent += indentunit;
	}
	descr.append(indent + this.getClass().getName() + ": ");
	if (isNamed()) {
	    descr.append("(name: ");
	    do {
		descr.append(((BinderResult) current).getName());
		current = ((BinderResult) current).value;
		if (current instanceof BinderResult) {
		    descr.append(".");
		} else
		    break;
	    } while (true);
	    descr.append(") ");
	} else {
	    descr.append("(unnamed) ");
	    descr.append(current.getClass().getName());
	}

	if (current instanceof StructResult) {
	    descr.append("{");
	    descr.append(NEW_LINE);
	    SingleResult[] resarr  =((StructResult) current).fieldsToArray();
	    for (int i = 0; i < resarr.length; i++)
		descr.append(new Result(resarr[i]).dump(level + 1));
	    descr.append(indent + "}");
	} else if (current instanceof SingleResult) {
	    descr.append("(");
	    Result simple = new Result(current);
	    try {
		if (current instanceof StringResult) {
		    descr.append(simple.getString());
		} else if (current instanceof IntegerResult) {
		    descr.append(simple.getInteger());
		} else if (current instanceof DoubleResult) {
		    descr.append(simple.getReal());
		} else if (current instanceof DateResult) {
		    descr.append(simple.getDate());
		} else if (current instanceof RemoteReferenceResult) {
		    descr.append("object reference");
		}
	    } catch (JOBCException e) {
		// ignore
	    }
	    descr.append(")");
	} else {
	    SingleResult[] resarr = current.elementsToArray();
	    for (int i = 0; i < resarr.length; i++)
		descr.append(new Result(resarr[i]).dump(level + 1));
	}

	descr.append(NEW_LINE);

	return descr.toString();
    }

   
    private Result skipNames(){
	Result result = this;
	while(result.isNamed()){
	    result = new Result(((BinderResult)result.result).value);
	}
	return result;
    }
    
    /**
     * @param encoding - output encoding
     * @return result in the XML format
     */
    public String getAsXML(String encoding){
	JOBCXMLResultPrinter printer = new JOBCXMLResultPrinter();
	printer.setOutputEncoding(encoding);
	return printer.print(this.result);
    }
    
    /**
     * @return result in the XML format
     */
    public String getAsXML(){
	return this.getAsXML(Charset.forName(DEFAULT_ENCODING).name());
    }
    public Result[] toArray() {
	Result unnamed = this.skipNames();
	
	odra.sbql.results.runtime.Result[] resarr = unnamed.result.elementsToArray();
	Result[] jobcresarr = new Result[resarr.length]; 
	for(int i = 0; i < resarr.length; i++)
	{
        	    jobcresarr[i] = new Result(resarr[i]);
        }
	return jobcresarr;
    }
    
    private static String DEFAULT_ENCODING = "utf-8";
}
