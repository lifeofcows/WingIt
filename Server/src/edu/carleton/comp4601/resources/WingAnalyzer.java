package edu.carleton.comp4601.resources;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

public class WingAnalyzer extends NaiveBayes {
	
	public static final ArrayList<String> WINGS = new ArrayList<String>(Arrays.asList("left", "neutral", "right"));
	private static final int NUM_THREADS = 3;
	
	private ArrayDeque<WebPage> webpages;
	
	public WingAnalyzer() {
		super();
	}

	/*
	 * Description: this class implements the Naive Bayes algorithm with the purpose of calculating movie genres
	 */
	public WingAnalyzer(ArrayList<String> WINGS) {
		super(WINGS);
	}
	
	/*
	 * Description: analyzes the contents of all of the movie webpages to determine their genres
	 * Input: none
	 * Return: none
	 */
	@Override
	public void train() {
		webpages = new ArrayDeque<WebPage>(Database.getInstance().getWebPages());
		
		ArrayList<ArrayList<String>> classTexts = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < WINGS.size(); i++) {
			classTexts.add(new ArrayList<String>());
		}
		
		WebPage webpage;
		while ((webpage = getNext()) != null) {
			classTexts.get(WINGS.indexOf(webpage.getWing())).add(webpage.getContent());
		}
		
		analyzeTrainingData(classTexts);
		Database.getInstance().insert(classConditionalProbabilities, classPriors);
	}
	
	public String analyze(String url) {
		//TODO: get text from url
		String urlText = "this is a test";
		
		//TODO: store scores retrieved from processText
		processText(urlText);
		
		//TODO: calculate wing based off score
		String wing = "neutral";
		
		String res = "{";
		res += json("url", url);
		res += json("wing", wing);
		if (res.length() > 1) {
			res = res.substring(0, res.length() - 2);
		}
		res += "}";
		return res;
	}
	
	private String json(String key, String value) {
		return "\"" + key + "\": \"" + value + "\", ";
	}
	
	private synchronized WebPage getNext() {
		System.out.println("Webpages left: " + webpages.size());
		WebPage webpage = null;
		if (webpages.size() > 0) {
			webpage = webpages.pop();
		}
		return webpage;
	}

}
