package similarity.results;

public class MatchingResult {
	
	int rank;
	String supplierId;
	double matchingScore;
	
	public MatchingResult(int rank, String supplierId, double matchingScore) {
		super();
		this.rank = rank;
		this.supplierId = supplierId;
		this.matchingScore = matchingScore;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public double getMatchingScore() {
		return matchingScore;
	}

	public void setMatchingScore(double matchingScore) {
		this.matchingScore = matchingScore;
	}
	
	
	
	

}
