package server;

import java.util.Arrays;
import java.util.Date;

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
				Status status = Status.OK;
				try {
					addFriend(args[1]);
				} catch (ProtocolException e) {
					status = e.getStatus();
				}
				yield response(status);
			}
			case GETCHANNELMEMBERS -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case GETCHANNELS -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case GETFRIENDS -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case GETPUBLICGROUPS -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case GETUSER -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case JOINGROUP -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case LOGIN -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case QUIT -> {
				quit();
				yield response(Status.OK);
			}
			case RECEIVEMESSAGES -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case REGISTER -> {
				Status status = Status.OK;

				try {
					register(args[1], args[2]);
				} catch (ProtocolException e) {
					status = e.getStatus();
				}
				yield response(status);
			}
			case SENDDM -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			case SENDMESSAGE -> {
				// TODO
				yield "NOT YET IMPLEMENTED";
			}
			};
		} catch (ArrayIndexOutOfBoundsException e) {
			return response(Status.NOT_ENOUGH_PARAMETERS);
		}

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
	public User[] getFriends(int channelID, Byte[] data, DataType dataType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(int channelID, Byte[] data, DataType dataType) throws ProtocolException {
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
