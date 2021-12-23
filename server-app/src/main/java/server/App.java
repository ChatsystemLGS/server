package server;

import server.config.Config;

public class App {
	
	@SuppressWarnings("unused") // will later be used for console stuff
	private Server server;

	public static void main(String[] args) {

		new App(Config.createFromArgs(args, Config.DEFAULT_CONFIG));

	}

	private App(Config cfg) {

		server = new Server(cfg);

	}

}
