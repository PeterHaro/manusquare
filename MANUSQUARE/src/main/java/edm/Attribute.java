package edm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;

import query.BPQuery;
import query.CSQuery;
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

	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}

	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}	
	
	//TODO: Simplify and have the same method for both CS and BP
	public static Map<String, String> createAttributeWeightMap (BindingSet solution, Attribute supplierAttribute, CSQuery query) {
		
		//create supplierAttribute that can be compared to consumerAttribute
		supplierAttribute.setKey(StringUtilities.stripIRI(solution.getValue("attributeType").stringValue().replaceAll("\\s+", "")));
		supplierAttribute.setUnitOfMeasurement(solution.getValue("uomStr").stringValue().replaceAll("\\s+", ""));
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
					
					if (Double.parseDouble(updatedSupplierAttribute.getValue()) >= Double.parseDouble(bpa.getValue())) {
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
	
	public static Map<String, String> createBPAttributeWeightMap (Attribute supplierAttribute, BPQuery query) {
		
		Set<Attribute> consumerAttributes = new HashSet<Attribute>();
		for (Attribute ca : query.getAttributes()) {
			if (!ca.getKey().equals("AttributeMaterial") && !ca.getKey().equals("Appearance")) {
				consumerAttributes.add(ca);
			}
		}
		
		Attribute updatedSupplierAttribute = new Attribute();

		Map<String, String> attributeMap = null;
	
		for (Attribute bpa : consumerAttributes) {
									
			if (!bpa.getKey().equals("Appearance") 
					&& !bpa.getKey().equals("AttributeMaterial") 
					&& supplierAttribute.getKey().equals(bpa.getKey())) {
				
				
				String condition = mapAttributeConditions(StringUtilities.stripIRI(supplierAttribute.getKey()));
				

				updatedSupplierAttribute = alignAttributeValues(supplierAttribute, bpa);
				
				if (condition.equals(">=")) {
					attributeMap = new HashMap<String, String>();

					if (Double.parseDouble(updatedSupplierAttribute.getValue()) >= Double.parseDouble(bpa.getValue())) {
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

			 else {
				
				attributeMap = new HashMap<String, String>();
				attributeMap.put(supplierAttribute.getKey(), "N");
				
			}
			}
		}
		

		return attributeMap;
	}
	

	public static Attribute alignAttributeValues (Attribute supplierAttribute, Attribute consumerAttribute) {

		double newValue = 0;

		if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("mm")) {

			if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("mm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("cm")) {

			if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("cm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("dm")) {

			if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("dm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			}


		} else if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("m")) {

			if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("m")) {
				return supplierAttribute;
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("µm")) {

			if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("µm")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000000;
				supplierAttribute.setValue(Double.toString(newValue));
			}
			
			
		} 
		
		else if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("kg")) {

			if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("kg")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("hg")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("dag")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("g")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("dg")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("cg")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("mg")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000000;
				supplierAttribute.setValue(Double.toString(newValue));
			}
			
			
		} 		
		
		else if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("MPa")) {

			if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("MPa")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("Pa")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 1000000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("hPa")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("kPa")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("bar")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("Mbar")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("psi")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 145.0378;
				supplierAttribute.setValue(Double.toString(newValue));
			}
			
			
		} 
		
		else if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("ºC") || consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("C") || consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("celcius")) {

			if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("ºC") || supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("C") || supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("celcius")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("ºF") || supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("F") || supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("fahrenheit")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 37.8;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("K") || supplierAttribute.getUnitOfMeasurement().equalsIgnoreCase("kelvin")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 274.15;
				supplierAttribute.setValue(Double.toString(newValue));
			} 
			
		}
		
		else if (consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase("") || consumerAttribute.getUnitOfMeasurement().equalsIgnoreCase(" ")) { //if true/false attribute
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
