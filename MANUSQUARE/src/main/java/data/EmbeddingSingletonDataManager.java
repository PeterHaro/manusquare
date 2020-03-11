package data;

import embedding.vectoraggregation.VectorAggregationMethod;
import utilities.StringUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class EmbeddingSingletonDataManager {
    private static final int NUM_VECTOR_DIMS = 300;
    private static final String EMBEDDING_FILE = "../files/EMBEDDINGS/manusquare_wikipedia_trained_NN_VBG.txt";
    private static Map<String, double[]> vectorMap = null;

    private static final EmbeddingSingletonDataManager instance = new EmbeddingSingletonDataManager();

    public static final VectorAggregationMethod VAM = VectorAggregationMethod.AVG;

    public static EmbeddingSingletonDataManager getInstance() {
        return instance;
    }

    public Map<String, double[]> getVectorMap() {
        return vectorMap;
    }

    private EmbeddingSingletonDataManager() {
        try {
            vectorMap = createVectorMap(EMBEDDING_FILE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a map holding a class as key along with an array of vectors as value.
     *
     * @return a Map<String, double[]) representing classes and corresponding embedding vectors.
     */
    public Map<String, double[]> createOntologyVectorMap(Set<String> ontologyClasses, VectorAggregationMethod vectorAggregationMethod) {
        Map<String, double[]> vectors = new HashMap<String, double[]>();
        //create the vector map from the source vector file
        double[] labelVector;
        for (String s : ontologyClasses) {
            //if the embeddings file contains the whole concept name as-is
            if (vectorMap.containsKey(s.toLowerCase())) {
                labelVector = getLabelVector(s, vectorAggregationMethod);
                vectors.put(s.toLowerCase(), labelVector);
                //check if any of the compound parts are included in the embeddings file
            } else if (StringUtilities.isCompoundWord(s)) {
                String[] compounds = StringUtilities.getCompoundParts(s);
                List<double[]> compoundsWithVectors = new ArrayList<double[]>();
                for (int i = 0; i < compounds.length; i++) {
                    if (vectorMap.containsKey(compounds[i].toLowerCase())) {
                        double[] vectorArray = vectorMap.get(compounds[i].toLowerCase());
                        compoundsWithVectors.add(vectorArray);
                    }
                }
                vectors.put(s.toLowerCase(), getAVGVectors(compoundsWithVectors));
            }
        }
        return vectors;
    }

    /**
     * Takes a file of words and corresponding vectors and creates a Map where the word in each line is key and the vectors are values (as ArrayList<Double>)
     *
     * @param vectorFile A file holding a word and corresponding vectors on each line
     * @return A Map<String, ArrayList<Double>> where the key is a word and the value is a list of corresponding vectors
     * @throws FileNotFoundException
     */
    private static Map<String, double[]> createVectorMap(String vectorFile) throws FileNotFoundException {
        Map<String, double[]> vectorMap = new HashMap<String, double[]>();
        Scanner sc = new Scanner(new File(vectorFile));
        //read the file holding the vectors and extract the concept word (first word in each line) as key and the vectors as ArrayList<Double> as value in a Map
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] strings = line.split(" ");
            //get the word, not the vectors
            String word1 = strings[0];
            //initialize an array that includes only the vector entries
            double[] labelVectorArray = new double[strings.length - 1];
            for (int i = 1; i < strings.length; i++) {
                labelVectorArray[i - 1] = Double.valueOf(strings[i]);
            }

            //put the word and associated vectors in the vectormap
            vectorMap.put(word1, labelVectorArray);
        }
        sc.close();

        return vectorMap;
    }

    /**
     * Receives a list of vectors and averages each vector component
     *
     * @param a_input list of vectors
     * @return an averaged vector
     * Mar 3, 2020
     */
    private static double[] getAVGVectors(List<double[]> a_input) {
        double[] avg = new double[EmbeddingSingletonDataManager.NUM_VECTOR_DIMS];
        double[] temp = new double[EmbeddingSingletonDataManager.NUM_VECTOR_DIMS];
        for (double[] singleVector : a_input) {
            for (int i = 0; i < temp.length; i++) {
                temp[i] += singleVector[i];
            }
        }

        for (int i = 0; i < temp.length; i++) {
            avg[i] = temp[i] / (double) a_input.size();
        }
        return avg;
    }

    /**
     * Receives a list of vectors and sums each vector component
     *
     * @param a_input list of vectors
     * @return a summed vector
     * Mar 3, 2020
     */
    private static double[] getSummedVectors(List<double[]> a_input) {
        double[] sum = new double[EmbeddingSingletonDataManager.NUM_VECTOR_DIMS];
        for (double[] singleVector : a_input) {
            for (int i = 0; i < sum.length; i++) {
                sum[i] += singleVector[i];
            }
        }
        return sum;
    }

    /**
     * Checks if the vectorMap contains the label of an OWL class as key and if so the vectors of the label are returned.
     *
     * @return a set of vectors (as a string) associated with the label
     */
    public static double[] getLabelVector(String label, VectorAggregationMethod vectorAggregationMethod) {
        List<double[]> aggregatedLabelVectors = new ArrayList<>();
        double[] labelVectorArray = new double[EmbeddingSingletonDataManager.NUM_VECTOR_DIMS];
        double[] localVectorArray;

        //if the class name is not a compound, turn it into lowercase,
        if (!StringUtilities.isCompoundWord(label)) {
            String lcLabel = label.toLowerCase();
            //if the class name is in the vectormap, get its vectors
            labelVectorArray = vectorMap.getOrDefault(lcLabel, null);

            //if the class name is a compound, split the compounds, and if the vectormap contains ANY of the compounds, extract the vectors from
            //the compound parts and average them in order to return the vector for the compound class name
            //TODO: The compound head should probably have more impact on the score than the compound modifiers
        } else if (StringUtilities.isCompoundWord(label)) {
            //get the compound parts and check if any of them are in the vector file
            String[] compounds = StringUtilities.getCompoundParts(label);
            for (String compound : compounds) {
                if (vectorMap.containsKey(compound.toLowerCase())) {
                    localVectorArray = vectorMap.get(compound.toLowerCase());
                    aggregatedLabelVectors.add(localVectorArray);
                } else {
                    labelVectorArray = null;
                }
            }

            //average or sum all vector arraylists
            if (vectorAggregationMethod == VectorAggregationMethod.AVG) {
                labelVectorArray = getAVGVectors(aggregatedLabelVectors);
            } else if (vectorAggregationMethod == VectorAggregationMethod.SUM) {
                labelVectorArray = getSummedVectors(aggregatedLabelVectors);
            }
        }
        return labelVectorArray;
    }

}
