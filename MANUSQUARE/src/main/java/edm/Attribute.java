package edm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;

import query.ByProductQuery;
import utilities.StringUtilities;

public class Attribute {

	private String key;
	private String value;
	private String type;
	private String unitOfMeasurement;


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

	public String getunitOfMeasurement() {
		return unitOfMeasurement;
	}

	public void setunitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}	
	

	public static Map<String, String> createAttributeWeightMap (BindingSet solution, Attribute supplierAttribute, ByProductQuery query) {
		
		//create supplierAttribute that can be compared to consumerAttribute
		supplierAttribute.setKey(StringUtilities.stripIRI(solution.getValue("attributeType").stringValue().replaceAll("\\s+", "")));
		supplierAttribute.setunitOfMeasurement(solution.getValue("uomStr").stringValue().replaceAll("\\s+", ""));
		supplierAttribute.setValue(solution.getValue("attributeValue").stringValue().replaceAll("\\s+", ""));

		Set<Attribute> consumerAttributes = query.getAttributes();

		String condition = mapAttributeConditions(supplierAttribute.getKey());

		Attribute updatedSupplierAttribute = new Attribute();

		Map<String, String> attributeMap = null;

		for (Attribute bpa : consumerAttributes) {

			if (StringUtilities.stripIRI(solution.getValue("attributeType").stringValue().replaceAll("\\s+", "")).equals(bpa.getKey())) {

				updatedSupplierAttribute = alignAttributeValues(supplierAttribute, bpa);

				if (condition.equals(">=")) {
					attributeMap = new HashMap<String, String>();

					if (Double.parseDouble(bpa.getValue()) >= Double.parseDouble(updatedSupplierAttribute.getValue())) {
						attributeMap.put(updatedSupplierAttribute.getKey(), "Y");									

					} else {
						attributeMap.put(updatedSupplierAttribute.getKey(), "N");									
					}

				}

				else if (condition.equals("<=")) {

					attributeMap = new HashMap<String, String>();
					updatedSupplierAttribute = alignAttributeValues(supplierAttribute, bpa);

					if (Double.parseDouble(updatedSupplierAttribute.getValue()) <= Double.parseDouble(bpa.getValue()) ) {

						attributeMap.put(updatedSupplierAttribute.getKey(), "Y");									

					} else {

						attributeMap.put(updatedSupplierAttribute.getKey(), "N");									

					}

				}

				else if (condition.equals("=")) {

					attributeMap = new HashMap<String, String>();
					updatedSupplierAttribute = alignAttributeValues(supplierAttribute, bpa);

					if (Double.parseDouble(bpa.getValue()) == Double.parseDouble(updatedSupplierAttribute.getValue())) {
						attributeMap.put(updatedSupplierAttribute.getKey(), "Y");								

					} else {

						attributeMap.put(updatedSupplierAttribute.getKey(), "N");									
					}

				}
				
				else if (condition.equals("T")) {
					attributeMap.put(updatedSupplierAttribute.getKey(), "Y");
				}

			} else {
				
				attributeMap = new HashMap<String, String>();
				attributeMap.put(bpa.getKey(), "N");
				
			}
		}

		return attributeMap;
	}


	public static Attribute alignAttributeValues (Attribute supplierAttribute, Attribute consumerAttribute) {

		double newValue = 0;

		if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			}


		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("µm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("µm")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000000;
				supplierAttribute.setValue(Double.toString(newValue));
			}
			
			
		} 
		
		else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("kg")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("kg")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("hg")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dag")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("g")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dg")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cg")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mg")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000000;
				supplierAttribute.setValue(Double.toString(newValue));
			}
			
			
		} 		
		
		else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("MPa")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("MPa")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("Pa")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 1000000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("hPa")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("kPa")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("bar")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("Mbar")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("psi")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 145.0378;
				supplierAttribute.setValue(Double.toString(newValue));
			}
			
			
		} 
		
		else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("ºC") || consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("C") || consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("celcius")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("ºC") || supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("C") || supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("celcius")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("ºF") || supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("F") || supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("fahrenheit")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 37.8;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("K") || supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("kelvin")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 274.15;
				supplierAttribute.setValue(Double.toString(newValue));
			} 
			
		}
		
		else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("") || consumerAttribute.getunitOfMeasurement().equalsIgnoreCase(" ")) { //if true/false attribute
			return supplierAttribute;
		}

		return supplierAttribute;

	}
	
	/**
	 * Finds the relevant conditions ('<=', '>=' or '=') for a given sample of attributes.
	 *
	 * @param attributes attribute keys and values
	 * @return a map of attribute (key) and the conditions (value) used for determining whether they satisfy attribute reqs from the consumer.
	 * Feb 8, 2020
	 */
	public static String mapAttributeConditions(String attribute) {
		
		String condition = null;

		if (attribute == null) {
			
			condition = "!";

		} else if (attribute.equalsIgnoreCase("Length") || attribute.equalsIgnoreCase("Width") || attribute.equalsIgnoreCase("Depth") || attribute.equalsIgnoreCase("MinFeatureSize")
				|| attribute.equalsIgnoreCase("MinLayerThickness") || attribute.equalsIgnoreCase("MinKerfWidth") || attribute.equalsIgnoreCase("WorkingVolumeX")
				|| attribute.equalsIgnoreCase("MinSheetThickness") || attribute.equalsIgnoreCase("PartSizeX") || attribute.equalsIgnoreCase("PartSizeY") || attribute.equalsIgnoreCase("PartSizeZ")
				|| attribute.equalsIgnoreCase("MoldSizeX") || attribute.equalsIgnoreCase("MoldSizeY") || attribute.equalsIgnoreCase("MoldSizeZ") || attribute.equalsIgnoreCase("Capacity")
				|| attribute.equalsIgnoreCase("WorkingAreaX") || attribute.equalsIgnoreCase("WorkingAreaY") || attribute.equalsIgnoreCase("WorkingAreaZ")
				|| attribute.equalsIgnoreCase("AspectRatio") || attribute.equalsIgnoreCase("MinMeltingTemperature") || attribute.equalsIgnoreCase("MinYoungModulusE") || attribute.equalsIgnoreCase("MinLoadAtPermanentSetLimitRp") 
				|| attribute.equalsIgnoreCase("MinUltimateStrengthRm")  || attribute.equalsIgnoreCase("MinBrinellHardnessHB")  || attribute.equalsIgnoreCase("Area")  || attribute.equalsIgnoreCase("Diameter") 
				|| attribute.equalsIgnoreCase("MinGlassTransitionTemperature")  || attribute.equalsIgnoreCase("MinWaterAbsorption")  || attribute.equalsIgnoreCase("MinShoreDHardness")  
				|| attribute.equalsIgnoreCase("WeightCapacity") || attribute.equalsIgnoreCase("Weight")) {
			
			condition = ">=";
			
			
		} else if (attribute.equalsIgnoreCase("Tolerance") || attribute.equalsIgnoreCase("SurfaceFinishing") || attribute.equalsIgnoreCase("MaxWallThickness")
				|| attribute.equalsIgnoreCase("MaxPartSizeX") || attribute.equalsIgnoreCase("MaxPartSizeY") || attribute.equalsIgnoreCase("MaxPartSizeZ")
				|| attribute.equalsIgnoreCase("MaxKerfWidth") || attribute.equalsIgnoreCase("MaxSheetThickness") || attribute.equalsIgnoreCase("MaxMeltingTemperature") 
				|| attribute.equalsIgnoreCase("MaxYoungModulusE") || attribute.equalsIgnoreCase("MaxLoadAtPermanentSetLimitRp") || attribute.equalsIgnoreCase("MaxUltimateStrengthRm")
				|| attribute.equalsIgnoreCase("MaxBrinellHardnessHB") || attribute.equalsIgnoreCase("Thickness") || attribute.equalsIgnoreCase("MaxGlassTransitionTemperature") 
				|| attribute.equalsIgnoreCase("MaxWaterAbsorption") || attribute.equalsIgnoreCase("MaxShoreDHardness")) {
		
			
			condition = "<=";
			
		} else if (attribute.equalsIgnoreCase("Axis") || attribute.equalsIgnoreCase("CuttingSpeed")) {
			
			condition = "=";
			
		} else if (attribute.equalsIgnoreCase("Model") || attribute.equalsIgnoreCase("Stackability") || attribute.equalsIgnoreCase("PresenceOfHandles") || attribute.equalsIgnoreCase("PresenceOfCover")
				|| attribute.equalsIgnoreCase("Material")) {
			
			condition = "T";
			
		}
		
		return condition;

	}
	
	public static boolean isSupportedAttribute (String attributeKey) {

		Set<String> supportedAttributes = new HashSet<String>();
		supportedAttributes.add("Length");
		supportedAttributes.add("Width");
		supportedAttributes.add("Depth");
		supportedAttributes.add("MinFeatureSize");
		supportedAttributes.add("MinLayerThickness");
		supportedAttributes.add("MinKerfWidth");
		supportedAttributes.add("WorkingVolumeX");
		supportedAttributes.add("MinSheetThickness");
		supportedAttributes.add("PartSizeX");
		supportedAttributes.add("PartSizeY");
		supportedAttributes.add("PartSizeZ");
		supportedAttributes.add("MoldSizeX");
		supportedAttributes.add("MoldSizeY");
		supportedAttributes.add("MoldSizeZ");
		supportedAttributes.add("Capacity");
		supportedAttributes.add("WorkingAreaX");
		supportedAttributes.add("WorkingAreaY");
		supportedAttributes.add("WorkingAreaZ");
		supportedAttributes.add("AspectRatio");
		supportedAttributes.add("Tolerance");
		supportedAttributes.add("SurfaceFinishing");
		supportedAttributes.add("MaxWallThickness");
		supportedAttributes.add("SurfaceFinishing");
		supportedAttributes.add("MaxPartSizeX");
		supportedAttributes.add("MaxPartSizeY");
		supportedAttributes.add("MaxPartSizeZ");
		supportedAttributes.add("MaxKerfWidth");
		supportedAttributes.add("MaxSheetThickness");
		supportedAttributes.add("Axis");
		supportedAttributes.add("CuttingSpeed");
		supportedAttributes.add("MinMeltingTemperature");
		supportedAttributes.add("MinYoungModulusE");
		supportedAttributes.add("MinLoadAtPermanentSetLimitRp");
		supportedAttributes.add("MinSheetThickness");
		supportedAttributes.add("MinUltimateStrengthRm");
		supportedAttributes.add("MinBrinellHardnessHB");
		supportedAttributes.add("Area");
		supportedAttributes.add("Diameter");
		supportedAttributes.add("MinGlassTransitionTemperature");
		supportedAttributes.add("MinWaterAbsorption");
		supportedAttributes.add("MinShoreDHardness");
		supportedAttributes.add("WeightCapacity");
		supportedAttributes.add("Weight");
		supportedAttributes.add("MaxMeltingTemperature");
		supportedAttributes.add("MaxYoungModulusE");
		supportedAttributes.add("MaxLoadAtPermanentSetLimitRp");
		supportedAttributes.add("MaxUltimateStrengthRm");
		supportedAttributes.add("MaxBrinellHardnessHB");
		supportedAttributes.add("Thickness");
		supportedAttributes.add("MaxGlassTransitionTemperature");
		supportedAttributes.add("MaxWaterAbsorption");
		supportedAttributes.add("MaxShoreDHardness");
		supportedAttributes.add("Model");
		supportedAttributes.add("Stackability");
		supportedAttributes.add("PresenceOfHandles");
		supportedAttributes.add("PresenceOfCover");
		supportedAttributes.add("AttributeMaterial");

		if (supportedAttributes.contains(attributeKey)) {
			return true;
		} else {
			return false;
		}


	}
	

}
