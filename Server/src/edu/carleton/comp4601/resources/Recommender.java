package edu.carleton.comp4601.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
	
	/*
	 * Description: constructor for the recommender class
	 * Input: none
	 * Return: none
	 */
	public Recommender() {
		authorName1 = "Avery Vine";
		authorName2 = "Maxim Kuzmenko";
		name = "WingIt";
	}

	/*
	 * Description: gets the name of the recommender system
	 * Input: none
	 * Return: html representation of the name
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getName() {
		System.out.println("name");
		String res = "{\"name\": \"" + name + "\"}";
		return res;
	}

	/*
	 * Description: reset the recommender system by wiping the database, crawling the pages, and analyzing page genres
	 * Input: the directory of the pages to crawl
	 * Return: 200 response code if successful, 500 otherwise
	 */
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
				wingAnalyzer = new WingAnalyzer();
				wingAnalyzer.analyze();
				break;
			default:
				res = Response.noContent().build();
		}
		return res;
	}
	
	/*
	 * Description: calculates sentiments for users' reviews and groups users into communities based off preferred genre
	 * Input: none
	 * Return: html representation of the users and the fields used for community calculation
	 */
	@GET
	@Path("url")
	@Produces(MediaType.APPLICATION_JSON)
	public String url(@QueryParam("url") String url) {
		System.out.println("url -> " + url);
		String res = "{'wing': 'neutral'}";
		return res;
	}
	
	/*
	 * Description: retrieves the list of webpages
	 * Input: none
	 * Return: html representation of the list of webpages
	 */
	@GET
	@Path("fetch")
	@Produces(MediaType.TEXT_HTML)
	public String fetch() {
		System.out.println("fetch");
		boolean setPrompts = true;
		String res = "<table border> ";
		res += WebPage.htmlTableHeader();
		for (WebPage webpage: Database.getInstance().getWebPages()) {
			res += webpage.htmlTableData(setPrompts);
		}
		res += " </table>";
		return wrapHTML("Fetch", res);
	}
	
	/*
	 * Description: wraps the body of an html document with the required tags
	 * Input: the title of the page, the body of the page
	 * Return: the wrapped page
	 */
	public String wrapHTML(String title, String body) {
		return "<html> <head> <title> " + title + " </title> </head> <body> " + body + " </body> </html>";
	}
}
