package com.sap.sia.wechat.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.sia.object.Brick;
import com.sap.sia.websocket.WebSocketHandler;
import com.sap.sia.wechat.resource.InputSalesOrder;
import com.sun.jersey.json.impl.reader.Jackson2StaxReader;
import com.sun.jersey.server.impl.model.parameter.multivalued.StringReaderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;

public class Processor {
	
	private String text_content;
	private String priority = "3";
	private String number = "1";
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getText_content() {
		return text_content;
	}
	public void setText_content(String text_content) {
		this.text_content = text_content;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public static final String DESTINATION_NAME1 = "ABAP_AS_WITHOUT_POOL";
	static {
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "eccids01.dhcp.pvgl.sap.corp");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "800"); 
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "Proen1");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "Industry40");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");
		createDestinationDataFile(DESTINATION_NAME1, connectProperties);
	}
	public Processor(InputSalesOrder order){
		this.text_content = order.getText_content();
		this.priority = order.getPriority();
		this.number = order.getNumber();
	}
	public Processor() {
		
	}
	static void createDestinationDataFile(String destinationName, Properties connectProperties) {
		File destCfg = new File(destinationName+".jcoDestination");
		if(!destCfg.exists()){
			System.out.println("config file does not exist,new file will be created.");
			try {
				FileOutputStream fos = new FileOutputStream(destCfg, false);
				connectProperties.store(fos, "for tests only !");
			 	fos.close();
			} catch (Exception e) {
				throw new RuntimeException("Unable to create the destination files", e);
			}	
		}
		else {
			System.out.println("config file has exsited.");
			try {
				System.out.println("ASHOST:" + JCoDestinationManager.getDestination(destinationName).getApplicationServerHost());
				JCoDestinationManager.getDestination(destinationName).getType();
			} catch (JCoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public int createSAPOrder() {
		JCoFunction function;
		JCoFunction function_commit;
		JCoDestination destination;
		try {
			destination = JCoDestinationManager
					.getDestination(DESTINATION_NAME1);
			function = destination.getRepository().getFunction(
					"BAPI_SALESORDER_CREATEFROMDAT2");
			function_commit = destination.getRepository().getFunction(
					"BAPI_TRANSACTION_COMMIT");
		} catch (JCoException e1) {
			e1.printStackTrace();
//			return -5;
			throw new RuntimeException("Unable to create connection to ERP", e1);
		}

		if (function == null){
			throw new RuntimeException(
					"BAPI_SALESORDER_CREATEFROMDAT2 not found in SAP.");
		}

		// importing parameter
		JCoStructure salehd = function.getImportParameterList().getStructure(
				"ORDER_HEADER_IN");
		JCoStructure salehdx = function.getImportParameterList().getStructure(
				"ORDER_HEADER_INX");
		// Tables
		JCoTable returntable = function.getTableParameterList().getTable(
				"RETURN");
		JCoTable item = function.getTableParameterList().getTable(
				"ORDER_ITEMS_IN");
		JCoTable itemx = function.getTableParameterList().getTable(
				"ORDER_ITEMS_INX");
		JCoTable partner = function.getTableParameterList().getTable(
				"ORDER_PARTNERS");
		JCoTable schedule = function.getTableParameterList().getTable(
				"ORDER_SCHEDULES_IN");
		JCoTable schedulex = function.getTableParameterList().getTable(
				"ORDER_SCHEDULES_INX");
		JCoTable text = function.getTableParameterList().getTable(
			    "ORDER_TEXT");

		salehd.setValue("DOC_TYPE", "TA"); // 订单类型
		salehd.setValue("SALES_ORG", "2800"); // 销售机构
		salehd.setValue("DISTR_CHAN", "10"); // 分销渠道
		salehd.setValue("DIVISION", "00"); // 产品组
		salehd.setValue("PURCH_NO_C", "SIA_LEGO_01"); // 采购订单编号
		salehd.setValue("PRICE_DATE", "20160815");// 定价日期
		salehd.setValue("PMNTTRMS", "0001"); // 付款条款

		salehdx.setValue("DOC_TYPE", "X"); // 订单类型
		salehdx.setValue("SALES_ORG", "X"); // 销售机构
		salehdx.setValue("DISTR_CHAN", "X"); // 分销渠道
		salehdx.setValue("DIVISION", "X"); // 产品组
		salehdx.setValue("PURCH_NO_C", "X"); // 采购订单编号
		salehdx.setValue("PRICE_DATE", "X");// 定价日期
		salehdx.setValue("PMNTTRMS", "X"); // 付款条款

		item.appendRow();
		item.setValue("ITM_NUMBER", "000010"); // 项目
		item.setValue("MATERIAL", "SIA_LEGO"); // 物料编号
		item.setValue("DLV_PRIO", getPriority());

		itemx.appendRow();
		itemx.setValue("ITM_NUMBER", "000010"); // 项目
		itemx.setValue("MATERIAL", "X"); // 物料编号

		schedule.appendRow();
		schedule.setValue("ITM_NUMBER", "000010");// 项目
		schedule.setValue("SCHED_LINE", "0001");// 计划行
		schedule.setValue("REQ_QTY", getNumber());// 数量

		schedulex.appendRow();
		schedulex.setValue("ITM_NUMBER", "000010");// 项目
		schedulex.setValue("SCHED_LINE", "0001");// 计划行
		schedulex.setValue("REQ_QTY", "X");// 数量

		partner.appendRow();
		partner.setValue("PARTN_ROLE", "AG"); // 售达方
		partner.setValue("PARTN_NUMB", "0000301340"); // 售达方编号
		partner.appendRow();
		partner.setValue("PARTN_ROLE", "WE"); // 运达方
		partner.setValue("PARTN_NUMB", "0000301340"); // 运达方编号
		
		ArrayList<String> divList = getDivLines(getText_content(), 132);
		for(int i = 0; i < divList.size(); i++){
			text.appendRow();
			text.setValue("ITM_NUMBER","000010");// 项目
			text.setValue("TEXT_ID","0006");//Productoin Memo
			text.setValue("LANGU_ISO","ZH");//文本语言
			text.setValue("TEXT_LINE",divList.get(i));//文本内容
		}

		try {
			JCoContext.begin(destination);
			// Execute both RFC functions here
			function.execute(destination);
			function_commit.execute(destination);
			JCoContext.end(destination);
		} catch (AbapException e) {
			System.out.println(e.toString());
//			return -6;
			throw new RuntimeException("Execution error from ABAP!",e);
		} catch (JCoException e) {
			e.printStackTrace();
//			return -6;
			throw new RuntimeException("Excution error from JCO!",e);
		}

		String saledocu = function.getExportParameterList().getString("SALESDOCUMENT");
		if(null == saledocu || "".equals(saledocu)){
//			return -7;
			throw new RuntimeException("No sales order number was returned!");
		}
		System.out.println("Order created successfully!");
		System.out.println("SalesOrder number:" + saledocu);
//		System.out.println("返回信息数:" + returntable.getNumRows());
//		for (int j = 0; j < returntable.getNumRows(); j++) {
//			for (int i = 0; i < returntable.getMetaData().getFieldCount(); i++) {
//				System.out.println(returntable.getMetaData().getName(i) + ":/t"
//						+ returntable.getString(i));
//			}
//			returntable.nextRow();
//			System.out.println();
//		}
		
		return Integer.parseInt(saledocu);
	}
	
	public int createSalesOrderByME(){
		String priority = getPriority();
		String qty = getNumber();
		String customBom = getText_content();
		
		String url = "http://siamessys:50200/XMII/Illuminator"+"?QueryTemplate=CIIF/CreateShopOrderFn"+
					"&param.1="+priority
					+"&param.2="+customBom
					+"&param.3="+qty
					+"&Content-Type=text/json";
//		HttpClient client = HttpClientBuilder.create().build();
//		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//		credentialsProvider.setCredentials(AuthScope.ANY,
//											new UsernamePasswordCredentials("proen1", "Industry40"));
//		CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();
//		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(url);
		get.setHeader("Authorization", "Basic cHJvZW4xOkluZHVzdHJ5NDA=");
		CloseableHttpResponse response;
		int salesOrderID = 0;
		try {
			response = client.execute(get);
			System.out.println("status: " + response.getStatusLine());
			String responseStr = EntityUtils.toString(response.getEntity());
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory factory = mapper.getJsonFactory();
			JsonParser jp = factory.createJsonParser(responseStr);
			JsonNode node = mapper.readTree(jp);
			salesOrderID = node.findValue("outSalesOrder").asInt();
			System.out.println("Order created successfully! outSalesOrderId: " + salesOrderID);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return salesOrderID;
	}
	  public static void updateMaterial(JSONObject inputText) throws JSONException, InterruptedException, IOException{
			String material = inputText.getString("material");
			WebSocketHandler wt = new WebSocketHandler();
			if(material.equalsIgnoreCase("All")) {
				wt.sendMessage(material);
				updateJsonFile();
			}else{
				String color = material.substring(0,1);
				String qty = material.substring(1,2);
				String type = material.substring(2,4);
				Brick brick = new Brick();
				brick.setColor(color);
				brick.setType(type);
				brick.setQuantity(qty);
				wt.sendMessage(color,qty,type);
				updateJsonFile(brick);
			}
		}

	
	public static ArrayList<String> getDivLines(String inputString, int fixedLength){
		ArrayList<String> divList  = new ArrayList<String>();
		if(inputString.length() > fixedLength){
			System.out.println("length > 132 content: "+inputString);
			int reminder = (inputString.length())%fixedLength;
			int number = (inputString.length())/fixedLength;
			for(int i = 0; i < number; i++){
				String childStr = inputString.substring(i*fixedLength,(i+1)*fixedLength);
				System.out.println("childStr: " + childStr);;
				divList.add(childStr);
			}
			if(reminder > 0){
				String reminderStr = inputString.substring(number*fixedLength,inputString.length());
				System.out.println("childStr:" + reminderStr);
				divList.add(reminderStr);
			}
		}
		else{
			divList.add(inputString);
			System.out.println("length <= 132 content: "+inputString);
		}
		return divList;
	}
	
	public static String ReadFile(String path) {
		String laststr = "";
		File file = new File(path);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
			// 显示行号
			//System.out.println("line " + line + ": " + tempString);
			laststr = laststr + tempString;
			line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		  if (reader != null) {
			try {
				reader.close();
			} catch (IOException e1) {
			}
		}
		}
		return laststr;
	}
	
	public static void updateJsonFile() throws IOException{
		String fileURI = "C:\\SIA\\Bricks.json";
		System.out.println(fileURI);  
		String sets = ReadFile(fileURI);// 读取本地json
		try {
		//	JSONObject dataJson = new JSONObject(sets);
			JSONArray ja = new JSONArray("["+sets+"]");
			JSONArray newJa = new JSONArray();
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo = ja.getJSONObject(i);
				if(!jo.getString("type").equalsIgnoreCase("11")){
					Brick brick = new Brick();
					brick.setColor(jo.getString("color"));
					brick.setType(jo.getString("type"));
					brick.setQuantity("4");
					//System.out.println("before:"+jo.get("type")+" "+jo.getString("quantity"));
					newJa.put(new JSONObject(brick));
					//System.out.println("after:"+jo.get("type")+" "+jo.getString("quantity"));
					
				}else{
					Brick brick = new Brick();
					brick.setColor(jo.getString("color"));
					brick.setType(jo.getString("type"));
					brick.setQuantity(jo.getString("quantity"));
					newJa.put(new JSONObject(brick));
				}
				
			}
			
			FileWriter fw = null;
			FileWriter writer = null;
			try {
				fw = new FileWriter(fileURI);
				writer = new FileWriter(fileURI, true);// 重新写入json
				fw.write("");
				for (int i = 0; i < newJa.length(); i++) {
					writer.write(newJa.get(i).toString() + ",\r\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				fw.close();
				writer.close();
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void updateJsonFile(Brick brick) throws IOException {
		
		String fileURI = "C:\\SIA\\Bricks.json";
		System.out.println(fileURI);  
		String sets = ReadFile(fileURI);// 读取本地json
		try {
		//	JSONObject dataJson = new JSONObject(sets);
			JSONArray ja = new JSONArray("["+sets+"]");
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo = ja.getJSONObject(i);
				if(jo.get("type").equals(brick.getType())&&jo.get("color").equals(brick.getColor())) {
					JSONObject brickJso = new JSONObject(brick);
					ja.remove(i);// 如果token无效，则删除该记录，
					i=i-1;
					ja.put(brickJso);
					break;
				}
				//System.out.println(jo.get("type"));
			}
			
			FileWriter fw = null;
			FileWriter writer = null;
			try {
				fw = new FileWriter(fileURI);
				writer = new FileWriter(fileURI, true);// 重新写入json
				fw.write("");
				for (int i = 0; i < ja.length(); i++) {
					writer.write(ja.get(i).toString() + ",\r\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				fw.close();
				writer.close();
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
}
