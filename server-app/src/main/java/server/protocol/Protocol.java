package server.protocol;

import server.protocol.exceptions.ProtocolException.*;

public interface Protocol {

	void register(String email, String password) throws EmailAlreadyRegisteredException, PasswordRequirementNotMet;

	void login(String email, String password);

	Channel[] getPublicGroups(String email, String password);

	void joinGroup(String email, String password);

	Channel[] getChannels(String email, String password);

	User[] getChannelMembers(String email, String password);

	User getUser(String email, String password);
	
	void addFriend();
	
	User[] getFriends();
	
	void sendMessage();
	
	Message[] receiveMessages();
	
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
		RECEIVEMESSAGES
		
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
	
}
