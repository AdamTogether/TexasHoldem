package com.algorandex.model;

import java.util.Optional;

import com.algorandex.appuser.AppUser;
import com.algorandex.appuser.AppUserRepository;

import lombok.Data;

@Data
public class Game {
	
	private String gameId;
	private Player[] players;
	private Player[] foldedPlayers;
	private GameStatus gameStatus;
	private GameRoundType currentRound;
	private String[] board;
	private Integer pot = 0;
	private Integer checkAmount = 0;
	private Integer lastStartingPlayerIndex;
	private Boolean firstTimeThroughRound;
	private Player bigBlind;
	private Player littleBlind;
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
			if (this.players[i] == null) {
				this.players[i] = player;
				break;
			}
		}
	}
	
	public void setFoldedPlayers(Player[] foldedPlayers) {
		this.foldedPlayers = foldedPlayers;
	}

	public void addFoldedPlayer(Player foldedPlayer) {
		for (int i = 0; i < this.foldedPlayers.length; i++) {
			if (this.foldedPlayers[i] == null) {
				this.foldedPlayers[i] = foldedPlayer;
				break;
			}
		}
	}
	
	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}
	
	public void setCurrentRound(GameRoundType currentRound) {
		this.currentRound = currentRound;
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


	public void setCheckAmount(Integer checkAmount) {
		this.checkAmount = checkAmount;
	}
	
	public void addToCheckAmount(Integer checkAmount) {
		this.checkAmount += checkAmount;
	}
	

	public void setCurrentTurn(Player currentTurn) {
		this.currentTurn = currentTurn;
	}

	public void setWinner(Player winner) {
		this.winner = winner;
	}
	
	public void setFirstTimeThroughRound(Boolean firstTimeThroughRound) {
		this.firstTimeThroughRound = firstTimeThroughRound;
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
	
	public GameRoundType setCurrentRound() {
		return this.currentRound;
	}

	public String[] getBoard() {
		return this.board;
	}
	
	public Integer getPot() {
		return this.pot;
	}
	
	public Integer getCheckAmount() {
		return this.checkAmount;
	}
	
	public Player getCurrentTurn() {
		return this.currentTurn;
	}
	
	public Integer getCurrentTurnIndex() {
		int i = 0;
		for (i = 0; i < this.players.length; i++) {
			if (players[i] == this.getCurrentTurn()) {
				break;
			}
		}
		return i;
	}
	
	public Player getWinner() {
		return this.winner;
	}
	
	public Boolean getFirstTimeThroughRound() {
		return this.firstTimeThroughRound;
	}
	
	public Integer getCurrentPlayerCount() {
		int i = 0;
		for (i = 0; i < this.players.length; i++) {
			if (players[i+1] == null) {
				break;
			}
		}
		return i+1;
	}
	
	public Integer getPlayerIndexByUsername(String username) {
		int i;
		for (i = 0; i < this.players.length; i++) {
			if (players[i] == null) {
				break;
			}
//			System.out.format("players[i].getUsername(): %s\n", players[i].getUsername());
//			System.out.format("username: %s\n\n", username);
			if (players[i].getUsername().equals(username)) {
				break;
			}
		}
		return i;
	}
}
