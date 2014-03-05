package odra.db.indices;

import java.util.Date;
import java.util.TreeSet;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.dataaccess.DBObjectDirectKeyAccess;
import odra.db.indices.dataaccess.DBObjectToKeyAccess;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.dataaccess.TemporaryResultAccess;
import odra.db.indices.keytypes.BooleanKeyType;
import odra.db.indices.keytypes.DateKeyType;
import odra.db.indices.keytypes.DoubleKeyType;
import odra.db.indices.keytypes.IntegerKeyType;
import odra.db.indices.keytypes.ReferenceKeyType;
import odra.db.indices.keytypes.StringKeyType;
import odra.db.indices.recordtypes.BooleanEnumRecordType;
import odra.db.indices.recordtypes.DateLHRangeRecordType;
import odra.db.indices.recordtypes.DoubleLHRangeRecordType;
import odra.db.indices.recordtypes.EnumRecordType;
import odra.db.indices.recordtypes.IntegerLHRangeRecordType;
import odra.db.indices.recordtypes.IntegerRangeEnumRecordType;
import odra.db.indices.recordtypes.LHEnumRecordType;
import odra.db.indices.recordtypes.MultiKeyEnumRecordType;
import odra.db.indices.recordtypes.MultiKeyLHRangeRecordType;
import odra.db.indices.recordtypes.MultiKeyRecordType;
import odra.db.indices.recordtypes.RecordType;
import odra.db.indices.recordtypes.SimpleRecordType;
import odra.db.indices.recordtypes.StringLHRangeRecordType;
import odra.db.indices.structures.LinearHashingMap;
import odra.db.indices.updating.IndexableStore;
import odra.db.indices.updating.TriggersManager;
import odra.db.objects.data.DBIndex;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBPrimitiveType;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.PrimitiveTypeKind;
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.utils.ASTNodeSkipper;
import odra.sbql.ast.utils.patterns.ASTNodePattern;
import odra.sbql.ast.utils.patterns.DirectReferencePathPattern;
import odra.sbql.emiter.EmiterFactory;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.JulietGen;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.optimizers.OptimizationFramework;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.optimizers.queryrewrite.index.IndexASTChecker;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import odra.sbql.results.compiletime.ValueSignature;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.StructResult;
import odra.sbql.typechecker.SBQLTypeChecker;

/**
 * This class is responsible for creating index in typechecking mode.
 * This is done by introducing proper DBIndex structure in module and MBIndex in metabase.<br>
 * While creating index class checks if index definition is proper from the
 * point of view of index optimizer and automatic index updating.
 * 
 * @author tkowals
 * @version 1.0
 */
class SafeGenerateIndex {

	protected static IndexManager idxMgr;

	/**
	 * @param idxMgr index register object
	 * @param node AST of index creating query
	 * @param mod index home module
	 * @param idxname name of an index
	 * @param temporary true for temporary, false for materialized index
	 * @param prog index creating query
	 * @param rParams parameters indicating record types for each key (dense, range or enum)
	 * @return OID of MBIndex object
	 * @throws Exception
	 */
	public static OID createIndex(IndexManager idxMgr, ASTNode node, DBModule mod, String idxname, boolean temporary, String prog, String []rParams) throws DatabaseException {
		
		SafeGenerateIndex.idxMgr = idxMgr;  
			
		if (temporary)
			((JoinExpression) node).replaceSubexpr(((JoinExpression) node).getRightExpression(), new DerefExpression(((JoinExpression) node).getRightExpression()));

		SBQLTypeChecker checker = new SBQLTypeChecker(mod);
		checker.typecheckAdHocQuery(node);

		// TODO: Maybe non-key values should be checked for uniqueness - then reference and pointer should be allowed
		if (new ASTNodeSkipper(new DirectReferencePathPattern()).findChildNodes(((JoinExpression) node).getLeftExpression()).size() > 0)
			throw new IndicesException("Nonkeys should not be constructed using reference and pointer objects (to avoid duplicates)", null);
		 		
		if (!(((Expression) node).getSignature() instanceof StructSignature)) 
			throw new IndicesException("Query should return nonkey and key values", null);
		
		StructSignature sign = (StructSignature) ((Expression) node).getSignature();

		checkIndexQuery(sign, temporary);
		
		checkIndexDependencies(sign);
		
		DataAccess dataAccess;
		if (temporary)
			dataAccess = new TemporaryResultAccess(); 
		else
			dataAccess = getSafeDataAccessKind(sign, mod);

//		ASTNode optnode = DeepCopyAST.copy(node); // FIXME: copy with signatures causes errors
		ASTNode optnode = DeepCopyAST.copyWithoutSign(node);
		checker.typecheckAdHocQuery(optnode);
		OptimizationFramework opfrm = new OptimizationFramework(checker);
		opfrm.setOptimizationSequence(OptimizationSequence.getForName(OptimizationSequence.INDEXCONSTRUCTOR));
		optnode = opfrm.optimize(optnode, mod);
		BagResult bres = getQueryResult(optnode, mod);
		
		RecordType recordType = getSafeRecordType(sign, rParams, bres);

		boolean uniqueNonkeys = areNonkeysUnique(sign);
	
		OID idxoid = mod.createLinearHashingIndex(idxname, temporary, recordType, 
				dataAccess, prog, 
				idxMgr.generator.getCode().getByteCode(),
				idxMgr.generator.getConstantPool().getAsBytes());
		
		if (!temporary) {
			LinearHashingMap lhm = (LinearHashingMap) new DBIndex(idxoid).getIndex();
			lhm.initialize(LHBUCKETSCOUNT, LHBUCKETCAPACITY, LHPERSPLITLOAD, LHPERMERGELOAD);
			
			if (Database.getStore() instanceof IndexableStore)
				new TriggersManager(new DBIndex(idxoid).getTriggersManagerRef(), mod).enableAutomaticUpdating(sign);
			else 
				insertResult(bres, idxoid);
		} else {
			
		}
		
		OID mbidxoid = mod.getMetaBase().createMetaIndex(idxname, temporary, sign, recordType, uniqueNonkeys);
		
		return mbidxoid;		
		
	}

