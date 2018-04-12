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
		
		for (int i = 0; i < NUM_THREADS; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					WebPage webpage;
					while ((webpage = getNext()) != null) {
						ArrayList<String> output = new ArrayList<String>();
						ArrayList<BigDecimal> scores = processText(webpage.getContent());
						int indexOfBestScore = -1;
						for (int i = 0; i < scores.size(); i++) {
							if (indexOfBestScore == -1 || scores.get(i).compareTo(scores.get(indexOfBestScore)) == 1) {
								indexOfBestScore = i;
							}
							output.add(getClasses().get(i) + " score for page " + webpage.getName() + " is " + scores.get(i).toEngineeringString());
						}
						webpage.setGenre(getClasses().get(indexOfBestScore));
						System.out.println("Set genre of " + webpage.getName() + ": " + webpage.getGenre());
						Database.getInstance().insert(webpage);
					}
				}
				
			}).start();
		}
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
