package validation;

public class UnitOfMeasurementConverter {
	
	
	//FIXME: Use javax.measure for unit conversion + additional metrics
	public static String convertUnitOfMeasurement (String inputValue, String inputUOM) {
	
		double outputValue = 0;
		
		if (inputUOM.equalsIgnoreCase("m")) {
			outputValue = Double.parseDouble(inputValue) * 1000;
		} else if (inputUOM.equalsIgnoreCase("dm")) {
			outputValue = Double.parseDouble(inputValue) * 100;
		} else if (inputUOM.equalsIgnoreCase("cm")) {
			outputValue = Double.parseDouble(inputValue) * 10;
		} else {
			outputValue = Double.parseDouble(inputValue);
		}

		return Double.toString(outputValue);
	}
	
}
