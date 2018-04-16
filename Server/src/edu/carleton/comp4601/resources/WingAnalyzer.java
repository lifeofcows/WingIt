package edu.carleton.comp4601.resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.uci.ics.crawler4j.url.WebURL;

public class WingAnalyzer extends NaiveBayes {
	
	public static final String left = "left";
	public static final String neutral = "neutral";
	public static final String right = "right";
	public static final ArrayList<String> WINGS = new ArrayList<String>(Arrays.asList(left, neutral, right));
	
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
		
		System.out.println("webpages size in train is " + webpages.size());
		
		ArrayList<ArrayList<String>> classTexts = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < WINGS.size(); i++) {
			classTexts.add(new ArrayList<String>());
		}
		
		WebPage webpage;
		while ((webpage = getNext()) != null) {
			classTexts.get(WINGS.indexOf(webpage.getWing())).add(webpage.getContent());
		}
		
		try {
			analyzeTrainingData(classTexts);
			Database.getInstance().insert(classConditionalProbabilities, classPriors);
			Recommender.res = Recommender.JSONify("statusCode", "200", true);
		} catch (Exception e) {
			Recommender.res = Recommender.JSONify("statusCode", "500", true);
		}
		
	}
	
	public String analyze(String url) {
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		try {
			setClassConditionalProbabilities(Database.getInstance().getClassConditionalProbabilities());
			setClassPriors(Database.getInstance().getClassPriors());
			
			Document doc = Jsoup.connect(url).get();
			String urlText = Jsoup.parse(doc.html()).text();
			
			System.out.println("Retrieved website html:");
			System.out.println(urlText);
			
			ArrayList<BigDecimal> wingSentiments = processText(urlText, WINGS);
			
			int maxIndex = 0;
			for (int i = 0; i < wingSentiments.size(); i++) {
				//System.out.println("Wing sentiment for " + WINGS.get(i) + " is " + wingSentiments.get(i).doubleValue());
				maxIndex = wingSentiments.get(i).compareTo(wingSentiments.get(maxIndex)) == 1 ? i : maxIndex;
			}
			
			//Get wing, then add page to list of pages to crawl
			String wing = WINGS.get(maxIndex);
	
			//Database.getInstance().insert(new WebPage(Crawler.getAndIncrementDocId(), Crawler.getPageTitle(url), url, urlText, wing));
			
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
