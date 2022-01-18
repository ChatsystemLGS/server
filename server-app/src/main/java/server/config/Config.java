package server.config;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	// @formatter=off
	public static final String HELP_MESSAGE = "Usage: [optional parameters] -dbUser=DATABASE_USER -dbPassword=DATABASE_PASSWORD\n"
			+ "\n"
			+ "Options:\n"
			+ "    Parameter                    Default Value  Description\n"
			+ "    -port=PORT                   1465           Port the server should listen on\n"
			+ "    -dbHost=DATABASE_HOST        localhost      Hostname of the database the server should connect to\n"
			+ "    -dbPort=DATABASE_PORT        3306           Port of the database the server should connect to\n"
			+ "    -dbTable=DATABASE_TABLE      Chat           Table to be used by the server";
	// @formatter=on

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

	public static Config createFromArgs(String[] args, Config defaultValues) throws ParameterParseException {

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

	public static Config readCfg(File f) {
		// TODO
		return null;
	}

	public static void WriteCfg(File f, Config cfg) {
		// TODO
	}

	private static class ArgMap {

		Map<String, String> argMap = new HashMap<>();
		Set<String> flags = new HashSet<>();

		public ArgMap(String args[]) {

			for (int i = 0; i < args.length; i++) {

				if (args[i].startsWith("-")) {

					String[] arg = args[i].split("=", 2);

					if (arg.length == 1)
						flags.add(arg[1]);

					String key = arg[0].substring(1);
					String value = arg[1];

					if (key.contentEquals(""))
						throw new IllegalArgumentException(String.format("Missing key for value \"%s\"", key));
					if (value.contentEquals(""))
						throw new IllegalArgumentException(String.format("Missing value for key \"%s\"", value));

					argMap.put(key, value);

				} else {
					// could handle that do not need it...
					throw new IllegalArgumentException(String.format("\"%s\" is not a valid argument.", args[i]));
				}

			}

		}

		public String getString(String key, String defaultValue) throws ParameterParseException {

			String value = argMap.get(key);

			if (value != null)
				return value;
			else if (defaultValue != null)
				return defaultValue;

			throw new ParameterParseException(String.format("No default value specified for key \"%s\".", key));
		}

		public int getInteger(String key, Integer defaultValue) throws ParameterParseException {

			String value = argMap.get(key);

			if (value != null)
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new ParameterParseException(
							String.format("Value specified for key \"%s\" should be an Integer.", key));
				}
			else if (defaultValue != null)
				return defaultValue;

			throw new ParameterParseException(String.format("No default value specified for key \"%s\".", key));

		}

	}

	public static String helpMessage() {
		return HELP_MESSAGE;
	}

	public static String helpMessage(String message) {
		return String.format("%s%n%s", message, HELP_MESSAGE);
	}

	public static class ParameterParseException extends Exception {

		private static final long serialVersionUID = -6451504040323548262L;

		public ParameterParseException(String message) {
			super(message);
		}

	}

}
