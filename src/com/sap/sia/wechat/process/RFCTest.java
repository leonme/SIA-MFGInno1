package com.sap.sia.wechat.process;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Retention;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoRecord;
import com.sap.conn.jco.JCoRecordFieldIterator;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.mw.jco.*;
import com.sap.mw.jco.JCO.Field;
import com.sap.mw.jco.JCO.Table;


public class RFCTest {
    static String DST = "DST";

    static {
        Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "vsECD607.pro.coil");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "710"); 
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "proen");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "Industry40");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");
        createDestinationDataFile(DST, connectProperties);
    }


    static void createDestinationDataFile(String destinationName, Properties connectProperties) {
        File destCfg = new File(destinationName + ".jcoDestination");
        try {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectProperties.store(fos, "for tests only !");
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create the destination files", e);
        }
    }
    

    public static String getPOBySO(String SONumber) throws JCoException {
        JCoDestination destination;
        JCoRepository sapRepository;

        destination = JCoDestinationManager.getDestination(DST);
        JCoDestinationManager.getDestination(DST);
//        System.out.println("Attributes:");
//        System.out.println(destination.getAttributes());
//        System.out.println();
        String PONumber = null;
        
        try {
            JCoContext.begin(destination);
            sapRepository = destination.getRepository();
            
            if (sapRepository == null) {
                System.out.println("Couldn't get repository!");
                System.exit(0);
            } 
            
            JCoFunctionTemplate template2 = sapRepository.getFunctionTemplate("RFC_READ_TABLE");
            System.out.println("Getting template");
            JCoFunction function2 = template2.getFunction();
            function2.getImportParameterList().setValue("QUERY_TABLE", "AFPO");
            function2.getImportParameterList().setValue("DELIMITER", ",");
            function2.getImportParameterList().setValue("ROWSKIPS", Integer.valueOf(0));
            function2.getImportParameterList().setValue("ROWCOUNT", Integer.valueOf(0));
            
            System.out.println("Setting FIELDS");
            JCoTable returnFields = function2.getTableParameterList().getTable("FIELDS");
            returnFields.appendRow();
            returnFields.setValue("FIELDNAME", "AUFNR");
            returnFields.appendRow();
            returnFields.setValue("FIELDNAME", "KDAUF");
            
            function2.execute(destination);
            
            JCoTable jcoTablef = function2.getTableParameterList().getTable("FIELDS");
            JCoTable jcoTabled = function2.getTableParameterList().getTable("DATA");
            int icodeOffSet = 0;
            int icodeLength = 0;

            int numRows = jcoTabled.getNumRows();
//            System.out.println("Field :" +jcoTablef.toString());
            System.out.println("numRows = " + numRows);
        	HashMap<String, String> orderMap = new HashMap<String, String>();
        	for(int i = 0;i < numRows;i++){
        		jcoTabled.setRow(i);
        		String recordStr[] = jcoTabled.getString("WA").split("\\,");
        		if(recordStr.length > 1){
        			orderMap.put(recordStr[1], recordStr[0]);
        		}
        	}
//        	System.out.println(orderMap);
        	PONumber = orderMap.get(SONumber);
//        	return PONumber;
        }
          catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());   
        } finally {
            JCoContext.end(destination);
        }
		return PONumber;
    }

    public static void getPOStatus(String PONumber) throws JCoException{
        JCoDestination destination;
        JCoRepository sapRepository;

        destination = JCoDestinationManager.getDestination(DST);
        JCoDestinationManager.getDestination(DST);
        
        try {
            JCoContext.begin(destination);
            sapRepository = destination.getRepository();
            
            if (sapRepository == null) {
                System.out.println("Couldn't get repository!");
                System.exit(0);
            } 
            
            JCoFunctionTemplate template2 = sapRepository.getFunctionTemplate("BAPI_PRODORD_GET_DETAIL");
            JCoFunction function2 = template2.getFunction();
            function2.getImportParameterList().setValue("NUMBER", PONumber);
            JCoStructure order_objects = function2.getImportParameterList().getStructure("ORDER_OBJECTS");
            order_objects.setValue("HEADER", "1");
            
            function2.execute(destination);
            
            JCoTable returned_table = function2.getTableParameterList().getTable("HEADER");
            System.out.println(returned_table.getString("SYSTEM_STATUS"));
            
        }
        catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());   
        } finally {
            JCoContext.end(destination);
        }
    }
    public static void main(String[] args) {
        String salesOrderNum = "0000007300";
        String productionOrderNum = null;
    	try {
            productionOrderNum = getPOBySO("0000007300");
            System.out.println("salesOrder Number:"+salesOrderNum);
            System.out.println("producitonOrderNumber:"+productionOrderNum);
            getPOStatus(productionOrderNum);
        } catch (JCoException jce) {
            System.out.println("Exception > " + jce);
        }
    }
}
