package odra.wrapper.net;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Message class. 
 * @author jacenty
 * @version   2007-04-25
 * @since   2006-09-11
 */
public class Message
{
	/** message core */
	private final Core core;
	/** parameter (may be <code>null</code>) */
	private final String param;
	
	/**
	 * The constructor.
	 * 
	 * @param core message core
	 */
	public Message(Core core)
	{
		this(core, null);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param core message core
	 * @param param message param
	 */
	public Message(Core core, String param)
	{
		this.core = core;
		this.param = param;
	}
	
	@Override
	public String toString()
	{
		if(param == null)
			return core.toString();

		return core.toString() + ": " + param;
	}
	
	/**
	 * Sends the message to a print writer.
	 * 
	 * @param out <code>PrintWriter</code>
	 * @param stdoutMessage standart output (console) displayed message prefix
	 * @param verbose verbose?
	 */
  public void send(PrintWriter out, String stdoutMessage, boolean verbose)
  {
    out.println(encode(this.toString()));
    if(verbose)
    	System.out.println(stdoutMessage + " -> " + this);
  }
  
  /**
	 * Sends a miscellaneous message to a print writer.
	 * 
	 * @param out <code>PrintWriter</code>
	 * @param msg message
	 * @param stdoutMessage standart output (console) displayed message prefix
	 * @param verbose verbose
	 */
  public static void sendMiscellaneousMessage(PrintWriter out, String msg, String stdoutMessage, boolean verbose)
  {
    out.println(encode(msg));
    if(verbose)
    	System.out.println(stdoutMessage + " -> " + msg);
  }
  
  public String getParameter()
  {
  	return param;
  }
	
  /**
   * Message cores.
   */
	public enum Core
	{ 
		HELLO ("hello"),
		BYE ("bye"),
		CLOSE ("close"),
		READY ("ready"),
		REQUEST_IDENTITY ("request.identity"),
		IDENTITY ("identity"),
		GET_TRANSFER_PORT ("get.transfer.port"),
		TRANSFER_PORT ("transfer.port"),
		REJECT ("reject"),
		ERROR ("error"),
		QUERY ("query"),
		DATA_READY ("data.ready"),
		SEND_DATA ("send.data"),
		SENDING_DATA ("sending.data"),
		GET_DATA_LENGTH ("get.data.length"),
		DATA_LENGTH ("data.length"),
		DATA_RECEIVED ("data.received"),
		WANT_ANOTHER ("want.another"),
		SEND_DATABASE ("send.database"),
		SEND_METABASE ("send.metabase"),
		REQUEST_MODE ("request.mode"),
		MODE ("mode"),
		;
		
		private final String msg;
		
		/**
		 * Constructor.
		 * 
		 * @param msg message core text
		 */
		Core(String msg)
		{
			this.msg = msg;
		}
	  
		/**
		 * Returns a core corresponding to the <code>msg</code>;
		 * 
		 * @param msg message core text
		 * @return message core
		 */
	  public static Core getMessageForString(String msg)
		{
			if(msg.equals(HELLO.msg))
				return HELLO;
			else if(msg.equals(BYE.msg))
				return BYE;
			else if(msg.equals(CLOSE.msg))
				return CLOSE;
			else if(msg.equals(READY.msg))
				return READY;
			else if(msg.equals(REQUEST_IDENTITY.msg))
				return REQUEST_IDENTITY;
			else if(msg.equals(IDENTITY.msg))
				return IDENTITY;
			else if(msg.equals(GET_TRANSFER_PORT.msg))
				return GET_TRANSFER_PORT;
			else if(msg.equals(TRANSFER_PORT.msg))
				return TRANSFER_PORT;
			else if(msg.equals(REJECT.msg))
				return REJECT;
			else if(msg.equals(ERROR.msg))
				return ERROR;
			else if(msg.equals(QUERY.msg))
				return QUERY;
			else if(msg.equals(DATA_READY.msg))
				return DATA_READY;
			else if(msg.equals(SEND_DATA.msg))
				return SEND_DATA;
			else if(msg.equals(SENDING_DATA.msg))
				return SENDING_DATA;
			else if(msg.equals(GET_DATA_LENGTH.msg))
				return GET_DATA_LENGTH;
			else if(msg.equals(DATA_LENGTH.msg))
				return DATA_LENGTH;
			else if(msg.equals(DATA_RECEIVED.msg))
				return DATA_RECEIVED;
			else if(msg.equals(WANT_ANOTHER.msg))
				return WANT_ANOTHER;
			else if(msg.equals(SEND_DATABASE.msg))
				return SEND_DATABASE;
			else if(msg.equals(SEND_METABASE.msg))
				return SEND_METABASE;
			else if(msg.equals(REQUEST_MODE.msg))
				return REQUEST_MODE;
			else if(msg.equals(MODE.msg))
				return MODE;
			else
				throw new AssertionError("Unknown message: " + msg);
		}

		@Override
		public String toString()
		{
			return msg;
		}
	};
	
	/**
	 * Returns a message core text and a param (may be <code>null</code>).
	 * 
	 * @param rawMessage raw message
	 * @return message core text and param
	 */
	public static String[] splitMessage(String rawMessage)
	{
		String[] result = new String[2];
		
		if(rawMessage.indexOf(":") >= 0)
		{
			String[] split = rawMessage.split(":", 2);
			result[0] = split[0].trim();
			result[1] = split[1].trim();
		}
		else
			result[0] = rawMessage.trim();
		
		return result;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof Message))
			return false;

		return ((Message)object).getCore().equals(this.getCore());
	}
	
	/**
	 * Returns a message core.
	 * 
	 * @return message core
	 */
	public Core getCore()
	{
		return core;
	}
	
	/**
	 * Encodes a string.
	 * 
	 * @param string string to encode
	 * @return encoded string
	 */
	public static String encode(String string)
	{
		try
		{
			return URLEncoder.encode(string, "utf-8");
		}
		catch(UnsupportedEncodingException exc)
		{
			return string;
		}
	}
	
	/**
	 * Decodes a string.
	 * 
	 * @param string string to decode
	 * @return decoded string
	 */
	public static String decode(String string)
	{
		try
		{
			return URLDecoder.decode(string, "utf-8");
		}
		catch(UnsupportedEncodingException exc)
		{
			return string;
		}
	}
}