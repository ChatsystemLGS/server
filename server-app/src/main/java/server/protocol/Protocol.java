package server.protocol;

import java.sql.Date;

import server.protocol.exceptions.ProtocolException.ChannelNotFoundException;
import server.protocol.exceptions.ProtocolException.EmailAlreadyRegisteredException;
import server.protocol.exceptions.ProtocolException.EmailNotRegisteredException;
import server.protocol.exceptions.ProtocolException.MessageTooLongException;
import server.protocol.exceptions.ProtocolException.NotMemberOfChannelException;
import server.protocol.exceptions.ProtocolException.PasswordInvalidException;
import server.protocol.exceptions.ProtocolException.PasswordRequirementNotMetException;
import server.protocol.exceptions.ProtocolException.TooManyMessagesException;

public interface Protocol {
	
	static final String PROTOCOL_VERSION = "0.0.0";

	void register(String email, String password) throws EmailAlreadyRegisteredException, PasswordRequirementNotMetException;

	void login(String email, String password) throws EmailNotRegisteredException, PasswordInvalidException;

	Channel[] getPublicGroups();

	void joinGroup(int channelID) throws ChannelNotFoundException;

	Channel[] getChannels();

	User[] getChannelMembers(int channelID) throws NotMemberOfChannelException;

	User getUser(String email) throws EmailNotRegisteredException;
	
	void addFriend(String email) throws EmailNotRegisteredException;
	
	User[] getFriends();
	
	void sendMessage(int channelID, String data, DataType dataType) throws EmailNotRegisteredException, MessageTooLongException;
	
	Message[] receiveMessages(int channelID, Date tFrom, Date tUntil) throws NotMemberOfChannelException, TooManyMessagesException;
	
	void quit();

	enum State {
		
		CONNECTED,
		AUTHENTICATED,
		DISCONNECTED;
		
	}
	
	enum Command {
		
		REGISTER,
		LOGIN,
		GETPUBLICGROUPS,
		JOINGROUP,
		GETCHANNELS,
		GETCHANNELMEMBERS,
		GETUSER,
		ADDFRIEND,
		GETFRIENDS,
		SENDMESSAGE,
		SENDDM,
		RECEIVEMESSAGES,
		QUIT
		
	}
	

	enum Status {
		
		OK,
		NOT_ENOUGH_PARAMETERS,
		TOO_MANY_PARAMETERS,
		INVALID_PARAMETER,
		EMAIL_ALREADY_REGISTERED,
		PASSWORD_REQ_NOT_MET,
		EMAIL_NOT_REGISTERED,
		PASSWORD_INVALID,
		NOT_MEMBER_OF_CHANNEL,
		MESSAGE_TOO_LONG,
		TOO_MANY_MESSAGES,
		CHANNEL_NOT_FOUND,
		DM_ALREADY_EXISTING,
		COMMAND_NOT_FOUND;
		
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
		FILE_PDF
		
	}
	
}