	private static void checkIndexQuery(StructSignature sign, boolean temporary) throws DatabaseException {
		
		Signature[] signs = sign.getFields();
		
		if (!(signs[0] instanceof ReferenceSignature))
			throw new IndicesException("Nonkey value must be reference", null);
		
		//TODO: check if isVirtual flag apply independently to key fields  
		if (!temporary && ((ReferenceSignature) signs[0]).isVirtual()) 
			throw new IndicesException("Cannot materialize index of virtual data", null);
			
		for (int i = 0; i < signs.length; i++)
			if ((signs[i].getMinCard() != 1) || (signs[i].getMaxCard() != 1))
				throw new IndicesException("Unexpected cardinality in index creating query", null);

		for (int i = 1; i < signs.length; i++) {		 			
			if (signs[i].getOwnerExpression() == null)
				throw new IndicesException("All key values must be given explicitly", null);
			if (signs[i].getOwnerExpression().getSignature().getMaxCard() != 1)
				throw new IndicesException("Key cannot be a collection : " + AST2TextQueryDumper.AST2Text(signs[i].getOwnerExpression()), null);
		}
			
		if ((signs[0]).getOwnerExpression() != ((JoinExpression) (sign.getOwnerExpression())).getLeftExpression())
			throw new IndicesException("Nonkey expression error", null);

		ASTNodeSkipper skipper = new ASTNodeSkipper(new ASTNodePattern(CommaExpression.class));
		skipper.findChildNodes(((JoinExpression) (sign.getOwnerExpression())).getRightExpression());
		
		Expression[] keyexprs = (Expression[]) skipper.getResultAsArray(new Expression[0]);
		
		if (keyexprs.length != signs.length - 1)
			throw new IndicesException("Number of keys error - all keys should be given explicitly", null);
		
		for (int i = 0; i < keyexprs.length; i++)
			if ((signs[i + 1]).getOwnerExpression() != keyexprs[i]) 
				throw new IndicesException(i + " key expression error", null);						
			
	}

	
	private static void checkIndexDependencies(StructSignature sign) throws DatabaseException {
		
		Signature[] signs = sign.getFields();
		
		IndexASTChecker checker = new IndexASTChecker();
		for (int i = 0; i < signs.length; i++)
			checker.markIndexSubAST(signs[i].getOwnerExpression());

		for (int i = 0; i < signs.length; i++)
			checker.checkASTBoundTo(signs[i].getOwnerExpression(), signs[0].getOwnerExpression());				
			
	}
	
	private static boolean areNonkeysUnique(StructSignature sign) {
		Signature[] signs = sign.getFields();
		
		for (int i = 1; i < signs.length; i++)			 			
			if (signs[i].getOwnerExpression().getSignature().getMaxCard() > 1)
				return false;		
		return true;
		
	}
	
