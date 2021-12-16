package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {

	Socket client;
	Scanner s;
	PrintWriter pw;
	
	Session session;
	
	private ClientHandler(Socket client, App server) throws IOException {
		this.client = client;
		
		s = new Scanner(client.getInputStream());
		pw = new PrintWriter(client.getOutputStream());
		
		session = new Session(server);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public static void createThread(Socket client, App server) throws IOException {
		new Thread(new ClientHandler(client, server)).start();
	}

	
	
}
