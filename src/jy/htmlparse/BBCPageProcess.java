package jy.htmlparse;

import gmc.extractor.ReglarExpression;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class BBCPageProcess {
	
	private static final NodeFilter tagSpan = new TagNameFilter("span");
	private static final NodeFilter classDate = new HasAttributeFilter("class", "date");
	private static final NodeFilter tagH1 = new TagNameFilter("h1");
	private static final NodeFilter classStoryHeader = new HasAttributeFilter("class", "story-header");
	private static final NodeFilter tagDiv = new TagNameFilter("div");
	private static final NodeFilter classStoryBody = new HasAttributeFilter("class", "story-body");
	private static final NodeFilter tagP = new TagNameFilter("p");
	private static final NodeFilter tagUl = new TagNameFilter("ul");
	private static final NodeFilter tagA = new TagNameFilter("a");
	private static final NodeFilter classRelatedLinksList = new HasAttributeFilter("class", "related-links-list");
	private static final NodeFilter classStory = new HasAttributeFilter("class", "story");
	
	
	private static String nodesToString(NodeList nodes)
	{
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<nodes.size(); ++i)
		{
			sb.append(nodes.elementAt(i).toPlainTextString());
			if(i!=nodes.size()-1)
				sb.append("\n");
		}
		return sb.toString();
	}
	
	private static String nodesToHtml(NodeList nodes)
	{
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<nodes.size(); ++i)
		{
			sb.append(nodes.elementAt(i).toHtml());
		}
		return sb.toString();
	}
	
	private static ArrayList<String> nodesToUrlList(NodeList nodes)
	{
		System.out.println("nodesToUrlList");
		ArrayList<String> urls = new ArrayList<String>();
		String aHtml = null;
		String url = null;
		for(int i=0; i<nodes.size(); ++i)
		{
			Node node = nodes.elementAt(i);
			aHtml = node.toHtml();
			url = ReglarExpression.Regular("href=\".*?\"", aHtml);
			url = url.replaceAll("href=|\"", "");
			//保证添加进去的url都是合法的.
			if(url.matches("^/news.*?"))
				urls.add(BBCUrlProcess.joinUrl(url)); //获取到的url是news/开头的，属于相对地址，把地址处理成完整的地址
		}
		return urls;
	}
	
	public static ArrayList<String> getBeginPageUrls(String beginPage) throws ParserException
	{
		Parser parser = new Parser(beginPage);
		NodeList nodes = parser.extractAllNodesThatMatch(new AndFilter(tagA, classStory));
		
		return nodesToUrlList(nodes);
	}
	
	public static String getTime(String htmlString) throws ParserException
	{
		//获取span节点，class=date
		Parser parser = new Parser(htmlString);
		NodeFilter andFilter = new AndFilter(tagSpan, classDate);
		NodeList nodes = parser.extractAllNodesThatMatch(andFilter);
		return nodesToString(nodes);
	}
	
	public static String getTitle(String htmlString) throws ParserException
	{
		//获取h1节点class=story-header
		Parser parser = new Parser(htmlString);
		NodeFilter andFilter = new AndFilter(tagH1, classStoryHeader);
		NodeList nodes = parser.extractAllNodesThatMatch(andFilter);
		return enescape(nodesToString(nodes));
	}
	
	public static String getArticleContent(String htmlString) throws ParserException
	{
		//htmlString = htmlString.replaceAll("<script.*?</script>", "");
		Parser parser = new Parser(htmlString);
		NodeFilter andFilter1 = new AndFilter(tagDiv, classStoryBody); 
		NodeList nodes =  parser.extractAllNodesThatMatch(andFilter1); //先获得storybody里面的内容，再进一步提取出div里面的内容
		String bodyHtml = nodesToHtml(nodes);
		parser = new Parser(bodyHtml);
		nodes = parser.extractAllNodesThatMatch(tagP);
		return enescape(nodesToString(nodes));
	}
	
	public static ArrayList<String> getRelatedUrls(String htmlString) throws ParserException
	{
		Parser parser = new Parser(htmlString);
		NodeFilter filter = new AndFilter(tagUl, classRelatedLinksList);
		NodeList nodes = parser.extractAllNodesThatMatch(filter);
		htmlString = nodesToHtml(nodes);
		parser = new Parser(htmlString);
		nodes = parser.extractAllNodesThatMatch(tagA);
		//System.out.println(htmlString);
		return nodesToUrlList(nodes);
	}
	

	private static String enescape(String content)
	{
		if(content == null)
			return "";
		String html = content;
		html = StringUtils.replace(html, "&apos;","'");
		html = StringUtils.replace(html, "&quot;","\"");
		html = StringUtils.replace(html, "&nbsp;&nbsp;", "\t");// 替换跳格
		//html = StringUtils.replace(html, " ", "&nbsp;");// 替换空格
		html = StringUtils.replace(html, "&lt;", "<");
		html = StringUtils.replace(html, "&gt;", ">");
		html = StringUtils.replace(html, "&#039;", "'");
		return html;
	}
	
	public static void main(String[] args)
	{
		System.out.println(enescape("<html>"));
	}
	
}
