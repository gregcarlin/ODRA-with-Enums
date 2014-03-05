package odra.virtualnetwork;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.axis2.transport.http.AdminAgent;

import odra.OdraCoreAssemblyInfo;
import odra.cli.batch.BatchException;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.CMUHandlerImpl;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.api.ExternalConsole;
import odra.virtualnetwork.api.LocalTransport;
import odra.virtualnetwork.api.TransportPeer;
import odra.virtualnetwork.api.WindowHandler;
import odra.virtualnetwork.cmu.CMUnit;



public class CMUPeer { 

	CMUnit cmu = null;
	
	private ObjectManager manager;
	private DefaultStore store;
	private DBInstance instance;
	
//	private WindowHandler handler = null;
//	private Logger logger = null;

	
	public void createDatabase() throws Exception{
		try
		{
			DataFileHeap fileHeap;
			RevSeqFitMemManager allocator;
	
			fileHeap = new DataFileHeap("/tmp/" + odra.virtualnetwork.facade.Config.repoIdentity + ".dbf");
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
		
		CMUPeer cmupeer = new CMUPeer();
		
//		test.handler = WindowHandler.getInstance();

		//obtaining a logger instance and setting the handler
//		test.logger = Logger.getLogger("CMUPeer");
//	    test.logger.addHandler(test.handler);

		//seeting up a type of peer
		odra.virtualnetwork.facade.Config.peerType  = odra.virtualnetwork.facade.Config.PEER_TYPE.PEER_CMU;
		
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
			odra.virtualnetwork.facade.Config.repoIdentity = "CMUPeer";
			odra.virtualnetwork.facade.Config.platformHome = URI.create("file:///tmp/" + odra.virtualnetwork.facade.Config.repoIdentity);
			batchFilename = "conf/SchemaModel/cmu.cli";
			odra.virtualnetwork.api.IntegrViewGenConfig.xmlIntegrGenConfigFileName = "conf/IntegrationModel/integrationconfig.xml";
			odra.virtualnetwork.api.IntegrViewGenConfig.targetFileName = "conf/IntegrationModel/integrationconfig_out.xml";
			odra.virtualnetwork.api.IntegrViewGenIO.xslTransformSchemaFileName = "conf/IntegrationModel/integrationscript.xsl";
			odra.virtualnetwork.api.IntegrViewGen.xmlViewSchemaFileName = "conf/IntegrationModel/integrationscript.xml";
			odra.virtualnetwork.api.IntegrViewGen.targetFileName = "conf/IntegrationModel/integrationscript_out.xml";
			odra.virtualnetwork.api.IntegrViewGen.xmlGlobalViewSchemaFileName = "conf/IntegrationModel/globalviewscript.xml";
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
					odra.virtualnetwork.facade.Config.repoIdentity = "CMUPeer";
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
					batchFilename = "conf/SchemaModel/cmu.cli";
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
		odra.virtualnetwork.api.IntegrViewGenConfig.xmlIntegrGenConfigFileName = "conf/IntegrationModel/integrationconfig.xml";
		odra.virtualnetwork.api.IntegrViewGenConfig.targetFileName = "conf/IntegrationModel/integrationconfig_out.xml";
		odra.virtualnetwork.api.IntegrViewGenIO.xslTransformSchemaFileName = "conf/IntegrationModel/integrationscript.xsl";
		odra.virtualnetwork.api.IntegrViewGen.xmlViewSchemaFileName = "conf/IntegrationModel/integrationscript.xml";
		odra.virtualnetwork.api.IntegrViewGen.targetFileName = "conf/IntegrationModel/integrationscript_out.xml";
		odra.virtualnetwork.api.IntegrViewGen.xmlGlobalViewSchemaFileName = "conf/IntegrationModel/globalviewscript.xml";
		}
		
		try {
			cmupeer.createDatabase();
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

		cmupeer.shutdown();
		System.exit(0);
		
		//turn off autoconnect because CLI Class is used in ServerProcces
		ConfigClient.CONNECT_AUTO = false;

	}


	private void shutdown()
	{
		instance.shutdown();
		store.close();
		System.out.println("Database Stopped...");
	}
}
