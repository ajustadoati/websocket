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

import javax.ejb.Singleton;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import com.aati.model.Mensaje;
import com.aati.model.Response;
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
	private static IncomingChatMessageListener in= getIncoming();
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
  		    	Response response = getResponseByUserAndMessage(us, texto);
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
			String[] result = msj.getUsers().split("&&");
			//Se recorre la lista de usuarios
			//connect.getChatManager().addIncomingListener(getIncoming());
	        for(String user : result){
		        	log.info(">"+user+"<");
		            user=user+"@ajustadoati.com";
		            log.info("User: "+user);
		        	
		  			EntityBareJid jid = JidCreate.entityBareFrom(user);
		  			
		  			Chat chat = chatManager.chatWith(jid);
		  			
		  			Message ms= new Message();
		  			ms.setBody(msj.getCode()+"---"+msj.getMensaje()+"---"+msj.getLatitud()+"---"+msj.getLongitud());
		  			try {
						chat.send(ms);
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
	
	public static Response getResponseByUserAndMessage(String user, String message) {
		HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
		ClientConfig config = new DefaultClientConfig();
		SSLContext ctx=null;
		try {
			ctx = SSLContext.getInstance("SSL");
			ctx.init(null, myTrustManager, null);
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hostnameVerifier, ctx));
		Client client = Client.create(config);
  	//Client client = Client.create();
		System.out.println("realizando consulta a 9000************"+user);
		WebResource webResource = client
		   .resource("https://ajustadoati.com:9000/ajustadoati/usuario/"+user);

		ClientResponse resp = webResource.accept("application/json")
                 .get(ClientResponse.class);

		if (resp.getStatus() != 200) {
		   throw new RuntimeException("Failed : HTTP error code : "
			+ resp.getStatus());
		}else{
			System.out.println("The request has passed the test:");
		}

		String output = resp.getEntity(String.class);
		JSONObject obj=null;
		Response response=null;
		try {
			obj = new JSONObject(output);
			System.out.println("response:"+ obj);
			System.out.println("latitud: "+obj.getDouble("latitud"));
			response = new Response(user, message, obj.getDouble("latitud"), obj.getDouble("longitud"));
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	static {
        disableSslVerification();
    }
	
	
	@OnOpen
	public void open(final Session session) {
		 clientes.add(session);
		log.info("Abriendo Websocket"+session);

	}
	
	final static TrustManager[] myTrustManager = new TrustManager[]{new X509TrustManager() {
		
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// TODO Auto-generated method stub
			}
			
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// TODO Auto-generated method stub
			}
		}
	};

}
