package odra.sbql.parser;

import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBProcedure;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.ProcArgument;
import odra.db.schema.OdraProcedureSchema.ProcedureAST;
import odra.sbql.ast.declarations.CardinalityDeclaration;
import odra.sbql.ast.declarations.CompoundName;
import odra.sbql.ast.declarations.FieldDeclaration;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.SingleFieldDeclaration;
import odra.sbql.ast.declarations.TypeDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.CloseByExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.CreateLocalExpression;
import odra.sbql.ast.expressions.CreatePermanentExpression;
import odra.sbql.ast.expressions.DateprecissionExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ExternalNameExpression;
import odra.sbql.ast.expressions.ExternalProcedureCallExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.InsertCopyExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NowExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RandomExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ForEachStatement;
import odra.sbql.ast.statements.IfElseStatement;
import odra.sbql.ast.statements.IfStatement;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.sbql.ast.terminals.IntegerLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.ast.terminals.RealLiteral;
import odra.sbql.ast.terminals.StringLiteral;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.builder.procedures.ProcedureLocalEnvironmentConstructor;

/**
 * This class generates more complex SBQL expression in OCL parser.
 * 
 * @author stencel
 */


public final class OCLSBQLGenerator {

	static Expression generateLetExpression(FieldDeclaration v, Expression e, OCLParser parser) throws Exception {
		Expression res = null;
   		
		for (SingleFieldDeclaration vs : v.flattenFields()) {
   		   VariableDeclaration vd = ((VariableFieldDeclaration) vs).getVariableDeclaration();
   		   if (vd.getInitExpression() == null)
			  parser.report_error("Let expression with unintialized variable: " + vd.getName(), parser.getScanner().next_token());
		   else
			 if (res == null)
			   res = new GroupAsExpression(vd.getInitExpression(), new Name(vd.getName()));
			 else  
			   res = new CommaExpression(res, new GroupAsExpression(vd.getInitExpression(), new Name(vd.getName())));
   		}	  

		return new DotExpression(res, e);   											        
	}
	
	static Expression generateInfixCall(Expression e1, String n, Expression e2, OCLParser parser) {
        if (n.equals("and"))
            return new SimpleBinaryExpression(e1, e2, Operator.opAnd);
 		else if (n.equals("or"))
 			return new SimpleBinaryExpression(e1, e2, Operator.opOr);
 		else if (n.equals("implies"))
 			return new SimpleBinaryExpression(new SimpleUnaryExpression(e1, Operator.opNot), e2, Operator.opOr);
 		else if (n.equals("xor"))
 			return new EqualityExpression(e1, e2, Operator.opDifferent);
        else     
        	return new DotExpression(e1, new ProcedureCallExpression(new NameExpression(new Name(n)), e2)); 	
	}
	
	static Expression generatePrefixCall(String n, Expression e, OCLParser parser) {
		if (n.equals("not"))
	        return new SimpleUnaryExpression(e, Operator.opNot);
		else     
		    return new ProcedureCallExpression(new NameExpression(new Name(n)), e);
	}
	
