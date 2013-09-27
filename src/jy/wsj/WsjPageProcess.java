package jy.wsj;

import gmc.extractor.ReglarExpression;

import java.util.List;

import jy.htmlparse.HtmlParseUtil;

/**
 * maybe can use singleton pattern ?
 * it isn't suitable
 * @author JY
 *
 */
public class WsjPageProcess implements PageProcessApi {

	
	private String title;
	private String datetime;
	private String content;
	
	
	public WsjPageProcess() {
		
	}


	@Override
	public void process(String htmlPage)
	{
		StringBuilder sb = new StringBuilder(); 
		String tmp = null;
		//1 - 获取<div class="articleHeadlineBox headlineType-newswir">..</div>里面的内容。
		//	the div box include "datetime" and "title"
		// 将获取到的内容存放到一个list里面。
		List<String> titlelist = HtmlParseUtil.getContentByTagNameAndAttribute
							(htmlPage, "div", "class", "articleHeadlineBox headlineType-newswire");
		//2 - save the target html text into sb
    	for(String s : titlelist)
    	{
    		sb.append(s);
    	}
    	// 3 - retrieve <div class="article">..</div>. it include the "article content"
    	// same as step 1, save the content into sb
    	List<String> articleList = HtmlParseUtil.getContentByTagNameAndAttribute(htmlPage, "div", "class", "articlePage");
    	for(String s : articleList)
    	{
    		sb.append(s);
    	}
    	//4 - using regular expression extract title datetime and content.
    		// 4.1 - save the content of "sb" into variable "tmp"
    	tmp = sb.toString();
    		// 4.2 - using re to extract specific content
    	this.datetime = ReglarExpression.Regular("<small>(.*?)\\s*</small>", tmp, 1);
    	this.title = ReglarExpression.Regular("<h1>(.*?)</h1>", tmp,1);
    	this.content = ReglarExpression.Regular("<p>(.*?)</p>", tmp, 1).replaceAll("<.*?>", "");
	}


	public String getTitle() {
		return title;
	}


	public String getDatetime() {
		return datetime;
	}


	public String getContent() {
		return content;
	}
	
	
}
