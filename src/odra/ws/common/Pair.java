package odra.ws.common;


/**
 * Represents a strongly typed pair
 * 
 * @since 2006-12-28
 * @version 2007-06-22
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 * @param <K> first element
 * @param <V> second element
 */

public class Pair<K, V> {
	private K key;
	private V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return this.key;
	}
	public void setKey(K key) {
		this.key = key;
	}
	public V getValue() {
		return this.value;
	}
	public void setValue(V value) {
		this.value = value;
	}



}
