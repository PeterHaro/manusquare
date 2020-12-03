package NEW;

import utilities.MathUtils;

public class ByProductQuantityComparison {
	
	public static boolean supplyTypeReqSatisfied (String consumerSupplyType, double consumerQuantity, String consumerUOM, String supplierSupplyType, double supplierQuantity, double supplierMinQuantity, String supplierUOM) {
		
		boolean reqSatisfied = true;
		boolean consumerQuantityLowerThanSupplierQuantity = MathUtils.lowerThan(consumerQuantity, consumerUOM, supplierQuantity, supplierMinQuantity, supplierUOM);
		
		if (consumerSupplyType.equalsIgnoreCase("continuous")) {
			if (!supplierSupplyType.equalsIgnoreCase("continuous") || !consumerQuantityLowerThanSupplierQuantity) {
				reqSatisfied = false;
			}
		} else if (consumerSupplyType.equalsIgnoreCase("limited amount")) {
			if (!consumerQuantityLowerThanSupplierQuantity) {
				reqSatisfied = false;
		}
		}
		
		return reqSatisfied;
		
	}
	
	//	boolean consumerQuantityLowerThanSupplierQuantity = MathUtils.lowerThan(consumerQuantity, consumerUOM, supplierQuantity, supplierUOM);
	
	//debuggingOutput.append("\nIs consumer quantity lower than supplier quantity?: " + consumerQuantityLowerThanSupplierQuantity);
	
//	if (consumerSupplyType.equalsIgnoreCase("continuous")) {
//		if (!supplierSupplyType.equalsIgnoreCase("continuous") || !consumerQuantityLowerThanSupplierQuantity) {
//			debuggingOutput.append("\nConsumer supply type is continuous and supplier supply type is either not continuous or offers a lower quantity.");
//			finalByProductSim = 0;
//		}
//	} else if (consumerSupplyType.equalsIgnoreCase("limited amount")) {
//		if (!consumerQuantityLowerThanSupplierQuantity) {
//			debuggingOutput.append("\nConsumer supply type is limited amount, but the quantiy offered by the supplier is lower than what is requested by the consumer.");
//			finalByProductSim = 0;
//	}
//	}

}
