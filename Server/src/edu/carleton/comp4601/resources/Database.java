package edu.carleton.comp4601.resources;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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
	
	public synchronized void insert(ArrayList<HashMap<String, Double>> classConditionalProbabilities, ArrayList<Double> classPriors) {
		analyzerCollection.drop();
		analyzerCollection = database.getCollection("analyzerData");
		Document doc = new Document();
		doc.put("classPriors", classPriors);
		analyzerCollection.insertOne(doc);
		
		for (int i = 0; i < WingAnalyzer.WINGS.size();  i++) {
			Document classDoc = new Document();
			HashMap<String, Double> map = classConditionalProbabilities.get(i);
			if (map == null) {
				map = new HashMap<String, Double>();
			}
			System.out.println("for wing analyzer index " + i + ", keyset is " + map.keySet() + ", values are " + map.values());
			classDoc.putAll(map);
			analyzerCollection.insertOne(classDoc);
		}
	}
	
	public ArrayList<Double> getClassPriors() {
		FindIterable<Document> result = analyzerCollection.find();
		Document doc = result.first();
		if (doc != null) {
			return (ArrayList<Double>) doc.get("classPriors");
		}
		return null;
	}
	
	public ArrayList<HashMap<String, Double>> getClassConditionalProbabilities() {
		FindIterable<Document> result = analyzerCollection.find();
		ArrayList<HashMap<String, Double>> classConditionalProbabilities = new ArrayList<HashMap<String, Double>>();
		int count = 0;
		for (Document doc : result) {
			count++;
			if (count == 1) {
				continue;
			}
			HashMap<String, Double> map = new HashMap<String, Double>();
			for (Entry<String, Object> entry : doc.entrySet()) {
				if (!entry.getKey().equals("_id")) {
					map.put(entry.getKey(), (Double) entry.getValue());
				}
			}
			classConditionalProbabilities.add(map);
		}
		System.out.println("count is " + count);
		return classConditionalProbabilities;
	}
	
	/*
	 * Description: serializes a webpage so it can be added to the database
	 * Input: the webpage to serialize
	 * Return: the serialized webpage
	 */
	private Document serialize(WebPage webpage) {
		Document doc = new Document();
		doc.put("docId", webpage.getDocId());
		doc.put("name", webpage.getTitle());
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
		Crawler.resetDocId();
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
	
	public WebPage getWebPageByURL(String url) {
		Document query = new Document("url", url);
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
	public static synchronized Database getInstance() {
		if (instance == null) {
			instance = new Database();
		}
		return instance;
	}
	
}