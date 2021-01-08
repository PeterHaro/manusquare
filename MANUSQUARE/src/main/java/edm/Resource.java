package edm;

import java.util.Map;
import java.util.Set;

public class Resource {

	//mandatory
	private String id;
	private String name;
	private Set<Attribute> attributes;
	private Map<String, String> attributeWeightMap; 
	private Set<String> materials; 

	//optional
	private String material;

	Resource (Builder<?> builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.attributes = builder.attributes;
		this.attributeWeightMap = builder.attributeWeightMap;
		this.materials = builder.materials;
		this.material = builder.material;
	}


	public abstract static class Builder<T extends Builder> {

		//mandatory
		private String id;
		private String name;
		private Set<Attribute> attributes;
		private Map<String, String> attributeWeightMap; 
		private Set<String> materials; 

		//optional
		private String material;

		public Builder() {}


		public T setId (String id) {
			this.id = id;
			return self();
		}

		public T setName (String name) {
			this.name = name;
			return self();
		}

		public T setAttributes (Set<Attribute> attributes) {
			this.attributes = attributes;
			return self();
		}

		public T setAttributeWeightMap (Map<String, String> attributeWeightMap) {
			this.attributeWeightMap = attributeWeightMap;
			return self();
		}

		public T setMaterials (Set<String> materials) {
			this.materials = materials;
			return self();
		}

		public T setMaterial (String material) {
			this.material = material;
			return self();
		}

		public abstract Resource build(); 

		protected abstract T self();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<Attribute> getAttributes() {
		return attributes;
	}

	public Map<String, String> getAttributeWeightMap() {
		return attributeWeightMap;
	}

	public Set<String> getMaterials() {
		return materials;
	}

	public String getMaterial() {
		return material;
	}


}
