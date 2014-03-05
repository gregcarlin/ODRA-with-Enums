package odra.sbql.ast.serializer;

import static odra.sbql.ast.serializer.SerializationUtil.deserializePositionInfo;
import static odra.sbql.ast.serializer.SerializationUtil.deserializeString;

import java.nio.ByteBuffer;
import java.util.Date;

import odra.sbql.ast.ASTNode;
import odra.sbql.ast.declarations.TypeDeclaration;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AtLeastExpression;
import odra.sbql.ast.expressions.AtMostExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CastExpression;
import odra.sbql.ast.expressions.CloseByExpression;
import odra.sbql.ast.expressions.CloseUniqueByExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.CreateLocalExpression;
import odra.sbql.ast.expressions.CreatePermanentExpression;
import odra.sbql.ast.expressions.CreateTemporalExpression;
import odra.sbql.ast.expressions.DateExpression;
import odra.sbql.ast.expressions.DateprecissionExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DeserializeOidExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ExternalNameExpression;
import odra.sbql.ast.expressions.ExternalProcedureCallExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.InsertCopyExpression;
import odra.sbql.ast.expressions.InsertExpression;
import odra.sbql.ast.expressions.InstanceOfExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.LeavesByExpression;
import odra.sbql.ast.expressions.LeavesUniqueByExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ParallelExpression;
import odra.sbql.ast.expressions.ParallelUnionExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RandomExpression;
import odra.sbql.ast.expressions.RangeAsExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.RemoteQueryExpression;
import odra.sbql.ast.expressions.RenameExpression;
import odra.sbql.ast.expressions.SequentialExpression;
import odra.sbql.ast.expressions.SerializeOidExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.StructExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBagExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.ToDateExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.ToRealExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.ToStringExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.serializer.declarations.DeclarationDeserializer;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.BreakStatement;
import odra.sbql.ast.statements.CatchBlock;
import odra.sbql.ast.statements.ContinueStatement;
import odra.sbql.ast.statements.DoWhileStatement;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ForEachStatement;
import odra.sbql.ast.statements.ForStatement;
import odra.sbql.ast.statements.IfElseStatement;
import odra.sbql.ast.statements.IfStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.ReturnWithoutValueStatement;
import odra.sbql.ast.statements.SequentialCatchBlock;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.SingleCatchBlock;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.TransactionAbortStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.DateLiteral;
import odra.sbql.ast.terminals.IntegerLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.ast.terminals.RealLiteral;
import odra.sbql.ast.terminals.StringLiteral;
import odra.sbql.debugger.compiletime.DebugNodeData;
import odra.sbql.stack.BindingInfo;
import odra.sbql.typechecker.EnvironmentInfo;

/**
 * ASTDeserializer
 * @author Radek Adamus
 *last modified: 2007-02-02
 *@version 1.0
 */
public class ASTDeserializer {
	boolean withPositionInfo;

	ByteBuffer serast;
	
	
	public ASTNode readAST(byte[] bast){
		serast = ByteBuffer.wrap(bast);
		this.withPositionInfo = serast.get() == 1 ? true : false;
		return read();
	}
	
