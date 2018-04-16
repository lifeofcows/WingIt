package edu.carleton.comp4601.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ArticleRecommender {
	
	private static double threshold = 0.15; 
	
	public ArticleRecommender() {
		
	}
	
	//find articles with relevant title 
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
				HashSet<String> otherWords = new HashSet<String>(Arrays.asList(article.getTitle().split(" ")));
				int matchingWords = 0;
				for (String word : otherWords) {
					if (queriedWords.contains(word)) {
						matchingWords++;
					}
				}
				double matchingWordRatio = ((double) matchingWords)/otherWords.size();
				if (matchingWordRatio >= threshold) {
					wingArticleUrls.get(article.getWing()).add("<a href=\'" + article.getUrl() + "\'>" + article.getUrl() + "</a>");
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
