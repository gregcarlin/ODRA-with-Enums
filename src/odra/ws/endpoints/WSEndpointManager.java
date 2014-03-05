package odra.ws.endpoints;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.logging.Level;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBEndpoint;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MetaObjectKind;
import odra.system.Names;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;
import odra.ws.facade.IEndpointFacade;
import odra.ws.facade.WSManagersFactory;
import odra.ws.facade.WSProxyException;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;


/**
 * Web services endpoint manager.
 *
 * @since 2007-03-19
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class WSEndpointManager implements IEndpointFacade {
    // this hashtable is for temporaty use only
    private static Hashtable<String, HttpContext> contexts = new Hashtable<String, HttpContext>();
    // one server port for all endpoints is utilized in this manager
    private static HttpServer server = null;

    private static int serverPort;
    private static boolean exposeGeneric;
    private static String wsModule;

    private static DBModule rootModule;

    private OID endpoints;

    public WSEndpointManager()
    {

    }

    /**
     * Dummy function used to force initialization on database start
     * @throws WSEndpointException
     * @throws DatabaseException
     */
    public void initialize() throws DatabaseException
    {
        serverPort = ConfigServer.WS_SERVER_PORT;
        exposeGeneric = ConfigServer.WS_EXPOSE_GENERIC;
        wsModule = ConfigServer.WS_CONTEXT_USER;
        rootModule = Database.getModuleByName(wsModule);

        // start http server
        try {
            server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            server.start();

        } catch (IOException e) {
            throw new DatabaseException("Error creating HTTP server. " , e);
        }

        // prepare global dictionaries

        this.endpoints = rootModule.findFirstByNameId(Names.S_ENDPOINTS_ID, rootModule.getDatabaseEntry());

        if (this.endpoints == null) {
            throw new DatabaseException("Invalid database configuration");
        }


        try {
            synchronized (endpoints) {
                // load each one and expose it
                for (OID eid : this.endpoints.derefComplex()) {

                    this.registerEndpoint(eid);

                }
            }
        } catch (WSEndpointException ex) {
            throw new DatabaseException("Error registering endpoint. ", ex);
        }

        if (exposeGeneric) {
            WSEndpoint impl = WSEndpointFactory.createEndpoint();

            if (impl != null) {
                this.attachEndpoint(impl);

            } else {
                ConfigServer.getLogWriter().getLogger().log(Level.WARNING, "Error occured while creating generic endpoint");

            }
        }

    }

    /**
     * @param mod
     * @throws DatabaseException
     */
    public void createMetadata(DBModule mod) throws DatabaseException
    {
        OID oid = mod.createComplexObject(Names.namesstr[Names.S_ENDPOINTS_ID], mod.getDatabaseEntry(), 0);

    }

    /* (non-Javadoc)
     * @see odra.wsinterface.IEndpointManager#createEndpoint(java.lang.String, odra.db.OID, odra.wsinterface.EndpointState, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public OID createEndpoint(String name, OID exposedObject, EndpointState state, String relativePath, String portTypeName, String portName, String serviceName,  String targetNamespace) throws DatabaseException, WSEndpointException {
        // sanity checks
        if (exposedObject == null) {
            String  msg = "Parameter exposedObject in createEndpoint method of WSEndpointManager cannot be null";
            ConfigServer.getLogWriter().getLogger().log(Level.WARNING, msg);
            throw new RuntimeException(msg);
        }

        synchronized (endpoints) {
            if ( rootModule.findFirstByName(name, this.endpoints) != null){
                throw new WSEndpointException("Endpoint with name " + name + " already exists.");
            }
            try {
                DBModule parmod = new DBModule(exposedObject.getParent().getParent().getParent());
                if (parmod.isValid() && (WSManagersFactory.createProxyManager().isProxy(parmod.getOID()))) {
                    throw new WSEndpointException("Cannot create endpoint on object from proxy module. ");
                }

            } catch (WSProxyException ex) {
                throw new WSEndpointException(ex);

            }

            synchronized (contexts) {
                if ( contexts.containsKey(relativePath) ) {
                    WSEndpoint wse = (WSEndpoint) contexts.get(relativePath).getHandler();
                    String endpointName = wse.getDbEndpoint().getName();
                    throw new WSEndpointException("Endpoint " + endpointName + " already uses requested path " + relativePath);
                }
            }

            OID eid = null;
            try {
                eid = rootModule.createComplexObject(name, this.endpoints, DBEndpoint.FIELD_COUNT);
                DBEndpoint endpoint = new DBEndpoint(eid);
                endpoint.initialize(exposedObject, state, relativePath, portTypeName, portName, serviceName, targetNamespace);

                this.registerEndpoint(eid);

                return eid;

            } catch (Exception ex) {
                if (eid != null) {
                    eid.delete();
                }
                throw new WSEndpointException(ex);
            }
        }

    }

    /* (non-Javadoc)
     * @see odra.wsinterface.IEndpointManager#removeEndpoint(java.lang.String)
     */
    public void removeEndpoint(String endpointName)	 throws DatabaseException, WSEndpointException {
        synchronized (endpoints) {
            OID endpointid = rootModule.findFirstByName(endpointName, this.endpoints);

            if (endpointid != null) {
                DBEndpoint dbEndpoint = new DBEndpoint(endpointid);
                this.unregisterEndpoint(dbEndpoint.getRelativePath());
                endpointid.delete();
            } else {
                throw new DatabaseException("Endpoint " + endpointName + " doesn't exist.");
            }
        }
    }


    /* (non-Javadoc)
     * @see odra.wsinterface.IEndpointManager#suspendEndpoint(java.lang.String)
     */
    public void suspendEndpoint(String name) throws WSEndpointException {
        try {
            synchronized (endpoints) {
                OID oid = rootModule.findFirstByName(name, this.endpoints);
                if (oid == null) {
                    throw new WSEndpointException("Unknown endpoint name. ");
                }
                DBEndpoint dbEndpoint = new DBEndpoint(oid);
                dbEndpoint.updateState(EndpointState.STOPPED);

                synchronized (contexts) {
                    HttpContext context = contexts.get(dbEndpoint.getRelativePath());
                    if (context == null) {
                        throw new WSEndpointException("Internal error while updating endpoint state. ");
                    }

                    if (context.getHandler() instanceof WSEndpoint) {
                        WSEndpoint instance = (WSEndpoint) context.getHandler();
                        instance.setDbEndpoint(dbEndpoint);

                    } else {
                        throw new WSEndpointException("Internal error while updating endpoint state. ");
                    }
                }
            }
        } catch (DatabaseException ex) {
            throw new WSEndpointException("Error while stopping " + name + " endpoint. ", ex);
        }
    }


    /* (non-Javadoc)
     * @see odra.wsinterface.IEndpointManager#resumeEndpoint(java.lang.String)
     */
    public void resumeEndpoint(String name)  throws WSEndpointException {
        try {
            synchronized (endpoints) {
                OID oid = rootModule.findFirstByName(name, this.endpoints);
                if (oid == null) {
                    throw new WSEndpointException("Unknown endpoint name. ");
                }
                DBEndpoint dbEndpoint = new DBEndpoint(oid);
                dbEndpoint.updateState(EndpointState.STARTED);

                synchronized (contexts) {
                    HttpContext context = contexts.get(dbEndpoint.getRelativePath());
                    if (context == null) {
                        throw new WSEndpointException("Internal error while updating endpoint state. ");
                    }

                    if (context.getHandler() instanceof WSEndpoint) {
                        WSEndpoint instance = (WSEndpoint) context.getHandler();
                        instance.setDbEndpoint(dbEndpoint);

                    } else {
                        throw new WSEndpointException("Internal error while updating endpoint state. ");
                    }
                }
            }
        } catch (DatabaseException ex) {
            throw new WSEndpointException("Error while stopping " + name + " endpoint. ", ex);
        }
    }


    /* (non-Javadoc)
     * @see odra.wsinterface.IEndpointManager#stopServer()
     */
    public void stopServer() {
    	if (server != null) {
		    synchronized (server) {
		    	server.stop(0);
		    }
    	}
    }

    private void registerEndpoint(OID eid) throws WSEndpointException {

        try {
            DBEndpoint dbEndpoint = new DBEndpoint(eid);

            if ( dbEndpoint.getExposedObject() != null) {
                MBObject exposed = new MBObject(dbEndpoint.getExposedObject());

                WSEndpoint impl = WSEndpointFactory.createEndpoint(dbEndpoint, exposed.getModule(), exposed.getName());

                if (ConfigDebug.ASSERTS) {
                    assert exposed.getObjectKind() == MetaObjectKind.PROCEDURE_OBJECT || exposed.getObjectKind() == MetaObjectKind.CLASS_OBJECT;
                }

                if (impl != null) {
                    this.attachEndpoint(impl);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Problem creating endpoint for ");
                    sb.append(dbEndpoint.getName());

                    String msg = sb.toString();

                    ConfigServer.getLogWriter().getLogger().log(Level.WARNING, msg);
                }
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Object exposed by endpoint ");
                sb.append(dbEndpoint.getName());
                sb.append(" must have been deleted.");

                String msg = sb.toString();

                ConfigServer.getLogWriter().getLogger().log(Level.WARNING, msg);

            }

        } catch (DatabaseException ex) {
            ConfigServer.getLogWriter().getLogger().log(Level.WARNING, "Error registering endpoint", ex);
            throw new WSEndpointException("Error registering endpoint", ex);
        }
    }

    /** Exposes given endpoint implmentation (wrapped with http handler adapter)
     * @param implementator
     */
    private void attachEndpoint(WSEndpoint implementator) {
        // create & register endpoint
        synchronized (contexts) {
            synchronized (server) {
            HttpContext context = server.createContext(implementator.getOptions().getRelativePath(), implementator);
            contexts.put(implementator.getOptions().getRelativePath(), context);
            }
        }
    }

    private void unregisterEndpoint(String path) throws WSEndpointException{
        synchronized (contexts) {
            // locate endpoint identified by a path
            HttpContext context = contexts.get(path);
            if (context == null) {
                throw new WSEndpointException("Internal error while unregistering endpoint. ");
            }
            // stop endpoint and delete if from directories
            context.getServer().removeContext(context);
            contexts.remove(path);
        }

    }

	@Override
	public boolean endpointExist(String name) throws WSEndpointException {
		try {
			synchronized (endpoints) {
	            OID oid = rootModule.findFirstByName(name, this.endpoints);
	            return oid != null;
			}
		} catch (DatabaseException ex) {            
            throw new WSEndpointException("Error checking endpoint existance. ", ex);
        }
		
	}



}
