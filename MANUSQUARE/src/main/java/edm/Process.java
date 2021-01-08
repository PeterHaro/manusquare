package edm;

import utilities.StringUtilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class Process extends Resource {

	private Set<String> equivalentProcesses; //added 11.02.2020 to compare equivalent processes in SimilarityMeasures.java

	private Process(Builder builder) {
		super(builder);

	}

	public static class Builder extends Resource.Builder<Builder> {

		private Set<String> equivalentProcesses;

		public Builder() {
			super();

		}

		public Builder setEquivalentProcesses (Set<String> equivalentProcesses) {
			this.equivalentProcesses = equivalentProcesses;
			return this;
		}
		

		@Override
		public Process build() {
			return new Process(this);
		}

		@Override
		protected Builder self() {
			return this;
		}


	}


//	public String getName() {
//		return this.getName();
//	}


	public Set<String> getEquivalentProcesses() {
		return equivalentProcesses;
	}


	@Override
	public boolean equals(Object o) {
		if (o instanceof Process && ((Process) o).getName().equals(this.getName())) {
			return true;
		} else {
			return false;
		}
	}


	@Override
	public int hashCode() {
		return Objects.hash(this.getName());
	}

	//a toString() method that prints processes along with relevant materials
	public String toString() {

		StringBuffer returnedString = new StringBuffer();

		if (this.getAttributeWeightMap() != null) {
			//get attributeKeys associated with process
			Map<String, String> attributeWeightMap = this.getAttributeWeightMap();

			Set<String> attributes = new HashSet<String>();
			Set<String> attributeValue = new HashSet<String>();

			for (Entry<String, String> e : attributeWeightMap.entrySet()) {
				attributes.add(e.getKey());
				attributeValue.add(e.getValue());
			}




			returnedString.append(this.getName());

			returnedString.append("\n\n- Attributes:");

			if (attributes == null || attributes.isEmpty()) {
				returnedString.append(" ( no attributes )");
			} else {
				for (Entry<String, String> e : attributeWeightMap.entrySet()) {
					returnedString.append(e.getKey() + ": " + e.getValue() + " ");
				}

			}

		} else {

			returnedString.append(this.getName());

			returnedString.append("\n\n- Attributes:");

			if (this.getAttributes() == null || this.getAttributes().isEmpty()) {
				returnedString.append(" ( no attributes )");
			}

		}

		Set<String> materialNames = new HashSet<String>();
		Set<String> materials = this.getMaterials();

		//need to check if there are no materials associated with the process
		if (materials == null || materials.isEmpty()) {

			returnedString.append("\n- Materials: ( no materials )");


		} else {

			for (String material : materials) {
				materialNames.add(material);
			}

			returnedString.append("\n- Materials: ( " + StringUtilities.printSetItems(materialNames) + " )");

		}

		returnedString.append("\n");

		return returnedString.toString();

	}

}