	static Expression generateSingleVariableIterator(Expression e1, String n, VariableDeclaration v, Expression e2, OCLParser parser) throws Exception {
        if (n.equals("collect") || n.equals("collectNested"))
        	return new DotExpression(new AsExpression(e1, new Name(v.getName())), e2);
      	else if (n.equals("select"))
      		return new DotExpression(new WhereExpression(new AsExpression(e1, new Name(v.getName())), e2), new NameExpression(new Name(v.getName())));
      	else if (n.equals("any")) {
      		Name freshVar = new Name("else");      		
      		return
      			new DotExpression(
      				new GroupAsExpression(new DotExpression(new WhereExpression(new AsExpression(e1, new Name(v.getName())), e2), new NameExpression(new Name(v.getName()))), freshVar),
      				new RangeExpression(
      					new NameExpression(freshVar),
      					new RandomExpression(
      						new IntegerExpression(new IntegerLiteral(0)),
      						new SimpleBinaryExpression(new CountExpression(new NameExpression(freshVar)), new IntegerExpression(new IntegerLiteral(1)), Operator.opMinus)
      					)
      				)
      			);
      	} else if (n.equals("one"))
      		return 
      			new EqualityExpression(
      				new CountExpression(new WhereExpression(new AsExpression(e1, new Name(v.getName())), e2)),
      				new IntegerExpression(new IntegerLiteral(1)),
      				Operator.opEquals
      			);
      	else if (n.equals("sortedBy"))
      		return new DotExpression(new OrderByExpression(new AsExpression(e1, new Name(v.getName())), e2), new NameExpression(new Name(v.getName())));
      	else if (n.equals("reject"))
      		return new DotExpression(new WhereExpression(new AsExpression(e1, new Name(v.getName())), new SimpleUnaryExpression(e2, Operator.opNot)), new NameExpression(new Name(v.getName())));
      	else if (n.equals("exists"))
      		return new ForSomeExpression(new AsExpression(e1, new Name(v.getName())), e2);
      	else if (n.equals("forAll"))
      		return new ForAllExpression(new AsExpression(e1, new Name(v.getName())), e2);
      	else 
      	    parser.report_error("Unknown collection op with one iterator: " + n, parser.getScanner().next_token());

        return null;
	}
	
	static Expression generateDoubleVariableIterator(Expression e1, String n, VariableDeclaration v1, VariableDeclaration v2, Expression e2, OCLParser parser) throws Exception {	
		if (n.equals("forAll"))
	     	return new ForAllExpression(new CommaExpression(new AsExpression(e1, new Name(v1.getName())), new AsExpression(e1, new Name(v2.getName()))), e2);
		else if (n.equals("iterate")) {
			Name freshVar = new Name("then");
			Name freshCounter = new Name("if");
			Expression setupExpression = v2.getInitExpression();
			if (setupExpression == null)
				parser.report_error("No setup expression in iterate." , parser.getScanner().next_token());
			
			return
				new DotExpression(
					new WhereExpression(
						new DotExpression( 
							new GroupAsExpression(e1, freshVar),
							new CloseByExpression(
								new CommaExpression(new AsExpression(new IntegerExpression(new IntegerLiteral(0)), freshCounter), new GroupAsExpression(setupExpression, new Name(v2.getName()))),
								new IfThenExpression(
									new SimpleBinaryExpression(new NameExpression(freshCounter), new CountExpression(new NameExpression(freshVar)), Operator.opLower),	
									new DotExpression(
										new AsExpression(new RangeExpression(new NameExpression(freshVar),  new NameExpression(freshCounter)),  new Name(v1.getName())),
										new CommaExpression(
											new AsExpression(new SimpleBinaryExpression(new NameExpression(freshCounter), new IntegerExpression(new IntegerLiteral(1)), Operator.opPlus), freshCounter),
											new GroupAsExpression(e2, new Name(v2.getName()))
										)	
									)	
								)
							)
						),
						new SimpleBinaryExpression(new NameExpression(freshCounter), new CountExpression(e1), Operator.opGreaterEquals)			
					),
					new NameExpression(new Name(v2.getName()))
				);	
		} else
	     	parser.report_error("Unknown collection op with two iterators: " + n, parser.getScanner().next_token());

		return null;		
	}

	static Expression generateDoubleVariableIteratorWithTrick(Expression e1, String n, Expression v1, VariableDeclaration v2, Expression e2, OCLParser parser) throws Exception {	
		if (!(v1 instanceof NameExpression)) 
			parser.report_error("Bogus first iterator variable : " + v1.toString(), parser.getScanner().next_token());
		Name firstVar = ((NameExpression) v1).name();
		VariableDeclaration v1Decl = new VariableDeclaration(firstVar, null, new CardinalityDeclaration(), null);
		
		return generateDoubleVariableIterator(e1, n, v1Decl, v2, e2, parser);
	}

