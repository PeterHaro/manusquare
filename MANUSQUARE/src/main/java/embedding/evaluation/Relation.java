package embedding.evaluation;

public class Relation {
	
	private String source;
	private String target;
	private double sim;
	
	public Relation(String source, String target, double sim) {
		super();
		this.source = source;
		this.target = target;
		this.sim = sim;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public double getSim() {
		return sim;
	}

	public void setSim(double sim) {
		this.sim = sim;
	}
	
	public String toString() {
		return this.source + ";" + this.target + ";" + this.sim;
	}

}
