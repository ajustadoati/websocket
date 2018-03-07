/**
 * 
 */
package com.aati.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aati.model.Dispositivo;
import com.aati.model.Notificacion;
import com.aati.model.Response;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * @author Richard Rojas
 *
 */
public class Util {
	
	public static void sendNotifications(List<String> dispositivos, String mensaje){
		
		
		Client client = Client.create();
        WebResource webResource = client.resource("https://onesignal.com/api/v1/notifications");
        Gson gson = new Gson();
        
        Notificacion notif= new Notificacion("d42e3099-ca74-4cde-bf95-6b7033f53b0f", dispositivos, mensaje);
        System.out.println("gson:  "+gson.toJson(notif));

        // POST method
        ClientResponse response = webResource.accept("application/json")
                .type("application/json").header("Authorization", "Basic ODU4NGFlNzktYzIzMi00MmEyLWFkMmEtNmFlMTE3ODg3OTVj").post(ClientResponse.class, gson.toJson(notif));

        // check response status code
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        // display response
        String output = response.getEntity(String.class);
        System.out.println("Output from Server .... ");
        System.out.println(output + "\n");

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
	
	
	public static List<String> getDevicesByUser(String user) {
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
		   .resource("https://ajustadoati.com:9000/ajustadoati/dispositivo/usuario/"+user);

		ClientResponse resp = webResource.accept("application/json")
                 .get(ClientResponse.class);

		if (resp.getStatus() != 200) {
		   throw new RuntimeException("Failed : HTTP error code : "
			+ resp.getStatus());
		}else{
			System.out.println("The request has passed the test:");
		}

		String output = resp.getEntity(String.class);
		JSONArray obj=null;
		List<String> dispositivos= new ArrayList<String>();
		try {
			obj= new JSONArray(output);
			
			for(int i=0;i<obj.length();i++){
				JSONObject ob = (JSONObject)obj.getJSONObject(i);
				dispositivos.add(ob.getString("uuid"));
				
			}
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dispositivos;
	}
	
	public static List<Dispositivo> getDevicesByListUsers(String users) {
		HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
		ClientConfig config = new DefaultClientConfig();
		SSLContext ctx = null;
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
		System.out.println("realizando consulta a 9000************"+users);
		WebResource webResource = client
		   .resource("https://ajustadoati.com:9000/ajustadoati/dispositivo/usuarios/"+users);

		ClientResponse resp = webResource.accept("application/json")
                 .get(ClientResponse.class);

		if (resp.getStatus() != 200) {
		   throw new RuntimeException("Failed : HTTP error code : "
			+ resp.getStatus());
		}else{
			System.out.println("The request has passed the test:");
		}

		String output = resp.getEntity(String.class);
		JSONArray obj=null;
		List<Dispositivo> dispositivos= new ArrayList<Dispositivo>();
		try {
			obj= new JSONArray(output);
			
			for(int i=0;i<obj.length();i++){
				JSONObject ob = (JSONObject)obj.getJSONObject(i);
				JSONObject disp = ob.getJSONObject("dispositivo");
				String usr = ob.getString("usuario");
				Dispositivo dis= new Dispositivo();
				dis.setNombre(disp.getString("nombre"));
				dis.setTipo(disp.getString("tipo"));
				dis.setUuid(disp.getString("uuid"));
				dis.setUsuario(usr);
				dispositivos.add(dis);
			}
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dispositivos;
	}
	
	public static List<String> getDevicesForUser(List<Dispositivo> devices, String user){
		List<String> lista= new ArrayList<String>();
		for(Dispositivo dev:devices){
			if(dev.getUsuario().equals(user)){
				lista.add(dev.getUuid());
			}
		}
		if(lista.size() > 0)
			return lista;
		
		return null;
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