	static Expression generateCollectionCallNoArgs(Expression e, String n, OCLParser parser) throws Exception {
	    if (n.equals("allInstances"))
	     	return e;
	    else if (n.equals("size"))
	    	return new CountExpression(e);
	    else if (n.equals("sum"))
	    	return new SumExpression(e);
	    else if (n.equals("avg"))
	    	return new AvgExpression(e);
	    else if (n.equals("min"))
	    	return new MinExpression(e);
	    else if (n.equals("max"))
	    	return new MaxExpression(e);
	    else if (n.equals("isEmpty"))
	    	return new EqualityExpression(new CountExpression(e), new IntegerExpression(new IntegerLiteral(0)), Operator.opEquals);
	    else if (n.equals("notEmpty"))
	    	return new EqualityExpression(new CountExpression(e), new IntegerExpression(new IntegerLiteral(0)), Operator.opDifferent);
	    else if (n.equals("isUnique"))
	    	return new EqualityExpression(new CountExpression(e), new CountExpression(new UniqueExpression(e, false)), Operator.opEquals);
	    else if (n.equals("first"))
	    	return new RangeExpression(e, new IntegerExpression(new IntegerLiteral(0)));
	    else if (n.equals("last"))
	    	return new RangeExpression(e, new SimpleBinaryExpression(new CountExpression(e), new IntegerExpression(new IntegerLiteral(1)), Operator.opMinus));
	    else if (n.equals("asBag") || n.equals("flatten"))
	    	return e;						// all is a bag and no nested collections in SBQL
	    else if (n.equals("asSet"))			// remove duplicates
	    	return new UniqueExpression(e, false);		
	    else if (n.equals("asSequence"))   	// sort it somehow
	    	return new OrderByExpression(e, new IntegerExpression(new IntegerLiteral(0)));		
	    else if (n.equals("asOrderedSet"))	// remove duplicates and sort it somehow
	    	return new OrderByExpression(new UniqueExpression(e, false), new IntegerExpression(new IntegerLiteral(0)));		
	    else
	     	parser.report_error("Unknown unparameterized collection method: " + n, parser.getScanner().next_token());

	    return null;
	}

