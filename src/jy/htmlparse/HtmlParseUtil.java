package jy.htmlparse;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class HtmlParseUtil {

	
	
	/**
	 * 获取规定标签及属性的内容
	 * 使用示例:
	 * HtmlParseUtil.getContentByTagNameAndAttribute(sourse, "div", "class", "hello");
	 * 会获取所有div节点，并且该节点具有属性class="hello"
	 * 
	 * @param sourse
	 * @param tagName
	 * @param attribute
	 * @param attributeValue
	 * @return
	 */
	public static List<String> getContentByTagNameAndAttribute(String sourse, String tagName, String attribute, String attributeValue)
	{
		List<String> list = new ArrayList<String>();
		Parser parser = null;
		NodeFilter tagNameFilter = new TagNameFilter(tagName);
		NodeFilter classNameFilter = new HasAttributeFilter(attribute, attributeValue);
		NodeFilter and = new AndFilter(tagNameFilter, classNameFilter);
		try {
			parser = new Parser(sourse);
			NodeList nodeList = parser.extractAllNodesThatMatch(and);
			for(int i=0; i<nodeList.size(); ++i)
			{
				String text = nodeList.elementAt(i).toHtml();
				list.add(text);
			}

		} catch (ParserException e) {
			e.printStackTrace();
		}
		return list;
		
	}

}
