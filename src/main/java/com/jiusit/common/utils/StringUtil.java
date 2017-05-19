package com.jiusit.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.mail.internet.MimeUtility;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用于对字符串的一些操作
 * @author Cjava-team
 * @createTime 2014-01-02下午09:48:06
 *
 */

public class StringUtil {
	private static final String HexCharSet = "0123456789ABCDEF";
	/**
	 * 判断是否为数字
	 * @param str
	 * @return
	 */
	public static boolean isNumer(String str){ 
		if(str==null || str.equals("")){
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]*"); 
		return pattern.matcher(str).matches(); 
	} 
	
	/**
	 * 字符串数组转字符串
     * @param str 
     * @param mobile
     * @return
     */
    public static String ArrayToString(String[] strs,String split) {
        String destId = "";
        for (String str : strs) {
            destId += " " + str;
        }
        
        destId = destId.trim();
        return destId.replaceAll(" ", split);
    }

    /**
	 * 字符串是否在数组里
     * @param str 
     * @param mobile
     * @return
     */
    public static boolean strInArray(String[] strs,String str) {
        for (String a : strs) {
            if(a.equals(str)){
            	return true;
            }
        }
        return false;
    }
    
    /**
	 * 字符串是否在集合里
     * @param str 
     * @param mobile
     * @return
     */
    public static boolean strInArray(ArrayList<String> strs,String str) {
        for (String a : strs) {
            if(a.equals(str)){
            	return true;
            }
        }
        return false;
    }
    
    public static boolean isEmpty(String str){
    	if(str==null || str.trim().equals("") || str.trim().equals("null")){
    		return true;
    	}else{
    		return false;
    	}
    }

    /**
     * 将文本域内容输出
     * @param text
     * @return
     */
    public static String textToHtml(String text){
    	if(!isEmpty(text)){
    	text = text.replaceAll("\n","<br/>").replaceAll(" ", "&nbsp;");
    	}
    	return text;
    }
    
    
    /**
     * 过滤字符串内的SQL关键字
     * @param text
     * @return
     */
    public static String filterSql(String text){
    	if(!isEmpty(text)){
    		text = Pattern.compile("insert |update |delete |select |creat |drop |truncate |or |grant |union |sysobjects |syslogins |sysremote |sysusers |sysxlogins |sysdatabases |exec \\*",Pattern.CASE_INSENSITIVE).matcher(text).replaceAll("SQL关键字");
    	}
    	return text;
    }
    
    /**
     * 将字符串按指定长度切割
     * 
     * @User: qinfz
     * @Date: 2012-12-13 上午03:21:16
     * @param str 要切割的字符串
     * @param length 要切割的长度，该长度是字符长度，一个汉字2个字符
     * @return
     */
    public static String subString(String str,int length){
    	if(isEmpty(str)){
    		return "";
    	}
    	StringBuffer sb=new StringBuffer();
    	int num=0;
    	for(char c:str.trim().toCharArray()){
    		byte[] b=String.valueOf(c).getBytes();
    		if(b.length==1){
    			num=num+1;
    		}else{
    			num=num+2;
    		}
    		sb.append(c);
    		if(num>=length){
    			return sb.append("...").toString();
    		}
    	}
    	return sb.toString();
    }
    
    public static String nullToString(Object o){
    	if(o==null){
    		return "";
    	}else{
    		return o.toString().trim();
    	}
    }
    /**
	 * 对象转字符
	 * 
	 * @return
	 */
	public static String object_to_string(Object parameter) {
		try {
			if (parameter == null) { return null; }
			StringBuffer str = new StringBuffer();
			final String split = "&";
			final String equal = "=";
			// 处理Map对象
			if (parameter instanceof Map) {
				Map p = (Map) parameter;
				Iterator iterator = p.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					Object obj = p.get(key);
					if (obj instanceof String || obj instanceof Integer) {
						str.append(split + key + equal + URLEncoder.encode(obj.toString(), "utf-8"));
					}
				}
			}
			else {
				// 处理bean对象
				Field[] fields = parameter.getClass().getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					field.setAccessible(true);
					Type type = field.getGenericType();
					if (type.equals(String.class) || type.equals(Integer.class)) {
						String name = field.getName();
						Object value = field.get(parameter);
						if (value != null && !value.toString().equals("")) {
							str.append(split + name + equal + URLEncoder.encode(URLEncoder.encode(value.toString(), "utf-8"), "utf-8"));
						}
					}
				}
			}
			String p = str.toString();
			if (!p.equals("")) {
				p = p.substring(1);
			}
			return p;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 是否有中文字符
	 * 
	 * @param s
	 * @return
	 */
	public static boolean hasCn(String s) {
		if (s == null) {
			return false;
		}
		return countCn(s) > s.length();
	}

