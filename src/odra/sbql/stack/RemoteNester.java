package odra.sbql.stack;

import odra.db.OID;

public class RemoteNester extends EnvsElement
{
	public OID link;

	public RemoteNester(OID lOID)
	{
		this.link = lOID;
	}
}
