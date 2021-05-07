package com.algorandex.registration;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@Controller
//@RestController
//@RequestMapping(path = "api/v1/registration")
//@RequestMapping(path = "registration")
@AllArgsConstructor
public class RegistrationController {
	
	private RegistrationService registrationService;

	@PostMapping(path = "/register", consumes = "application/json")
	public String register_json(@RequestBody RegistrationRequest request) {
		return registrationService.register(request);
	}
	
	@PostMapping(path = "/register", consumes = "application/x-www-form-urlencoded")
	public String register(RegistrationRequest request) {
		registrationService.register(request);
		return "confirmation-email-notification";
	}
	
	@GetMapping(path = "/login")
	public String login() {
		return "login";
	}
	
	@GetMapping(path = "/registration/confirm")
	public String confirm(@RequestParam("token") String token) {
		registrationService.confirmToken(token);

		return "email-confirmed";
	}
}
