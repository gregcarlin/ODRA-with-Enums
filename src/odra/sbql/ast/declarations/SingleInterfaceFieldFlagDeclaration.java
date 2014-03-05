package odra.sbql.ast.declarations;


public class SingleInterfaceFieldFlagDeclaration extends InterfaceFieldFlagDeclaration {
	private int flag;

	private SingleInterfaceFieldFlagDeclaration(int i) {
		flag = i;
	}
	
	public SingleInterfaceFieldFlagDeclaration[] flattenFlags() {
		return new SingleInterfaceFieldFlagDeclaration[] { this };
	}
	
	public int encodeFlag() {
		int v = 0;
		
		if (this == createFlag)
			v |= CREATE;
		else if (this == retrieveFlag)
			v |= RETRIEVE;
		else if (this == updateFlag)
			v |= UPDATE;
		else if (this == deleteFlag)
			v |= DELETE;
		else
			assert false : "unexpected interface field flag";

		return v;
	}

	private final static int CREATE = 1;
	private final static int RETRIEVE = 2;
	private final static int UPDATE = 4;
	private final static int DELETE = 8;

	public static SingleInterfaceFieldFlagDeclaration createFlag = new SingleInterfaceFieldFlagDeclaration(CREATE);
	public static SingleInterfaceFieldFlagDeclaration retrieveFlag = new SingleInterfaceFieldFlagDeclaration(RETRIEVE);
	public static SingleInterfaceFieldFlagDeclaration updateFlag = new SingleInterfaceFieldFlagDeclaration(UPDATE);
	public static SingleInterfaceFieldFlagDeclaration deleteFlag = new SingleInterfaceFieldFlagDeclaration(DELETE);
}
