package com.sap.sia.websocket;
 
import java.io.IOException;
 

import java.util.ArrayList;
import java.util.List;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

 
@ServerEndpoint("/websocket")
public class WebSocketHandler {
 
 private static List<Session> globalSession = new ArrayList<Session>();
  @OnMessage
  public void onMessage(String message, Session session)
    throws IOException, InterruptedException {
   
    // Print the client message for testing purposes
    //System.out.println("Received: " + message);
    globalSession.add(session);
    // Send the first message to the client
    session.getBasicRemote().sendText("This is the first server message");
   
    // Send 3 messages to the client every 5 seconds
    // Send a final message to the client
    
  }
   
  @OnOpen
  public void onOpen() {
    System.out.println("Client connected");
  }
 
  @OnClose
  public void onClose(Session session) throws IOException {
	  	if (session.isOpen()) {
	  		session.close();
	  	}
		globalSession.remove(session);
	    System.out.println("Connection closed");
  }
  
  public void sendMessage(String color,String quantity,String type) throws InterruptedException, IOException{
	   for(Session session:globalSession){
			   session.getBasicRemote().
			   sendText(quantity+','+color+','+type); 
	   }  
  }
  public void sendMessage(String material) throws InterruptedException, IOException{
	   for(Session session:globalSession){
			   session.getBasicRemote().
			   sendText(material); 
	   }  
 }
}