	static Expression generateCollectionCallWithArgs(Expression e1, String n, Expression e2, Expression t, OCLParser parser) throws Exception {
		Expression[] args = t.flatten();
		int numArgs = args.length + 1;
		
		switch (numArgs) {
		case 1: 
	        if (n.equals("at"))
            	return new RangeExpression(e1, new SimpleBinaryExpression(e2, new IntegerExpression(new IntegerLiteral(1)), Operator.opMinus));
	        else if (n.equals("collect") || n.equals("collectNested"))
	        	return new DotExpression(e1, e2);
    	 	else if (n.equals("select"))
    	 		return new WhereExpression(e1, e2);
          	else if (n.equals("any")) {
          		Name freshVar = new Name("for");      		
          		return
          			new DotExpression(
          				new GroupAsExpression(new WhereExpression(e1, e2), freshVar),
          				new RangeExpression(
          					new NameExpression(freshVar),
          					new RandomExpression(
          						new IntegerExpression(new IntegerLiteral(0)),
          						new SimpleBinaryExpression(new CountExpression(new NameExpression(freshVar)), new IntegerExpression(new IntegerLiteral(1)), Operator.opMinus)
          					)
          				)
          			);
          	} else if (n.equals("one"))
          		return 
          			new EqualityExpression(
          				new CountExpression(new WhereExpression(e1, e2)),
          				new IntegerExpression(new IntegerLiteral(1)),
          				Operator.opEquals
          			);
    	 	else if (n.equals("sortedBy"))
    	 		return new OrderByExpression(e1, e2);
	     	else if (n.equals("reject"))
	     		return new WhereExpression(e1, new SimpleUnaryExpression(e2, Operator.opNot));
     		else if (n.equals("exists"))
     			return new ForSomeExpression(e1, e2);
     		else if (n.equals("forAll"))
     			return new ForAllExpression(e1, e2);
     		else if (n.equals("union") || n.equals("append") || n.equals("including")) // all are synonyms of union
     			return new UnionExpression(e1, e2);
     		else if (n.equals("prepend"))
     			return new UnionExpression(e2, e1);	        
     		else if (n.equals("excluding"))
     			return new MinusExpression(e1, e2);	        
     		else if (n.equals("intersection"))
     			return new IntersectExpression(e1, e2);	        
     		else if (n.equals("symmetricDifference"))
     			return new MinusExpression(new UnionExpression(e1, e2), new IntersectExpression(e1, e2)); 
     		else if (n.equals("count")) // x->count(o) == how many o occurs in collection x
     			return new CountExpression(new IntersectExpression(e1, e2));
     		else if (n.equals("includes")) // x->includes(o) == x->count(o) <> 0
     			return new EqualityExpression(
     	   					new CountExpression(new IntersectExpression(e1, e2)),
     	   					new IntegerExpression(new IntegerLiteral(0)),
     	   					Operator.opDifferent
     	   				 );
     		else if (n.equals("includesAll"))
     			return new EqualityExpression(
     	   					new CountExpression(new IntersectExpression(e1, e2)),
     	   					new CountExpression(e2),
     	   					Operator.opEquals
     	   				 );
     		else if (n.equals("excludes") || n.equals("excludesAll")) // x->excludes*(o) == x->count(o) = 0
     			return new EqualityExpression(
     	   					new CountExpression(new IntersectExpression(e1, e2)),
     	   					new IntegerExpression(new IntegerLiteral(0)),
     	   					Operator.opEquals
     	   				 );
     		else if (n.equals("indexOf")) {
     			Name freshVar = new Name("unlink");
     			Name freshIndex = new Name("link");
     			return
     				new MinExpression(
     					new DotExpression(
     						new GroupAsExpression(e1, freshVar),
     						new DotExpression(
     							new WhereExpression(
     								new AsExpression(
     									generateRangeExpression(
     										new IntegerExpression(new IntegerLiteral(0)),
     											new SimpleBinaryExpression(
     											new CountExpression(new NameExpression(freshVar)),
     											new IntegerExpression(new IntegerLiteral(1)), 
     											Operator.opMinus
     										), parser
     									),
     									freshIndex
     								),
     								new EqualityExpression(e2, new RangeExpression(
     																new NameExpression(freshVar), 
     																new NameExpression(freshIndex)), 
     																Operator.opEquals
     														   )
     							),
     						new SimpleBinaryExpression(new NameExpression(freshIndex), new IntegerExpression(new IntegerLiteral(1)), Operator.opPlus)
     					)
     				)
     			);
			} else
				parser.report_error("Unknown collection method with 1 parameter: " + n, parser.getScanner().next_token());
	        break;
		case 2:
			Expression firstArg = e2;
			Expression secondArg = t.flatten()[0];
			if (n.equals("subSequence") || n.equals("subOrderedSet")) 
				return 
					new RangeExpression(e1, 
						generateRangeExpression(
							new SimpleBinaryExpression(firstArg,  new IntegerExpression(new IntegerLiteral(1)), Operator.opMinus),
							new SimpleBinaryExpression(secondArg, new IntegerExpression(new IntegerLiteral(1)), Operator.opMinus), parser
						)
					); 
			if (n.equals("insertAt")) {
				Name freshVar = new Name("while");
				return 
					new DotExpression(
						new GroupAsExpression(e1, freshVar),
						new UnionExpression(
							new UnionExpression(
								new RangeExpression(
									new NameExpression(freshVar), 
									generateRangeExpression(
										new IntegerExpression(new IntegerLiteral(0)), 
										new SimpleBinaryExpression(firstArg, new IntegerExpression(new IntegerLiteral(2)), Operator.opMinus), parser
									)
								),
								secondArg
							),
							new RangeExpression(
								new NameExpression(freshVar), 
								generateRangeExpression(
									new SimpleBinaryExpression(firstArg, new IntegerExpression(new IntegerLiteral(1)), Operator.opMinus),
									new SimpleBinaryExpression(
										new CountExpression(new NameExpression(freshVar)),
										new IntegerExpression(new IntegerLiteral(1)), 
										Operator.opMinus
									), parser
								)
							)
						)
					); 
			} else
				parser.report_error("Unknown collection method with 2 parameters: " + n, parser.getScanner().next_token());
		default: 
        	parser.report_error("Unknown collection method with " + numArgs + " parameters: " + n, parser.getScanner().next_token());
  		}

		return null;
	}

