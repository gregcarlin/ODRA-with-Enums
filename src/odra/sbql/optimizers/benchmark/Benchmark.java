package odra.sbql.optimizers.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;

import odra.cli.CLI;
import odra.cli.CLIVariable;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.SingleResult;

/**
 * Optimization benchmarking utility.
 * <br />
 * After n-time execution of a query in compare/comparesimple/plaintimes test the results are written to a CSV file.
 * 
 * @author jacenty
 * @version 2007-08-01
 * @since 2007-02-24
 */
public class Benchmark
{
	public static String TEST_MODE = "test_mode";
	
	public static String OPTIMIZATION_SEQUENCE_REFERENCE = "optimization_sequence_reference";
	public static String OPTIMIZATION_SEQUENCE_APPLIED = "optimization_sequence_applied";
	
	public static String QUERY_RAW = "raw_query";
	public static String QUERY_REFERENCE = "reference_query";
	public static String QUERY_OPTIMIZED = "optmized_query";
	
	public static String TIME_TYPECHECKING = "typechecking_time";
	public static String TIME_REFERENCE = "reference_total_time";
	public static String TIME_OPTIMIZED = "optimized_total_time";
	public static String TIME_OPTIMIZED_OPTIMIZATION = "optimization_time";
	public static String TIME_OPTIMIZED_EXECUTION = "optimized_execution_time";
	
	public static String RESULT_RATIO = "time_ratio";
	public static String RESULT_PERCENTAGE = "speed_increase_percent";
	public static String RESULT_COMMENT = "comment";
	public static String RESULT_COMPARISON = "result_comparision";
	
	public static final String RESULT_COMPARISON_OK = "Results comparision OK - referenced and optimized query results are identical";
	public static final String RESULT_COMPARISON_ERROR = "Results comparision ERROR - referenced and optimized query results are different";
	public static final String RESULT_COMPARISON_UNCERTAIN = "Results comparision uncertain - referenced and optimized query results have identical length and contain the same bytes (in different order) but the actual contents may differ";
	
	public static final String STORE_USED_FOR_REFERENCE = "store_used_for_reference";
	public static final String STORE_USED_FOR_OPTIMIZED = "store_used_for_optimized";
	public static final String STORE_USED_RATIO = "store_used_ratio";
	
	/** CSV column separator */
	protected final String SEPARATOR = ",";
	
	/** CLI instance */
	protected final CLI cli;
	/** query */
	protected final String query;
	/** number of repeats */
	private final int repeat;
	/** prints individual tests results on console */
	protected final boolean verbose;
	
	/**
	 * The constructor.
	 * 
	 * @param cli CLI instance
	 * @param query query
	 * @param repeat number of repeats
	 */
	public Benchmark(CLI cli, String query, int repeat, boolean verbose)
	{
		this.cli = cli;
		this.query = query;
		this.repeat = repeat;
		this.verbose = verbose;
		
		/*StringBuilder newQuery = new StringBuilder();
        for(int i=0; i<10; i++) {
            newQuery.append(query);
        }
        this.query = newQuery.toString();*/
	}
	
	/**
	 * Starts the benchmark.
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
	    System.out.println("Warming up...");
	    start(false, 10);
	    System.out.println("Starting benchmarks...");
	    start(true, repeat);
	}
	
	protected void start(boolean output, int repeat) {
        try {
            
            DBConnection connection = cli.getConnection();
            Hashtable<String, Object> data = new Hashtable<String, Object>();
            int executionTimeA = 0;
            long executionTimeB = 0;
            BufferedWriter bw = null;
            if(output) {
                bw = new BufferedWriter(new FileWriter(new File(cli.getWorkingDirectory() + "/benchmarks.csv"), true));
                /*bw.write("query" + SEPARATOR + "execution time");
                bw.newLine();*/
            }
            
            for(int i=0; i<repeat; i++) {
                if(output && verbose) System.out.print("test " + (i + 1) + " of " + repeat + "... ");
                
                data.clear();
                
                DBRequest request = new DBRequest(
                    DBRequest.EXECUTE_SBQL_RQST, 
                    new String[] {
                        query, 
                        cli.getCurrMod(), 
                        cli.getVar(CLIVariable.AUTODEREF), 
                        cli.getVar(CLIVariable.TEST)});
                long start = System.nanoTime();
                DBReply reply = connection.sendRequest(request);
                BagResult bagResult = (BagResult)reply.getResult();
                long thisExecutionTimeB = System.nanoTime() - start;
                executionTimeB += thisExecutionTimeB;
                for(SingleResult result : bagResult.elementsToArray()) {
                    BinderResult binderResult = (BinderResult)result;
                    data.put(binderResult.getName(), binderResult.value);
                }
                
                if(!output) continue;
                
                int thisExecutionTimeA = ((IntegerResult) data.get(TIME_OPTIMIZED_EXECUTION)).value;
                if(output && verbose) System.out.println(thisExecutionTimeA + "ms; " + thisExecutionTimeB + "ns");
                executionTimeA += thisExecutionTimeA;
                
                /*bw.write(query + SEPARATOR + executionTime);
                bw.newLine();*/
            }
            
            if(output) {
                bw.write(query + SEPARATOR + ((double) executionTimeA/* / repeat*/) + SEPARATOR + (executionTimeB/* / (double) repeat*/));
                bw.newLine();
                bw.close();
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}