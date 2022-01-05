package server.db;

import java.sql.Date;

public class DatabaseConnector {

	private final String connectionUrl;

	public DatabaseConnector(String dbHost, int dbPort, String dbTable, String dbUser, String dbPassword) {
		connectionUrl = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", dbHost, dbPort, dbTable, dbUser, dbPassword);
	}

	public void addUser(User user) {
		// TODO Auto-generated method stub

	}

	public int checkAuth(User user) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Channel[] getPublicGroups(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void joinGroup(User user, Channel channel) {
		// TODO Auto-generated method stub

	}

	public Channel[] getChannels(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	public User[] getChannelMembers(Channel channel) {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addFriend(User use, User friend) {
		// TODO Auto-generated method stub

	}

	public User[] getFriends(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendMessage(User user, Channel channel, Message message) {
		// TODO Auto-generated method stub

	}

	public int createDm(User user, User userB) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Message[] receiveMessages(User user, Channel channel, Date tFrom, Date tUntil) {
		// TODO Auto-generated method stub
		return null;
	}

}
