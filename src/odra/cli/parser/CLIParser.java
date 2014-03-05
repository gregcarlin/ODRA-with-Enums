package odra.cli.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import javax.swing.event.CaretListener;

import odra.cli.CLISyntaxErrorException;
import odra.cli.ast.Command;
import odra.cli.ast.ConditionalCommand;
import odra.cli.ast.ConnectCommand;
import odra.cli.ast.DatabaseURL;
import odra.cli.ast.SimpleCommand;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.optimizers.Type;
import odra.wrapper.Wrapper;

/**
 * This class represents a simple parser used by jodra's cli tool.
 * It is used only during parsing of cli commands. SBQL has its
 * own lexer/parser generated using jflex/cup.
 *
 * @author raist
 */

public class CLIParser {
    private CLILexer lexer;
    private CLIToken token;

    public CLIParser(CLILexer lexer) {
        this.lexer = lexer;
    }

    public final Command parseCommand() throws CLISyntaxErrorException {
        token = lexer.nextToken();
        Command cmd = parseSimpleCommand();

        if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.SBQL_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.ADD_INDEX_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.ADD_TMPINDEX_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.ADD_LINK_TO_SCHEMA_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.REMOVE_LINK_FROM_SCHEMA_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.ADD_TMPINDEX_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.REFRESH_LINK_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.EXPLAIN_OPTIMIZATION_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.EXPLAIN_JULIET_CODE_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.EXPLAIN_TYPECHECKER_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.EXPLAIN_PROCEDURE_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.ADD_MODULE_AS_WRAPPER_CMD)
            return cmd;
        else if (cmd instanceof SimpleCommand && ((SimpleCommand) cmd).cmdid == SimpleCommand.META_CMD)
            return cmd;

