package edu.carleton.comp4601.resources;

import java.util.HashSet;

public class WebPage {

	private int docId;
	private String title, url, genre, content, html, wing;
	private HashSet<String> users;
	
	/*
	 * Description: a webpage is a representation of a page containing reviews for a particular movie
	 */
	public WebPage(int docId, String title, String url, String content, String wing) {
		this.docId = docId;
		this.title = title;
		this.url = url;
		this.content = content;
		this.wing = wing;
	}
	
	/*
	 * Description: retrieves the docId of a webpage
	 * Input: none
	 * Return: the docId of the webpage
	 */
	public int getDocId() { 
		return docId;
	}
	
	public String getWing() {
		return wing;
	}
	
	/*
	 * Description: retrieves the name of a webpage
	 * Input: none
	 * Return: the name of the webpage
	 */
	public String getTitle() {
		return title;
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
			return "<tr> <td> " + docId + " </td> <td> " + title + " </td> <td> <a onclick='parent.promptForUser(\"" + title + "\");' href='javascript:void(0);'> " + url + " </a> </td> <td> " + genre + " </td> </tr> ";
		} else {
			return "<tr> <td> " + docId + " </td> <td> " + title + " </td> <td> <a href='" + url + "'> " + url + " </a> </td> <td> " + genre + " </td> </tr> ";	
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
