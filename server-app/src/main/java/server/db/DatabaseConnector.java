package server.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import server.protocol.ProtocolException.EmailNotRegisteredException;
import server.protocol.ProtocolException.InternalServerErrorException;
import server.protocol.ProtocolException.PasswordInvalidException;
import server.simplelogger.SimpleLogger;
import server.simplelogger.SimpleLogger.LogLevel;

public class DatabaseConnector {

	private final String connectionUrl;

	public DatabaseConnector(String dbHost, int dbPort, String dbTable, String dbUser, String dbPassword) {
		connectionUrl = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", dbHost, dbPort, dbTable, dbUser,
				dbPassword);

		SimpleLogger.logf(LogLevel.DEBUG, "Instanciated DatabaseConnector with connectionUrl %s", connectionUrl);

		// TODO Check if connection can be established/get db version

		SimpleLogger.logf(LogLevel.INFO, "Connected to database %s:%s/%s", dbHost, dbPort, dbTable);
	}

	public void addUser(User user) {
		// TODO Auto-generated method stub

	}

	private boolean checkAuth(User user) throws SQLException, EmailNotRegisteredException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT (SELECT u.passwordHash FROM Users u WHERE u.emailAddress = ?) = ? passwordMatches");) {
			stmt.setString(1, user.getEmailAddress());
			stmt.setString(2, user.getPasswordHash());
			ResultSet rs = stmt.executeQuery();
			rs.first();
			boolean authenticated = rs.getBoolean("passwordMatches");
			if (rs.wasNull())
				throw new EmailNotRegisteredException();
			return authenticated;
		}
	}

	public User login(User user)
			throws InternalServerErrorException, PasswordInvalidException, EmailNotRegisteredException {

		try {
			if (!checkAuth(user))
				throw new PasswordInvalidException();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.emailAddress emailAddress, u.nickname nickname, ur.note note FROM Users u LEFT JOIN userRelationships ur ON ur.userA=ur.userB WHERE u.emailAddress=?");) {
			stmt.setString(1, user.getEmailAddress());
			ResultSet rs = stmt.executeQuery();
			rs.first();
			int id = rs.getInt("id");
			String email = rs.getString("emailAddress");
			String nickname = rs.getString("nickname");
			String note = rs.getString("note");

			return new User(id, email, nickname, note);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

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