	private static DataAccess getSafeDataAccessKind(StructSignature sign, DBModule mod) throws DatabaseException {
	
		Signature[] signs = sign.getFields();
		
		int keyCount = signs.length - 1;

		byte[][] n2kBytecode = getN2KBytecode(sign.getOwnerExpression(), mod);
		
		if (keyCount ==  1) {
			if (signs[1] instanceof ReferenceSignature)			
				return new DBObjectDirectKeyAccess(n2kBytecode[0], n2kBytecode[1]);
			else {			
				if (signs[1] instanceof ValueSignature) { 
					OID sigval = ((ValueSignature) signs[1]).value;
					MBPrimitiveType mbpt = new MBPrimitiveType(sigval);
					if (mbpt.isValid() && mbpt.getTypeKind() == PrimitiveTypeKind.INTEGER_TYPE)
						return new DBObjectDirectKeyAccess(n2kBytecode[0], n2kBytecode[1]);
				}
			}
		}
			
		return new DBObjectToKeyAccess(n2kBytecode[0], n2kBytecode[1]);
		
	}

	private static RecordType getSafeRecordType(StructSignature sign, String[] rParams, BagResult bres) throws DatabaseException {	
	
		Signature[] signs = sign.getFields();
	
		int keyCount = signs.length - 1;
	
		RecordType[] recordType = new RecordType[keyCount];		
		boolean[] obligatory = new boolean[keyCount];
		
		OID sigval;
		
		for (int j = 0; j < keyCount; j++) {				
			obligatory[j] = false;
			if (signs[j + 1].getOwnerExpression().getSignature().getMinCard() == 0)
				obligatory[j] = true;
			if (signs[j + 1] instanceof ReferenceSignature) {
				sigval = ((ReferenceSignature) signs[j + 1]).value;
				MBVariable mvar = new MBVariable(sigval);
				sigval = mvar.getType();
			}
			else if (signs[j + 1] instanceof ValueSignature) 
				sigval = ((ValueSignature) signs[j + 1]).value;
			else throw new IndicesException("Key value type not supported for indexing!", null); 
			
			MBPrimitiveType mbpt = new MBPrimitiveType(sigval);
			if (!mbpt.isValid()) {
				MBVariable mvar = new MBVariable(sigval);
				if (mvar.isValid()) {
					recordType[j] = new SimpleRecordType(new ReferenceKeyType());
					continue;
				}
				throw new IndicesException("Key value type not supported for indexing!", null);
			}
			switch (mbpt.getTypeKind()) {
			case INTEGER_TYPE:
				recordType[j] = new SimpleRecordType(new IntegerKeyType());
				break;
			case REAL_TYPE:
				recordType[j] = new SimpleRecordType(new DoubleKeyType());
				break;
			case STRING_TYPE:
				recordType[j] = new SimpleRecordType(new StringKeyType());
				break;
			case BOOLEAN_TYPE:
				recordType[j] = new BooleanEnumRecordType(obligatory[j]);
				break;
			case DATE_TYPE:
				recordType[j] = new SimpleRecordType(new DateKeyType());
				break;
			default:
				throw new IndicesException("Key value type not supported for indexing!", null);
			}				
		}
							
		if (keyCount > 1)
			return updateRecordTypes(rParams, obligatory, new MultiKeyRecordType(recordType), bres);
		
		return updateRecordTypes(rParams, obligatory, recordType[0], bres);
		
	}

	protected static RecordType updateRecordTypes(String[] rParams, boolean[] obligatory, RecordType recordType, BagResult bres) throws DatabaseException {
		if (rParams.length == 0)
			return recordType;

		boolean isEnum = true;
		for(String param: rParams)
			if (!param.equals(ENUM_INDEX)) {
				isEnum = false;
				break;
			}
		
		if (recordType instanceof MultiKeyRecordType) {
			
			MultiKeyRecordType mRecordType = (MultiKeyRecordType) recordType;
			for(int i = 0; i < mRecordType.countRecordType(); i++)
				if (isEnum) mRecordType.setRecordType(i, updateToEnumRecordType(rParams, obligatory, mRecordType.getRecordType(i), bres, i + 1));
				else mRecordType.setRecordType(i, updateSingleRecordType(rParams, obligatory, mRecordType.getRecordType(i), bres, i + 1));					
			
			if (isEnum) {
				mRecordType = new MultiKeyEnumRecordType(mRecordType.recordType);
			}  else if (mRecordType.supportRangeQueries()) {
				mRecordType = new MultiKeyLHRangeRecordType(mRecordType.recordType);
			}
			return mRecordType;
		}	
		
		if (isEnum) return updateToEnumRecordType(rParams, obligatory, recordType, bres, 1);
		return updateSingleRecordType(rParams, obligatory, recordType, bres, 1);
	}
	
