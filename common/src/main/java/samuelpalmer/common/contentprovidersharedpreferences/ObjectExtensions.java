package samuelpalmer.common.contentprovidersharedpreferences;

public final class ObjectExtensions {

	/**
	 * Compares by reference if either is {@code null}. Otherwise compares using {@link Object#equals(Object)}.
	 */
	public static boolean areEqual(Object a, Object b) {
		return a == null || b == null ? a == b : a.equals(b);
	}

}
