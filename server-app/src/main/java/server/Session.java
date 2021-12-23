package server;

import java.sql.Date;
import java.util.Arrays;

import server.protocol.Channel;
import server.protocol.Message;
import server.protocol.Protocol;
import server.protocol.User;
import server.protocol.exceptions.ProtocolException;

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

		String[] args = line.split(" ");

		Command cmd;
		try {
			cmd = Command.valueOf(args[0]);
		} catch (IllegalArgumentException e) {
			return response(Status.COMMAND_NOT_FOUND);
		}

		try {
			return switch (cmd) {
			case ADDFRIEND -> {
				addFriend(args[1]);
				yield response(Status.OK);
			}
			case GETCHANNELMEMBERS -> {
				User[] users = getChannelMembers(Integer.parseInt(args[1]));
				yield response(users);
			}
			case GETCHANNELS -> {
				Channel[] channels = getChannels();
				yield response(channels);
			}
			case GETFRIENDS -> {
				User[] users = getFriends();
				yield response(users);
			}
			case GETPUBLICGROUPS -> {
				Channel[] channels = getPublicGroups();
				yield response(channels);
			}
			case GETUSER -> {
				User user = getUser(args[1]);
				yield response(user);
			}
			case JOINGROUP -> {
				joinGroup(Integer.parseInt(args[1]));
				yield response();
			}
			case LOGIN -> {
				login(args[1], args[2]);
				yield response();
			}
			case QUIT -> {
				quit();
				yield response(Status.OK);
			}
			case RECEIVEMESSAGES -> {

				// handle IllegalArgumetException
//				Date tFrom = Date.valueOf(args[2]);
//				Date tUntil = Date.valueOf(args[3]);
//
//				Message[] messages;
//
//				try {
//					messages = receiveMessages(Integer.parseInt(args[1]), tFrom, tUntil);
//				} catch (ProtocolException e) {
//					Status status = e.getStatus();
//					if (status == Status.TOO_MANY_MESSAGES) {
//						response(status, messages);
//					}
//				}
//				yield response(messages);
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case REGISTER -> {
				register(args[1], args[2]);
				yield response();
			}
			case SENDDM -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case SENDMESSAGE -> {
				sendMessage(Integer.parseInt(args[1]), args[2], DataType.valueOf(args[3]));
				yield response();
			}
			};
		} catch (ArrayIndexOutOfBoundsException e) {
			return response(Status.NOT_ENOUGH_PARAMETERS);
		} catch (ProtocolException e) {
			return response(e.getStatus());
		} catch (NumberFormatException e) {
			return response(Status.INVALID_PARAMETER);
		}

	}

	String response(Status status) {
		return String.format("%s", status);
	}

	String response() {
		return response(Status.OK);
	}

	String response(Status status, Object retVal) {
		return String.format("%s %s", status, retVal);
	}

	String response(Status status, Object[] retVal) {
		return String.format("%s %s", status, Arrays.toString(retVal));
	}

	String response(Object retVal) {
		return response(Status.OK, retVal);
	}

	String response(Object[] retVal) {
		return response(Status.OK, retVal);
	}

	public void disconnect() {
		state = State.DISCONNECTED;
	}

	public State getState() {
		return state;
	}

	@Override
	public void register(String email, String password) throws ProtocolException {
		// TODO Auto-generated method stub

	}

	@Override
	public void login(String email, String password) throws ProtocolException {
		// TODO Auto-generated method stub

	}

	@Override
	public Channel[] getPublicGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void joinGroup(int channelID) throws ProtocolException {
		// TODO Auto-generated method stub

	}

	@Override
	public Channel[] getChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User[] getChannelMembers(int channelID) throws ProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUser(String email) throws ProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addFriend(String email) throws ProtocolException {
		// TODO Auto-generated method stub
	}

	@Override
	public User[] getFriends() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(int channelID, String data, DataType dataType) throws ProtocolException {
		// TODO Auto-generated method stub

	}

	@Override
	public Message[] receiveMessages(int channelID, Date tFrom, Date tUntil) throws ProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void quit() {
		state = State.DISCONNECTED;
	}

	public String greet() {
		return response(Status.OK, PROTOCOL_VERSION);
	}

}
