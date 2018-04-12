package edu.carleton.comp4601.resources;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class SentimentAnalyzer extends NaiveBayes {
	
	public static final ArrayList<String> SENTIMENTS = new ArrayList<String>(Arrays.asList("positive", "negative"));
	private static final int NUM_THREADS = 3;
	
	private HashMap<String, User> users;
	private ArrayDeque<WebPage> webpages;
	private Thread[] threads;

	/*
	 * Description: this class implements the Naive Bayes algorithm with the purpose of calculating the sentiment of text
	 */
	public SentimentAnalyzer() {
		super(SENTIMENTS);
		threads = new Thread[NUM_THREADS];
	}
	
	/*
	 * Description: analyzes the movie reviews for every user in the database to determine sentiment
	 * Input: none
	 * Return: none
	 */
	@Override
	public void analyze() {
		System.out.println("Analyzing user sentiments...");
		ArrayList<User> dbUsers = Database.getInstance().getUsers(false);
		users = new HashMap<String, User>();
		for (User user : dbUsers) {
			users.put(user.getName(), user);
		}
		
		webpages = new ArrayDeque<WebPage>(Database.getInstance().getWebPages());
		
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					WebPage webpage;
					while ((webpage = getNext()) != null) {
						HashMap<String, String> reviews = getReviewsFromPage(webpage);
						
						for (Entry<String, String> entry : reviews.entrySet()) {
							ArrayList<BigDecimal> scores = processText(entry.getValue());
							
							BigDecimal positiveScore = scores.get(0);
							BigDecimal negativeScore = scores.get(1);
							BigDecimal finalScore;
							if (positiveScore.compareTo(negativeScore) == 1) {
								finalScore = positiveScore.divide(negativeScore, MathContext.DECIMAL128);
							}
							else {
								finalScore = negativeScore.divide(positiveScore, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(-1));
							}
							User user = users.get(entry.getKey());
							user.addGenreSentiment(webpage.getGenre(), webpage.getName(), finalScore);
						}
					}
				}
				
			});
			threads[i].start();
		}
		for (int i = 0; i < NUM_THREADS; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.out.println("Unable to join threads during sentiment analysis");
				e.printStackTrace();
			}
		}
		System.out.println("Updating user preferences...");
		for (User user : users.values()) {
			user.calculatePreferredGenre();
			Database.getInstance().insert(user);
		}
	}
	
	/*
	 * Description: retrieves the list of reviews from a given webpage, linked to their respective users
	 * Input: the webpage to analyze
	 * Return: the list of reviews from the webpage
	 */
	public HashMap<String, String> getReviewsFromPage(WebPage webPage) {
		HashMap<String, String> reviews = new HashMap<String, String>();
		HashSet<String> usernames = webPage.getUsers();
		ArrayList<String> wordsInPage = new ArrayList<String>(Arrays.asList(webPage.getContent().split(" ")));
		String currentReviewer = null;
		String currentReview = null;
		for (String word : wordsInPage) {
			if (usernames.contains(word)) {
				if (currentReviewer != null) {
					reviews.put(currentReviewer, currentReview);
				}
				currentReviewer = word;
				currentReview = "";
			} else {
				currentReview += word + " ";
			}
		}
		if (currentReviewer != null) {
			reviews.put(currentReviewer, currentReview);
		}
		return reviews;
	}
	
	public ArrayList<User> getAnalyzedUsers() {
		return new ArrayList<User>(users.values());
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
