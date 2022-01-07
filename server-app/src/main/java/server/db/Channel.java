package server.db;

public class Channel extends TransmittableObject {

	private Attr<Integer> id = new Attr<>();
	private Attr<ChannelType> type = new Attr<>();
	private Attr<String> name = new Attr<>();
	
	public Channel() {
		registerAttributes(id, type, name);
	}
	
	public Channel withId(int id) {
		this.id.set(id);
		return this;
	}

	public Channel withType(ChannelType type) {
		this.type.set(type);
		return this;
	}

	public Channel withName(String name) {
		this.name.set(name);
		return this;
	}

	public int getId() {
		return id.getValue();
	}

	public ChannelType getType() {
		return type.getValue();
	}

	public String getName() {
		return name.getValue();
	}

	public enum ChannelType {
		DM, PUBLIC_GROUP, PRIVATE_GROUP
	}

}
