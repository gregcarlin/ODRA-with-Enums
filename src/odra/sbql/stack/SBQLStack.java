package odra.sbql.stack;

import java.util.Stack;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.sbql.results.AbstractQueryResult;
import odra.system.config.ConfigDebug;

/**
 * The class represents functionality of SBA stacks. It can be used to model
 * runtime ENVS, runtime QRES, static ENVS and static QRES. ENVS consists of a
 * list of StackFrame instances. QRES consists of a list of QresElement
 * instances.
 * 
 * @author raist
 */
public class SBQLStack {
    private Vector<StackFrame> sections = new Vector(); // envs

    private Vector<AbstractQueryResult> results = new Vector(); // qres

    private IBindingGuru guru;

    
	// the binding guru knows how to perform binding in envs sections.
    // that knowledge is independent of whether the binding is performed
    // during compile time or during run time.
    public SBQLStack(IBindingGuru guru) {
	this.guru = guru;
    }

    /**
	 * @return the curent binding guru
	 */
	public IBindingGuru getGuru() {
		return guru;
	}

	/**
	 * @param guru the binding guru to set
	 */
	public void setGuru(IBindingGuru guru) {
		this.guru = guru;
	}

	
    // qres.push
    public void push(AbstractQueryResult res) {
	results.addElement(res);
    }

    // qres.pop
    public AbstractQueryResult pop() {
	if (ConfigDebug.ASSERTS)
	    assert results.size() > 0 : "qres has no elements";

	return results.remove(results.size() - 1);
    }

    // qres.peek
    public AbstractQueryResult peek() {
	if (ConfigDebug.ASSERTS)
	    assert results.size() > 0 : "qres has no elements";

	return results.lastElement();
    }

    // second result from top of qres
    public AbstractQueryResult peek2() {
	if (ConfigDebug.ASSERTS)
	    assert results.size() > 1 : "qres has <= 1 elements";

	return results.elementAt(results.size() - 2);
    }

    // qres.empty
    public boolean empty() {
	if (results.size() > 0)
	    return false;

	return true;
    }

    // returns index of null pushed on qres
    public int pushEmptyResultFrame() {
	results.addElement(null);
	return results.size() - 1;
    }

    // qres.replace @ index (if it is empty)
    public void replaceEmptyResultFrame(int index, AbstractQueryResult res) {
	assert results.elementAt(index) == null : "";

	results.setElementAt(res, index);
    }

    // create a new frame on envs with a dynamic link pointing at a particular
    // frame
    public void createEnvironment(StackFrame callee) {
	sections.addElement(new StackFrame(callee));

    }

    // create a new frame on envs with a dynamic link pointing at a base frame
    public void createScopedEnvironment() {
	sections.addElement(new StackFrame(baseFrame()));

    }

    /**
     * Enter the envs frames with the result of nested
     * 
     * @param newframes -
     *                stack of frames (new top frame should be at the bottom of
     *                newframes!!)
     */
    public void createNestedEnvironment(Stack<StackFrame> newframes) {
	CounterData counter = topFrame().counterData;
	while (!newframes.empty()) {
	    StackFrame frame = newframes.pop();
	    frame.counterData = counter;
	    frame.ebp = topFrame();
	    sections.add(frame);

	}
    }

    /**
     * Enter the envs frames with the result of nested
     * 
     * @param newframes -
     *                stack of frames (new top frame should be at the bottom of
     *                newframes!!)
     */
    public void createNestedScopedEnvironment(Stack<StackFrame> newframes) {
	StackFrame frame = newframes.pop();
	frame.ebp = baseFrame();
	sections.add(frame);
	while (!newframes.empty()) {
	    frame = newframes.pop();
	    frame.ebp = topFrame();
	    sections.add(frame);

	}
    }

    // create a new frame on envs with a dynamic link pointing at the previous
    // frame
    public void createEnvironment() {
	sections.addElement(new StackFrame(topFrame()));

    }

    // returns the current size of the ENVS
    public int environmentSize() {
	return sections.size();
    }

    // returns the current size of the QRES
    public int size() {
	return results.size();
    }

    public void initialize() {
	sections.addElement(new StackFrame());
    }

    // creates the first environment and initializes it
    public void initialize(Nester init) {
	sections.addElement(new StackFrame());
	topFrame().enter(init);
    }

