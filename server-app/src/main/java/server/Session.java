package server;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import server.ProtocolException.InternalServerErrorException;
import server.ProtocolException.InvalidParameterException;
import server.ProtocolException.NotEnoughParametersException;
import server.ProtocolException.Status;
import server.ProtocolException.TooManyParametersException;
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

		try {

			// check if amount of parameters matches command
			if (args.length - 1 < cmd.getMinNumArgs())
				throw new NotEnoughParametersException(cmd.getMinNumArgs());
			if (args.length - 1 > cmd.getMaxNumArgs())
				throw new TooManyParametersException(cmd.getMaxNumArgs());

			try {

				return switch (cmd) {
				case ADDFRIEND -> {

					int userId = getInt(args, 1);

					server.DBC.addFriendById(this.user.getId(), userId);

					yield response();
				}
				case GETCHANNELMEMBERS -> {

					int channelId = getInt(args, 1);

					User[] users = server.DBC.getChannelMembers(this.user.getId(), channelId);

					yield response(users);
				}
				case GETCHANNELS -> {

					Channel[] channels = server.DBC.getChannels(this.user.getId());

					yield response(channels);
				}
				case GETFRIENDS -> {

					User[] users = server.DBC.getFriends(this.user.getId());

					yield response(users);
				}
				case GETPUBLICGROUPS -> {

					Channel[] channels = server.DBC.getPublicGroups();

					yield response(channels);
				}
				case GETUSER -> {

					User user;

					try {
						int userId = getInt(args, 1);
						user = server.DBC.getUserById(this.user.getId(), userId);
					} catch (InvalidParameterException e) { // TODO could this catch an exception thrown by
															// getUserById...?
						String emailAddress = getBase64String(args, 1);
						user = server.DBC.getUserByEmail(this.user.getId(), emailAddress);
					}

					yield response(user);
				}
				case JOINGROUP -> {

					int channelId = getInt(args, 1);

					server.DBC.joinGroup(this.user.getId(), channelId);

					yield response();

				}
				case LOGIN -> {

					String emailAddress = getBase64String(args, 1);
					String passwordHash = User.hashPassword(getBase64String(args, 2));

					user = server.DBC.login(emailAddress, passwordHash);

					state = State.AUTHENTICATED;

					yield response(Status.OK, user.getId());

				}
				case QUIT -> {

					state = State.DISCONNECTED;

					yield response();
				}
				case RECEIVEMESSAGES -> {

					int channelId = getInt(args, 1);
					Timestamp tFrom = getTimestamp(args, 2);
					Timestamp tUntil = getTimestamp(args, 3);

					Message[] messages = server.DBC.receiveMessages(this.user.getId(), channelId, tFrom, tUntil);

					yield response(messages);
				}
				case REGISTER -> {

					String emailAddress = getBase64String(args, 1);
					String nickname = getBase64String(args, 2);
					String passwordHash = User.hashPassword(getBase64String(args, 3));

					server.DBC.addUser(emailAddress, nickname, passwordHash);

					yield response();
				}
				case CREATEDM -> {

					int userId = getInt(args, 1);

					server.DBC.createDm(this.user.getId(), userId);

					yield response();
				}
				case SENDMESSAGE -> {

					int channelId = getInt(args, 1);
					byte[] data = getBase64Bytes(args, 2);
					DataType dataType = getEnum(args, 3, DataType.class);
					Timestamp timestamp = Timestamp.from(Instant.now());

					server.DBC.sendMessage(this.user.getId(), channelId, timestamp, data, dataType);

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

	private String response(ProtocolException e) {
		return e.transmittableString();
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

}
