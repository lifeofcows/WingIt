package edu.carleton.comp4601.resources;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

public class UserGenreSentiment {
	
	private HashMap<String, BigDecimal> sentiments;

	/*
	 * Description: this class provides an easy interface for storing sentiments for various movies in a shared genre for a particular user
	 */
	public UserGenreSentiment() {
		sentiments = new HashMap<String, BigDecimal>();
	}
	
	/*
	 * Description: adds a sentiment to the collection of movie sentiments
	 * Input: the movie being scored, the sentiment of the movie's review
	 * Return: none
	 */
	public void add(String webpage, BigDecimal sentiment) {
		sentiments.put(webpage, sentiment);
	}
	
	/*
	 * Description: determines whether a sentiment for the given movie exists
	 * Input: the movie to search for
	 * Return: a boolean indicating whether or not the movie has a sentiment stored
	 */
	public boolean contains(String webpage) {
		return sentiments.containsKey(webpage);
	}
	
	/*
	 * Description: retrieves the sentiment for a particular movie
	 * Input: the movie to search for
	 * Return: the sentiment of the movie
	 */
	public BigDecimal get(String webpage) {
		BigDecimal sentiment = sentiments.get(webpage);
		if (sentiment != null) {
			return sentiment;
		}
		return null;
	}
	
	/*
	 * Description: calculates the user's overall score for this particular genre
	 * Input: none
	 * Return: the score for this genre
	 */
	public BigDecimal calculateGenreScore() {
		BigDecimal total = BigDecimal.valueOf(0);
		for (BigDecimal sentiment : sentiments.values()) {
			total = total.add(sentiment);
		}
		total = total.divide(BigDecimal.valueOf(sentiments.size()), MathContext.DECIMAL128);
		return total;
	}
	
}
