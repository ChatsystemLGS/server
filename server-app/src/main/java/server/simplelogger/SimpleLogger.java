package server.simplelogger;

import java.util.HashSet;
import java.util.Locale;

public final class SimpleLogger {

	private static HashSet<LogListener> logListeners = new HashSet<>();

	// prevent instantiation
	private SimpleLogger() {

	}

	private static void log(LogLevel logLevel, String message) {
		for (LogListener logListener : logListeners) {
			logListener.log(message, logLevel);
		}
	}

	public static void logf(LogLevel logLevel, String format, Object... args) {
		log(logLevel, String.format(format, args));
	}

	public static void logf(LogLevel logLevel, Locale l, String format, Object... args) {
		log(logLevel, String.format(l, format, args));
	}

	public static void addLogListener(LogListener listener) {
		logListeners.add(listener);
	}

	public static void removeLogListener(LogListener listener) {
		logListeners.remove(listener);
	}

	public enum LogLevel {
		DEBUG, INFO, WARN, ERROR;
	}

	public interface LogListener {

		public abstract void log(String message, LogLevel logLevel);

	}

}