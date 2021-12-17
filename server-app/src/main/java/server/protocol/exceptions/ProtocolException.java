package server.protocol.exceptions;

import server.protocol.Protocol;
import server.protocol.Protocol.Status;

public abstract class ProtocolException extends Exception {

	private static final long serialVersionUID = 5178921358562979513L;

	Status status;
	
	ProtocolException(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}

	public class EmailAlreadyRegisteredException extends ProtocolException {

		private static final long serialVersionUID = -6486262374408769558L;

		EmailAlreadyRegisteredException() {
			super(Protocol.Status.EMAIL_ALREADY_REGISTERED);
		}

	}
	
	public class PasswordRequirementNotMet extends ProtocolException {

		private static final long serialVersionUID = -2748115488968839743L;

		PasswordRequirementNotMet() {
			super(Protocol.Status.PASSWORD_REQ_NOT_MET);
		}

	}
	
}