	private ASTNode read(){
		ASTNode node = null;
		
		EnvironmentInfo ei;
		BindingInfo bi;
		ParallelExpression puExpr;
		
		byte type = serast.get();
		
		switch(type) {
		case IASTDescriptor.AS_EXPRESSION:
			String name = deserializeString(serast);
			node = new AsExpression((Expression)read(), new Name(name));	
			break;
		case IASTDescriptor.ASSIGN_EXPRESSION:
			node = new AssignExpression((Expression)read(),(Expression)read(), Operator.opAssign);
			break;
		case IASTDescriptor.ATMOST_EXPRESSION:
		    	int maxCardinality = serast.getInt();
			node = new AtMostExpression((Expression)read(), maxCardinality);
			break;
		case IASTDescriptor.ATLEAST_EXPRESSION:
		    	int minCardinality = serast.getInt();
			node = new AtLeastExpression((Expression)read(), minCardinality);
			break;
		case IASTDescriptor.AVG_EXPRESSION:
			node = new AvgExpression((Expression)read());
			break;
		case IASTDescriptor.BAG_EXPRESSION:
			node = new BagExpression((Expression)read());
			break;
		case IASTDescriptor.BLOCK_STATEMENT:
			node = new BlockStatement((Statement)read());
			break;
		case IASTDescriptor.BOOLEAN_EXPRESSION: 
			boolean bvalue = serast.get() == 1 ? true : false;
			node = new BooleanExpression(new BooleanLiteral(bvalue));
			break;
		case IASTDescriptor.COMMA_EXPRESSION:
			node = new CommaExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.CAST_EXPRESSION:
//			name = deserializeString(serast);
			node = new CastExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.CLOSE_BY_EXPRESSION:
			node = new CloseByExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.CLOSE_UNIQUE_BY_EXPRESSION:
			node = new CloseUniqueByExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.COUNT_EXPRESSION:
			node = new CountExpression((Expression)read());
			break;
		case IASTDescriptor.CREATE_EXPRESSION:
			node = new CreateExpression(new Name(deserializeString(serast)),(Expression)read());
			break;
		case IASTDescriptor.CREATE_PERMANENT_EXPRESSION:
			node = new CreatePermanentExpression(new Name(deserializeString(serast)),(Expression)read());
			break;
		case IASTDescriptor.CREATE_TEMPORAL_EXPRESSION:
			node = new CreateTemporalExpression(new Name(deserializeString(serast)),(Expression)read());
			break;
		case IASTDescriptor.CREATE_LOCAL_EXPRESSION:
			node = new CreateLocalExpression(new Name(deserializeString(serast)),(Expression)read());
			break;
		case IASTDescriptor.DELETE_EXPRESSION:
			node = new DeleteExpression((Expression)read());
			break;
		case IASTDescriptor.DEREF_EXPRESSION:
			node = new DerefExpression((Expression)read());
			break;
		case IASTDescriptor.DOT_EXPRESSION:
			ei = readEnvironmentInfo(serast);
			node = new DotExpression((Expression)read(), (Expression)read());
			((NonAlgebraicExpression) node).setEnvsInfo(ei);
			break;
		case IASTDescriptor.EMPTY_EXPRESSION:
			node = new EmptyExpression();
			break;
		case IASTDescriptor.EMPTY_STATEMENT:
			node = new EmptyStatement();
			break;
		case IASTDescriptor.EQUALITY_EXPRESSION:
			Operator oper = Operator.get(serast.get());
			assert oper != null : "unknown operator";
			node = new EqualityExpression((Expression)read(), (Expression)read(), oper);
			break;
		case IASTDescriptor.EXECSQL_EXPRESSION:
			node = new ExecSqlExpression((Expression)read(), (StringExpression)read(), (StringExpression)read());
			break;
		case IASTDescriptor.EXISTS_EXPRESSION:
			node = new ExistsExpression((Expression)read());
			break;
		case IASTDescriptor.EXPRESSION_STATEMENT:
			node = new ExpressionStatement((Expression)read());
			break;
		case IASTDescriptor.FORALL_EXPRESSION:
			node = new ForAllExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.FORSOME_EXPRESSION:
			node = new ForSomeExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.FOREACH_STATEMENT:
			node = new ForEachStatement((Expression)read(), (Statement)read());
			break;
		case IASTDescriptor.GROUPAS_EXPRESSION:
			name = deserializeString(serast);
			node = new GroupAsExpression((Expression)read(), new Name(name));
			break;
		case IASTDescriptor.IF_THEN_ELSE_EXPRESSION:
			node = new IfThenElseExpression((Expression)read(), (Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.IF_THEN_EXPRESSION:
			node = new IfThenExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.IFELSE_STATEMENT:
			node = new IfElseStatement((Expression)read(), (Statement)read(),  (Statement)read());
			break;
		case IASTDescriptor.IF_STATEMENT:
			node = new IfStatement((Expression)read(), (Statement)read());
			break;
		case IASTDescriptor.IN_EXPRESSION:

			node = new InExpression((Expression)read(), (Expression)read());

			break;
		case IASTDescriptor.INSERT_EXPRESSION:
			node = new InsertExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.INSERT_COPY_EXPRESSION:
		    name = deserializeString(serast);
			node = new InsertCopyExpression((Expression)read(), (Expression)read(), new Name(name));
			break;
		case IASTDescriptor.INSTANCEOF_EXPRESSION:
			node = new InstanceOfExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.INTEGER_EXPRESSION:			
			node = new IntegerExpression(new IntegerLiteral(serast.getInt()));
			break;
		case IASTDescriptor.INTERSECT_EXPRESSION:
			node = new IntersectExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.JOIN_EXPRESSION:
			ei = readEnvironmentInfo(serast);
			node = new JoinExpression((Expression)read(), (Expression)read());
			((NonAlgebraicExpression) node ).setEnvsInfo(ei);
			break;
		case IASTDescriptor.LEAVES_BY_EXPRESSION:
			node = new LeavesByExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.LEAVES_UNIQUE_BY_EXPRESSION:
			node = new LeavesUniqueByExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.MAX_EXPRESSION:
			node = new MaxExpression((Expression)read());
			break;
		case IASTDescriptor.MIN_EXPRESSION:
			node = new MinExpression((Expression)read());
			break;
		case IASTDescriptor.MINUS_EXPRESSION:
			node = new MinusExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.NAME_EXPRESSION:
			name = deserializeString(serast);
			bi = readBindingInfo(serast);
			node = new NameExpression(new Name(name));
			((NameExpression) node).setBindingInfo(bi) ;
			break;
		case IASTDescriptor.EXTERNAL_NAME_EXPRESSION: //TW
			name = deserializeString(serast);
			node = new ExternalNameExpression(new Name(name));
			break;			
		case IASTDescriptor.ORDERBY_EXPRESSION:
			ei = readEnvironmentInfo(serast);
			node = new OrderByExpression((Expression)read(), (Expression)read());
			((NonAlgebraicExpression) node ).setEnvsInfo(ei);
			break;
		case IASTDescriptor.PROCCALL_EXPRESSION:
			node = new ProcedureCallExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.EXTERNAL_PROCCALL_EXPRESSION: //TW
			node = new ExternalProcedureCallExpression((Expression)read(), (Expression)read());
			break;			
		case IASTDescriptor.RANGE_EXPRESSION:
			node = new RangeExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.REAL_EXPRESSION:			
			node = new RealExpression(new RealLiteral(serast.getDouble()));
			break;
		case IASTDescriptor.REF_EXPRESSION:
			node = new RefExpression((Expression)read());
			break;
		case IASTDescriptor.REMOTE_QUERY_EXPRESSION:
			node = new RemoteQueryExpression((Expression)read());
			break;
		case IASTDescriptor.RETURN_WITHOUT_VALUE_STATEMENT:
			node = new ReturnWithoutValueStatement();
			break;
		case IASTDescriptor.RETURN_WITH_VALUE_STATEMENT:
			node = new ReturnWithValueStatement((Expression)read());
			break;
		case IASTDescriptor.SEQUENTIAL_STATEMENT:
		    	int stmtsNo = this.serast.getInt();
		    	Statement second = (Statement)read();
		    	Statement first = (Statement)read();
		    	node = new SequentialStatement(first, second);
		    	node.line = first.line;
		    	node.column = first.column;
		    	for(int i = 2; i < stmtsNo; i++){
		    		first = (Statement)read();
		    	    node = new SequentialStatement(first, (Statement)node);
		    	    node.line = first.line;
			    	node.column = first.column;
		    	}			
			break;
		case IASTDescriptor.SEQUENTIAL_EXPRESSION:
			node = new SequentialExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.SIMPLE_BINARY_EXPRESSION:
			oper = Operator.get(serast.get());
			assert oper != null : "unknown operator";
			node = new SimpleBinaryExpression((Expression)read(), (Expression)read(), oper);
			break;
		case IASTDescriptor.SIMPLE_UNARY_EXPRESSION:
			oper = Operator.get(serast.get());
			assert oper != null : "unknown operator";
			node = new SimpleUnaryExpression((Expression)read(), oper);
			break;
		case IASTDescriptor.STRING_EXPRESSION:
			node = new StringExpression(new StringLiteral(deserializeString(serast)));
			break;
		case IASTDescriptor.STRUCT_EXPRESSION:
			node = new StructExpression((Expression)read());
			break;
		case IASTDescriptor.SUM_EXPRESSION:
			node = new SumExpression((Expression)read());
			break;
		case IASTDescriptor.TO_BAG_EXPRESSION:

			node = new ToBagExpression((Expression)read());
			break;
		case IASTDescriptor.TO_BOOLEAN_EXPRESSION:
			node = new ToBooleanExpression((Expression)read());
			break;
		case IASTDescriptor.TO_INTEGER_EXPRESSION:
			node = new ToIntegerExpression((Expression)read());
			break;
		case IASTDescriptor.TO_REAL_EXPRESSION:
			node = new ToRealExpression((Expression)read());
			break;
		case IASTDescriptor.TO_SINGLE_EXPRESSION:
			node = new ToSingleExpression((Expression)read());
			break;
		case IASTDescriptor.TO_STRING_EXPRESSION:
			node = new ToStringExpression((Expression)read());
			break;
		case IASTDescriptor.UNION_EXPRESSION:
			node = new UnionExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.UNIQUE_EXPRESSION:
			boolean isuniqueref = serast.get() == 1 ? true : false;
			node = new UniqueExpression((Expression)read(), isuniqueref);
			break;
		case IASTDescriptor.VARIABLE_DECLARATION_STATEMENT:
			String vname = deserializeString(serast);
			
			TypeDeclaration typed = deserializeType();
//			String tname = deserializeString(serast);
			node = new VariableDeclarationStatement(vname, typed, serast.getInt(), serast.getInt(), serast.getInt(), (Expression)read());
			break;
		case IASTDescriptor.WHERE_EXPRESSION:
			ei = readEnvironmentInfo(serast);
			node = new WhereExpression((Expression)read(), (Expression)read());
			((NonAlgebraicExpression)node).setEnvsInfo(ei);
			break;
		case IASTDescriptor.WHILE_STATEMENT:
			node = new WhileStatement((Expression)read(), (Statement)read());
			break;
		case IASTDescriptor.DO_WHILE_STATEMENT:
			node = new DoWhileStatement((Statement)read(), (Expression)read());
			break;
		case IASTDescriptor.FOR_STATEMENT:
			node = new ForStatement( (Expression)read(), (Expression)read(),(Expression)read(),(Statement)read());
			break;
		case IASTDescriptor.BREAK_STATEMENT:
			node = new BreakStatement();
			break;
		case IASTDescriptor.CONTINUE_STATEMENT:
			node = new ContinueStatement();
			break;
		case IASTDescriptor.DATE_EXPRESSION:
			node = new DateExpression(new DateLiteral(new Date(serast.getLong())));
			break;
		case IASTDescriptor.DATEPREC_EXPRESSION:
			node = new DateprecissionExpression((Expression)read(), (StringExpression)read());
			break;
		case IASTDescriptor.TO_DATE_EXPRESSION:
			node = new ToDateExpression((Expression)read());
			break;
		case IASTDescriptor.RANDOM_EXPRESSION:
			node = new RandomExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.TRY_CATCH_FINALLY_STATEMENT:
		    BlockStatement tryS = (BlockStatement)read();
		    SingleCatchBlock[] blocks = new SingleCatchBlock[this.serast.getInt()];
		    assert blocks.length > 0 : "catch " + blocks.length;
		    for(int i = 0; i < blocks.length; i++){
			String blockName = deserializeString(this.serast);
			String excName = deserializeString(this.serast);
			String excType = deserializeString(this.serast);
			blocks[i] = new SingleCatchBlock(excName, excType, (BlockStatement)read());
			blocks[i].setCatchBlockName(blockName);
		    }
		    CatchBlock sblock;
		    if(blocks.length > 1){
			sblock = new SequentialCatchBlock(blocks[0], blocks[1]);
			for(int i = 2; i < blocks.length; i++){
			    sblock = new SequentialCatchBlock(sblock, blocks[i]);
			}
		    }else 
			sblock = blocks[0];
		    Statement finallyS = (Statement)read();
		    node = new TryCatchFinallyStatement(tryS, sblock, finallyS);
		    break;
		case IASTDescriptor.THROW_STATEMENT:
		    node = new ThrowStatement((Expression)read());
		    break;
		case IASTDescriptor.PARALLEL_UNION_EXPRESSION:
			puExpr = new ParallelUnionExpression();			
			node = readParallelData(puExpr);
			break;
		case IASTDescriptor.ABORT_STATEMENT:
		    node = new TransactionAbortStatement();
		    break;
		case IASTDescriptor.RANGE_AS_EXPRESSION:
			name = deserializeString(serast);
			node = new RangeAsExpression((Expression)read(), new Name(name));
		   	break;
		case IASTDescriptor.SERIALIZE_OID_EXPRESSION:
				node = new SerializeOidExpression((Expression)read());
			break;
		case IASTDescriptor.DESERIALIZE_OID_EXPRESSION:
			node = new DeserializeOidExpression((Expression)read(), (Expression)read());
			break;
		case IASTDescriptor.RENAME_EXPRESSION:
			name = deserializeString(serast);
			node = new RenameExpression((Expression)read(), new Name(name));
			break;
		default:
			assert false : "unknown AST node " + type;
			break;
		}
		if(node instanceof Expression){		    
		    String [] links = this.readLinksDecoration(serast);
		    node.addLinkDecoration(links);
		}
		if(withPositionInfo)
			deserializePositionInfo(node, serast);
	
		return node;
	}
	
	/**
	 * @return
	 */
	private TypeDeclaration deserializeType() {
	    int length = serast.getInt();
	    byte[] stype = new byte[length];
	    serast.get(stype);
	    DeclarationDeserializer deser = new DeclarationDeserializer();
	    return (TypeDeclaration)deser.readDeclarationAST(stype);
	}

	private ASTNode readParallelData(ParallelExpression expr)
	{
		int count = serast.getInt();
		for(int i = 0; i < count; i++)
			expr.addExpression((Expression) read()); 
		
		return expr;
	}

	
	
	
	
	private String[] readLinksDecoration(ByteBuffer buf)
	{
		int count = buf.getInt();
						
		String []links = new String[count];
		
		for (int i = 0; i < count; i++)
		{
			int len = buf.getInt();
			byte[] bytea = new byte[len];
			buf.get(bytea);
			links[i] = new String(bytea);			
		}
		
		return links;
	}
	
	private BindingInfo readBindingInfo(ByteBuffer buf)
	{
		BindingInfo bi = new  BindingInfo();
		bi.boundat = buf.getInt();
		
		if ( bi.boundat == -1 )
			return null;
		
		return bi;
	}
	
	private EnvironmentInfo readEnvironmentInfo(ByteBuffer buf)
	{
		EnvironmentInfo ei = new EnvironmentInfo();
		
		ei.baseEnvsSize = buf.getInt();
		ei.framesOpened = buf.getInt();
		
		if ( ei.baseEnvsSize == -1)
			return null;
		
		return ei;
	}
}
