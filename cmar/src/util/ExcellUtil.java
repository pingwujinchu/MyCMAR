package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 
 *  @fileName :   application.ExcelReader.java
 *
 *	@version : 1.0
 *
 * 	@see { }
 *
 *	@author   :   fan
 *
 *	@since : JDK1.4
 *  
 *  Create date  : 2016年7月26日 上午10:39:38
 *  Last modified time :
 *	
 * 	Test or not : No
 *	Check or not: No
 *
 * 	The modifier :
 *	The checker	: 
 *	 
 *  @describe :
 *  ALL RIGHTS RESERVED,COPYRIGHT(C) FCH LIMITED 2016
*/

public class ExcellUtil {
	 @SuppressWarnings("unchecked")
	static WritableWorkbook book;
	    public void createData(String path,String sheetname){
	        try {
	            String sourcefile = path;
	            InputStream is = new FileInputStream(sourcefile);
	            Workbook rwb = Workbook.getWorkbook(is);
	             
	            //get sheet
	            Sheet sheet = rwb.getSheet(sheetname);
	            //System.out.println(sheet.getName());
	             
	            //get rows 
	            if(sheet==null){
	            	
	            }
	            int cr =sheet.getRows();
	             
	            String header = "";
	            String preheader = "";
	            List<String> fieldsList = new ArrayList<String>();
	            List dataList = new ArrayList();
	            for(int i = 0;i<cr;i++){
	                Cell[] testcell  = sheet.getRow(i);
	                 
	                if(testcell.length == 0) continue;
	                if(!header.equals(preheader)){ 
	                    fieldsList.clear();
	                    preheader = header;
	                }
	                String tempString  = testcell[0].getContents();
	                int datatype = tempString.indexOf("&&");
	                if(datatype != -1 ) continue;
	 
	                System.out.println("第"+i+"行------");
	                //get cells of row
	                for (int j = 0; j < testcell.length; j++) {
	                    String str1 = testcell[j].getContents();
	                     
	                    if(str1 != null && !"".equals(str1)){
	                         
	                        int fields = str1.indexOf("#");
	                        int cheader = str1.indexOf("**");
//	                        int coment = str1.indexOf("$$");
	                        //int datatype = str1.indexOf("&&");
	                        int length = str1.length();
	                        //get table name
	                        if(cheader != -1){
	                            header = str1.substring(cheader+2, length);
	                        }else if(fields != -1){
	                            fieldsList.add(str1.substring(fields+1, length));
	                        }else{
	                            dataList.add(str1);
	                        }
	                    }
	 
	                }
	                if(!header.equals("") && fieldsList.size() != 0 && dataList.size() != 0 ){
	                    deleteData(header,dataList);
	                    System.out.println("成功删除");
	                    inserData(header,fieldsList,dataList);
	                    System.out.println("成功插入");
	                    dataList.clear();
	                }
	            }
	 
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	         
	    }
	     
	    public void deleteDataByid(String path,String sheetname){
	        try {
	            String sourcefile = path;
	            InputStream is = new FileInputStream(sourcefile);
	            Workbook rwb = Workbook.getWorkbook(is);
	             
	            //get sheet
	            Sheet sheet = rwb.getSheet(sheetname);
	            //System.out.println(sheet.getName());
	             
	            //get rows 
	            int cr =sheet.getRows();
	             
	            String header = "";
	            String preheader = "";
	            List<String> fieldsList = new ArrayList<String>();
	            List dataList = new ArrayList();
	            for(int i = 0;i<cr;i++){
	                Cell[] testcell  = sheet.getRow(i);
	                 
	                if(testcell.length == 0) continue;
	                if(!header.equals(preheader)){ 
	                    fieldsList.clear();
	                    preheader = header;
	                }
	                String tempString  = testcell[0].getContents();
	                int datatype = tempString.indexOf("&&");
	                if(datatype != -1 ) continue;
	 
	                System.out.println("第"+i+"行------");
	                //get cells of row
	                for (int j = 0; j < testcell.length; j++) {
	                    String str1 = testcell[j].getContents();
	                     
	                    if(str1 != null && !"".equals(str1)){
	                         
	                        int fields = str1.indexOf("#");
	                        int cheader = str1.indexOf("**");
//	                        int coment = str1.indexOf("$$");
	                        //int datatype = str1.indexOf("&&");
	                        int length = str1.length();
	                        //get table name
	                        if(cheader != -1){
	                            header = str1.substring(cheader+2, length);
	                        }else if(fields != -1){
	                            fieldsList.add(str1.substring(fields+1, length));
	                        }else{
	                            dataList.add(str1);
	                        }
	                    }
	 
	                }
	                if(!header.equals("") && fieldsList.size() != 0 && dataList.size() != 0 ){
	                    deleteData(header,dataList);
	                    System.out.println("成功删除");
//	                    inserData(header,fieldsList,dataList);
//	                    System.out.println("成功插入");
	                    dataList.clear();
	                }
	            }
	 
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	         
	    }
	     
	    public void deleteData(String header,List dataList){
	        try {
	            String sql = "delete from "+header + " where 1=1 and id = "+dataList.get(0);
	            excute(sql);
	             
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	 
	         
	    }
	     
	     
	    public int inserData(String header,List<String> fieldsList,List<String> dataList){
	        StringBuffer sql = new StringBuffer("insert into ");
	        sql.append(header+" (");
	        for(int i = 0; i<fieldsList.size();i++){
	            sql.append(fieldsList.get(i)+",");
	             
	        }
	        sql.delete(sql.length()-1, sql.length());
	        sql.append(") values(");
	         
	        for(int i = 0; i<dataList.size();i++){
	            sql.append("'"+dataList.get(i)+"',");
	        }
	         
	        sql.delete(sql.length()-1, sql.length());
	        sql.append(")");
	         
	        excute(sql.toString());
	         
	        return 0;
	    }
	     
	    private void excute(String sql){
	        //载入Oracle驱动程序
	        try {
	            //Class.forName("oracle.jdbc.OracleDriver").newInstance();
	            Class.forName("com.mysql.jdbc.Driver").newInstance();
	        } catch (InstantiationException ex) {
	            ex.printStackTrace();
	            System.out.println("载入MySQL数据库驱动时出错");
	        } catch (ClassNotFoundException ex) {
	            ex.printStackTrace();
	            System.out.println("载入MySQL数据库驱动时出错");
	        } catch (IllegalAccessException ex) {
	            ex.printStackTrace();
	            System.out.println("载入MySQL数据库驱动时出错");
	        }
	        /////////////////////////////////////////////////////////////////////////
	         
	        //连接到Oracle数据库
	        java.sql.Connection conn = null;
	        try{
	            //连接Oracle数据库
//	            conn = java.sql.DriverManager.getConnection(
//	                    "jdbc:oracle:thin:@192.168.1.2:1521:dbname", "username", "password");
	 
	          //连接Mysql库
	 
	 
	              conn = java.sql.DriverManager.getConnection(
	 
	                    "jdbc:mysql://localhost/cookbook", "root", "x5");
	        } catch (Exception ex){
	            ex.printStackTrace();
	            System.out.println("连接到MySQL数据库时出错！");
	            System.exit(0);
	        }
	        ////////////////////////////////////////////////////////////////////////
	         
	        //得到MySQL操作流
	        
	        try {
	            System.out.println("-----------------  "+sql);
	            java.sql.PreparedStatement stat = conn.prepareStatement(sql);
	            boolean rs = stat.execute();
	             
	        } catch(Exception ex) {
	            ex.printStackTrace();
	            System.exit(0);
	        }
	         
	        //关半程序所占用的资源
	        try{
	            conn.close();
	        }catch(Exception ex){
	            ex.printStackTrace();
	            System.out.println("关闭程序所占用的资源时出错");
	            System.exit(0);
	        }
	    }
	 
	     
	    public static List<List<String>> getData(String path,String sheetname){
	         
	        List<List<String>> dataList = new ArrayList();
	        try {
	            String sourcefile = path;
	            InputStream is = new FileInputStream(sourcefile);
	            Workbook rwb = Workbook.getWorkbook(is);
	             
	            //get sheet
	            Sheet sheet = rwb.getSheet(sheetname);
	            //System.out.println(sheet.getName());
	             
	            //get rows 
	            int cr =sheet.getRows();
	             
	            String header = "";
	            List<String> fieldsList = new ArrayList<String>();
	             
	            for(int i = 0;i<cr;i++){
	                Cell[] testcell  = sheet.getRow(i);
	                 
	                //get cells of row
	                List<String> row = new ArrayList();
	                for (int j = 0; j < testcell.length; j++) {
	                    String str1 = testcell[j].getContents();
	                    if(str1 != null && !"".equals(str1)){
	                        int fields = str1.indexOf("#");
	                        int cheader = str1.indexOf("**");
	                        int length = str1.length();
	                        //get table name
	                        if(cheader != -1){
	                            header = str1.substring(cheader+2, length);
	                        }else if(fields != -1){
	                            fieldsList.add(str1.substring(fields+1, length));
	                        }else{
	                            row.add(str1);
	                        }
	                    }
	                }
	                dataList.add(row);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return dataList;
	    }
	     
	    public void deleteData(String path,String sheetname){
	         
	        try {
	            String sourcefile = path;
	            InputStream is = new FileInputStream(sourcefile);
	            Workbook rwb = Workbook.getWorkbook(is);
	             
	            //get sheet
	            Sheet sheet = rwb.getSheet(sheetname);
	            //System.out.println(sheet.getName());
	             
	            //get rows 
	            int cr =sheet.getRows();
	             
	            for(int i = 0;i<cr;i++){
	                String header = "";
	                Cell[] testcell  = sheet.getRow(i);
	                 
	                //get cells of row
	                for (int j = 0; j < testcell.length; j++) {
	                    String str1 = testcell[j].getContents();
	                     
	                    if(str1 != null && !"".equals(str1)){
	                         
	                        int cheader = str1.indexOf("**");
	                        int length = str1.length();
	                        //get table name
	                        if(cheader != -1){
	                            header = str1.substring(cheader+2, length);
	                            String sql = "delete from "+header;
	                            excute(sql);
	                        }
	                    }
	 
	                }
	            }
	             
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }
	    
	 public static List<List<String>> getUser(){
		 String path = "excell/userInfo.xls";
		 String sheet = "Sheet1";
		 return getData(path, sheet);
	 }
	 
	 public static List<String> getCommit(){
		 String path = "excell/commit.xls";
		 String sheet = "Sheet1";
		 List<List<String>> data = null;
		 List<String> commit = new ArrayList();
		 data = getData(path, sheet);
		 for(int i = 0 ; i < data.size() ; i++){
			 if(!data.get(i).isEmpty()){
				 commit.add(data.get(i).get(0));
			 }
		 }
		 return commit;
	 }
	 
	public static String getRandomCommit(){
		String commit;
		List<String> commitList = getCommit();
		int index=  (int)(Math.random()*(commitList.size()-1));
		index = index%(commitList.size()-1)+1;
		return commitList.get(index);
	}
	 public static void main(String []args){
		 ExcellUtil er = new ExcellUtil();
		 List<List<String>> getdata = new ArrayList();
		 String path = "excell/userInfo.xls";
		 String sheet = "Sheet1";
		 getdata = er.getData(path, sheet);
		 Iterator it = getdata.iterator();
		 while(it.hasNext()){
			 List row = (List) it.next();
			 System.out.println(row.get(1));
		 }
		 System.out.println(er.getRandomCommit());
	 }
	 
	 public static void createXLSFile(String fileName){
		 try {
			book=Workbook.createWorkbook(new File(fileName));
			WritableSheet sheet=book.createSheet("log",0);
			Label[]label = new Label[8];
			label[0]=new Label(0,0,"dataset");
			label[1]=new Label(1,0,"TPR");
			label[2]=new Label(2,0,"FPR");
			label[3]=new Label(3,0,"Precision");
			label[4]=new Label(4,0,"Recall");
			label[5]=new Label(5,0,"F-Measure");
			label[6]=new Label(6,0,"ROC Area");
			label[7]=new Label(7,0,"ACC");
			for(int i=0 ; i<8 ; i++){
				sheet.addCell(label[i]);
			}

			book.write();
			book.close();
		} catch (IOException | WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
}
