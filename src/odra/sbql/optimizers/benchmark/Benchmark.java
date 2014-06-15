package odra.sbql.optimizers.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import odra.cli.CLI;
import odra.cli.CLIVariable;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;

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
	
	protected void start(boolean output, int repeat) throws Exception
	{
	
		File outputFile = null;
		PrintWriter writer = null;
		
		if(output) {
    		do {
    			String date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date());
    			date = date.replaceAll(" ", ".").replaceAll(":", ".");
    			outputFile = new File(cli.getWorkingDirectory() + "/benchmark." + date + ".csv");
    		} while (outputFile.exists());
    		
    		writer = new PrintWriter(new FileWriter(outputFile));
	    }
		
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
		DBConnection connection = cli.getConnection();
		Hashtable<String, Object> data = new Hashtable<String, Object>();
		double typechecking_time_sum = 0;
		double optimization_time_sum = 0;
		double opt_execution_time_sum = 0;
		double ref_total_time_sum = 0;
		
		double first_typechecking_time = 0;
		double first_optimization_time = 0; 
		double first_opt_execution_time = 0;
		double first_ref_total_time = 0;
		
		for(int i = 0; i < repeat; i++)
		{
		    if(output) {
    			if (verbose) System.out.print("test " + (i + 1) + " of " + repeat + "... ");
    			else System.out.print(".");
		    }
			
			data.clear();
			
			DBRequest request = new DBRequest(
				DBRequest.EXECUTE_SBQL_RQST, 
				new String[] {
					query, 
					cli.getCurrMod(), 
					cli.getVar(CLIVariable.AUTODEREF), 
					cli.getVar(CLIVariable.TEST)});
			DBReply reply = connection.sendRequest(request);
			BagResult bagResult = (BagResult)reply.getResult();
			for(SingleResult result : bagResult.elementsToArray())
			{
				BinderResult binderResult = (BinderResult)result;
				data.put(binderResult.getName(), binderResult.value);
			}

			// first test is omitted in counting average value
			if (i == 0) {
				first_typechecking_time = ((IntegerResult)data.get(TIME_TYPECHECKING)).value;
				first_optimization_time = ((IntegerResult)data.get(TIME_OPTIMIZED_OPTIMIZATION)).value;
				first_opt_execution_time = ((IntegerResult)data.get(TIME_OPTIMIZED_EXECUTION)).value;
			}		
			typechecking_time_sum += ((IntegerResult)data.get(TIME_TYPECHECKING)).value;
			optimization_time_sum += ((IntegerResult)data.get(TIME_OPTIMIZED_OPTIMIZATION)).value;
			opt_execution_time_sum += ((IntegerResult)data.get(TIME_OPTIMIZED_EXECUTION)).value;
			
			if(!output) continue;
			
			if (cli.getVar(CLIVariable.TEST).equals("plaintimes")) {
				if(i == 0)
				{
					writer.println(TEST_MODE + SEPARATOR + cli.getVar(CLIVariable.TEST));
					writer.println(QUERY_RAW + SEPARATOR + query);
					writer.println();
					writer.println("#" + SEPARATOR + 
							TIME_TYPECHECKING + SEPARATOR +
							TIME_OPTIMIZED_OPTIMIZATION + SEPARATOR +
							TIME_OPTIMIZED_EXECUTION + SEPARATOR);
				}
				
				writer.println(i + SEPARATOR + 
						((IntegerResult)data.get(TIME_TYPECHECKING)).value + SEPARATOR +
						((IntegerResult)data.get(TIME_OPTIMIZED_OPTIMIZATION)).value + SEPARATOR +
						((IntegerResult)data.get(TIME_OPTIMIZED_EXECUTION)).value + SEPARATOR);
				if (verbose) System.out.println("typechecking [ms]: " + ((IntegerResult)data.get(TIME_TYPECHECKING)).value + 
						" optimization [ms]: " + ((IntegerResult)data.get(TIME_OPTIMIZED_OPTIMIZATION)).value + 
						" execution [ms]: " + ((IntegerResult)data.get(TIME_OPTIMIZED_EXECUTION)).value);
				continue;
			}
	
			// first test is omitted in counting average value
			if (i == 0) {
				first_ref_total_time = ((IntegerResult)data.get(TIME_REFERENCE)).value;
			}
			ref_total_time_sum += ((IntegerResult)data.get(TIME_REFERENCE)).value;
			
			if(i == 0)
			{
				writer.println(TEST_MODE + SEPARATOR + ((StringResult)data.get(TEST_MODE)).value);
				writer.println(OPTIMIZATION_SEQUENCE_REFERENCE + SEPARATOR + ((StringResult)data.get(OPTIMIZATION_SEQUENCE_REFERENCE)).value);
				writer.println(OPTIMIZATION_SEQUENCE_APPLIED + SEPARATOR + ((StringResult)data.get(OPTIMIZATION_SEQUENCE_APPLIED)).value);
				writer.println(QUERY_RAW + SEPARATOR + ((StringResult)data.get(QUERY_RAW)).value);
				writer.println(QUERY_REFERENCE + SEPARATOR + ((StringResult)data.get(QUERY_REFERENCE)).value);
				writer.println(QUERY_OPTIMIZED + SEPARATOR + ((StringResult)data.get(QUERY_OPTIMIZED)).value);
				writer.println();
				writer.println(
					"#" + SEPARATOR +
					TIME_TYPECHECKING + SEPARATOR +
					TIME_REFERENCE + SEPARATOR +
					TIME_OPTIMIZED + SEPARATOR +
					TIME_OPTIMIZED_OPTIMIZATION + SEPARATOR +
					TIME_OPTIMIZED_EXECUTION + SEPARATOR +
					RESULT_RATIO + SEPARATOR +
					RESULT_PERCENTAGE + SEPARATOR +
					RESULT_COMPARISON + SEPARATOR +
					STORE_USED_FOR_REFERENCE + SEPARATOR +
					STORE_USED_FOR_OPTIMIZED + SEPARATOR +
					STORE_USED_RATIO + SEPARATOR);
			}
			
			String line = Integer.toString(i + 1) + SEPARATOR;
			line += ((IntegerResult)data.get(TIME_TYPECHECKING)).value + SEPARATOR;
			line += ((IntegerResult)data.get(TIME_REFERENCE)).value + SEPARATOR;
			line += ((IntegerResult)data.get(TIME_OPTIMIZED)).value + SEPARATOR;
			line += ((IntegerResult)data.get(TIME_OPTIMIZED_OPTIMIZATION)).value + SEPARATOR;
			line += ((IntegerResult)data.get(TIME_OPTIMIZED_EXECUTION)).value + SEPARATOR;
			line += numberFormat.format(((DoubleResult)data.get(RESULT_RATIO)).value) + SEPARATOR;
			line += numberFormat.format(((DoubleResult)data.get(RESULT_PERCENTAGE)).value) + SEPARATOR;
			
			String comparisonInfo = ((StringResult)data.get(RESULT_COMPARISON)).value;
			String comparisonResult = "";
			if(comparisonInfo.equals(RESULT_COMPARISON_OK))
				comparisonResult = "OK";
			else if(comparisonInfo.equals(RESULT_COMPARISON_ERROR))
				comparisonResult = "ERROR";
			else if(comparisonInfo.equals(RESULT_COMPARISON_UNCERTAIN))
				comparisonResult = "uncertain";
			line += comparisonResult + SEPARATOR;
			
			try
			{
				line += ((IntegerResult)data.get(STORE_USED_FOR_REFERENCE)).value + SEPARATOR;
				line += ((IntegerResult)data.get(STORE_USED_FOR_OPTIMIZED)).value + SEPARATOR;
				line += numberFormat.format(((DoubleResult)data.get(STORE_USED_RATIO)).value) + SEPARATOR;
			}
			catch(NullPointerException exc) {}
			
			writer.println(line);
			
			if (verbose) System.out.println("exec times ratio: " + numberFormat.format(((DoubleResult)data.get(RESULT_RATIO)).value) + " (" + comparisonResult + ")");
		}
		
		if(!output) return;
		
		writer.println("");
		if (!verbose) System.out.println();		
		
		
		System.out.print("avg_typechecking [ms]: " + 
				numberFormat.format(typechecking_time_sum / repeat) + 
				" avg_optimization [ms]: " + 
				numberFormat.format(optimization_time_sum / repeat) + 
				" avg_opt_execution [ms]: " + 
				numberFormat.format(opt_execution_time_sum / repeat));
		
		if (!cli.getVar(CLIVariable.TEST).equals("plaintimes")) {
			System.out.println(" avg_ref_total [ms]: " + 
					numberFormat.format(ref_total_time_sum / repeat));
			System.out.print("avg_result_ratio: " + numberFormat.format(ref_total_time_sum / (typechecking_time_sum + optimization_time_sum + opt_execution_time_sum)) + " (with typechecking)");	
		}
		
		for(int k = 0; k <= 1; k++) {
			if(repeat > k) {
				if (k == 1) {
					typechecking_time_sum -= first_typechecking_time; 
					optimization_time_sum -= first_optimization_time;
					opt_execution_time_sum -= first_opt_execution_time;
					ref_total_time_sum -= first_ref_total_time;
					writer.println("");
					writer.println("Below results with test no 1 omitted in calculating avarage values (often erroneous).");
				}
				writer.print("avg_typechecking [ms]: " + SEPARATOR + " avg_optimization [ms]: " + SEPARATOR + " avg_opt_execution [ms]: ");
				if (!cli.getVar(CLIVariable.TEST).equals("plaintimes")) 
					writer.print(SEPARATOR + "avg_reference [ms]: " + SEPARATOR + "avg_result_ratio (with typechecking): ");
				writer.println("");
				writer.print(numberFormat.format(typechecking_time_sum / (repeat - k)) + SEPARATOR +
						numberFormat.format(optimization_time_sum / (repeat - k)) + SEPARATOR +
						numberFormat.format(opt_execution_time_sum / (repeat - k)));
				if (!cli.getVar(CLIVariable.TEST).equals("plaintimes")) 
					writer.print(SEPARATOR + numberFormat.format(ref_total_time_sum / (repeat - k)) + SEPARATOR + numberFormat.format(ref_total_time_sum / (typechecking_time_sum + optimization_time_sum + opt_execution_time_sum)));
				writer.println("");
			}			
		}
		writer.close();
		System.out.println();
		System.out.println("results written in " + outputFile.getAbsolutePath());
	}
}