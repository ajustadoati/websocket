/**
 * 
 */
package com.aati.service;

import java.io.IOException;
import java.util.logging.Logger;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 * @author Richard Rojas
 *
 */
public class ConnectAdmin {
	
	private static AbstractXMPPConnection connection;
	private final Logger log = Logger.getLogger(getClass().getName());
	private static ConnectAdmin connectAdmin;
	private ChatManager chatManager;
	private ConnectAdmin(){
		log.info("init connection admin in opnefire");
		XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword("admin@ajustadoati.com", "Dexter876");
		try {
			configBuilder.setResource("SmackJavaTestClient");
			configBuilder.setHost("ajustadoati.com");
			configBuilder.setXmppDomain("ajustadoati.com");
			configBuilder.setSecurityMode(SecurityMode.disabled);
			configBuilder.setPort(5222);
		} catch (XmppStringprepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connection = new XMPPTCPConnection(configBuilder.build());
		// Connect to the server
		try {
			if(!connection.isConnected()){
				connection.connect();
				connection.login();
				chatManager = org.jivesoftware.smack.chat2.ChatManager.getInstanceFor(getConnection());
			}
		} catch (SmackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ConnectAdmin getInstance(){
		
		if(connectAdmin!=null){
			System.out.println("es null");
			return connectAdmin;
		}else{
			System.out.println("no es null");
			return new ConnectAdmin();
		}
	}

	/**
	 * @return the connection
	 */
	public AbstractXMPPConnection getConnection() {
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public static void setConnection(AbstractXMPPConnection connection) {
		ConnectAdmin.connection = connection;
	}

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
	
	

}
