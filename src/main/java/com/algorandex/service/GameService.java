package com.algorandex.service;

import com.algorandex.exception.InvalidGameException;
import com.algorandex.exception.InvalidParamException;
import com.algorandex.exception.NotFoundException;
import com.algorandex.model.Game;
import com.algorandex.model.GamePlay;

import static com.algorandex.model.GameStatus.*;
import com.algorandex.model.Player;
import com.algorandex.model.TicTacToe;
import com.algorandex.storage.GameStorage;

import lombok.AllArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameService {

	public Game createGame(Player player) {
		Game game = new Game();
		game.setBoard(new int[3][3]);
		game.setGameId(UUID.randomUUID().toString());
		game.setGameStatus(NEW);
		
		Player[] players = new Player[8];
		players[0] = player;
		game.setPlayers(players);

		GameStorage.getInstance().setGame(game);
		
		return game;
	}
	
	public Game connectToGame(Player player2, String gameId) throws InvalidParamException, InvalidGameException {
		if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
			throw new InvalidParamException("Game with provided ID '" + gameId + "' does not exist.");
		}
		
		Game game = GameStorage.getInstance().getGames().get(gameId);
		
		if (game.getPlayers()[1] != null) {
			throw new InvalidGameException("Game with provided ID '" + gameId + "' is already full.");
		}
		
		game.setPlayer2(player2);
		game.setGameStatus(IN_PROGRESS);
        game.setCurrentTurn(TicTacToe.X);
		GameStorage.getInstance().setGame(game);
		
		return game;
	}
	
	public Game connectToRandomGame(Player player2) throws NotFoundException {
		Game game = GameStorage.getInstance().getGames().values().stream()
				.filter(it->it.getGameStatus().equals(NEW))
				.findFirst().orElseThrow(()-> new NotFoundException("Couldn't find any open games."));
		
		game.setPlayer2(player2);
		game.setGameStatus(IN_PROGRESS);
        game.setCurrentTurn(TicTacToe.X);
		GameStorage.getInstance().setGame(game);
		
		return game;
	}
	
	public Game gamePlay(GamePlay gamePlay) throws NotFoundException, InvalidGameException {
		if(!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId())) {
			throw new NotFoundException("Game not found");
		}
		
        Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());

        if (game.getGameStatus().equals(FINISHED)) {
            throw new InvalidGameException("Game is already finished");
        }
        
        if (!game.getCurrentTurn().equals(gamePlay.getType())) {
        	throw new InvalidGameException("It's not your turn yet.");
        }

        int[][] board = game.getBoard();
        board[gamePlay.getCoordinateX()][gamePlay.getCoordinateY()] = gamePlay.getType().getValue();

        Boolean xWinner = checkWinner(game.getBoard(), TicTacToe.X);
        Boolean oWinner = checkWinner(game.getBoard(), TicTacToe.O);

        if (xWinner) {
            game.setWinner(TicTacToe.X);
            game.setGameStatus(FINISHED);
        } else if (oWinner) {
            game.setWinner(TicTacToe.O);
            game.setGameStatus(FINISHED);
        }

        if (gamePlay.getType().equals(TicTacToe.X)) {
        	game.setCurrentTurn(TicTacToe.O);
        } else if (gamePlay.getType().equals(TicTacToe.O)) {
        	game.setCurrentTurn(TicTacToe.X);
        }
        
        GameStorage.getInstance().setGame(game);
		
		return game;
	}

	private Boolean checkWinner(int[][] board, TicTacToe ticTacToe) {
	       int[] boardArray = new int[9];
	        int counterIndex = 0;
	        for (int i = 0; i < board.length; i++) {
	            for (int j = 0; j < board[i].length; j++) {
	                boardArray[counterIndex] = board[i][j];
	                counterIndex++;
	            }
	        }

	        int[][] winCombinations = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
	        for (int i = 0; i < winCombinations.length; i++) {
	            int counter = 0;
	            for (int j = 0; j < winCombinations[i].length; j++) {
	                if (boardArray[winCombinations[i][j]] == ticTacToe.getValue()) {
	                    counter++;
	                    if (counter == 3) {
	                        return true;
	                    }
	                }
	            }
	        }
	        
	        return false;
	}
}
