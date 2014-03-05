package odra.sbql.typechecker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBAnnotatedVariableObject;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBEnum;
import odra.db.objects.meta.MBInterface;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MBView;
import odra.db.objects.meta.MetaObjectKind;
import odra.db.schema.OdraViewSchema;
import odra.sbql.SBQLException;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.compiletime.BinderSignature;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.results.compiletime.StructSignature;
import odra.sbql.results.compiletime.ValueSignature;
import odra.sbql.results.compiletime.util.SignatureInfo;
import odra.sbql.stack.Binder;
import odra.sbql.stack.BindingInfo;
import odra.sbql.stack.Nester;
import odra.sbql.stack.RemoteNester;
import odra.sbql.stack.SBQLStack;
import odra.sbql.stack.StackFrame;
import odra.sessions.Session;

/**
 * StaticEnvironmentManager Manages static environment
 * 
 * @author Radek Adamus
 * @since 2007-03-28 last modified: 2007-05-12
 * @version 1.0
 */
class StaticEnvironmentManager {
	Stack<StackFrame> stack = new Stack<StackFrame>();
	private SBQLStack sbqlStack = new SBQLStack(new StaticBindingManager());
	Stack<EnvironmentInfo> eistack = new Stack<EnvironmentInfo>();
	DBModule module;

	/**
	 * @param mod
	 */
	StaticEnvironmentManager(DBModule mod, boolean isParmDependent) throws DatabaseException {
		this.module = mod;
		OID systemMetabaseEntry = Database.getSystemModule().getMetabaseEntry();
		sbqlStack.initialize(new Nester(systemMetabaseEntry));
		StackFrame frame = new StackFrame();
		initEnvironment(mod, frame, true);

		if (isParmDependent)
			injectDependenParameters();

		sbqlStack.enterAll(frame);
	
	}

	StaticEnvironmentManager(DBModule mod) throws DatabaseException {

		this(mod, false);
	}

	int getEnvsSize() {
		return this.eistack.size();
	}

	/**
	 * Injects remote dependent parameters' signatures into static stack
	 * 
	 * @param metabase
	 * @param frame
	 * @throws DatabaseException
	 */
	private void injectDependenParameters() throws DatabaseException {
		if (Session.exists() && Session.getCurrent().isParmDependent()) {
			ArrayList<SignatureInfo> injParms = Session.getCurrent().getParmSigantures();

			for (SignatureInfo si : injParms) {
				Database.getNameIndex().addName(si.getName());
				BinderSignature bs = si.getBinderSignature();

				createStaticNestedEnvironment(bs);
			}

			Session.getCurrent().setParmSigantures(null);
		}
	}

	private void initEnvironment(DBModule mod, StackFrame frame, boolean withImports) throws DatabaseException {
		OID systemMetabaseEntry = Database.getSystemModule().getMetabaseEntry();
		frame.enter(new Nester(mod.getMetabaseEntry()));
		// initMetaEntry(frame,mod.getMetabaseEntry());
		frame.enter(new Nester(mod.getSessionMetaDataEntry()));
		// initMetaEntry(frame,mod.getSessionMetaDataEntry());
		// binder with module name
		frame.enter(new Binder(mod.getOID().getObjectNameId(), new ReferenceSignature(mod.getOID())));
		// self binder
		frame.enter(new Binder(Database.getStore().addName("self"), new ReferenceSignature(mod.getOID())));
		// named binder to system module
		frame.enter(new Binder(Database.getSystemModule().getOID().getObjectNameId(), new ReferenceSignature(mod.getOID())));

		if (withImports) {
			// system module is inside the compiled imports
			OID[] imprefs = mod.getCompiledImports();
			OID[] aliases = mod.getImportsAliases();
			for (int i = 0; i < imprefs.length; i++) {
				DBModule impmod = new DBModule(imprefs[i].derefReference());
				OID metabaseEntry = impmod.getMetabaseEntry();
				OID sessionEntry = impmod.getSessionMetaDataEntry();
				if (!metabaseEntry.equals(systemMetabaseEntry)) {
					String alias = aliases[i].derefString();
					if (!"".equals(alias))
						frame.enter(new Binder(Database.getStore().addName(alias), new ReferenceSignature(impmod.getOID())));
					frame.enter(new Binder(impmod.getOID().getObjectNameId(), new ReferenceSignature(impmod.getOID())));
					frame.enter(new Nester(metabaseEntry));
					frame.enter(new Nester(sessionEntry));
				}
			}
		}

	}

