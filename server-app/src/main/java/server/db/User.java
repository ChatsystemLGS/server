package server.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {

	private Integer id;
	private String emailAddress;
	private String nickname;
	private String passwordHash;
	private String note;

	// TODO
	// check password requirements
	// check email valid format
	// change hashing algorithm to bcrypt and add salt

	public User(Integer id, String emailAddress, String nickname, String password, String note) {
		this.id = id;
		this.emailAddress = emailAddress;
		this.nickname = nickname;
		if (password != null)
			this.passwordHash = hashPassword(password);
		else
			passwordHash = null;
		this.note = note;
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

	private String hashPassword(String password) {
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return Base64.getEncoder().encodeToString(md.digest(password.getBytes()));
	}

}
