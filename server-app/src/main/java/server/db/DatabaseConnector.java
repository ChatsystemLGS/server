package server.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import server.db.Channel.ChannelType;
import server.protocol.ProtocolException.ChannelNotFoundException;
import server.protocol.ProtocolException.EmailAlreadyRegisteredException;
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

	public void addUser(User user) throws InternalServerErrorException, EmailAlreadyRegisteredException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"INSERT INTO Users (emailAddress, nickname, passwordHash) VALUES (?, ?, ?)")) {
			stmt.setString(1, user.getEmailAddress());
			stmt.setString(2, user.getNickname());
			stmt.setString(3, user.getPasswordHash());
			if (stmt.executeUpdate() == 0)
				throw new EmailAlreadyRegisteredException();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

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
						"SELECT u.id id, u.emailAddress emailAddress, u.nickname nickname, ur.note note FROM Users u LEFT JOIN userRelationships ur ON ur.userA=ur.userB WHERE u.emailAddress = ?");) {
			stmt.setString(1, user.getEmailAddress());
			ResultSet rs = stmt.executeQuery();
			rs.first();
			int id = rs.getInt("id");
			String emailAddress = rs.getString("emailAddress");
			String nickname = rs.getString("nickname");
			String note = rs.getString("note");

			return new User(id, emailAddress, nickname, null, note);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

	}

	public Channel[] getPublicGroups() throws InternalServerErrorException {
		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT c.id id, c.type type, c.name name FROM Channels c WHERE c.type = 'PUBLIC_GROUP'")) {

			ResultSet rs = stmt.executeQuery();

			ArrayList<Channel> channelList = new ArrayList<>();

			while (rs.next()) {
				int id = rs.getInt("id");
				ChannelType type = ChannelType.valueOf(rs.getString("type"));
				String name = rs.getString("name");
				channelList.add(new Channel(id, type, name));
			}

			return channelList.toArray(new Channel[channelList.size()]);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}
	}

	private boolean isPublicGroup(Channel channel) throws SQLException, ChannelNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT (SELECT c.type FROM Channels c WHERE c.id = ?) = 'PUBLIC_GROUP' isPublicGroup")) {
			stmt.setInt(1, channel.getId());
			ResultSet rs = stmt.executeQuery();
			rs.first();
			boolean isPublicGroup = rs.getBoolean("isPublicGroup");
			if (rs.wasNull())
				throw new ChannelNotFoundException();
			return isPublicGroup;
		}

	}

	public void joinGroup(User user, Channel channel) throws InternalServerErrorException, ChannelNotFoundException {

		// check if group is of type PUBLIC_GROUP
		try {
			if (!isPublicGroup(channel))
				throw new ChannelNotFoundException();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c
						.prepareStatement("INSERT INTO channelMembers(user, channel) VALUES (?, ?)")) {
			stmt.setInt(1, user.getId());
			stmt.setInt(2, channel.getId());
			if (stmt.executeUpdate() == 0)
				; // TODO already member of group -> exception
		} catch (SQLException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

	}

	public Channel[] getChannels(User user) throws InternalServerErrorException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT c.id id, c.type type, c.name name FROM Channels c INNER JOIN channelMembers cm ON cm.channel = c.id INNER JOIN Users u ON u.id = cm.user WHERE u.id = ?")) {
			stmt.setInt(1, user.getId());
			ResultSet rs = stmt.executeQuery();

			ArrayList<Channel> channelList = new ArrayList<>();

			while (rs.next()) {
				int id = rs.getInt("id");
				ChannelType type = ChannelType.valueOf(rs.getString("type"));
				String name = rs.getString("name");
				channelList.add(new Channel(id, type, name));
			}

			return channelList.toArray(new Channel[channelList.size()]);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

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
