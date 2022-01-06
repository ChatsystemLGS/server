package server.db;

import java.util.Base64;

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
			return "";

		if (value == null)
			return "null";

		if (value instanceof String) {
			if (((String) value).contentEquals(""))
				return "-";

			return Base64.getEncoder().encodeToString(((String) value).getBytes());
		}

		return value.toString();
	}

}
