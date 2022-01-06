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

	private String hashPassword(String password) {
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return Base64.getEncoder().encodeToString(md.digest(password.getBytes()));
	}

	public User withId(int id) {
		this.id = id;
		return this;
	}

	public User withEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
		return this;
	}

	public User withNickname(String nickname) {
		this.nickname = nickname;
		return this;
	}

	public User withPassword(String password) {
		if (password != null)
			this.passwordHash = hashPassword(password);
		else
			this.passwordHash = null;
		return this;
	}

	public User withNote(String note) {
		this.note = note;
		return this;
	}

	public User withType(RelationshipType type) {
		this.type = type;
		return this;
	}

	public User withAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
		return this;
	}

	public Integer getId() {
		return id;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getNote() {
		return note;
	}

	public RelationshipType getType() {
		return type;
	}

	public Boolean isAdmin() {
		return isAdmin;
	}

	@Override
	public String toString() {
		return objsToString(id, emailAddress, nickname, passwordHash, note, type, isAdmin);
	}

	public enum RelationshipType {
		FRIEND, BLOCKED
	}

}
