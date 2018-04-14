package edu.carleton.comp4601.resources;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class Recommender {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	String name, authorName1, authorName2;
	CrawlerController controller;
	WingAnalyzer wingAnalyzer;
	public static HashMap<String, String> newsSites;
	private static ArrayDeque<String> siteDeque;
	private String res;
	private Thread[] threads;
	static {
		newsSites = new HashMap<String, String>();
		newsSites.put("https://www.vox.com/", WingAnalyzer.left);
		newsSites.put("https://www.buzzfeed.com/", WingAnalyzer.left);
		newsSites.put("https://www.economist.com/", WingAnalyzer.neutral);
		newsSites.put("https://www.reuters.com/", WingAnalyzer.neutral);
		newsSites.put("https://www.infowars.com/", WingAnalyzer.right);
		newsSites.put("www.drudgereport.com/", WingAnalyzer.right);
		
	}
	private static final int NUM_THREADS = 3;
	
	public Recommender() {
		authorName1 = "Avery Vine";
		authorName2 = "Maxim Kuzmenko";
		name = "WingIt";
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getName() {
		System.out.println("name");
		String res = "{\"name\": \"" + name + "\"}";
		return res;
	}

	@GET
	@Path("admin")
	public String admin(@QueryParam("adminRequest") String adminRequest) {
		System.out.println("admin -> " + adminRequest);
		res = "";
		switch (adminRequest) {
			case "reset":
				crawl();
				if (res.contains("200")) {
					train();	
				}
				break;
			case "analysis":
				analysis();
				break;
			default:
				res = JSONify("statusCode", "500");
		}
		return res;
	}

	@GET
	@Path("url")
	@Produces(MediaType.APPLICATION_JSON)
	public String url(@QueryParam("url") String url) {
		System.out.println("url -> " + url);
		wingAnalyzer = new WingAnalyzer();
		res = wingAnalyzer.analyze(url);
		return res;
	}
	
	private void crawl() {
		Database.getInstance().clear();
		siteDeque = new ArrayDeque<String>(newsSites.keySet());
		threads = new Thread[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println("New thread started...");
					String site;
					while ((site = getNext()) != null) {
						System.out.println("Crawling website: " + site);
						try {
							controller = new CrawlerController(site);
							controller.crawl();
						} catch (Exception e) {
							System.err.println("Error crawling data with site: " + site);
							e.printStackTrace();
							res = JSONify("statusCode", "500");
						}
					}
				}
				
			});
			threads[i].start();
		}
		for (int i = 0; i < NUM_THREADS; i++) {
			try {
				threads[i].join();
				System.out.println("Joined thread");
			} catch (InterruptedException e) {
				System.out.println("Unable to join threads");
				e.printStackTrace();
			}
		}
		System.out.println("All threads completed");
		res = JSONify("statusCode", "200");
	}
	
	private void train() {
		wingAnalyzer = new WingAnalyzer(WingAnalyzer.WINGS);
		wingAnalyzer.train();
	}
	
	private void analysis() {
		try {
			ArrayList<String> keys = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
			keys.add("statusCode");
			values.add("200");
			keys.add("classConditionalProbabilities");
			values.add(Database.getInstance().getClassConditionalProbabilities().toString());
			keys.add("classPriors");
			values.add(Database.getInstance().getClassPriors().toString());
			res = JSONify(keys, values);
		} catch (Exception e) {
			res = JSONify("statusCode", "500");
		}
	}
	
	public static String JSONify(ArrayList<String> keys, ArrayList<String> values) {
		String json = "{";
		if (keys.size() == values.size()) {
			for (int i = 0; i < keys.size(); i++) {
				json += JSONify(keys.get(i), values.get(i));
				if (i < keys.size() - 1) {
					json += ", ";
				}
			}
		}
		json += "}";
		return json;
	}
	
	public static String JSONify(String key, String value) {
		String json = "\"" + key + "\": \"" + value + "\"";
		return json;
	}
	
	private synchronized String getNext() {
		System.out.println("Webpages left: " + siteDeque.size());
		String site = null;
		if (siteDeque.size() > 0) {
			site = siteDeque.pop();
		}
		return site;
	}
}
