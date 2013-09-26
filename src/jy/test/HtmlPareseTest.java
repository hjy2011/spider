package jy.test;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.FileInputStream;

import java.io.File;

import java.net.HttpURLConnection;

import java.net.URL;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;

import org.htmlparser.Parser;

/**
 * 
 * @author www.baizeju.com
 */

public class HtmlPareseTest {

	private static String ENCODE = "utf-8";

	private static void message(String szMsg) {

		try {
			System.out.println(new String(szMsg.getBytes(ENCODE), System
					.getProperty("file.encoding")));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String openFile(String szFileName) {

		try {

			BufferedReader bis = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(szFileName)), ENCODE));

			String szContent = "";

			String szTemp;

			while ((szTemp = bis.readLine()) != null) {

				szContent += szTemp + "\n";

			}

			bis.close();

			return szContent;

		}

		catch (Exception e) {

			return "";

		}

	}

	public static void main(String[] args) {

		try {
			
			Parser parser = new Parser(
					(HttpURLConnection) (new URL(
							"http://blogs.wsj.com/chinarealtime/?mg=blogs-wsj&url=http%253A%252F%252Fblogs.wsj.com%252Fchinarealtime%252Fpage%252F1"))
							.openConnection());

//			NodeFilter filter1 = new TagNameFilter("span");
//			NodeFilter fileter2 = new HasAttributeFilter("class", "story-date");
//			NodeFilter andFilter = new AndFilter(filter1, fileter2);
//			NodeList nodes = parser.extractAllNodesThatMatch(andFilter);
//			System.out.println(nodes.size());
//			for(int i=0; i<nodes.size(); ++i)
//			{
//				Node textNode = nodes.elementAt(i);
//				message(textNode.toPlainTextString());
//			}
			System.out.println();
		}

		catch (Exception e) {

			System.out.println("Exception:" + e);

		}

	}

}