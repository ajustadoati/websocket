package com.aati.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import com.aati.model.Dispositivo;
import com.aati.model.Mensaje;
import com.aati.model.Notificacion;
import com.aati.model.Response;
import com.aati.util.Util;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;


@ServerEndpoint(value="/openfire")
public class OFEndpoint {
	private final Logger log = Logger.getLogger(getClass().getName());
	
	private static ConnectAdmin connect= ConnectAdmin.getInstance();
	//controla la lista de peticiones
	private static List<Mensaje> mensajes= new ArrayList<Mensaje>();
	private static Set<Session> clientes = 
		    Collections.synchronizedSet(new HashSet<Session>());
	private static org.jivesoftware.smack.chat2.ChatManager chatManager;
	
	static{
		connect.getChatManager().addIncomingListener(getIncoming());
		chatManager=connect.getChatManager();
	}
	
	public static IncomingChatMessageListener getIncoming(){
		return new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid from, Message message, org.jivesoftware.smack.chat2.Chat chat) {
				
  			    System.out.println("New message from " + from + ": " + message.getBody());
  			    System.out.println("Received message from proveedor: " + message.getBody());
  			    String codearr[]=message.getBody().split("---");
  			    String code="";
	  		    String texto="";
  			    if(codearr.length>1){
  			    	 code=codearr[0];
		  			 texto=codearr[1];
  			    }
  			   
  		    	String arr[]=from.toString().split("@");
  		    	String us=arr[0];
  		    	Response response = Util.getResponseByUserAndMessage(us, texto);
  		    	System.out.println("Received from: " + response.getLatitud());
  		    	Gson gson = new Gson();
  		         String json = gson.toJson(response); 
  		    	try {
						
						synchronized(clientes){
						      // Iterate over the connected sessions
						      // and broadcast the received message
						      for(Session cliente : clientes){
						        if (cliente.equals(getSessionByCode(code))){
						          cliente.getBasicRemote().sendText(json);
						        }
						      }
						    }
						
  		    			//session.getBasicRemote().sendText(json);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	  			}
			
		};
	}
	
	
	
	@OnMessage
	public void message(String mensaje, final Session session) throws IOException{
		log.info("recibiendo mensaje desde cliente: "+ session);
	
		log.info("enviando mensaje a proveedores");
		try{
			JSONObject obj = new JSONObject(mensaje);
			Mensaje msj= new Mensaje();
			msj.setMensaje(obj.getString("mensaje"));
			msj.setUsers(obj.getString("users"));	
			msj.setLatitud(obj.getString("latitud"));
			msj.setLongitud(obj.getString("longitud"));
			msj.setCode(String.valueOf(new Date().getTime()));
			msj.setSession(session);
			mensajes.add(msj);
			List<Dispositivo> dispositivos= Util.getDevicesByListUsers(msj.getUsers());
			String[] result = msj.getUsers().split("&&");
			//Se recorre la lista de usuarios
			//connect.getChatManager().addIncomingListener(getIncoming());
	        for(String user : result){
		        	log.info(">"+user+"<");
		        	List<String> listDevicesForUser=Util.getDevicesForUser(dispositivos, user);
		            user=user+"@ajustadoati.com";
		            log.info("User: "+user);
		        	
		  			EntityBareJid jid = JidCreate.entityBareFrom(user);
		  			
		  			Chat chat = chatManager.chatWith(jid);
		  			
		  			Message ms= new Message();
		  			ms.setBody(msj.getCode()+"---"+msj.getMensaje()+"---"+msj.getLatitud()+"---"+msj.getLongitud());
		  			try {
						chat.send(ms);
						
						if(listDevicesForUser!=null && listDevicesForUser.size()>0){
							Util.sendNotifications(listDevicesForUser, "Hola, Han realizado una solicitud: "+msj.getMensaje());
						}
						
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
		    	
		        }
		} catch (JSONException e) {
            e.printStackTrace();
        }
	
		
	}
	
	@OnClose
	public void onClose(Session session){
		try {
			
			log.info("Cerrando websocket actual session");
			//this.connection.disconnect();
			mensajes.remove(getMensajeBySession(session));
			session.close();
			clientes.remove(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	public static Session getSessionByCode(String code){
		System.out.println("searching session");
		for ( Mensaje mensaje : mensajes) {
			if(mensaje.getCode().equals(code)){
				System.out.println("session"+mensaje.getSession());
				return mensaje.getSession();
			}
		}
		return null;
	}
	
	public Mensaje getMensajeBySession(Session session){
		log.info("buscando mensaje");
		for ( Mensaje mensaje : mensajes) {
			if(mensaje.getSession().equals(session)){
				log.info("session"+mensaje.getSession());
				return mensaje;
			}
		}
		return null;
	}
	
	
	
	
	static {
        disableSslVerification();
    }
	
	
	@OnOpen
	public void open(final Session session) {
		 clientes.add(session);
		log.info("Abriendo Websocket"+session);

	}
	
	

	private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
