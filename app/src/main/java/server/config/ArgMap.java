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
	
	public String getString(String key, String defaultValue) {

		String value = argMap.get(key);

		if (value != null)
			return value;
		else
			return defaultValue;
	}

	public Integer getInteger(String key, int defaultValue) throws NumberFormatException {

		String value = argMap.get(key);

		if (value != null) {
			return Integer.parseInt(value);

		} else
			return defaultValue;
	}

}
