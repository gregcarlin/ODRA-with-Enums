/**
 * 
 */
package tests.sbql.ast.serializer;

import static org.junit.Assert.*;

import odra.sbql.ast.AST2TextDeclarationPrinter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.serializer.declarations.DeclarationDeserializer;
import odra.sbql.ast.serializer.declarations.DeclarationSerializer;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.parser.SBQLParser;

import org.junit.Before;
import org.junit.Test;

/**
 * DeclarationSerializationTest
 * @author Radek Adamus
 *@since 2008-04-26
 *last modified: 2008-04-26
 *@version 1.0
 */
public class DeclarationSerializationTest {

    //static String DECL = "module test { i:iteger; class PersonCls {instance Person: {name:string; age:itneger;} getName():string {return name;}} Person:record {a:PersonCls[0..1]; b:string;} [0..*];}"; 
    //static String DECL = "module test { type mydef is record {a:integer; b:string;} }";
    static String DECL = "module test { import admin; import admin as a; implement inter; interface inter { objects iobj; x : integer; y() : integer;} type mydef is record {a:integer; b:string;} session i:integer; view MyViewDef {virtual MyView: record {a:integer; b:string;}; seed : record {a:integer;} {return Person as a;} on_retrieve {return a;} } class PersonCls extends c1, c2 {instance Person: {name:string; age:itneger reverse dd;} getName():string {return name;}} Person:record {a:PersonCls[0..1]; b:string;} [0..*]; }";
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	
    }
    
    @Test
    public void test() throws Exception {
	ASTNode node = BuilderUtils.parseSBQL(DECL);
	DeclarationSerializer ser = new DeclarationSerializer();
	byte [] serast = ser.writeDeclarationAST(node, true);
	DeclarationDeserializer deser = new DeclarationDeserializer();
	ASTNode resnode = deser.readDeclarationAST(serast);
	System.out.print(AST2TextDeclarationPrinter.AST2Text(resnode));
	
    }

}
