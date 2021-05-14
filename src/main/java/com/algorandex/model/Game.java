package com.algorandex.model;

import java.util.Optional;

import com.algorandex.appuser.AppUser;
import com.algorandex.appuser.AppUserRepository;

import lombok.Data;

@Data
public class Game {
	
	private String gameId;
	private Player[] players = new Player[8];
	private Player[] foldedPlayers;
	private GameStatus gameStatus;
	private Boolean resetLobby = false;
	private GameRoundType currentRound;
	private String[] board;
	private Double pot = 0.0;
	private Double checkAmount = 0.0;
	private Integer lastStartingPlayerIndex;
	private Boolean firstTimeThroughRound;
	private Player justLeftLobby;
	private Player bigBlind;
	private Player littleBlind;
	private Player currentTurn;
	private Player[] winners = new Player[8];
	private String holdemWinString;
	
	// SETTER FUNCTIONS
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	public void setPlayers(Player[] players) {
		this.players = players;
	}
	
	public void rotatePlayers() {
		// Move first player to the last.
		this.players[this.getCurrentPlayerCount()] = this.players[0];
		
		// Shift all players forward one slot and set remove the duplicate.
		for (int i = 0; i < this.players.length; i++) {
			this.players[i] = this.players[i+1];
			
			if (this.players[i] == null) {
				break;
			}
		}
	}

	public void addPlayer(Player player) {
		for (int i = 0; i < this.players.length; i++) {
			if (this.players[i] == null) {
				this.players[i] = player;
				this.setJustLeftLobby(null);
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
	
	public void resetBoard() {
		for (int i = 0; i < this.board.length; i++) {
			this.board[i] = null;
		}
	}

	public void setPot(Double pot) {
		this.pot = pot;
	}
	
	public void addToPot(Double pot) {
		this.pot += pot;
	}


	public void setCheckAmount(Double checkAmount) {
		this.checkAmount = checkAmount;
	}
	
	public void addToCheckAmount(Double checkAmount) {
		this.checkAmount += checkAmount;
	}
	

	public void setCurrentTurn(Player currentTurn) {
		this.currentTurn = currentTurn;
	}

	public void setWinners(Player[] winners) {
		this.winners = winners;
	}

	public void resetWinners() {
		for (int i = 0; i < this.winners.length; i++) {
			this.winners[i] = null;
		}
	}

	public void addWinner(Player winner) {
		for (int i = 0; i < this.winners.length; i++) {
			if (this.winners[i] == null) {
				this.winners[i] = winner;
				break;
			}
		}
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
	
	public Double getPot() {
		return this.pot;
	}
	
	public Double getCheckAmount() {
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
	
	public Player[] getWinners() {
		return this.winners;
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
				i = -1;
				break;
			}
			if (players[i].getUsername().equals(username)) {
				break;
			}
		}
		return i;
	}
	
	public Integer getWinnerCount() {
		int i = 0;
		for (i = 0; i < this.winners.length; i++) {
			if (this.winners[i+1] == null) {
				break;
			}
		}
		return i+1;
	}

	public void removePlayer(Player playerToRemove) {
		Boolean playerRemoved = false;
		
		// Go through players, remove the player and shift all other players over to fill in the gap.
		for (int i = 0; i < this.players.length; i++) {
			if (this.players[i] == null) {
				break;
			} else if (playerRemoved) {
				this.players[i] = this.players[i+1];
			} else if (this.players[i].getUsername().equals(playerToRemove.getUsername())) {
				this.setJustLeftLobby(playerToRemove);
				this.players[i] = this.players[i+1];
				playerRemoved = true;
			}
		}
	}
}
