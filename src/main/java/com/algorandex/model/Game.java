package com.algorandex.model;

import lombok.Data;

@Data
public class Game {
	
	private String gameId;
	private Player[] players;
	private Player player1;
	private Player player2;
	private GameStatus gameStatus;
	private int[][] board;
	private TicTacToe currentTurn;
	private TicTacToe winner;
	
	// SETTER FUNCTIONS
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	public void setPlayers(Player[] players) {
		this.players = players;
		this.player1 = players[0];
	}
	
	public void setPlayer2(Player player) {
		this.players[1] = player;
		this.player2 = player;
	}
	
	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}

	public void setBoard(int[][] board) {
		this.board = board;
	}

	public void setCurrentTurn(TicTacToe currentTurn) {
		this.currentTurn = currentTurn;
	}

	public void setWinner(TicTacToe winner) {
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

	public int[][] getBoard() {
		return this.board;
	}
	
	
}
