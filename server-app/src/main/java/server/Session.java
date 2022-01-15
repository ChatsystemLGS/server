package server;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import server.ProtocolException.InternalServerErrorException;
import server.ProtocolException.InvalidParameterException;
import server.ProtocolException.Status;
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

			try {

				return switch (cmd) {
				case ADDFRIEND -> {
//					try {
					server.DBC.addFriendById(user, new User().withId(getInt(args, 1)));

//					} catch (InvalidParameterException e) {
//						server.DBC.addFriendByEmail(user, new User().withEmailAddress(args[1]));
//					}
					yield response();
				}
				case GETCHANNELMEMBERS -> {

					User[] users = server.DBC.getChannelMembers(user, new Channel().withId(getInt(args, 1)));

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
						user = server.DBC.getUserById(this.user, new User().withId(getInt(args, 1)));
					} catch (InvalidParameterException e) {
						user = server.DBC.getUserByEmail(this.user, new User().withEmailAddress(args[1]));
					}

					yield response(user);
				}
				case JOINGROUP -> {
					server.DBC.joinGroup(user, new Channel().withId(getInt(args, 1)));

					yield response();

				}
				case LOGIN -> {
					user = server.DBC.login(new User().withEmailAddress(getBase64String(args, 1))
							.withPassword(getBase64String(args, 2)));
					state = State.AUTHENTICATED;

					yield response();

				}
				case QUIT -> {
					state = State.DISCONNECTED;

					yield response();
				}
				case RECEIVEMESSAGES -> {

					Message[] messages = server.DBC.receiveMessages(user, new Channel().withId(getInt(args, 1)),
							getTimestamp(args, 2), getTimestamp(args, 3));

					yield response(messages);
				}
				case REGISTER -> {
					server.DBC.addUser(new User().withEmailAddress(getBase64String(args, 1))
							.withNickname(getBase64String(args, 2)).withPassword(getBase64String(args, 3)));

					yield response();
				}
				case CREATEDM -> {
					server.DBC.createDm(user, new User().withId(getInt(args, 1)));

					yield response();
				}
				case SENDMESSAGE -> {

					DataType dataType = getEnum(args, 3, DataType.class);
					server.DBC.sendMessage(user, new Channel().withId(getInt(args, 1)),
							new Message().withData(getBase64Bytes(args, 2)).withDataType(dataType)
									.withTimestamp(Timestamp.from(Instant.now())));

					yield response();
				}
				};

			} catch (SQLException e) {
				e.printStackTrace();
				throw new InternalServerErrorException();
			}

		} catch (InternalServerErrorException e) {
			disconnect();
			return response(e);
		} catch (ProtocolException e) {
			return response(e);
		}

	}

	// convert string at some index in args to specified enum if possible; else
	// throw exception
	private <T extends Enum<T>> T getEnum(String[] args, int i, Class<T> enumType) throws InvalidParameterException {
		try {
			return Enum.valueOf(enumType, args[i]);
		} catch (IllegalArgumentException e) {
			throw new InvalidParameterException(i, ArgType.STRING_ENUM);
		}
	}

	private int getInt(String[] args, int i) throws InvalidParameterException {
		try {
			return Integer.parseInt(args[i]);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException(i, ArgType.INTEGER);
		}
	}

	private Timestamp getTimestamp(String[] args, int i) throws InvalidParameterException {
		try {
			return new Timestamp(getInt(args, i));
		} catch (IllegalArgumentException e) {
			throw new InvalidParameterException(i, ArgType.INTEGER_TIMESTAMP);
		}
	}

	private String getBase64String(String[] args, int i) throws InvalidParameterException {
		try {
			return new String(TransmittableObject.fromBase64String(args[i]));
		} catch (IllegalArgumentException e) {
			throw new InvalidParameterException(i, ArgType.STRING);
		}
	}

	private byte[] getBase64Bytes(String[] args, int i) throws InvalidParameterException {
		try {
			return TransmittableObject.fromBase64String(args[i]);
		} catch (IllegalArgumentException e) {
			throw new InvalidParameterException(i, ArgType.STRING_DATA);
		}
	}

	private String response(Status status, Object obj, TransmittableObject object, TransmittableObject[] objectList) {

		String s = status.toString();

		if (obj != null)
			s += " " + obj.toString();

		if (object != null)
			s += " " + object.transmittableString();

		if (objectList != null)
			s += " " + TransmittableObject.toString(objectList);

		return s;
	}

	private String response(TransmittableObject retVal) {
		return response(Status.OK, null, retVal, null);
	}

	private String response(Status status, Object retVal) {
		return response(status, retVal, null, null);
	}

	private String response(Status status) {
		return response(status, null, null, null);
	}

	private String response(TransmittableObject[] retVal) {
		return response(Status.OK, null, null, retVal);
	}

	private String response() {
		return response(Status.OK);
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
	
	enum ArgType {
		STRING, INTEGER, STRING_DATA, STRING_ENUM, INTEGER_TIMESTAMP
	}

}
