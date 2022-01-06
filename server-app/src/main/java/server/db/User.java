package server.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User extends TransmittableObject {

	private Attr<Integer> id = new Attr<>();
	private Attr<String> emailAddress = new Attr<>();
	private Attr<String> nickname = new Attr<>();
	private Attr<String> passwordHash = new Attr<>();
	private Attr<String> note = new Attr<>();
	private Attr<RelationshipType> type = new Attr<>();
	private Attr<Boolean> isAdmin = new Attr<>();

	// TODO
	// check password requirements
	// check email valid format
	// change hashing algorithm to bcrypt and add salt

	public User() {
		registerAttributes(id, emailAddress, nickname, passwordHash, note, type, isAdmin);
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

	public User withId(int id) {
		this.id.set(id);
		return this;
	}

	public User withEmailAddress(String emailAddress) {
		this.emailAddress.set(emailAddress);
		return this;
	}

	public User withNickname(String nickname) {
		this.nickname.set(nickname);
		return this;
	}

	public User withPassword(String password) {
		if (password != null)
			this.passwordHash.set(hashPassword(password));
		else
			this.passwordHash = null;
		return this;
	}

	public User withNote(String note) {
		this.note.set(note);
		return this;
	}

	public User withType(RelationshipType type) {
		this.type.set(type);
		return this;
	}

	public User withAdmin(boolean isAdmin) {
		this.isAdmin.set(isAdmin);
		return this;
	}

	public Integer getId() {
		return id.getValue();
	}

	public String getEmailAddress() {
		return emailAddress.getValue();
	}

	public String getNickname() {
		return nickname.getValue();
	}

	public String getPasswordHash() {
		return passwordHash.getValue();
	}

	public String getNote() {
		return note.getValue();
	}

	public RelationshipType getType() {
		return type.getValue();
	}

	public Boolean isAdmin() {
		return isAdmin.getValue();
	}

	public enum RelationshipType {
		FRIEND, BLOCKED
	}

}
