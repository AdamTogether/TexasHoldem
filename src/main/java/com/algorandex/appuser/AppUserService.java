package com.algorandex.appuser;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.algorandex.registration.token.ConfirmationToken;
import com.algorandex.registration.token.ConfirmationTokenService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

	private final static String USER_NOT_FOUND_MSG = "User with username %s not found.";
	private final AppUserRepository appUserRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final ConfirmationTokenService confirmationTokenService;
	
//	@Override
//	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//		return appUserRepository.findByEmail(email)
//				.orElseThrow(() -> 
//					new UsernameNotFoundException(
//							String.format(USER_NOT_FOUND_MSG, email)));
//	}
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return appUserRepository.findByUsername(username)
				.orElseThrow(() -> 
					new UsernameNotFoundException(
							String.format(USER_NOT_FOUND_MSG, username)));
	}

	public String signUpUser(AppUser appUser) {
		
		if (appUserRepository.findByEmail(appUser.getEmail()).isPresent()) {
			// TODO: If attributes are the same and email is not confirmed, send another confirmation email.
			throw new IllegalStateException("email already taken");
		} else if (appUserRepository.findByUsername(appUser.getUsername()).isPresent()) {
			throw new IllegalStateException("username already taken");
		}
		
		String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
		
		appUser.setPassword(encodedPassword);
		
		appUserRepository.save(appUser);

		String token = UUID.randomUUID().toString();
		
		ConfirmationToken confirmationToken = new ConfirmationToken(
				token,
				LocalDateTime.now(),
				LocalDateTime.now().plusMinutes(15),
				appUser
		);
		
		confirmationTokenService.saveConfirmationToken(confirmationToken);
		
		return token;
	}

	public int enableAppUser(String username) {
		return appUserRepository.enableAppUser(username);
	}
	
}
