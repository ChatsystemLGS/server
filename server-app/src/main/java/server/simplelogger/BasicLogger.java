package server.simplelogger;

import java.text.SimpleDateFormat;
import java.util.Date;

import server.simplelogger.SimpleLogger.LogLevel;
import server.simplelogger.SimpleLogger.LogListener;

public abstract class BasicLogger implements LogListener {

	private static final LogLevel DEFAULT_LOGLEVEL = LogLevel.INFO;
	private static final String DEFAULT_DATE_FORMAT = "dd-mm-yy-hh:mm:ss";

	private final LogLevel logLevel;
	private final SimpleDateFormat dateFormat;

	BasicLogger() {
		this(DEFAULT_LOGLEVEL);
	}

	BasicLogger(LogLevel logLevel) {
		this(logLevel, DEFAULT_DATE_FORMAT);
	}

	BasicLogger(LogLevel logLevel, String dateFormat) {
		this.logLevel = logLevel;
		this.dateFormat = new SimpleDateFormat(dateFormat);
		SimpleLogger.addLogListener(this);
	}

	@Override
	public void log(String message, LogLevel logLevel) {
		if (logLevel.ordinal() >= this.logLevel.ordinal())
			writeLog(logString(message, logLevel));
	}

	private String logString(String s, LogLevel logLevel) {
		return String.format("%s - [%s]: %s", logLevel, dateFormat.format(new Date()), s);
	}

	protected abstract void writeLog(String logString);

}
