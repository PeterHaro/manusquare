package edm;

import java.util.Set;

public class ProcessChain {
	
	private float meanTime;
	private String processChainID;
	private float additionaTime;
	private String processChainName;
	private float throughputTime;
	private String description;
	private String version;
	private Set<ByProduct> processes;
	
	public ProcessChain(float meanTime, String processChainID, float additionaTime, String processChainName,
			float throughputTime, String description, String version, Set<ByProduct> processes) {
		super();
		this.meanTime = meanTime;
		this.processChainID = processChainID;
		this.additionaTime = additionaTime;
		this.processChainName = processChainName;
		this.throughputTime = throughputTime;
		this.description = description;
		this.version = version;
		this.processes = processes;
	}
	
	public ProcessChain() {}

	public float getMeanTime() {
		return meanTime;
	}

	public void setMeanTime(float meanTime) {
		this.meanTime = meanTime;
	}

	public String getProcessChainID() {
		return processChainID;
	}

	public void setProcessChainID(String processChainID) {
		this.processChainID = processChainID;
	}

	public float getAdditionaTime() {
		return additionaTime;
	}

	public void setAdditionaTime(float additionaTime) {
		this.additionaTime = additionaTime;
	}

	public String getProcessChainName() {
		return processChainName;
	}

	public void setProcessChainName(String processChainName) {
		this.processChainName = processChainName;
	}

	public float getThroughputTime() {
		return throughputTime;
	}

	public void setThroughputTime(float throughputTime) {
		this.throughputTime = throughputTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Set<ByProduct> getProcesses() {
		return processes;
	}

	public void setProcesses(Set<ByProduct> processes) {
		this.processes = processes;
	}
	
	
	
	
	

}
