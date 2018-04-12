package edu.carleton.comp4601.resources;

import java.util.HashSet;

public class WebPage {

	private int docId;
	private String name, url, genre, content, html;
	private HashSet<String> users;
	
	/*
	 * Description: a webpage is a representation of a page containing reviews for a particular movie
	 */
	public WebPage(int docId, String name, String url, String content, String html) {
		this.docId = docId;
		this.name = name;
		this.url = url;
		this.content = content;
		this.html = html;
	}
	
	/*
	 * Description: retrieves the docId of a webpage
	 * Input: none
	 * Return: the docId of the webpage
	 */
	public int getDocId() { 
		return docId;
	}
	
	/*
	 * Description: retrieves the name of a webpage
	 * Input: none
	 * Return: the name of the webpage
	 */
	public String getName() {
		return name;
	}
	
	/*
	 * Description: retrieves the url of a webpage
	 * Input: none
	 * Return: the url of the webpage
	 */
	public String getUrl() {
		return url;
	}
	
	/*
	 * Description: retrieves the list of users who wrote reviews on a webpage
	 * Input: none
	 * Return: the list of users
	 */
	public HashSet<String> getUsers() {
		return users;
	}
	
	/*
	 * Description: sets the genre of the movie on a webpage
	 * Input: the genre to be set
	 * Return: none
	 */
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	/*
	 * Description: retrieves the genre of the movie on a webpage
	 * Input: none
	 * Return: the genre of the movie
	 */
	public String getGenre() {
		return genre;
	}
	
	/*
	 * Description: retrieves the content of a webpage
	 * Input: none
	 * Return: the content of the webpage
	 */
	public String getContent() {
		return content;
	}
	
	/*
	 * Description: retrieves the html representation of a webpage
	 * Input: none
	 * Return: the html representation of the webpage
	 */
	public String getHTML() {
		return html;
	}
	
	
	
	/*
	 * Description: constructs an html table representation of the data in this class
	 * Input: a boolean indicating whether the urls in this webpage should prompt for a user on click
	 * Return: the html table representation of the data
	 */
	public String htmlTableData(boolean setPrompts) {
		if (setPrompts) {
			return "<tr> <td> " + docId + " </td> <td> " + name + " </td> <td> <a onclick='parent.promptForUser(\"" + name + "\");' href='javascript:void(0);'> " + url + " </a> </td> <td> " + genre + " </td> </tr> ";
		} else {
			return "<tr> <td> " + docId + " </td> <td> " + name + " </td> <td> <a href='" + url + "'> " + url + " </a> </td> <td> " + genre + " </td> </tr> ";	
		}
	}
	
	/*
	 * Description: constructs an html table header for the data in this class
	 * Input: none
	 * Return: the html table header for the data
	 */
	public static String htmlTableHeader() {
		return "<tr> <th> ID </th> <th> Name </th> <th> URL </th> <th> Genre </th> </tr> ";
	}
	
}
