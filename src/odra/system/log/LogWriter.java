package odra.system.log;

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * A class responsible for logging system activity.
 * 
 * @author kk, jacenty
 */

public class LogWriter {
	private Logger logger;
	private StreamHandler consoleHandler;

	/**
	 * The constructor.
	 * 
	 * @param logPath log file path
	 * @param level logging level
	 */
	public LogWriter(String logPath, LoggingLevel level)
	{
		try
		{
			FileHandler fileHandler = new FileHandler(logPath);
			fileHandler.setLevel(level.setFileLevel());
			fileHandler.setFormatter(new LongFormatter());
			consoleHandler = new StreamHandler(System.out, new ShortFormatter());
			consoleHandler.setLevel(level.setConsoleLevel());
			logger = Logger.getLogger(logPath);
			logger.setLevel(Level.INFO);
			logger.setUseParentHandlers(false);
			logger.addHandler(fileHandler);
			logger.addHandler(consoleHandler);

			logger.fine("Logging service initiated.");
		}
		catch (Exception e) 
		{
			System.err.println("Cannot initiate the logging service (error: " + e.getMessage() + ")");
		}
	}

	public void flushConsole()
	{
		consoleHandler.flush();
	}
	
	public Logger getLogger() {
		return logger;
	}

	class ShortFormatter extends Formatter {
		String lineSeparator = System.getProperty("line.separator");

		public String format(LogRecord record) {
			StringBuffer addTrace = new StringBuffer();
			if (record.getThrown() != null) {
				addTrace.append(record.getThrown().toString() + lineSeparator);
				StackTraceElement[] frames = record.getThrown().getStackTrace();
				for (int i = 0; i < frames.length; i++)
					addTrace.append("\t" + frames[i].toString() + lineSeparator);
			}
			Object[] frames = record.getParameters();
			if (frames != null) {
			    
				for (int i = 0; i < frames.length; i++)
					addTrace.append("\t" + frames[i].toString() + lineSeparator);
			}
			return record.getLevel() + ": " + record.getMessage() + " (" + record.getSourceClassName() + " - " + record.getSourceMethodName() + ")" + lineSeparator + addTrace;
		}
	}

	class LongFormatter extends SimpleFormatter {
		String lineSeparator = System.getProperty("line.separator");

		public String format(LogRecord record) {

			StringBuffer paramTrace = new StringBuffer(super.format(record));
			Object[] frames = record.getParameters();
			if (frames != null)
				for (int i = 0; i <frames.length; i++)
					paramTrace.append("\t" + frames[i].toString() + lineSeparator);

			return paramTrace.toString();
		}
	}
}
