package edu.carleton.comp4601.resources;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bson.Document;

public class Database {

	static Database instance;
	MongoCollection<Document> userCollection, webpageCollection;
	MongoClient mongoClient;
	MongoDatabase database;
	
	/*
	 * Description: this class provides an access point for the MongoDB collections
	 */
	public Database() {
		mongoClient = new MongoClient("localhost", 27017);
		database = mongoClient.getDatabase("assignment2");
		
		userCollection = database.getCollection("userData");
		webpageCollection = database.getCollection("webpageData");
	}
	
	/*
	 * Description: inserts a user into the database, replacing whatever may exist already
	 * Input: the user to insert
	 * Return: none
	 */
	public synchronized void insert(User user) {
		userCollection.replaceOne(new Document("docId", user.getDocId()), serialize(user), new UpdateOptions().upsert(true));
	}
	
	/*
	 * Description: inserts a webpage into the databse, replacing whatever may exist already
	 * Input: the webpage to insert
	 * Return: none
	 */
	public synchronized void insert(WebPage webpage) {
		webpageCollection.replaceOne(new Document("docId", webpage.getDocId()), serialize(webpage), new UpdateOptions().upsert(true));
	}
	
	/*
	 * Description: serializes a user so it can be added to the database
	 * Input: the user to serialize
	 * Return: the serialized user
	 */
	private Document serialize(User user) {
		Document doc = new Document();
		doc.put("docId", user.getDocId());
		doc.put("name", user.getName());
		doc.put("url", user.getUrl());
		doc.put("preferredGenre", user.getPreferredGenre());
		doc.put("webpages", user.getWebPages());
		HashMap<String, BigDecimal> sentimentScores = user.getSentiments();
		for (String genre : GenreAnalyzer.GENRES) {
			doc.put(genre, sentimentScores.get(genre).toEngineeringString());
		}
		for (String webpage : user.getWebPages()) {
			doc.put(webpage, user.getSentiment(webpage).toEngineeringString());
		}
		return doc;
	}
	
	/*
	 * Description: serializes a webpage so it can be added to the database
	 * Input: the webpage to serialize
	 * Return: the serialized webpage
	 */
	private Document serialize(WebPage webpage) {
		Document doc = new Document();
		doc.put("docId", webpage.getDocId());
		doc.put("name", webpage.getName());
		doc.put("url", webpage.getUrl());
		doc.put("users", webpage.getUsers());
		doc.put("genre", webpage.getGenre());
		doc.put("content", webpage.getContent());
		doc.put("html", webpage.getHTML());
		return doc;
	}
	
	/*
	 * Description: deserializes a user so it can be used
	 * Input: the user to be deserialized, a boolean indicating whether user movie sentiments should be transferred from the database
	 * Return: the deserialized user
	 */
	@SuppressWarnings("unchecked")
	private User deserializeUser(Document doc, boolean addSentiments) {
		int docId = doc.getInteger("docId", -1);
		String name = doc.getString("name");
		String url = doc.getString("url");
		String preferredGenre = doc.getString("preferredGenre");
		ArrayList<String> webpages = (ArrayList<String>) doc.get("webpages");
		HashMap<String, BigDecimal> sentimentScores = new HashMap<String, BigDecimal>();
		for (String genre : GenreAnalyzer.GENRES) {
			sentimentScores.put(genre, new BigDecimal(doc.getString(genre)));
		}
		User user = new User(docId, name, url, preferredGenre, webpages, sentimentScores);
		if (addSentiments) {
			for (String webpageString : webpages) {
				WebPage webpage = getWebPage(webpageString);
				if (webpage != null) {
					String sentiment = doc.getString(webpageString);
					user.addGenreSentiment(webpage.getGenre(), webpageString, new BigDecimal(sentiment));
				}
			}
		}
		return user;
	}
	
	/*
	 * Description: deserializes a webpage so it can be used
	 * Input: the webpage to be deserialized
	 * Return: the deserialized webpage
	 */
	@SuppressWarnings("unchecked")
	private WebPage deserializeWebPage(Document doc) {
		int docId = doc.getInteger("docId", -1);
		String name = doc.getString("name");
		String url = doc.getString("url");
		HashSet<String> users = new HashSet<String>((ArrayList<String>) doc.get("users"));
		String genre = doc.getString("genre");
		String content = doc.getString("content");
		String html = doc.getString("html");
		return new WebPage(docId, name, url, users, genre, content, html);
	}

	/*
	 * Description: drops the content of the database
	 * Input: none
	 * Return: none
	 */
	public void clear() {
		userCollection.drop();
		webpageCollection.drop();
	}
	
	/*
	 * Description: retrieves a user by name from the database
	 * Input: the name of the user, a boolean indicating whether user movie sentiments should be transferred from the database
	 * Return: the deserialized user
	 */
	public User getUser(String name, boolean addSentiments) {
		Document query = new Document("name", name);
		FindIterable<Document> result = userCollection.find(query);
		Document doc = result.first();
		if (doc != null) {
			return deserializeUser(doc, addSentiments);
		}
		return null;
	}
	
	/*
	 * Description: retrieves all the users from the database
	 * Input: boolean indicating whether or not movie sentiments should be retrieved
	 * Return: the list of all users
	 */
	public ArrayList<User> getUsers(boolean addSentiments) {
		Document query = new Document();
		FindIterable<Document> docs = userCollection.find(query);
		ArrayList<User> users = new ArrayList<User>();
		for (Document doc : docs) {
			users.add(deserializeUser(doc, addSentiments));
		}
		return users;	 
	}
	
	public ArrayList<User> getUsersByPreferredGenre(String genre) {
		genre = genre.toLowerCase();
		if (!GenreAnalyzer.GENRES.contains(genre)) {
			return null;
		}
		Document query = new Document("preferredGenre", genre);
		FindIterable<Document> docs = userCollection.find(query);
		ArrayList<User> users = new ArrayList<User>();
		for (Document doc : docs) {
			users.add(deserializeUser(doc, false));
		}
		return users;
	}
	
	public void setUsers(ArrayList<User> users) {
		userCollection.drop();
		userCollection = database.getCollection("userData");
		
		for (User user : users) {
			insert(user);
		}
	}
	
	/*
	 * Description: retrieves a webpage by name from the database
	 * Input: the name of the webpage
	 * Return: the webpage
	 */
	public WebPage getWebPage(String name) {
		Document query = new Document("name", name);
		FindIterable<Document> result = webpageCollection.find(query);
		Document doc = result.first();
		if (doc != null) {
			return deserializeWebPage(doc);
		}
		return null;
	}
	
	/*
	 * Description: retrieves a list of all of the webpages in the database
	 * Input: none
	 * Return: the list of webpages
	 */
	public ArrayList<WebPage> getWebPages() {
		ArrayList<Document> docs = (ArrayList<Document>) webpageCollection.find().into(new ArrayList<Document>());
		System.out.println("Number of webpage documents: " + docs.size());
		ArrayList<WebPage> webpageList = new ArrayList<WebPage>();
		for (Document doc : docs) {
			webpageList.add(deserializeWebPage(doc));
		}
		return webpageList;	 
	}
	
	/*
	 * Description: provides access to the singleton instance of the database
	 * Input: none
	 * Return: the instance of the database
	 */
	public static Database getInstance() {
		if (instance == null) {
			instance = new Database();
		}
		return instance;
	}
	
}
