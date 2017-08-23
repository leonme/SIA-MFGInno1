package com.sap.sia.wechat.resource;
import org.codehaus.jackson.annotate.JsonProperty;

public class InputSalesOrder {
	@JsonProperty
	private String text_content;
	private String priority;
	private String number;
	
	public InputSalesOrder(String text_content, String priority, String number){
		this.text_content = text_content;
		this.priority = priority;
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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

}
