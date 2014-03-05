package odra.virtualnetwork;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import odra.cli.CLI;
import odra.cli.batch.BatchException;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.Result;
import odra.security.UserContext;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.RemoteP2PStore;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.api.TransportPeer;
import odra.virtualnetwork.pu.ClientUnit;



public class CUPeer { 

	private ObjectManager manager;
	private static DefaultStore store;
	private DBInstance instance;
	
//	private WindowHandler handler = null;
//	private Logger logger = null;

	
	public void createDatabase() throws Exception{
		try
		{
			DataFileHeap fileHeap;
			RevSeqFitMemManager allocator;
	
			fileHeap = new DataFileHeap("/tmp/" + odra.virtualnetwork.facade.Config.repoIdentity +".dbf");
			fileHeap.format(1024 * 1024 * 20);
			fileHeap.open();
	
			allocator = new RevSeqFitMemManager(fileHeap);
			allocator.initialize();		
			
			manager = new ObjectManager(allocator);
			manager.initialize(100);
			
			store = new DefaultStore(manager);
			store.initialize();
	
			// prepare the database
			Database.initialize(store);
			Database.open(store);

			instance = new DBInstance();
			instance.startup();
	 
			System.out.println("Database created");
	
		}
		catch (DatabaseException exc)
		{
			exc.printStackTrace();
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		
		CUPeer cupeer = new CUPeer();
		
//		test.handler = WindowHandler.getInstance();

		//obtaining a logger instance and setting the handler
//		test.logger = Logger.getLogger("CMUnit");
//	    test.logger.addHandler(test.handler);

		//seeting up a type of peer
		odra.virtualnetwork.facade.Config.peerType  = odra.virtualnetwork.facade.Config.PEER_TYPE.PEER_ENDPOINT;
		
		String batchFilename = null;

		boolean lsnrPortFlag = false;
		boolean connPortFlag = false;
		boolean peerPortFlag = false;
		boolean peerNameFlag = false;
		boolean ipRDVFlag = false;
		boolean typecheckerFlag = false;
		boolean peerMonFlag = false;
		boolean platformHomeFlag = false;
		boolean batchFileFlag = false;
		boolean jxtaPortStartFlag = false;
		boolean jxtaPortEndFlag = false;
		boolean wsPortFlag = false;

		if (args.length == 0) {
			odra.virtualnetwork.facade.Config.repoIdentity = "CUPeer";
			odra.virtualnetwork.facade.Config.platformHome = URI.create("file:///tmp/" + odra.virtualnetwork.facade.Config.repoIdentity);
			batchFilename = "conf/SchemaModel/pu1.cli";
		}
		else {
			for (int a = 0; a < args.length; a++)
			{
				if (args[a].contains("lsnrPort")){
					ConfigServer.LSNR_PORT = Integer.parseInt(args[a].replace("lsnrPort=", ""));
					lsnrPortFlag = true;
				} else if (lsnrPortFlag == false){
					lsnrPortFlag = true;
				}
				
				if (args[a].contains("connPort")){
					ConfigClient.CONNECT_PORT = Integer.parseInt(args[a].replace("connPort=", ""));
					connPortFlag = true;
				} else if (connPortFlag == false){
					connPortFlag = true;
				}
				
				if (args[a].contains("peerPort")){
					odra.virtualnetwork.facade.Config.peerPort = Integer.parseInt(args[a].replace("peerPort=", ""));
					peerPortFlag = true;
				} else if (peerPortFlag == false){
					peerPortFlag = true;
				}
				
				if (args[a].contains("peerName")){
					odra.virtualnetwork.facade.Config.repoIdentity = args[a].replace("peerName=", "");
					peerNameFlag = true;
				} else if (peerNameFlag == false){
					odra.virtualnetwork.facade.Config.repoIdentity = "CUPeer";
					peerNameFlag = true;
				}

				if (args[a].contains("ipRDV")){
					odra.virtualnetwork.facade.Config.ipRandezVous = args[a].replace("ipRDV=", "");
					ipRDVFlag = true;
				} else if (ipRDVFlag == false){
					ipRDVFlag = true;
				}

				if (args[a].contains("typechecker")){
					if((args[a].replace("typechecker=", "").equals("off"))) ConfigServer.TYPECHECKING = false;
					typecheckerFlag = true;
				} else if(typecheckerFlag == false){
					typecheckerFlag = true;
				}

				if (args[a].contains("peerMon")){
					if((args[a].replace("peerMon=", "").equals("on"))) odra.virtualnetwork.facade.Config.peerMonitor = true;
					peerMonFlag = true;
				} else if (peerMonFlag == false){
					peerMonFlag = true;
				}

				if (args[a].contains("platformHome")){
					odra.virtualnetwork.facade.Config.platformHome = URI.create(args[a].replace("platformHome=", ""));
					platformHomeFlag = true;
				} else if (platformHomeFlag == false){
					odra.virtualnetwork.facade.Config.platformHome = URI.create("file:///tmp/" + odra.virtualnetwork.facade.Config.repoIdentity);
					platformHomeFlag = true;
				}

				if (args[a].contains("batchFile")){
					batchFilename = args[a].replace("batchFile=", "");
					batchFileFlag = true;
				} else if (batchFileFlag == false){
					batchFilename = "conf/SchemaModel/pu1.cli";
					batchFileFlag = true;
				}
				
				if (args[a].contains("jxtaPortStart")){
					odra.virtualnetwork.facade.Config.jxtaTransportPortStart = Integer.parseInt(args[a].replace("jxtaPortStart=", ""));
					jxtaPortStartFlag = true;
				} else if (jxtaPortStartFlag == false){
					jxtaPortStartFlag = true;
				}

				if (args[a].contains("jxtaPortEnd")){
					odra.virtualnetwork.facade.Config.jxtaTransportPortEnd = Integer.parseInt(args[a].replace("jxtaPortEnd=", ""));
					jxtaPortEndFlag = true;
				} else if (jxtaPortEndFlag == false){
					jxtaPortEndFlag = true;
				}

				if (args[a].contains("wsPort")){
					ConfigServer.WS_SERVER_PORT = Integer.parseInt(args[a].replace("wsPort=", ""));
					wsPortFlag = true;
				} else if (wsPortFlag == false){
					wsPortFlag = true;
				}


			}
		}
		
		try {
			cupeer.createDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}

		odra.cli.CLI cli = new odra.cli.CLI();
		try {
			cli.execBatch(new String[] {batchFilename});
			cli.begin();
		} catch (BatchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		cupeer.shutdown();
		System.exit(0);
	}

	private void shutdown()
	{
		instance.shutdown();
		store.close();
		System.out.println("Database Stopped...");
	}
}
