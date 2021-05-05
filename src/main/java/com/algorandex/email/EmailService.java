package com.algorandex.email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailService implements EmailSender {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
	private final JavaMailSender mailSender;

	// Queue may be more appropriate than async to retry sending emails at scale.
	@Override
	@Async
	public void send(String recipient, String email_body) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
			helper.setText(email_body, true);
			helper.setTo(recipient);
			helper.setSubject("Confirm your email");
			helper.setFrom("do-not-reply@texasholdem.com");
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			LOGGER.error("Failed to send email.", e);
			throw new IllegalStateException("Failed to send email.");
		}
	}
	
	
}