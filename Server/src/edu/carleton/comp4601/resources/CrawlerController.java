package edu.carleton.comp4601.resources;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import javax.net.ssl.X509TrustManager;

public class CrawlerController {
	
	CrawlController controller;
	int numberOfCrawlers = 7;
	String crawlStorageFolder = "data/crawl/root";
	public static String crawlBaseURL = "";
	int maxDepthOfCrawling = 10;
	int numberOfPagesToCrawl = 40;

	/*
	 * Description: this class prepares and controls the web crawler
	 */
	public CrawlerController(String seed) throws Exception {
		System.setProperty("http.agent", "Chrome");
//		disableCertificates();
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxDepthOfCrawling(maxDepthOfCrawling);
		config.setIncludeBinaryContentInCrawling(true);
		config.setMaxPagesToFetch(60);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		controller = new CrawlController(config, pageFetcher, robotstxtServer);

		controller.addSeed(seed);
		
		Database.getInstance();
	}
	
	/*
	 * Description: wipes the database and starts the web crawler
	 * Input: none
	 * Return: none
	 */
	public void crawl() {
		controller.start(Crawler.class, numberOfCrawlers);
	}
	
	/*
	 * Description: Max's crawler wasn't working, so this disables certificate limitations
	 * Input: none
	 * Return: none
	 */
	public void disableCertificates() {
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					@Override
					public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

					}
					@Override
					public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

					}
				}
		};

		// Install the all-trusting trust manager
		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try {
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
}