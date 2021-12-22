package server.protocol;

import java.util.Date;

import server.protocol.exceptions.ProtocolException;

public interface Protocol {
	
	static final String PROTOCOL_VERSION = "0.0.0";

	void register(String email, String password) throws ProtocolException;

	void login(String email, String password) throws ProtocolException;

	Channel[] getPublicGroups(String email, String password) throws ProtocolException;

	void joinGroup(String email, String password) throws ProtocolException;

	Channel[] getChannels(String email, String password) throws ProtocolException;

	User[] getChannelMembers(String email, String password) throws ProtocolException;

	User getUser(String email, String password) throws ProtocolException;
	
	void addFriend(String email) throws ProtocolException;
	
	User[] getFriends(int channelID, Byte[] data, DataType dataType) throws ProtocolException;
	
	void sendMessage(int channelID, Byte[] data, DataType dataType) throws ProtocolException;
	
	Message[] receiveMessages(int channelID, Date tFrom, Date tUntil) throws ProtocolException;
	
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
