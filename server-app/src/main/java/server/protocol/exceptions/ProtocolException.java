package server.protocol.exceptions;

import server.protocol.Protocol.Status;

public abstract class ProtocolException extends Exception {
	
	
	// rework/revert back to extending ProtocolException

	private static final long serialVersionUID = 5178921358562979513L;

	Status status;

	ProtocolException(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}

}
