package odra.sbql.optimizers.benchmark;

import java.io.*;
import java.util.Hashtable;

import odra.cli.CLI;
import odra.cli.CLIVariable;
import odra.exceptions.rd.RDException;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.SingleResult;

//import static odra.sbql.optimizers.benchmark.Benchmark.*;

/**
 * Benchmarking tool adapted from odra.sbql.optimizers.benchmark.Benchmark.
 * 
 * @author Greg Carlin
 * @version 2014-06-15
 * @since 2014-06-15
 *
 */
public class GregBenchmark extends Benchmark {

    public GregBenchmark(CLI cli, String query, int repeat, boolean verbose) {
        super(cli, query, repeat, verbose);
    }

    @Override
    protected void start(boolean output, int repeat) {
        try {
        
            DBConnection connection = cli.getConnection();
            Hashtable<String, Object> data = new Hashtable<String, Object>();
            //double executionTime = 0.0;
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
                DBReply reply = connection.sendRequest(request);
                BagResult bagResult = (BagResult)reply.getResult();
                for(SingleResult result : bagResult.elementsToArray()) {
                    BinderResult binderResult = (BinderResult)result;
                    data.put(binderResult.getName(), binderResult.value);
                }
                
                if(!output) continue;
                
                int executionTime = ((IntegerResult) data.get(TIME_OPTIMIZED_EXECUTION)).value;
                
                bw.write(query + SEPARATOR + executionTime);
            }
            
            bw.close();
        
        } catch (RDException | IOException e) {
            e.printStackTrace();
        }
    }
}
