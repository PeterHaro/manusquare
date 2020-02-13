package edm;

public class Attribute extends Resource {
	
	private String key;
	private String value;
	private String type;
	
	public Attribute(String id, String key, String value, String type) {
		super(id);
		this.key = key;
		this.value = value;
		this.type = type;
	}
	
	public Attribute(String key, String value) {
		this.key = key;
		this.value = value;

	}
	
	public Attribute(String key) {
		this.key = key;
	}
	
	
	public Attribute() {}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	

}
