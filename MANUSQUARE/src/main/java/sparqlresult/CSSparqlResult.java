package sparqlresult;

import java.util.Map;
import java.util.Set;

public class CSSparqlResult extends SparqlResult {
	
	private String processChainId;
	private String process;

	
	public CSSparqlResult(String processChainId, String supplierId, String process, String material,
			String certification, Set<String> attributes,double attributeWeight) {
		super(supplierId, material, certification, attributes, attributeWeight);
		this.processChainId = processChainId;
		this.process = process;

	}
	
	public CSSparqlResult(String processChainId, String supplierId, String process, String material,
			String certification, Set<String> attributes, Map<String, String> attributeWeightMap) {
		super(supplierId, material, certification, attributes, attributeWeightMap);
		this.processChainId = processChainId;
		this.process = process;

	}
	
	
	public CSSparqlResult(String processChainId, String supplierId, String process, String material,
			String certification) {
		super(supplierId, material, certification);
		this.processChainId = processChainId;
		this.process = process;

	}
	
	public CSSparqlResult(String supplierId, String process, String material,
			String certification) {
		super(supplierId, material, certification);
		this.process = process;
	}


	public CSSparqlResult() {
		super();

	}

	public String getProcessChainId() {
		return processChainId;
	}

	public void setProcessChainId(String processChainId) {
		this.processChainId = processChainId;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

}
