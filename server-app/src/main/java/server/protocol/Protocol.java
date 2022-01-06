package server.protocol;

import java.sql.Date;

import server.db.Channel;
import server.db.Message;
import server.db.User;
import server.protocol.ProtocolException.ChannelNotFoundException;
import server.protocol.ProtocolException.DmAlreadyExistsException;
import server.protocol.ProtocolException.EmailAlreadyRegisteredException;
import server.protocol.ProtocolException.EmailNotRegisteredException;
import server.protocol.ProtocolException.InternalServerErrorException;
import server.protocol.ProtocolException.MessageTooLongException;
import server.protocol.ProtocolException.NotMemberOfChannelException;
import server.protocol.ProtocolException.PasswordInvalidException;
import server.protocol.ProtocolException.PasswordRequirementNotMetException;
import server.protocol.ProtocolException.TooManyMessagesException;
import server.protocol.ProtocolException.UserNotFoundException;

public interface Protocol {

	static final String PROTOCOL_VERSION = "0.0.0";

	void register(String emailAddress, String nickname, String password)
			throws InternalServerErrorException, EmailAlreadyRegisteredException, PasswordRequirementNotMetException;

	void login(String emailAddress, String password) throws InternalServerErrorException, EmailNotRegisteredException, PasswordInvalidException;

	Channel[] getPublicGroups() throws InternalServerErrorException;

	void joinGroup(int channelId) throws InternalServerErrorException, ChannelNotFoundException;

	Channel[] getChannels() throws InternalServerErrorException;

	User[] getChannelMembers(int channelId) throws InternalServerErrorException, NotMemberOfChannelException;

	User getUser(int id) throws UserNotFoundException;

	User getUser(String emailAddress) throws UserNotFoundException;

	void addFriend(int id) throws UserNotFoundException;

	void addFriend(String emailAddress) throws UserNotFoundException;

	User[] getFriends();

	void sendMessage(int channelId, String data, DataType dataType)
			throws NotMemberOfChannelException, MessageTooLongException;

	int createDm(int id) throws UserNotFoundException, DmAlreadyExistsException;

	Message[] receiveMessages(int channelId, Date tFrom, Date tUntil)
			throws NotMemberOfChannelException, TooManyMessagesException;

	void quit();

	enum State {

		CONNECTED(null),
		AUTHENTICATED(State.CONNECTED),
		DISCONNECTED(State.CONNECTED);

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

		REGISTER(State.CONNECTED, 3),
		LOGIN(State.CONNECTED, 2),
		GETPUBLICGROUPS(State.AUTHENTICATED, 0),
		JOINGROUP(State.AUTHENTICATED, 1),
		GETCHANNELS(State.AUTHENTICATED, 0),
		GETCHANNELMEMBERS(State.AUTHENTICATED, 1),
		GETUSER(State.AUTHENTICATED, 1),
		ADDFRIEND(State.AUTHENTICATED, 1),
		GETFRIENDS(State.AUTHENTICATED, 0),
		SENDMESSAGE(State.AUTHENTICATED, 3),
		CREATEDM(State.AUTHENTICATED, 1),
		RECEIVEMESSAGES(State.AUTHENTICATED, 3),
		QUIT(State.CONNECTED, 0);

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

	enum Status {

		OK, NOT_ENOUGH_PARAMETERS,
		TOO_MANY_PARAMETERS,
		INVALID_PARAMETER,
		COMMAND_NOT_FOUND,
		INTERNAL_SERVER_ERROR,
		AUTHENTICATION_REQUIRED,
		EMAIL_ALREADY_REGISTERED,
		PASSWORD_REQ_NOT_MET,
		EMAIL_NOT_REGISTERED,
		PASSWORD_INVALID,
		NOT_MEMBER_OF_CHANNEL,
		MESSAGE_TOO_LONG,
		TOO_MANY_MESSAGES,
		CHANNEL_NOT_FOUND,
		USER_NOT_FOUND,
		DM_ALREADY_EXISTS;

		@Override
		public String toString() {
			return this.name();
		}

	}

	enum DataType {

		TEXT,
		FILE_TXT,
		FILE_PNG,
		FILE_GIF,
		FILE_PDF;

	}

}
