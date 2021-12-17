package server;

import java.io.IOException;
import java.net.ServerSocket;

import server.config.Config;
import server.db.DatabaseConnector;

public class Server {
	
	public final Config CFG;
	public final DatabaseConnector DBC;

	Server(Config cfg) {

		CFG = cfg;
		DBC = new DatabaseConnector(cfg.DB_HOST, cfg.DB_PORT);

		System.out.println(String.format("Listening on port %s...", cfg.PORT));
		try (ServerSocket server = new ServerSocket(cfg.PORT)) {
			while (true) {
				ClientHandler.createThread(server.accept(), this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
