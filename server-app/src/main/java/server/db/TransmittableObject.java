package server.db;

import java.util.Arrays;
import java.util.Base64;

public abstract class TransmittableObject {

	private Attr<?>[] attributes = new Attr<?>[0];

	// should be called to register all attributes that should be included in
	// generated String
	protected void registerAttributes(Attr<?>... attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {

		Attr<?>[] filteredAttributes = Arrays.stream(attributes).filter(a -> a.isSet()).toArray(Attr<?>[]::new);

		String s = "";

		for (int i = 0; i < filteredAttributes.length - 1; i++) {
			s += filteredAttributes[i].toString() + " ";
		}
		s += filteredAttributes[filteredAttributes.length - 1].toString();

		return s;
	}

	public static String toBase64String(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public static byte[] fromBase64String(String data) {
		return Base64.getDecoder().decode((data).getBytes());
	}

}