package com.sap.sia.wechat.resource;

import javax.ws.rs.Consumes;
import org.codehaus.jackson.annotate.JsonProperty;

@Consumes("application/json")
public class Message {
	@JsonProperty
	String message;

	public Message(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
