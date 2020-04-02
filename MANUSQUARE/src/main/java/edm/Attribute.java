package edm;

public class Attribute extends Resource {
	
	private String key;
	private String value;
	private String type;
	private String unitOfMeasurement;
	
//	public Attribute(String id, String key, String value, String type, String unitOfMeasurement) {
//		super(id);
//		this.key = key;
//		this.value = value;
//		this.type = type;
//		this.unitOfMeasurement = unitOfMeasurement;
//	}
	
	//if attribute is material there is no unitOfMeasurement
	public Attribute(String key, String value) {
		this.key = key;
		this.value = value;

	}
	
	public Attribute(String key, String value, String unitOfMeasurement) {
		this.key = key;
		this.value = value;
		this.unitOfMeasurement = unitOfMeasurement;

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

	public String getunitOfMeasurement() {
		return unitOfMeasurement;
	}

	public void setunitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}
	
	
	

}
