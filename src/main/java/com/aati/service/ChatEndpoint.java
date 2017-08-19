package com.aati.service;

/*
import com.google.gson.Gson;
import com.websockets.messages.ConnectionInfoMessage;
import com.websockets.messages.MessageInfoMessage;
import com.websockets.messages.StatusInfoMessage;
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
//import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
//import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.aati.model.Response;


@ServerEndpoint(value = "/sala/{users}")//, encoders = ChatMessageEncoder.class, decoders = ChatMessageDecoder.class
public class ChatEndpoint {
	private final Logger log = Logger.getLogger(getClass().getName());
	private Connection connection;
	public ChatEndpoint(){
		this.connection=new XMPPConnection("ajustadoati.sytes.net");
		
		try{
			log.info("conectando a server openfire");
			this.connection.connect();
			this.connection.login("admin@ajustadoati.sytes.net", "Dexter876");
			log.info("Conectado");
		}catch(XMPPException e){
			System.out.println("Error Delivering block");
		}
	}
 
	@OnOpen
	public void open(final Session session, @PathParam("users") final String users) {
		URL url;
		try {
			url = new URL("http://localhost:8080/RESTfulExample/json/product/post");
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
 
		String input = "{\"qty\":100,\"name\":\"iPad 4\"}";
 
		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes());
		os.flush();
 
		if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
			throw new RuntimeException("Failed : HTTP error code : "
				+ conn.getResponseCode());
		}
 
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
 
		String output;
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			System.out.println(output);
		}
 
		conn.disconnect();
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}catch (IOException e) {
			 
			e.printStackTrace();
	 
		 }
		System.out.println("ENTRANDO A OPEN POR EL ROOM: " + users);
		System.out.println("MOSTRANDO SESSION: " + session);
		log.info("session openend and bound to room: " + users);
		log.info("el log es"+ Logger.getLogger(getClass().getName()) );		
		session.getUserProperties().put("room", users);
		
		ChatManager chatmanager = this.connection.getChatManager();
		log.info("enviando mensaje");
		String[] result = users.split("&&");
		//Se recorre la lista de usuarios
		
        for(String user : result){
        	log.info(">"+user+"<");
            user=user+"@ajustadoati.sytes.net";
            log.info("User: "+user);
           
            Chat newChat = chatmanager.createChat(user, new MessageListener() {   			
    		    public void processMessage(Chat chat, Message message) {
    		    	log.info("Received message: " + message.getBody());
    		    	log.info("Received from: " + message.getFrom());
    		    	try {
						session.getBasicRemote().sendText(message.getBody());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
    		    	
    		    	
    		    }
    		});
    		try {
    		    newChat.sendMessage("Posada44");
    		}catch (XMPPException e) {
    			log.info("Error Delivering block");
    		}
    		
    		//connection.disconnect();
        }
		
	}
 
	@OnMessage
	public void message(String chatMessage, Session session) throws IOException{
		System.out.println("EL MENSAJE QUE ESTA PASANDO POR ONMESSAGE ES: "+ chatMessage);
		String room = (String) session.getUserProperties().get("room");
		log.info("room"+room);
		for (Session s : session.getOpenSessions()) {
			if (s.isOpen() && room.equals(s.getUserProperties().get("room"))) {
				s.getBasicRemote().sendText(chatMessage);
			}
		}
		/*	for (Session peer : peers){
			System.out.println("UN PEER CONTIENE: "+ peer);
			peer.getBasicRemote().sendText(message);
		}*/
		
	}
	@OnClose
	public void onClose(Session session){
		try {
			log.info("Cerrando websocket");
			this.connection.disconnect();
			session.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*@OnMessage
	public void onMessage(final Session session, final ChatMessage chatMessage) {
		System.out.println("RECIBIENDO MENSAJE SESSION Y MENSAJE" + session + chatMessage);
		String room = (String) session.getUserProperties().get("room");
		try {
			for (Session s : session.getOpenSessions()) {
				if (s.isOpen() && room.equals(s.getUserProperties().get("room"))) {
					s.getBasicRemote().sendObject(chatMessage);
				}
			}
		} catch (IOException | EncodeException e) {
			log.log(Level.WARNING, "onMessage failed", e);
		}
	}*/
}
