package server.db;

import java.sql.Timestamp;

import server.TransmittableObject;

public class Attr<T> {

	private T value = null;
	private boolean set = false;

	public void set(T value) {
		this.value = value;
		set = true;
	}

	public T unset() {
		T value = this.value;
		this.value = null;
		return value;
	}

	public T getValue() {
		return value;
	}

	public boolean isSet() {
		return set;
	}

	@Override
	public String toString() {

		if (!isSet())
			return "-";

		if (value == null)
			return "null";

		if (value instanceof byte[])
			return TransmittableObject.toBase64String((byte[]) value);

		return value.toString();
	}

	public String transmittableString() {
		if (!isSet())
			return "-";

		if (value == null)
			return "null";

		if (value instanceof String) {
			if (((String) value).contentEquals(""))
				return "-";

			return TransmittableObject.toBase64String(((String) value).getBytes());
		}

		if (value instanceof byte[]) {
			return TransmittableObject.toBase64String((byte[]) value);
		}

		if (value instanceof Timestamp)
			return Long.toString(((Timestamp) value).getTime());

		return value.toString();
	}

}
