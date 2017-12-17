package hmm;

import hmm.Cell;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class Predict {

    private final Map<String, Map<String, Double>> transitionMatrix;
    private final Map<String, Map<String, Double>> emissionMatrix;
    private final Set<String> vocabulary;
    private final String MOST_FREQUENT_TAG = "NN";
    private final String[] TAG_LIST = {"DT","NN","VB","VBD","IN","PRP"};
    Map<String, Map<String, Cell>> V = new HashMap<String, Map<String, Cell>>();

    public Predict() {
        transitionMatrix = new HashMap<>();
        emissionMatrix = new HashMap<>();
        vocabulary = new HashSet<>();
    }    

    // Implements the viterbi algorithm for tagging parts of speech in a sentence, uses helper method maxProb()
    public String viterbi(List<String> words) {       
        String word = "";
        double probability = 0.0;
        Map<String, Cell> column = new HashMap<String, Cell>(); // For temporarily storing columns of cells before adding them to V
        Cell cell = null; // For temporarily storing cells before adding them to a column
        int T = words.size();
        
        // init step
        word = words.get(0);
        if(vocabulary.contains(word)){
        	for(String tag : transitionMatrix.keySet()){
        		probability = retrieveTransitionProb("<s>",tag) * retrieveEmissionProb(tag,word);
        		cell = new Cell(word,tag,null,probability);
        		column.put(tag, cell);
        	}
        	V.put(word, column);
        }
        // word is not recognized, set this word to correspond to MOST_FREQUENT_TAG
        else{
        	probability = retrieveTransitionProb("<s>",MOST_FREQUENT_TAG) * retrieveEmissionProb(MOST_FREQUENT_TAG,word);
        	cell = new Cell(word, MOST_FREQUENT_TAG, null, probability);
        	column = new HashMap<String, Cell>();
        	column.put(MOST_FREQUENT_TAG, cell);
        	V.put(word, column);
        }
        
        // recursion step
        for(int i = 1; i < T; i++){
        	word = words.get(i);
        	column = new HashMap<String,Cell>();
        	if(vocabulary.contains(word)) {
        		for(String tag : transitionMatrix.keySet()) {
        			cell = maxProb(words.get(i-1), word, tag);
        			column.put(tag,  cell);
        		}
        	}
        	else {
        		cell = maxProb(words.get(i-1), word, MOST_FREQUENT_TAG);
        		column.put(MOST_FREQUENT_TAG,  cell);
        	}
        	V.put(word,  column);
        }
        
        // termination step
        String taggedSentence = backtrace((HashMap<String, Cell>) V.get(words.get(T-1)));
        return taggedSentence;
    }   
    
    /*
     * Calculates the max of product v[s', t-1] * a[s', s] * b[s, w_t], for every state s'
     * Returns a cell with max probability over iterations and a backpointer to the previous cell used in calculating that max
     */
    private Cell maxProb(String prevWord, String currentWord, String currentTag) {
    	Cell returnCell = new Cell(currentWord,currentTag);
    	Map<String, Cell> previousColumn = V.get(prevWord);
    	double previousProb = 0.0;
    	double transitionProb = 0.0; 
    	double emissionProb = retrieveEmissionProb(currentTag, currentWord);
    	double currentProb = 0.0; // stores probability of current iteration
    	double maxProb = 0.0; // max probability over iterations
    	Cell bp = null; // backpointer referencing previous node used in calculating maxProb
    	
    	for(String tag : transitionMatrix.keySet()) {
    		previousProb = previousColumn.get(tag).prob;
    		transitionProb = retrieveTransitionProb(tag,currentTag);
    		currentProb = previousProb * transitionProb * emissionProb;
    		if(maxProb < currentProb) {
    			maxProb = currentProb;
    			bp = previousColumn.get(tag);
    		}
    	}
    	
    	returnCell.prob = maxProb;
    	returnCell.backpointer = bp;
    	return returnCell;
    }
    	
    

    /*
     * Retrieves transition probability P(tag2|tag1)
     */
    private double retrieveTransitionProb(String tag1, String tag2) {
        return transitionMatrix.get(tag1).get(tag2);
    }

    /*
     * Retrieves emission probability P(word|tag)
     */
    private double retrieveEmissionProb(String tag, String word) {
        return emissionMatrix.get(tag).get(word);
    }

    /*
     * prints out the linked list of the correctly tagged words
     */
    private String backtrace(HashMap<String, Cell> map) {
        StringBuilder str = new StringBuilder();
        Cell c = new Cell("NOMAX", "NOMAX");
        for (String key : map.keySet()) {
            Cell currentCell = map.get(key);
            if (currentCell.prob >= c.prob) {
                c = currentCell;
            }
        }

        Stack<Cell> stack = new Stack<Cell>();
        while (c != null) {
            stack.push(c);
            c = c.backpointer;
        }

        while (!stack.isEmpty()) {
            c = stack.pop();
            str.append(String.format("%s %s ", c.word, c.tag));
        }
        return str.toString().trim();
    }

    public void loadModel(String transitionFilename, String emissionFilename, String vocabularyFilename) {
        Scanner transitionScanner;
        Scanner emissionScanner;
        Scanner vocabScanner;
        File transitionFile = new File(transitionFilename);
        File emissionFile = new File(emissionFilename);
        File vocabFile = new File(vocabularyFilename);
        
        try {
            transitionScanner = new Scanner(transitionFile);
            emissionScanner = new Scanner(emissionFile);
            vocabScanner = new Scanner(vocabFile);

            while (transitionScanner.hasNext()) {
                String tag1 = transitionScanner.next();
                String tag2 = transitionScanner.next();
                Double prob = Double.valueOf(transitionScanner.next());
                addToMap(transitionMatrix, tag1, tag2, prob);
            }
            while (emissionScanner.hasNext()) {
                String tag = emissionScanner.next();
                String word = emissionScanner.next();
                Double prob = Double.valueOf(emissionScanner.next());
                addToMap(emissionMatrix, tag, word, prob);
            }
            while (vocabScanner.hasNext()) {
                String word = vocabScanner.next();
                vocabulary.add(word);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        }        
    }
	
    private void addToMap(Map<String, Map<String, Double>> map, String key1, String key2, Double value) {
        if (map.containsKey(key1)) {
            map.get(key1).put(key2, value);
        } else {
            Map<String, Double> subMap = new HashMap<>();
            subMap.put(key2, value);
            map.put(key1, subMap);
        }
    }

    public String question1() {
        String sentence = "<s> The quick brown fox jumps over the lazy river .";
        List<String> words = Arrays.asList(sentence.split(" "));
        return viterbi(words);
    }

    public String question2() {
        String sentence = "<s> Rockwell International Corp. 's Tulsa unit said it signed a tentative agreement extending its contract with Boeing Co. to provide structural parts for Boeing 's 747 jetliners .";
        List<String> words = Arrays.asList(sentence.split(" "));
        return viterbi(words);
    }

    public String question3() {
        String sentence = "<s> I saw the man with the telescope .";
        List<String> words = Arrays.asList(sentence.split(" "));
        return viterbi(words);
    }

    public String question4() {
        String sentence = "<s> In the absence of humans , would the Earth enjoy a constant climate over the long term ?";
        List<String> words = Arrays.asList(sentence.split(" "));
        return viterbi(words);
    }

    public static void main(String[] args) {
        Predict p = new Predict();
        p.loadModel("data/a.txt", "data/b.txt", "data/vocabulary.txt");
        System.out.println(p.question1());
        System.out.println(p.question2());
        System.out.println(p.question3());
        System.out.println(p.question4());
    }
}
