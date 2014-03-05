package odra.sbql.optimizers;

import java.util.Hashtable;
import java.util.Vector;

import odra.system.config.ConfigDebug;

/**
 * Optimizer sequence.
 * 
 * @author jacenty
 * @version 2007-11-25
 * @since 2007-02-12
 */
public class OptimizationSequence extends Vector<Type>
{
	/** a constant name */
	public static final String OPTIMIZATION = "optimization";
	/** a constant name */
	public static final String REFOPTIMIZATION = "refoptimization";
	
	/** predefined sequences */
	public static Hashtable<String, OptimizationSequence> PREDEFINED = new Hashtable<String, OptimizationSequence>();
	/** standard optimization sequence name */
	public static final String STANDARD = "standard";
	/** wrapper optimization sequence name */
	public static final String WRAPPER = "wrapper";
	/** index optimization sequence name */
	public static final String INDEX = "index";
	/** index construction optimization sequence name */
	public static final String INDEXCONSTRUCTOR = "indexconstructor";
	/** view optimization sequence name */
	public static final String VIEW = "view";
	
	/** empty optimization sequence name */
	public static final String NONE = "none";
	
	/** sequence name, can be <code>null</code> */
	private final String name;
	
	static
	{
		OptimizationSequence minimal = new OptimizationSequence(
			STANDARD,
			new Type[] {});
		OptimizationSequence standardSequence = new OptimizationSequence(
			WRAPPER,
			new Type[] {Type.WRAPPER_REWRITE});
		OptimizationSequence indexSequence = new OptimizationSequence(
			INDEX,
			new Type[] {Type.INDEX});
		
		OptimizationSequence indexConstructorSequence = new OptimizationSequence(
				INDEXCONSTRUCTOR,				
			new Type[] {Type.UNSTRICTVIEWREWRITE, Type.AUXNAMES, Type.WRAPPER_OPTIMIZE});
		
		OptimizationSequence viewSequence = new OptimizationSequence(
			VIEW,
			new Type[] {Type.VIEWREWRITE, Type.AUXNAMES});
		
		OptimizationSequence noneSequence = new OptimizationSequence(
			NONE,
			new Type[] {});
		
		PREDEFINED.put(minimal.name, minimal);
		PREDEFINED.put(standardSequence.name, standardSequence);
		PREDEFINED.put(indexSequence.name, indexSequence);
		PREDEFINED.put(indexConstructorSequence.name, indexConstructorSequence);
		PREDEFINED.put(viewSequence.name, viewSequence);
		PREDEFINED.put(noneSequence.name, noneSequence);
	}
	
	/**
	 * The constructor.
	 *
	 */
	public OptimizationSequence()
	{
		super();
		
		this.name = null;
	}
	
	/**
	 * The constructor for predefined sequences.
	 * 
	 * @param name sequence name
	 * @param types types in the sequence
	 */
	public OptimizationSequence(String name, Type[] types)
	{
		super();
		
		this.name = name;
		
		for(Type type : types)
			try
			{
				addType(type);
			}
			catch (OptimizationException exc)
			{
				if(ConfigDebug.DEBUG_EXCEPTIONS)
					exc.printStackTrace();
				
				throw new AssertionError(exc.getMessage());
			}
	}
	
	@Override
	public String toString()
	{
		String sequence = "";
		
		if(isEmpty())
			return Type.NONE.getTypeName();
		
		for(Type type : this)
			sequence += type.getTypeName() + " | ";
		
		if(sequence.length() > 0)
			sequence = sequence.substring(0, sequence.length() - 3);
		
		return sequence;
	}

	/**
	 * Do not use this method.
	 */
	@Override
	@Deprecated
	public synchronized boolean add(@SuppressWarnings("unused") Type type)
	{
		throw new RuntimeException("add(Type) is not safe, use addType(type) instead");
	}
	
	/**
	 * Do not use this method.
	 */
	@Override
	@Deprecated
	public synchronized void addElement(@SuppressWarnings("unused") Type type)
	{
		throw new RuntimeException("addElement(Type) is not safe, use addType(type) instead");
	}
	
	/**
	 * Adds a type to the sequence, <code>NONE</code> resets.
	 * 
	 * @param type type
	 * @throws OptimizationException a conflict exists
	 */
	public synchronized void addType(Type type) throws OptimizationException 
	{
		checkConflict(type);
		
		if(type.equals(Type.NONE))
		{
			removeAllElements();
			return;
		}
		
		super.add(type);
	}
	
	/**
	 * A method for checking confilts between different optimizer types.
	 * <br />
	 * Conflicts can arrise if two types are used in a wrong order. 
	 * Some types cannot be used in the same optmization procedure at all.
	 * 
	 * @param checkedType checked type
	 * @throws OptimizationException a conflict detected
	 */
	private void checkConflict(Type checkedType) throws OptimizationException
	{
		if(
				checkedType.equals(Type.WRAPPER_OPTIMIZE) && this.contains(Type.WRAPPER_REWRITE) ||
				checkedType.equals(Type.WRAPPER_REWRITE) && this.contains(Type.WRAPPER_OPTIMIZE))
			throw new OptimizationException("'" + Type.WRAPPER_OPTIMIZE.getTypeName() + "' and '" + Type.WRAPPER_REWRITE.getTypeName() + "' cannot be combined in the same optimization sequence.");
	}
	
	/**
	 * Returns the predefined named sequence.
	 * 
	 * @param name sequence name
	 * @return predefined named sequence
	 */
	public static OptimizationSequence getForName(String name)
	{
		return PREDEFINED.get(name);
	}
}