	/**
	 * @param frame
	 * @param metabaseEntry
	 * @throws DatabaseException
	 */
	private void initMetaEntry(StackFrame frame, OID metabaseEntry) throws DatabaseException {
		for (OID metaoid : metabaseEntry.derefComplex()) {
			if (!metaoid.isComplexObject()) {
				continue;
			}

		}

	}

	void enterBinder(Binder b) {
		this.sbqlStack.enter(b);
	}

	private void reset() {
		stack.clear();
	}

	AbstractQueryResult[] bind(String name) throws DatabaseException {
		return sbqlStack.bind(SBQLTypeCheckerHelper.name2id(name));

	}

	AbstractQueryResult[] bind(String name, BindingInfo bi) throws DatabaseException {
		AbstractQueryResult[] result = sbqlStack.bind(SBQLTypeCheckerHelper.name2id(name), bi, Integer.MAX_VALUE);
		if (result.length > 0)
			this.calculateRelativeSectionNumber(bi);
		return result;

	}

	AbstractQueryResult[] bindTop(String name, BindingInfo bi) throws DatabaseException {
		AbstractQueryResult[] result = sbqlStack.bind(SBQLTypeCheckerHelper.name2id(name), bi, eistack.peek().framesOpened);
		if (result.length > 0)
			bi.relativeSection = this.eistack.size() - 1;
		return result;
	}

	void createEnvironment() {
		EnvironmentInfo ei = new EnvironmentInfo();
		ei.baseEnvsSize = sbqlStack.environmentSize();
		ei.framesOpened = 1;
		ei.relativeSection = this.eistack.size();
		eistack.push(ei);
		this.sbqlStack.createEnvironment();
	}

	EnvironmentInfo createStaticNestedEnvironment(Signature sig) throws SBQLException {
		EnvironmentInfo ei = new EnvironmentInfo();
		ei.baseEnvsSize = sbqlStack.environmentSize();

		Stack<StackFrame> sf = this.statNested(sig);
		ei.framesOpened = sf.size();
		ei.relativeSection = this.eistack.size();
		sbqlStack.createNestedEnvironment(sf);
		this.eistack.push(ei);

		return ei;
	}

	EnvironmentInfo getTopEnvironmentInfo() {
		return this.eistack.peek();
	}

	void destroyEnvironment() {
		assert !eistack.empty() : "environment stack must not be empty";
		EnvironmentInfo ei = eistack.pop();
		for (int i = 0; i < ei.framesOpened; i++)
			sbqlStack.destroyEnvironment();
	}

	/**
	 * Computes the static nested function.
	 * 
	 * @param res
	 *            is the argument signature which is to be "unnested".
	 */
	Stack<StackFrame> statNested(Signature sig) throws SBQLException {
		reset();
		StackFrame frame = new StackFrame();
		// topmost section at the bottom !!
		stack.push(frame);
		nested(sig, frame);
		return stack;
	}

