package com.algorandex.storage;

import java.util.HashMap;
import java.util.Map;

import com.algorandex.model.Game;

public class GameStorage {
	
	private static Map<String, Game> games;
	private static GameStorage instance;
	
	private GameStorage() {
		games = new HashMap<>();
	}
	
	public static synchronized GameStorage getInstance() {
		if(instance==null) {
			instance = new GameStorage();
		}
		return instance;
	}
	
	public Map<String, Game> getGames() {
		return games;
	}
	
	public void setGame(Game pGame) {
		games.put(pGame.getGameId(), pGame);
	}
	
	public static void deleteGame(Game pGame) {
		games.remove(pGame.getGameId());
	}
}