    public void resetBaseFrame() {
	baseFrame().removeAll();
    }

    public void enterBaseFrame(Binder el) {
	baseFrame().enter(el);
    }

    public void enterBaseFrame(Nester el) {
	baseFrame().enter(el);
    }

    /**
     * insert all 'frame' content into base frame
     * 
     * @param frame
     */
    public void enterAllBaseFrame(StackFrame frame) {
	assert frame != null : "stack frame == null";
	baseFrame().enterAll(frame);
    }

    // removes the topmost envs frame
    public void destroyEnvironment() {
	if (ConfigDebug.ASSERTS)
	    assert sections.size() > 0 : "envs has no sections";

	sections.removeElementAt(sections.size() - 1);
    }

    // returns a reference to the topmost envs frame
    private final StackFrame topFrame() {
	if (ConfigDebug.ASSERTS)
	    assert sections.size() > 0 : "envs has no sections";

	return sections.lastElement();
    }

    private final StackFrame baseFrame() {
	return sections.firstElement();
    }

    // introduces a new binder in the topmost frame of the envs
    public void enter(Binder binder) {
	topFrame().enter(binder);
    }

    // introduces a new nester in the topmost frame of the envs
    public void enter(Nester nester) {
	topFrame().enter(nester);
    }

    public void enter(RemoteNester nester) {
	topFrame().enter(nester);
    }

    /**
     * insert all 'frame' content into top frame
     * 
     * @param frame
     */
    public void enterAll(StackFrame frame) {
	assert frame != null : "stack frame == null";
	topFrame().enterAll(frame);
    }

    // removes a binder from the topmost envs section
    public void remove(int name_id) {
	topFrame().remove(name_id);
    }

    // binds a name on the top of environment stack.
    // if the name cannot be found, returns empty table
    public AbstractQueryResult[] bindTop(int name_id) throws DatabaseException {
	return bind(name_id, DUMBINFO, 1);
    }

    // binds a name on the environment stack.
    // if the name cannot be found, returns empty table
    public AbstractQueryResult[] bind(int name_id) throws DatabaseException {
	return bind(name_id, DUMBINFO, Integer.MAX_VALUE);
    }

    // binds a name on the environment stack. the variable boundat indicates the
    // stack section at which the name was bound (used only in "out" mode,
    // i.e. to pass additional result from the method)
    public AbstractQueryResult[] bind(int name_id, BindingInfo boundat,
	    int sectionsNumber) throws DatabaseException {
	assert sections.size() > 0 : "envs has no sections";
	assert sectionsNumber > 0 : "sections number must be 0";
	assert boundat != null : "BindingInfo != null";

	AbstractQueryResult[] bound;

	StackFrame frame = topFrame();

	boundat.boundat = sections.size() - 1;

	while (frame != null) {
	    guru.setFrame(frame);
	    bound = guru.bind(name_id);

	    if (bound.length > 0) {
		return bound;
	    }
	    if (--sectionsNumber == 0)
		break;
	    frame = frame.ebp;
	    boundat.boundat--;
	}

	return new AbstractQueryResult[0];
    }

    public void setCounterData(CounterData cd) {
	topFrame().counterData = cd;
    }

    public CounterData getCounterData() {
	return topFrame().counterData;
    }

    public String dump() {
	StringBuffer buf = new StringBuffer();

	buf.append("------\n");
	for (StackFrame sec : sections) {
	    buf.append(sec.dump());
	    buf.append("\n------\n");
	}

	return buf.toString();
    }

    public byte[] getAsBytes(ISBQLStackSerializer serializer, int sectionNumber)
	    throws Exception {
	if (ConfigDebug.ASSERTS)
	    assert sectionNumber <= results.size() : "not enough sections on the QRES";
	if (sectionNumber == results.size())
	    return serializer.serialize(results);
	Vector<AbstractQueryResult> qres = new Vector<AbstractQueryResult>();
	for (int i = this.results.size() - sectionNumber; i < this.results
		.size(); i++)
	    qres.add(results.get(i));
	return serializer.serialize(qres);
    }

    public byte[] getEnvironmentAsBytes(ISBQLStackSerializer serializer,
	    int sectionNumber) throws Exception {
	if (ConfigDebug.ASSERTS)
	    assert false : "unimplemented";
	return null;
    }

    private final static BindingInfo DUMBINFO = new BindingInfo();
}
