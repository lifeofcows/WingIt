package edu.carleton.comp4601.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

public abstract class NaiveBayes {
	
	protected static String stopWordPath = "/Users/maximkuzmenko/Desktop/School/Third Year/First Semester/COMP 4601/WingIt/Server/training/";
	//protected static String stopWordPath = "/Users/AveryVine/Documents/School/Third Year/COMP4601/eclipse-workspace/COMP4601Assignment2/COMP4601-Assignment2/training/";
	protected ArrayList<String> classes;
	protected ArrayList<Double> classPriors;
	protected ArrayList<ArrayList<String>> classTexts;
	protected ArrayList<Integer> classValues;
	protected LinkedHashMap<String, Integer> topWords;
	protected ArrayList<LinkedHashMap<String, Integer>> classWordMaps;
	protected ArrayList<HashMap<String, Double>> classConditionalProbabilities;
	protected static HashSet<String> stopWords;
	protected int totalClassDocs, totalVocabulary;
	
	protected NaiveBayes() {
		//TODO: pull from database
	}
	
	/*
	 * Description: this class provides an interface for use of the Naive Bayes algorithm, for various purposes
	 */
	protected NaiveBayes(ArrayList<String> classes) {
		this.classes = classes;
		
		classPriors = new ArrayList<Double>();
		classTexts = new ArrayList<ArrayList<String>>();
		classValues = new ArrayList<Integer>();
		topWords = new LinkedHashMap<String, Integer>();
		classWordMaps = new ArrayList<LinkedHashMap<String, Integer>>();
		classConditionalProbabilities = new ArrayList<HashMap<String, Double>>();
		
		readStopWords();
	}
	
	public void analyzeTrainingData(ArrayList<ArrayList<String>> classTexts) {
		this.classTexts = classTexts;
		countClassDocs();
		calculateClassPriors();
		cleanClassTexts();
		determineTopWords();
		countClassValues();
		calculateConditionalWordProbabilities();
	}
	
	/*
	 * Description: a function that should be implemented by derived classes, to work with the data provided by the Naive Bayes algorithm
	 * Input: none
	 * Return: none
	 */
	public abstract void train();
	
	/*
	 * Description: processes a piece of text and returns a score based off the Naive Bayes algorithm
	 * Input: the text to process
	 * Return: the list of scores relevant to the text
	 */
	protected ArrayList<BigDecimal> processText(String text, ArrayList<String> classes) {
		this.classes = classes;
		
		text = cleanText(text);
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(text.split(" ")));
		
