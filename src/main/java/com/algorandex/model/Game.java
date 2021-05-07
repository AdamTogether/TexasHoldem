package com.algorandex.model;

import com.algorandex.appuser.AppUserRepository;

import lombok.Data;

@Data
public class Game {
	
//	private final AppUserRepository appUserRepository;
	
	private String gameId;
	private Player[] players;
	private GameStatus gameStatus;
	private String[] board;
	private Integer pot = 0;
	private Player currentTurn;
	private Player winner;
	
	// SETTER FUNCTIONS
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	public void setPlayers(Player[] players) {
		this.players = players;
	}

	public void addPlayer(Player player) {
		for (int i = 0; i < this.players.length; i++) {
			if (players[i] == null) {
				this.players[i] = player;
				break;
			}
		}
	}
	
	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}

	public void setBoard(String[] board) {
		this.board = board;
	}

	public void setPot(Integer pot) {
		this.pot = pot;
	}
	
	public void addToPot(Integer pot) {
		this.pot += pot;
	}

	public void setCurrentTurn(Player currentTurn) {
		this.currentTurn = currentTurn;
	}

	public void setWinner(Player winner) {
		this.winner = winner;
	}
	
	// GETTER FUNCTIONS
	public String getGameId() {
		return this.gameId;
	}
	
	public Player[] getPlayers() {
		return this.players;
	}
	
	public GameStatus getGameStatus() {
		return this.gameStatus;
	}

	public String[] getBoard() {
		return this.board;
	}
	
	public Integer getPot() {
		return this.pot;
	}
	
	public Player getCurrentTurn() {
		return this.currentTurn;
	}
	
	public Player getWinner() {
		return this.winner;
	}
}
