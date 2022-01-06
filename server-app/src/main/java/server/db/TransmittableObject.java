package server.db;

import java.util.Base64;

public abstract class TransmittableObject {

	@Override
	public abstract String toString();

	protected String objsToString(Object... objects) {

		String s = "";

		for (int i = 0; i < objects.length - 1; i++) {
			s += objToString(objects[i], true);
		}
		s += objToString(objects[objects.length - 1], false);

		return s;
	}

	private String objToString(Object o, boolean space) {

		if (o == null)
			return "";

		String s;

		if (o instanceof String)
			s = Base64.getEncoder().encodeToString(((String) o).getBytes());
		else
			s = o.toString();

		if (space)
			s += " ";

		return s;
	}

}
