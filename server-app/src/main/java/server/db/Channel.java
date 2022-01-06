package server.db;

public class Channel extends TransmittableObject {
	
	private Integer id;
	private ChannelType type;
	private String name;

	public Channel(int id, ChannelType type, String name) {
		this.id = id;
		this.type = type;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}
	
	public ChannelType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return objsToString(id, type, name);
	}
	
	public enum ChannelType {
		DM,
		PUBLIC_GROUP,
		PRIVATE_GROUP
	}

}
