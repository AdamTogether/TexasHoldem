package com.algorandex.email;

public interface EmailSender {
	void send(String recipient, String email_body);
}
