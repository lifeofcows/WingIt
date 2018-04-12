package edu.carleton.comp4601.resources;

public class Advertiser {

	/*
	 * Description: augments a page's html with advertising relevant to the user viewing the page and the page itself
	 * Input: the user viewing the page, the page to view
	 * Return: the augmented html
	 */
	public static String augment(String user, String page) {
		WebPage webpage = Database.getInstance().getWebPage(page);
		String pageHtml = webpage.getHTML();
		String pageAdGenre = webpage.getGenre();
		String userAdGenre = Database.getInstance().getUser(user, true).getPreferredGenre();
		String bodyTag = "<body>";
		int insertIndex = pageHtml.indexOf(bodyTag) + bodyTag.length();
		String ads = "<div>User-based Advertisement: " + userAdGenre + "<hr></div>" + "<div>Page-based Advertisement: " + pageAdGenre + "<hr></div>";
		pageHtml = pageHtml.substring(0, insertIndex) + ads + pageHtml.substring(insertIndex, pageHtml.length() - 1);
		return pageHtml;
	}
	
}
