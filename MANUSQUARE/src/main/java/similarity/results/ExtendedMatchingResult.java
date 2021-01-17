package similarity.results;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ExtendedMatchingResult {
	
	int rank;
	String supplierId;
	Map<String, Double> matchingScores;

	
	public ExtendedMatchingResult(int rank, String supplierId, Map<String, Double> matchingScores) {
		super();
		this.rank = rank;
		this.supplierId = supplierId;
		this.matchingScores = matchingScores;
	}
	
	public ExtendedMatchingResult() {}
	
	public static List<ExtendedMatchingResult> returnEmptyResults() {
		
		List<ExtendedMatchingResult> results = new LinkedList<ExtendedMatchingResult>();
		Map<String, Double> matchingScores = new HashMap<String, Double>();
		matchingScores.put("", 0.0);
		
		results.add(new ExtendedMatchingResult(0, "", matchingScores));
		
		
		return results;
	}
	
	public static List<ExtendedMatchingResult> computeExtendedMatchingResult (Map<String, Map<String, Double>> input) {
		
		Map<String, Double> bestSuppliers = new HashMap<String, Double>();		
		
		for (Entry<String, Map<String, Double>> e : input.entrySet()) {
						
			Map<String, Double> scoreMap = sortDescending(e.getValue());
			
			if (!e.getValue().isEmpty()) {
			bestSuppliers.put(e.getKey(), Collections.max(scoreMap.values())); //get the highest score from each suppliers map
			}
			
		}		
				
		List<ExtendedMatchingResult> results = new LinkedList<ExtendedMatchingResult>();

		Map<String, Double> rankedSupplierMap = sortDescending(bestSuppliers);
		
		//product map with ranking
		Map<String, Integer> rankMap = new TreeMap<String, Integer>();
		
		double previousScore = 0;
		double thisScore = 0;
		int rank = 1;
		
		for (Entry<String, Double> e : rankedSupplierMap.entrySet()) {
			thisScore = e.getValue();
			rankMap.put(e.getKey(), rank);
			
			
			if (thisScore < previousScore) {
				rank++;
				rankMap.put(e.getKey(), rank);
				previousScore = thisScore;
			} else {
				rankMap.put(e.getKey(), rank);
				previousScore = thisScore;
			}

		}

		
		for (Entry<String, Double> e : rankedSupplierMap.entrySet()) {

			Map<String, Double> supplierScore = sortDescending(input.get(e.getKey()));
			results.add(new ExtendedMatchingResult(rankMap.get(e.getKey()), e.getKey(), supplierScore));
		}

		return results;
		
		
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


	public Map<String, Double> getByProductScores() {
		return matchingScores;
	}

	public void setByProductScores(Map<String, Double> byProductScores) {
		this.matchingScores = byProductScores;
	}
	
	
	
	/**
	 * Sorts a map based on similarity scores (values in the map)
	 *
	 * @param map the input map to be sorted
	 * @return map with sorted values
	 * May 16, 2019
	 */
	private static <K, V extends Comparable<V>> Map<K, V> sortDescending(final Map<K, V> map) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = map.get(k2).compareTo(map.get(k1));
				if (compare == 0) return 1;
				else return compare;
			}
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);

		sortedByValues.putAll(map);

		return sortedByValues;
	}
	

}
