package odra.db.indices.updating;

import java.util.Stack;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.updating.triggers.KeyUpdateTrigger;
import odra.db.indices.updating.triggers.NonkeyUpdateTrigger;
import odra.db.indices.updating.triggers.NonkeypathUpdateTrigger;
import odra.db.indices.updating.triggers.UpdateTrigger;
import odra.db.indices.updating.triggers.RootUpdateTrigger;
import odra.db.objects.data.DBModule;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.emiter.EmiterFactory;
import odra.sbql.emiter.IJulietCodeGenerator;
import odra.sbql.emiter.JulietCode;
import odra.sbql.emiter.JulietGen;
import odra.sbql.results.compiletime.StructSignature;

/**
 * Class that finds store objects associated with given index and enables them for automatic updating
 * 
 * @author tkowals
 * @version 1.0
 */
public class TriggersManager {

	IndexableStore store;
	OID oid;
	DBModule mod;
	
	public TriggersManager(OID oid, DBModule mod) throws DatabaseException { 	
		this.oid = oid;
		this.mod = mod;
		store = (IndexableStore) Database.getStore();
	}
	
	public void enableAutomaticUpdating(StructSignature sign) throws DatabaseException {
		assert oid.countChildren() == 0 : "Empty complex object needed for triggers manager";

		store.createComplexObject(store.addName("$updtrig"), oid, 1);
		
		initializeUpdateTriggers(sign, mod);
		
		getUpdateTrigger().enableAutomaticUpdating(mod.getDatabaseEntry());
		
	}

	public void disableAutomaticUpdating() throws DatabaseException {
		
		getUpdateTrigger().disableAutomaticUpdating(mod.getDatabaseEntry());
		
	}
		
	private void initializeUpdateTriggers(StructSignature sign, DBModule mod) throws DatabaseException {
		
		IJulietCodeGenerator generator = EmiterFactory.getJulietCodeGenerator(mod);
		
		Expression expr = ((JoinExpression) (sign.getOwnerExpression())).getLeftExpression();		
		while (expr instanceof DotExpression)
			expr = ((DotExpression) (expr)).getRightExpression();		

		Stack<NameExpression> namestack = new Stack<NameExpression>();
		do { 
			if (expr instanceof NameExpression) 
				namestack.push((NameExpression) expr);
			expr = expr.getSignature().getAssociatedExpression(); 
		} while (expr != null);		
	
		RootUpdateTrigger parent = null;
		
		String currentName = "$root";		
		
		while (!namestack.empty()) {

			NameExpression nameExpr = namestack.pop();
			generator.generate(nameExpr);
			
			if (parent == null) {
				parent = RootUpdateTrigger.initialize(
						getUpdateTrigRef(),
						currentName,
						oid.getParent(),
						generator.getCode().getByteCode(), 
						generator.getConstantPool().getAsBytes()
						);
			} else {
				JulietCode code = JulietGen.genIndexKeyValueCode(generator.getCode());
				parent = NonkeypathUpdateTrigger.initialize(
						parent.getUpdateTriggerRef(),
						currentName,
						oid.getParent(),
						code.getByteCode(),
						generator.getConstantPool().getAsBytes()
				);
				
			}
			currentName = nameExpr.name().value();
			
		}
		
		UpdateTrigger nonkeyOUT = NonkeyUpdateTrigger.initialize(
				parent.getUpdateTriggerRef(),
				currentName,
				oid.getParent());					
						
		KeyUpdateTrigger.initialize(nonkeyOUT.getUpdateTriggerRef(),
						((JoinExpression) (sign.getOwnerExpression())).getRightExpression().toString(),
						oid.getParent());

		
		KeyUpdateTrigger.initialize(parent.getUpdateTriggerRef(),
						((JoinExpression) (sign.getOwnerExpression())).getRightExpression().toString(),
						oid.getParent());

	}	 
	
	private final RootUpdateTrigger getUpdateTrigger() throws DatabaseException {
		return (RootUpdateTrigger) UpdateTrigger.generateTrigger(getUpdateTrigRef(), null);
	}
	
	/***********************************
	 * access to general subobjects describing the TriggersManager
	 * */ 
	
	private final OID getUpdateTrigRef() throws DatabaseException {
		return oid.getChildAt(UPDATETRIGGERS_POS);
	}

	private static final int UPDATETRIGGERS_POS = 0;

//	private static final int FIELDSCOUNT = 1;
}
 