		ArrayList<BigDecimal> scores = calculateClassScores(words);
		return scores;
	}
	
	private void countClassDocs() {
		totalClassDocs = 0;
		for (ArrayList<String> arr : classTexts) {
			totalClassDocs += arr.size();
		}
	}
	
	public void setClassConditionalProbabilities(ArrayList<HashMap<String, Double>> classConditionalProbabilities) {
		this.classConditionalProbabilities = classConditionalProbabilities;
	}
	
	public void setClassPriors(ArrayList<Double> classPriors) {
		this.classPriors = classPriors;
	}
	
	/*
	 * Description: reads stop words in from a file
	 * Input: none
	 * Return: none
	 */
	private static void readStopWords() {
		stopWords = new HashSet<String>();
		
		File file = new File(stopWordPath + "stopwords.txt");
		System.out.println("Reading stop words from directory: " + file.getAbsolutePath());
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				stopWords.add(scanner.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			if (scanner != null) {
				scanner.close();
			}
		} 
	}

	
	/*
	 * Description: calculates the prior for each class
	 * Input: none
	 * Return: none
	 */
	private void calculateClassPriors() {
		System.out.println("Calculating class priors...");
		for (int i = 0; i < classes.size(); i++) {
			classPriors.add((double) (((float) classTexts.get(i).size()) / ((float) totalClassDocs)));
		}
		
	}
	
	/*
	 * Description: cleans all of the training data by removing stop words, etc. 
	 * Input: none
	 * Return: none
	 */
	private void cleanClassTexts() {
		System.out.println("Cleaning class texts...");
		ArrayList<ArrayList<String>> cleanedClassTexts = new ArrayList<ArrayList<String>>();
		for (ArrayList<String> classText : classTexts) {
			ArrayList<String> cleanedClassText = new ArrayList<String>();
			for (String text: classText) {
				cleanedClassText.add(cleanText(text));
			}
			cleanedClassTexts.add(cleanedClassText);
		}
		classTexts = cleanedClassTexts;
	}
	
	/*
	 * Description: cleans the provided text by removing stop words, etc.
	 * Input: the text to clean
	 * Return: the cleaned text
	 */
	public static String cleanText(String text) {	
		readStopWords();
		
		text = text.toLowerCase().replaceAll("[^A-Za-z0-9 ]", " ").trim().replaceAll(" +", " ");
		
		List<String> textList = Arrays.asList(text.split(" "));
		List<String> removedTextList = new ArrayList<String>();
		for (int i = 0; i < textList.size(); i++) {
			String currTextWord = textList.get(i);
			if (!stopWords.contains(currTextWord) && !currTextWord.matches("^.*[^a-zA-Z0-9 ].*$") && !currTextWord.equals("")) {
				removedTextList.add(textList.get(i));
			}
		}
		return String.join(" ", removedTextList);
	}
	
	/*
	 * Description: determines the most common words across all classes
	 * Input: none
	 * Return: none
	 */
	private void determineTopWords() {
		System.out.println("Determining top words...");
		ArrayList<LinkedHashMap<String, Integer>> tempClassWordMaps = new ArrayList<LinkedHashMap<String, Integer>>();
		for (int i = 0; i < classes.size(); i++) {
			ArrayList<String> classText = classTexts.get(i);
			
			ArrayList<String> wordsToProcess = new ArrayList<String>();
			for (String text : classText) {
				wordsToProcess.addAll(Arrays.asList(text.split(" ")));
			}
			
			HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
			
			for (String word : wordsToProcess) {
				int count = 0;
				if (wordMap.containsKey(word)) {
					count = wordMap.get(word);
				}
				wordMap.put(word,  count + 1);
			}
			
			LinkedHashMap<String, Integer> sortedWordMap = new LinkedHashMap<String, Integer>(sortByValue(wordMap));
			
			tempClassWordMaps.add(sortedWordMap);
		}
		
		classWordMaps = onlyKeepMutualWords(tempClassWordMaps);
		if (classWordMaps.size() > 0) {
			totalVocabulary = classWordMaps.get(0).size();
		}
	}
	
	/*
	 * Description: takes in a list of words across multiple classes, and keeps only those words that appear in all classes
	 * Input: the list of words in all of the classes
	 * Return: the mutual words between those classes
	 */
	public ArrayList<LinkedHashMap<String, Integer>> onlyKeepMutualWords(ArrayList<LinkedHashMap<String, Integer>> mapList) {
		HashSet<String> mutualWords = new HashSet<String>();
		if (mapList.size() == 0) {
			return mapList;
		}
		for (String key : mapList.get(0).keySet()) {
			boolean canAdd = true;
			for (int i = 1; i < mapList.size(); i++) {
				LinkedHashMap<String, Integer> map = mapList.get(i);
				if (!map.containsKey(key)) {
					canAdd = false;
					break;
				}
			}
			if (canAdd) {
				mutualWords.add(key);
			}
		}
		
		ArrayList<LinkedHashMap<String, Integer>> tempMapList = new ArrayList<LinkedHashMap<String, Integer>>();
		
		for (int i = 0; i < mapList.size(); i++) {
			LinkedHashMap<String, Integer> tempMap = new LinkedHashMap<String, Integer>(mapList.get(i));
			for (String word : mapList.get(i).keySet()) {
				if (!mutualWords.contains(word)) {
					tempMap.remove(word);
				}
			}
			tempMapList.add(tempMap);
		}
		
		return tempMapList;
	}
	
	/*
	 * Description: sorts the contents of a word map by value (number of times it appears)
	 * Input: the word map to sort
	 * Return: the sorted word map
	 */
	private static <K, V extends Comparable<? super Integer>> LinkedHashMap<String, Integer> sortByValue(Map<String, Integer> map) {
        List<Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((o2, o1) -> o1.getValue().compareTo(o2.getValue()));
        
        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
        		result.put(entry.getKey(), entry.getValue());
        }
        
        return result;
    }
	
	/*
	 * Description: counts the total number of words in a class
	 * Input: none
	 * Return: none
	 */
	private void countClassValues() {
		System.out.println("Counting class values...");
		for (int i = 0; i < classes.size(); i++) {
			int count = 0;
			for (Integer value : classWordMaps.get(i).values()) {
				count += value;
			}
			classValues.add(count);
		}
	}
	
	/*
	 * Description: counts the number of times a specific word appears in a class
	 * Input: the word to count, the map of words in the class
	 * Return: the number of times the word appears in the class
	 */
	private int getCountOfWordInClass(String word, LinkedHashMap<String, Integer> classWords) {
		return classWords.containsKey(word) ? classWords.get(word) : 0; 
	}
	
	/*
	 * Description: calculates the chance of any given word appearing in any given class
	 * Input: none
	 * Return: none
	 */
	private void calculateConditionalWordProbabilities() {
		System.out.println("Calculating conditional word probabilities...");
		for (int i = 0; i < classes.size(); i++) {
			HashMap<String, Double> classProbabilities = new HashMap<String, Double>();
			LinkedHashMap<String, Integer> classWords = classWordMaps.get(i);
			double denominator = classValues.get(i) + totalVocabulary; 
			for (String word: classWords.keySet()) {
				double numerator = getCountOfWordInClass(word, classWords) + 1;
				double result = numerator / denominator;
				classProbabilities.put(word, result);
			}
			classConditionalProbabilities.add(classProbabilities);
		}
	}
	
	/*
	 * Description: calculates the score of a particular list of words according to the trained Naive Bayes algorithm
	 * Input: the list of words to score
	 * Return: the score of the words
	 */
	protected ArrayList<BigDecimal> calculateClassScores(ArrayList<String> words) {
		ArrayList<BigDecimal> classScores = new ArrayList<BigDecimal>();
		for (int i = 0; i < classes.size(); i++) {
			HashMap<String, Double> conditionalProbabilities = classConditionalProbabilities.get(i);
			Double classPrior = classPriors.get(i);
			BigDecimal probability = BigDecimal.valueOf(1);
			
			for (String word : words) {
				if (conditionalProbabilities.containsKey(word) && conditionalProbabilities.get(word) > 0) {
					probability = probability.multiply(BigDecimal.valueOf(conditionalProbabilities.get(word)));
				}
			}
			probability = probability.multiply(BigDecimal.valueOf(classPrior));
			classScores.add(probability);
		}
		return classScores;
	}
	
	/*
	 * Description: retrieves the classes in this Naive Bayes instance
	 * Input: none
	 * Return: the list of classes
	 */
	public ArrayList<String> getClasses() {
		return classes;
	}

}