	static Expression generateOrdinaryCall(Expression e1, String n, Expression e2, OCLParser parser) throws Exception {
		switch (e2.flatten().length) {
		case 0: 
	        if (n.equals("round")) {
	        	Expression e1prime = new SimpleBinaryExpression(e1, new RealExpression(new RealLiteral(0.5)), Operator.opPlus);
	     	    return 
	     	    	new IfThenElseExpression(
	     	    		new SimpleBinaryExpression(new ToIntegerExpression(e1prime), e1prime, Operator.opGreater),
						new SimpleBinaryExpression(
							new ToIntegerExpression(e1prime), 
							new IntegerExpression(new IntegerLiteral(1)), 
							Operator.opMinus
						),
	     	    		new ToIntegerExpression(e1prime)
	     	    	);
	        } else if (n.equals("floor"))
	     	    return 
	     	    	new IfThenElseExpression(
	     	    		new SimpleBinaryExpression(new ToIntegerExpression(e1), e1, Operator.opGreater),
						new SimpleBinaryExpression(
							new ToIntegerExpression(e1), 
							new IntegerExpression(new IntegerLiteral(1)), 
							Operator.opMinus
						),
	     	    		new ToIntegerExpression(e1)
	     	    	);
			else if (n.equals("abs"))
	     	    return 
	     	    	new IfThenElseExpression(
	     	    		new SimpleBinaryExpression(e1, new IntegerExpression(new IntegerLiteral(0)), Operator.opGreater),
	     	    		e1,
	     	    		new SimpleUnaryExpression(e1, Operator.opMinus)
	     	    	);
    	 	else if (n.equals("toLower") || n.equals("toUpper"))
    	 		return generateExternalJavaCall(e1, n, null);
    	 	else	
    	 		break;
    	case 1: 
    		e2 = e2.flatten()[0]; 
	        if (n.equals("mod"))
            	return new SimpleBinaryExpression(e1, e2, Operator.opModulo);
	        else if (n.equals("div"))
	     	    return new SimpleBinaryExpression(e1, e2, Operator.opDivide);
			else if (n.equals("min"))
				return new IfThenElseExpression(new SimpleBinaryExpression(e1, e2, Operator.opLower), e1, e2);
			else if (n.equals("max"))
				return new IfThenElseExpression(new SimpleBinaryExpression(e1, e2, Operator.opGreater), e1, e2);
			else if (n.equals("concat"))
				return new SimpleBinaryExpression(e1, e2, Operator.opPlus);
    	 	else
    	 		break;
  		}
	    
        return new DotExpression(e1, new ProcedureCallExpression(new NameExpression(new Name(n)), e2)); 
	}

	static Expression generateProcedureCall(String n, Expression args, OCLParser parser) throws Exception {
		Expression[] flatArgs = args.flatten();
		
		switch (flatArgs.length) {
		case 0: 
	        if (n.equals("now"))
	        	return new NowExpression();
	        break;
	        
		case 2:
			if (n.equals("dateprec")) {
				Expression date = flatArgs[0];
				Expression prec = flatArgs[1];
				if (!(prec instanceof StringExpression))
					parser.report_error("The second arg of dateprec must be a string literal.", parser.getScanner().next_token());
				return new DateprecissionExpression(date, (StringExpression) prec);				
			}
			break;
		}
	    
		return new ProcedureCallExpression(new NameExpression(new Name(n)), args);
	}

	static Expression generateTupleExpression(FieldDeclaration v, OCLParser parser) throws Exception {
        Expression res = null;
   		
		for (SingleFieldDeclaration vs : v.flattenFields()) {
   		   VariableDeclaration vd = ((VariableFieldDeclaration) vs).getVariableDeclaration();
   		   if (vd.getInitExpression() == null)
			  parser.report_error("Tuple expression has item with no value: " + vd.getName(), parser.getScanner().next_token());
		   else
			 if (res == null)
			   res = new GroupAsExpression(vd.getInitExpression(), new Name(vd.getName()));
			 else  
			   res = new CommaExpression(res, new GroupAsExpression(vd.getInitExpression(), new Name(vd.getName())));
   		}	  

		return res;   											        
	}

