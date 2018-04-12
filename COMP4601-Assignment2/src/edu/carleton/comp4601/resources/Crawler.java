package edu.carleton.comp4601.resources;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.xml.sax.ContentHandler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp4|zip|gz))$");
	ArrayList<String> saxParserMimeTypes;

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
		return !FILTERS.matcher(href).matches() && ((href.startsWith("https://sikaman.dyndns.org/courses/4601/assignments")));
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
		String title = "";
		String content = "";

		try {
			InputStream input = TikaInputStream.get(new URL(page.getWebURL().getURL()));
			ContentHandler contentHandler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			ParseContext parseContext = new ParseContext();
			Parser parser = new AutoDetectParser();
			parser.parse(input, contentHandler, metadata, parseContext);
			title = metadata.get(Metadata.TITLE);
			content = Jsoup.parse(contentHandler.toString()).text().trim().replaceAll(" +", " ");
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (page.getParseData() instanceof HtmlParseData) {
			// HTML parsing
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			Set<WebURL> outgoingUrls = htmlParseData.getOutgoingUrls();
			ArrayList<String> links = new ArrayList<String>();
			for (WebURL webUrl : outgoingUrls) {
				//links.add(webUrl.getAnchor());
			}

			int docId = page.getWebURL().getDocid();
			if (title != "") {
				System.out.println("Adding webpage");
				//html = modifyHTMLLinks(html);
				String genre = null;
				WebPage webPage = new WebPage(docId, title, url, content, html); 
				Database.getInstance().insert(webPage);
			}
			
			// Output for debugging purposes
			System.out.println("Text length: " + text.length());
			System.out.println("Html length: " + html.length());
			System.out.println("Number of outgoing links: " + outgoingUrls.size());
		}
	}
}