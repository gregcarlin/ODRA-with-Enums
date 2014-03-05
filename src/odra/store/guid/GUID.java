package odra.store.guid;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import odra.sessions.Session;
import odra.transactions.store.Transaction;

/**
 * Realization of the well-known concept of the Globally Unique IDentifier utilized for identifying resources of the
 * enviroment, such as: {@link Transaction}, {@link Session}, etc.
 * <p>
 * Hopefully it will be more commonly used throughout the jODRA2 implementation in the near future.
 * 
 * @author edek
 */
public final class GUID implements IGUIDIdentifiableResource {

	private final static int GUID_LENGTH = 16;

	private final static Random rand = new Random();

	private final static Comparator<GUID> guidComparator = new GUIDComparator();

	private final BigInteger id;

	private GUID(byte[] bytes) {
		this.id = new BigInteger(bytes);
	}

	private static GUID getRandomGUID() {
		final byte[] bytes = new byte[GUID_LENGTH];
		rand.nextBytes(bytes);
		return new GUID(bytes);
	}

	private final static int GUID_EXISTS = -1;

	public static synchronized GUID generateGUID(List<? extends IGUIDIdentifiableResource> extent) {
		try {
			GUID guid = null;
			do {
				guid = getRandomGUID();
			} while (getInsertionIndex(extent, guid, false) == GUID_EXISTS);
			return guid;
		} catch (GUIDException ex) {
			throw new GUIDRuntimeException("should never occur", ex);
		}
	}

	public static synchronized int getInsertionIndex(List<? extends IGUIDIdentifiableResource> extent, GUID guid)
				throws GUIDException {
		return getInsertionIndex(extent, guid, true);
	}

	private static synchronized int getInsertionIndex(List<? extends IGUIDIdentifiableResource> extent, GUID guid,
				boolean checkUniqness) throws GUIDException {
		int index = Collections.binarySearch(extent, guid);
		int insertionIndex = (index < 0) ? Math.abs(index + 1) : GUID_EXISTS;
		if (checkUniqness) {
			checkInsertionIndex(insertionIndex);
		}
		return insertionIndex;
	}

	public int compareTo(IGUIDIdentifiableResource otherGUID) {
		return guidComparator.compare(this, otherGUID.getGUID());
	}

	public String toString() {
		return this.id.toString();
	}

	public GUID getGUID() {
		return this;
	}

	private static boolean checkInsertionIndex(int insertionIndex) throws GUIDException {
		if (insertionIndex == GUID_EXISTS) {
			throw new GUIDException("should never occurr --- suggests that GUID is not unique");
		}
		return true;
	}

	private final static class GUIDComparator implements Comparator<GUID> {
		public int compare(GUID o1, GUID o2) {
			return o1.id.compareTo(o2.id);
		}
	}
}