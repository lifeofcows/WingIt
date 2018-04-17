package edu.carleton.comp4601.resources;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WingAnalyzer extends NaiveBayes {
	
	public static final String left = "left";
	public static final String centrist = "centrist";
	public static final String right = "right";
	public static final ArrayList<String> WINGS = new ArrayList<String>(Arrays.asList(left, centrist, right));
	
	private ArrayDeque<WebPage> webpages;
	
	public WingAnalyzer() {
		super();
	}


	public WingAnalyzer(ArrayList<String> WINGS) {
		super(WINGS);
	}
	
	/*
	 * Description: trains the system using the crawled webpages from the database; inserts a response code of 200 to Main.res if successful, 500 if unsuccessful.
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
			Main.res = Main.JSONify("statusCode", "200", true);
		} catch (Exception e) {
			Main.res = Main.JSONify("statusCode", "500", true);
		}
		
	}
	
	
	/*
	 * Description: computes a logarithm with a custom base
	 * Input: base and number to be taken the logarithm with
	 * Return: a double representing the output of the logarithm
	 */
	public double logOfBase(int base, int num) {
	    return Math.log(num) / Math.log(base);
	}
	
	
	/*
	 * Description: function gets the number of decimal places in a big decimal
	 * Input: a bigDecimal number
	 * Return: an integer representing the number after the "-" if exists; otherwise return 1.
	 */
	int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
	    String str = bigDecimal.toEngineeringString();
	    int index = str.indexOf("-");
	    System.out.println(str);
	    
	    if (index < 0) {
	    		return 1;
	    }
	    
	    return Integer.parseInt(str.substring(index + 1, str.length()));
	}
	
	
	/*
	 * Description: analyzes a webpage article and returns response codes and JSON string containing various political analytics data about the article
	 * Input: article url as string
	 * Return: JSON string containing various political anlaytics data about the article
	 */
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
			
			BigDecimal scalingFactor = BigDecimal.valueOf(0);
			int maxIndex = 0;
			for (int i = 0; i < wingSentiments.size(); i++) {
				System.out.println("Wing sentiment for " + WINGS.get(i) + " is " + wingSentiments.get(i).toEngineeringString());
				maxIndex = wingSentiments.get(i).compareTo(wingSentiments.get(maxIndex)) == 1 ? i : maxIndex;
				scalingFactor = scalingFactor.add(wingSentiments.get(i));	
			}

			System.out.println("scaling factor is " + scalingFactor.toEngineeringString());
			
			ArrayList<Double> scaledWingSentiments = new ArrayList<Double>();
			
			double total = 0;
			
			for (int i = 0; i < wingSentiments.size(); i++) {
				System.out.println("Scaling: " + wingSentiments.get(i).divide(scalingFactor, MathContext.DECIMAL32));
				Double result = logOfBase(10, getNumberOfDecimalPlaces(wingSentiments.get(i).divide(scalingFactor, RoundingMode.HALF_UP)));
				scaledWingSentiments.add(result);
				System.out.println("Adding: " + result);
				total += result;
			}
			
			for (int i = 0; i < scaledWingSentiments.size(); i++) {
				scaledWingSentiments.set(i, scaledWingSentiments.get(i)/total);
				System.out.println("Scaled: " + scaledWingSentiments.get(i));
			}
			
			double leanAmount = 0.5;
			
			double scaler = (1.0 / (scaledWingSentiments.size() - 1));
			System.out.println("Scaler is: " + scaler);
			for (int i = 0; i < scaledWingSentiments.size(); i++) {
				if (i < (scaledWingSentiments.size() - 1) / 2) {
					leanAmount += scaler * scaledWingSentiments.get(i);
				} else if (i > (scaledWingSentiments.size() - 1) / 2) {
					leanAmount -= scaler * scaledWingSentiments.get(i);
				}
			}
			
			leanAmount = leanAmount * 100;
			
			System.out.println("Lean amount is " + leanAmount);
			
			//Get wing, then add page to list of pages to crawl
			String wing = WINGS.get(maxIndex);
	
			String recommendations = ArticleRecommender.recommendArticles(url);
			System.out.println("recommendations are " + recommendations);
			//Database.getInstance().insert(new WebPage(Crawler.getAndIncrementDocId(), Crawler.getPageTitle(url), url, urlText, wing));
			
			keys.add("statusCode");
			values.add("200");
			keys.add("wing");
			values.add(wing);
			keys.add("recommendations");
			values.add(recommendations);
			keys.add("wingPercentage");
			values.add(((int) leanAmount) + "");
		} catch (Exception e) {
			e.printStackTrace();
			keys.add("statusCode");
			values.add("500");
		}
		
		return Main.JSONify(keys, values);
	}
	
	
	/*
	 * Description: returns the next webpage from the queue; use for multithreading
	 * Input: None
	 * Return: Next webpage from the queue
	 */
	private synchronized WebPage getNext() {
		System.out.println("Webpages left: " + webpages.size());
		WebPage webpage = null;
		if (webpages.size() > 0) {
			webpage = webpages.pop();
		}
		return webpage;
	}

}