	static Expression generateRangeExpression(Expression e1, Expression e2, OCLParser parser) throws Exception {
		
		Name freshVar = new Name("do");
		
        // ((e1 as a) closeby (((e1 + a) as a) where a <= e2)).a;
		return 
			new DotExpression(
				new CloseByExpression(
					new AsExpression(e1, freshVar),
					new WhereExpression(
						new AsExpression(
							new SimpleBinaryExpression(
								new NameExpression(freshVar),
								new IntegerExpression(new IntegerLiteral(1)),
								Operator.opPlus
							),
							freshVar
						),
						new SimpleBinaryExpression(new NameExpression(freshVar), e2, Operator.opLowerEquals)
					)
				),
				new NameExpression(freshVar)
			);
	}

	public static final Name switchVar = new Name("+++switch+++");		// such a name is not a valid identifier which cannot be generated by the parser
	
	private static Statement replaceLastCaseWithElse(Statement s, Statement replacement) throws Exception {
		if (s instanceof IfElseStatement) {
			IfElseStatement ifelse = (IfElseStatement) s;
			ifelse.setElseStatement(replaceLastCaseWithElse(ifelse.getElseStatement(), replacement)); 
			return ifelse;
		} else if (s instanceof IfStatement) {
			IfStatement ifStat = (IfStatement) s;
			IfElseStatement newIf = new IfElseStatement(ifStat.getExpression(), ifStat.getStatement(), replacement);
			
			newIf.column = ifStat.column;
			newIf.line= ifStat.line;
			
			return newIf;
		} else  
			throw new Exception("Method replaceLastCaseWithElse called with something else than if.");
	}
	
	
	static Statement generateSwitchStatement(Expression e, Statement cases, Statement defaults, OCLParser parser) throws Exception {
		Expression seedVar =  new AsExpression(e, switchVar);
		
		if (defaults != null)
			cases = replaceLastCaseWithElse(cases, defaults);
		
		return new ForEachStatement(seedVar, cases);
	}
	
	static Statement generateCasePart(Expression e, Statement s, Statement c, OCLParser parser) throws Exception {
		Expression test = new EqualityExpression(e, new NameExpression(switchVar), Operator.opEquals);
		
		if (c == null)
			return new IfStatement(test, s);
		else
			return new IfElseStatement(test, s, c);
	}
	
	static Statement generateForStatement(VariableDeclaration v, Expression e, Statement s, OCLParser parser) throws Exception {
		if (v.getInitExpression() == null)
			parser.report_error("For statement has uninitialized iteration variable: " + v.getName(), parser.getScanner().next_token());
		
		// let the variable be optional
		v.getCardinality().setMinimalCardinality(0);
		
		return 
			new SequentialStatement(
				new VariableDeclarationStatement(v),
				new SequentialStatement(
					new ExpressionStatement(new CreateLocalExpression(new Name(v.getName()), v.getInitExpression())),
					new WhileStatement(e, s)
				)
			);
	}

	static Statement generateInsertStatement(Expression e1, Expression e2, OCLParser parser) throws Exception {

		// special case
		// we map: name1 insert name2 create { ... }
		//     to: create name1 { ... }	
		if (e1 instanceof NameExpression && e2 instanceof CreateExpression) {
        	NameExpression ne = (NameExpression) e1;
        	CreateExpression ce2 = (CreateExpression) e2;
        	CreateExpression ce = new CreateExpression(ne.name(), ce2.getExpression()); 
        	return new ExpressionStatement(ce);
        }	
		
		// special case: we add self if no subject is specified
		if (e1 instanceof NameExpression)
			e1 = new DotExpression(new NameExpression(new Name("self")), e1);
		
		if (!(e1 instanceof DotExpression))
			parser.report_error("The left hand side of an insert/replace statement is not an attribute call.", parser.getScanner().next_token());
		
		DotExpression de1 = (DotExpression) e1;
		if (!(de1.getRightExpression() instanceof NameExpression))
			parser.report_error("The left hand side of an insert/replace statement is not an attribute call.", parser.getScanner().next_token());
		
		Name attribName = ((NameExpression) (de1.getRightExpression())).name();
		
		return new ExpressionStatement(new InsertCopyExpression(de1.getLeftExpression(), e2, attribName));
	}
		
