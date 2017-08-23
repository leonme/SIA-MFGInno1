package com.sap.sia.wechat.restws;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.sia.wechat.process.*;
import com.sap.sia.wechat.resource.InputSalesOrder;
import com.sap.sia.wechat.resource.SalesOrder;

/**
 * 
 * @author Siqi
 * 
 */

// @Path here defines class level path. Identifies the URI path that
// a resource class will serve requests for.
@Path("SIAService")
public class RESTfulService {
	
	private static Object lockObject = new Object();
	
	private int salesOrderID = -1;
	SalesOrder salesOrder = new SalesOrder();
	@POST
	@Path("/salesOrder")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public SalesOrder createOrder(String inputJSON) throws JSONException{
		System.out.println("post request received!");
		JSONObject jb = new JSONObject(inputJSON);
		String text_content = null;
		Boolean priority = null;
		int number = -1;
		try {
		    text_content = jb.getString("text_content");
		} catch (JSONException e) {
			e.printStackTrace();
//			salesOrderID = -2;
//			salesOrder.setSalesOrderID(salesOrderID);
//			return salesOrder;
			throw new WebApplicationException();
		}
		try {
		    priority = jb.getBoolean("priority");
		} catch (JSONException e) {
			e.printStackTrace();
//			salesOrderID = -3;
//			salesOrder.setSalesOrderID(salesOrderID);
//			return salesOrder;
			throw new WebApplicationException();
		}
		try {
		    number = jb.getInt("number");
		} catch (JSONException e) {
			e.printStackTrace();
//			salesOrderID = -4;
//			salesOrder.setSalesOrderID(salesOrderID);
//			return salesOrder;
//			throw new WebApplicationException(Response.status(500).entity("Parameter type wrong!").type(MediaType.TEXT_PLAIN).build());
			throw new WebApplicationException();
		}
		String input_priority = null;
		String input_number = Integer.toString(number);
		if(priority == true){
			input_priority = "003";
		}
		else{
			input_priority = "001"; 
		}
		System.out.println("priority:"+input_priority+" number:"+input_number);
		InputSalesOrder order = new InputSalesOrder(text_content, input_priority, input_number);
		Processor process = new Processor(order);
		String type = null; 
		try {
			type = String.valueOf(JCoDestinationManager.getDestination(Processor.DESTINATION_NAME1).getType());
		} catch (JCoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// to make request always send to ME to create order in ME.
		type = "1";
		if("1".equalsIgnoreCase(type)){
			if(priority == true){
				process.setPriority("700");
			}
			else{
				process.setPriority("300");
			}
			synchronized (lockObject) {
				salesOrderID = process.createSalesOrderByME();
			}
		}else{
			salesOrderID = process.createSAPOrder();
		}

		salesOrder.setSalesOrderID(salesOrderID);
		return salesOrder;
	}
	
	@GET
	@Path("/hello")
	public String restfulTest() {
		System.out.println("test request received!");
		String response = "Test successfully!";
		return response;
	}
	
	@POST
	@Path("/MaterailUpdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateMaterial(String inputJSON) throws JSONException{
		
		JSONObject json = new JSONObject(inputJSON);	
		try {
			Processor.updateMaterial(json);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@GET
	@Path("/getAllBricks")
	public String getAllBricks() {
		String path = "c:\\SIA\\Bricks.json";
		String sets = Processor.ReadFile(path);
		JSONArray ja = null;
		try {
			ja = new JSONArray("["+sets+"]");
			//System.out.println(ja.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ja.toString();
	}
};