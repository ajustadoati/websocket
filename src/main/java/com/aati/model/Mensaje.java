package com.aati.model;

import java.io.Serializable;

import javax.websocket.Session;
import javax.xml.bind.annotation.XmlRootElement;

import org.jivesoftware.smack.chat2.ChatManager;
@XmlRootElement
/*
 * 
 */
public class Mensaje implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String users;
	private String mensaje;
	private String latitud;
	private String longitud;
	private String code;
	private Session session;
	private ChatManager chatManager;
	/**
	 * @return the chatManager
	 */
	public ChatManager getChatManager() {
		return chatManager;
	}
	/**
	 * @param chatManager the chatManager to set
	 */
	public void setChatManager(ChatManager chatManager) {
		this.chatManager = chatManager;
	}
	public String getUsers() {
		return users;
	}
	public void setUsers(String users) {
		this.users = users;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	/**
	 * @return the latitud
	 */
	public String getLatitud() {
		return latitud;
	}
	/**
	 * @param latitud the latitud to set
	 */
	public void setLatitud(String latitud) {
		this.latitud = latitud;
	}
	/**
	 * @return the longitud
	 */
	public String getLongitud() {
		return longitud;
	}
	/**
	 * @param longitud the longitud to set
	 */
	public void setLongitud(String longitud) {
		this.longitud = longitud;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}
	/**
	 * @param session the session to set
	 */
	public void setSession(Session session) {
		this.session = session;
	}
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	
	
	

}
