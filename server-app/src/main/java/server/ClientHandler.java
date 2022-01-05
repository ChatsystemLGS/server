package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import server.simplelogger.SimpleLogger;
import server.simplelogger.SimpleLogger.LogLevel;

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

		SimpleLogger.logf(LogLevel.DEBUG, "client [%s]:%s(%s) : connected", client.getInetAddress(), client.getPort(),
				session.getState());
	}

	@Override
	public void run() {
		try {
			writeLine(session.greet());
			while (session.getState() != Session.State.DISCONNECTED) {
				writeLine(session.execute(readLine()));
			}
		} catch (NoSuchElementException e) { // caused (by timeout, when underlying InputStream gets closed?) when
												// Scanner has no more lines
			session.disconnect();
		} finally {
			SimpleLogger.logf(LogLevel.DEBUG, "client [%s]:%s : disconnected", client.getInetAddress(),
					client.getPort());
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String readLine() {
		String line = s.nextLine();
		SimpleLogger.logf(LogLevel.DEBUG, "client [%s]:%s(%s) > %s", client.getInetAddress(), client.getPort(),
				session.getState(), line);
		return line;
	}

	private void writeLine(String line) {
		SimpleLogger.logf(LogLevel.DEBUG, "client [%s]:%s(%s) < %s", client.getInetAddress(), client.getPort(),
				session.getState(), line);
		pw.println(line);
	}

	public static void createThread(Socket client, Server server) throws IOException {
		new Thread(new ClientHandler(client, server)).start();
	}

}
