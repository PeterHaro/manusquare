package testing;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TestRanking {
	
	public static void main(String[] args) {
		
	
	Map<String, Double> rankedSupplierMap = new TreeMap<String, Double>();
	
	rankedSupplierMap.put("SUP1", 0.79);
	rankedSupplierMap.put("SUP2", 0.72);
	rankedSupplierMap.put("SUP3", 0.68);
	rankedSupplierMap.put("SUP4", 0.68);
	rankedSupplierMap.put("SUP5", 0.63);
		
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
	
//	for (Entry<String, Double> e : rankedSupplierMap.entrySet()) {
//		thisScore = e.getValue();
//		
//		if (thisScore >= previousScore) {
//		
//		rankMap.put(e.getKey(), rank);
//		previousScore = thisScore;
//		}
//		
//		else if (thisScore < previousScore) {
//			rank++;
//			rankMap.put(e.getKey(), rank);
//			previousScore = thisScore;
//		}
//	}

	System.out.println(rankMap);
	
	}	
}
