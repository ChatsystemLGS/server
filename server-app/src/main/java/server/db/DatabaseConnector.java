package server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;

import server.ProtocolException.ChannelNotFoundException;
import server.ProtocolException.EmailAlreadyRegisteredException;
import server.ProtocolException.EmailNotRegisteredException;
import server.ProtocolException.MessageTooLongException;
import server.ProtocolException.NotMemberOfChannelException;
import server.ProtocolException.PasswordInvalidException;
import server.ProtocolException.TooManyMessagesException;
import server.ProtocolException.UserNotFoundException;
import server.db.Channel.ChannelType;
import server.db.Message.DataType;
import server.db.User.RelationshipType;
import server.simplelogger.SimpleLogger;
import server.simplelogger.SimpleLogger.LogLevel;

// TODO ids should be stored in long (serial/bigint <=> long)
public class DatabaseConnector {

	private final String connectionUrl;

	public DatabaseConnector(String dbHost, int dbPort, String dbTable, String dbUser, String dbPassword) {
		connectionUrl = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", dbHost, dbPort, dbTable, dbUser,
				dbPassword);

		SimpleLogger.logf(LogLevel.DEBUG, "Instanciated DatabaseConnector with connectionUrl %s", connectionUrl);

		// TODO Check if connection can be established/get db version

