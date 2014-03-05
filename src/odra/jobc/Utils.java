/**
 * 
 */
package odra.jobc;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utils
 * utility class for JOBC
 * @author Radek Adamus
 *@since 2008-04-21
 *last modified: 2008-04-21
 *@version 1.0
 */
class Utils {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    
    public static String date2Str(Date date){
	return format.format(date);	
    }
}
