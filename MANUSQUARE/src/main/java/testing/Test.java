package testing;

import java.util.HashSet;
import java.util.Set;

import utilities.StringUtilities;

public class Test {

	public static void main(String[] args) {

		double cQuantity = 1000;
		String cUOM = "mm";

		double sQuantity = 1.0;
		String sUOM = "m";

		System.out.println("Is cUOM compatible with sUOM?: " + isCompatible(cUOM, sUOM));
		System.out.println("Is cQuantity lower than sQuantity?: " + lowerThan(cQuantity, cUOM, sQuantity, sUOM));

	}

	private static boolean lowerThan (double consumerQuantity, String consumerUOM, double supplierQuantity, String supplierUOM) {

		boolean lowerThan = true;

		if (consumerUOM.equalsIgnoreCase(supplierUOM)) {
			if (consumerQuantity <= supplierQuantity) {
				lowerThan = true;
			} else {
				lowerThan = false;
			}
		} else if (isCompatible (consumerUOM, supplierUOM)) {
			if (cLowerThanS(consumerQuantity, consumerUOM, supplierQuantity, supplierUOM)) {
				lowerThan = true;
			} else {
				lowerThan = false;
			}


		} else {
			lowerThan = false;
		}

		return lowerThan;

	}

	private static boolean isCompatible (String consumerUOM, String supplierUOM) {

		boolean isCompatible = true;

		Set<String> weight = new HashSet<String>();
		Set<String> length = new HashSet<String>();

		weight.add("kg");
		weight.add("hg");
		weight.add("dag");
		weight.add("g");
		weight.add("dg");
		weight.add("cg");
		weight.add("mg");

		length.add("µm");
		length.add("mm");
		length.add("cm");
		length.add("dm");
		length.add("m");

		if ((StringUtilities.containsIgnoreCase(weight, consumerUOM) && StringUtilities.containsIgnoreCase(weight,  supplierUOM))
				|| StringUtilities.containsIgnoreCase(length, consumerUOM) && StringUtilities.containsIgnoreCase(length,  supplierUOM)) {
			isCompatible = true;
		} else {
			isCompatible = false;
		}


		return isCompatible;

	}

	private static boolean cLowerThanS (double consumerQuantity, String consumerUOM, double supplierQuantity, String supplierUOM) {

		boolean cLowerThanS = true;

		if (consumerUOM.equalsIgnoreCase("mm")) {

			if (supplierUOM.equalsIgnoreCase("mm")) {
				if (consumerQuantity <= supplierQuantity) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("cm")) {
				if (consumerQuantity <= (supplierQuantity * 10)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}

			} else if (supplierUOM.equalsIgnoreCase("dm")) {
				if (consumerQuantity <= (supplierQuantity * 100)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("m")) {
				if (consumerQuantity <= (supplierQuantity * 1000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			}

		} else if (consumerUOM.equalsIgnoreCase("mg")) {

			if (supplierUOM.equalsIgnoreCase("mg")) {
				if (consumerQuantity <= supplierQuantity) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} 	else if (supplierUOM.equalsIgnoreCase("cg")) {
				if (consumerQuantity <= (supplierQuantity * 10)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("dg")) {
				if (consumerQuantity <= (supplierQuantity * 100)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("g")) {
				if (consumerQuantity <= (supplierQuantity * 1000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("dag")) {
				if (consumerQuantity <= (supplierQuantity * 10000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("hg")) {
				if (consumerQuantity <= (supplierQuantity * 100000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("kg")) {
				if (consumerQuantity <= (supplierQuantity * 1000000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			}


		}


		return cLowerThanS;

	}

}