	private void nested(Signature res, StackFrame stackframe) throws SBQLException {
		// To unnest a StructSignature, recursively unnest its components and
		// collect the results
		try {
			if (res instanceof StructSignature) {
				StructSignature str = (StructSignature) res;

				Signature[] rarr = str.getFields();
				for (int i = 0; i < rarr.length; i++)
					nested(rarr[i], stackframe);
			}
			// To unnest a BinderSignature, simply return it
			else if (res instanceof BinderSignature) {
				BinderSignature bin = (BinderSignature) res;
				bin.value.setAssociatedExpression(res.getOwnerExpression());
				stackframe.enter(new Binder(SBQLTypeCheckerHelper.name2id(bin.name), bin.value));
			}
			// To unnest a ReferenceSignature, consult the metabase
			else if (res instanceof ReferenceSignature) {
				ReferenceSignature ref = (ReferenceSignature) res;
				// check if we have virtual reference
				if (ref.isVirtual()) {
					this.nestedVirtualReference(stackframe, ref);
					return;
				}
				// extension for binders to imported modules
				if (new DBModule(ref.value).isValid()) {
					this.initEnvironment(new DBModule(ref.value), stackframe, false);
					return;

				}

				MBObject mbo = new MBObject(ref.value);

				MetaObjectKind bo = mbo.getObjectKind();
				if (bo == MetaObjectKind.ANNOTATED_VARIABLE_OBJECT) {

					mbo = new MBObject(new MBAnnotatedVariableObject(ref.value).getValueRef());
					bo = mbo.getObjectKind();
				}

				switch (mbo.getObjectKind()) {
				case PROCEDURE_OBJECT:
					break;
				case VIEW_OBJECT:
					this.nestedView(stackframe, ref, ref.value);
					break;
				case INTERFACE_OBJECT:
					this.nestedInterface(stackframe, ref, ref.value);

					break;
				case LINK_OBJECT:
				case SCHEMA_OBJECT:
					stackframe.enter(new RemoteNester(mbo.getOID()));
					break;

				case STRUCT_OBJECT:
					this.nestedComplex(stackframe, ref, ref.value);
					break;
				case ENUM_OBJECT:
					this.nestedEnum(stackframe, ref, ref.value);
					break;	
				case CLASS_OBJECT:
					// static_fields - class object state (if introduced)
					break;
				case VARIABLE_OBJECT:
					// variable...
					MBVariable mvo = new MBVariable(mbo.getOID());
					// ...and its type
					MBObject mvot = new MBObject(mvo.getType());

					// this covers the situation where nested is realized on a
					// reference object.
					// the referred object is pushed onto the static environment
					// stack
					// only if it is a variable
					// otherwise it cannot be bound
					// SBA allows binding only variables by name
					if (mvo.isTypeReference()) {
						this.nestedPointer(stackframe, ref, mvo);

						// otherwise, do nothing
					}
					// this is used when we deal with non-reference objects
					else {
						MetaObjectKind knd = mvot.getObjectKind();
						while (knd == MetaObjectKind.TYPEDEF_OBJECT) {
							mvot = new MBObject(SBQLTypeCheckerHelper.enforceExpandTypeDef(mvot.getOID(), new String[1]));
							knd = mvot.getObjectKind();
						}
						switch (knd) {

						case STRUCT_OBJECT:
							this.nestedComplex(stackframe, ref, mvot.getOID());
							break;
						case CLASS_OBJECT:
							MBClass mbcls = new MBClass(mvot.getOID());
							nestedM1Class(ref, stackframe, mbcls, new Hashtable<OID, OID>());
							ReferenceSignature selfsig = new ReferenceSignature(mbcls.getSelfVariable());
							selfsig.setAssociatedExpression(res.getOwnerExpression());
							stackframe.enter(new Binder(Database.getStore().addName("self"), selfsig));
							break;

						}
					}

				}
			}
		} catch (DatabaseException e) {
			throw new TypeCheckerException(e);
		}
	}

