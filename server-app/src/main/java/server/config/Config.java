package server.config;

import java.io.File;
import java.util.HashMap;

public class Config {

	public final int MAX_MESSAGE_LENGTH;
	public final int PORT;
	public final String DB_HOST;
	public final int DB_PORT;
	public final String DB_TABLE;
	public final String DB_USER;
	public final String DB_PASSWORD;

	public static final Config DEFAULT_CONFIG = new Config(Integer.MAX_VALUE, 1465, "localhost", 3306, "Chat", null,
			null);

	public Config(int maxMessageLength, int port, String dbHost, int dbPort, String dbTable, String dbUser,
			String dbPassword) {

		MAX_MESSAGE_LENGTH = maxMessageLength;
		this.PORT = port;
		this.DB_HOST = dbHost;
		this.DB_PORT = dbPort;
		this.DB_TABLE = dbTable;
		this.DB_USER = dbUser;
		this.DB_PASSWORD = dbPassword;

	}

	public static Config createFromArgs(String[] args, Config defaultValues) {

		ArgMap parameters = new ArgMap(args);

		int port;
		String dbHost;
		int dbPort;
		String dbTable;
		String dbUser;
		String dbPassword;

		port = parameters.getInteger("port", defaultValues.PORT);
		dbHost = parameters.getString("dbHost", defaultValues.DB_HOST);
		dbPort = parameters.getInteger("dbPort", defaultValues.DB_PORT);
		dbTable = parameters.getString("dbTable", defaultValues.DB_TABLE);
		dbUser = parameters.getString("dbUser", defaultValues.DB_USER);
		dbPassword = parameters.getString("dbPassword", defaultValues.DB_PASSWORD);

		return new Config(DEFAULT_CONFIG.MAX_MESSAGE_LENGTH, port, dbHost, dbPort, dbTable, dbUser, dbPassword);
	}

	public static Config loadCfg(File f) {
		// TODO
		return null;
	}

	public static void WriteCfg(File f, Config cfg) {
		// TODO
	}
	
	private static class ArgMap {

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

}
