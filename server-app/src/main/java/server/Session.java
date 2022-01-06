package server;

import java.sql.Date;
import java.util.Arrays;

import server.ProtocolException.InternalServerErrorException;
import server.ProtocolException.InvalidParameterException;
import server.ProtocolException.MessageTooLongException;
import server.ProtocolException.Status;
import server.ProtocolException.TooManyMessagesException;
import server.db.Channel;
import server.db.Message;
import server.db.Message.DataType;
import server.db.User;

public class Session {

	static final String PROTOCOL_VERSION = "0.0.0";

	private final Server server;

	private State state;
	private User user;

	public Session(Server server) {
		this.server = server;
		state = State.CONNECTED;
	}

	public String execute(String line) {

		// replace with different limitation (MAX_MESSAGE_LENGTH refers to length of
		// data of message)
		if (line.length() > server.CFG.MAX_MESSAGE_LENGTH)
			return response(Status.MESSAGE_TOO_LONG);

		String[] args = line.split(" ");

		Command cmd;
		try {
			cmd = getEnum(args, 0, Command.class);
		} catch (InvalidParameterException e) {
			return response(Status.COMMAND_NOT_FOUND);
		}

		// check if state requirement is met (may have to rework this if more states get
		// added)
		if (!(cmd.getRequiredState() == state || state.hasParent(cmd.getRequiredState())))
			return response(Status.AUTHENTICATION_REQUIRED);

		// check if amount of parameters matches command
		if (args.length - 1 != cmd.getNumArgs())
			if (args.length - 1 < cmd.getNumArgs())
				return response(Status.NOT_ENOUGH_PARAMETERS, cmd.getNumArgs());
			else
				return response(Status.TOO_MANY_PARAMETERS, cmd.getNumArgs());

		try {

			return switch (cmd) {
			case ADDFRIEND -> {
				try {
					server.DBC.addFriend(user, new User(getInt(args, 1), null, null, null, null, null, null));
				} catch (InvalidParameterException e) {
					server.DBC.addFriend(user, new User(null, args[1], null, null, null, null, null));
				}
				yield response();
			}
			case GETCHANNELMEMBERS -> {
				User[] users = server.DBC.getChannelMembers(user, new Channel(getInt(args, 1), null, null));
				yield response(users);
			}
			case GETCHANNELS -> {
				Channel[] channels = server.DBC.getChannels(user);
				yield response(channels);
			}
			case GETFRIENDS -> {
				User[] users = server.DBC.getFriends(user);
				yield response(users);
			}
			case GETPUBLICGROUPS -> {
				Channel[] channels = server.DBC.getPublicGroups();
				yield response(channels);
			}
			case GETUSER -> {
				User user;
				try {
					user = server.DBC.getUser(new User(getInt(args, 1), null, null, null, null, null, null));
				} catch (InvalidParameterException e) {
					user = server.DBC.getUser(new User(null, args[1], null, null, null, null, null));
				}
				yield response(user);
			}
			case JOINGROUP -> {
				server.DBC.joinGroup(user, new Channel(getInt(args, 1), null, null));
				yield response();
			}
			case LOGIN -> {
				user = server.DBC.login(new User(null, args[1], null, args[2], null, null, null));
				state = State.AUTHENTICATED;
				yield response();
			}
			case QUIT -> {
				state = State.DISCONNECTED;
				yield response();
			}
			case RECEIVEMESSAGES -> {
				Message[] messages = server.DBC.receiveMessages(user,
						new Channel(Integer.parseInt(args[1]), null, null), getDate(args, 2), getDate(args, 3));
				yield response(messages);
			}
			case REGISTER -> {
				server.DBC.addUser(new User(null, args[1], args[2], args[3], null, null, null));
				yield response();
			}
			case CREATEDM -> {
				int channelId = server.DBC.createDm(user,
						new User(getInt(args, 1), null, null, null, null, null, null));
				yield response(channelId);
			}
			case SENDMESSAGE -> {
				DataType dataType = getEnum(args, 3, DataType.class);
				server.DBC.sendMessage(user, new Channel(getInt(args, 1), null, null), new Message(args[2], dataType));
				yield response();
			}
			};

		} catch (InternalServerErrorException e) {
			state = State.DISCONNECTED;
			return response(e.getStatus());
		} catch (MessageTooLongException e) {
			return response(e.getStatus(), e.getMaxMessageSize());
		} catch (TooManyMessagesException e) {
			return response(e.getStatus(), e.getLastMessageTime(), e.getMessages());
		} catch (InvalidParameterException e) {
			return response(e.getStatus(), e.getIndex());
		} catch (ProtocolException e) {
			return response(e.getStatus());
		}

	}

