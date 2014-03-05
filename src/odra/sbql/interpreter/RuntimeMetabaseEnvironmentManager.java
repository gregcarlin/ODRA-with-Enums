/**
 * 
 */
package odra.sbql.interpreter;

import java.util.Stack;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.stack.CounterData;
import odra.sbql.stack.SBQLStack;
import odra.sbql.stack.StackFrame;

/**
 * RuntimeMetabaseEnvironmentManager
 * @author Radek Adamus
 *@since 2007-10-01
 *last modified: 2007-10-01
 *@version 1.0
 */
public class RuntimeMetabaseEnvironmentManager extends
	RuntimeEnvironmentManager {

    
    /**
     * @param sbqlStack
     * @throws DatabaseException
     */
    public RuntimeMetabaseEnvironmentManager(SBQLStack sbqlStack)
	    throws DatabaseException {
	super();
	this.sbqlStack = sbqlStack;	
	this.sbqlStack.initialize();
	this.nestingManager = new RuntimeMetabaseNestingManager();

    }

    /* (non-Javadoc)
     * @see odra.sbql.interpreter.RuntimeEnvironmentManager#initializeModuleEnvironment(odra.db.objects.data.DBModule, boolean)
     */
    @Override
    void initializeModuleEnvironment(DBModule mod, boolean base)
	    throws DatabaseException
    {
	
	StackFrame frame = this.nestingManager.nestedRootEnvironment(mod.getMetabaseEntry());
	if(base){
		this.sbqlStack.enterAllBaseFrame(frame);
	}
	else
	    this.sbqlStack.enterAll(frame);
    }

    /* (non-Javadoc)
     * @see odra.sbql.interpreter.RuntimeEnvironmentManager#destroyEnvironment()
     */
    @Override
    void destroyEnvironment() throws DatabaseException
    {	
	this.sbqlStack.destroyEnvironment();
    }

    /* (non-Javadoc)
     * @see odra.sbql.interpreter.RuntimeEnvironmentManager#bind(int, odra.db.objects.data.DBModule)
     */
    @Override
    AbstractQueryResult[] bind(int nameid, DBModule cmod)
	    throws DatabaseException
    {
	AbstractQueryResult[] resarr = this.sbqlStack.bind(nameid);
	return resarr;
    }
    
    void createCounterEnvironment(int limit) {
	    this.sbqlStack.createEnvironment();
	    this.sbqlStack.setCounterData(new CounterData(limit));
	}
    
    void createNestedEnvironment(SingleResult res) throws DatabaseException{
	assert res != null : "res == null";
	
	//perform nested
	Stack<StackFrame> stack = this.nestingManager.nested(res);

	//push nested result on the ENVS
	this.sbqlStack.createNestedEnvironment(stack);
	
    }
    void destroyNestedEnvironment() throws DatabaseException{
	 
	    this.destroyEnvironment();
    }
}
