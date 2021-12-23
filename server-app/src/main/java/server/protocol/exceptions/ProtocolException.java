package server.protocol.exceptions;

import java.sql.Date;

import server.protocol.Message;
import server.protocol.Protocol;
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

	public class EmailAlreadyRegisteredException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4237922086005335690L;

		EmailAlreadyRegisteredException() {
			super(Protocol.Status.EMAIL_ALREADY_REGISTERED);
		}

	}

	public class PasswordRequirementNotMetException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1035791352733735378L;

		PasswordRequirementNotMetException() {
			super(Protocol.Status.PASSWORD_REQ_NOT_MET);
		}

	}
	
	public class EmailNotRegisteredException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6532826769572086272L;

		EmailNotRegisteredException() {
			super(Protocol.Status.EMAIL_NOT_REGISTERED);
		}

	}
	
	public class PasswordInvalidException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1832448351451110039L;

		PasswordInvalidException() {
			super(Protocol.Status.PASSWORD_INVALID);
		}

	}
	
	public class NotMemberOfChannelException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2170636056686421482L;

		NotMemberOfChannelException() {
			super(Protocol.Status.NOT_MEMBER_OF_CHANNEL);
		}

	}
	
	public class MessageTooLongException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5241748522110696394L;
		private final int MAX_MESSAGE_SIZE;
		
		MessageTooLongException(int MAX_MESSAGE_SIZE) {
			super(Protocol.Status.MESSAGE_TOO_LONG);
			this.MAX_MESSAGE_SIZE = MAX_MESSAGE_SIZE;
		}
		
		public int getMaxMessageSize() {
			return MAX_MESSAGE_SIZE;
		}
		
	}
	
	public class TooManyMessagesException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4931284760707863961L;
		public final Date lastMessageTime;
		private final Message[] messages;
		
		TooManyMessagesException(Date lastMessageTime, Message[] messages) {
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
	
	public class ChannelNotFoundException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6705554276488394002L;

		ChannelNotFoundException() {
			super(Protocol.Status.CHANNEL_NOT_FOUND);
		}
		
	}
	
	public class DmAlreadyExistsException extends ProtocolException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2923409626696600479L;

		DmAlreadyExistsException() {
			super(Protocol.Status.DM_ALREADY_EXISTING);
		}
		
	}

}
