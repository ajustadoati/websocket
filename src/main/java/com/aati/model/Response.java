package com.aati.model;

public class Response {
	private String user;
	private String message;
	private Double latitud;
	private Double longitud;
	public Response(String user, String message, Double latitud, Double longitud){
		this.user=user;
		this.message=message;
		this.latitud=latitud;
		this.longitud=longitud;
	}
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Double getLatitud() {
		return latitud;
	}
	public void setLatitud(Double latitud) {
		this.latitud = latitud;
	}
	public Double getLongitud() {
		return longitud;
	}
	public void setLongitud(Double longitud) {
		this.longitud = longitud;
	}
	
	
	

}
