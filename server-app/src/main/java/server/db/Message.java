package server.db;

import java.sql.Timestamp;

import server.TransmittableObject;

public class Message implements TransmittableObject {

	private Attr<Integer> id = new Attr<>();
	private Attr<Integer> channel = new Attr<>();
	private Attr<Integer> author = new Attr<>();
	private Attr<Timestamp> timestamp = new Attr<>();
	private Attr<byte[]> data = new Attr<>();
	private Attr<DataType> dataType = new Attr<>();

	public Message withId(int id) {
		this.id.set(id);
		return this;
	}

	public Message withChannel(int id) {
		this.channel.set(id);
		return this;
	}

	public Message withAuthor(int id) {
		this.author.set(id);
		return this;
	}

	public Message withTimestamp(Timestamp timestamp) {
		this.timestamp.set(timestamp);
		return this;
	}

	public Message withData(byte[] data) {
		this.data.set(data);
		return this;
	}

	public Message withDataType(DataType dataType) {
		this.dataType.set(dataType);
		return this;
	}

	public int getId() {
		return id.getValue();
	}

	public int getChannel() {
		return channel.getValue();
	}

	public int getAuthor() {
		return author.getValue();
	}

	public Timestamp getTimestamp() {
		return timestamp.getValue();
	}

	public byte[] getData() {
		return data.getValue();
	}

	public DataType getDataType() {
		return dataType.getValue();
	}

	public enum DataType {
		TEXT, FILE_TXT, FILE_PNG, FILE_GIF, FILE_PDF;
	}
	
	@Override
	public String transmittableString() {
		return transmittableString(id, channel, author, timestamp, data, dataType);
	}
	
	@Override
	public String toString() {
		return readableString(id, channel, author, timestamp, data, dataType);
	}

}
