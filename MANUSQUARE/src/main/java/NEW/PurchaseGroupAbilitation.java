package NEW;

public class PurchaseGroupAbilitation {

	public static boolean validPurchaseGroupAbilitation (String consumerPGA, int consumerMinParticipants, int consumerMaxParticipants, String supplierPGA, int supplierMinParticipants, int supplierMaxParticipants) {

		boolean validPGA = false;

		if (consumerPGA.equals("") || consumerPGA.equals("false")) {
			validPGA = true;
		} else if (consumerPGA.equals("true")) {
			if (supplierPGA.equals("false")) {
				validPGA = false;
			} else if (supplierPGA.equals("true")) {
				if ((consumerMinParticipants >= supplierMinParticipants) && (consumerMaxParticipants <= supplierMaxParticipants)) {
					validPGA = true;
				} else {
					validPGA = false;
				}
			}
		}


		return validPGA;

	}

}
