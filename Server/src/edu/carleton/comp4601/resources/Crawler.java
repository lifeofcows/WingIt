package edu.carleton.comp4601.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.ContentHandler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

	long beginTime, diffTime;
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp4|zip|gz))$");
	ArrayList<String> saxParserMimeTypes;
	private static int docId = 0;

	/**
	 * This method receives two parameters. The first parameter is the page in which
	 * we have discovered this new url and the second parameter is the new url. You
	 * should implement this function to specify whether the given url should be
	 * crawled or not (based on your crawling logic). In this example, we are
	 * instructing the crawler to ignore urls that have css, js, git, ... extensions
	 * and to only accept urls that start with "http://www.ics.uci.edu/". In this
	 * case, we didn't need the referringPage parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		
		boolean urlMatches = false;
		for (String site : Main.newsSites.keySet()) {
			if (href.startsWith(site)) {
				urlMatches = true; 
				break;
			}
		}
		return !FILTERS.matcher(href).matches() && urlMatches;
	}

	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		beginTime = System.currentTimeMillis();
	}
	
	/*
	 * Description: visits a webpage, crawls the data, then stores it in the database
	 * Input: the page to visit
	 * Return: none
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		System.out.println("URL: " + url);
		String title = getPageTitle(url);
		String content = "";
		try {
			Document doc = Jsoup.connect(url).get();
			content = Jsoup.parse(doc.html()).text();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (page.getParseData() instanceof HtmlParseData) {
			// HTML parsing
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			
			if (title != "") {
				String wing = "centrist";
				for (String site : Main.newsSites.keySet()) {
					if (url.contains(site)) {
						wing = Main.newsSites.get(site);
						break;
					}
				}
				WebPage webPage = new WebPage(getAndIncrementDocId(), title, url, content, wing);
				System.out.println("Added webpage with docId: " + webPage.getDocId());
				Database.getInstance().insert(webPage);
			}
			
			// Output for debugging purposes
			System.out.println("Text length: " + text.length());
			System.out.println("Html length: " + html.length());
			//System.out.println("Number of outgoing links: " + outgoingUrls.size());
			
		}
		
		diffTime = System.currentTimeMillis() - beginTime;
//		this.getMyController().getConfig().setPolitenessDelay((int) (diffTime * 30));
	}
		
	/*
	 * Description: gets the title of a webpage using the TikaParser for later use
	 * Input: the url of the website/article for which to get a title
	 * Output: the title of the webpage
	 */
	public static String getPageTitle(String url) {
		String title = "";
		try {
			InputStream input = TikaInputStream.get(new URL(url));
			ContentHandler contentHandler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			ParseContext parseContext = new ParseContext();
			Parser parser = new AutoDetectParser();
			parser.parse(input, contentHandler, metadata, parseContext);
			
			input.close();
			
			title = NaiveBayes.cleanText(metadata.get(Metadata.TITLE)); //remove stop words from title
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return title;
	}
	
	/*
	 * Description: ensures a synchronized docId is being used across all crawlers
	 * Input: none
	 * Return: the docId
	 */
	public static synchronized int getAndIncrementDocId() {
		return ++docId;
	}
	
	/*
	 * Description: ensures a synchronized docId is being used across all crawlers
	 * Input: none
	 * Return: none
	 */
	public static void resetDocId() {
		docId = 0;
	}
}