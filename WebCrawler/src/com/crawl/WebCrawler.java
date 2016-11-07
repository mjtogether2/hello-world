package com.crawl;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.pojo.Book;

import utils.StreamUtil;


public class WebCrawler {
	public static void main(String[] args) {
		String[] types = {"互联网","编程","算法"};
		for (String string : types) {
			new WebCrawler().crawl(string);
		}
		
//		String text = "hasdks\ntitle=\"(浪潮之巅)\"\nsdsd ";
//		System.out.println(text);
//		System.out.println("*********************");
//		Pattern pattern = Pattern.compile("title=\"\\((.*?)\\)\"");
//		Matcher m = pattern.matcher(text);
//		while(m.find()){
//			String now_group = m.group(1);
//			System.out.println(now_group);
//		}
	}

	public  void  crawl(String type){
		try {
			//获取所有满足条件的书籍的url
			int bookNum = 0;
			int pageNow = 0;
			int pageCount = 20;
			List<Book> books = new ArrayList<Book>();
			String urlStr;
			String returnStr;
			Pattern pattern;
			Matcher m;
			while(true){
				urlStr = "https://book.douban.com/tag/"+URLEncoder.encode(type)+"?type=S&start="+pageNow*pageCount;
				returnStr = sendPost(urlStr);
				pattern = Pattern.compile("<li class=\"subject-item\">(.*?)</li>",Pattern.DOTALL);
				m = pattern.matcher(returnStr);
				while(m.find()&&bookNum<100){
					Book book = new Book();
					
					String now_group = m.group();
					//图书的评价人数
					Pattern evalPattern = Pattern.compile("<span class=\"pl\">\\s*\\(.*?(\\d*?)人评价\\)\\s*</span>",Pattern.DOTALL);
					Matcher evalMatcher = evalPattern.matcher(now_group);
					if(evalMatcher.find()){
						String evalNum = evalMatcher.group(1);
						if(Integer.parseInt(evalNum)<1000){
							continue;
						}
						
						book.setEvalNum(evalNum);
					}
					//图书的名字
					Pattern titlePattern = Pattern.compile("title=\"(.*)\"");
					Matcher titleMatcher = titlePattern.matcher(now_group);
					if(titleMatcher.find()){
						String title = titleMatcher.group(1);
						book.setName(title);
					}
					//图书的作者，出版社，出版时间，价格
					Pattern infoPattern = Pattern.compile("<div class=\"pub\">\\s*(.*?)\\s*</div>",Pattern.DOTALL);
					Matcher infoMatcher = infoPattern.matcher(now_group);
					if(infoMatcher.find()){
						String info = infoMatcher.group(1);
						String[] infos = info.split("/");
						int len = infos.length;
						book.setAuthor(infos[0]);
						book.setPrice(infos[len-1]);
						book.setPubDate(infos[len-2]);
						book.setPress(infos[len-3]);
					}
					
					//图书的评分
					Pattern scorePattern = Pattern.compile("<span class=\"rating_nums\">(.*?)</span>");
					Matcher scoreMatcher = scorePattern.matcher(now_group);
					if(scoreMatcher.find()){
						String score = scoreMatcher.group(1);
						book.setScore(score);
					}
					
					book.setId(bookNum+1);
					books.add(book);
					bookNum++;
				}
				if(bookNum>=100){
					break;
				}else{
					pageNow++; 
				}
			}
			
			toExcel(books);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送请求
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String sendPost(String urlStr) throws Exception {

		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setReadTimeout(0);
		connection.connect();
		int code = connection.getResponseCode();
		String result = null;
		InputStream inputStream=null;
		System.out.println(code);
		if (code == 200) {
			inputStream = connection.getInputStream();
			result = StreamUtil.StreamToString(inputStream);
			System.out.println(result);
		}
		if(inputStream!=null){
			inputStream.close();
		}
		connection.disconnect();
		return result;	
	}
	
	public void toExcel(List<Book> books){
		
		HSSFWorkbook wb = new HSSFWorkbook();   
		HSSFSheet sheet = wb.createSheet("书籍表一");  
		HSSFRow row = sheet.createRow((int) 0);
		HSSFCellStyle style = wb.createCellStyle();  
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		HSSFCell cell = row.createCell((short) 0);  
		cell.setCellValue("序号");  
		cell.setCellStyle(style);  
		cell = row.createCell((short) 1);  
		cell.setCellValue("书名");  
		cell.setCellStyle(style);  
		cell = row.createCell((short) 2);  
		cell.setCellValue("评分");  
		cell.setCellStyle(style);  
		cell = row.createCell((short) 3);  
		cell.setCellValue("评价人数");  
		cell.setCellStyle(style);  
		
		cell = row.createCell((short) 4);  
		cell.setCellValue("作者");  
		cell.setCellStyle(style);  
		cell = row.createCell((short) 5);  
		cell.setCellValue("出版社");  
		cell.setCellStyle(style);  
		cell = row.createCell((short) 6);  
		cell.setCellValue("出版日期");  
		cell.setCellStyle(style);  
		cell = row.createCell((short) 7);  
		cell.setCellValue("价格");  
		cell.setCellStyle(style);  
		

		for(int i=0;i<books.size();i++){
			 row = sheet.createRow((int) i + 1);  
			 Book book = (Book) books.get(i);  
			 row.createCell((short) 0).setCellValue(book.getId());  
			 row.createCell((short) 1).setCellValue(book.getName());  
			 row.createCell((short) 2).setCellValue(book.getScore());  
			 row.createCell((short) 3).setCellValue(book.getEvalNum());  
			 row.createCell((short) 4).setCellValue(book.getAuthor());  
			 row.createCell((short) 5).setCellValue(book.getPress());   
			 row.createCell((short) 6).setCellValue(book.getPubDate());  
			 row.createCell((short) 7).setCellValue(book.getPrice());  

		}
		
		 try  
		 {  
			 FileOutputStream fout = new FileOutputStream("E:/books.xls");  
		     wb.write(fout);  
		     fout.close();  
		 }catch (Exception e){  
			 e.printStackTrace();  
		 }  


		
	}
	public void writeStr(String s){
		try {
		FileOutputStream fileOutputStream = new FileOutputStream("E:/str.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
		bufferedWriter.write(s);
		
			bufferedWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
