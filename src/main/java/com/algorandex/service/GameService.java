package com.algorandex.service;

import com.algorandex.appuser.AppUser;
import com.algorandex.appuser.AppUserRepository;
import com.algorandex.exception.InvalidGameException;
import com.algorandex.exception.InvalidParamException;
import com.algorandex.exception.NotFoundException;
import com.algorandex.model.Game;
import com.algorandex.model.GamePlay;
import com.algorandex.model.HoldemWinType;

import static com.algorandex.model.GameStatus.*;
import static com.algorandex.model.HoldemMoveType.*;
import static com.algorandex.model.HoldemWinType.*;
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
	private final String[] suites = {"clubs", "diamonds", "hearts", "spades"};
	private final String[] values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

	public Game createGame(Player player) {
		Game game = new Game();
		game.setBoard(new String[5]);
		game.setGameId(UUID.randomUUID().toString());
		game.setGameStatus(NEW);
		
		Player[] players = new Player[8];
		players[0] = player;
		game.setPlayers(players);
		
		Player[] foldedPlayers = new Player[8];
		game.setFoldedPlayers(foldedPlayers);
		game.setCurrentTurn(player);

		GameStorage.getInstance().setGame(game);

		Optional<AppUser> appUserSearch = appUserRepository.findByUsername(game.getPlayers()[game.getPlayerIndexByUsername(player.getUsername())].getUsername());
		AppUser appUser = appUserSearch.get();

		appUser.setAmountBetThisRound(0.0);
		appUser.setCurrentHand(null);
		appUser.setFolded(false);
		appUser.setHoldemWinString(null);
		appUser.setHoldemWinType(null);
		appUserRepository.save(appUser);
		
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
//			throw new InvalidGameException("Game is already finished");
			// TODO: Reset game.
			game.resetBoard();
			game.resetWinners();
			game.setResetLobby(true);
			game.setPot(0.0);
		}

		if (game.getPlayers()[1] != null) {
			game.setGameStatus(IN_PROGRESS);
		} else {
			throw new InvalidGameException("Couldn't start match, waiting for more players...");
		}

		Player[] foldedPlayers = new Player[8];
		game.setFoldedPlayers(foldedPlayers);
		
		game.setCurrentRound(START);
		game.setCurrentTurn(game.getPlayers()[0]);
		game.setFirstTimeThroughRound(true);
		for (int i=0; i < game.getPlayers().length; i++) {
			if (game.getPlayers()[i] == null) {
				break;
			} else {
				Optional<AppUser> appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
				AppUser appUser = appUserSearch.get();

				String[] testHand = new String[2];
				testHand[0] = getRandomCard();
				testHand[1] = getRandomCard();
				appUser.setAmountBetThisRound(0.0);
				appUser.setCurrentHand(testHand);
				appUser.setFolded(false);
				appUser.setHoldemWinString(null);
				appUser.setHoldemWinType(null);
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
		
		if (!game.getGameStatus().equals(NEW) || !game.getGameStatus().equals(FINISHED)) {
			throw new InvalidGameException("Game with provided ID '" + gameId + "' has already started.");
		}
		
		// If player already in the lobby, throw exception.
		if (game.getPlayerIndexByUsername(newPlayer.getUsername()) != -1) {
			throw new InvalidGameException("You're already in this lobby");
		}
		
		game.addPlayer(newPlayer);
		GameStorage.getInstance().setGame(game);
		
		Optional<AppUser> appUserSearch = appUserRepository.findByUsername(game.getPlayers()[game.getPlayerIndexByUsername(newPlayer.getUsername())].getUsername());
		AppUser appUser = appUserSearch.get();

		appUser.setAmountBetThisRound(0.0);
		appUser.setCurrentHand(null);
		appUser.setFolded(false);
		appUser.setHoldemWinString(null);
		appUser.setHoldemWinType(null);
		appUserRepository.save(appUser);
		
		return game;
	}
	
	public Game connectToRandomGame(Player newPlayer) throws NotFoundException, InvalidGameException {
		//TODO: Add logic to filter iterator to check for a full lobby.
		Game game = GameStorage.getInstance().getGames().values().stream()
				.filter(
						it -> (	it.getGameStatus().equals(NEW) || it.getGameStatus().equals(FINISHED)) 
								&& (it.getPlayers()[7] == null)
								&& (it.getPlayerIndexByUsername(newPlayer.getUsername()) == -1) )
				.findFirst().orElseThrow(()-> new NotFoundException("Couldn't find any open games."));

		if (game.getPlayers()[7] != null) {
			throw new InvalidGameException("Game lobby is already full. Create your own game!");
		}
		
		game.addPlayer(newPlayer);
		GameStorage.getInstance().setGame(game);

		Optional<AppUser> appUserSearch = appUserRepository.findByUsername(game.getPlayers()[game.getPlayerIndexByUsername(newPlayer.getUsername())].getUsername());
		AppUser appUser = appUserSearch.get();

		appUser.setAmountBetThisRound(0.0);
		appUser.setCurrentHand(null);
		appUser.setFolded(false);
		appUser.setHoldemWinString(null);
		appUser.setHoldemWinType(null);
		appUserRepository.save(appUser);
		
		return game;
	}
	
	public Game gamePlay(GamePlay gamePlay) throws NotFoundException, InvalidGameException {
		System.out.println("\ngamePlay: '" + gamePlay + "'");
		if(!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId())) {
			throw new NotFoundException("Game not found");
		}
		
		Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());

		// Get appUser from player username.
		Optional<AppUser> appUserSearch = appUserRepository.findByUsername(gamePlay.getPlayer().getUsername());
		AppUser appUser = appUserSearch.get();
		System.out.format("\nappUser: %s\n\n", appUser);
		
		// Check if the user has already folded.
		if (appUser.getFolded()) {
			throw new InvalidGameException("You have already folded.");
		}
		
		// Check if game has started.
		if (game.getGameStatus().equals(NEW)) {
			System.out.println("\nThe game hasn't started yet.");
			throw new InvalidGameException("The game hasn't started yet.");
 
		}
		
		// Check if game is finished.
		if (game.getGameStatus().equals(FINISHED)) {
			System.out.println("Game is already finished.");
			throw new InvalidGameException("Game is already finished.");
		}
		
		// Check if it is this player's turn.
		if (!game.getCurrentTurn().getUsername().equals(gamePlay.getPlayer().getUsername())) {
			System.out.println("\nIt's not your turn yet.");
			throw new InvalidGameException("It's not your turn yet.");
		}
		
		// Check if the lobby has been reset.
		if (game.getResetLobby()) {
			game.setResetLobby(false);
		}

		// Check the move type and proceed accordingly.
		if (gamePlay.getMove().equals(CHECK)) {
			// If diff between game and player's check amount and subtract from player's balance.
			Double checkAmountDifference = game.getCheckAmount() - appUser.getAmountBetThisRound();
			if (checkAmountDifference != 0) {
				game.addToPot(checkAmountDifference);
				appUser.addToAmountBetThisRound(checkAmountDifference);
				// TODO: Subtract from player's balance.
//				appUserSearch = appUserRepository.findByUsername(gamePlay.getPlayer().getUsername());
//				AppUser tempAppUser = appUserSearch.get();
				appUser.subtractFromBalance(checkAmountDifference);
//				appUserRepository.save(tempAppUser);
			}
		} else if (gamePlay.getMove().equals(BET)) {
			Double betAmountDifference = game.getCheckAmount() - appUser.getAmountBetThisRound();
			
			game.addToPot(betAmountDifference+gamePlay.getBetAmount());
			appUser.addToAmountBetThisRound(betAmountDifference+gamePlay.getBetAmount());
			game.addToCheckAmount(Double.valueOf(gamePlay.getBetAmount()));
			// TODO: Subtract from player's balance.
//			appUserSearch = appUserRepository.findByUsername(appUser.getUsername());
//			AppUser tempAppUser = appUserSearch.get();
			appUser.subtractFromBalance(betAmountDifference+gamePlay.getBetAmount());
//			appUserRepository.save(tempAppUser);
		} else if (gamePlay.getMove().equals(FOLD)) {
			// Set folded to true and save to appUserRepository.
			appUser.setFolded(true);
			game.addFoldedPlayer(game.getPlayers()[game.getPlayerIndexByUsername(appUser.getUsername())]);
		}
		
		// Save changes to appUser.
		appUserRepository.save(appUser);
		
		// Check if there is only one non-folded player remaining, if so they win.
		if (this.getNonFoldedPlayerCount(game) == 1) {
			String[] tempBoard = {game.getBoard()[0], game.getBoard()[1], game.getBoard()[2], game.getBoard()[3], game.getBoard()[4]};
			for (int i = 0; i < tempBoard.length; i++) {
				if (tempBoard[i] == null) {
					tempBoard[i] = getRandomCard();
				}
			}
			game.setBoard(tempBoard);
			game.setCurrentTurn(null);
			game.setGameStatus(FINISHED);
			Player[] winners = new Player[8];
			winners[0] = game.getPlayers()[this.getFirstNonFoldedPlayerIndex(game)];
			game.setWinners(winners);
			// TODO: Add pot to winner's appUser balance.
			appUserSearch = appUserRepository.findByUsername(winners[0].getUsername());
			AppUser tempAppUser = appUserSearch.get();
			tempAppUser.addToBalance(game.getPot()/game.getWinnerCount());
			appUserRepository.save(tempAppUser);
		}

		Boolean checkAmountMetByAllActivePlayers = true;
		
		// Check that all players have met the checkAmount.
		for (int i = 0; i < game.getCurrentPlayerCount(); i++) {
			appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
			AppUser tempAppUser = appUserSearch.get();
			
			// If the user hasn't folded and they haven't met the check amount, set them as the next player.
			if ((!tempAppUser.getFolded()) && (!game.getCheckAmount().equals(tempAppUser.getAmountBetThisRound()))) {
				System.out.println("User: '" + tempAppUser.getUsername() + "' has not met the checkAmount.");
				System.out.format("game.getPlayers()[game.getPlayerIndexByUsername(tempAppUser.getUsername())]: '%s'\n\n", game.getPlayers()[game.getPlayerIndexByUsername(tempAppUser.getUsername())]);
				System.out.format("tempAppUser.getUsername(): '%s'\n", tempAppUser.getUsername());
				System.out.format("game.getPlayerIndexByUsername(tempAppUser.getUsername()): '%s'\n", game.getPlayerIndexByUsername(tempAppUser.getUsername()));
				checkAmountMetByAllActivePlayers = false;
				break;
			}
		}
		
		System.out.format("game.getFirstTimeThroughRound(): '%s'\n", game.getFirstTimeThroughRound());
		System.out.format("!game.getFirstTimeThroughRound(): '%s'\n", !game.getFirstTimeThroughRound());
		
		// If it is the active player in the lobbies' turn or checkAmount is met by all active players, proceed accordingly.
		if (
			(!game.getFirstTimeThroughRound() && checkAmountMetByAllActivePlayers) 
			|| (game.getCurrentTurnIndex() == getLastNonFoldedPlayerIndex(game)) || (game.getCurrentTurnIndex()+1 == game.getCurrentPlayerCount())
			) {
			game.setFirstTimeThroughRound(false);
			System.out.println("Last player in lobbies' turn.\n");
			System.out.format("game.getCurrentTurnIndex(): '%s'\n", game.getCurrentTurnIndex());
			System.out.format("getLastNonFoldedPlayerIndex(game): '%s'\n", getLastNonFoldedPlayerIndex(game));
			System.out.format("game.getCurrentTurn(): '%s'\n", game.getCurrentTurn());
			System.out.format("game.getCurrentTurnIndex()+1: '%d'\n", game.getCurrentTurnIndex()+1);
			System.out.format("game.getCurrentPlayerCount(): '%s'\n", game.getCurrentPlayerCount());
			
			// If checkAmount has been met by all players who aren't folded, proceed to next round.
			if (checkAmountMetByAllActivePlayers) {
				for (int i = 0; i < game.getCurrentPlayerCount(); i++) {
					appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
					AppUser tempAppUser = appUserSearch.get();
					
					// Assign currentTurn to the first non-folded player found.
					if (!tempAppUser.getFolded()) {
						System.out.format("game.getPlayers()[%d]: %s\n\n", i, game.getPlayers()[i]);
						game.setCurrentTurn(game.getPlayers()[i]);
						break;
					}
				}
				game.setCheckAmount(0.0);
				game.setFirstTimeThroughRound(true);
				
				// Reset amountBetThisRound for all active players.
				for (int i = 0; i < game.getPlayers().length; i++) {
					if (game.getPlayers()[i] == null) {
						break;
					}
					appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
					AppUser tempAppUser = appUserSearch.get();
					tempAppUser.setAmountBetThisRound(0.0);
					appUserRepository.save(tempAppUser);
				}
				
				// Advance to the next round and draw cards appropriate to current round.
				if (game.getCurrentRound().equals(START)) {
					game.setCurrentRound(FLOP);
					String[] tempBoard = {getRandomCard(), getRandomCard(), getRandomCard(), null, null};
					game.setBoard(tempBoard);
				} else if (game.getCurrentRound().equals(FLOP)) {
					game.setCurrentRound(TURN);
					String[] tempBoard = {game.getBoard()[0], game.getBoard()[1], game.getBoard()[2], getRandomCard(), null};
					game.setBoard(tempBoard);
				} else if (game.getCurrentRound().equals(TURN)) {
					String[] tempBoard = {game.getBoard()[0], game.getBoard()[1], game.getBoard()[2], game.getBoard()[3], getRandomCard()};
					game.setCurrentTurn(null);
					game.setGameStatus(FINISHED);
					game.setBoard(tempBoard);
					
					// TODO: Evaluate winner and add pot to balance(s).
					checkWinners(game);
					for (int i = 0; i < game.getWinners().length; i++) {
						if (game.getWinners()[i] == null) {
							break;
						} else {
							appUserSearch = appUserRepository.findByUsername(game.getWinners()[i].getUsername());
							AppUser tempAppUser = appUserSearch.get();
							tempAppUser.addToBalance(game.getPot()/game.getWinnerCount());
							appUserRepository.save(tempAppUser);
						}
					}
					
					// Reset appUser temp variables (folded, amountBetThisRound).
					for (int i = 0; i < game.getPlayers().length; i++) {
						if (game.getPlayers()[i] == null) {
							break;
						}
						
						appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
						AppUser tempAppUser = appUserSearch.get();
						tempAppUser.setAmountBetThisRound(0.0);
						tempAppUser.setFolded(false);
						appUserRepository.save(tempAppUser);
					}
				} else if (game.getCurrentRound().equals(RIVER)) {
					game.setCurrentTurn(null);
					game.setGameStatus(FINISHED);
				}
			// Otherwise, checkAmount hasn't been met by all players. Set currentTurn to the first active player who hasn't met the check amount.
			} else {
				// Iterate through all active players.
				for (int i = 0; i < game.getCurrentPlayerCount(); i++) {
					appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
					AppUser tempAppUser = appUserSearch.get();
					
					// If the user hasn't folded and they haven't met the check amount, set them as the next player.
					if ((!tempAppUser.getFolded()) && (game.getCheckAmount() != tempAppUser.getAmountBetThisRound())) {
						System.out.println("User: '" + tempAppUser.getUsername() + "' has not met the checkAmount.");
						System.out.format("game.getPlayers()[game.getPlayerIndexByUsername(tempAppUser.getUsername())]: '%s'\n\n", game.getPlayers()[game.getPlayerIndexByUsername(tempAppUser.getUsername())]);
						System.out.format("tempAppUser.getUsername(): '%s'\n", tempAppUser.getUsername());
						System.out.format("game.getPlayerIndexByUsername(tempAppUser.getUsername()): '%s'\n", game.getPlayerIndexByUsername(tempAppUser.getUsername()));
						if (!game.getFirstTimeThroughRound()) {
							game.setCurrentTurn(game.getPlayers()[game.getPlayerIndexByUsername(tempAppUser.getUsername())]);
						// If it's the last player in the lobbies' turn, or the last non-folded player in the lobby's turn, set currentTurn to first player who hasn't met the checkAmount.
						} else if ((game.getCurrentTurnIndex()+1 == game.getCurrentPlayerCount()) || (game.getCurrentTurnIndex() == getLastNonFoldedPlayerIndex(game))) {
							game.setCurrentTurn(game.getPlayers()[game.getPlayerIndexByUsername(tempAppUser.getUsername())]);
							game.setFirstTimeThroughRound(false);
						}
						checkAmountMetByAllActivePlayers = false;
						break;
					}
				}
			}
		// Otherwise, set the current turn to the next player in the lobby
		} else {
			// Iterate through remaining players in the lobby and check if they have folded.
			for (int i = game.getCurrentTurnIndex()+1; i < game.getCurrentPlayerCount(); i++) {
				appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
				AppUser tempAppUser = appUserSearch.get();
				
				// Assign currentTurn to the first non-folded player found.
				if (!tempAppUser.getFolded()) {
					System.out.format("game.getPlayers()[%d]: %s\n\n", i, game.getPlayers()[i]);
					game.setCurrentTurn(game.getPlayers()[i]);
					break;
				}
			}
		}
		
		GameStorage.getInstance().setGame(game);
		
		return game;
	}

	private Player[] checkWinners(Game game) {
		System.out.println("Running checkWinners()...");

		Optional<AppUser> appUserSearch;
		
		// Iterate through all active players.
		for (int i = 0; i < game.getCurrentPlayerCount(); i++) {
			appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
			AppUser appUser = appUserSearch.get();
			
			// If the user hasn't folded, check their hand.
			if (!appUser.getFolded()) {
				System.out.println("Checking hand for User: '" + appUser.getUsername() + "'");
				String[] cardsToCheck = {appUser.getCurrentHand()[0], appUser.getCurrentHand()[1], game.getBoard()[0], game.getBoard()[1], game.getBoard()[2], game.getBoard()[3], game.getBoard()[4]};
				int[] userSuites = new int[4];
				int[] userValues = new int[14];

				for (int j = 0; j < cardsToCheck.length; j++) {
					System.out.format(	"cardsToCheck[%d]: { suite: '%s' (index: '%d'), value: '%s' (index: '%d') }\n", j,
										cardsToCheck[j].split("_")[0], getSuiteIndexByString(cardsToCheck[j].split("_")[0]),
										cardsToCheck[j].split("_")[1], getValueIndexByString(cardsToCheck[j].split("_")[1]));
					userSuites[getSuiteIndexByString(cardsToCheck[j].split("_")[0])]++;
					userValues[getValueIndexByString(cardsToCheck[j].split("_")[1])]++;
					if (j == cardsToCheck.length-1) {
						System.out.println();
					}
				}
				
				for (int x = 0; x < userSuites.length; x++) {
					System.out.format("%s: '%d'\n", this.suites[x], userSuites[x]);
					// Check for flush.
					if (userSuites[x] >= 5) {
						System.out.println("FLUSH");
					}
				}
				
				int highCardIndex = 0;
				int firstPairIndex = -1;
				int secondPairIndex = -1;
				int thirdPairIndex = -1;
				int threeOfAKindIndex = -1;
				int pairCount = 0;
				int straightTracker = 0;
				Boolean threeOfAKind = false;
				for (int x = 0; x < userValues.length; x++) {
					System.out.format("%s: '%d'\n", this.values[x], userValues[x]);
					// Check for high card.
					if (userValues[x] > 0) {
						highCardIndex = x;
						straightTracker += 1;
					} else {
						straightTracker = 0;
					}
					
					// Check for pair.
					if (userValues[x] == 2) {
						if (firstPairIndex == -1) {
							firstPairIndex = x;
						} else if (secondPairIndex == -1){
							secondPairIndex = x;
						} else {
							thirdPairIndex = x;
						}
						pairCount++;
					// Check for three of a kind.
					} else if (userValues[x] == 3) {
						System.out.println("THREE_OF_A_KIND");
						threeOfAKind = true;
						threeOfAKindIndex = x;
						if (appUser.setHoldemWinType(THREE_OF_A_KIND)) {
							appUser.setHoldemWinString(String.format("threeOfAKind_%s", this.values[x]));
						}
					// Check for four of a kind.
					} else if (userValues[x] == 4) {
						System.out.println("FOUR_OF_A_KIND");
						if (appUser.setHoldemWinType(FOUR_OF_A_KIND)) {
							appUser.setHoldemWinString(String.format("fourOfAKind_%s", this.values[x]));
						}
					// Check for five of a kind.
					} else if (userValues[x] == 5) {
						System.out.println("FIVE_OF_A_KIND");
						if (appUser.setHoldemWinType(FIVE_OF_A_KIND)) {
							appUser.setHoldemWinString(String.format("fiveOfAKind_%s", this.values[x]));
						}
					}
					
					// Check for straight.
					if (straightTracker >= 5) {
						System.out.println("STRAIGHT");
						if (appUser.setHoldemWinType(STRAIGHT)) {
							appUser.setHoldemWinString(String.format("straight_%s", this.values[x]));
						}
					}
				}

				// Assign high card.
				System.out.format("HIGH_CARD: '%s'\n", this.values[highCardIndex]);
				if (appUser.setHoldemWinType(HIGH_CARD)) {
					appUser.setHoldemWinString(String.format("highCard_%s", this.values[highCardIndex]));
				}
				
				// Check for one pair.
				if (pairCount == 1) {
					System.out.println("PAIR");
					if (appUser.setHoldemWinType(PAIR)) {
						appUser.setHoldemWinString(String.format("pair_%s", this.values[firstPairIndex]));
					}
				// Check for two pair.
				} else if (pairCount == 2) {
					System.out.println("TWO_PAIR");
					if (appUser.setHoldemWinType(TWO_PAIR)) {
						appUser.setHoldemWinString(String.format("pair_%s-pair_%s", this.values[firstPairIndex], this.values[secondPairIndex]));
					}
				} else if (pairCount == 3) {
					System.out.println("TWO_PAIR");
					if (appUser.setHoldemWinType(TWO_PAIR)) {
						appUser.setHoldemWinString(String.format("pair_%s-pair_%s", this.values[secondPairIndex], this.values[thirdPairIndex]));
					}
				}
				
				// Check for full house.
				if ((pairCount == 1) && (threeOfAKind)) {
					System.out.println("FULL_HOUSE");
					if (appUser.setHoldemWinType(FULL_HOUSE)) {
						appUser.setHoldemWinString(String.format("pair_%s-threeOfAKind_%s", this.values[firstPairIndex], this.values[threeOfAKindIndex]));
					}
				}
				

				int[] flushValues = new int[14];
				
				// Iterate through userSuites and find the flush suite.
				for (int x = 0; x < userSuites.length; x++) {
					System.out.format("%s: '%d'\n", this.suites[x], userSuites[x]);

					// Check for flush.
					if (userSuites[x] >= 5) {
						System.out.println("FLUSH");

						for (int j = 0; j < cardsToCheck.length; j++) {
							if (getSuiteIndexByString(cardsToCheck[j].split("_")[0]) == x) {
								System.out.format(	"cardsToCheck[%d]: { suite: '%s' (index: '%d'), value: '%s' (index: '%d') }\n", j,
													cardsToCheck[j].split("_")[0], getSuiteIndexByString(cardsToCheck[j].split("_")[0]),
													cardsToCheck[j].split("_")[1], getValueIndexByString(cardsToCheck[j].split("_")[1]));
								flushValues[getValueIndexByString(cardsToCheck[j].split("_")[1])]++;
								if (j == cardsToCheck.length-1) {
									System.out.println();
								}
							}
						}
						
						straightTracker = 0;
						for (int j = 0; j < flushValues.length; j++) {
							// Check for high card.
							if ((straightTracker == 5) && flushValues[j] == 0) {
								// If high card isn't an ace, then it's a straight flush.
								if (highCardIndex != 13) {
									System.out.println("STRAIGHT_FLUSH");
									if (appUser.setHoldemWinType(STRAIGHT_FLUSH)) {
										appUser.setHoldemWinString(String.format("straightFlush_%s", this.values[highCardIndex]));
									}
								// Otherwise, the high card is an ace, so it's a royal flush
								} else {
									System.out.println("ROYAL_FLUSH");
									if (appUser.setHoldemWinType(ROYAL_FLUSH)) {
										appUser.setHoldemWinString(String.format("royalFlush_%s", this.values[highCardIndex]));
									}
								}
								break;
							} else if (flushValues[j] > 0) {
								highCardIndex = j;
								straightTracker += 1;
							} else {
								straightTracker = 0;
							}
						}
						// If no straight, then it's just a flush on the high card.
						if (straightTracker != 5 ) {
							System.out.println("FLUSH");
							if (appUser.setHoldemWinType(FLUSH)) {
								appUser.setHoldemWinString(String.format("flush_%s", this.values[highCardIndex]));
							}
						}
						
					}
				}
			}
			
			// Save user state.
			appUserRepository.save(appUser);
		}

		HoldemWinType bestWinType = null;
		// Iterate through all active players and add players with highest ranked win type to winners array.
		for (int i = 0; i < game.getCurrentPlayerCount(); i++) {
			appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
			AppUser appUser = appUserSearch.get();
			
			// If the user hasn't folded, check their hand.
			if (!appUser.getFolded()) {
				System.out.println("Checking HoldemWinType for User: '" + appUser.getUsername() + "'");
				System.out.format("appUser.getHoldemWinType(): '%s'\n", appUser.getHoldemWinType());
				System.out.format("appUser.getHoldemWinString(): '%s'\n", appUser.getHoldemWinString());
				System.out.format("appUser.getHoldemWinType().getValue(): '%d'\n", appUser.getHoldemWinType().getValue());
				if ((bestWinType == null) || (bestWinType.getValue() > appUser.getHoldemWinType().getValue())) {
					// Replace winners.
					game.resetWinners();
					game.addWinner(game.getPlayers()[game.getPlayerIndexByUsername(appUser.getUsername())]);
					bestWinType = appUser.getHoldemWinType();
				} else if (bestWinType.getValue() == appUser.getHoldemWinType().getValue()) {
					// Add to winners.
					game.addWinner(game.getPlayers()[game.getPlayerIndexByUsername(appUser.getUsername())]);
				}
				System.out.format("bestWinType.getValue(): '%d'\n\n", bestWinType.getValue());
			}
		}
		
		GameStorage.getInstance().setGame(game);

		// If there is no tie, return the winners array.
		if (game.getWinners()[1] == null) {
			appUserSearch = appUserRepository.findByUsername(game.getWinners()[0].getUsername());
			AppUser appUser = appUserSearch.get();
			System.out.format("Winner: '%s'\n", game.getWinners()[0].getUsername());
			System.out.format("appUser.getHoldemWinType(): '%s'\n", appUser.getHoldemWinType());
			System.out.format("appUser.getHoldemWinString(): '%s'\n\n", appUser.getHoldemWinString());
			return game.getWinners();
		} else {
			// Implement tie breaker logic/rules.
			Player[] tempWinners = new Player[8];
			appUserSearch = appUserRepository.findByUsername(game.getWinners()[0].getUsername());
			AppUser appUser = appUserSearch.get();
			
			// Resolve tie breakers for High Card, Pair, Three of a Kind, Straight, Flush, Four of a Kind, Straight Flush, Royal Flush, and Five of a Kind.
			if (	(appUser.getHoldemWinType() == HIGH_CARD) 
				||	(appUser.getHoldemWinType() == PAIR)
				||	(appUser.getHoldemWinType() == THREE_OF_A_KIND)
				||	(appUser.getHoldemWinType() == STRAIGHT)
				||  (appUser.getHoldemWinType() == ROYAL_FLUSH)
				||	(appUser.getHoldemWinType() == FOUR_OF_A_KIND)
				||  (appUser.getHoldemWinType() == STRAIGHT_FLUSH)
				||  (appUser.getHoldemWinType() == ROYAL_FLUSH)
				||	(appUser.getHoldemWinType() == FIVE_OF_A_KIND)) {
				tempWinners = this.getSingleHighCardTieBreaker(game.getWinners());
			// Resolve tie breakers for Two Pair and Full House.
			} else if ((appUser.getHoldemWinType() == TWO_PAIR) || (appUser.getHoldemWinType() == FULL_HOUSE)) {
				tempWinners = this.getDoubleHighCardTieBreaker(game.getWinners());
			}

			for (int i = 0; i < tempWinners.length; i++) {
				System.out.format("tempWinners[%d]: '%s'", i, tempWinners[i]);
			}
			
			game.setWinners(tempWinners);
		}
		

		GameStorage.getInstance().setGame(game);

		return game.getWinners();
	}
	
	private Player[] getSingleHighCardTieBreaker(Player[] winners) {
		Player[] tempWinners = new Player[8];
		int bestHighCardIndex = -1;
		// Iterate through all the winners and get player(s) with best high cards.
		for (int i = 0; i < winners.length; i++) {
			if (winners[i] == null) {
				break;
			}
			Optional<AppUser> appUserSearch = appUserRepository.findByUsername(winners[i].getUsername());
			AppUser appUser = appUserSearch.get();
					
			System.out.println("winners[" + Integer.toString(i) + "].getUsername(): '" + winners[i].getUsername() + "'");
			System.out.println("appUser.getHoldemWinType(): '" + appUser.getHoldemWinType() + "'");
			System.out.println("appUser.getHoldemWinString(): '" + appUser.getHoldemWinString() + "'\n");
			
			// Check if high card is better than best high card.
			if (this.getValueIndexByString(appUser.getHoldemWinString().split("_")[1]) > bestHighCardIndex) {
				tempWinners = this.resetTempWinners(tempWinners);
				tempWinners = this.addToTempWinners(tempWinners, winners[i]);
				bestHighCardIndex = this.getValueIndexByString(appUser.getHoldemWinString().split("_")[1]);
			} else if (this.getValueIndexByString(appUser.getHoldemWinString().split("_")[1]) == bestHighCardIndex) {
				tempWinners = this.addToTempWinners(tempWinners, winners[i]);
			}
		}
		return tempWinners;
	}
	
	private Player[] getDoubleHighCardTieBreaker(Player[] winners) {
		Player[] tempWinners = new Player[8];
		int bestHighCardIndex = -1;
		
		// Iterate through all the winners and get player(s) with best dominant high cards.
		for (int i = 0; i < winners.length; i++) {
			if (winners[i] == null) {
				break;
			}
			Optional<AppUser> appUserSearch = appUserRepository.findByUsername(winners[i].getUsername());
			AppUser appUser = appUserSearch.get();
					
			System.out.println("winners[" + Integer.toString(i) + "].getUsername(): '" + winners[i].getUsername() + "'");
			System.out.println("appUser.getHoldemWinType(): '" + appUser.getHoldemWinType() + "'");
			System.out.println("appUser.getHoldemWinString(): '" + appUser.getHoldemWinString() + "'\n");
			
			// Check if high card is better than best high card.
			if (this.getValueIndexByString(appUser.getHoldemWinString().split("-")[1].split("_")[1]) > bestHighCardIndex) {
				tempWinners = this.resetTempWinners(tempWinners);
				tempWinners = this.addToTempWinners(tempWinners, winners[i]);
				bestHighCardIndex = this.getValueIndexByString(appUser.getHoldemWinString().split("-")[1].split("_")[1]);
			} else if (this.getValueIndexByString(appUser.getHoldemWinString().split("-")[1].split("_")[1]) == bestHighCardIndex) {
				tempWinners = this.addToTempWinners(tempWinners, winners[i]);
			}
		}
		

		Player[] tempWinnersFinal = new Player[8];
		
		// If tie was resolved by dominant high card, return the winner array.
		if (tempWinners[1] == null) {
			return tempWinners;
		// Otherwise, check secondary high card.
		} else {
			bestHighCardIndex = -1;
			
			// Iterate through all the winners and get player(s) with best high cards.
			for (int i = 0; i < tempWinners.length; i++) {
				if (tempWinners[i] == null) {
					break;
				}
				Optional<AppUser> appUserSearch = appUserRepository.findByUsername(tempWinners[i].getUsername());
				AppUser appUser = appUserSearch.get();
						
				System.out.println("tempWinners[" + Integer.toString(i) + "].getUsername(): '" + tempWinners[i].getUsername() + "'");
				System.out.println("appUser.getHoldemWinType(): '" + appUser.getHoldemWinType() + "'");
				System.out.println("appUser.getHoldemWinString(): '" + appUser.getHoldemWinString() + "'\n");
				
				// Check if high card is better than best high card.
				if (this.getValueIndexByString(appUser.getHoldemWinString().split("-")[0].split("_")[1]) > bestHighCardIndex) {
					tempWinnersFinal = this.resetTempWinners(tempWinnersFinal);
					tempWinnersFinal = this.addToTempWinners(tempWinnersFinal, tempWinners[i]);
					bestHighCardIndex = this.getValueIndexByString(appUser.getHoldemWinString().split("-")[0].split("_")[1]);
				} else if (this.getValueIndexByString(appUser.getHoldemWinString().split("-")[0].split("_")[1]) == bestHighCardIndex) {
					tempWinnersFinal = this.addToTempWinners(tempWinnersFinal, tempWinners[i]);
				}
			}
		}
		
		return tempWinnersFinal;
	}
