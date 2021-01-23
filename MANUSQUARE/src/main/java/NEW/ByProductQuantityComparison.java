package NEW;

import java.util.HashSet;
import java.util.Set;

import utilities.StringUtilities;

public class ByProductQuantityComparison {

	public static boolean supplyTypeReqSatisfied (String consumerSupplyType, double consumerQuantity, String consumerUOM, String supplierSupplyType, double supplierQuantity, double supplierMinQuantity, String supplierUOM) {
		
		System.out.println("consumerSupplyType: " + consumerSupplyType);
		System.out.println("supplierSupplyType: " + supplierSupplyType);
		System.out.println("consumerQuantity: " + consumerQuantity);
		System.out.println("supplierQuantity: " + supplierQuantity);
		System.out.println("supplierMinQuantity: " + supplierMinQuantity);
		System.out.println("consumerUOM: " + consumerUOM);
		System.out.println("supplierUOM: " + supplierUOM);

		boolean reqSatisfied = true;
		boolean consumerQuantityLowerThanSupplierQuantity = lowerThan(consumerQuantity, consumerUOM, supplierQuantity, supplierMinQuantity, supplierUOM);
		System.out.println("consumerQuantityLowerThanSupplierQuantity: " + consumerQuantityLowerThanSupplierQuantity);
		System.out.println("lowerThan: " + lowerThan(consumerQuantity, consumerUOM, supplierQuantity, supplierMinQuantity, supplierUOM));

		//FIXME: Assuming that supplyTypeReq is satisfied if the consumer has quantity = "" (parsed into 0.0 which is not relevant in any other case)
		if (consumerQuantity == 0.0 || consumerSupplyType.equalsIgnoreCase("ANY")) {
			reqSatisfied = true;

		} else {

			if (consumerSupplyType.equalsIgnoreCase("continuous")) {
				if (!supplierSupplyType.equalsIgnoreCase("continuous") || !consumerQuantityLowerThanSupplierQuantity) {
					reqSatisfied = false;
				}
			} else if (consumerSupplyType.equalsIgnoreCase("SINGLE_BATCH") || consumerSupplyType.equalsIgnoreCase("Single batch")) {
				if (!consumerQuantityLowerThanSupplierQuantity) {
					reqSatisfied = false;
				} 
			}
		}

		return reqSatisfied;

	}

	public static boolean lowerThan (double consumerQuantity, String consumerUOM, double supplierQuantity, double supplierMinQuantity, String supplierUOM) {

		boolean lowerThan = true;

		//if consumerQuantity is lower than supplierMinQuantity the supplier is not interested
		if (consumerQuantity < supplierMinQuantity) {
			lowerThan = false;
		} else if (consumerUOM.equalsIgnoreCase(supplierUOM)) {
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

		} else if (consumerUOM.equalsIgnoreCase("m")) {

			if (supplierUOM.equalsIgnoreCase("m")) {
				if (consumerQuantity <= supplierQuantity) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("cm")) {
				if (consumerQuantity <= (supplierQuantity / 10)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}

			} else if (supplierUOM.equalsIgnoreCase("dm")) {
				if (consumerQuantity <= (supplierQuantity / 100)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("mm")) {
				if (consumerQuantity <= (supplierQuantity / 1000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			}


		}

		else if (consumerUOM.equalsIgnoreCase("mg")) {

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

		else if (consumerUOM.equalsIgnoreCase("g")) {

			if (supplierUOM.equalsIgnoreCase("g")) {
				if (consumerQuantity <= supplierQuantity) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} 	else if (supplierUOM.equalsIgnoreCase("cg")) {
				if (consumerQuantity <= (supplierQuantity / 100)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("dg")) {
				if (consumerQuantity <= (supplierQuantity / 10)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("mg")) {
				if (consumerQuantity <= (supplierQuantity / 1000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("dag")) {
				if (consumerQuantity <= (supplierQuantity * 10)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("hg")) {
				if (consumerQuantity <= (supplierQuantity * 100)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("kg")) {
				if (consumerQuantity <= (supplierQuantity * 1000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			}


		} else if (consumerUOM.equalsIgnoreCase("kg")) {

			if (supplierUOM.equalsIgnoreCase("kg")) {
				if (consumerQuantity <= supplierQuantity) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} 	else if (supplierUOM.equalsIgnoreCase("hg")) {
				if (consumerQuantity <= (supplierQuantity / 10)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("dag")) {
				if (consumerQuantity <= (supplierQuantity / 100)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("g")) {
				if (consumerQuantity <= (supplierQuantity / 1000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("dg")) {
				if (consumerQuantity <= (supplierQuantity / 10000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("cg")) {
				if (consumerQuantity <= (supplierQuantity / 100000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			} else if (supplierUOM.equalsIgnoreCase("mg")) {
				if (consumerQuantity <= (supplierQuantity / 1000000)) {
					cLowerThanS = true;
				} else {
					cLowerThanS = false;
				}
			}


		}


		return cLowerThanS;

	}


}
