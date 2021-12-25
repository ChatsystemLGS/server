package server.simplelogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import server.simplelogger.SimpleLogger.LogLevel;

public class FileLogger extends BasicLogger implements AutoCloseable {

	private static final LogType DEFAULT_LOG_TYPE = LogType.DO_NOT_OVERWRITE;

	private final File logFile;
	private final BufferedWriter bw;

	public FileLogger(File logFile) throws IllegalArgumentException, IOException {
		this(logFile, DEFAULT_LOG_TYPE);
	}

	public FileLogger(File logFile, LogType logType) throws IllegalArgumentException, IOException {
		super();
		this.logFile = logFile;

		bw = initBufferedWriter(logType);
	}

	public FileLogger(File logFile, LogLevel logLevel) throws IllegalArgumentException, IOException {
		this(logFile, DEFAULT_LOG_TYPE, logLevel);
	}

	public FileLogger(File logFile, LogType logType, LogLevel logLevel) throws IllegalArgumentException, IOException {
		super(logLevel);
		this.logFile = logFile;

		bw = initBufferedWriter(logType);
	}

	public FileLogger(File logFile, LogLevel logLevel, String dateFormat) throws IllegalArgumentException, IOException {
		this(logFile, DEFAULT_LOG_TYPE);
	}

	public FileLogger(File logFile, LogType logType, LogLevel logLevel, String dateFormat)
			throws IllegalArgumentException, IOException {
		super(logLevel, dateFormat);
		this.logFile = logFile;

		bw = initBufferedWriter(logType);
	}

	private BufferedWriter initBufferedWriter(LogType logType) throws IllegalArgumentException, IOException {

		return switch (logType) {
		case APPEND -> {
			yield new BufferedWriter(new FileWriter(logFile, true));
		}
		case OVERWRITE -> {
			yield new BufferedWriter(new FileWriter(logFile));
		}
		case DO_NOT_OVERWRITE -> {
			if (!logFile.exists())
				throw new IllegalArgumentException(String.format("File %s already exists.", logFile));
			yield new BufferedWriter(new FileWriter(logFile, false));
		}
		};

	}

	@Override
	protected void writeLog(String logString) {
		try {
			bw.write(logString);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws Exception {
		bw.close();
		System.out.println("hey");
	}

	public enum LogType {
		APPEND, OVERWRITE, DO_NOT_OVERWRITE; // RENAME latest.log -> {date}.log
	}

}
