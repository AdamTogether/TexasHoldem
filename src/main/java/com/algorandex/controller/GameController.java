package com.algorandex.controller;

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
//import org.slf4j.LoggerFactory.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
	
	@PostMapping("/start")
	public ResponseEntity<Game> start(@RequestBody Player player) {
		log.info("start game request: {}", player);
		return ResponseEntity.ok(gameService.createGame(player));
	}
	
	@PostMapping("/connect")
	public ResponseEntity<Game> connect(@RequestBody ConnectRequest request) throws InvalidParamException, InvalidGameException {
		log.info("connect request: {}", request);
		Game game = gameService.connectToGame(request.getPlayer(), request.getGameId());
		simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
		return ResponseEntity.ok(game);
	}
	
	@PostMapping("/connect/random")
	public ResponseEntity<Game> connectRandom(@RequestBody Player player) throws NotFoundException {
		log.info("connect random: {}", player);
		Game game = gameService.connectToRandomGame(player);
		simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
		return ResponseEntity.ok(game);
	}
	
	@PostMapping("/gameplay")
	public ResponseEntity<Game> gamePlay(@RequestBody GamePlay request) throws NotFoundException, InvalidGameException {
		log.info("gameplay: {}", request);
		Game game = gameService.gamePlay(request);
		simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
		return ResponseEntity.ok(game);
	}
}
