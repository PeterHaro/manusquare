package edm;

import java.util.Map;

public class Customer {
	
	private String customerName;		
	public Map<String, String> customerInfo;

	public Customer(String customerName, Map<String, String> customerInfo) {
		super();
		this.customerName = customerName;
		this.customerInfo = customerInfo;
	}

	public Customer(Map<String, String> customerInfo) {
		super();
		this.customerInfo = customerInfo;

	}
	
	public Customer() {}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public Map<String, String> getCustomerInfo() {
		return customerInfo;
	}

	
}