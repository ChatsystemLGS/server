package server.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User extends TransmittableObject {

	private Integer id;
	private String emailAddress;
	private String nickname;
	private String passwordHash;
	private String note;
	private RelationshipType type;
	private Boolean isAdmin;

	// TODO
	// check password requirements
	// check email valid format
	// change hashing algorithm to bcrypt and add salt

	public User(Integer id, String emailAddress, String nickname, String password, String note, RelationshipType type,
			Boolean isAdmin) {
		this.id = id;
		this.emailAddress = emailAddress;
		this.nickname = nickname;
		if (password != null)
			this.passwordHash = hashPassword(password);
		else
			passwordHash = null;
		this.note = note;
		this.type = type;
		this.isAdmin = isAdmin;
	}

	public int getId() {
		return id;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getNickname() {
		return nickname;
	}

	public String getNote() {
		return note;
	}

	public RelationshipType getType() {
		return type;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	private String hashPassword(String password) {
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return Base64.getEncoder().encodeToString(md.digest(password.getBytes()));
	}

	@Override
	public String toString() {
		return objsToString(id, emailAddress, nickname, passwordHash, note, type, isAdmin);
	}

	public enum RelationshipType {
		FRIEND, BLOCKED
	}

}
