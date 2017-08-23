package com.sap.sia.wechat.resource;

import javax.ws.rs.Consumes;
import org.codehaus.jackson.annotate.JsonProperty;

@Consumes("application/json")

public class SalesOrder {

	@JsonProperty
    int salesOrderID;
	public int getSalesOrderID() {
		return salesOrderID;
	}
	public void setSalesOrderID(int slaesOrderID) {
		this.salesOrderID = slaesOrderID;
	}
	
	@Override
	public String toString () {
		return "Track [salesOrderID=" + salesOrderID + "]";
	}
}
