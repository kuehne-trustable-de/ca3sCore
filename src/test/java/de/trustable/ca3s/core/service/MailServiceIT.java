package de.trustable.ca3s.core.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.trustable.ca3s.core.IntegrationTest;
import de.trustable.ca3s.core.config.Constants;
import de.trustable.ca3s.core.domain.User;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

/**
 * Integration tests for {@link MailService}.
 */
@IntegrationTest
class MailServiceIT {

    private static final String[] languages = {
        "en",
        "de",
        "pl",
        // jhipster-needle-i18n-language-constant - JHipster will add/remove languages in this array
    };
    private static final Pattern PATTERN_LOCALE_3 = Pattern.compile("([a-z]{2})-([a-zA-Z]{4})-([a-z]{2})");
    private static final Pattern PATTERN_LOCALE_2 = Pattern.compile("([a-z]{2})-([a-z]{2})");

    @Autowired
    private JHipsterProperties jHipsterProperties;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Spy
    private JavaMailSenderImpl javaMailSender;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    private MailService mailService;
    private MailService mailServiceUseHTMLTitle;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        mailService = new MailService(jHipsterProperties,
            javaMailSender,
            messageSource,
            templateEngine,
            false,
            null);
        mailServiceUseHTMLTitle = new MailService(jHipsterProperties,
            javaMailSender,
            messageSource,
            templateEngine,
            true,
            null);
    }

    @Test
    void testSendEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", new String[]{}, "testSubject", "testContent", false, false);
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getSubject()).isEqualTo("testSubject");
        assertThat(message.getAllRecipients()[0]).hasToString("john.doe@example.com");
        assertThat(message.getFrom()[0]).hasToString(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent()).isInstanceOf(String.class);
        assertThat(message.getContent()).hasToString("testContent");
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/plain; charset=UTF-8");
    }

    @Test
    void testSendHtmlEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", new String[]{},"testSubject", "testContent", false, true);
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getSubject()).isEqualTo("testSubject");
        assertThat(message.getAllRecipients()[0]).hasToString("john.doe@example.com");
        assertThat(message.getFrom()[0]).hasToString(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent()).isInstanceOf(String.class);
        assertThat(message.getContent()).hasToString("testContent");
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    @Test
    void testSendMultipartEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", new String[]{},"testSubject", "testContent", true, false);
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        MimeMultipart mp = (MimeMultipart) message.getContent();
        MimeBodyPart part = (MimeBodyPart) ((MimeMultipart) mp.getBodyPart(0).getContent()).getBodyPart(0);
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        part.writeTo(aos);
        assertThat(message.getSubject()).isEqualTo("testSubject");
        assertThat(message.getAllRecipients()[0]).hasToString("john.doe@example.com");
        assertThat(message.getFrom()[0]).hasToString(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent()).isInstanceOf(Multipart.class);
        assertThat(aos).hasToString("\r\ntestContent");
        assertThat(part.getDataHandler().getContentType()).isEqualTo("text/plain; charset=UTF-8");
    }

    @Test
    void testSendMultipartHtmlEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", new String[]{},"testSubject", "testContent", true, true);
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        MimeMultipart mp = (MimeMultipart) message.getContent();
        MimeBodyPart part = (MimeBodyPart) ((MimeMultipart) mp.getBodyPart(0).getContent()).getBodyPart(0);
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        part.writeTo(aos);
        assertThat(message.getSubject()).isEqualTo("testSubject");
        assertThat(message.getAllRecipients()[0]).hasToString("john.doe@example.com");
        assertThat(message.getFrom()[0]).hasToString(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent()).isInstanceOf(Multipart.class);
        assertThat(aos).hasToString("\r\ntestContent");
        assertThat(part.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    @Test
    void testSendEmailFromTemplate() throws Exception {
        User user = new User();
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        user.setLangKey("en");
        mailService.sendEmailFromTemplate(user, "mail/testEmail", "email.test.title");
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getSubject()).isEqualTo("test title");
        assertThat(message.getAllRecipients()[0]).hasToString(user.getEmail());
        assertThat(message.getFrom()[0]).hasToString(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent().toString()).isEqualToNormalizingNewlines("<html>test title, http://127.0.0.1:8080, john</html>\n");
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    @Test
    void testSendActivationEmail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        mailService.sendActivationEmail(user, "activationKey");
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0]).hasToString(user.getEmail());
        assertThat(message.getFrom()[0]).hasToString(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent().toString()).isNotEmpty();
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    @Test
    void testCreationEmail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        mailService.sendCreationEmail(user);
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0]).hasToString(user.getEmail());
        assertThat(message.getFrom()[0]).hasToString(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent().toString()).isNotEmpty();
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    @Test
    void testSendPasswordResetMail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        mailService.sendPasswordResetMail(user, "resetKey");
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0]).hasToString(user.getEmail());
        assertThat(message.getFrom()[0]).hasToString(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent().toString()).isNotEmpty();
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    @Test
    void testSendEmailWithException() {
        doThrow(MailSendException.class).when(javaMailSender).send(any(MimeMessage.class));
        try {
            mailService.sendEmail("john.doe@example.com", new String[]{},"testSubject", "testContent", false, false);
            fail("Exception shouldn't have been thrown");
        } catch (MailSendException e) {
            // as expected ...
        } catch (Exception e) {
            fail("unexpected exception");
        }
    }

    @Test
    void testSendLocalizedEmailForAllSupportedLanguages() throws Exception {
        User user = new User();
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        for (String langKey : languages) {
            user.setLangKey(langKey);
            mailService.sendEmailFromTemplate(user, "mail/testEmail", "email.test.title");
            verify(javaMailSender, atLeastOnce()).send(messageCaptor.capture());
            MimeMessage message = messageCaptor.getValue();

            String propertyFilePath = "i18n/messages_" + getJavaLocale(langKey) + ".properties";
            URL resource = this.getClass().getClassLoader().getResource(propertyFilePath);
            File file = new File(new URI(resource.getFile()).getPath());
            Properties properties = new Properties();
            properties.load(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));

            String emailTitle = (String) properties.get("email.test.title");
            assertThat(message.getSubject()).isEqualTo(emailTitle);
            assertThat(message.getContent().toString())
                .isEqualToNormalizingNewlines("<html>" + emailTitle + ", http://127.0.0.1:8080, john</html>\n");
        }
    }

    /**
     * Convert a lang key to the Java locale.
     */
    private String getJavaLocale(String langKey) {
        String javaLangKey = langKey;
        Matcher matcher2 = PATTERN_LOCALE_2.matcher(langKey);
        if (matcher2.matches()) {
            javaLangKey = matcher2.group(1) + "_" + matcher2.group(2).toUpperCase();
        }
        Matcher matcher3 = PATTERN_LOCALE_3.matcher(langKey);
        if (matcher3.matches()) {
            javaLangKey = matcher3.group(1) + "_" + matcher3.group(2) + "_" + matcher3.group(3).toUpperCase();
        }
        return javaLangKey;
    }


    @Test
    void testSubjectRetrievalFromTitle (){
        String content = "<!DOCTYPE html> <html> <head> =20 <title>1 Certificate Expires Soon!</title> <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3DU= TF-8\" /> <link rel=3D\"shortcut icon\" href=3D\"https://ca3s.services.domain.org/favicon.ico\" /> <style> table th { font-weight: bold; } table th, table td { padding: 1px 2px; border: 1px solid #ddd; } table tr { background-color: #fff; border-top: 1px solid #ccc; } table tr:nth-child(2n) { background-color: #f8f8f8; } </style> </head> <body> <div> <p> =09=09<br/>  </p> </br> </div> =09=09<p> Hello <span></span> <span></span>, </p> =09=09<p> =09=09this is a reminder regarding expiring certificates: =09=09</p> <p> <h2>Expiring User Certificate</h2> <table> <tr> <th>remaining days</th> <th>valid until</th> <th>Subject</th> <th>SAN</th> =09=09=09=09=09<th>Serial</th> =20 </tr> <tr> <td>7</td> =09=09=09=09=09<td>04.12.2025, 12:36</td> <td>C=3DDE, CN=3Dsubject.domain.org, EMAILADDRESS=3Dsysmgmt@domain.org</td> <td>subject.domain.org</td> =09=09=09=09=09<td>23d8068be958a38cca5c179584257d43ef3d4</td> = =20 </tr> </table> </p> <div> <br/> <p>Greetings</p> <p>PKI team</p> </div> </body> </html>";
        String subject = mailServiceUseHTMLTitle.getSubject(content, "dummyTitleKey", new String[0], Locale.ROOT);
        Assertions.assertThat(subject).isEqualTo("1 Certificate Expires Soon!");

        try {
            mailService.getSubject(content, "dummyTitleKey", new String[0], Locale.ROOT);
            Assertions.fail("Should have thrown an exception");
        }catch( Exception e){
            // as expected
        }

        try {
            String noTitleContent = "<!DOCTYPE html> <html> <head> =20 <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3DU= TF-8\" /> ";
            mailService.getSubject(noTitleContent, "dummyTitleKey", new String[0], Locale.ROOT);
            Assertions.fail("Should have thrown an exception");
        }catch( Exception e){
            // as expected
        }

        String[] contentArr = {
            "<!DOCTYPE html> <html> <head> =20 <title >1 Certificate Expires Soon!</title> <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3DU= TF-8\" /> ",
            "<!DOCTYPE html> <html> <head> =20 <Title >1 Certificate Expires Soon!</title          > <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3DU= TF-8\" /> ",
            "<!DOCTYPE html> <html> <head> =20 <Title >1 Certificate Expires Soon!</title> <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3DU= TF-8\" /> ",
            "<!DOCTYPE html> <html> <head> =20 <Title >1 Certificate Expires Soon!</title> </title>  <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3DU= TF-8\" /> ",
            "<!DOCTYPE html> <html> <head> =20 <Title                        >1 Certificate Expires Soon!</title> <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3DU= TF-8\" /> "};
        for( String testContent: contentArr) {
            Assertions.assertThat(
                mailServiceUseHTMLTitle.getSubject(testContent, "dummyTitleKey", new String[0], Locale.ROOT)
            ).isEqualTo("1 Certificate Expires Soon!");
        }

    }
}
