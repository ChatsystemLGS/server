package server.protocol;

import java.sql.Date;

import server.db.Message;
import server.protocol.Protocol.Status;

public abstract class ProtocolException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2162828148268985546L;
	private final Status status;

	ProtocolException(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public static class InvalidParameterException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4208867606970220161L;
		private final int index;

		public InvalidParameterException(int index) {
			super(Protocol.Status.INVALID_PARAMETER);
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

	}

	public static class EmailAlreadyRegisteredException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4237922086005335690L;

		public EmailAlreadyRegisteredException() {
			super(Protocol.Status.EMAIL_ALREADY_REGISTERED);
		}

	}

	public static class PasswordRequirementNotMetException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1035791352733735378L;

		public PasswordRequirementNotMetException() {
			super(Protocol.Status.PASSWORD_REQ_NOT_MET);
		}

	}

	public static class EmailNotRegisteredException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6532826769572086272L;

		public EmailNotRegisteredException() {
			super(Protocol.Status.EMAIL_NOT_REGISTERED);
		}

	}

	public static class PasswordInvalidException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1832448351451110039L;

		public PasswordInvalidException() {
			super(Protocol.Status.PASSWORD_INVALID);
		}

	}

	public static class NotMemberOfChannelException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2170636056686421482L;

		public NotMemberOfChannelException() {
			super(Protocol.Status.NOT_MEMBER_OF_CHANNEL);
		}

	}

	public static class MessageTooLongException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5241748522110696394L;
		private final int MAX_MESSAGE_SIZE;

		public MessageTooLongException(int MAX_MESSAGE_SIZE) {
			super(Protocol.Status.MESSAGE_TOO_LONG);
			this.MAX_MESSAGE_SIZE = MAX_MESSAGE_SIZE;
		}

		public int getMaxMessageSize() {
			return MAX_MESSAGE_SIZE;
		}

	}

	public static class TooManyMessagesException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4931284760707863961L;
		private final Date lastMessageTime;
		private final Message[] messages;

		public TooManyMessagesException(Date lastMessageTime, Message[] messages) {
			super(Protocol.Status.TOO_MANY_MESSAGES);
			this.lastMessageTime = lastMessageTime;
			this.messages = messages;
		}

		public Date getLastMessageTime() {
			return lastMessageTime;
		}

		public Message[] getMessages() {
			return messages;
		}

	}

	public static class ChannelNotFoundException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6705554276488394002L;

		public ChannelNotFoundException() {
			super(Protocol.Status.CHANNEL_NOT_FOUND);
		}

	}

	public static class UserNotFoundException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6940922235095667798L;

		public UserNotFoundException() {
			super(Protocol.Status.USER_NOT_FOUND);
		}

	}

	public static class DmAlreadyExistsException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2923409626696600479L;
		private final int channelId;

		public DmAlreadyExistsException(int channelId) {
			super(Protocol.Status.DM_ALREADY_EXISTS);
			this.channelId = channelId;
		}

		public int getChannelId() {
			return channelId;
		}

	}

}
