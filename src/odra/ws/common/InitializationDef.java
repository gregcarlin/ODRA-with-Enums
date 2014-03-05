package odra.ws.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gathers initialization statements required to execute procedure with class type parameters.
 * Object will be created basing on definition included here before execution as
 * session variables instances. Procedure will contain references to
 * exact items of such created collections instead of ad-hoc object of class type
 * creation (because ODRA does not support such instantination).
 * @author <a href="mailto: merdacz@gmail.com">Marcin Daczkowski</a>
 *
 */
public class InitializationDef {

	// serves a counting purpose to track the last index to use for a given class
	private Map<String, Integer> counters = new HashMap<String, Integer>();
	private List<String> initializationStatements = new ArrayList<String>();

	/**
	 * Adds statement for pre-execution.
	 * @param instanceName
	 * @param sbqlCode
	 */
	public void addStatement(String instanceName, String sbqlCode) {
		this.initializationStatements.add(sbqlCode);
		Integer previousCount = this.counters.get(instanceName);
		Integer newCount = previousCount == null ? 1 : previousCount + 1;
		this.counters.put(instanceName, newCount);

	}

	/**
	 * Returns the next index to use for a given class instance name.
	 * @param instanceName
	 * @return
	 */
	public int getNextIndex(String instanceName) {
		Integer statementsCount = this.counters.get(instanceName);
		return statementsCount == null  ? 0 : statementsCount.intValue();

	}

	/**
	 * Return pre-registered statements
	 * @return
	 */
	public List<String> getStatements() {
		return this.initializationStatements;
	}

	/**
	 * Returns class names for which at least one statement has been registered
	 * @return
	 */
	public Set<String> getClassNames() {
		return this.counters.keySet();
	}
}
