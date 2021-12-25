package server.simplelogger;

import server.simplelogger.SimpleLogger.LogLevel;

public class ConsoleLogger extends BasicLogger {

	public ConsoleLogger() {
		super();
	}

	public ConsoleLogger(LogLevel logLevel) {
		super(logLevel);
	}

	public ConsoleLogger(LogLevel logLevel, String dateFormat) {
		super(logLevel, dateFormat);
	}

	@Override
	protected void writeLog(String logString) {
		System.out.println(logString);
	}

}
