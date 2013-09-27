package jy.wsj;

import gmc.pagecrawler.Crawler;

public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String host = "blogs.wsj.com";
    	String cookie = "djcs_route=290e9e9b-de3b-460f-9b13-5462a4b89f21; DJCOOKIE=ORC=na,us||isClickedDESCrimNoThanksCookie=0; utag_main=_st:1376971053200$ses_id:1376969563399;exp-session; utag_main=_st:1380114165174$ses_id:1380111430788;exp-session; s_vnum=1382669047982&vn=2; _chartbeat2=vleui929xe42glhe.1380077059129.1380111334963.1; _chartbeat_uuniq=1; DJSESSION=continent=as||zip=||country=cn||region=gd||ORCS=asia||city=guangzhou||longitude=113.25||timezone=gmt+8||latitude=23.12; wsjregion=na,us; s_cc=true; s_invisit=true; gpv_pn=WSJ_Chinarealtimereportblog_home; s_sq=[[B]]";
    	String charset = "utf-8";
    	Crawler clawler = new Crawler(host, cookie, charset);
    	String url = "http://blogs.wsj.com/chinarealtime/2013/09/27/watch-translating-chinas-twitter-equivalent/";
    	String htmlString = clawler.crawler(url);
    	
    	WsjPageProcess pageProcess = new WsjPageProcess();
    	pageProcess.process(htmlString);
    	System.out.println("title:"+pageProcess.getTitle());
    	System.out.println("date time :" + pageProcess.getDatetime());
    	System.out.println("content :" + pageProcess.getContent());
	}

}
