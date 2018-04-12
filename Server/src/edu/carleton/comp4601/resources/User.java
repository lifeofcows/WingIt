package edu.carleton.comp4601.resources;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class User {
	
	private int docId;
	private String name, url, preferredGenre;
	private ArrayList<String> webpages;
	
	private HashMap<String, UserGenreSentiment> sentiments;
	private HashMap<String, BigDecimal> sentimentScores;
	
	/*
	 * Description: this class contains the information about a user, including name, preferred genre, and more
	 */
	public User(int docId, String name, String url, String preferredGenre, ArrayList<String> webpages, HashMap<String, BigDecimal> sentimentScores) {
		this.docId = docId;
		this.name = name;
		this.url = url;
		this.preferredGenre = preferredGenre;
		this.webpages = webpages;
		this.sentimentScores = sentimentScores;
		
		sentiments = new HashMap<String, UserGenreSentiment>();
	}
	
	/*
	 * Description: retrieves the docId of a user
	 * Input: none
	 * Return: the docId of the user
	 */
	public int getDocId() { 
		return docId;
	}
	
	/*
	 * Description: retrieves the name of a user
	 * Input: none
	 * Return: the name of the user
	 */
	public String getName() {
		return name;
	}
	
	/*
	 * Description: retrieves the url of a user
	 * Input: none
	 * Return: the url of the user
	 */
	public String getUrl() {
		return url;
	}
	
	/*
	 * Description: gives a new sentiment to a user for a particular movie/genre
	 * Input: the genre of the movie, the movie to be added, the sentiment towards the movie
	 * Return: none
	 */
	public void addGenreSentiment(String genre, String page, BigDecimal sentiment) {
		UserGenreSentiment genreSentiments = sentiments.get(genre);
		if (genreSentiments == null) {
			genreSentiments = new UserGenreSentiment();
		}
		genreSentiments.add(page, sentiment);
		sentiments.put(genre, genreSentiments);
	}
	
	/*
	 * Description: calculates the preferred genre of a user based off the sentiments they've expressed in their reviews
	 * Input: none
	 * Return: none
	 */
	public void calculatePreferredGenre() {
		preferredGenre = null;
		BigDecimal preferredGenreSentiment = null;
		for (String genre : sentiments.keySet()) {
			UserGenreSentiment genreSentiment = sentiments.get(genre);
			BigDecimal genreTotal = genreSentiment.calculateGenreScore();
			sentimentScores.put(genre, genreTotal);
			if (preferredGenreSentiment == null || genreTotal.compareTo(preferredGenreSentiment) == 1) {
				preferredGenreSentiment = genreTotal;
				preferredGenre = genre;
			}
		}
		if (preferredGenre != null) {
			System.out.println("Set " + name + "'s preferred genre to " + preferredGenre);
		} else {
			System.out.println(name + " has not reviewed any movies and cannot be assigned a preferred genre.");
		}
	}
	
	/*
	 * Description: retrieves the sentiments of a user for each genre
	 * Input: none
	 * Return: the sentiments of the user
	 */
	public HashMap<String, BigDecimal> getSentiments() {
		return sentimentScores;
	}
	
	/*
	 * Description: retrieves the preferred genre of a user
	 * Input: none
	 * Return: the preferred genre of the user
	 */
	public String getPreferredGenre() {
		return preferredGenre;
	}
	
	/*
	 * Description: retrieves the list of movies a user has reviewed
	 * Input: none
	 * Return: the list of movies
	 */
	public ArrayList<String> getWebPages() {
		return webpages;
	}
	
	/*
	 * Description: retrieves the sentiment of a user for a particular movie
	 * Input: the movie to get the sentiment for
	 * Return: the sentiment of the user
	 */
	public BigDecimal getSentiment(String webpage) {
		for (UserGenreSentiment genreSentiment : sentiments.values()) {
			if (genreSentiment.contains(webpage)) {
				return genreSentiment.get(webpage);
			}
		}
		return BigDecimal.valueOf(0);
	}
	
	/*
	 * Description: constructs an html table representation of the data in this class
	 * Input: none
	 * Return: the html table representation of the data
	 */
	public String htmlTableData() {
		String html = "<tr> <td> " + docId + " </td> <td> " + name + " </td> <td> <a href='" + url + "'> " + url + " </a> </td> <td> " + preferredGenre + " </td> ";
		for (String genre : GenreAnalyzer.GENRES) {
			html += "<td> " + sentimentScores.get(genre) + " </td> ";
		}
		html += "</tr>";
		return html;
	}
	
	/*
	 * Description: constructs an html table header for the data in this class
	 * Input: none
	 * Return: the html table header for the data
	 */
	public static String htmlTableHeader() {
		String html = "<tr> <th> ID </th> <th> Name </th> <th> URL </th> <th> Preferred Genre </th> ";
		for (String genre : GenreAnalyzer.GENRES) {
			html += "<th> " + genre + " score </th> ";
		}
		html += "</tr>";
		return html;
	}
	
}
