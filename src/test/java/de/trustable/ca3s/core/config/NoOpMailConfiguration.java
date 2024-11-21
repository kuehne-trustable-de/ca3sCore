package de.trustable.ca3s.core.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import de.trustable.ca3s.core.service.MailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.MessagingException;

@Configuration
@ConditionalOnProperty(
    value="mailservice.mock.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class NoOpMailConfiguration {

    private final MailService mockMailService;
    public NoOpMailConfiguration() throws MessagingException {
        mockMailService = mock(MailService.class);
        doNothing().when(mockMailService).sendActivationEmail(any(), anyString());
    }

    @Bean
    public MailService mailService() {
        return mockMailService;
    }
}
