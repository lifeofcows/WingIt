package edu.carleton.comp4601.resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WingAnalyzer extends NaiveBayes {
	
	public static final ArrayList<String> WINGS = new ArrayList<String>(Arrays.asList("left", "neutral", "right"));
	
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
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		try {
			
			Document doc = Jsoup.connect(url).get();
			String urlText = Jsoup.parse(doc.html()).text();
			
			System.out.println("Retrieved website html:");
			System.out.println(urlText);
			
			processText(urlText, WINGS);
			
			//TODO: calculate wing based off score
			String wing = "neutral";
			
			keys.add("statusCode");
			values.add("200");
			keys.add("wing");
			values.add(wing);
			
		} catch (Exception e) {
			e.printStackTrace();
			keys.add("statusCode");
			values.add("500");
		}
		
		return Recommender.JSONify(keys, values);
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