	private void nestedM1Class(ReferenceSignature source, StackFrame instanceFrame, MBClass cls, Hashtable<OID, OID> fetched)
			throws DatabaseException {
		OID[] cflds = new MBStruct(cls.getType()).getFields();
		// add class structure fields to instance section
		for (OID fld : cflds) {
			ReferenceSignature fsig = new ReferenceSignature(fld, true);
			if (source != null) {
				fsig.setAssociatedExpression(source.getOwnerExpression());
			}
			instanceFrame.enter(new Binder(fld.getObjectNameId(), fsig));
		}

		// add method to a new section
		StackFrame classFrame = new StackFrame();
		for (OID methodid : cls.getMethods()) {
			ReferenceSignature fsig = new ReferenceSignature(methodid, true);
			if (source != null) {
				fsig.setAssociatedExpression(source.getOwnerExpression());
			}
			classFrame.enter(new Binder(methodid.getObjectNameId(), fsig));
		}
		stack.push(classFrame);

		// check superclasses
		for (OID superclassid : cls.getDirectSuperClasses()) {
			if (fetched.get(superclassid) != null)
				continue;
			nestedM1Class(source, instanceFrame, new MBClass(superclassid), fetched);
			fetched.put(superclassid, superclassid);
		}

	}

	private void evalViewOnNavigate(StackFrame frame, ReferenceSignature sig, MBView mbview) throws DatabaseException {
		OID navigateid = mbview.getGenericProc(OdraViewSchema.GenericNames.ON_NAVIGATE_NAME.toString());
		if (navigateid != null) {
			MBProcedure navigate = new MBProcedure(navigateid);
			// get type
			ReferenceSignature res = (ReferenceSignature) SBQLTypeCheckerHelper.inferSignature(navigate.getType(), navigate.getMinCard(), navigate
					.getMaxCard());
			// ReferenceSignature res = new ReferenceSignature(navigateid,
			// true);
			res.setAssociatedExpression(sig.getOwnerExpression());
			// int binderNameId = getObjectNameId(res);
			int binderNameId;
			if (res.isVirtual()) {
				binderNameId = new MBView(res.value).getVirtualObject().getObjectNameId();
			} else {
				binderNameId = res.value.getObjectNameId();
			}
			frame.enter(new Binder(binderNameId, res));

		}
	}

	/**
	 * @param cls
	 *            - meta-class
	 * @return
	 * @throws DatabaseException
	 */
	void generateClassMethodCompilationEnvironment(MBClass cls) throws DatabaseException {
		this.reset();
		StackFrame instanceFrame = new StackFrame();
		generateSelfSignature(instanceFrame, cls);
		stack.push(instanceFrame);
		this.nestedM1Class(null, instanceFrame, cls, new Hashtable<OID, OID>());
		sbqlStack.createNestedEnvironment(stack);
	}

	private void generateSelfSignature(StackFrame stackframe, MBClass mbclass) throws DatabaseException {

		stackframe.enter(new Binder(Database.getStore().addName("self"), new ReferenceSignature(mbclass.getSelfVariable())));
	}

	private void calculateRelativeSectionNumber(BindingInfo bi) {
		int boundat = bi.boundat;
		for (int i = this.eistack.size() - 1; i >= 0; i--) {
			EnvironmentInfo ei = this.eistack.get(i);
			if (boundat > ei.baseEnvsSize && boundat < (ei.baseEnvsSize + ei.framesOpened))
				bi.relativeSection = i;
		}
	}

	private void nestedVirtualReference(StackFrame stackframe, ReferenceSignature ref) throws DatabaseException {
		MBView mbview = new MBView(ref.value);
		assert mbview.isValid() : "virtual reference signature value must be an oid of metaview";
		for (OID field : mbview.getVirtualFieldsEntry().derefComplex()) {
			ReferenceSignature fsig = new ReferenceSignature(field);
			fsig.setAssociatedExpression(ref.getOwnerExpression());
			fsig.links.addAll(ref.links);
			stackframe.enter(new Binder(field.getObjectNameId(), fsig));
		}
		this.evalViewOnNavigate(stackframe, ref, mbview);
	}

	private void nestedView(StackFrame stackframe, ReferenceSignature parent, OID viewid) throws DatabaseException {
		MBView view = new MBView(viewid);
		for (OID viewfld : view.getViewFieldsEntry().derefComplex()) {
			ReferenceSignature vfsig = new ReferenceSignature(viewfld, true);
			vfsig.setAssociatedExpression(parent.getOwnerExpression());
			// copy links to signature
			vfsig.links.addAll(parent.links);
			stackframe.enter(new Binder(viewfld.getObjectNameId(), vfsig));
		}
	}

