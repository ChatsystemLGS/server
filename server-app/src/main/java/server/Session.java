package server;

import java.sql.Date;

import java.util.Arrays;

import server.db.Channel;
import server.db.Message;
import server.db.User;
import server.protocol.Protocol;
import server.protocol.ProtocolException;
import server.protocol.ProtocolException.ChannelNotFoundException;
import server.protocol.ProtocolException.DmAlreadyExistsException;
import server.protocol.ProtocolException.EmailAlreadyRegisteredException;
import server.protocol.ProtocolException.EmailNotRegisteredException;
import server.protocol.ProtocolException.InvalidParameterException;
import server.protocol.ProtocolException.MessageTooLongException;
import server.protocol.ProtocolException.NotMemberOfChannelException;
import server.protocol.ProtocolException.PasswordInvalidException;
import server.protocol.ProtocolException.PasswordRequirementNotMetException;
import server.protocol.ProtocolException.TooManyMessagesException;
import server.protocol.ProtocolException.UserNotFoundException;

public class Session implements Protocol {

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
			cmd = Command.valueOf(args[0]);
		} catch (IllegalArgumentException e) {
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
					addFriend(getInt(args, 1));
				} catch (InvalidParameterException e) {
					addFriend(args[1]);
				}
				yield response();
			}
			case GETCHANNELMEMBERS -> {
				User[] users = getChannelMembers(getInt(args, 1));
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
				User user;
				try {
					user = getUser(getInt(args, 1));
				} catch (InvalidParameterException e) {
					user = getUser(args[1]);
				}
				yield response(user);
			}
			case JOINGROUP -> {
				joinGroup(getInt(args, 1));
				yield response();
			}
			case LOGIN -> {
				login(args[1], args[2]);
				yield response();
			}
			case QUIT -> {
				quit();
				yield response();
			}
			case RECEIVEMESSAGES -> {
				Message[] messages = receiveMessages(Integer.parseInt(args[1]), getDate(args, 2), getDate(args, 3));
				yield response(messages);
			}
			case REGISTER -> {
				register(args[1], args[2]);
				yield response();
			}
			case CREATEDM -> {
				int channelId = createDm(getInt(args, 1));
				yield response(channelId);
			}
			case SENDMESSAGE -> {
				DataType dataType = getEnum(args, 3, DataType.class);
				sendMessage(getInt(args, 1), args[2], dataType);
				yield response();
			}
			};

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

	private <T extends Enum<T>> T getEnum(String[] args, int i, Class<T> enumType) throws InvalidParameterException {
		try {
			return Enum.valueOf(enumType, args[i]);
		} catch (NumberFormatException e) {
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

	@Override
	public void register(String email, String password)
			throws EmailAlreadyRegisteredException, PasswordRequirementNotMetException {
		server.DBC.addUser(new User(email, password));
	}

	@Override
	public void login(String email, String password) throws EmailNotRegisteredException, PasswordInvalidException {
		user = server.DBC.getUser(new User(email, password));
		state = State.AUTHENTICATED;
	}

	@Override
	public Channel[] getPublicGroups() {
		return server.DBC.getPublicGroups(user);
	}

	@Override
	public void joinGroup(int channelId) throws ChannelNotFoundException { // ChannelNotPublicException?
		server.DBC.joinGroup(user, new Channel(channelId));
	}

	@Override
	public Channel[] getChannels() {
		return server.DBC.getChannels(user);
	}

	@Override
	public User[] getChannelMembers(int channelId) throws NotMemberOfChannelException { // ChannelNotFoundException?
		return server.DBC.getChannelMembers(new Channel(channelId));
	}

	@Override
	public User getUser(int userId) throws UserNotFoundException {
		return server.DBC.getUser(new User(userId));
	}

	@Override
	public User getUser(String email) throws UserNotFoundException {
		return server.DBC.getUser(new User(email));
	}

	@Override
	public void addFriend(int userId) throws UserNotFoundException {
		server.DBC.addFriend(user, new User(userId));
	}

	@Override
	public void addFriend(String email) throws UserNotFoundException {
		server.DBC.addFriend(user, new User(email));
	}

	@Override
	public User[] getFriends() {
		return server.DBC.getFriends(user);
	}

	@Override
	public void sendMessage(int channelId, String data, DataType dataType)
			throws NotMemberOfChannelException, MessageTooLongException {
		server.DBC.sendMessage(user, new Channel(channelId), new Message(data, dataType));
	}

	@Override
	public int createDm(int userId) throws UserNotFoundException, DmAlreadyExistsException {
		return server.DBC.createDm(user, new User(userId));
	}

	@Override
	public Message[] receiveMessages(int channelId, Date tFrom, Date tUntil)
			throws NotMemberOfChannelException, TooManyMessagesException {
		return server.DBC.receiveMessages(user, new Channel(channelId), tFrom, tUntil);
	}

	@Override
	public void quit() {
		state = State.DISCONNECTED;
	}

	public String greet() {
		return response(Status.OK, PROTOCOL_VERSION);
	}

}
