package server.config;

import java.io.File;

public class Config {

	public final int MAX_MESSAGE_LENGTH;
	public final int PORT;
	public final String DB_HOST;
	public final int DB_PORT;

	public static final Config DEFAULT_CONFIG = new Config(Integer.MAX_VALUE, 1465, "localhost", 3306);

	public Config(int maxMessageLength, int port, String dbHost, int dbPort) {

		MAX_MESSAGE_LENGTH = maxMessageLength;
		this.PORT = port;
		this.DB_HOST = dbHost;
		this.DB_PORT = dbPort;

	}

	public static Config createFromArgs(String[] args, Config defaultValues) {

		ArgMap parameters = new ArgMap(args);

		int port;
		String dbHost;
		int dbPort;

		try {
			port = parameters.getInteger("port", defaultValues.PORT);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}

		dbHost = parameters.getString("dbHost", defaultValues.DB_HOST);

		try {
			dbPort = parameters.getInteger("dbPort", defaultValues.DB_PORT);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}

		return new Config(DEFAULT_CONFIG.MAX_MESSAGE_LENGTH, port, dbHost, dbPort);
	}

	public static Config loadCfg(File f) {
		// TODO
		return null;
	}

	public static void WriteCfg(File f, Config cfg) {
		// TODO
	}

}
