package server.db;

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

		return value.toString();
	}

}
