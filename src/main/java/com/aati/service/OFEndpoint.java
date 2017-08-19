package com.aati.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

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
	private Connection connection;
	public OFEndpoint(){
		this.connection=new XMPPConnection("ajustadoati.com");
		
		try{
			log.info("conectando a server openfire");
			this.connection.connect();
			this.connection.login("admin@ajustadoati.com", "Dexter876");
			log.info("Conectado");
		}catch(XMPPException e){
			System.out.println("Error Conectando "+e.getMessage());
		}
	}
	
	
	static {
        disableSslVerification();
    }
	
	
	@OnOpen
	public void open(final Session session) {
		
		
		log.info("Abriendo Websocket"+session);
			
		//session.getUserProperties().put("room", se);
		
		
		
	}
	
	final TrustManager[] myTrustManager = new TrustManager[]{new X509TrustManager() {
		
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
	@OnMessage
	public void message(String mensaje, final Session session) throws IOException{
		log.info("recibiendo mensaje desde cliente: "+ mensaje);
		ChatManager chatmanager = this.connection.getChatManager();
		log.info("enviando mensaje a proveedores");
		try{
		JSONObject obj = new JSONObject(mensaje);
		Mensaje msj= new Mensaje();
		msj.setMensaje(obj.getString("mensaje"));
		msj.setUsers(obj.getString("users"));	
		String[] result = msj.getUsers().split("&&");
		
		
		//Se recorre la lista de usuarios
		
        for(String user : result){
        	log.info(">"+user+"<");
            user=user+"@ajustadoati.com";
            log.info("User: "+user);
           
            Chat newChat = chatmanager.createChat(user, new MessageListener() {   			
    		    public void processMessage(Chat chat, Message message) {
    		    	
    		    	log.info("Received message desde proveedor: " + message.getBody());
    		    	String arr[]=message.getFrom().split("@");
    		    	String from=arr[0];
    		    	
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
					log.info("realizando consulta a 9000************"+from);
        			WebResource webResource = client
        			   .resource("https://ajustadoati.com:9000/ajustadoati/usuario/"+from);

        			ClientResponse resp = webResource.accept("application/json")
        	                   .get(ClientResponse.class);

        			if (resp.getStatus() != 200) {
        			   throw new RuntimeException("Failed : HTTP error code : "
        				+ resp.getStatus());
        			}else{
        				log.info("The request has passed the test:");
        			}

        			String output = resp.getEntity(String.class);
        			JSONObject obj=null;
        			Response response=null;
					try {
						obj = new JSONObject(output);
						log.info("response:"+ obj);
						log.info("latitud: "+obj.getDouble("latitud"));
						response = new Response(from, message.getBody(), obj.getDouble("latitud"), obj.getDouble("longitud"));
	    		    	
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			
        			
    		    	log.info("Received from: " + response.getLatitud());
    		    	Gson gson = new Gson();
    		         String json = gson.toJson(response); 
    		    	try {
						session.getBasicRemote().sendText(json);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
    		    	
    		    	
    		    }
    		});
    		try {
    		    newChat.sendMessage(msj.getMensaje());
    		}catch (XMPPException e) {
    			log.info("Error Delivering block");
    		}
    	
        }
		} catch (JSONException e) {
            e.printStackTrace();
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
