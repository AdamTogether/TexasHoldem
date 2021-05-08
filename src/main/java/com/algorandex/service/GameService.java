package com.algorandex.service;

import com.algorandex.appuser.AppUser;
import com.algorandex.appuser.AppUserRepository;
import com.algorandex.exception.InvalidGameException;
import com.algorandex.exception.InvalidParamException;
import com.algorandex.exception.NotFoundException;
import com.algorandex.model.Game;
import com.algorandex.model.GamePlay;

import static com.algorandex.model.GameStatus.*;
import static com.algorandex.model.HoldemMoveType.*;
import static com.algorandex.model.GameRoundType.*;
import com.algorandex.model.Player;
import com.algorandex.model.TicTacToe;
import com.algorandex.storage.GameStorage;

import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameService {
	
	private final AppUserRepository appUserRepository;
	private final String[] suites = {"hearts", "spades" ,"diamonds", "clubs"};
	private final String[] values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

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
	
	public String getRandomCard() {
		int randomSuiteNum = ThreadLocalRandom.current().nextInt(0, suites.length);
		int randomValueNum = ThreadLocalRandom.current().nextInt(0, values.length);
		String randomCard = String.format("%s_%s", suites[randomSuiteNum], values[randomValueNum]);
		
		return randomCard;
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
        game.setCurrentRound(START);
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
        		testHand[0] = getRandomCard();
        		testHand[1] = getRandomCard();
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
		GameStorage.getInstance().setGame(game);
		
		return game;
	}
	
	public Game gamePlay(GamePlay gamePlay) throws NotFoundException, InvalidGameException {
		System.out.println("\ngamePlay: '" + gamePlay + "'");
		if(!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId())) {
			throw new NotFoundException("Game not found");
		}
		
        Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());
		System.out.println("\nGameStorage.getInstance().getGames(): '" + GameStorage.getInstance().getGames() + "'");
        
        System.out.format("gamePlay.getPlayer().getUsername(): %s\n\n", gamePlay.getPlayer().getUsername());
    	System.out.format("game.getCurrentTurn().getUsername(): %s\n\n", game.getCurrentTurn().getUsername());
    	System.out.format("game.getCurrentTurnIndex(): %s\n\n", game.getCurrentTurnIndex());
    	System.out.format("game.getPlayers()[game.getCurrentTurnIndex()]: %s\n\n", game.getPlayers()[game.getCurrentTurnIndex()]);
		
		if (game.getGameStatus().equals(NEW)) {
        	System.out.println("\nThe game hasn't started yet.");
        	throw new InvalidGameException("The game hasn't started yet.");
 
        } 
		
        if (game.getGameStatus().equals(FINISHED)) {
        	System.out.println("Game is already finished.");
            throw new InvalidGameException("Game is already finished.");
        }
        
        if (!game.getCurrentTurn().getUsername().equals(gamePlay.getPlayer().getUsername())) {
        	System.out.println("\nIt's not your turn yet.");
        	throw new InvalidGameException("It's not your turn yet.");
        }

        if (gamePlay.getMove().equals(CHECK)) {
        	// TODO: Check checkAmount, if non-zero perform bet logic.
        	;
        } else if (gamePlay.getMove().equals(BET)) {
        	game.addToPot(gamePlay.getBetAmount());
        	game.addToCheckAmount(gamePlay.getBetAmount());
        	// TODO: Subtract from player's balance.
        } else if (gamePlay.getMove().equals(FOLD)) {
        	;
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
    	

        // If it is the last player in the lobbies' turn, reset the current turn to the first player in the lobby.
        if (game.getCurrentTurnIndex()+1 == game.getCurrentPlayerCount()) {
        	
        	game.setCurrentTurn(game.getPlayers()[0]);
        	game.setCheckAmount(0);
        	
        	// Advance to the next round and TODO: Draw cards appropriate to current round.
        	if (game.getCurrentRound().equals(START)) {
            	game.setCurrentRound(FLOP);
            	String[] tempBoard = {getRandomCard(), getRandomCard(), getRandomCard(), null, null};
            	game.setBoard(tempBoard);
            } else if (game.getCurrentRound().equals(FLOP)) {
            	game.setCurrentRound(TURN);
            	String[] tempBoard = {game.getBoard()[0], game.getBoard()[1], game.getBoard()[2], getRandomCard(), null};
            	game.setBoard(tempBoard);
            } else if (game.getCurrentRound().equals(TURN)) {
//            	game.setCurrentRound(RIVER);
            	String[] tempBoard = {game.getBoard()[0], game.getBoard()[1], game.getBoard()[2], game.getBoard()[3], getRandomCard()};
            	game.setCurrentTurn(null);
            	game.setGameStatus(FINISHED);
            	game.setBoard(tempBoard);
            } else if (game.getCurrentRound().equals(RIVER)) {
            	game.setCurrentTurn(null);
            	game.setGameStatus(FINISHED);
            	// TODO: Evaluate winner.
            	// TODO: Clear currentHands.
            }
        // Otherwise, set the current turn to the next player in the lobby
        } else {
        	game.setCurrentTurn(game.getPlayers()[game.getCurrentTurnIndex()+1]);
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
