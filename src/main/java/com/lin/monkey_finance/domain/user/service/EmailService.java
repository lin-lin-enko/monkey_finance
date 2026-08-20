package com.lin.monkey_finance.domain.user.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void sendConfirmationEmail(String toAddress, String token){
        String confirmationUrl = "http://localhost:8080/api/v1/auth/confirm?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@monkeyfinance.com");
        message.setTo(toAddress);
        message.setSubject("Registration confirmation | Monkey Finance");
        message.setText("To activate your account and use all the benefits of the Monkey Finance app please confirm your email via this link: " + confirmationUrl);

        mailSender.send(message);
    }
}
