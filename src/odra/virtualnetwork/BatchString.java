package odra.virtualnetwork;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Vector;

import odra.cli.batch.BatchException;
import odra.util.StringUtils;

/**
 * Batch file.
 * 
 * @author jacenty
 * @version 2007-09-21
 * @since 2007-07-05
 */
public class BatchString
{
	
	enum BatchHeader
	{
		ENCODING("encoding"),
		VERBOSE("verbose"),
		COMPACT("compact");
		
		private final String name;
		private BatchHeader(String name)
		{
			this.name = name;
		}
		
		static BatchHeader getBatchHeaderForName(String name)
		{
			for(BatchHeader header : values())
				if(header.name.equals(name))
					return header;
			
			throw new AssertionError("Undefined batch header: '" + name + "'...");
		}
	}

	/** verbose to CLI? */
	private boolean verbose = true;
	/** compact multiline commands and trim white spaces? */
	private boolean compact = false;
	
	/** batch file comment prefix */
	protected final String BATCH_COMMENT_PREFIX = "#";
	/** batch file header prefix */
	protected final String BATCH_HEADER_PREFIX = "$";
	
	/**
	 * The constructor.
	 * 
	 * @param path file path
	 */
	
	
	private String batch_string = null;
	
	public BatchString(String batch_string)
	{
		this.batch_string = batch_string;
	}
	
	
	/**
	 * Reads commands from a batch file.
	 * 
	 * @return list of commands
	 * @throws IOException
	 * 
	 * @author jacenty
	 * @throws IOException
	 * @throws BatchException 
	 */
	public Vector<String> read() throws IOException, BatchException
	{
		Charset charset = Charset.defaultCharset();
		
		Vector<Object> headers = analyzeHeader();
		int headerLenght = (Integer)headers.get(0);
		for(Object element : headers.subList(1, headers.size()))
		{
			Object[] header = (Object[])element;
			if(header[0].equals(BatchHeader.ENCODING))
			{
				String charsetName = (String)header[1];
				if(Charset.isSupported(charsetName))
					charset = Charset.forName(charsetName);
				else
					throw new BatchException("Unsupported encoding specified: '" + charsetName + "'.");
			}
			else if(header[0].equals(BatchHeader.VERBOSE))
				verbose = new Boolean((String)header[1]);
			else if(header[0].equals(BatchHeader.COMPACT))
				compact = new Boolean((String)header[1]);
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(batch_string.getBytes()), charset));
		
		Vector<String> commands = new Vector<String>();
		String command = "";
		boolean multiline = false;
		String line;
		int lineCount = 0;
		readline: while((line = reader.readLine()) != null)
		{
			//skip headers
			while(lineCount < headerLenght)
			{
				lineCount++;
				continue readline;
			}
			
			if(compact)
				line = line.trim();
			
			if(!multiline && StringUtils.trimTrailing(line).endsWith("\\"))
			{
				multiline = true;
				line = StringUtils.trimTrailing(line);
				line = line.substring(0, line.length() - 1);
				if(compact)
					line = line.trim();
			}
			
			if(line.startsWith(BATCH_COMMENT_PREFIX))
				continue;
			
			if(!multiline && line.length() > 0)
			{
				if(compact)
					line = line.trim();
				commands.addElement(line);
			}
			else if(multiline)
			{
				if(line.trim().equals("."))
				{
					if(command.length() > 0)
						commands.addElement(command);
					command = "";
					multiline = false;
				}
				else
				{
					command += line;
					if(!compact)
						command += "\n";
				}
			}
		}
		reader.close();
		
		return commands;
	}
	
	/**
	 * Analalyzes the batch file and reads its headers. The returned vector's 1st element is number of 
	 * header lines (header length), other elements are pairs of a {@link BatchHeader} and its value.
	 * 
	 * TODO: this should be a dedicated class, instead of a {@link Vector}...
	 * 
	 * @return {@link Vector} containing headers
	 * @throws IOException
	 * @throws BatchException
	 */
	private Vector<Object> analyzeHeader() throws IOException, BatchException
	{
		Vector<Object> headers = new Vector<Object>();
		int lineCount = 0;
		
		BufferedReader reader = new BufferedReader(new StringReader(batch_string));
		String line;
		while((line = reader.readLine()) != null)
		{
			if(line.startsWith(BATCH_HEADER_PREFIX))
			{
				lineCount++;
				
				String[] split = line.substring(BATCH_HEADER_PREFIX.length()).split("=");
				try
				{
					BatchHeader header = BatchHeader.getBatchHeaderForName(split[0].trim());
					String value = split[1].trim();
					
					headers.addElement(new Object[] {header, value});
				}
				catch(AssertionError err)
				{
					throw new BatchException("Invalid batch header.", err);
				}
				catch(Exception exc)
				{
					throw new BatchException("Invalid batch header.", exc);
				}
			}
			else if(line.startsWith(BATCH_COMMENT_PREFIX) || line.trim().length() == 0)
				lineCount++;
			else
				break;
		}
		
		headers.insertElementAt(lineCount, 0);
		
		reader.close();
		
		return headers;
	}
	
	/**
	 * Verbose commands to CLI?
	 * 
	 * @return is verbose?
	 */
	public boolean verbose()
	{
		return verbose;
	}
}
