package com.formaccess.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Mock email: prints the email in the console. No email account needed. */
@Service
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "console", matchIfMissing = true)
public class ConsoleEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailService.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("""

                ======================= EMAIL (console mode) =======================
                To:      {}
                Subject: {}
                {}
                ====================================================================""", to, subject, body);
    }
}
