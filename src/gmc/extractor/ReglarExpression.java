/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gmc.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author SNA3
 */
public class ReglarExpression {

	/**
	 * 返回正则表达式匹配的全部内容
	 * @param regex 正则表达式
	 * @param source 源字符串
	 * @return 匹配后的字符串
	 */
    public static String Regular(String regex, String source) {
        return Regular(regex, source, 0);
    }
    
    /**
     * 对source内容进行正则匹配，并返回匹配后的字符串
     * @param regex 正则表达式
     * @param source 匹配内容
     * @param groupNum 匹配的组，第几个括号里面的内容。如果是0，则返回全部，如果是1则返回第一个圆括号
     * 					里面的内容 
     * @return 返回表达式匹配后的字符串
     */
    public static String Regular(String regex, String source, int groupNumber)
    {
    	String content = "";
        final List<String> list = new ArrayList<String>();
        final Pattern pa = Pattern.compile(regex);
        final Matcher ma = pa.matcher(source);
        while (ma.find()) {
            list.add(ma.group(groupNumber));
        }
        for (int i = 0; i < list.size(); i++) {
            content = content + list.get(i);
        }
        return content;
    }

    

    public static ArrayList<String> RegularArray(String regex, String source) {
        return RegularArray(regex, source, 0);
    }
    
    public static ArrayList<String> RegularArray(String regex, String source, int groupNumber)
    {
    	final ArrayList<String> list = new ArrayList<String>();
        final Pattern pa = Pattern.compile(regex);
        final Matcher ma = pa.matcher(source);
        while (ma.find()) {
            list.add(ma.group(groupNumber));
        }
        return list;
    }
}
