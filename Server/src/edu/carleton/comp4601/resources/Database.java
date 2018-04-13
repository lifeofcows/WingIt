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
	MongoCollection<Document> analyzerCollection, webpageCollection;
	MongoClient mongoClient;
	MongoDatabase database;
	
	/*
	 * Description: this class provides an access point for the MongoDB collections
	 */
	public Database() {
		mongoClient = new MongoClient("localhost", 27017);
		database = mongoClient.getDatabase("wingit");
		
		webpageCollection = database.getCollection("webpageData");
		analyzerCollection = database.getCollection("analyzerData");
	}
	
	/*
	 * Description: inserts a webpage into the database, replacing whatever may exist already
	 * Input: the webpage to insert
	 * Return: none
	 */
	public synchronized void insert(WebPage webpage) {
		webpageCollection.replaceOne(new Document("docId", webpage.getDocId()), serialize(webpage), new UpdateOptions().upsert(true));
	}
	
	public void insert(ArrayList<HashMap<String, Double>> classConditionalProbabilities, ArrayList<Double> classPriors) {
		analyzerCollection.drop();
		analyzerCollection = database.getCollection("analyzerData");
		Document doc = new Document();
		doc.put("classConditionalProbabilities", classConditionalProbabilities);
		doc.put("classPriors", classPriors);
		analyzerCollection.insertOne(doc);
	}
	
	public ArrayList<Double> getClassPriors() {
		FindIterable<Document> result = webpageCollection.find();
		Document doc = result.first();
		if (doc != null) {
			return (ArrayList<Double>) doc.get("classPriors");
		}
		return null;
	}
	
	public ArrayList<HashMap<String, Double>> getClassConditionalProbabilities() {
		FindIterable<Document> result = webpageCollection.find();
		Document doc = result.first();
		if (doc != null) {
			return (ArrayList<HashMap<String, Double>>) doc.get("classConditionalProbabilities");
		}
		return null;
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
		doc.put("content", webpage.getContent());
		doc.put("wing", webpage.getWing());
		return doc;
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
		String content = doc.getString("content");
		String html = doc.getString("html");
		String wing = doc.getString("wing");
		return new WebPage(docId, name, url, content, wing);
	}

	/*
	 * Description: drops the content of the database
	 * Input: none
	 * Return: none
	 */
	public void clear() {
		webpageCollection.drop();
		analyzerCollection.drop();
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
