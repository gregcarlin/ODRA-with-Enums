package odra.db;

import java.util.HashMap;
import java.util.Map;

import odra.db.objects.data.DBSystemModule;
import odra.sbql.emiter.OpCodes;

/**
 * This class holds the information on standard arithmetic, logic and string
 * sbql operators.
 * 
 * @author raist, stencel
 */

public class StdEnvironment {
    private static StdEnvironment env;


    /**
     * Initializes an empty standard enviroment.
     */
    private StdEnvironment() {
    }

    /**
     * Fills the environment with the information on operators.
     * 
     * @param sysmod
     *                is the interface object which represents the system
     *                module.
     */
    public static void init(DBSystemModule sysmod) throws DatabaseException {
	env = new StdEnvironment();

	OID string = sysmod.createStringType("string");
	OID integer = sysmod.createIntegerType("integer");
	OID bool = sysmod.createBooleanType("boolean");
	OID real = sysmod.createRealType("real");
	OID date = sysmod.createDateType("date");
	OID blank = sysmod.createVoidType("void");
	OID oid = sysmod.createNamedSpecialType("OID", string.getObjectName());

	
	env.stringType = string;
	env.integerType = integer;
	env.booleanType = bool;
	env.realType = real;
	env.dateType = date;
	env.voidType = blank;
	env.oidType = oid;

	env.addIntInt = sysmod.createBinaryOperator("+", integer, integer,
		integer, null, null, OpCodes.addI.getCode());
	env.addIntReal = sysmod.createBinaryOperator("+", real, integer, real,
		real, null, OpCodes.addR.getCode());
	env.addRealInt = sysmod.createBinaryOperator("+", real, real, integer,
		null, real, OpCodes.addR.getCode());
	env.addRealReal = sysmod.createBinaryOperator("+", real, real, real,
		null, null, OpCodes.addR.getCode());
	env.addIntString = sysmod.createBinaryOperator("+", string, integer,
		string, string, null, OpCodes.conS.getCode());
	env.addStringInt = sysmod.createBinaryOperator("+", string, string,
		integer, null, string, OpCodes.conS.getCode());
	env.addRealString = sysmod.createBinaryOperator("+", string, real,
		string, string, null, OpCodes.conS.getCode());
	env.addStringReal = sysmod.createBinaryOperator("+", string, string,
		real, null, string, OpCodes.conS.getCode());
	env.addDateString = sysmod.createBinaryOperator("+", string, date,
		string, string, null, OpCodes.conS.getCode());
	env.addStringDate = sysmod.createBinaryOperator("+", string, string,
		date, null, string, OpCodes.conS.getCode());
	env.addStringString = sysmod.createBinaryOperator("+", string, string,
		string, null, null, OpCodes.conS.getCode());

	env.eqIntInt = sysmod.createBinaryOperator("=", bool, integer, integer,
		null, null, OpCodes.eqI.getCode());
	env.eqIntReal = sysmod.createBinaryOperator("=", bool, integer, real,
		real, null, OpCodes.eqR.getCode());
	env.eqRealInt = sysmod.createBinaryOperator("=", bool, real, integer,
		null, real, OpCodes.eqR.getCode());
	env.eqRealReal = sysmod.createBinaryOperator("=", bool, real, real,
		null, null, OpCodes.eqR.getCode());
	env.eqIntString = sysmod.createBinaryOperator("=", bool, integer,
		string, string, null, OpCodes.eqS.getCode());
	env.eqStringInt = sysmod.createBinaryOperator("=", bool, string,
		integer, null, string, OpCodes.eqS.getCode());
	env.eqRealString = sysmod.createBinaryOperator("=", bool, real, string,
		string, null, OpCodes.eqS.getCode());
	env.eqStringReal = sysmod.createBinaryOperator("=", bool, string, real,
		null, string, OpCodes.eqS.getCode());
	env.eqBoolBool = sysmod.createBinaryOperator("=", bool, bool, bool,
		null, null, OpCodes.eqB.getCode());
	env.eqBoolString = sysmod.createBinaryOperator("=", bool, bool, string,
		string, null, OpCodes.eqS.getCode());
	env.eqStringBool = sysmod.createBinaryOperator("=", bool, string, bool,
		null, string, OpCodes.eqS.getCode());
	env.eqStringString = sysmod.createBinaryOperator("=", bool, string,
		string, null, null, OpCodes.eqS.getCode());
	env.eqDateDate = sysmod.createBinaryOperator("=", bool, date, date,
		null, null, OpCodes.eqD.getCode());

	env.neqIntInt = sysmod.createBinaryOperator("<>", bool, integer,
		integer, null, null, OpCodes.nEqI.getCode());
	env.neqIntReal = sysmod.createBinaryOperator("<>", bool, integer, real,
		real, null, OpCodes.nEqR.getCode());
	env.neqRealInt = sysmod.createBinaryOperator("<>", bool, real, integer,
		null, real, OpCodes.nEqR.getCode());
	env.neqRealReal = sysmod.createBinaryOperator("<>", bool, real, real,
		null, null, OpCodes.nEqR.getCode());
	env.neqIntString = sysmod.createBinaryOperator("<>", bool, integer,
		string, string, null, OpCodes.nEqS.getCode());
	env.neqStringInt = sysmod.createBinaryOperator("<>", bool, string,
		integer, null, string, OpCodes.nEqS.getCode());
	env.neqRealString = sysmod.createBinaryOperator("<>", bool, real,
		string, string, null, OpCodes.nEqS.getCode());
	env.neqStringReal = sysmod.createBinaryOperator("<>", bool, string,
		real, null, string, OpCodes.nEqS.getCode());
	env.neqBoolBool = sysmod.createBinaryOperator("<>", bool, bool, bool,
		null, null, OpCodes.nEqB.getCode());
	env.neqBoolString = sysmod.createBinaryOperator("<>", bool, bool,
		string, string, null, OpCodes.nEqS.getCode());
	env.neqStringBool = sysmod.createBinaryOperator("<>", bool, string,
		bool, null, string, OpCodes.nEqS.getCode());
	env.neqStringString = sysmod.createBinaryOperator("<>", bool, string,
		string, null, null, OpCodes.nEqS.getCode());
	env.neqDateDate = sysmod.createBinaryOperator("<>", bool, date, date,
		null, null, OpCodes.nEqD.getCode());

	env.subIntInt = sysmod.createBinaryOperator("-", integer, integer,
		integer, null, null, OpCodes.subI.getCode());
	env.subIntReal = sysmod.createBinaryOperator("-", real, integer, real,
		real, null, OpCodes.subR.getCode());
	env.subRealInt = sysmod.createBinaryOperator("-", real, real, integer,
		null, real, OpCodes.subR.getCode());
	env.subRealReal = sysmod.createBinaryOperator("-", real, real, real,
		null, null, OpCodes.subR.getCode());

	env.mulIntInt = sysmod.createBinaryOperator("*", integer, integer,
		integer, null, null, OpCodes.mulI.getCode());
	env.mulIntReal = sysmod.createBinaryOperator("*", real, integer, real,
		real, null, OpCodes.mulR.getCode());
	env.mulRealInt = sysmod.createBinaryOperator("*", real, real, integer,
		null, real, OpCodes.mulR.getCode());
	env.mulRealReal = sysmod.createBinaryOperator("*", real, real, real,
		null, null, OpCodes.mulR.getCode());

	env.divIntInt = sysmod.createBinaryOperator("/", integer, integer,
		integer, null, null, OpCodes.divI.getCode());
	env.divIntReal = sysmod.createBinaryOperator("/", real, integer, real,
		real, null, OpCodes.divR.getCode());
	env.divRealInt = sysmod.createBinaryOperator("/", real, real, integer,
		null, real, OpCodes.divR.getCode());
	env.divRealReal = sysmod.createBinaryOperator("/", real, real, real,
		null, null, OpCodes.divR.getCode());

	env.remIntInt = sysmod.createBinaryOperator("%", integer, integer,
		integer, null, null, OpCodes.remI.getCode());
	env.remIntReal = sysmod.createBinaryOperator("%", real, integer, real,
		real, null, OpCodes.remR.getCode());
	env.remRealInt = sysmod.createBinaryOperator("%", real, real, integer,
		null, real, OpCodes.remR.getCode());
	env.remRealReal = sysmod.createBinaryOperator("%", real, real, real,
		null, null, OpCodes.remR.getCode());

	env.assIntInt = sysmod.createBinaryOperator(":=", integer, integer,
		integer, null, null, OpCodes.storeI.getCode());
	env.assRealReal = sysmod.createBinaryOperator(":=", real, real, real,
		null, null, OpCodes.storeR.getCode());
	env.assBoolBool = sysmod.createBinaryOperator(":=", bool, bool, bool,
		null, null, OpCodes.storeB.getCode());
	env.assStringString = sysmod.createBinaryOperator(":=", string, string,
		string, null, null, OpCodes.storeS.getCode());
	env.assDateDate = sysmod.createBinaryOperator(":=", date, date, date,
		null, null, OpCodes.storeD.getCode());

	env.loEqIntInt = sysmod.createBinaryOperator("<=", bool, integer,
		integer, null, null, OpCodes.loEqI.getCode());
	env.loEqIntReal = sysmod.createBinaryOperator("<=", bool, integer,
		real, real, null, OpCodes.loEqR.getCode());
	env.loEqRealInt = sysmod.createBinaryOperator("<=", bool, real,
		integer, null, real, OpCodes.loEqR.getCode());
	env.loEqRealReal = sysmod.createBinaryOperator("<=", bool, real, real,
		null, null, OpCodes.loEqR.getCode());
	env.loEqStringString = sysmod.createBinaryOperator("<=", bool, string,
		string, null, null, OpCodes.loEqS.getCode());
	env.loEqDateDate = sysmod.createBinaryOperator("<=", bool, date, date,
		null, null, OpCodes.loEqD.getCode());

	env.loIntInt = sysmod.createBinaryOperator("<", bool, integer, integer,
		null, null, OpCodes.loI.getCode());
	env.loIntReal = sysmod.createBinaryOperator("<", bool, integer, real,
		real, null, OpCodes.loR.getCode());
	env.loRealInt = sysmod.createBinaryOperator("<", bool, real, integer,
		null, real, OpCodes.loR.getCode());
	env.loRealReal = sysmod.createBinaryOperator("<", bool, real, real,
		null, null, OpCodes.loR.getCode());
	env.loStringString = sysmod.createBinaryOperator("<", bool, string,
		string, null, null, OpCodes.loS.getCode());
	env.loDateDate = sysmod.createBinaryOperator("<", bool, date, date,
		null, null, OpCodes.loD.getCode());

	env.grIntInt = sysmod.createBinaryOperator(">", bool, integer, integer,
		null, null, OpCodes.grI.getCode());
	env.grIntReal = sysmod.createBinaryOperator(">", bool, integer, real,
		real, null, OpCodes.grR.getCode());
	env.grRealInt = sysmod.createBinaryOperator(">", bool, real, integer,
		null, real, OpCodes.grR.getCode());
	env.grRealReal = sysmod.createBinaryOperator(">", bool, real, real,
		null, null, OpCodes.grR.getCode());
	env.grStringString = sysmod.createBinaryOperator(">", bool, string,
		string, null, null, OpCodes.grS.getCode());
	env.grDateDate = sysmod.createBinaryOperator(">", bool, date, date,
		null, null, OpCodes.grD.getCode());

	env.grEqIntInt = sysmod.createBinaryOperator(">=", bool, integer,
		integer, null, null, OpCodes.grEqI.getCode());
	env.grEqIntReal = sysmod.createBinaryOperator(">=", bool, integer,
		real, real, null, OpCodes.grEqR.getCode());
	env.grEqRealInt = sysmod.createBinaryOperator(">=", bool, real,
		integer, null, real, OpCodes.grEqR.getCode());
	env.grEqRealReal = sysmod.createBinaryOperator(">=", bool, real, real,
		null, null, OpCodes.grEqR.getCode());
	env.grEqStringString = sysmod.createBinaryOperator(">=", bool, string,
		string, null, null, OpCodes.grEqS.getCode());
	env.grEqDateDate = sysmod.createBinaryOperator(">=", bool, date, date,
		null, null, OpCodes.grEqD.getCode());

	env.andBoolBool = sysmod.createBinaryOperator("and", bool, bool, bool,
		null, null, OpCodes.and.getCode());
	env.orBoolBool = sysmod.createBinaryOperator("or", bool, bool, bool,
		null, null, OpCodes.or.getCode());

	env.negInt = sysmod.createUnaryOperator("-", integer, integer,
		OpCodes.negI.getCode());
	env.negReal = sysmod.createUnaryOperator("-", real, real, OpCodes.negR.getCode());
	env.negBool = sysmod.createUnaryOperator("not", bool, bool,
		OpCodes.notB.getCode());

	env.matchString = sysmod.createBinaryOperator("~~", bool, string,
		string, string, string, OpCodes.matchString.getCode());
	env.notMatchString = sysmod.createBinaryOperator("~!", bool, string,
		string, string, string, OpCodes.notMatchString.getCode());

    }

