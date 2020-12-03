package utilities;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilities {
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	
	
	/**
	 * Removes the IRIs in front of processes etc. retrieved from the Semantic Infrastructure
	 *
	 * @param inputConcept an input ontology concept (with full IRI)
	 * @return ontology concept with the IRI removed
	 * Nov 5, 2019
	 */
	public static String stripIRI(String inputConcept) {
		String returnedConceptName = null;
		if (inputConcept.contains("http://manusquare.project.eu/industrial-manusquare#")) {
			returnedConceptName = inputConcept.replaceAll("http://manusquare.project.eu/industrial-manusquare#", "");
		} else if (inputConcept.contains("http://manusquare.project.eu/core-manusquare#")) {
			returnedConceptName = inputConcept.replaceAll("http://manusquare.project.eu/core-manusquare#", "");
		} else {
			returnedConceptName = inputConcept;
		}
		return returnedConceptName;

	}
	
	/**
	 * Checks if a set of strings includes the given string while ignoring the casing
	 * @param set set of strings
	 * @param soughtFor the string to check if exists in the set of strings
	 * @return true if the set of strings contains the given string, false otherwise
	   Mar 27, 2020
	 */
	public static boolean containsIgnoreCase(Set<String> set, String soughtFor) {
		for (String current : set) {
			if (current.equalsIgnoreCase(soughtFor)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if a target set of strings includes all strings in a source set of strings while ignoring the casing
	 * @param sourceSet the source set of strings
	 * @param targetSet the target set of strings
	 * @return true if the target set of strings contains all strings in the source set of strings, false otherwise
	   Sept 2, 2020
	 */
	public static boolean containsAllIgnoreCase(Set<String> sourceSet, Set<String> targetSet) {
		
		int itemsInSourceSet = sourceSet.size();
		int numMatch = 0;
		
		for (String source : sourceSet) {
			for (String target : targetSet) {
				if (source.equalsIgnoreCase(target)) {
					numMatch++;
				}
			}

		}
		
		if (numMatch == itemsInSourceSet) {
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * Checks if a target set of strings includes all strings in a source set of strings while ignoring the casing
	 * @param sourceSet the source set of strings
	 * @param targetSet the target set of strings
	 * @return true if the target set of strings contains all strings in the source set of strings, false otherwise
	   Sept 2, 2020
	 */
	public static boolean containsAllIgnoreCase(List<String> sourceSet, List<String> targetSet) {
		
		int itemsInSourceSet = sourceSet.size();
		int numMatch = 0;
		
		for (String source : sourceSet) {
			for (String target : targetSet) {
				if (source.equalsIgnoreCase(target)) {
					numMatch++;
				}
			}

		}
		
		if (numMatch == itemsInSourceSet) {
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * Capitalises each word
	 * @param str input string
	 * @return string where each word is capitalised
	   Mar 4, 2020
	 */
	public static String capitaliseWord(String str){  
		System.err.println("StringUtilities: str is " + str);
	    String words[]=str.split("\\s");  
	    String capitaliseWord="";  
	    for(String w:words){  
	        String first=w.substring(0,1);  
	        String afterfirst=w.substring(1);  
	        capitaliseWord+=first.toUpperCase()+afterfirst+" ";  
	    }  
	    return capitaliseWord.trim();  
	}  

	/**
	 * Returns ONE string by random from a list of strings
	 * @param listOfStrings
	 * @return
	   Jul 2, 2019
	 */
	public static String getRandomString1(List<String> listOfStrings) {
		Random rand = new Random();
		String returnedString = null;

		int numberOfElements = 1;

		for (int i = 0; i < numberOfElements; i++) {
			int randomIndex = rand.nextInt(listOfStrings.size());
			returnedString = listOfStrings.get(randomIndex);

		}

		return returnedString;
	}

	/**
	 * Returns THREE string by random from a list of strings
	 * @param listOfStrings
	 * @return
	   Jul 2, 2019
	 */
	public static Set<String> getRandomString3(List<String> listOfStrings) {
		Random rand = new Random();
		Set<String> returnedStrings = new HashSet<String>();

		int numberOfElements = 3;

		for (int i = 0; i < numberOfElements; i++) {
			int randomIndex = rand.nextInt(listOfStrings.size());
			returnedStrings.add(listOfStrings.get(randomIndex));
		}

		return returnedStrings;
	}

	/**
	 * Returns ONE integer by random from a list of integers
	 * @param list
	 * @return
	   Jul 2, 2019
	 */
	public static int getRandomInt1(List<Integer> list) {
		Random rand = new Random();
		int returnedString = 0;

		int numberOfElements = 1;

		for (int i = 0; i < numberOfElements; i++) {
			int randomIndex = rand.nextInt(list.size());
			returnedString = list.get(randomIndex);

		}

		return returnedString;
	}

	public static String splitCompounds(String input) {
		String[] compounds = input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		StringBuilder sb = new StringBuilder();

		for (String compound : compounds) {
			sb.append(compound).append(" ");
		}
		return sb.toString();
	}

	public static void fixAlignmentName(String folder) throws IOException {
		File allFiles = new File(folder);
		File[] files = allFiles.listFiles();
		System.err.println("Number of files: " + Objects.requireNonNull(files).length);

		String fileName = null;
		File newFile = null;

		for (File file : files) {
			fileName = file.getName();
			file.renameTo(new File(fileName = "./files/DBLP-Scholar/alignments/new/" + fileName.replaceAll("[^a-zA-Z0-9.-]", "_")));
		}
	}


	/**
	 * Takes a string as input and returns an arraylist of tokens from this string
	 *
	 * @param s:         the input string to tokenize
	 * @param lowercase: if the output tokens should be lowercased
	 * @return an ArrayList of tokens
	 */
	public static ArrayList<String> tokenize(String s, boolean lowercase) {
		if (s == null) {
			return null;
		}

		ArrayList<String> strings = new ArrayList<>();
		performTokinization(s, lowercase, strings);
		return strings;
	}

	private static void performTokinization(String s, boolean lowercase, Collection<String> strings) {
		String current = "";
		Character prevC = 'x';
		for (Character c : s.toCharArray()) {
			if ((Character.isLowerCase(prevC) && Character.isUpperCase(c)) ||
					c == '_' || c == '-' || c == ' ' || c == '/' || c == '\\' || c == '>') {
				current = current.trim();
				if (current.length() > 0) {
					if (lowercase)
						strings.add(current.toLowerCase());
					else
						strings.add(current);
				}
				current = "";
			}

			if (c != '_' && c != '-' && c != '/' && c != '\\' && c != '>') {
				current += c;
				prevC = c;
			}
		}

		current = current.trim();
		if (current.length() > 0) {
			// this check is to handle the id numbers in YAGO
			if (!(current.length() > 4 && Character.isDigit(current.charAt(0)) &&
					Character.isDigit(current.charAt(current.length() - 1)))) {
				strings.add(current.toLowerCase());
			}
		}
	}

	/**
	 * Takes a string as input and returns set of tokens from this string
	 *
	 * @param s:         the input string to tokenize
	 * @param lowercase: if the output tokens should be lowercased
	 * @return a set of tokens
	 */
	public static Set<String> tokenizeToSet(String s, boolean lowercase){
		if (s == null) {
			return null;
		}

		String stringWOStopWords = removeStopWords(s);
		Set<String> strings = new HashSet<>();
		performTokinization(stringWOStopWords, lowercase, strings);
		return strings;
	}


	/**
	 * Returns a string of tokens
	 *
	 * @param s:         the input string to be tokenized
	 * @param lowercase: whether the output tokens should be in lowercase
	 * @return a string of tokens from the input string
	 */
	public static String stringTokenize(String s, boolean lowercase) {
		StringBuilder result = new StringBuilder();
		ArrayList<String> tokens = tokenize(s, lowercase);
		for (String token : tokens) {
			result.append(token).append(" ");
		}
		return result.toString().trim();
	}


	/**
	 * Removes prefix from property names (e.g. hasCar is transformed to car)
	 *
	 * @param s: the input property name to be
	 * @return a string without any prefix
	 */
	public static String stripPrefix(String s) {
		if (s.startsWith("has")) {
			s = s.replaceAll("^has", "");
		} else if (s.startsWith("is")) {
			s = s.replaceAll("^is", "");
		} else if (s.startsWith("is_a_")) {
			s = s.replaceAll("^is_a_", "");
		} else if (s.startsWith("has_a_")) {
			s = s.replaceAll("^has_a_", "");
		} else if (s.startsWith("was_a_")) {
			s = s.replaceAll("^was_a_", "");
		} else if (s.endsWith("By")) {
			s = s.replaceAll("By", "");
		} else if (s.endsWith("_by")) {
			s = s.replaceAll("_by^", "");
		} else if (s.endsWith("_in")) {
			s = s.replaceAll("_in^", "");
		} else if (s.endsWith("_at")) {
			s = s.replaceAll("_at^", "");
		}
		s = s.replaceAll("_", " ");
		s = stringTokenize(s, true);

		return s;
	}

	public static String removeSymbols(String s) {
		s = s.replace("\"", "");
		s = s.replace(".", "");
		s = s.replace("@en", "");

		return s;
	}

	/**
	 * Takes a filename as input and removes the IRI prefix from this file
	 *
	 * @param fileName
	 * @return filename - without IRI
	 */
	public static String stripPath(String fileName) {
		return fileName.substring(fileName.lastIndexOf("/") + 1);

	}


	/**
	 * Returns the label from on ontology concept without any prefix
	 *
	 * @param label: an input label with a prefix (e.g. an IRI prefix)
	 * @return a label without any prefix
	 */
	public static String getString(String label) {
		if (label.contains("#")) {
			label = label.substring(label.indexOf('#') + 1);
			return label;
		}

		if (label.contains("/")) {
			label = label.substring(label.lastIndexOf('/') + 1);
			return label;
		}
		return label;
	}


	/**
	 * Removes underscores from a string (replaces underscores with "no space")
	 *
	 * @param input: string with an underscore
	 * @return string without any underscores
	 */
	public static String replaceUnderscore(String input) {
		String newString = null;
		Pattern p = Pattern.compile("_([a-zA-Z])");
		Matcher m = p.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase());
		}

		m.appendTail(sb);
		newString = sb.toString();

		return newString;
	}

	/**
	 * Checks if an input string is an abbreviation (by checking if there are two consecutive uppercased letters in the string)
	 *
	 * @param s input string
	 * @return boolean stating whether the input string represents an abbreviation
	 */
	public static boolean isAbbreviation(String s) {

		boolean isAbbreviation = false;
		int counter = 0;
		for (int i = 0; i < s.length(); i++) {
			if (Character.isUpperCase(s.charAt(i))) {
				counter++;
			}
			isAbbreviation = counter > 2;
		}
		return isAbbreviation;
	}

	/**
	 * Returns the names of the ontology from the full file path (including owl or rdf suffixes)
	 *
	 * @param ontology name without suffix
	 * @return
	 */
	public static String stripOntologyName(String fileName) {
		String trimmedPath = fileName.substring(fileName.lastIndexOf("/") + 1);
		String owl = ".owl";
		String rdf = ".rdf";
		String stripped = null;

		if (fileName.endsWith(".owl")) {
			stripped = trimmedPath.substring(0, trimmedPath.indexOf(owl));
		} else {
			stripped = trimmedPath.substring(0, trimmedPath.indexOf(rdf));
		}

		return stripped;
	}

	/**
	 * Returns the full IRI of an input ontology
	 *
	 * @param o the input OWLOntology
	 * @return the IRI of an OWLOntology
	 */
	public static String getOntologyIRI(OWLOntology o) {
		return o.getOntologyID().getOntologyIRI().toString();
	}

	/**
	 * Convert from a filename to a file URL.
	 */
	public static String convertToFileURL(String filename) {
		String path = new File(filename).getAbsolutePath();
		if (File.separatorChar != '/') {
			path = path.replace(File.separatorChar, '/');
		}

		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return "file:" + path;
	}

	public static String validateRelationType(String relType) {
		if (relType.equals("<")) {
			relType = "&lt;";
		}
		return relType;
	}

	public static String removeStopWords (String inputString) {

		List<String> stopWordsList = Arrays.asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", 
				"he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", 
				"which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having",
				"do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", 
				"about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", 
				"off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", 
				"more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just",
				"don", "should", "now");

		String[] words = inputString.split(" ");
		ArrayList<String> wordsList = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();

		for(String word : words)
		{
			String wordCompare = word.toLowerCase();
			if(!stopWordsList.contains(wordCompare))
			{
				wordsList.add(word);
			}
		}

		for (String str : wordsList){
			sb.append(str + " ");
		}

		return sb.toString();
	}

	/**
	 * Takes as input a String and produces an array of Strings from this String
	 *
	 * @param s
	 * @return result
	 */
	public static String[] split(String s) {
		return s.split(" ");
	}
	
	public static String[] getCompoundParts(String input) {
		
		return input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
	}


	public static boolean isCompoundWord(String input) {
		String[] compounds = getCompoundParts(input);
		return compounds.length > 1 && !StringUtils.isAllUpperCase(input);

	}

	public static String splitCompoundString(String input) {
		StringBuilder splitCompound = new StringBuilder();
		String[] compounds = getCompoundParts(input);
		for (String compound : compounds) {
			splitCompound.append(" ").append(compound);
		}

		return splitCompound.toString();

	}

	public static String getCompoundWordWithSpaces(String s) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> compoundWordsList = getWordsFromCompound(s);
		for (String word : compoundWordsList) {
			sb.append(word).append(" ");
		}
		return sb.toString();
	}

	public static String getCompoundHead(String input) {
		if (isCompoundWord(input)) {
			String[] compounds = getCompoundParts(input);
			return compounds[compounds.length - 1];
		} else {
			return null;
		}
	}

	public static String getCompoundQualifier(String input) {
		String[] compounds = getCompoundParts(input);
		return compounds[0];
	}

	public static String getCompoundModifier(String s) {
		return s.replace(Objects.requireNonNull(getCompoundHead(s)), "");
	}

	public static ArrayList<String> getWordsFromCompound(String input) {
		String[] compounds = getCompoundParts(input);
		return new ArrayList<>(Arrays.asList(compounds));

	}

	public static Set<String> getWordsAsSetFromCompound(String input) {
		String[] compounds = getCompoundParts(input);
		return new HashSet<>(Arrays.asList(compounds));

	}

	public static int countCharsInString(String s) {
		int counter = 0;
		for (int i = 0; i < s.length(); i++) {
			if (Character.isLetter(s.charAt(i)))
				counter++;
		}
		return counter;
	}

	/**
	 * prints each (string) item in a set of items
	 * @param set of strings
	 * @return sequenced string of certifications separated by commas
   	   Oct 12, 2019
	 */
	public static String printSetItems(Set<String> set) {
		StringBuffer sb = new StringBuffer();
		
		if (!set.isEmpty()) {
		
		for (String s : set) {
			sb.append(s + ", ");
		}

		String setItem = sb.deleteCharAt(sb.lastIndexOf(",")).toString();

		return setItem;
		} else {
			return null;
		}

	}
	
	/**
	 * prints each (string) item in a list of items
	 * @param list of strings
	 * @return sequenced string of certifications separated by commas
   	   Oct 12, 2019
	 */
	public static String printListItems(List<String> list) {
		StringBuffer sb = new StringBuffer();
		
		if (!list.isEmpty()) {
		
		for (String s : list) {
			sb.append(s + ", ");
		}

		String listItem = sb.deleteCharAt(sb.lastIndexOf(",")).toString();

		return listItem;
		} else {
			return null;
		}

	}
	
	/**
	 * prints each (string) item with quotes from a set of string items
	 * @param set of strings
	 * @return sequenced string of languages surrounded by quotes and separated by commas
	   May 4, 2020
	 */
	public static String printLanguageSetItems(Set<String> set) {
		StringBuffer sb = new StringBuffer();
		
		if (!set.isEmpty()) {
		
		for (String s : set) {
			sb.append("\"" + s + "\"" + ", ");
		}

		String setItem = sb.deleteCharAt(sb.lastIndexOf(",")).toString();

		return setItem;
		} else {
			return null;
		}

	}

	public static boolean isValidNumber(String input) {
		final String Digits     = "(\\p{Digit}+)";
		final String HexDigits  = "(\\p{XDigit}+)";
		// an exponent is 'e' or 'E' followed by an optionally
		// signed decimal integer.
		final String Exp        = "[eE][+-]?"+Digits;
		final String fpRegex    =
				("[\\x00-\\x20]*"+  // Optional leading "whitespace"
						"[+-]?(" + // Optional sign character
						"NaN|" +           // "NaN" string
						"Infinity|" +      // "Infinity" string

						// A decimal floating-point string representing a finite positive
						// number without a leading sign has at most five basic pieces:
						// Digits . Digits ExponentPart FloatTypeSuffix
						//
						// Since this method allows integer-only strings as input
						// in addition to strings of floating-point literals, the
						// two sub-patterns below are simplifications of the grammar
						// productions from section 3.10.2 of
						// The Java Language Specification.

						// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
						"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

						// . Digits ExponentPart_opt FloatTypeSuffix_opt
						"(\\.("+Digits+")("+Exp+")?)|"+

						// Hexadecimal strings
						"((" +
						// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
						"(0[xX]" + HexDigits + "(\\.)?)|" +

						// 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
						"(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

						")[pP][+-]?" + Digits + "))" +
						"[fFdD]?))" +
						"[\\x00-\\x20]*");// Optional trailing "whitespace"

		return Pattern.matches(fpRegex, input);
	}

	public static void main(String[] args) {
		
		Set<String> consumerMaterials = new HashSet<String>();
		consumerMaterials.add("StainlessSteel");
		consumerMaterials.add("CarbonSteel");
		
		Set<String> supplierMaterials = new HashSet<String>();
		supplierMaterials.add("Stainlesssteel");
		supplierMaterials.add("Carbonsteel");
		
		if (containsAllIgnoreCase(consumerMaterials, supplierMaterials)) {
			System.out.println("True");
		} else {
			System.out.println("False");
		}
		
	}


}
