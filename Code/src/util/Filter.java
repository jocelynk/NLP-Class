package util;

/**
 * Filters are boolean functions which accept or reject items.
 */
public interface Filter<T> {
	boolean accept(T t);
}
