package server;

import java.util.Arrays;

import server.protocol.Channel;
import server.protocol.Message;
import server.protocol.Protocol;
import server.protocol.User;
import server.protocol.exceptions.ProtocolException;
import server.protocol.exceptions.ProtocolException.EmailAlreadyRegisteredException;

public class Session implements Protocol {

	private State state;

	private final Server server;

	public Session(Server server) {
		this.server = server;
		state = State.CONNECTED;
	}

	public String execute(String line) {
		
		System.out.println(line);

		if (line.length() > server.CFG.MAX_MESSAGE_LENGTH)
			return response(Status.MESSAGE_TOO_LONG);

		String[] cmd = line.split(" ");

		if (cmd.length == 0)
			return response(Status.COMMAND_NOT_FOUND);

		try {
			switch (cmd[0]) {
			case "REGISTER": {

				Status status = Status.OK;

				try {
					register(cmd[1], cmd[2]);
				} catch (ProtocolException e) {
					status = e.getStatus();
				}

				return response(status);
			}
			// TODO
			case "QUIT": {
				quit();
				return response(Status.OK);
			}

			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return response(Status.NOT_ENOUGH_PARAMETERS);
		}

		return response(Status.COMMAND_NOT_FOUND);
	}

	String response(Status status, Object... retVal) {
		return String.format("%s %s", status, Arrays.toString(retVal));
	}

	public void disconnect() {
		state = State.DISCONNECTED;
	}

	public State getState() {
		return state;
	}

	@Override
	public void register(String email, String password) throws EmailAlreadyRegisteredException {
		// TODO Auto-generated method stub
	}

	@Override
	public void login(String email, String password) {
		// TODO Auto-generated method stub
	}

	@Override
	public Channel[] getPublicGroups(String email, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void joinGroup(String email, String password) {
		// TODO Auto-generated method stub

	}

	@Override
	public Channel[] getChannels(String email, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User[] getChannelMembers(String email, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUser(String email, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addFriend() {
		// TODO Auto-generated method stub

	}

	@Override
	public User[] getFriends() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage() {
		// TODO Auto-generated method stub

	}

	@Override
	public Message[] receiveMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void quit() {
		state = State.DISCONNECTED;
	}

	public String greet() {
		return "Hello";
	}

}
