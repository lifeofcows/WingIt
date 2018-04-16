package edu.carleton.comp4601.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleRecommender {
	
	private static double threshold = 0.3; 
	
	public ArticleRecommender() {
		
	}
	
	//find articles with relevant title 
	public void recommendArticles(String url) {
		WebPage queriedArticle = Database.getInstance().getWebPageByURL(url);
		
		ArrayList<WebPage> articles = Database.getInstance().getWebPages();
		for (WebPage article : articles) {
			ArrayList<String> titleWords = new ArrayList<String>(Arrays.asList(article.getTitle().split(" ")));
			int matchingWords = 0;
//			for () {
//				
//			}
		}
	}
	
}
