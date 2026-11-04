package com.formaccess.mail;

/**
 * ABSTRACTION: the rest of the app only knows "send an email".
 * Which class actually does it is chosen by app.mail.mode in application.properties:
 *   console -> ConsoleEmailService (prints to the terminal, for development)
 *   smtp    -> SmtpEmailService    (really sends using JavaMailSender)
 */
public interface EmailService {
    void send(String to, String subject, String body);
}
