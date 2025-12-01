package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Service for sending emails.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private static final String ACTIVATION_KEY = "activationKey";

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;

    private final boolean useTitleAsMailSubject;

    private final String[] defaultBCC;

    public MailService(JHipsterProperties jHipsterProperties,
                       JavaMailSender javaMailSender,
                       MessageSource messageSource,
                       SpringTemplateEngine templateEngine,
                       @Value("${ca3s.template.email.useTitleAsMailSubject:false}") boolean useTitleAsMailSubject,
                       @Value("${ca3s.template.email.all.bcc:#{null}}") String[] defaultBCC) {

        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.useTitleAsMailSubject = useTitleAsMailSubject;
        this.defaultBCC = defaultBCC;
    }

    @Transactional
    public void sendEmail(String to, String[] cc, String subject, String content, boolean isMultipart, boolean isHtml) throws MessagingException {
        log.debug("Send email[multipart' '{}' and html '{}'] to '{}' (cc to '{}', bcc to '{}') with subject '{}' and content={}",
            isMultipart, isHtml, to, cc, defaultBCC, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
        message.setTo(to);
        if( cc != null) {
            message.setCc(cc);
        }

        if( defaultBCC != null ) {
            log.info( "Added general BCC email '{}'.", (Object) defaultBCC);
            message.setBcc(defaultBCC);
        }

        if(jHipsterProperties.getMail() != null &&
            jHipsterProperties.getMail().getFrom() != null &&
            !jHipsterProperties.getMail().getFrom().isEmpty()) {
            message.setFrom(jHipsterProperties.getMail().getFrom());
        }else{
            log.warn( "Email 'from' address not set for email delivery! Consider defining a useful sender address.");
            message.setFrom("ca3s@localhost");
        }
        message.setSubject(subject);
        message.setText(content, isHtml);
        javaMailSender.send(mimeMessage);
        log.debug("Sent email to User '{}'", to);
    }

    @Transactional
    public void sendEmailFromTemplate(Context context, User user, String email, String[] cc, String templateName, String titleKey) throws MessagingException {
        sendEmailFromTemplate(context, user, email, cc, templateName, titleKey, null);
    }

    @Transactional
    public void sendEmailFromTemplate(Context context, User user, String[] cc, String templateName, String titleKey) throws MessagingException {
        sendEmailFromTemplate(context, user, user.getEmail(),  cc, templateName, titleKey, null);
    }

    public void sendEmailFromTemplate(Context context, User user, String[] cc, String templateName, String titleKey, String[] args) throws MessagingException {
        sendEmailFromTemplate(context, user, user.getEmail(), cc, templateName, titleKey, args);
    }

    @Transactional
    public void sendEmailFromTemplate(Context context, User user, String email, String[] cc, String templateName, String titleKey, String[] args) throws MessagingException {

        if (user != null) {
            context.setVariable(USER, user);
        }

        if(jHipsterProperties.getMail() != null &&
            jHipsterProperties.getMail().getBaseUrl() != null &&
            !jHipsterProperties.getMail().getBaseUrl().isEmpty()) {
            context.setVariable(BASE_URL, getBaseUrl());
        }else{
            log.warn(BASE_URL + " not set for email templates");
            context.setVariable(BASE_URL, "/");
        }

        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());

        String content = getContent(templateName, context);
        String subject = getSubject(content, titleKey, args, context.getLocale());
        sendEmail(email, cc, subject, content, false, true);
    }

    @NotNull
    private String getBaseUrl() {
        return jHipsterProperties.getMail().getBaseUrl();
    }

    public void sendEmailFromTemplate(User user, String templateName, String titleKey ) throws MessagingException {
        sendEmailFromTemplate(user, templateName, titleKey, null);
    }
    @Transactional
    public void sendEmailFromTemplate(User user, String templateName,
                                      String titleKey,
                                      String activationKey) throws MessagingException {
        if (user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, getBaseUrl());
        if( activationKey != null && !activationKey.isEmpty()) {
            context.setVariable(ACTIVATION_KEY, activationKey);
        }
        String content = getContent(templateName, context);
        String subject = getSubject(content, titleKey, null, locale);
        sendEmail(user.getEmail(), null, subject, content, false, true);
    }

    String getSubject(final String content, final String titleKey, final String[] args, final Locale locale){

        if (useTitleAsMailSubject){
            String contentLowerCase = content.toLowerCase(Locale.ROOT);
            int contentLen = contentLowerCase.length();
            int startPosTitleTag = contentLowerCase.indexOf("<title");
            int posTitleStart = startPosTitleTag+6;
            for(; posTitleStart < contentLen; posTitleStart++ ){
                if(contentLowerCase.charAt(posTitleStart) == '>'){
                    posTitleStart++;
                    break;
                }
            }
            int endPosTitle = contentLowerCase.indexOf("</title");
            if( endPosTitle > 0 &&
                endPosTitle > posTitleStart){

                String templateTitle = content.substring(posTitleStart, endPosTitle);
                log.debug("Template title used as email subject '{}'", templateTitle);
                return templateTitle;
            }
        }
        String subject = messageSource.getMessage(titleKey, args, locale);
        log.debug("Title key used to find the email subject '{}'", subject);
        return subject;
    }

    private String getContent(String templateName, Context context){
        return templateEngine.process(templateName, context);
    }
    public void sendActivationEmail(User user, String activationKey) throws MessagingException {
        log.debug("Sending activation email to '{}'", user.getEmail());

        sendEmailFromTemplate(user,
            "mail/activationEmail",
            "email.activation.title",
            activationKey);
    }

    public void sendCreationEmail(User user) throws MessagingException {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmail", "email.activation.title", "");
    }

    public void sendPasswordResetMail(User user, String resetKey) throws MessagingException {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title", resetKey);
    }

}