	/**
	 * 获得字符,符合中文习惯。
	 * 
	 * @param s
	 * @param length
	 * @return
	 */
	public static String getCn(String s, int len) {
		if (s == null) {
			return s;
		}
		int sl = s.length();
		if (sl <= len) {
			return s;
		}
		// 留出一个位置用于…
		len -= 1;
		int maxCount = len * 2;
		int count = 0;
		int i = 0;
		while (count < maxCount && i < sl) {
			if (s.codePointAt(i) < 256) {
				count++;
			} else {
				count += 2;
			}
			i++;
		}
		if (count > maxCount) {
			i--;
		}
		return s.substring(0, i) + "…";
	}

	/**
	 * 计算GBK编码的字符串的字节数
	 * 
	 * @param s
	 * @return
	 */
	public static int countCn(String s) {
		if (s == null) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.codePointAt(i) < 256) {
				count++;
			} else {
				count += 2;
			}
		}
		return count;
	}

    /**
     * 匹配是否有非法字符(中文符号的)
     * @param text
     * @return
     */
    public static boolean ifSignStr(String text){
    	
    	boolean flag = true;
    	if(!"".equals(text) && text != null){
    	if(text.contains("＞")||text.contains("＜")||text.contains("‘")||text.contains("“")||text.contains("＆")
    			||text.contains("＼")||text.contains("＃")||text.contains("非")||text.contains("＋")||text.contains("％")
    			||text.contains("；")||text.contains("（")||text.contains("）")||text.contains("－")||text.contains("and")
    			||text.contains("select")||text.contains("update")||text.contains("delete")){
    		flag = false;
    	    }
        }else{
        	flag = true;
        }
    	return flag;
    }
    /**
	 * 获取工程根目录文件夹
	 * 
	 * @return
	 */
	public static String getFileRoot() {
		String t = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		t = t.replaceAll("/WEB-INF/classes/", "");
		return t;
	}
	
	/**
	 * 获取随机数
	 * @param size 几位
	 * @return
	 */
	public static String getRoundNum(int size){
		StringBuffer randomCode = new StringBuffer();
		// 创建一个随机数生成器类
		Random random = new Random();
		char[] codeText = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		// 随机产生codeCount数字的验证码。
		for (int i = 0; i < size; i++) {
			// 得到随机产生的验证码数字。
			String rand = String.valueOf(codeText[random.nextInt(codeText.length)]);

			// 将产生的四个随机数组合在一起。
			randomCode.append(rand);
		}
		return randomCode.toString();
//		return "111111";
	}
	
	 public static final String inputStream2String(InputStream in) throws UnsupportedEncodingException, IOException{
        if(in == null)
            return "";
        
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n, "UTF-8"));
        }
        return out.toString();
    }
	 
	public static final String getUUID(){
		UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
	}
	
	// 判断old_date是否超过当前时间3个月 90天
	public static final boolean greaterNowDate2OldDate90(Date old_date) throws ParseException{
        Date now_date = new Date(); 
        Calendar now = Calendar.getInstance();
        Calendar out = Calendar.getInstance();
        now.setTime(now_date);
        out.setTime(old_date);
        int now_month = now.get(Calendar.MONTH);
        int out_month = out.get(Calendar.MONTH);
        int now_day = now.get(Calendar.DAY_OF_MONTH);
        int out_day = out.get(Calendar.DAY_OF_MONTH);
        int subYear = now.get(Calendar.YEAR) - out.get(Calendar.YEAR);
        //判断月份是否超过3个月
    	int month = now_month - out_month;
        //subYear==0,说明是同一年
        if(subYear != 0){
        	month = month + 12;
        }
        
        //判断月份是否超过3个月
    	if(month > 3){
    		return true;
    	}else if(month == 3){
    		// 刚好等于3个月时  判断 时间是否超过
    		int day = out_day - now_day;
    		if(day > 0){
    			return false;
    		}else{
    			return true;
    		}
    	}else{
    		return false;
    	}
	}
	
	/**
     * 将时间戳转换为date
     * @param text
     * @return
	 * @throws ParseException 
     */
    public static Date long2Date(String text){
    	Date date = null;
    	try{
	    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	        Long time = new Long(text);
	        String d = format.format(time);
	        date = format.parse(d);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return date;
    }
    
    // 判断old_date距离当前多久
 	public static final int dayBetweenNow(String old_date) throws ParseException{
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        Date sdate = sdf.parse(old_date);  
        Date bdate = sdf.parse(sdf.format(new Date()));
        Calendar cal = Calendar.getInstance();    
        cal.setTime(sdate);    
        long time1 = cal.getTimeInMillis();                 
        cal.setTime(bdate);    
        long time2 = cal.getTimeInMillis();         
        long between_days=(time2-time1)/(1000*3600*24);  
        return Integer.parseInt(String.valueOf(between_days));   
 	}
 	
 	//获取当前时间的下一天
	public static String getNextDate(String date) throws ParseException{
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date d = (Date) sf.parse(date);
		Calendar calendar = new GregorianCalendar(); 
	    calendar.setTime(d);
	    
	    calendar.add(Calendar.DATE,1);//得到后一天
	    return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
	}
	
	public static String[] split(String string, String separator) {
		String[] result;
		if (isEmpty(string)) {
			result = new String[1];
			result[0] = "";
			return result;
		}
		result = new String[stringOccur(string, separator) + 1];
		int oldPos = 0, pos = 0, count = 0, len = separator.length();
		while (pos != -1) {
			pos = string.indexOf(separator, oldPos);
			if (pos != -1) {
				result[count++] = string.substring(oldPos, pos);
				pos += len;
				oldPos = pos;
			}
		}
		result[count] = string.substring(oldPos);
		return result;
	}
	
	public static String concat(String s, String... more) {
		StringBuilder buf = new StringBuilder(s);
		for (String t : more)
			buf.append(t);
		return buf.toString();
	}

	public static int stringOccur(String source, String dest) {
		if (isEmpty(source) || isEmpty(dest))
			return 0;
		int pos = 0, count = 0;
		while (pos != -1) {
			pos = source.indexOf(dest, pos);
			if (pos != -1) {
				pos++;
				count++;
			}
		}
		return count;
	}
	
	public static byte[] hexToByte(String s) {
		int i, j = s.length() / 2, k;
		char[] b = s.toCharArray();
		byte[] d = new byte[j];

		for (i = 0; i < j; i++) {
			k = i * 2;
			d[i] = (byte) (HexCharSet.indexOf(b[k]) << 4 | HexCharSet
					.indexOf(b[k + 1]));
		}
		return d;
	}
	
	public static String byteToHex(byte[] bs) {
		StringBuilder buf = new StringBuilder(bs.length * 2);
		String s;
		for (byte b : bs) {
			s = Integer.toHexString(b & 0XFF);
			if (s.length() == 1)
				buf.append('0');
			buf.append(s);
		}
		return buf.toString().toUpperCase();
	}
	
	public static String optString(String s) {
		if (s == null)
			return "";
		else
			return s;
	}
	
	public static boolean isEqual(String string1, String string2) {
		String s1, s2;

		if (string1 == null)
			s1 = "";
		else
			s1 = string1;
		if (string2 == null)
			s2 = "";
		else
			s2 = string2;
		return s1.equals(s2);
	}
	private static String innerReplace(String string, String oldString,
			String newString, boolean isAll) {
		if (string == null)
			return "";
		int index = string.indexOf(oldString);
		if (index == -1)
			return string;
		int start = 0, len = oldString.length();
		if (len == 0)
			return string;
		StringBuilder buffer = new StringBuilder(string.length() + len);
		do {
			buffer.append(string.substring(start, index));
			buffer.append(newString);
			start = index + len;
			if (!isAll)
				break;
			index = string.indexOf(oldString, start);
		} while (index != -1);
		buffer.append(string.substring(start));
		return buffer.toString();
	}

	public static String replace(String string, String oldString,
			String newString) {
		return innerReplace(string, oldString, newString, true);
	}
	
	/**
	 * 将json 数组转换为Map 对象
	 * 
	 * @param jsonString
	 * @return
	 */
	public static Map<String, Object> getMap(String jsonString) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonString);
			@SuppressWarnings("unchecked")
			Iterator<String> keyIter = jsonObject.keys();
			String key;
			Object value;
			Map<String, Object> valueMap = new HashMap<String, Object>();
			while (keyIter.hasNext()) {
				key = (String) keyIter.next();
				value = jsonObject.get(key);
				valueMap.put(key, value);
			}
			return valueMap;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
