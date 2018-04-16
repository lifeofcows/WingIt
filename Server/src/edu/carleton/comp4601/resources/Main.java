package edu.carleton.comp4601.resources;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class Main {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	String name, authorName1, authorName2;
	CrawlerController controller;
	WingAnalyzer wingAnalyzer;
	public static HashMap<String, String> newsSites;
	private static ArrayDeque<String> siteDeque;
	public static String res;
	private Thread[] threads;
	static {
		newsSites = new HashMap<String, String>();
		newsSites.put("https://www.vox.com/", WingAnalyzer.left);
		newsSites.put("https://www.buzzfeed.com/", WingAnalyzer.left);
		newsSites.put("https://www.washingtonpost.com/", WingAnalyzer.left);
		newsSites.put("https://www.cnn.com/", WingAnalyzer.left);
		newsSites.put("https://www.economist.com/", WingAnalyzer.centrist);
		newsSites.put("https://www.reuters.com/", WingAnalyzer.centrist);
		newsSites.put("https://apnews.com/", WingAnalyzer.centrist);
		newsSites.put("https://www.usatoday.com/", WingAnalyzer.centrist);
		newsSites.put("https://www.infowars.com/", WingAnalyzer.right);
		newsSites.put("http://www.breitbart.com/", WingAnalyzer.right);
		newsSites.put("https://www.wsj.com/", WingAnalyzer.right);
		newsSites.put("https://www.nationalreview.com/", WingAnalyzer.right);
	}
	private static final int NUM_THREADS = 3;
	
	public Main() {
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
			case "re-train":
				train();
				break;
			case "analytics":
				analysis();
				break;
			default:
				res = JSONify("statusCode", "500", true);
		}
		System.out.println("Returning response:");
		System.out.println(res);
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
							res = JSONify("statusCode", "500", true);
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
		res = JSONify("statusCode", "200", true);
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
			res = JSONify("statusCode", "500", true);
		}
	}
	
	public static String JSONify(ArrayList<String> keys, ArrayList<String> values) {
		String json = "";
		if (keys.size() == values.size()) {
			for (int i = 0; i < keys.size(); i++) {
				json += JSONify(keys.get(i), values.get(i), false);
				if (i < keys.size() - 1) {
					json += ", ";
				}
			}
		}
		return wrapJSON(json);
	}
	
	public static String JSONify(String key, String value, boolean wrap) {
		String json = "\"" + key + "\": ";
		if (value.startsWith("{")) {
			json += value;
		} else {
			json += "\"" + value + "\"";
		}
		if (wrap) {
			return wrapJSON(json);
		}
		return json;
	}
	
	public static String JSONify(String key, List<String> list, boolean wrap) {
		String json = "\"" + key + "\": [";
		for (int i = 0; i < list.size(); i++) {
			json += "\"" + list.get(i) + "\"";
			if (i < list.size() - 1) {
				json += ", ";
			}
		}
		json += "]";
		if (wrap) {
			return wrapJSON(json);
		}
		return json;
	}
	
	private static String wrapJSON(String json) {
		json = "{" + json + "}";
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
