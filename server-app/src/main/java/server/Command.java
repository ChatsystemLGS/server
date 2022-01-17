package server;

import server.Session.State;

enum Command {

	REGISTER(State.CONNECTED, new ArgType[] { 
		ArgType.STRING, 
		ArgType.STRING, 
		ArgType.STRING 
	}),

	LOGIN(State.CONNECTED, new ArgType[] { 
		ArgType.STRING, 
		ArgType.STRING 
	}), 

	GETPUBLICGROUPS(State.AUTHENTICATED),

	JOINGROUP(State.AUTHENTICATED, new ArgType[] { 
		ArgType.INTEGER 
	}), 

	GETCHANNELS(State.AUTHENTICATED),

	GETCHANNELMEMBERS(State.AUTHENTICATED, new ArgType[] {
		ArgType.INTEGER 
	}),

	GETUSER(State.AUTHENTICATED, new ArgType[] { 
		ArgType.INTEGER 
	}, new ArgType[] { 
		ArgType.STRING 
	}),

	ADDFRIEND(State.AUTHENTICATED, new ArgType[] { 
		ArgType.INTEGER 
	}), 

	GETFRIENDS(State.AUTHENTICATED),

	SENDMESSAGE(State.AUTHENTICATED, new ArgType[] { 
		ArgType.INTEGER, 
		ArgType.STRING_DATA,
		ArgType.STRING_ENUM, 
	}),

	CREATEDM(State.AUTHENTICATED, new ArgType[] { 
		ArgType.INTEGER 
	}),

	RECEIVEMESSAGES(State.AUTHENTICATED, new ArgType[] { 
		ArgType.INTEGER, 
		ArgType.INTEGER_TIMESTAMP, 
		ArgType.INTEGER_TIMESTAMP 
	}),

	QUIT(State.CONNECTED);

	
	private final State requiredState;
	private final int minNumArgs;
	private final int maxNumArgs;

	// builds a Command and allows multiple possible combinations of different
	// parameters
	Command(State requiredState, ArgType[]... parameters) {
		this.requiredState = requiredState;

		if (parameters.length == 0) {
			minNumArgs = 0;
			maxNumArgs = 0;
		} else {
			int min = parameters[0].length;
			int max = parameters[0].length;
			for (int i = 1; i < parameters.length; i++) {
				int len = parameters[i].length;
				if (min > len)
					min = len;
				if (max < len)
					max = len;
			}

			this.minNumArgs = min;
			this.maxNumArgs = max;
		}

	}

	public State getRequiredState() {
		return requiredState;
	}

	public int getMinNumArgs() {
		return minNumArgs;
	}

	public int getMaxNumArgs() {
		return maxNumArgs;
	}

}

enum ArgType {
	STRING, INTEGER, STRING_DATA, STRING_ENUM, INTEGER_TIMESTAMP
}