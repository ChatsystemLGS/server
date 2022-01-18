package server;

import java.io.File;
import java.io.IOException;

import server.config.Config;
import server.config.Config.ParameterParseException;
import server.simplelogger.ConsoleLogger;
import server.simplelogger.FileLogger;
import server.simplelogger.FileLogger.LogType;
import server.simplelogger.SimpleLogger;
import server.simplelogger.SimpleLogger.LogLevel;

public class App {

	@SuppressWarnings("unused") // will later be used for console stuff
	private Server server;

	public static void main(String[] args) {

		// setup logging
		try {
			FileLogger fileLogger = new FileLogger(new File("./latest.log"), LogType.APPEND, LogLevel.DEBUG);
			SimpleLogger.addLogListener(fileLogger);

			ConsoleLogger consoleLogger = new ConsoleLogger(LogLevel.DEBUG);
			SimpleLogger.addLogListener(consoleLogger);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Config cfg;

		try {
			cfg = Config.createFromArgs(args, Config.DEFAULT_CONFIG);
			new App(cfg);
		} catch (ParameterParseException e) {
			System.out.println(Config.helpMessage(e.getMessage()));
		}
		
	}

	private App(Config cfg) {

		server = new Server(cfg);

	}

}
