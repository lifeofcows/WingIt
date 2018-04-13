package edu.carleton.comp4601.resources;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

public class WingAnalyzer extends NaiveBayes {
	
	public static final ArrayList<String> WINGS = new ArrayList<String>(Arrays.asList("left", "neutral", "right"));
	private static final int NUM_THREADS = 3;
	
	private ArrayDeque<WebPage> webpages;

	/*
	 * Description: this class implements the Naive Bayes algorithm with the purpose of calculating movie genres
	 */
	public WingAnalyzer() {
		super(WINGS);
	}
	
	/*
	 * Description: analyzes the contents of all of the movie webpages to determine their genres
	 * Input: none
	 * Return: none
	 */
	@Override
	public void analyze() {
		webpages = new ArrayDeque<WebPage>(Database.getInstance().getWebPages());
		
		ArrayList<ArrayList<String>> classTexts = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < WINGS.size(); i++) {
			classTexts.add(new ArrayList<String>());
		}
		
		WebPage webpage;
		while ((webpage = getNext()) != null) {
			classTexts.get(WINGS.indexOf(webpage.getWing())).add(webpage.getContent());
		}
		train(classTexts);
		Database.getInstance().insert(classConditionalProbabilities, classPriors);
		
//		for (int i = 0; i < NUM_THREADS; i++) {
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//			
//				}
//			}).start();
//		}
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
