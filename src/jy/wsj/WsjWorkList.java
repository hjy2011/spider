package jy.wsj;

import gmc.extractor.ReglarExpression;
import gmc.pagecrawler.Crawler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpException;

import jy.db.DBUtils;
import jy.util.MD5Util;

public class WsjWorkList {

	private String host = "blogs.wsj.com";
	private String cookie = "djcs_route=290e9e9b-de3b-460f-9b13-5462a4b89f21; DJCOOKIE=ORC=na,us||isClickedDESCrimNoThanksCookie=0; utag_main=_st:1376971053200$ses_id:1376969563399;exp-session; utag_main=_st:1380114165174$ses_id:1380111430788;exp-session; s_vnum=1382669047982&vn=2; _chartbeat2=vleui929xe42glhe.1380077059129.1380111334963.1; _chartbeat_uuniq=1; DJSESSION=continent=as||zip=||country=cn||region=gd||ORCS=asia||city=guangzhou||longitude=113.25||timezone=gmt+8||latitude=23.12; wsjregion=na,us; s_cc=true; s_invisit=true; gpv_pn=WSJ_Chinarealtimereportblog_home; s_sq=[[B]]";
	private String charset = "utf-8";
	private ArrayList<String> doList = new ArrayList<String>(); //已经爬过的url
	private ArrayList<String> urls = new ArrayList<String>(); //待爬取的url
	private Connection conn = null;
	
	public WsjWorkList() throws SQLException
	{
		
		PreparedStatement pstmt = null; 	//声明PreparedStatement对象
		ResultSet rs = null;			 	//声明ResultSet对象
		conn = DBUtils.getConnection();
		if(conn.isClosed())
		{
			System.out.println("conn is closed");
		}
		
		String sql = "select * from worklist";
		pstmt = conn.prepareStatement(sql);
		rs = pstmt.executeQuery();
		while(rs.next())
		{
			doList.add(rs.getString(2));
		}
	}
	
	
	private void process()
	{
    	Crawler clawler = new Crawler(host, cookie, charset);
    	String base = "http://blogs.wsj.com/chinarealtime/page/";
    	int i;
    	int count=0;
    	for(i=582; i<=695; ++i)
    	{
    		try {
    			String curUrl = base + i + "/";
				String page = clawler.crawler(curUrl);
				List<String> ulist = ReglarExpression.RegularArray
							("(<h1|<h2)\\s*?class=\"postTitle\".*?href=\"(.*?)\".*?(</h1>|</h2>)", page, 2);
				saveUrl(ulist);
				count+=10;
			} catch (HttpException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("time out");
				e.printStackTrace();
			} catch (SQLException e) {
				System.out.println("sql exception");
				e.printStackTrace();
			}
    		finally{
    			System.out.println("爬过页面："+i);
    			System.out.println("已存url数目："+count);
    		}
    	}
	}
	
	private void saveUrl(List<String> list) throws SQLException
	{
		PreparedStatement pstmt = null; 	//声明PreparedStatement对象
		ResultSet rs = null;			 	//声明ResultSet对象
		
		String sql = "insert into worklist(url, md5) values(?, ?)";
		
		for(String s : list)
		{
			String md5 = MD5Util.MD5(s);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, s);
			pstmt.setString(2, md5);
			int result = pstmt.executeUpdate();
			
		}
	}
	
	public static void main(String[] args) throws SQLException
	{
//		String host = "blogs.wsj.com";
//    	String cookie = "djcs_route=290e9e9b-de3b-460f-9b13-5462a4b89f21; DJCOOKIE=ORC=na,us||isClickedDESCrimNoThanksCookie=0; utag_main=_st:1376971053200$ses_id:1376969563399;exp-session; utag_main=_st:1380114165174$ses_id:1380111430788;exp-session; s_vnum=1382669047982&vn=2; _chartbeat2=vleui929xe42glhe.1380077059129.1380111334963.1; _chartbeat_uuniq=1; DJSESSION=continent=as||zip=||country=cn||region=gd||ORCS=asia||city=guangzhou||longitude=113.25||timezone=gmt+8||latitude=23.12; wsjregion=na,us; s_cc=true; s_invisit=true; gpv_pn=WSJ_Chinarealtimereportblog_home; s_sq=[[B]]";
//    	String charset = "utf-8";
//    	Crawler clawler = new Crawler(host, cookie, charset);
		WsjWorkList w = new WsjWorkList();
    	w.process();
    	
    	
    	
		
	}

}
