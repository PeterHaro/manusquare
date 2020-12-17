package NEW;

public class PurchaseGroupAbilitation {

	public static boolean validPurchaseGroupAbilitation (String consumerPGA, int consumerMinParticipants, int consumerMaxParticipants, String supplierPGA, int supplierMinParticipants, int supplierMaxParticipants) {

		boolean validPGA = false;

		//FIXME: According to the specs from SUPSI it can be ignored if minParticipants or maxParticipants are zero or "". 
		if (consumerPGA.equals("") || consumerPGA.equals("false") || consumerMinParticipants == 0 || consumerMaxParticipants == 0) {
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