	// TODO: Allow different distribution of data (not only equally) 
	private static RecordType updateSingleRecordType(String[] rParams, boolean[] obligatory, RecordType recordType, BagResult bres, int num) throws DatabaseException {	
		if (rParams[num - 1].equals(RANGE_INDEX)) {
			if (recordType.keyType instanceof IntegerKeyType) {
				int min = Integer.MAX_VALUE;
				int max = Integer.MIN_VALUE;
				for(int i = 0; i < bres.elementsCount(); i++) {
					int value = (Integer) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					if (value < min) min = value;
					if (value > max) max = value;
				}				
				return new IntegerLHRangeRecordType(min, max, LHBUCKETSCOUNT);		
			}
			if (recordType.keyType instanceof DoubleKeyType) {
				double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;
				for(int i = 0; i < bres.elementsCount(); i++) {
					double value = (Double) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					if (value < min) min = value;
					if (value > max) max = value;
				}				
				return new DoubleLHRangeRecordType(min, max);
			}
			if (recordType.keyType instanceof StringKeyType) {
				char[] minch = new char[4];
				char[] maxch = new char[4];
				for(int i = 0; i < 4; i++) {
					minch[i] = StringLHRangeRecordType.MAX_CHAR;
					maxch[i] = StringLHRangeRecordType.MIN_CHAR;
				}
				String min = new String(maxch);
				String max = new String(minch);
				for(int i = 0; i < bres.elementsCount(); i++) {
					String value = (String) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					if (value.compareTo(min) < 0) min = value;
					if (value.compareTo(max) > 0) max = value;
				}				
				return new StringLHRangeRecordType(min, max);
			}
			if (recordType.keyType instanceof DateKeyType) {
				Date val0 = (Date) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(0)).fieldAt(num));
				Date min = new Date(val0.getTime());
				Date max = new Date(val0.getTime());
				for(int i = 1; i < bres.elementsCount(); i++) {
					Date value = (Date) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					if (value.compareTo(min) < 0) min.setTime(value.getTime());
					if (value.compareTo(max) > 0) max.setTime(value.getTime());
				}				
				return new DateLHRangeRecordType(min, max);
			}
			throw new IndicesException("Unsupported range index on " + recordType.getClass().getName(), null);
		}	
		
		if (rParams[num - 1].equals(ENUM_INDEX)) {
			if (recordType.keyType instanceof BooleanKeyType) {					
				return recordType;
			}
			if (recordType.keyType instanceof IntegerKeyType) {
				TreeSet<Integer> set = new TreeSet<Integer>(); 
				for(int i = 0; i < bres.elementsCount(); i++) {
					Integer value = (Integer) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					set.add(value);
				}	
				/* TODO: fix IntegerLHRangeRecordType to be not obligatory (then uncomment)
				if (set.last() - set.first() < 2 * set.size())
					return new IntegerLHRangeRecordType(set.first(), set.last(), LHBUCKETSCOUNT);
				*/
				return new LHEnumRecordType(new IntegerKeyType(), set, LHBUCKETSCOUNT, obligatory[num - 1]);				
			}
			if (recordType.keyType instanceof DoubleKeyType) {
				TreeSet<Double> set = new TreeSet<Double>(); 
				for(int i = 0; i < bres.elementsCount(); i++) {
					Double value = (Double) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					set.add(value);
				}
				return new LHEnumRecordType(new DoubleKeyType(), set, LHBUCKETSCOUNT, obligatory[num - 1]);				
			}
			if (recordType.keyType instanceof StringKeyType) {				
				TreeSet<String> set = new TreeSet<String>(); 
				for(int i = 0; i < bres.elementsCount(); i++) {
					String value = (String) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					set.add(value);
				}	
				return new LHEnumRecordType(new StringKeyType(), set, LHBUCKETSCOUNT, obligatory[num - 1]);
			}
			if (recordType.keyType instanceof DateKeyType) {
				TreeSet<Date> set = new TreeSet<Date>(); 
				for(int i = 0; i < bres.elementsCount(); i++) {
					Date value = (Date) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					set.add(value);
				}
				return new LHEnumRecordType(new DateKeyType(), set, LHBUCKETSCOUNT, obligatory[num - 1]);				
			}
			throw new IndicesException("Unsupported enum index on " + recordType.getClass().getName(), null);			
		}
		
		if (!rParams[num - 1].equals(DENSE_INDEX))
			throw new IndicesException("Expected range, enum or dense parameter not " + rParams[num - 1], null);
			
		return recordType;
	}

	private static RecordType updateToEnumRecordType(String[] rParams, boolean[] obligatory, RecordType recordType, BagResult bres, int num) throws DatabaseException {	

		if (rParams[num - 1].equals(ENUM_INDEX)) {
			if (recordType.keyType instanceof BooleanKeyType) {					
				return recordType;
			}
			if (recordType.keyType instanceof IntegerKeyType) {
				TreeSet<Integer> set = new TreeSet<Integer>(); 
				for(int i = 0; i < bres.elementsCount(); i++) {
					Integer value = (Integer) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					set.add(value);
				}	
				if (set.last() - set.first() < 2 * set.size())
					return new IntegerRangeEnumRecordType(set.first(), set.last());
				return new EnumRecordType(new IntegerKeyType(), set, obligatory[num - 1]);				
			}
			if (recordType.keyType instanceof DoubleKeyType) {
				TreeSet<Double> set = new TreeSet<Double>(); 
				for(int i = 0; i < bres.elementsCount(); i++) {
					Double value = (Double) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					set.add(value);
				}
				return new EnumRecordType(new DoubleKeyType(), set, obligatory[num - 1]);				
			}
			if (recordType.keyType instanceof StringKeyType) {				
				TreeSet<String> set = new TreeSet<String>(); 
				for(int i = 0; i < bres.elementsCount(); i++) {
					String value = (String) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					set.add(value);
				}	
				return new EnumRecordType(new StringKeyType(), set, obligatory[num - 1]);
			}
			if (recordType.keyType instanceof DateKeyType) {
				TreeSet<Date> set = new TreeSet<Date>(); 
				for(int i = 0; i < bres.elementsCount(); i++) {
					Date value = (Date) recordType.keyType.key2KeyValue(((StructResult) bres.elementAt(i)).fieldAt(num));
					set.add(value);
				}
				return new EnumRecordType(new DateKeyType(), set, obligatory[num - 1]);				
			}
			throw new IndicesException("Unsupported enum index on " + recordType.getClass().getName(), null);			
		}
			
		throw new IndicesException("Enum parameter expected not " + rParams[num - 1], null);
			
	}
	
	protected static byte[][] getN2KBytecode(ASTNode node, DBModule mod) throws DatabaseException {
		
		Expression keyNode = ((JoinExpression) node).getRightExpression();
		idxMgr.generator = EmiterFactory.getJulietCodeGenerator(mod);
		idxMgr.generator.generate(keyNode);
	
		JulietCode code = JulietGen.genIndexKeyValueCode(idxMgr.generator.getCode());			
		return new byte[][] {code.getByteCode(), idxMgr.generator.getConstantPool().getAsBytes()};		
	
	}

	protected static BagResult getQueryResult(ASTNode node, DBModule mod) throws DatabaseException {
					
		idxMgr.generator = EmiterFactory.getJulietCodeGenerator(mod);
		
		SBQLInterpreter interpreter = new SBQLInterpreter(mod);

		idxMgr.generator.generate(node);
		
		byte[] byteCode = idxMgr.generator.getCode().getByteCode();
		byte[] cnstPool = idxMgr.generator.getConstantPool().getAsBytes();
	
		interpreter.runCode(byteCode, cnstPool);
		
		BagResult bres = new BagResult();
		bres.addAll(interpreter.getResult());	
	
		return bres;
		
	}

	// inserting result into index
	protected static void insertResult(BagResult bres, OID idxoid) throws DatabaseException {						
		DBIndex idxdb = new DBIndex(idxoid);
		
		idxdb.getIndex().insertResult(bres); 
	}
	
	public static final String RANGE_INDEX = "range";
	public static final String DENSE_INDEX = "dense";
	public static final String ENUM_INDEX = "enum";

	public static final int LHBUCKETSCOUNT = 13,
							LHBUCKETCAPACITY = 5, 
							LHPERSPLITLOAD = 75, 
							LHPERMERGELOAD = 65;
	
}
