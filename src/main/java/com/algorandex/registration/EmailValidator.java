package com.algorandex.registration;

import java.util.function.Predicate;

import org.springframework.stereotype.Service;

@Service
public class EmailValidator implements Predicate<String> {
	@Override
	public boolean test(String s) {
		//!TODO Validate email.
		return true;
	}
}
