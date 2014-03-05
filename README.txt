
1. Running ODRA distribution
ODRA is a Java-based application and requires Sun™ JRE (Java Runtime Environment) 
to run. The required JRE version is 1.6+.

1.1 ODRA server
	1.1.1 Creating database 
		To create new database run server.bat (on Windows) or server.sh (on Unix). with the 
		following parameters:
							--<createstore> databasename size1 [size2] [size3]
		where:
		- databasename - name of the newly created database
		 - <createstore> - parameter is one of the following values 
		(representing data store types):
		a. 'create' - create default ODRA store with a fixed size. This option
			must be followed with the store size (in bytes).
			server(.bat/.sh) --create databasename storesize
			
			SAMPLE (create 'test' database with default store): 
						server.bat --create test 100000000
			
		b. 'create_optimized' - create optimized version of ODRA store with fixed
			size, divided into three separate spaces - object space, values space
			and special reference space. The size of each space must be defined as parameters.
			The syntax is presented below:
			server(.bat/.sh) --create_optimized databasename o_space_size v_space_size s_space_size 
			
			SAMPLE (create 'test' database with optimize store): 
						server.bat --create_optimized test 50000000 50000000 50000000
			
		c. 'create_expandable'  - create optimized version of ODRA store with changeable
			size (recommended). Size parameters as same as in optimized case 
			but define initial spaces	size.		 
			The syntax is presented below:
			server(.bat/.sh) --create_expandable databasename o_init_space_size value_space_size special_space_size
			 
			SAMPLE (create 'test' database with expandable store): 
						server.bat --create_expandable test 5000000 5000000 5000000
			 
			
	1.1.2 Running ODRA database instance 
	To start ODRA database instance run server.bat (on Windows) or server.sh (on Unix) with the 
	following parameters:
						--<startstoretype> databasename [port]
	where:
	
	- <startstoretype> is one of the following values:
		a. 'start' - starts default ODRA store
		b. 'start_optimized' - starts optimized ODRA store
		c. 'start_expandable' - starts expandable ODRA store

	- databasename - the name of the database. Database has to be created first (see section : 1.1.1)
	  and its type has to be compatible with the <startstoretype> parameter.

	- port - optional server port number (the default is 1521).
	
	SAMPLE (start 'test' database instance with expandable store on default store):
			server.bat --start_expandable test
 
1.2. ODRA Command Line Interface (CLI)
	1.2.1 Running CLI
	1.2.2 Connecting to a database instance
	
1.3 Easy start 
	The distribution contains also 'easystart' script (for testing purpose) that, 
	when run, perform following steps:
	- creates new database named 'test' with expandable store, 
	- start database instance for 'test' database on default port,
	- start CLI and automatically connects to a server as user admin.  
	 

2. Configuring ODRA

3. Building ODRA from source code
	ODRA source code is available from Subversion repository at: 
	svn://odra.pjwstk.edu.pl:2401/egovbus.
	Building ODRA from source code requires ant (ant.apache.org). 
	The ant bulid.xml file for ODRA can be found in the repository root. 
	If ant is properly configured (http://ant.apache.org/manual/index.html) simply
	run 'ant' in the project root folder.
	
4.  	 
	The repository contains also project files for Eclipse Platform (www.eclipse.org). 
	Nevertheless it still contain ant to generate parser & lexer source files.
	run 'ant generate' in the project to generate required source files 
	(it might require to refresh the project in Eclipse IDE). 
	 
	 
 
5. ODRA and Stack-Based Query Language description
	Visit the ODRA/SBQL Description and Programmer Manual for details on ODRA 
	architecture and features as well as the SBQL language reference:
		http://sbql.pl/various/ODRA/ODRA_manual.html

	For development questions visit project ODRA forum:
		http://iolab.pjwstk.edu.pl/forum/
 
6. Samples
Various sample and test SBQL code for the ODRA database is located in the /res folder 
in the form of .cli files (ODRA CLI batch files). To run a .cli file: 
1. start ODRA CLI 
2. connect to ODRA server
3. run 'batch path_to_file/filename.cli' from within ODRA CLI 

-------------------------------------------------------------------------------
"This product includes software developed by Sun Microsystems, Inc. for JXTA(TM) technology."