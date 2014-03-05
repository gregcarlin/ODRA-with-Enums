package odra.wrapper.misc.testschema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Info distributor.
 * 
 * @author jacenty
 * @version 2007-02-20
 * @since 2007-02-20
 */
class InfoDistributor extends Distributor
{
	private final String dataFilePath = "res/wrapper/loremipsum.txt";
	
	InfoDistributor()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(dataFilePath));
			String line;
			while((line = reader.readLine()) != null)
				super.add(line, 1);
		}
		catch(IOException exc)
		{
			exc.printStackTrace();
		}
	}
}
