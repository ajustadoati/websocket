/**
 * 
 */
package com.aati.service;

import java.util.ArrayList;
import java.util.List;

import com.aati.model.Notificacion;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * @author Richard Rojas
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args){
		try {
	        Client client = Client.create();
	        System.out.println("Consultando");
	        WebResource webResource = client.resource("https://onesignal.com/api/v1/notifications");
	        Gson gson = new Gson();
		    //String json = gson.toJson(response); 
	        List<String> ids= new ArrayList<String>();
	        ids.add("4880e13f-126e-4c76-ba8c-83fa7ff26dc0");
	        ids.add("76ece62b-bcfe-468c-8a78-839aeaa8c5fa");
	        
	        Notificacion notif= new Notificacion("d42e3099-ca74-4cde-bf95-6b7033f53b0f", ids, "Texto de prueba");
	        System.out.println("gson:  "+gson.toJson(notif));
	        
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
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
