package odra.ws.common;

import java.util.List;


/**
 * Represent parameter definition passed to SBQLHelpers methods
 *
 * @since 2007-01-16
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class ParamDef {
	private String name;
	private String value;
	private List<String> depended;
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return this.value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getDepended() {
		return depended;
	}
	public void setDepended(List<String> depended) {
		this.depended = depended;
	}


}
