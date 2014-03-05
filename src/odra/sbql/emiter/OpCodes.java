package odra.sbql.emiter;

import java.util.Map;
import java.util.TreeMap;

public enum OpCodes {
    	nop(0),
    	pop (0),
	dup (1),
	dup2 (2),
	dup_x1 (1),
	dup_x2 (1),
	swap (1),
	bswap2 ,
	ldcR_0 ,
	ldcR ,
	ldcS ,
	ldI ,
	ldTrue ,
	ldFalse ,
	ldBag ,
	ldE ,
	ldSE ,
	ldLE ,
	
	//ENVS
	dsEnv ,
	crnEnv ,
	dsnEnv ,
	bind ,
	bindTop ,
	bindAgg,
	enterBinder ,
	enterRefAsBinder ,
	
	//counter opcodes 
	
	crCntr ,
	incCntr ,
	ldCntr ,
	endCntr ,
	rstCntr ,
	rbdCntr ,
	insPrt ,
	insPrt2 ,
	insPrt3 ,
	
	
//	 control opcodes
	bra ,
	lRet ,
	braRet ,
	braTrue ,
	braFalse ,
	
	//operations on bags
	crpd ,
	union ,
	in ,
	intersect ,
	diff ,
	orby ,
	extr ,
	fltn ,
	unique ,
	atmost ,
	atleast ,
	
	//parallel opcodes
	parallelUnion ,
	
	// object creation (argument - reference to the environment, parameter)
	crI ,
	crS ,
	crR ,
	crB ,
	crCpx ,
	crRef ,
	crAgg ,
	crAggCard ,
	
	
	// object creation in collections (argument - reference to the environment, parameter)
	crAggI ,
	crAggS ,
	crAggR ,
	crAggB ,
	crAggCpx ,
	crAggRef ,
	crAggD ,
	
	//dynamic create 
	crDyn ,

	// arithmetic operators opcodes 
	addI ,
	addR ,
	conS ,
	subI ,
	subR ,
	mulI ,
	mulR ,
	divI ,
	divR ,
	remI ,
	remR ,
	negI ,
	negR ,
	//dynamic versions
	dynAdd ,
	dynSub ,
	dynMul ,
	dynDiv ,
	dynRem ,
	dynNeg ,
	

	// logical operators opcodes 
	grI ,
	grR ,
	grEqI ,
	grEqR ,
	loI ,
	loR ,
	loEqI ,
	loEqR ,
	eqI ,
	eqR ,
	eqS ,
	eqB ,
	eqRef ,
	eqStruct ,
	nEqI ,
	nEqR ,
	nEqS ,
	nEqB ,
	nEqRef ,
	nEqStruct ,
	grS ,
	grEqS ,
	loS ,
	loEqS ,
	and ,
	or ,
	notB ,
	//string matching
	matchString ,
	notMatchString ,
	//dynamic versions
	dynGr ,
	dynLo ,
	dynGrEq ,
	dynLoEq ,
	dynEq ,
	dynNEq ,
	dynOr ,
	dynAnd ,
	dynNot ,
	// unary operators opcodes
	cnt ,
	exist ,
	ref ,
	colRef ,
	dynRef ,
	derefI ,
	derefR ,
	derefB ,
	derefS ,
	derefCpx ,
	derefRef ,
	derefColI ,
	derefColR ,
	derefColB ,
	derefColS ,
	derefColCpx ,
	derefColRef ,
	dynDeref ,
	as ,
	colAs ,
	grAs ,
	iinc ,
	idec ,
	//coerce
	i2r ,
	r2i ,
	i2s ,
	r2s ,
	d2s ,
	b2s ,
	s2i ,
	s2r ,
	//dynamic coerce (with runtime checking)
	dyn2r ,
	dyn2i ,
	dyn2s ,
	dyn2b ,
	s2b ,
	//procedure call and control
	ret ,
	retv ,
	call ,
	external ,
	crLE ,
	initLoc ,
	
	//assign
	storeI ,
	storeR ,
	storeB ,
	storeS ,
	storeRef ,
	storeRevRef ,
	dynStore ,
	del ,
	delChildren ,
	move ,
	moveCol ,
	virtMove ,
	moveNamed,
	moveNamedCol,
//	others and new
	modPush ,
	modPop ,
	fnd ,
	crvid ,
	ifnSack ,
	max ,
	min ,
	avg ,
	single ,
	nonEmpty ,
	rng ,
	
	derefStruct ,
	derefBinder ,
	tobag ,
	tostruct ,

	/** execute SQL statement clause */
	execsql ,

	//misc.
	random ,
	randomObj ,

	//date opcodes
	crD ,
	eqD ,
	grEqD ,
	loD ,
	grD ,
	loEqD ,
	nEqD ,
	storeD ,
	derefD ,
	dyn2d ,
	s2d ,
	ldcD ,
	derefColD ,
	dateprec ,
		
	//M1 model
	crInstRef ,
	instof ,
	castClass ,
	dynCast ,
	
	//reflection opcodes
	reflName ,
	reflParent ,
	reflBind ,
	
	ifVirt ,
	ldvGenPrc ,
	retSub ,
	exception ,
	remoteQuery,
	remoteAsyncQuery,
	waitForAsync,
	athrow,
////////////////////////////////////////////
// transaction related
	beginTrans ,
	abortTrans,
	commitTrans,
	
	commitStmt,
	
	//object identifier serialization & deserialization
	serializeOID,
	deserializeOID, rename, colRename,
	
	//lazy failure support
	beginLazyFailure, endLazyFailure;



   private int params;
   private OpCodes(){
       params = 0;
   }
   private OpCodes(int params){
    this.params = params;   
   }
    public int getCode() {
	  return   this.ordinal();
    }   
    
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {	
	return name();
    }

    private final static Map<Integer,  OpCodes> opCodes = new TreeMap<Integer,OpCodes>();
    private final static Map<String,  OpCodes> sopCodes = new TreeMap<String,OpCodes>();

       public static OpCodes getOpCode (int value) {
	   assert opCodes.get(value) != null : "unknown opcode";
          return opCodes.get(value);
       }
       public static OpCodes getOpCode (String value) {
	          return sopCodes.get(value);
       }
       
       static {
	   for(OpCodes opcode : OpCodes.values()){
	       opCodes.put(opcode.getCode(), opcode);
	       sopCodes.put(opcode.name(), opcode);
	   }	   
       }
}