package jy.test;

import gmc.pagecrawler.Crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jy.htmlparse.BBCPageProcess;
import jy.htmlparse.BBCUrlProcess;
import jy.util.MD5Util;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;

public class BBCCrawler {
	private static final String host = "www.bbc.co.uk";
	private static final String cookie = "";
	private static final String charset = "utf-8";
	private static final Set<String> urlDone = Collections.synchronizedSet(new HashSet<String>()); //存储已经爬过的url 共享
	private static final LinkedBlockingQueue<String> urlUnDo = new LinkedBlockingQueue<String>(); //存储未走过的url，阻塞队列  共享
	private static final Set<String> urlUnDoSet = Collections.synchronizedSet(new HashSet<String>());//未走过的 
	private Crawler crawler;
	private static Parser parser;
	private static int count = 0;//获取文章数目  共享
	private Lock lock = new ReentrantLock();
	/**
	 * 初始化的时候获取种子url中所有链接，并添加到待爬取队列中
	 * @param beginUrl 种子URL
	 * @throws IOException 
	 * @throws ParserException 
	 */
	public BBCCrawler(String beginUrl) throws IOException, ParserException
	{
		crawler = new Crawler(host, cookie, charset);
		String htmlResult = crawler.crawler(beginUrl);//获取种子页面
		ArrayList<String> urls = BBCPageProcess.getBeginPageUrls(htmlResult);
		addUnDoUrl(urls);
	}
	
	private boolean isDoneUrl(String url)
	{
		return urlDone.contains(MD5Util.MD5(url));
	}
	
	private boolean isContainUrl(String url)
	{
		return urlUnDoSet.contains(MD5Util.MD5(url));
	}
	private void addUnDoUrl(List<String> urls)
	{
		String url = null;
		for(int i=0; i<urls.size(); ++i)
		{
			url = urls.get(i);
			if(!isDoneUrl(url) && !isContainUrl(url))
			{
				urlUnDo.offer(url);
				urlUnDoSet.add(MD5Util.MD5(url));
				System.out.printf("添加url：%s%n", url);
			}
		}
	}
	
	private int addCount()
	{
		int ret;
		try{
			lock.lock();
			count++;
			ret = count;
		} finally{
			lock.unlock();
		}
		return ret;
	}
	private void saveAsAFile(String page, String fileName, String url) throws ParserException, IOException
	{
		String title = BBCPageProcess.getTitle(page);
		String content = BBCPageProcess.getArticleContent(page);
		String time = BBCPageProcess.getTime(page);
		File file = new File("page/"+fileName+".txt");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(url);
		bw.newLine();
		bw.write(title);
		bw.newLine();
		bw.write(time);
		bw.newLine();
		bw.write(content);
		
		bw.close();
	}
	
	public void start() throws IOException, ParserException
	{
		String url = null; //待爬取的url
		ArrayList<String> newUrls = null; //从待爬取页面上获得的url
		String page = null; //html页面
		while(!urlUnDo.isEmpty())
		{
			if(count > 5000)
				break;
			url = urlUnDo.remove();
			if(isDoneUrl(url))
				continue;
			page = crawler.crawler(url);			
			newUrls = BBCPageProcess.getRelatedUrls(page);
			int countNumber = addCount(); //爬完一个url
			saveAsAFile(page, countNumber+"", url); //保存到一个文件中
			addUnDoUrl(newUrls);
			
			//把爬完的插入完成队列中
			urlDone.add(MD5Util.MD5(url));
			System.out.printf("第%d个url:%s%n", count, url);
		}
		System.out.println("我跑完啦");
	}
	public static void main(String[] args) throws ParserException, IOException
	{
//		BBCCrawler c = new BBCCrawler("http://www.bbc.co.uk/news/world/asia/china/");
//		c.start();
		
		Executor executor = Executors.newCachedThreadPool();
		for(int i=0; i<10; ++i)
		{
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						new BBCCrawler("http://www.bbc.co.uk/news/world/asia/china/").start();
					} catch (ParserException | IOException e) {
						// TODO Auto-generated catch block
						System.out.println("打赌3块钱西瓜");
						e.printStackTrace();
					}
				}
			});
		}
		
	}

}	
