package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jxl.Cell;
import jxl.CellFeatures;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 
 *  @fileName :   LogProcess.LogProcess.java
 *
 *	@version : 1.0
 *
 * 	@see { }
 *
 *	@author   :   fan
 *
 *	@since : JDK1.4
 *  
 *  Create date  : 2016年9月24日 下午5:11:06
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

public class LogProcess {
	  /**
	 * 该方法用于处理日志文件，从日志文件中提取出有用的信息
	 */
	
	private static List<String> datasetList = new ArrayList();
	public static void logProcess(String fileName){
		  File file = new File(fileName);
		  Scanner scan;
		      String excelFileName = "result/"+file.getName()+".xls";
			  ExcellUtil.createXLSFile(excelFileName);
			  int lineNumber = 0;
			  int colNumber = 0;
			  int posCount=0;
			  int negCount=0;
			  try {
				scan = new Scanner(file);
				while(scan.hasNextLine()){
					String line = scan.nextLine();
					
					if(line.startsWith("dataset:")){
						lineNumber++;
						String name = getDatasetName(line);
						datasetList.add(name);
						writeDatasetToExcell(excelFileName,name, lineNumber, colNumber);
					}
					else if(line.contains("Weighted Avg")){
						posCount++;
						if(posCount%2==0){
							String[] classifyInfo = removeSpace(line).split(" ");
							for(int k = 1 ; k < classifyInfo.length-1 ; k++){
								writeDatasetToExcell(excelFileName,classifyInfo[k+1], lineNumber, k);
							}
						}
					}else if(line.contains("Correctly Classified Instances")){
						negCount++;
						if(negCount%2==0){
							String filtered = removeSpace(line);
							StringBuffer strBuffer = new StringBuffer(filtered);
							strBuffer.deleteCharAt(strBuffer.lastIndexOf(" "));
							filtered = strBuffer.toString();
							writeDatasetToExcell(excelFileName,filtered.substring(filtered.lastIndexOf(" "), filtered.length()),lineNumber,7);
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  
	  }
	  
	  private static String getDatasetName(String str){
		  String name = null;
		  if(str!=null){
			  name=str.substring(str.lastIndexOf(":")+1, str.length());
		  }
		  return name;
	  }
	  
	  private static void writeDatasetToExcell(String fileName,String name,int row,int col){
	
//		  for(String data:datasetList){
//		  }
		try {
			Workbook workbook = Workbook.getWorkbook(new File(fileName));
			WritableWorkbook book=Workbook.createWorkbook(new File(fileName),workbook);
			
			WritableSheet sheet = book.getSheet(0); //获取工作表
			Label l = new Label(col,row,name);
			sheet.addCell(l);
			book.write();
			workbook.close();
			book.close();
		} catch (IOException | WriteException | BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}          
	  }
	  
	  private static String removeSpace(String str){
		  String reault = null;
		  StringBuffer strBuff = new StringBuffer(str);
		  boolean isSpace = false;
		  for(int i = 0 ; i < strBuff.length() ; i++){
			  if(strBuff.charAt(i)==' '){
				  if(isSpace){
					  strBuff.deleteCharAt(i);
					  i--;
				  }else{
					  isSpace = true;
				  }
			  }else{
				  isSpace = false;
			  }
		  }
		  return strBuff.toString();
	  }
}
