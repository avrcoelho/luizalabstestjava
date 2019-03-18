package com.luizalabstest.api.model;

import java.util.ArrayList;
import java.util.HashMap;

public class LuizaLabsTestModel {
	
	 private Integer total_kills;
	 private ArrayList<Object> players;
	 private HashMap<String, Integer> kills;
	   
	 public LuizaLabsTestModel(Integer total_kills, ArrayList<Object> players, HashMap<String, Integer> kills) {
	   this.total_kills = total_kills;
	   this.players = players;
	   this.kills = kills;
	 }

	public Integer getTotal_kills() {
		return total_kills;
	}

	public void setTotal_kills(Integer total_kills) {
		this.total_kills = total_kills;
	}

	public ArrayList<Object> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<Object> players) {
		this.players = players;
	}

	public HashMap<String, Integer> getKills() {
		return kills;
	}

	public void setKills(HashMap<String, Integer> kills) {
		this.kills = kills;
	}

}
