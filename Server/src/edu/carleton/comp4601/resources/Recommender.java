package edu.carleton.comp4601.resources;

import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
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
	static {
		newsSites = new HashMap<String, String>();
		newsSites.put("https://www.vox.com/", WingAnalyzer.WINGS.get(0));
		newsSites.put("https://www.economist.com/", WingAnalyzer.WINGS.get(1));
		newsSites.put("https://www.infowars.com/", WingAnalyzer.WINGS.get(2));
	}
	public static String currentWing;
	
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
	public Response admin(@QueryParam("adminRequest") String adminRequest) {
		System.out.println("admin -> " + adminRequest);
		Response res = Response.ok().build();
		switch (adminRequest) {
			case "reset":
				for (String site : newsSites.keySet()) {
					try {
						controller = new CrawlerController(site);
//						controller.crawl();
					} catch (Exception e) {
						System.err.println("Error crawling data with site: " + site);
						e.printStackTrace();
						res = Response.serverError().build();
					}
				}
				wingAnalyzer = new WingAnalyzer(WingAnalyzer.WINGS);
				wingAnalyzer.train();
				break;
			default:
				res = Response.noContent().build();
		}
		return res;
	}
	
	@GET
	@Path("url")
	@Produces(MediaType.APPLICATION_JSON)
	public String url(@QueryParam("url") String url) {
		System.out.println("url -> " + url);
		wingAnalyzer = new WingAnalyzer();
		String res = wingAnalyzer.analyze(url);
		return res;
	}
	
}