////if ((appUser.getHoldemWinType() == TWO_PAIR) || (appUser.getHoldemWinType() == FULL_HOUSE)) {
////// Check if high card is better than best high card.
////if (this.getValueIndexByString(appUser.getHoldemWinString().split("-")[1].split("_")[1]) > bestThreeOfAKindIndex) {
////	tempWinners = this.resetTempWinners(tempWinners);
////	tempWinners = this.addToTempWinners(tempWinners, game.getWinners()[i]);
////	bestHighCardIndex = this.getValueIndexByString(appUser.getHoldemWinString().split("_")[1]);
////} else if (this.getValueIndexByString(appUser.getHoldemWinString().split("-")[1].split("_")[1]) == bestThreeOfAKindIndex) {
////	tempWinners = this.addToTempWinners(tempWinners, game.getWinners()[i]);
////}
////}
	private Integer getSuiteIndexByString(String suite) {
		int i;
		for (i=0; i < this.suites.length; i++) {
			if (this.suites[i].equals(suite)) {
				break;
			}
		}
		return i;
	}

	
	private Integer getValueIndexByString(String value) {
		int i;
		for (i=0; i < this.values.length; i++) {
			if (this.values[i].equals(value)) {
				break;
			}
		}
		return i;
	}
	
	private Integer getNonFoldedPlayerCount(Game game) {
		Optional<AppUser> appUserSearch;
		Integer nonFoldedPlayerCount = 0;
		
		for (int i = 0; i < game.getPlayers().length; i++) {
			if (game.getPlayers()[i] == null) {
				break;
			}
			
			appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
			AppUser appUser = appUserSearch.get();
			
			if (!appUser.getFolded()) {
				nonFoldedPlayerCount++;
			}
		}
		
		return nonFoldedPlayerCount;
	}
	
	private Integer getFirstNonFoldedPlayerIndex(Game game) {
		Optional<AppUser> appUserSearch;
		int i;
		for (i = 0; i < game.getPlayers().length; i++) {
			if (game.getPlayers()[i] == null) {
				return null;
			}
			
			appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
			AppUser appUser = appUserSearch.get();
			
			if (!appUser.getFolded()) {
				break;
			}
		}
		
		return i;
	}
	
	private Integer getLastNonFoldedPlayerIndex(Game game) {
		Optional<AppUser> appUserSearch;
		int i;
		for (i = game.getPlayers().length-1; i >= 0 ; i--) {
			if (game.getPlayers()[i] == null) {
				continue;
			} else {
				appUserSearch = appUserRepository.findByUsername(game.getPlayers()[i].getUsername());
				AppUser appUser = appUserSearch.get();
				
				if (!appUser.getFolded()) {
					break;
				}
			}
		}
		
		return i;
	}
	
	public Double getCheckAmountByGameID(String gameId) {
		System.out.println("Running getCheckAmountByGameID()...");
		System.out.format("gameId: '%s'\n\n", gameId);
		Game game = GameStorage.getInstance().getGames().get(gameId);
		return game.getCheckAmount();
	}
	
	private Player[] resetTempWinners(Player[] tempWinners) {
		for (int tempWinnerIndex = 0; tempWinnerIndex < tempWinners.length; tempWinnerIndex++) {
			if (tempWinners[tempWinnerIndex] != null) {
				tempWinners[tempWinnerIndex] = null;
			} else {
				break;
			}
		}
		
		return tempWinners;
	}
	
	private Player[] addToTempWinners(Player[] tempWinners, Player player) {
		for (int tempWinnerIndex = 0; tempWinnerIndex < tempWinners.length; tempWinnerIndex++) {
			if (tempWinners[tempWinnerIndex] == null) {
				tempWinners[tempWinnerIndex] = player;
				break;
			}
		}
		
		return tempWinners;
	}
}
