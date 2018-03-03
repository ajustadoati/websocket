/**
 * 
 */
package com.aati.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Richard Rojas
 *
 */
public class Notificacion {
	@SerializedName("app_id")
	private String appId;
	@SerializedName("include_player_ids")
	private List<String> playersIds = new ArrayList<String>();
	@SerializedName("data")
	private Data data=new Data();
	@SerializedName("contents")
	private ContentsEN contents;
	
	/**
	 * 
	 * @param appId
	 * @param playerIds
	 * @param content
	 */
	public Notificacion(String appId,List<String> playerIds, String content){
		this.appId = appId;
		this.contents = new ContentsEN(content);
		this.data.setFoo("Bar");
		for (String id : playerIds) {
			getPlayersIds().add(id);
		}
		
	}
	
	/**
	 */
	public class ContentsEN{
		private String en;
		public ContentsEN(String en){
			this.en=en;
		}
		/**
		 * @return the en
		 */
		public String getEn() {
			return en;
		}

		/**
		 * @param en the en to set
		 */
		public void setEn(String en) {
			this.en = en;
		}
		
	}
	
	public class Data{
		private String foo;

		/**
		 * @return the foo
		 */
		public String getFoo() {
			return foo;
		}

		/**
		 * @param foo the foo to set
		 */
		public void setFoo(String foo) {
			this.foo = foo;
		}
		
	}
	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @param appId the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	/**
	 * @return the playersIds
	 */
	public List<String> getPlayersIds() {
		return playersIds;
	}
	/**
	 * @param playersIds the playersIds to set
	 */
	public void setPlayersIds(List<String> playersIds) {
		this.playersIds = playersIds;
	}
	
	/**
	 * @return the contents
	 */
	public ContentsEN getContents() {
		return contents;
	}
	/**
	 * @param contents the contents to set
	 */
	public void setContents(ContentsEN contents) {
		this.contents = contents;
	}
	
	

}
