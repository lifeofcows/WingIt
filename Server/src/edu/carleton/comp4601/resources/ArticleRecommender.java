package edu.carleton.comp4601.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ArticleRecommender {
	
	private static double THRESHOLD = 0.2;
	private static int MAX_RECOMMENDATIONS = 5;
	
	/*
	 * Description: returns a selection of articles of varying political bias based off the provided article
	 * Input: the url of the article being searched
	 * Output: a string containing the list of recommendations
	 */
	public static String recommendArticles(String url) {
		HashMap<String, ArrayList<String>> wingArticleUrls = new HashMap<String, ArrayList<String>>();
		for (String wing : WingAnalyzer.WINGS) {
			wingArticleUrls.put(wing, new ArrayList<String>());
		}
		
		System.out.println(Crawler.getPageTitle(url));
		HashSet<String> queriedWords = new HashSet<String>(Arrays.asList(Crawler.getPageTitle(url).split(" ")));
		ArrayList<WebPage> articles = Database.getInstance().getWebPages();
		for (WebPage article : articles) {
			if (!Main.newsSites.containsKey(article.getUrl())) {
				if (wingArticleUrls.get(article.getWing()).size() < MAX_RECOMMENDATIONS) {
					HashSet<String> otherWords = new HashSet<String>(Arrays.asList(article.getTitle().split(" ")));
					int matchingWords = 0;
					for (String word : queriedWords) {
						if (otherWords.contains(word)) {
							matchingWords++;
						}
					}
					double matchingWordRatio = ((double) matchingWords)/queriedWords.size();
					if (matchingWordRatio >= THRESHOLD) {
						wingArticleUrls.get(article.getWing()).add("<a href=\'" + article.getUrl() + "\'>" + article.getUrl() + "</a>");
					}
				}
			}
		}
		
		String articleRecommendationsByWing = "{";
		for (String wing : wingArticleUrls.keySet()) {
			articleRecommendationsByWing += Main.JSONify(wing, wingArticleUrls.get(wing), false) + ", ";
			System.out.println(articleRecommendationsByWing);
		}
		
		if (articleRecommendationsByWing.length() > 1) {
			articleRecommendationsByWing = articleRecommendationsByWing.substring(0, articleRecommendationsByWing.length() - 2);
		}

		articleRecommendationsByWing += "}";
		return articleRecommendationsByWing;
	}
	
}