	static Statement generateReplaceStatement(Expression e1, Expression e2, OCLParser parser) throws Exception {
		return 
			new SequentialStatement(
				new ExpressionStatement(new DeleteExpression(e1)),
				generateInsertStatement(e1, e2, parser)
			);
	}

	static Expression generateCPPAssigment(Expression e1, Expression e2, Operator op) {
		Name tmpName = new Name("+++Ass46436+&&^%&^^%&^");
		   	  
		return 
			new DotExpression(
				new GroupAsExpression(e1, tmpName),   
				new AssignExpression(
					new NameExpression(tmpName),	
					new SimpleBinaryExpression(new NameExpression(tmpName), e2, op),
					Operator.opAssign)
		    ); 
	}

	static Expression generateInitStatement(String n, Expression e, boolean addRef) {
		if (addRef)
			return new GroupAsExpression(new RefExpression(e), new Name(n));

		if (e instanceof CreateExpression) {
			CreateExpression ce = (CreateExpression) e;
			e = ce.getExpression();
		}
		return new GroupAsExpression(e, new Name(n));
	}

	static Statement generateLinkStatement(Expression e1, String n, Expression e2, OCLParser parser) {
		return 
			new ExpressionStatement(new InsertCopyExpression(e1, new RefExpression(e2), new Name(n))); 
	}
	
	static VariableDeclaration generateVariableDeclaration(String n, TypeDeclaration t, Expression e, OCLParser parser) {
		CardinalityDeclaration cd;
		if (t != null && t.VIDE_isCollection)
			cd = new CardinalityDeclaration(new IntegerLiteral(0));
		else
			cd = new CardinalityDeclaration();

		if (e == null)
		    e = new EmptyExpression();

		// special case
		// strip create in initialization
		// initialization is either way by value
		if (e instanceof CreateExpression) {
        	CreateExpression ce = (CreateExpression) e;
        	e = ce.getExpression();
        }	
		
		if (t instanceof NamedTypeDeclaration) {
			NamedTypeDeclaration nt = (NamedTypeDeclaration) t;
			String typeName = nt.getName().N2.value();
			if (nt.getName().N1 != null ||
					(	!(typeName.equals("integer")) &&
						!(typeName.equals("real")) 	  &&
						!(typeName.equals("date")) 	  &&
						!(typeName.equals("boolean")) &&
						!(typeName.equals("string")) 		)	)
				return new VariableDeclaration(new Name(n), t, cd, e, 1); 
		}	

		return new VariableDeclaration(new Name(n), t, cd, e);
	}
		
	static Statement generateUnlinkStatement(Expression e1, Vector<Name> v, Expression e2, OCLParser parser) {
		Statement body = new EmptyStatement();
			
		for (Name elem : v) {
			Expression deletedExpr = null; 
			
			if (e2 == null)
				deletedExpr = new NameExpression(elem);
			else {
				Name tmpName = new Name("+++Ass46$$$46565343s6+&&^%&^^%&^");
				deletedExpr = 
					new DotExpression(
						new WhereExpression(
							new AsExpression(new NameExpression(elem), tmpName),
							new EqualityExpression(
								new RefExpression(new DerefExpression(new NameExpression(tmpName))),
								new RefExpression(e2),
								Operator.opEquals
							)
						),
						new NameExpression(tmpName)
					);
			}
				
			body = new SequentialStatement(body, new ExpressionStatement(new DeleteExpression(deletedExpr)));
		}
		
		return new ForEachStatement(e1, body);
	}