    /**
     * Returms the stored standard enviroment.
     * 
     * @return is the standard enviroment.
     */
    public static StdEnvironment getStdEnvironment() {
	return env;
    }
    public OID oidType;
    public OID assIntInt, assRealReal, assBoolBool, assStringString,
	    assDateDate;

    public OID stringType, integerType, booleanType, realType, dateType,
	    voidType;

    public OID addIntInt, addIntString, addStringInt, addIntReal, addRealInt,
	    addRealReal, addRealString, addStringReal, addStringDate,
	    addDateString, addStringString;

    public OID eqIntInt, eqIntReal, eqRealInt, eqRealReal, eqIntString,
	    eqStringInt, eqRealString, eqStringReal, eqBoolBool, eqBoolString,
	    eqStringBool, eqStringString, eqDateDate;

    public OID neqIntInt, neqIntReal, neqRealInt, neqRealReal, neqIntString,
	    neqStringInt, neqRealString, neqStringReal, neqBoolBool,
	    neqBoolString, neqStringBool, neqStringString, neqDateDate;

    public OID subIntInt, subIntReal, subRealInt, subRealReal;

    public OID mulIntInt, mulIntReal, mulRealInt, mulRealReal;

    public OID remIntInt, remIntReal, remRealInt, remRealReal;

    public OID divIntInt, divIntReal, divRealInt, divRealReal;

    public OID loEqIntInt, loEqIntReal, loEqRealInt, loEqRealReal,
	    loEqStringString, loEqDateDate;

    public OID loIntInt, loIntReal, loRealInt, loRealReal, loStringString,
	    loDateDate;

    public OID grIntInt, grIntReal, grRealInt, grRealReal, grStringString,
	    grDateDate;

    public OID grEqIntInt, grEqIntReal, grEqRealInt, grEqRealReal,
	    grEqStringString, grEqDateDate;

    public OID andBoolBool, orBoolBool;

    public OID negInt, negReal, negBool;

    public OID matchString, notMatchString;
    
    public OID castInt2Real, castReal2Int, castInt2String,
    castReal2String, castDate2String, castBool2String, castString2Int,
    castString2Real;

    // endpoint and proxies meta dictionaries
    public OID endpoints, proxies;

}
