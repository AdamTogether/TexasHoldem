package com.algorandex.controller;

import com.algorandex.appuser.AppUser;
import com.algorandex.appuser.AppUserRepository;
import com.algorandex.controller.dto.ConnectRequest;
//import com.algorandex.controller.dto.ConnectRequest;
import com.algorandex.exception.InvalidGameException;
import com.algorandex.exception.InvalidParamException;
import com.algorandex.exception.NotFoundException;
import com.algorandex.model.Game;
import com.algorandex.model.GamePlay;
import com.algorandex.model.Player;
import com.algorandex.service.GameService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

//import org.slf4j.LoggerFactory.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/game")
public class GameController {
	
	private final GameService gameService;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final AppUserRepository appUserRepository;
	

	@GetMapping(path = "/whoami")
	public String getDetails() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		Optional<AppUser> appUserSearch = appUserRepository.findByUsername((String) auth.getName());
		AppUser appUser = appUserSearch.get();
		
		String appUserDetails =	"First Name: " + appUser.getFirstName() +
								"</br>Last Name: " + appUser.getLastName() +
								"</br>Username: " + appUser.getUsername() +
								"</br>Email: " + appUser.getEmail() +
								"</br>Current Hand: [" + appUser.getCurrentHand()[0] + ", " + appUser.getCurrentHand()[1] + "]" +
								"</br>Balance: $" + appUser.getBalance();
		
		return appUserDetails;
	}
	
	@PostMapping("/create")
	public ResponseEntity<Game> create() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		Optional<AppUser> appUserSearch = appUserRepository.findByUsername((String) auth.getName());
		AppUser appUser = appUserSearch.get();
		Player player = new Player(appUser.getUsername());
		
		log.info("create game request: {}", player);
		return ResponseEntity.ok(gameService.createGame(player));
	}
	
	@PostMapping("/startGame")
	public ResponseEntity<Game> startGame(@RequestBody GamePlay request) throws NotFoundException, InvalidGameException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		Optional<AppUser> appUserSearch = appUserRepository.findByUsername((String) auth.getName());
		AppUser appUser = appUserSearch.get();
		Player player = new Player(appUser.getUsername());
		request.setPlayer(player);
		
		log.info("start game request: {}", player);
		Game game = gameService.startGame(request);
		simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
		return ResponseEntity.ok(game);
	}
	
	@PostMapping("/connect")
	public ResponseEntity<Game> connect(@RequestBody ConnectRequest request) throws InvalidParamException, InvalidGameException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		Optional<AppUser> appUserSearch = appUserRepository.findByUsername((String) auth.getName());
		AppUser appUser = appUserSearch.get();
		Player player = new Player(appUser.getUsername());
		
		log.info("connect request: {}", request);
		Game game = gameService.connectToGame(player, request.getGameId());
		simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
		return ResponseEntity.ok(game);
	}
	
	@PostMapping("/connect/random")
	public ResponseEntity<Game> connectRandom() throws NotFoundException, InvalidGameException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		Optional<AppUser> appUserSearch = appUserRepository.findByUsername((String) auth.getName());
		AppUser appUser = appUserSearch.get();
		Player player = new Player(appUser.getUsername());
		
		log.info("connect random: {}", player);
		Game game = gameService.connectToRandomGame(player);
		simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
		return ResponseEntity.ok(game);
	}
	
	@PostMapping("/gameplay")
	public ResponseEntity<Game> gamePlay(@RequestBody GamePlay request) throws NotFoundException, InvalidGameException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		Optional<AppUser> appUserSearch = appUserRepository.findByUsername((String) auth.getName());
		AppUser appUser = appUserSearch.get();
		Player player = new Player(appUser.getUsername());
		request.setPlayer(player);
		
		log.info("gameplay: {}", request);
		Game game = gameService.gamePlay(request);
		simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
		return ResponseEntity.ok(game);
	}
}
