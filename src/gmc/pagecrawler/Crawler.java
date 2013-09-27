/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gmc.pagecrawler;


import gmc.extractor.ReglarExpression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jy.htmlparse.BBCPageProcess;
import jy.htmlparse.HtmlParseUtil;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 *
 * @author Pok
 */
public class Crawler {

    private String htmlSource;
    private String host;
    private String cookie;
    private String chartset;

    /**
     * 构造函数
     *
     * @param host 主机地址，如：book.douban.com
     * @param cookie 登录cookie
     * @param chartset 编码
     */
    public Crawler(String host, String cookie, String chartset) {
        this.host = host;
        this.cookie = cookie;
        this.chartset = chartset;
    }

    /**
     * 爬取函数
     *
     * @param url 待爬取的url
     * @return content 爬取返回的源代码
     * @throws HttpException 
     * @throws InterruptedException 
     * @throws IOException 
     */
	public String crawler(String url) throws HttpException, IOException{
		/* 1 生成 HttpClinet 对象并设置参数 */
		HttpClient httpClient = new HttpClient();
		// 设置 Http 连接超时为5秒
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5000);

		/* 2 生成 GetMethod 对象并设置参数 */
		GetMethod getMethod = new GetMethod(url);
		// 设置 get 请求超时为 5 秒
		getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 10000);
		// 设置请求重试处理，用的是默认的重试处理：请求三次
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		StringBuilder sb = new StringBuilder();
		/* 3 执行 HTTP GET 请求 */
		
		int statusCode = httpClient.executeMethod(getMethod);
		/* 4 判断访问的状态码 */
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("Method failed: "
					+ getMethod.getStatusLine());
		}

		/* 5 处理 HTTP 响应内容 */
		// HTTP响应头部信息，这里简单打印

		// 读取为 InputStream，在网页内容数据量大时候推荐使用
		InputStream response = getMethod.getResponseBodyAsStream();
		
		InputStreamReader ir = new InputStreamReader(response, chartset);
		BufferedReader br = new BufferedReader(ir);
		
		String temp = null;
		while ((temp = br.readLine()) != null) {
			sb.append(temp);
			//增加换行符会影响正则匹配的效果。
			//sb.append("\n");
		}
		
		
		
		return sb.toString();
	}
    
    public static void main(String[] args) throws IOException, ParserException, InterruptedException
    {
    	String host = "blogs.wsj.com";
    	String cookie = "djcs_route=290e9e9b-de3b-460f-9b13-5462a4b89f21; DJCOOKIE=ORC=na,us||isClickedDESCrimNoThanksCookie=0; utag_main=_st:1376971053200$ses_id:1376969563399;exp-session; utag_main=_st:1380114165174$ses_id:1380111430788;exp-session; s_vnum=1382669047982&vn=2; _chartbeat2=vleui929xe42glhe.1380077059129.1380111334963.1; _chartbeat_uuniq=1; DJSESSION=continent=as||zip=||country=cn||region=gd||ORCS=asia||city=guangzhou||longitude=113.25||timezone=gmt+8||latitude=23.12; wsjregion=na,us; s_cc=true; s_invisit=true; gpv_pn=WSJ_Chinarealtimereportblog_home; s_sq=[[B]]";
    	String charset = "utf-8";
    	Crawler clawler = new Crawler(host, cookie, charset);
    	String url = "http://blogs.wsj.com/chinarealtime/2013/09/26/chengdu-continues-global-push-amid-scandal/";
    	String htmlString = clawler.crawler(url);
    	
    	//System.out.println(htmlString);
    	//htmlString = htmlString.replaceAll("<(noscript|script).*?</(noscript|script)>", ""); //去除javascript
    	String content = "";
    	StringBuilder sb = new StringBuilder();
    	List<String> titlelist = HtmlParseUtil.getContentByTagNameAndAttribute(htmlString, "div", "class", "articleHeadlineBox headlineType-newswire");
    	for(String s : titlelist)
    	{
    		sb.append(s);
    		System.out.println(s);
    	}
    	
    	List<String> articleList = HtmlParseUtil.getContentByTagNameAndAttribute(htmlString, "div", "class", "articlePage");
    	for(String s : articleList)
    	{
    		sb.append(s);
    		System.out.println(s);
    	}
    	content = sb.toString();
    	System.out.println(ReglarExpression.Regular("<small>(.*?)\\s*</small>", content, 1));
    	System.out.println(ReglarExpression.Regular("<h1>(.*?)</h1>", content,1));
    	System.out.println(ReglarExpression.Regular("<p>(.*?)</p>", content, 1).replaceAll("<.*?>", ""));
    	//Parser parser = new Parser(htmlString);
//    	NodeFilter spanFilter = new TagNameFilter("span");
//    	NodeFilter dateFilter = new HasAttributeFilter("class", "date");
//    	NodeFilter h1Filter = new TagNameFilter("h1");
//    	NodeFilter titleFilter = new HasAttributeFilter("class","story-header");
//    	NodeFilter dateAndFilter = new AndFilter(spanFilter, dateFilter);
//		NodeList nodes = parser.extractAllNodesThatMatch(dateAndFilter);
//		System.out.println(nodes.size());
//    	for(int i=0; i<nodes.size(); ++i)
//    	{
//    		System.out.println(nodes.elementAt(i).toPlainTextString());
//    	}
    	//NodeFilter titleFilter = new HasAttributeFilter("class","story-header");
    	//NodeFilter titleAndFilter = new AndFilter(h1Filter, titleFilter);

    	//NodeList nodes2 = parser.extractAllNodesThatMatch(titleFilter);
//    	System.out.println("nodes size" + nodes2.size());
//    	System.out.println(nodes2.elementAt(0).toPlainTextString());
    	
    	//把主体拿下来
//    	NodeFilter divFilter = new TagNameFilter("div");
//    	NodeFilter body = new HasAttributeFilter("class", "story-body");
//    	NodeFilter bodyAnd = new AndFilter(divFilter, body);
//    	String bodyHtml = parser.extractAllNodesThatMatch(bodyAnd).elementAt(0).toHtml();
//    	System.out.println(bodyHtml);
//    	Parser parser2 = new Parser(bodyHtml);
//    	NodeFilter pFilter = new TagNameFilter("p");
//    	NodeList nodes = parser2.extractAllNodesThatMatch(pFilter);
//    	System.out.println(nodes.size());
//    	for(int i=0; i<nodes.size(); ++i)
//    	{
//    		System.out.println(nodes.elementAt(i).toPlainTextString());
//    	}
//    	System.out.println(BBCPageProcess.getTime(htmlString));
//    	System.out.println(BBCPageProcess.getTitle(htmlString));
//    	System.out.println(BBCPageProcess.getArticleContent(htmlString));
//    	System.out.println(BBCPageProcess.getRelatedUrls(htmlString));
    }

    
}
