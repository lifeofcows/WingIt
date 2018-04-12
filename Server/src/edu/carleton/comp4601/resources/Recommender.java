package edu.carleton.comp4601.resources;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
	GenreAnalyzer genreAnalyzer;
	SentimentAnalyzer sentimentAnalyzer;

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
	@Produces(MediaType.TEXT_HTML)
	public String getName() {
		System.out.println("name");
		String res = "<h1> " + name + " </h1>";
		return wrapHTML(name, res);
	}

	/*
	 * Description: reset the recommender system by wiping the database, crawling the pages, and analyzing page genres
	 * Input: the directory of the pages to crawl
	 * Return: 200 response code if successful, 500 otherwise
	 */
	@GET
	@Path("reset/{dir}")
	public Response reset(@PathParam("dir") String dir) {
		System.out.println("reset -> " + dir);
		Response res = Response.ok().build();
		try {
			controller = new CrawlerController(dir);
			controller.crawl();
			genreAnalyzer = new GenreAnalyzer();
			genreAnalyzer.analyze();
		} catch (Exception e) {
			System.err.println("Error crawling data in dir: " + dir);
			e.printStackTrace();
			res = Response.serverError().build();
		}
		return res;
	}
	
	/*
	 * Description: calculates sentiments for users' reviews and groups users into communities based off preferred genre
	 * Input: none
	 * Return: html representation of the users and the fields used for community calculation
	 */
	@GET
	@Path("context")
	@Produces(MediaType.TEXT_HTML)
	public String context() {
		System.out.println("context");
		sentimentAnalyzer = new SentimentAnalyzer();
		sentimentAnalyzer.analyze();
				
		String res = "<div>Context</div> <table border='1px'> ";
		res += User.htmlTableHeader();
		for (User user : sentimentAnalyzer.getAnalyzedUsers()) {
			res += user.htmlTableData();
		}
		res += "</table>";
		return wrapHTML("Context", res);
	}
	
	/*
	 * Description: visual representation of users grouped into their communities based on their preferred genre
	 * Input: None
	 * Return: an HTML representation of users grouped into their communities based on their preferred genre
	 */
	@GET
	@Path("community")
	@Produces(MediaType.TEXT_HTML)
	public String community() {
		System.out.println("community");
		String res = "<div>Community</div> <table border>";
		for (String genre : GenreAnalyzer.GENRES) {
			String usersInCommunity = "";
			ArrayList<User> users = Database.getInstance().getUsersByPreferredGenre(genre);
			if (users.size() == 0) {
				res += "</table>Error: please run /context first!";
				return wrapHTML("Community", res);
			}
			else {
				for (User user : users) { 
					usersInCommunity += "<a href=" + user.getUrl() + ">" + user.getName() + "</a>, ";
				}
				res += "<tr> <td> " + genre + " </td> <td> " + usersInCommunity.substring(0, usersInCommunity.length() - 2) + " </td> </tr>";	
			}
		}
		res += " </table>";
		
		return wrapHTML("Community", res);
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
	 * Description: fetches a particular page from the point of view of a particular user
	 * Input: the user accessing the page, the page to access
	 * Return: html representation of the page, augmented with ads relevant to the user and the page
	 */
	@GET
	@Path("fetch/{user}/{page}")
	@Produces(MediaType.TEXT_HTML)
	public String fetch(@PathParam("user") String user, @PathParam("page") String page) {
		System.out.println("fetch -> " + user + ", " + page);
		String res = Advertiser.augment(user, page);
		System.out.println(res);
		return res;
	}
	
	/*
	 * Description: shows the advertisements for a given category
	 * Input: Category as a string
	 * Return: html representation that gives advertising for the category (if exists)
	 */
	@GET
	@Path("advertising/{category}")
	@Produces(MediaType.TEXT_HTML)
	public String advertising(@PathParam("category") String category) {
		System.out.println("advertising -> " + category);
		String res;
		if (GenreAnalyzer.GENRES.contains(category.toLowerCase())) {
			res = "Advertising for " + category;
		}
		else {
			res = "Invalid genre " + category;
		}
		return wrapHTML("Advertising", res);
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
