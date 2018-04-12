package edu.carleton.comp4601.resources;

import java.util.ArrayList;
import java.util.Arrays;

public class ContentClass {

	private int currWord;
	private ArrayList<String> words;
	
	/*
	 * Description: this class provides an easy interface for accessing the contents of a document while processing 
	 */
	public ContentClass(String content) {
		currWord = 0;
		content = clean(content);
		words = new ArrayList<String>(Arrays.asList(content.split(" ")));
	}
	
	/*
	 * Description: cleans the text provided by removing newline characters and trimming spaces
	 * Input: the text to clean
	 * Return: the cleaned text
	 */
	private String clean(String content) {
		content = content.replaceAll("\n", " ");
		content.trim();
		return content;
	}
	
	/*
	 * Description: returns whether there is another word available to be retrieved
	 * Input: none
	 * Return: a boolean indicating whether there is another word available to be retrieved
	 */
	public boolean hasNext() {
		if (currWord >= words.size()) {
			return false;
		}
		return true;
	}
	
	/*
	 * Description: retrieves the next word in the content
	 * Input: none
	 * Return: the next word in the content
	 */
	public String next() {
		String word = words.get(currWord);
		currWord++;
		return word;
	}
	
}
