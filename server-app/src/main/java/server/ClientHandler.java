package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler implements Runnable {

	Socket client;
	Scanner s;
	PrintWriter pw;

	Session session;

	private ClientHandler(Socket client, Server server) throws IOException {
		this.client = client;

		s = new Scanner(client.getInputStream());
		pw = new PrintWriter(client.getOutputStream(), true);

		session = new Session(server);
	}

	@Override
	public void run() {
		try {
			pw.println(session.greet());
			while (session.getState() != Session.State.DISCONNECTED) {
				pw.println(session.execute(s.nextLine()));
			}
		} catch (NoSuchElementException e) { // caused (by timeout, when underlying InputStream gets closed?) when
												// Scanner has no more lines
			session.disconnect();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void createThread(Socket client, Server server) throws IOException {
		new Thread(new ClientHandler(client, server)).start();
	}

}
