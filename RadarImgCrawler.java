package ygy.test.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** 
 * @author 作者 yuanguoyan
 * @version 创建时间：2015-5-27 下午3:34:50 
 * 类说明 script:not([src])
 */
public class RadarImgCrawler {
	public static void main(String[] args) {
		RadarImgCrawler crawler = new RadarImgCrawler();
		crawler.execute();
//		while(true){
//			try {
//				Thread.sleep(5*60*1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	public void execute() {
		String station = "北京";
		String saveDir = "D:\\Radar\\"+station + File.separator;
		if (! new File(saveDir).exists()) {
			new File(saveDir).mkdirs();
		}
		
		Map<String, String> stations = getRadarStationURLs();
		String url= stations.get(station);
		String context = getResultByUrl(url);
		Document doc = Jsoup.parse(context);
		Elements es = doc.select("form[name=slideform]");
		es = es.select("select#slide");
		es = es.select("option[bigPic]");
		
		for (Element element : es) {
			String pic = element.attr("bigPic");
			savepic(saveDir, pic);
			
			
			
		}
		
		
	}
	
	
	
	
	public Map<String,String> getRadarStationURLs() {
		String url = "http://www.weather.com.cn";
		String context = getResultByUrl(url + "/radar/");
		if(context == null)return null;
		Document doc = Jsoup.parse(context);
		Elements maps = doc.select("map#Map");
		Elements areas = maps.select("area[title]");
		Map<String, String> stations = new HashMap<String, String>();
		for (Element element : areas) {
			String title = element.attr("title");
			String href = element.attr("href");
			stations.put(title, href);
			
		}
		return stations;
	}
	
	
	public void execute3() {
		String url = "http://www.weather.com.cn";
		String context = getResultByUrl(url + "/radar/");
		
		
		Document doc = Jsoup.parse(context);
		Elements lddzcz = doc.select("div.lddzcz");
		Elements zones = lddzcz.select("li[onclick]");
		for (Element element : zones) {
			String attr = element.attr("onclick");
			String[] params = getParams(attr);
			
			String zone = params[1];
			String abbr = params[3];
			
			System.out.println(zone + " : " + abbr);
			
			getRadarSations(lddzcz, abbr);
		}
		System.out.println("------------");
		
	}
	
	public Map<String , String> getRadarSations(Elements lddzcz, String zoneAbbr){
		Elements es = lddzcz.select("ul[data-key="+zoneAbbr+"]");
		es = es.select("li[onmousedown]");
		for (Element element : es) {
			String attr = element.attr("onmousedown");
			String[] params = getParams(attr);
			
			String name = params[1];
			String code  = params[2];
			
			System.out.println("  " + name + " : " + code);
		}
		return null;
	}
	
	
	/**
	 * getValue('txt11','华北','','HB');showAndHide('List11','hide');
	 * @param paramstr
	 * @return
	 */
	public String[] getParams(String paramstr){
		String regex = "getValue('";
		int m = paramstr.indexOf(regex) + regex.length();
		int n = paramstr.indexOf("');",m);
		String str = paramstr.substring(m, n);
		return str.split("','");
	}
	
	/**
	 * 保存图片
	 * 
	 * @param ImgURL
	 */
	public void savepic(String saveDir, String ImgURL) {
		if (ImgURL == null) return;
		
		String savePath = saveDir + ImgURL.substring(ImgURL.lastIndexOf("/")+1);
		if(new File(savePath).exists())return;
		
		System.out.println("图片地址："+ImgURL);
		HttpGet get = new HttpGet(ImgURL);
		InputStream is = null;
		OutputStream os = null;
		HttpEntity entity = null;
		try {
			HttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				System.out.println("保存图片>>>>.>>>>>>" + savePath);
				is = entity.getContent();
				os = new FileOutputStream(savePath);
				IOUtils.copy(is, os);
			}
		} catch (Exception e) {
			System.out.println("图片保存失败:"+e.getMessage());
		}finally{
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(is);
			try {
				EntityUtils.consume(entity);
			} catch (IOException e) {
				e.printStackTrace();
			}
			get.abort();
		}

	}
	
	public static void readScript(String script)throws Exception { 
		System.out.println(script);
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript"); 
		// 执行指定脚本
		engine.eval(script); 
		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		for (Iterator iterator = bindings.keySet().iterator(); 
				iterator.hasNext();) {
			String key = (String) iterator.next();
			
			Object value = bindings.get(key);     
			System.out.println( key + "===" +value);
		}
		
//		if(bindings.containsKey("image_domain")){
//			image_domain = (String) bindings.get("image_domain");
//		}
//		
//		if(bindings.containsKey("data")){
//			data = bindings.get("data");
//		}
		
	}
	
	public final static String findRegex(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(str);
		while (m.find()) {
			return m.group();
		}
		return null;
	}
	
	
	CloseableHttpClient client = HttpClientBuilder.create().build();
	private String encoding = "utf-8";
	String url = "http://10.10.31.30";
	
	
	
	public Element getNptElements(String menuname){
		String context = getResultByUrl(url + "//npt//");
		Document doc = Jsoup.parse(context);
		Elements es = doc.select("div#navMenu");
		es = es.select("a[href]");
		for (Element e : es) {
			String name = e.text();
			System.out.println("菜单名："+ name);
			if (name.equals(menuname)) {
				return e;
			}
		}
		return null;
	}
	/**
	 * 根据url获取页面
	 * 
	 * @param url
	 * @return
	 */
	public String getResultByUrl(String url) {
		System.out.println("打开网页" + url);
		HttpGet get = new HttpGet(url);
		HttpEntity entity = null;
		InputStream is = null;
		StringWriter sw = null;
		try {
			CloseableHttpResponse response = client.execute(get);
			entity = response.getEntity();
			if (entity != null) {
				is = entity.getContent();
				sw = new StringWriter();
				IOUtils.copy(is, sw, encoding);
				String res = sw.toString();
				return res;
			}
		} catch (Exception e) {
			System.out.println("网页打开出错:"+ e.getMessage());
			return null;
		} finally {
			IOUtils.closeQuietly(sw);
			IOUtils.closeQuietly(is);
			try {
				EntityUtils.consume(entity);
			} catch (IOException e) {
				e.printStackTrace();
			}
			get.abort();
		}
		return null;
	}
	
	

}
