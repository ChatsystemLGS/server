package server.db;

public class Channel extends TransmittableObject {

	private Integer id;
	private ChannelType type;
	private String name;

	public Channel withId(Integer id) {
		this.id = id;
		return this;
	}

	public Channel withType(ChannelType type) {
		this.type = type;
		return this;
	}

	public Channel withName(String name) {
		this.name = name;
		return this;
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
		DM, PUBLIC_GROUP, PRIVATE_GROUP
	}

}