        else if (token.kind != CLIToken.EOF)
            throw new CLISyntaxErrorException("Unexpected token '" + token.value + "'");
        else
            return cmd;
    }

    private final Command parseSimpleCommand() throws CLISyntaxErrorException {
         Command cmd;
         switch (token.kind) {
         case CLIToken.ALTER:
        	 accept();
        	 cmd = parseAlterElement();
             return cmd;
         case CLIToken.AC:
			 accept();
			 cmd = parseACElement();
			 return cmd;
         case CLIToken.AT:
             lexer.rewind(token.value.length());
             return new SimpleCommand(SimpleCommand.META_CMD, new String[] {lexer.getRemainingSource()});

             case CLIToken.SET:
                 accept();
                 String vn = parseName();
                 if(vn.equals(OptimizationSequence.OPTIMIZATION) || vn.equals(OptimizationSequence.REFOPTIMIZATION))
                 {
                     Vector<String> oparams = new Vector<String>();
                     oparams.addElement(Boolean.toString(vn.equals(OptimizationSequence.REFOPTIMIZATION)));
                     if(token.kind == CLIToken.STRING_LITERAL)
                     {
                         String name = parseString();
                         OptimizationSequence sequence = OptimizationSequence.getForName(name);
                         if(sequence == null)
                             throw new CLISyntaxErrorException("Unknown predefined optimization sequence name: " + name + ".");

                         oparams.add("none");
                         for(Type type : sequence)
                             oparams.add(type.getTypeName());
                     }
                     else
                     {
                         oparams.addElement(parseName());
                         while(token.kind == CLIToken.BAR)
                         {
                             accept();
                             oparams.add(parseName());
                         }
                     }

                     return new SimpleCommand(SimpleCommand.SET_OPTIMIZATION_CMD, oparams.toArray(new String[0]));
                 }

                 String vv = parseArbitrary();

                 return new SimpleCommand(SimpleCommand.SET_CMD, new String[] { vn, vv } );

             case CLIToken.SHOW:
                 accept();
                 vn = parseName();
                 return new SimpleCommand(SimpleCommand.SHOW_CMD, new String[] { vn, Boolean.toString(true) } );

             case CLIToken.ADD:
                 accept();
                 cmd = parseAddElement();
                 return cmd;

             case CLIToken.PROMOTE:
                 accept();
                 cmd = parsePromoteElement();
                 return cmd;

             case CLIToken.REMOVE:
                 accept();
                 cmd = parseRemoveElement();
                 return cmd;

             case CLIToken.SUSPEND:
                 accept();
                 cmd = parseSuspend();
                 return cmd;

             case CLIToken.RESUME:
                 accept();
                 cmd = parseResume();
                 return cmd;

             case CLIToken.COMPILE:
                 accept();
                 String name = parseName();
                 return new SimpleCommand(SimpleCommand.COMPILE_CMD, new String[] { name });

             case CLIToken.LS:
                 accept();
                 return new SimpleCommand(SimpleCommand.LS_CMD);

             case CLIToken.DUMP:
                 accept();
                 cmd = parseDumpElement();
                 return cmd;

             case CLIToken.DISASSEMBLE:
                 accept();
                 name = parseName();
                 return new SimpleCommand(SimpleCommand.DISASSEMBLE_CMD, new String[] { name });

             case CLIToken.CM:
                 accept();

                 if (token.kind == CLIToken.DOT) {
                     accept(CLIToken.DOT);
                     accept(CLIToken.DOT);
                     return new SimpleCommand(SimpleCommand.CMDOTDOT_CMD);
                 }
                 name = parseName();
                 return new SimpleCommand(SimpleCommand.CM_CMD, new String[] { name });


             case CLIToken.NAVIGATOR:
                 accept();
                 return new SimpleCommand(SimpleCommand.NAVIGATOR_CMD);

             case CLIToken.INQUIRER:
                 accept();
                 return new SimpleCommand(SimpleCommand.INQUIRER_CMD);

             case CLIToken.CONNECT:
                 accept();
                 return new ConnectCommand(parseDatabaseURL());

             case CLIToken.HELP:
                 accept();
                 return new SimpleCommand(SimpleCommand.HELP_CMD);

             case CLIToken.DISCONNECT:
                 accept();
                 return new SimpleCommand(SimpleCommand.DISCONNECT_CMD);

             case CLIToken.PWM:
                 accept();
                 return new SimpleCommand(SimpleCommand.PWM_CMD);

             case CLIToken.QUIT:
                 accept();
                 return new SimpleCommand(SimpleCommand.QUIT_CMD);

             case CLIToken.LOAD:
                 accept();
                 String path = parseString();
                 accept(CLIToken.USING);
                 String filter = parseName();
                 String params = "";

                 if (token.kind == CLIToken.LPAREN) {
                     accept();
                     params = parseString();
                     accept(CLIToken.RPAREN);
                 }

                 return new SimpleCommand(SimpleCommand.LOAD_CMD, new String[] { path, filter, params });

             case CLIToken.EXPLAIN:
                 accept();
                 if(token.kind == CLIToken.JULIETCODE)
                 {
                     accept(CLIToken.JULIETCODE);
                     return new SimpleCommand(SimpleCommand.EXPLAIN_JULIET_CODE_CMD, new String[] {lexer.getRemainingSource()});
                 }
                 else if(token.kind == CLIToken.OPTIMIZATION)
                 {
                     accept(CLIToken.OPTIMIZATION);
                     Vector<String> oparams = new Vector<String>();
                         oparams.add(parseName());
                     while (token.kind == CLIToken.BAR) {
                         accept();
                         oparams.add(parseName());
                     }
                     oparams.add(lexer.getRemainingSource());
                     return new SimpleCommand(SimpleCommand.EXPLAIN_OPTIMIZATION_CMD, oparams.toArray(new String[oparams.size()]));
                 }
                 else if(token.kind == CLIToken.TYPECHECKER)
                 {
                     accept(CLIToken.TYPECHECKER);
                     return new SimpleCommand(SimpleCommand.EXPLAIN_TYPECHECKER_CMD, new String[] {lexer.getRemainingSource()});
                 }
                 else if(token.kind == CLIToken.PROCEDURE){
                     accept(CLIToken.PROCEDURE);
                     if(token.kind == CLIToken.COLON)
                     return new SimpleCommand(SimpleCommand.EXPLAIN_PROCEDURE_CMD, new String[] {"sbql", lexer.getRemainingSource()});

                String type = this.parseName();
                return new SimpleCommand(SimpleCommand.EXPLAIN_PROCEDURE_CMD, new String[] {type, lexer.getRemainingSource()});
                 }
                 else
                     throw new CLISyntaxErrorException("'julietcode', 'optimization' or 'typechecker' expected, '" + token.value + "' received instead...");
             case CLIToken.BATCH:
                 accept();
                 params = parseArbitrary();
                 return new SimpleCommand(SimpleCommand.BATCH_CMD, new String[] {params});
             case CLIToken.CD:
                 accept();
                 params = parseArbitrary();
                 return new SimpleCommand(SimpleCommand.CD_CMD, new String[] {params});
             case CLIToken.PWD:
                 accept();
                 return new SimpleCommand(SimpleCommand.PWD_CMD);

             case CLIToken.MEMMONITOR:
                 accept();
                 if(token.kind == CLIToken.EOF)
                     return new SimpleCommand(SimpleCommand.MEMMONITOR_CMD, new String[] {});

                 String user = parseName();
                 accept(CLIToken.SLASH);
                 String password = parseName();
                 accept(CLIToken.AT);
                 String server = parseServerName();
                 accept(CLIToken.COLON);
                 int port = parseInteger();

                 return new SimpleCommand(SimpleCommand.MEMMONITOR_CMD, new String[] {user, password, server, Integer.toString(port)});

             case CLIToken.BENCHMARK:
                 accept();
                 
                 boolean verbose = token.kind != CLIToken.QUIET;
                 if (!verbose) 
                	 accept();
                 
                 int repeat = parseInteger();
                 if(token.kind != CLIToken.EOF)
                     lexer.rewind(token.value.length() + 1);
                 String query = lexer.getRemainingSource();
                 parseArbitrary();
                 if(query.trim().length() == 0 || query.equals(CLIToken.EOF))
                     throw new CLISyntaxErrorException("'benchmark' command requres a query string!");

                 return new SimpleCommand(SimpleCommand.BENCHMARK_CMD, new String[] {query, Integer.toString(repeat), Boolean.toString(verbose)});

             case CLIToken.REFRESH:
                 accept();
                 accept(CLIToken.METALINK);

                 String lnkname = parseName();

                 return new SimpleCommand(SimpleCommand.REFRESH_LINK_CMD, new String[] { lnkname });

             case CLIToken.AST_VISUALIZER:
                 accept();
                 return new SimpleCommand(SimpleCommand.AST_VISUALIZER_CMD);

             case CLIToken.WHATIS:
                 accept();
                 name = parseName();
                 return new SimpleCommand(SimpleCommand.WHATIS_CMD, new String[] {name});

             case CLIToken.NOT:
                 accept();
                 accept(CLIToken.EXISTS);
                 return new SimpleCommand(SimpleCommand.EXISTS_CMD, parseExists(true, false));

             case CLIToken.EXISTS:
                 accept();
                 return new SimpleCommand(SimpleCommand.EXISTS_CMD, parseExists(true, true));

             case CLIToken.IF:
                 accept();
                 boolean positive = true;
                 if(token.kind == CLIToken.NOT)
                 {
                     accept();
                     positive = false;
                 }
                 accept(CLIToken.EXISTS);

                 SimpleCommand condition = new SimpleCommand(SimpleCommand.EXISTS_CMD, parseExists(false, positive));

                 accept(CLIToken.THEN);
                 lexer.rewind(token.value.length() + 1);

                 return
                     new ConditionalCommand(
                         condition,
                         parseCommand());

             case CLIToken.ECHO:
                 accept();
                 if(token.value.length() > 0)
                     lexer.rewind(token.value.length()+1);
                 String message = lexer.getRemainingSource().trim();
                 token.kind = CLIToken.EOF;
                 return new SimpleCommand(SimpleCommand.ECHO_CMD, new String[] {message});

             case CLIToken.OPTIMIZATION:
                 accept();
                 return new SimpleCommand(SimpleCommand.OPTIMIZATION_CMD);

             case CLIToken.JOINTOGRID:
                 accept();
                  name = parseServerName();
                 return new SimpleCommand(SimpleCommand.JOINTOGRID_CMD, new String[] { name });

             case CLIToken.REMOVEFROMGRID:
                 accept();
                 name = parseServerName();
                 return new SimpleCommand(SimpleCommand.REMOVEFROMGRID_CMD, new String[] { name });

             case CLIToken.CONNECTTOGRID:
                 accept();
                 String jxtagroupname = parseName();
                 accept(CLIToken.DOT);
                 String peername = parseName();
                 accept(CLIToken.SLASH);
                 String passwd = parseName();
                 accept(CLIToken.AT);
                 String cmuuri = parseURI();
                 return new SimpleCommand(SimpleCommand.CONNECTTOGRID_CMD, new String[] { jxtagroupname, peername, passwd, cmuuri });

             case CLIToken.DEFRAGMENT:
            	 accept();
            	 return new SimpleCommand(SimpleCommand.DEFRAGMENT_CMD);
            	 
            default:
                return new SimpleCommand(SimpleCommand.SBQL_CMD, new String[] { lexer.getSource() });
         }
    }


    /**
	 * @return
     * @throws CLISyntaxErrorException
	 */
	private Command parseAlterElement() throws CLISyntaxErrorException {
		switch(token.kind){
		 case CLIToken.PROCEDURE:
             accept();
             String pname = parseCompoundName();
             accept(CLIToken.SET);

             if(this.token.kind == CLIToken.BODY){
                 acceptRemaining();
                 String psrc = this.token.value;
                 return new SimpleCommand(SimpleCommand.ALTER_PROCEDURE_BODY_CMD, new String[] { pname, psrc });
             }else //rise standard error
            	 accept(CLIToken.BODY);

//             String psrc = lexer.getRemainingSource();

		}
		return null;
	}

	/**
	 * @return
	 */
	private void acceptRemaining() {
		this.token = new CLIToken(CLIToken.EOF, lexer.getRemainingSource());
	}

	/**
     * Parses CLI input for checking existence of some schema object (module, user, view, etc.).
     *
     * @param echoResult echo result to output?
     * @param positive perform positive test?
     * @throws CLISyntaxErrorException
     *
     * @author jacenty
     */
    private final String[] parseExists(boolean echoResult, boolean positive) throws CLISyntaxErrorException
    {
        String[] result = new String[4];

        result[0] = Boolean.toString(echoResult);
        result[1] = Boolean.toString(positive);

        switch (token.kind)
        {
            case CLIToken.MODULE:
                accept();
                result[2] = "module";
                break;
            case CLIToken.USER:
                accept();
                result[2] = "user";
                break;
            case CLIToken.INDEX:
                result[2] = "index";
                accept();
                break;
            case CLIToken.VIEW:
                accept();
                result[2] = "view";
                break;
            case CLIToken.ENDPOINT:
                accept();
                result[2] = "endpoint";
                break;
            case CLIToken.LINK:
                accept();
                result[2] = "link";
                break;
            case CLIToken.GRIDLINK:
                accept();
                result[2] = "gridlink";
                break;
            default:
                throw new CLISyntaxErrorException(
                    "Expected one of: " +
                    CLIToken.spell(CLIToken.MODULE) + ", " +
                    CLIToken.spell(CLIToken.USER) + ", " +
                    CLIToken.spell(CLIToken.INDEX) + ", " +
                    CLIToken.spell(CLIToken.VIEW) + ", " +
                    CLIToken.spell(CLIToken.ENDPOINT) + ", " +
                    CLIToken.spell(CLIToken.LINK) + ", " +
                    CLIToken.spell(CLIToken.GRIDLINK) + ", " +
                    "but '" + token.value + "' found instead...");
        }

        result[3] = parseName();

        return result;
    }

    /**
     * Parses CLI input for an arbitrary sequence.
     * <br>
     * The method was originally designed for parsing file paths, where some tokens (file names)
     * can be unpredictable CLITokens.
     *
     * @return string
     * @throws CLISyntaxErrorException
     *
     * @author jacenty
     */
    private final String parseArbitrary() throws CLISyntaxErrorException
    {
        String result = "";
        while(true)
        {
            result += token.value;
            accept();
            if(token.kind == CLIToken.EOF)
                break;
        }

        return result;
    }

    private final Command parsePromoteElement() throws CLISyntaxErrorException {
        String proxyName = parseName();
        accept(CLIToken.TO);
        accept(CLIToken.PROXY);
        accept(CLIToken.ON);
        String wsdlUrl = parseString();
        accept(CLIToken.WITH);
        accept(CLIToken.LPAREN);
        accept(CLIToken.PORT_NAME);
        accept(CLIToken.EQ);
        String portName = parseString();
        accept(CLIToken.COMMA);
        accept(CLIToken.SERVICE_NAME);
        accept(CLIToken.EQ);
        String serviceName = parseString();
        accept(CLIToken.RPAREN);
        String[] params = new String[] { proxyName, wsdlUrl, portName, serviceName};
        return new SimpleCommand(SimpleCommand.PROMOTE_TO_PROXY_CMD, params );
    }
    private final Command parseACElement() throws CLISyntaxErrorException {
	switch (token.kind) {
	case CLIToken.ADD:
		accept();
		accept(CLIToken.ROLE);
		String rolename = parseName();
		if (rolename.trim().equals(""))
			throw new CLISyntaxErrorException("illegal role name!");
		return new SimpleCommand(SimpleCommand.ADD_ROLE_CMD, new String[] { rolename });

	case CLIToken.GRANT:
		accept();
		accept(CLIToken.PRIVILEGE);
		accept(CLIToken.TO);
		accept(CLIToken.ROLE);
		String role = parseName();
		int type = -1;
		String object="";

		if(token.kind == CLIToken.TYPE) {
			accept();
			type = parseInteger();

		} else if(token.kind == CLIToken.OBJECT) {
			accept();
			object = parseName();
			accept(CLIToken.TYPE);
			type = parseInteger();

		} else throw new CLISyntaxErrorException("'type' or 'object' expected.");

		accept(CLIToken.MODE);
		String mode = parseACMode();
		accept(CLIToken.VALUE);
		String value = parseACValue();

	//	System.out.println("[[[]]]] "+role + type + object + mode + value);
		return new SimpleCommand(SimpleCommand.GRANT_PRIVILEGE_CMD, new String[] { role, ""+type, object, mode, value });


	default:
		throw new CLISyntaxErrorException("'grant' or 'add' expected.");
	}
    }
    private final String parseACValue() throws CLISyntaxErrorException {
	CLIToken t = token;
	try {
		accept(CLIToken.ALLOW);
	} catch (CLISyntaxErrorException ex) {
		try {
			accept(CLIToken.DENY);
		} catch (CLISyntaxErrorException ex2) {
			throw new CLISyntaxErrorException("'allow' or 'deny' expected, " + CLIToken.spell(token.kind) + " received");
		}

	}

	return t.value;

}

    private final String parseACMode() throws CLISyntaxErrorException {
	CLIToken t = token;
	try {
		accept(CLIToken.CREATE);
	} catch (CLISyntaxErrorException ex) {
		try {
			accept(CLIToken.READ);
		} catch (CLISyntaxErrorException ex2) {
			try {
				accept(CLIToken.UPDATE);
			} catch (CLISyntaxErrorException ex3) {
				try {
					accept(CLIToken.DELETE);
				} catch (CLISyntaxErrorException ex4) {
					throw new CLISyntaxErrorException("'create', 'read', 'update' or 'delete' expected, " + CLIToken.spell(token.kind) + " received");
				}
			}
		}
	}

	return t.value;

}
    private final Command parseAddElement() throws CLISyntaxErrorException {
        switch (token.kind) {
            case CLIToken.USER:
                accept();
                String user = parseName();
                accept(CLIToken.IDENTIFIED);
                accept(CLIToken.BY);
                String password = parseString();

                return new SimpleCommand(SimpleCommand.ADD_USER_CMD, new String[] { user, password });

            case CLIToken.INTERFACE:
                String interfaceToken = token.value;
                accept();
                String interfaceName = parseName();
                CLIToken inext = token;

                lexer.rewind(interfaceToken.length() + 1);
                lexer.rewind(inext.value.length() + 1);
                lexer.rewind(interfaceName.length() + 1);

                String icode = lexer.getRemainingSource();
                token.kind = CLIToken.EOF;

                System.out.println(icode);

                return new SimpleCommand(SimpleCommand.ADD_INTERFACE_CMD, new String[] { interfaceName, icode });

            case CLIToken.MODULE:
                String moduleToken = token.value;
                accept();
                String moduleName = parseName();
                CLIToken next = token;
                if(next.kind != CLIToken.AS)
                {
                    lexer.rewind(moduleToken.length() + 1);
                    lexer.rewind(next.value.length() + 1);
                    lexer.rewind(moduleName.length() + 1);
                    String mcode = lexer.getRemainingSource();
                    token.kind = CLIToken.EOF;
                    return new SimpleCommand(SimpleCommand.ADD_MODULE_CMD, new String[] { mcode });
                }
                else
                {
                    next = lexer.nextToken();
                    accept();
                    accept(CLIToken.ON);
                    int mode;
                    switch (next.kind)
                    {
                        case CLIToken.PROXY:
                            String wsdlUrl = parseString();
                            return new SimpleCommand(SimpleCommand.ADD_MODULE_AS_PROXY_CMD, new String[] {moduleName, wsdlUrl});
                        case CLIToken.WRAPPER:
                            mode = Wrapper.MODE_SQL;
                            break;
                        case CLIToken.SDWRAPPER:
                            mode = Wrapper.MODE_SD;
                            break;
                        case CLIToken.SWARDWRAPPER:
                            mode = Wrapper.MODE_SWARD;
                            break;
                        default:
                            throw new CLISyntaxErrorException(
                                "Expected one of: " +
                                CLIToken.spell(CLIToken.PROXY) + ", " +
                                CLIToken.spell(CLIToken.WRAPPER) + ", " +
                                CLIToken.spell(CLIToken.SDWRAPPER) + ", " +
                                CLIToken.spell(CLIToken.SWARDWRAPPER) + ", but " +
                                "'" + next.value + "' found instead...");
                    }

                    String host = parseServerName();
                    accept(CLIToken.COLON);
                    int port = parseInteger();

                    return new SimpleCommand(SimpleCommand.ADD_MODULE_AS_WRAPPER_CMD, new String[] {moduleName, host, Integer.toString(port), Integer.toString(mode)});
                }


            case CLIToken.LINK:
                accept();

                String lnkname = parseName();

                if (token.kind == CLIToken.TO) {
                	accept(CLIToken.TO);
                	if (token.kind != CLIToken.SCHEMA)
                        throw new CLISyntaxErrorException(CLIToken.spell(CLIToken.ON) + " expected");
                	return new SimpleCommand(SimpleCommand.ADD_LINK_TO_SCHEMA_CMD, new String[] { lnkname, lexer.getRemainingSource() } );
                }

                String lnkschema = parseServerName();
                accept(CLIToken.SLASH);
                String lnkpasswd = parseName();

                accept(CLIToken.AT);
                String lnkhost = parseServerName();
                accept(CLIToken.COLON);
                int lnkport = parseInteger();

                return new SimpleCommand(SimpleCommand.ADD_LINK_CMD, new String[] { lnkname, lnkschema, lnkpasswd, lnkhost, String.valueOf(lnkport) });

            case CLIToken.GRIDLINK:
                accept();

                String gridlnkname = parseName();

                String gridlnkschema = parseServerName();
                accept(CLIToken.SLASH);
                String gridlnkpasswd = parseName();

                accept(CLIToken.AT);
                String gridlnkhost = parseServerName();

                return new SimpleCommand(SimpleCommand.ADD_GRIDLINK_CMD, new String[] { gridlnkname, gridlnkschema, gridlnkpasswd, gridlnkhost });

            case CLIToken.INDEX:
            case CLIToken.TMPINDEX:
                boolean temporary = (token.kind == CLIToken.TMPINDEX);
                accept();

                String idxname = parseName();

                Vector<String> oparams = new Vector<String>();
                oparams.add(idxname);
                if(token.kind == CLIToken.LPAREN) {
                    accept();
                    oparams.add(parseName());
                    while (token.kind == CLIToken.BAR) {
                        accept();
                        oparams.add(parseName());
                    }
                    accept(CLIToken.RPAREN);
                }

                if (token.kind != CLIToken.ON)
                    throw new CLISyntaxErrorException(CLIToken.spell(CLIToken.ON) + " expected");

                oparams.add(lexer.getRemainingSource());
                //mikser,
                return new SimpleCommand(temporary?SimpleCommand.ADD_TMPINDEX_CMD:SimpleCommand.ADD_INDEX_CMD, oparams.toArray(new String[oparams.size()]));
            case CLIToken.ENDPOINT:
                accept();

                String endpointName = parseName();

                accept(CLIToken.ON);

                String exposedObject = parseName();

                accept(CLIToken.WITH);

                accept(CLIToken.LPAREN);

                accept(CLIToken.STATE);
                accept(CLIToken.EQ);
                String state = parseState();
                accept(CLIToken.COMMA);

                accept(CLIToken.PATH);
                accept(CLIToken.EQ);
                String path = parseRelativePath();
                accept(CLIToken.COMMA);

                accept(CLIToken.PORTTYPE);
                accept(CLIToken.EQ);
                String portTypeName = parseString();
                accept(CLIToken.COMMA);

                accept(CLIToken.PORT_NAME);
                accept(CLIToken.EQ);
                String portName = parseString();
                accept(CLIToken.COMMA);

                accept(CLIToken.SERVICE_NAME);
                accept(CLIToken.EQ);
                String serviceName = parseString();
                accept(CLIToken.COMMA);

                accept(CLIToken.NS);
                accept(CLIToken.EQ);
                String ns = parseNameSpace();

                accept(CLIToken.RPAREN);

                String[] params = new String[] { endpointName, exposedObject, state, path, portTypeName, portName, serviceName, ns};
                return new SimpleCommand(SimpleCommand.ADD_ENDPOINT_CMD, params);
            case CLIToken.VIEW:
                lexer.rewind(token.value.length() + 1);
                String code = lexer.getRemainingSource();
                token.kind = CLIToken.EOF;
                return new SimpleCommand(SimpleCommand.ADD_VIEW_CMD, new String[] { code });
            default:
                throw new CLISyntaxErrorException("'module', 'user', 'index', 'tmpindex', 'link', 'gridlink', 'endpoint' or 'assembled procedure' expected.");
        }
    }

    private final Command parseRemoveElement() throws CLISyntaxErrorException {
        switch (token.kind) {
            case CLIToken.REMOVE:
                accept();
                String iname = parseName();

                return new SimpleCommand(SimpleCommand.REMOVE_INTERFACE_CMD, new String[] { iname });

            case CLIToken.MODULE:
                accept();
                String mname = parseName();

                return new SimpleCommand(SimpleCommand.REMOVE_MODULE_CMD, new String[] { mname });

            case CLIToken.INDEX:
                accept();
                String idxname = parseName();

                return new SimpleCommand(SimpleCommand.REMOVE_INDEX_CMD, new String[] { idxname });
            case CLIToken.ENDPOINT:
                accept();
                String endpointName = parseName();

                return new SimpleCommand(SimpleCommand.REMOVE_ENDPOINT_CMD, new String[] { endpointName} );
            case CLIToken.VIEW:
                accept();
                String viewname = parseName();

                return new SimpleCommand(SimpleCommand.REMOVE_VIEW_CMD, new String[] { viewname });
            case CLIToken.LINK:
                accept();
                String linkName = parseName();

                if (token.kind == CLIToken.FROM) {
                	accept(CLIToken.FROM);
                	if (token.kind != CLIToken.SCHEMA)
                        throw new CLISyntaxErrorException(CLIToken.spell(CLIToken.ON) + " expected");
                	return new SimpleCommand(SimpleCommand.REMOVE_LINK_FROM_SCHEMA_CMD, new String[] { linkName, lexer.getRemainingSource() } );
                }

                return new SimpleCommand(SimpleCommand.REMOVE_LINK_CMD, new String[] { linkName });

            case CLIToken.GRIDLINK:
                accept();
                String gridlinkName = parseName();

                return new SimpleCommand(SimpleCommand.REMOVE_GRIDLINK_CMD, new String[] { gridlinkName });
            case CLIToken.PROXY:
            	accept();
            	String proxyName = parseName();
            	return new SimpleCommand(SimpleCommand.REMOVE_PROXY_CMD, new String[] {proxyName });
            default:
                throw new CLISyntaxErrorException("'module', 'index', 'link' or 'gridlink' expected.");
        }
    }

    private final Command parseSuspend() throws CLISyntaxErrorException {
        switch (token.kind) {
            case CLIToken.ENDPOINT:
                accept();
                String name = parseName();

                return new SimpleCommand(SimpleCommand.SUSPEND_ENDPOINT_CMD, new String[] { name });

            default:
                throw new CLISyntaxErrorException("'endpoint'  expected.");
        }
    }

    private final Command parseResume() throws CLISyntaxErrorException {
        switch (token.kind) {
        case CLIToken.ENDPOINT:
            accept();
            String name = parseName();

            return new SimpleCommand(SimpleCommand.RESUME_ENDPOINT_CMD, new String[] { name });

        default:
            throw new CLISyntaxErrorException("'endpoint'  expected.");
    }
    }

    private final Command parseDumpElement() throws CLISyntaxErrorException {
        switch (token.kind) {
            case CLIToken.MEMORY:
                accept();
                boolean verbose = token.kind != CLIToken.QUIET;
                if (!verbose) 
                	accept();
                
                return new SimpleCommand(SimpleCommand.DUMP_MEMORY_CMD, new String[] { Boolean.toString(verbose)});

            case CLIToken.STORE:
                accept();
                return new SimpleCommand(SimpleCommand.DUMP_STORE_CMD, new String[] { });

            case CLIToken.MODULE:
                accept();
                String name;

                if (token.kind == CLIToken.NAME)
                    name = parseName();
                else {
                    accept(CLIToken.DOT);
                    name = ".";
                }

                return new SimpleCommand(SimpleCommand.DUMP_MODULE_CMD, new String[] { name });

            default:
                throw new CLISyntaxErrorException("'module' expected");
        }
    }


    private final String parseServerName() throws CLISyntaxErrorException {
        if (token.kind == CLIToken.NAME) {
             return parseCompoundName();
        }
        else if (token.kind == CLIToken.INTEGER_LITERAL) {
            int a1 = parseInteger();
            accept(CLIToken.DOT);
            int a2 = parseInteger();
            accept(CLIToken.DOT);
            int a3 = parseInteger();
            accept(CLIToken.DOT);
            int a4 = parseInteger();

            return a1 + "." + a2 + "." + a3 + "." + a4;
        }
        else
            throw new CLISyntaxErrorException("Server name or its IP address expected");
    }

    /**
	 * @return
	 */
	private String parseCompoundName() throws CLISyntaxErrorException {
		String name = "";

        while (token.kind == CLIToken.NAME || token.kind == CLIToken.DOT) {
            name += token.value;
            accept();
        }
        return name;
	}

	//parses URI type e.g. "tcp://127.0.0.1:9701" or "file:///tmp/nextfile"
    private final String parseURI() throws CLISyntaxErrorException {
        String name = "";
        if (token.kind == CLIToken.NAME) {

            while (token.kind == CLIToken.NAME || token.kind == CLIToken.COLON) {
                name += token.value;
                accept();
            }
            while (token.kind == CLIToken.SLASH) {
                name += token.value;
                accept();
            }
            if (token.kind == CLIToken.INTEGER_LITERAL) {
                int a1 = parseInteger();
                accept(CLIToken.DOT);
                int a2 = parseInteger();
                accept(CLIToken.DOT);
                int a3 = parseInteger();
                accept(CLIToken.DOT);
                int a4 = parseInteger();

                name += a1 + "." + a2 + "." + a3 + "." + a4;

                if(token.kind == CLIToken.COLON)
                {
                    name += token.value;
                    accept();

                    if (token.kind == CLIToken.INTEGER_LITERAL) {
                        int b1 = parseInteger();
                        name += b1;
                    }
                }
            }
            else if (token.kind == CLIToken.NAME || token.kind == CLIToken.SLASH)
            {
                while (token.kind == CLIToken.NAME || token.kind == CLIToken.SLASH || token.kind == CLIToken.DOT)
                {
                    name += token.value;
                    accept();
                }
            }
        return name;
        }
        else
            throw new CLISyntaxErrorException("URI type string expected");
    }



    public final DatabaseURL parseDatabaseURL() throws CLISyntaxErrorException {
         String user = parseName();
         accept(CLIToken.SLASH);
         String password = parseName();
         accept(CLIToken.AT);
         String host = parseServerName();
         accept(CLIToken.COLON);
         int port = parseInteger();

         return new DatabaseURL(user, password, host, port);
    }

    private final String parseName() throws CLISyntaxErrorException {
        CLIToken t = token;

        accept();
//		accept(CLIToken.NAME);

        String name = t.value;
        if(name.length() == 0)
            throw new CLISyntaxErrorException("Not a valid name given...");

        return t.value;
    }

    private final String parseString() throws CLISyntaxErrorException {
        CLIToken t = token;

        accept(CLIToken.STRING_LITERAL);

        return t.value;
    }

    private final String parseState() throws CLISyntaxErrorException {
        CLIToken t = token;
        try {
            accept(CLIToken.STARTED);
        } catch (CLISyntaxErrorException ex) {
            try {
                accept(CLIToken.STOPPED);
            } catch (CLISyntaxErrorException ex2) {
                throw new CLISyntaxErrorException("'started' or 'stopped' expected, " + CLIToken.spell(token.kind) + " received");
            }

        }

        return t.value;

    }

    private final String parseRelativePath() throws CLISyntaxErrorException {
        CLIToken t = token;
        accept(CLIToken.STRING_LITERAL);

        try {
            URI path = new URI(t.value);
            if (path.isAbsolute()) {
                throw new CLISyntaxErrorException("Relative path expected.");
            }

        } catch (URISyntaxException ex) {
            throw new CLISyntaxErrorException("Relative path expected.");
        }

        return t.value;
    }

    private final String parseNameSpace() throws CLISyntaxErrorException {
        CLIToken t = token;
        accept(CLIToken.STRING_LITERAL);

        try {
            new URI(t.value);

        } catch (URISyntaxException ex) {
            throw new CLISyntaxErrorException("Namespace must be correct URI.");
        }

        return t.value;
    }

    private final int parseInteger() throws CLISyntaxErrorException {
        CLIToken t = token;

        int ival;
        try {
            ival = Integer.valueOf(t.value);
        }
        catch (NumberFormatException ex) {
            throw new CLISyntaxErrorException("Integer value expected");
        }

        accept(CLIToken.INTEGER_LITERAL);

        return ival;
    }

    private final void accept(byte exp) throws CLISyntaxErrorException {
        if (token.kind != exp)
            throw new CLISyntaxErrorException(CLIToken.spell(exp) + " expected, " + CLIToken.spell(token.kind) + " received");

        accept();
    }

    private final void accept() throws CLISyntaxErrorException {
        token = lexer.nextToken();
    }

}
