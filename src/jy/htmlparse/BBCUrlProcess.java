package jy.htmlparse;

public class BBCUrlProcess {
	private static final String urlAdd = "http://www.bbc.co.uk";//要拼接到前段的url
	
	public static String joinUrl(String url)
	{
		return urlAdd+url;
	}
}