		SimpleLogger.logf(LogLevel.INFO, "Connected to database %s:%s/%s", dbHost, dbPort, dbTable);
	}

	public void addUser(String emailAddress, String nickname, String passwordHash) throws SQLException, EmailAlreadyRegisteredException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"INSERT INTO Users (emailAddress, nickname, passwordHash) VALUES (?, ?, ?)")) {
			stmt.setString(1, emailAddress);
			stmt.setString(2, nickname);
			stmt.setString(3, passwordHash);
			if (stmt.executeUpdate() == 0)
				throw new EmailAlreadyRegisteredException();
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new EmailAlreadyRegisteredException();
		}

		// TODO debugLog(user, "added user", user);
	}

	private void debugLog(int userId, String action, int id) {
		SimpleLogger.logf(LogLevel.DEBUG, "user (%s) : %s [%s]", userId, action, id);
	}

	private boolean checkAuth(String emailAddress, String passwordHash) throws SQLException, EmailNotRegisteredException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT (SELECT u.passwordHash FROM Users u WHERE u.emailAddress = ?) = ? passwordMatches")) {
			stmt.setString(1, emailAddress);
			stmt.setString(2, passwordHash);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			boolean authenticated = rs.getBoolean("passwordMatches");
			if (rs.wasNull())
				throw new EmailNotRegisteredException();
			return authenticated;
		}

	}

	public User login(String emailAddress, String passwordHash) throws SQLException, PasswordInvalidException, EmailNotRegisteredException {

		if (!checkAuth(emailAddress, passwordHash))
			throw new PasswordInvalidException();

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.emailAddress emailAddress, u.nickname nickname, ur.note note FROM Users u "
								+ "LEFT JOIN userRelationships ur ON ur.userA=ur.userB "
								+ "WHERE u.emailAddress = ?")) {
			stmt.setString(1, emailAddress);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			int id = rs.getInt("id");
//			String emailAddress = rs.getString("emailAddress");
			String nickname = rs.getString("nickname");
			String note = rs.getString("note");

			// TODO debugLog(user, "logged in");
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

	private boolean isPublicGroup(int channelId) throws SQLException, ChannelNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT (SELECT c.type FROM Channels c WHERE c.id = ?) = 'PUBLIC_GROUP' isPublicGroup")) {
			stmt.setInt(1, channelId);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			boolean isPublicGroup = rs.getBoolean("isPublicGroup");
			if (rs.wasNull())
				throw new ChannelNotFoundException();
			return isPublicGroup;
		}

	}

	public void joinGroup(int currentUserId, int channelId) throws SQLException, ChannelNotFoundException {

		// check if group is of type PUBLIC_GROUP
		if (!isPublicGroup(channelId))
			throw new ChannelNotFoundException();

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c
						.prepareStatement("INSERT INTO channelMembers(user, channel) VALUES (?, ?)")) {
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, channelId);
			if (stmt.executeUpdate() == 0)
				; // TODO already member of group -> exception
		}
		// TODO debugLog(user, "joined", channel);
	}

	public Channel[] getChannels(int currentUserId) throws SQLException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement("SELECT c.id id, c.type type, c.name name FROM Channels c "
						+ "INNER JOIN channelMembers cm ON cm.channel = c.id "
						+ "INNER JOIN Users u ON u.id = cm.user WHERE u.id = ?")) {
			stmt.setInt(1, currentUserId);
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

	private boolean isChannelMember(int currentUserId, int channelId) throws SQLException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT EXISTS (SELECT * FROM channelMembers cm WHERE cm.user = ? AND cm.channel = ?) isMember")) {
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, channelId);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			return rs.getBoolean("isMember");
		}

	}

	private ChannelType getChannelType(int channelId) throws SQLException, ChannelNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement("SELECT c.type type FROM Channels c WHERE c.id = ?")) {
			stmt.setInt(1, channelId);
			ResultSet rs = stmt.executeQuery();
			rs.first();

			String typeString = rs.getString("type");
			if (typeString == null)
				throw new ChannelNotFoundException();

			return ChannelType.valueOf(typeString);
		}

	}

	public User[] getChannelMembers(int currentUserId, int channelId)
			throws SQLException, NotMemberOfChannelException, ChannelNotFoundException {

		if (!isChannelMember(currentUserId, channelId))
			if (getChannelType(channelId) != ChannelType.PUBLIC_GROUP)
				throw new NotMemberOfChannelException();

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.nickname nickname, ur.note note, ur.type type, cm.isAdmin isAdmin FROM Users u "
								+ "INNER JOIN channelMembers cm ON cm.user = u.id "
								+ "LEFT JOIN userRelationships ur ON ur.userB = u.id AND userA = ? "
								+ "WHERE cm.channel = ?")) {
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, channelId);
			ResultSet rs = stmt.executeQuery();

			ArrayList<User> userList = new ArrayList<>();

			while (rs.next()) {
				int id = rs.getInt("id");
				String nickname = rs.getString("nickname");
				String note = rs.getString("note");

				RelationshipType type;
				String typeString = rs.getString("type");
				if (typeString == null)
					type = null;
				else
					type = RelationshipType.valueOf(typeString);

				boolean isAdmin = rs.getBoolean("isAdmin");

				userList.add(
						new User().withId(id).withNickname(nickname).withNote(note).withType(type).withAdmin(isAdmin));
			}

			return userList.toArray(new User[userList.size()]);
		}

	}

	public User getUserById(int currentUserId, int userId) throws SQLException, UserNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u "
								+ "LEFT OUTER JOIN userRelationships ur ON ur.userB = u.id AND ur.userA = ? "
								+ "WHERE u.id = ?")) {
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, userId);

			ResultSet rs = stmt.executeQuery();

			if (!rs.next())
				throw new UserNotFoundException();

			int id = rs.getInt("id");
			String nickname = rs.getString("nickname");
			String note = rs.getString("note");
			RelationshipType type;

			String typeString = rs.getString("type");
			if (typeString == null)
				type = null;
			else
				type = RelationshipType.valueOf(typeString);

			return new User().withId(id).withNickname(nickname).withNote(note).withType(type);
		}

	}

	public User getUserByEmail(int currenUserId, String emailAddress) throws SQLException, UserNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u "
								+ "LEFT OUTER JOIN userRelationships ur ON ur.userB = u.id AND ur.userA = ? "
								+ "WHERE u.emailAddress = ?")) {
			stmt.setInt(1, currenUserId);
			stmt.setString(2, emailAddress);

			ResultSet rs = stmt.executeQuery();

			if (!rs.next())
				throw new UserNotFoundException();

			int id = rs.getInt("id");
			String nickname = rs.getString("nickname");
			String note = rs.getString("note");
			RelationshipType type;

			String typeString = rs.getString("type");
			if (typeString == null)
				type = null;
			else
				type = RelationshipType.valueOf(typeString);

			return new User().withId(id).withNickname(nickname).withNote(note).withType(type);
		}

	}

	public void addFriendById(int currentUserId, int userId) throws SQLException, UserNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement("INSERT INTO userRelationships (userA, userB, type) "
						+ "VALUES (?, ?, 'FRIEND') ON DUPLICATE KEY UPDATE type = 'FRIEND'")) {
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new UserNotFoundException();
		}
		debugLog(currentUserId, "added friend", userId);
	}

	public User[] getFriends(int currentUserId) throws SQLException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u "
								+ "INNER JOIN userRelationships ur ON ur.userB = u.id " + "WHERE ur.userA = ?")) {
			stmt.setInt(1, currentUserId);
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

	public void sendMessage(int currentUserId, int channelId, Timestamp timestamp, byte[] data, DataType dataType)
			throws SQLException, NotMemberOfChannelException, MessageTooLongException {

		if (!isChannelMember(currentUserId, channelId))
			throw new NotMemberOfChannelException();

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c
						.prepareStatement("INSERT INTO Messages (channel, author, timestamp, data, dataType) "
								+ "VALUES (?, ?, ?, ?, ?)")) {
			stmt.setInt(1, channelId);
			stmt.setInt(2, currentUserId);
			stmt.setTimestamp(3, timestamp); // check if timestamp is within reasonable
												// range/set timestamp to current time
			stmt.setBytes(4, data);
			stmt.setString(5, dataType.toString());

			stmt.executeUpdate();
		}
		debugLog(currentUserId, "sent message to channel", channelId);
	}

	// yes, I know, I'm not checking if dm already exists...
	// but I think I like it that way
	public void createDm(int currentUserId, int userId) throws SQLException, UserNotFoundException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt1 = c.prepareStatement("INSERT INTO Channels (type) VALUES ('DM')");
				PreparedStatement stmt2 = c.prepareStatement("SELECT LAST_INSERT_ID() insertedId");
				PreparedStatement stmt3 = c
						.prepareStatement("INSERT INTO channelMembers (user, channel) " + "VALUES (?, ?), (?, ?)")) {

			// create channel entry
			stmt1.executeUpdate();

			// get id of inserted channel
			ResultSet rs = stmt2.executeQuery();

			rs.first();
			int insertedId = rs.getInt("insertedId");

			stmt3.setInt(1, currentUserId);
			stmt3.setInt(2, insertedId);
			stmt3.setInt(3, userId);
			stmt3.setInt(4, insertedId);

			// insert users into channelMembers table
			stmt3.executeUpdate();

			debugLog(currentUserId, "created dm with", userId);

		} catch (SQLIntegrityConstraintViolationException e) {
			throw new UserNotFoundException();
		}

	}

	// TODO change to RECEIVEMESSAGES (...) Timestamp tFrom int n
	// -> LIMIT x ORDERED BY abc
	// (Timestamp currently gets ignored)
	public Message[] receiveMessages(int currentUserId, int channelId, Timestamp tFrom, Timestamp tUntil)
			throws SQLException, TooManyMessagesException {

		try (Connection c = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = c.prepareStatement(
						"SELECT m.author author, m.timestamp timestamp, m.data data, m.dataType dataType FROM Messages m "
								+ "INNER JOIN Channels c ON c.id = m.channel "
								+ "INNER JOIN channelMembers cm ON cm.channel = c.id WHERE cm.user = ? AND c.id = ? ")) {
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, channelId);

			ResultSet rs = stmt.executeQuery();

			ArrayList<Message> messageList = new ArrayList<>();

			while (rs.next()) {
				int author = rs.getInt("author");
				Timestamp timestamp = rs.getTimestamp("timestamp");
				byte[] data = rs.getBytes("data");
				DataType dataType = DataType.valueOf(rs.getString("dataType"));

				messageList.add(new Message().withChannel(channelId).withAuthor(author).withTimestamp(timestamp)
						.withData(data).withDataType(dataType));
			}

			return messageList.toArray(new Message[messageList.size()]);
		}

	}

}
