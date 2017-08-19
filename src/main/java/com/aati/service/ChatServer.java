	package com.aati.service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import java.util.logging.Logger;

//import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chatroom")
public class ChatServer {
	
	private final Logger log = Logger.getLogger(getClass().getName());//para verificar el id que da a diferencia del id de session
	private static final Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
	
	@OnOpen
	public void onOpen(Session peer){
		log.info("ABRIENDO LA CONEXION RECIBIENDO: "+ peer);
		peers.add(peer);
		log.info("EL LOG ES: "+ getLog());
		
	}
	
	@OnClose
	public void onClose(Session peer){
		log.info("CERRANDO LA CONEXION CERRANDO CON: "+ peer);
		peers.remove(peer);
		
	}
	@OnMessage
	public void message(String message, Session session) throws IOException{
		log.info("EL MENSAJE QUE ESTA PASANDO POR ONMESSAGE ES: "+ message);
		for (Session peer : peers){
			log.info("UN PEER CONTIENE: "+ peer);
			peer.getBasicRemote().sendText(message);
		}
		
	}

	public Logger getLog() {
		return log;
	}
	
/*	@OnMessage
	public void message(String message, Session client) throws IOException, EncodeException{
		System.out.println("EL MENSAJE QUE ESTA PASANDO POR ONMESSAGE ES: "+ message);
		for (Session peer : peers){
			System.out.println("MENSAJE PARA ENVIAR: "+ message);
		peer.getBasicRemote().sendObject(message);
		}
	}*/
}




