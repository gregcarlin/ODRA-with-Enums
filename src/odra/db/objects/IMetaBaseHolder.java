/**
 * 
 */
package odra.db.objects;

import odra.db.DatabaseException;
import odra.db.objects.meta.MetaBase;

/**
 * IMetaBaseHolder
 * interface representing ODRA objects that contains
 * metabase subobject 
 * @author Radek Adamus
 *@since 2008-04-29
 *last modified: 2008-04-29
 *@version 1.0
 */
public interface IMetaBaseHolder {
    /**
     * @return the {@link odra.db.objects.meta.MetaBase} object 
     * @throws DatabaseException
     */
    public MetaBase getMetaBase() throws DatabaseException;
}
