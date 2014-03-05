package odra.ws.facade;


/**
 * Provides information about available binding implementations
 *
 * @since 2006-12-17
 * @version 2009-01-28
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public enum WSBindingType {
	SOAP11("Soap11");

	private final String name;

	WSBindingType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public static WSBindingType getByCode(String name) {
		for (WSBindingType value : values()) {
			if (value.name.equals(name)) {
				return value;
			}
		}
		return null;
	}


}
