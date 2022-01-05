package server.config;

import java.util.HashMap;

public class ArgMap {

	HashMap<String, String> argMap = new HashMap<>();

	public ArgMap(String args[]) {

		for (int i = 0; i < args.length; i++) {
			String[] arg = args[i].split("=", 2);
			if (arg[0].contentEquals(""))
				throw new IllegalArgumentException(String.format("Missing key for value \"%s\"", arg[1]));
			if (arg[1].contentEquals(""))
				throw new IllegalArgumentException(String.format("Missing value for key \"%s\"", arg[0]));

			argMap.put(arg[0], arg[1]);
		}

	}

	public String getString(String key, String defaultValue) throws IllegalArgumentException {

		String value = argMap.get(key);

		if (value != null)
			return value;
		else if (defaultValue != null)
			return defaultValue;

		throw new IllegalArgumentException(String.format("No default value specified for key \"%s\".", key));
	}

	public int getInteger(String key, Integer defaultValue) throws IllegalArgumentException {

		String value = argMap.get(key);

		if (value != null)
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
						String.format("Value specified for key \"%s\" shoukd be an Integer.", key));
			}
		else if (defaultValue != null)
			return defaultValue;

		throw new IllegalArgumentException(String.format("No default value specified for key \"%s\".", key));

	}

}
