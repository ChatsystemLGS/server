package server.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {

	private int id;
	private String emailAddress;
	private String nickname;
	private String passwordHash;
	private String note;

	// TODO
	// refactor -> only one constructor??
	// check password requirements
	// check email valid format
	
	public User(String emailAddress, String password) {
		this.emailAddress = emailAddress;
		this.passwordHash = hashPassword(password);
	}

	public User(int id) {
		this.id = id;
	}

	public User(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public User(String emailAddress, String nickname, String password) {
		this.emailAddress=emailAddress;
		this.nickname=nickname;
		this.passwordHash=hashPassword(password);
	}

	public User(int id, String emailAddress, String nickname, String note) {
		this.id = id;
		this.emailAddress = emailAddress;
		this.nickname = nickname;
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
