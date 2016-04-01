package util;

/**
 * Convenience Extension of Counter to use an IdentityHashMap.
 */
public class IdentityCounter<E> extends Counter<E> {
	private static final long serialVersionUID = 1L;

	public IdentityCounter() {
		super(new MapFactory.IdentityHashMapFactory<E, Double>());
	}
}
