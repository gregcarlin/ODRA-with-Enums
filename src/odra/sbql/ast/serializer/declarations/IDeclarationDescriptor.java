/**
 * 
 */
package odra.sbql.ast.serializer.declarations;

/**
 * IDeclarationDescriptor
 * 
 * @author Radek Adamus
 * @since 2008-04-25 last modified: 2008-04-25
 * @version 1.0
 */
public interface IDeclarationDescriptor {
    public static final byte CLASS_DECL = 1;

    public static final byte CLASS_INST_DECL = 2;

    public static final byte LINK_DECL = 3;

    public static final byte IFACE_DECL = 4;

    public static final byte VARIABLE_FLD_DECL = 5;

    public static final byte PROCEDURE_FLD_DECL = 6;

    public static final byte CLASS_FLD_DECL = 7;

    public static final byte IFACE_FLD_DECL = 8;

    public static final byte LINK_FLD_DECL = 9;

    public static final byte METHOD_FLD_DECL = 10;

    public static final byte VIEW_FLD_DECL = 11;

    public static final byte TYPEDEF_FLD_DECL = 12;
    public static final byte IMPORT_DECL = 13;
    public static final byte MODULE_DECL = 14;
    public static final byte VIEW_DECL = 15;
    public static final byte PROCEDURE_DECL = 16;
    public static final byte METHOD_DECL = 17;

    public static final byte NAMED_TYPE_DECL = 18;

    public static final byte RECORD_TYPE_DECL = 19;

    public static final byte TYPEDEF_DECL = 20;

    public static final byte VARIABLE_DECL = 21;

    public static final byte RECORD_DECL = 22;

    public static final byte SESSION_VARIABLE_FLD_DECL = 23;

    public static final byte PROC_HEADER_DECL = 24;

    public static final byte PROC_HEADER_FLD_DECL = 25;

    public static final byte REV_VARIABLE_DECL = 26;
}
