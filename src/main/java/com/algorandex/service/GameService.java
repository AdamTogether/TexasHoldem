package com.algorandex.service;

import com.algorandex.appuser.AppUser;
import com.algorandex.appuser.AppUserRepository;
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

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameService {
	
	private final AppUserRepository appUserRepository;

	public Game createGame(Player player) {
		Game game = new Game();
		game.setBoard(new String[5]);
		game.setGameId(UUID.randomUUID().toString());
		game.setGameStatus(NEW);
		
		Player[] players = new Player[8];
		players[0] = player;
		game.setPlayers(players);
        game.setCurrentTurn(player);

		GameStorage.getInstance().setGame(game);
		
		return game;
	}
	
	public Game startGame(GamePlay gamePlay) throws NotFoundException, InvalidGameException {
		if(!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId())) {
			throw new NotFoundException("Game not found");
		}
		
        Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());

        if (game.getGameStatus().equals(FINISHED)) {
            throw new InvalidGameException("Game is already finished");
        }
        
        game.setGameStatus(IN_PROGRESS);
        game.setCurrentTurn(game.getPlayers()[0]);
//        if (game.getPlayers()[2] != null) {
//        	game.setGameStatus(IN_PROGRESS);
//        } else {
//            throw new InvalidGameException("Couldn't start match, waiting for more players...");
//        }
        for (int i=0; i < game.getPlayers().length; i++) {
        	if (game.getPlayers()[i] == null) {
        		break;
        	} else {
        		Optional<AppUser> appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
        		AppUser appUser = appUserSearch.get();
        		String[] testHand = new String[2];
        		testHand[0] = "spade_5";
        		testHand[1] = "heart_K";
        		appUser.setCurrentHand(testHand);
        		appUserRepository.save(appUser);
        	}
        }
		
        return game;
	}
	
	public Game connectToGame(Player newPlayer, String gameId) throws InvalidParamException, InvalidGameException {
		if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
			throw new InvalidParamException("Game with provided ID '" + gameId + "' does not exist.");
		}
		
		Game game = GameStorage.getInstance().getGames().get(gameId);
		
		if (game.getPlayers()[7] != null) {
			throw new InvalidGameException("Game with provided ID '" + gameId + "' is already full.");
		}
		
		if (!game.getGameStatus().equals(NEW)) {
			throw new InvalidGameException("Game with provided ID '" + gameId + "' has already started.");
		}
		
		game.addPlayer(newPlayer);
//        game.setCurrentTurn(game.getPlayers()[0]);
		GameStorage.getInstance().setGame(game);
		
		return game;
	}
	
	public Game connectToRandomGame(Player newPlayer) throws NotFoundException, InvalidGameException {
		//TODO: Add logic to filter iterator to check for a full lobby.
		Game game = GameStorage.getInstance().getGames().values().stream()
				.filter(it->it.getGameStatus().equals(NEW))
				.findFirst().orElseThrow(()-> new NotFoundException("Couldn't find any open games."));

		if (game.getPlayers()[7] != null) {
			throw new InvalidGameException("Game lobby is already full. Create your own game!");
		}
		
		game.addPlayer(newPlayer);
//        game.setCurrentTurn(game.getPlayers()[0]);
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
        
        if (!game.getCurrentTurn().equals(gamePlay.getPlayer())) {
        	throw new InvalidGameException("It's not your turn yet.");
        }
        
//        int[][] board = game.getBoard();
//        board[gamePlay.getCoordinateX()][gamePlay.getCoordinateY()] = gamePlay.getType().getValue();
//
//        Boolean xWinner = checkWinner(game.getBoard(), TicTacToe.X);
//        Boolean oWinner = checkWinner(game.getBoard(), TicTacToe.O);
//
//        if (xWinner) {
//            game.setWinner(game.getPlayers()[0]);
//            game.setGameStatus(FINISHED);
//        } else if (oWinner) {
//            game.setWinner(game.getPlayers()[1]);
//            game.setGameStatus(FINISHED);
//        }
//
//        if (gamePlay.getType().equals(TicTacToe.X)) {
//        	game.setCurrentTurn(game.getPlayers()[1]);
//        } else if (gamePlay.getType().equals(TicTacToe.O)) {
//        	game.setCurrentTurn(game.getPlayers()[0]);
//        }
        
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
