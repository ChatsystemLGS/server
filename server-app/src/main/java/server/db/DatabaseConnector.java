package server.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import server.ProtocolException.ChannelNotFoundException;
import server.ProtocolException.EmailAlreadyRegisteredException;
import server.ProtocolException.EmailNotRegisteredException;
import server.ProtocolException.MessageTooLongException;
import server.ProtocolException.PasswordInvalidException;
import server.ProtocolException.TooManyMessagesException;
import server.ProtocolException.UserNotFoundException;
import server.db.Channel.ChannelType;
import server.db.User.RelationshipType;
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

	public void addUser(User user) throws SQLException, EmailAlreadyRegisteredException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"INSERT INTO Users (emailAddress, nickname, passwordHash) VALUES (?, ?, ?)")) {
			stmt.setString(1, user.getEmailAddress());
			stmt.setString(2, user.getNickname());
			stmt.setString(3, user.getPasswordHash());
			if (stmt.executeUpdate() == 0)
				throw new EmailAlreadyRegisteredException();
		}

	}

	private boolean checkAuth(User user) throws SQLException, EmailNotRegisteredException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT (SELECT u.passwordHash FROM Users u WHERE u.emailAddress = ?) = ? passwordMatches")) {
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

	public User login(User user) throws SQLException, PasswordInvalidException, EmailNotRegisteredException {

		if (!checkAuth(user))
			throw new PasswordInvalidException();

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.emailAddress emailAddress, u.nickname nickname, ur.note note FROM Users u "
								+ "LEFT JOIN userRelationships ur ON ur.userA=ur.userB "
								+ "WHERE u.emailAddress = ?")) {
			stmt.setString(1, user.getEmailAddress());
			ResultSet rs = stmt.executeQuery();
			rs.first();
			int id = rs.getInt("id");
			String emailAddress = rs.getString("emailAddress");
			String nickname = rs.getString("nickname");
			String note = rs.getString("note");

			return new User().withId(id).withEmailAddress(emailAddress).withNickname(nickname).withNote(note);
		}

	}

	public Channel[] getPublicGroups() throws SQLException {
		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT c.id id, c.type type, c.name name FROM Channels c WHERE c.type = 'PUBLIC_GROUP'")) {

			ResultSet rs = stmt.executeQuery();

			ArrayList<Channel> channelList = new ArrayList<>();

			while (rs.next()) {
				int id = rs.getInt("id");
				ChannelType type = ChannelType.valueOf(rs.getString("type"));
				String name = rs.getString("name");
				channelList.add(new Channel().withId(id).withType(type).withName(name));
			}

			return channelList.toArray(new Channel[channelList.size()]);
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

	public void joinGroup(User user, Channel channel) throws SQLException, ChannelNotFoundException {

		// check if group is of type PUBLIC_GROUP
		if (!isPublicGroup(channel))
			throw new ChannelNotFoundException();

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c
						.prepareStatement("INSERT INTO channelMembers(user, channel) VALUES (?, ?)")) {
			stmt.setInt(1, user.getId());
			stmt.setInt(2, channel.getId());
			if (stmt.executeUpdate() == 0)
				; // TODO already member of group -> exception
		}

	}

	public Channel[] getChannels(User user) throws SQLException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement("SELECT c.id id, c.type type, c.name name FROM Channels c "
						+ "INNER JOIN channelMembers cm ON cm.channel = c.id "
						+ "INNER JOIN Users u ON u.id = cm.user WHERE u.id = ?")) {
			stmt.setInt(1, user.getId());
			ResultSet rs = stmt.executeQuery();

			ArrayList<Channel> channelList = new ArrayList<>();

			while (rs.next()) {
				int id = rs.getInt("id");
				ChannelType type = ChannelType.valueOf(rs.getString("type"));
				String name = rs.getString("name");
				channelList.add(new Channel().withId(id).withType(type).withName(name));
			}

			return channelList.toArray(new Channel[channelList.size()]);
		}

	}

	// TODO check if user is member/channel is publicgroup
	public User[] getChannelMembers(User user, Channel channel) throws SQLException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.nickname nickname, ur.note note, ur.type type, cm.isAdmin isAdmin FROM Users u "
								+ "INNER JOIN channelMembers cm ON cm.user = u.id "
								+ "INNER JOIN userRelationships ur ON ur.userB = u.id "
								+ "WHERE cm.channel = ? AND ur.userA = ?")) {
			stmt.setInt(1, channel.getId());
			stmt.setInt(2, user.getId());
			ResultSet rs = stmt.executeQuery();

			ArrayList<User> userList = new ArrayList<>();

			while (rs.next()) {
				int id = rs.getInt("id");
				String nickname = rs.getString("nickname");
				String note = rs.getString("note");
				RelationshipType type = RelationshipType.valueOf(rs.getString("type"));
				boolean isAdmin = rs.getBoolean("isAdmin");
				userList.add(
						new User().withId(id).withNickname(nickname).withNote(note).withType(type).withAdmin(isAdmin));
			}

			return userList.toArray(new User[userList.size()]);
		}

	}

	public User getUserById(User userA, User userB) throws SQLException, UserNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u "
								+ "INNER JOIN userRelationships ur ON ur.userB = u.id "
								+ "WHERE ur.userA = ? AND u.id = ?")) {
			stmt.setInt(1, userA.getId());
			stmt.setInt(2, userB.getId());

			ResultSet rs = stmt.executeQuery();

			if (!rs.next())
				throw new UserNotFoundException();

			int id = rs.getInt("id");
			String nickname = rs.getString("nickname");
			String note = rs.getString("note");
			RelationshipType type = RelationshipType.valueOf(rs.getString("type"));

			return new User().withId(id).withNickname(nickname).withNote(note).withType(type);
		}

	}

	// TODO even if userA == userB we should return the user
	public User getUserByEmail(User userA, User userB) throws SQLException, UserNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u "
								+ "INNER JOIN userRelationships ur ON ur.userB = u.id "
								+ "WHERE ur.userA = ? AND u.emailAddress = ?")) {
			stmt.setInt(1, userA.getId());
			stmt.setString(2, userB.getEmailAddress());

			System.out.println(userB.getEmailAddress());

			ResultSet rs = stmt.executeQuery();

			if (!rs.next())
				throw new UserNotFoundException();

			int id = rs.getInt("id");
			String nickname = rs.getString("nickname");
			String note = rs.getString("note");
			RelationshipType type = RelationshipType.valueOf(rs.getString("type"));

			return new User().withId(id).withNickname(nickname).withNote(note).withType(type);
		}

	}

	public void addFriendById(User user, User friend) throws SQLException, UserNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement("INSERT INTO userRelationships (userA, userB, type) "
						+ "VALUES (?, ?, 'FRIEND') ON DUPLICATE KEY UPDATE type = 'FRIEND'")) {
			stmt.setInt(1, user.getId());
			stmt.setInt(2, friend.getId());
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new UserNotFoundException();
		}

	}

	public User[] getFriends(User user) throws SQLException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u "
								+ "INNER JOIN userRelationships ur ON ur.userB = u.id " + "WHERE ur.userA = ?")) {
			stmt.setInt(1, user.getId());
			ResultSet rs = stmt.executeQuery();

			ArrayList<User> userList = new ArrayList<>();

			while (rs.next()) {
				int id = rs.getInt("id");
				String nickname = rs.getString("nickname");
				String note = rs.getString("note");
				RelationshipType type = RelationshipType.valueOf(rs.getString("type"));
				userList.add(new User().withId(id).withNickname(nickname).withNote(note).withType(type));
			}

			return userList.toArray(new User[userList.size()]);
		}

	}

	public void sendMessage(User user, Channel channel, Message message) throws MessageTooLongException {
		// TODO Auto-generated method stub

	}

	public int createDm(User user, User userB) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Message[] receiveMessages(User user, Channel channel, Date tFrom, Date tUntil)
			throws TooManyMessagesException {
		// TODO Auto-generated method stub
		return null;
	}

}
