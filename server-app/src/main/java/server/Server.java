package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

import server.config.Config;
import server.db.DatabaseConnector;
import server.simplelogger.SimpleLogger;
import server.simplelogger.SimpleLogger.LogLevel;

public class Server {

	public final Config CFG;
	public final DatabaseConnector DBC;

	Server(Config cfg) {

		CFG = cfg;
		DBC = new DatabaseConnector(cfg.DB_HOST, cfg.DB_PORT,cfg.DB_TABLE, cfg.DB_USER, cfg.DB_PASSWORD);

		try (ServerSocket server = new ServerSocket(cfg.PORT)) {
			SimpleLogger.logf(LogLevel.INFO, "Listening on port %s", cfg.PORT);
			while (true) {
				ClientHandler.createThread(server.accept(), this);
			}
		} catch (BindException e) {
			SimpleLogger.logf(LogLevel.ERROR, "Could not bind. Port %s already in use", cfg.PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
