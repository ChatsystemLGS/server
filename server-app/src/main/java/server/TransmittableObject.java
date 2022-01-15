package server;

import java.util.Arrays;
import java.util.Base64;

import server.db.Attr;

public interface TransmittableObject {

	public String transmittableString();

	default String transmittableString(Attr<?>... attributes) {
		Attr<?>[] filteredAttributes = Arrays.stream(attributes).filter(a -> a.isSet()).toArray(Attr<?>[]::new);

		String s = "";

		for (int i = 0; i < filteredAttributes.length - 1; i++) {

			s += filteredAttributes[i].transmittableString() + " ";
		}
		s += filteredAttributes[filteredAttributes.length - 1].transmittableString();

		return s;
	}

	public static String toBase64String(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public static byte[] fromBase64String(String data) {
		return Base64.getDecoder().decode((data).getBytes());
	}

	// borrowed from Arrays.toString(Object[] o)
	public static String toString(TransmittableObject[] a) {
		if (a == null)
			return "null";

		int iMax = a.length - 1;
		if (iMax == -1)
			return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(a[i].transmittableString());
			if (i == iMax)
				return b.append(']').toString();
			b.append(", ");
		}
	}

}