package com.example.demowithtests.service.emailService;

import org.springframework.mail.MailException;

public interface EmailSenderService {

    void sendEmail(String toEmail, String subject, String body) throws MailException;
}