	private void nestedComplex(StackFrame stackframe, ReferenceSignature parent, OID coid) throws DatabaseException {
		MBStruct mbstruct = new MBStruct(coid);
		for (OID fld : mbstruct.getFields()) {
			ReferenceSignature fsig = new ReferenceSignature(fld, true);
			fsig.links = parent.links;
			fsig.setAssociatedExpression(parent.getOwnerExpression());
			// copy links to signature
			fsig.links.addAll(parent.links);
			stackframe.enter(new Binder(fld.getObjectNameId(), fsig));
		}
		for (OID prc : mbstruct.getProcedures()) {
			ReferenceSignature psig = new ReferenceSignature(prc, true);
			psig.setAssociatedExpression(parent.getOwnerExpression());
			// copy links to signature
			psig.links.addAll(parent.links);
			stackframe.enter(new Binder(prc.getObjectNameId(), psig));
		}
	}
	
	private void nestedEnum(StackFrame stackframe, ReferenceSignature parent, OID enuoid) throws DatabaseException {
		MBEnum mbenum = new MBEnum(enuoid);
		if(!mbenum.getState()){
			for (OID fld : mbenum.getFieldsValue()) {
				ReferenceSignature fsig = new ReferenceSignature(fld, false);
				fsig.links = parent.links;
				fsig.setAssociatedExpression(parent.getOwnerExpression());
				// copy links to signature
				fsig.links.addAll(parent.links);
				stackframe.enter(new Binder(fld.getObjectNameId(), fsig));
			
			}
			
		}
		else{
			Signature sig = SBQLTypeCheckerHelper.inferSignature(mbenum.getType());
			sig.setEnumerator(mbenum.getName());
			for (OID fld : mbenum.getFields()) {
				stackframe.enter(new Binder(fld.getObjectNameId(), sig));
			}
			
		}
		
		/*MetaObjectKind type = new MBObject(mbenum.getType()).getObjectKind();
		if(type==MetaObjectKind.PRIMITIVE_TYPE_OBJECT){
			for (OID fld : mbenum.getFields()) {
				ValueSignature vsig = new ValueSignature(mbenum.getType());
				vsig.links = parent.links;
				vsig.setAssociatedExpression(parent.getOwnerExpression());
				// copy links to signature
				vsig.links.addAll(parent.links);
				vsig.setEnumerator(mbenum.getType());
				stackframe.enter(new Binder(fld.getObjectNameId(), vsig));
			}
		}*/
		
		
		
		
		
	}

	private void nestedPointer(StackFrame stackframe, ReferenceSignature ref, MBVariable pvar) throws DatabaseException {
		MBVariable pointedVar = new MBVariable(pvar.getType());
		if (pointedVar.isValid()) {
			// the pointer leads to a variable
			ReferenceSignature sig = new ReferenceSignature(pointedVar.getOID());
			sig.links.addAll(ref.links);
			sig.setAssociatedExpression(ref.getOwnerExpression());
			stackframe.enter(new Binder(pointedVar.getNameId(), sig));
		}
		// else //UML compatibility extension the pointer leads to 'default'
		// class instance
		// if(new MBObject(pvar.getType()).getObjectKind() ==
		// MetaObjectKind.CLASS_OBJECT){
		// MBClass cls = new MBClass(pvar.getType());
		// ReferenceSignature sig = new
		// ReferenceSignature(cls.getDefaultVariable(),true);
		// sig.setAssociatedExpression(ref.getOwnerExpression());
		// stackframe.enter(new Binder(pvar.getType().getObjectNameId(), sig));
		// if(cls.hasInstanceName())
		// stackframe.enter(new Binder(sig.value.getObjectNameId(), sig));
		// }
		// otherwise, do nothing
	}

	private void nestedInterface(StackFrame stackframe, ReferenceSignature parent, OID ioid) throws DatabaseException {
		MBInterface iface = new MBInterface(ioid);

		System.out.println("nested na interfejsie");
	}

}