	// convert string at some index in args to specified enum if possible; else
	// throw exception
	private <T extends Enum<T>> T getEnum(String[] args, int i, Class<T> enumType) throws InvalidParameterException {
		try {
			return Enum.valueOf(enumType, args[i]);
		} catch (IllegalArgumentException e) {
			throw new InvalidParameterException(i);
		}
	}

	private int getInt(String[] args, int i) throws InvalidParameterException {
		try {
			return Integer.parseInt(args[i]);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException(i);
		}
	}

	private Date getDate(String[] args, int i) throws InvalidParameterException {
		try {
			return Date.valueOf(args[i]);
		} catch (IllegalArgumentException e) {
			throw new InvalidParameterException(i);
		}
	}

	// replace by response(Status status, Object...) -> iterate over array, call
	// Array.toString() if obj is array)...
	@SuppressWarnings("unused")
	private String response(Status status, Object retVal1, Message[] retVal2) {
		return String.format("%s %s %s", status, retVal1, Arrays.toString(retVal2));
	}

	private String response(Status status) {
		return String.format("%s", status);
	}

	private String response() {
		return response(Status.OK);
	}

	private String response(Status status, Object retVal) {
		return String.format("%s %s", status, retVal);
	}

	private String response(Status status, Object[] retVal) {
		return String.format("%s %s", status, Arrays.toString(retVal));
	}

	private String response(Object retVal) {
		return response(Status.OK, retVal);
	}

	private String response(Object[] retVal) {
		return response(Status.OK, retVal);
	}

	public void disconnect() {
		state = State.DISCONNECTED;
	}

	public State getState() {
		return state;
	}

	public String greet() {
		return response(Status.OK, PROTOCOL_VERSION);
	}

	enum State {

		CONNECTED(null), AUTHENTICATED(State.CONNECTED), DISCONNECTED(State.CONNECTED);

		private final State parentState;

		State(State parentState) {
			this.parentState = parentState;
		}

		// returns parent State or null if it is the default State
		public State getParentState() {
			return parentState;
		}

		public boolean hasParent(State state) {

			if (state == null)
				throw new IllegalArgumentException();

			if (parentState == null)
				return false;

			if (parentState == state)
				return true;
			else
				return parentState.hasParent(state);
		}

	}

	enum Command {

		REGISTER(State.CONNECTED, 3), LOGIN(State.CONNECTED, 2), GETPUBLICGROUPS(State.AUTHENTICATED, 0),
		JOINGROUP(State.AUTHENTICATED, 1), GETCHANNELS(State.AUTHENTICATED, 0),
		GETCHANNELMEMBERS(State.AUTHENTICATED, 1), GETUSER(State.AUTHENTICATED, 1), ADDFRIEND(State.AUTHENTICATED, 1),
		GETFRIENDS(State.AUTHENTICATED, 0), SENDMESSAGE(State.AUTHENTICATED, 3), CREATEDM(State.AUTHENTICATED, 1),
		RECEIVEMESSAGES(State.AUTHENTICATED, 3), QUIT(State.CONNECTED, 0);

		private final State requiredState;
		private final int numArgs;

		Command(State requiredState, int numArgs) {
			this.requiredState = requiredState;
			this.numArgs = numArgs;
		}

		public State getRequiredState() {
			return requiredState;
		}

		public int getNumArgs() {
			return numArgs;
		}

	}

}
