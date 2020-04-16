package similarity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Jaccard {
	
	/**
	 * jaccardSetSim = [number of common elements] / [total num elements] - [number of common elements]
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static double jaccardSetSim (Set<String> set1, Set<String> set2) {		

		int intersection = 0;
		
		for (String s1 : set1) {
			for (String s2 : set2) {
				if (s1.equals(s2)) {
					intersection += 1;
				}
			}
		}

		int union = (set1.size() + set2.size()) - intersection;
		
		double jaccardSetSim = (double) intersection / (double) union;
		
		return jaccardSetSim;
	}
	
	/**
	 * jaccardSetSim = [number of common elements] / [total num elements] - [number of common elements]
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static double jaccardListSim (List<String> set1, List<String> set2) {
		

		int intersection = 0;
		
		for (String s1 : set1) {
			for (String s2 : set2) {
				if (s1.equals(s2)) {
					intersection += 1;
				}
			}
		}

		int union = (set1.size() + set2.size()) - intersection;
		
		double jaccardSetSim = (double) intersection / (double) union;
		
		return jaccardSetSim;
	}
	
	//test method
	public static void main(String[] args) {
		Set<String> set1 = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
		
		set1.add("links");
		set1.add("flight");
		set1.add("actual");
		set1.add("aircraft");
		set1.add("used");
		
		set2.add("aircraft");
		set2.add("enabling");
		set2.add("flight");
		
		double jaccard = jaccardSetSim(set1, set2);
		
		System.out.println("The jaccard is " + jaccard);
		
	}

}