	static Statement installMethodBody(CompoundName n, Statement s, OCLParser parser) throws Exception {
		String methodName = n.N2.value();
		String containerName = n.N1.N2.value();
		String givenModuleName = "";
		String moduleName = "admin";
		DBModule mod = null;
		
		if (n.N1.N1 != null) {
			givenModuleName = n.N1.N1.nameAsString();
			moduleName += "." + givenModuleName;
		}
					
		try {
			mod = Database.getModuleByName(moduleName);
		} catch (DatabaseException ex) {
        	parser.report_error("No module named " + givenModuleName + ".", parser.getScanner().next_token());
		}
        
        OID myclassid = mod.findFirstByName(containerName, mod.getMetabaseEntry());
        
        // check whether the container is a class
        if (myclassid != null) {
        	MBClass myclass = new MBClass(myclassid);
        	if (myclass.isValid()) {            	
        		OID myprocid = mod.findFirstByName(methodName, myclass.getMethodsEntry());
        		if(myprocid == null)
        			parser.report_error(methodName + " is not a method name in class " + containerName + ".", parser.getScanner().next_token());        
        		// everything is OK. We install the method.
        		installProcedureBody(mod, myprocid, s);
//        		
                return new ExpressionStatement(new StringExpression(new StringLiteral("Method " + n.nameAsString() + " installed.")));
        	} 
        }
        
        // check whether the container is a module
        DBModule containerMod = null;
        try {
        	containerMod = new DBModule(mod.getSubmodule(containerName));
        } catch (DatabaseException ex) {
    		parser.report_error(containerName + " is not a name of a class or a module.", parser.getScanner().next_token());
		}

        OID mbprocid = containerMod.findFirstByName(methodName, containerMod.getMetabaseEntry());
    	if (mbprocid == null)
    		parser.report_error(methodName + " is not a procedure name in module " + containerName + ".", parser.getScanner().next_token());

    	// everything is OK. We install the procedure.
    	MBProcedure proc = new MBProcedure(mbprocid);
        if (!(proc.isValid()))
    		parser.report_error(methodName + " is not a procedure name in module " + containerName + ".", parser.getScanner().next_token());
        else {
            installProcedureBody(mod, mbprocid, s);
        }
         
    	return new ExpressionStatement(new StringExpression(new StringLiteral("Procedure " + n.nameAsString() + " installed.")));
	}

	private static Expression generateExternalJavaCall(Expression e1, String name, Expression param[]) {
		Name tmpVarName = new Name("externalCallVar+$%#$%+#$^+#$+^+#$%^");
		
		return
			new DotExpression(
				new GroupAsExpression(
					new ExternalProcedureCallExpression(
						new ExternalNameExpression(new Name("load_class")),
						new StringExpression(new StringLiteral("odra.sbql.external.lib.StringLib"))
					),
					tmpVarName
				),
//				new SequentialExpression(
					new ExternalProcedureCallExpression(new ExternalNameExpression(new Name("new_object")), new NameExpression(tmpVarName))/*,
					new SequentialExpression(
						new ExternalProcedureCallExpression(new ExternalNameExpression(new Name("init_parameters")), new NameExpression(tmpVarName)),
						new SequentialExpression(
							new ExternalProcedureCallExpression(
								new ExternalNameExpression(new Name("add_parameters")),
								new CommaExpression(new NameExpression(tmpVarName), e1)
							),
							new ExternalProcedureCallExpression(
								new ExternalNameExpression(new Name("invoke_string")),
								new CommaExpression(new NameExpression(tmpVarName), new StringExpression(new StringLiteral(name)))
							)
						)
					)
				) */ 
		  );
	}
	
	static private void installProcedureBody(DBModule mod, OID mbprocid, Statement ast)
		throws DatabaseException, Exception{
	    	ModuleOrganizer organizer = new ModuleOrganizer(mod, true);
	    	MBProcedure proc = new MBProcedure(mbprocid);
	    	OdraProcedureSchema procInfo = new OdraProcedureSchema(proc.getName(), new ProcArgument[0], new ProcedureAST(BuilderUtils.serializeAST(ast)), null);
		ProcedureLocalEnvironmentConstructor constructor = new ProcedureLocalEnvironmentConstructor(organizer.getModule(), procInfo);
		constructor.constructProcedureLocalMetadata(ast);
		organizer.alterProcedureBody(mbprocid, procInfo);
	}
	
}
