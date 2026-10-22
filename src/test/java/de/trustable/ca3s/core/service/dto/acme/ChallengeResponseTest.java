package de.trustable.ca3s.core.service.dto.acme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.service.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ChallengeResponseTest {

    @Test
    void getValidated() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String stringDate = "22:15:30, 10/09/2022";
        String pattern = "HH:mm:ss, M/d/uuuu";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.GERMANY);
        LocalDateTime localDateTime = LocalDateTime.parse(stringDate, dateTimeFormatter);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        Instant instant = zonedDateTime.toInstant();

        AcmeChallenge challengeDao = new AcmeChallenge();
        challengeDao.setValidated(instant);
        challengeDao.setToken("token");
        challengeDao.setChallengeId(12345L);
        challengeDao.setStatus(ChallengeStatus.PENDING);
        challengeDao.setId(1212L);
        challengeDao.setType("type");
        challengeDao.setValue("value");

        ChallengeResponse  challengeResponse = new ChallengeResponse(challengeDao, "http://url");
        assertEquals(challengeResponse.getStatus(), challengeDao.getStatus());
        assertEquals(challengeResponse.getType(), challengeDao.getType());
        assertEquals(challengeResponse.getToken(), challengeDao.getToken());
        assertEquals(challengeResponse.getValidated(), DateUtil.asDate(instant));

        ObjectWriter objectWriter = objectMapper.writerFor(ChallengeResponse.class);
        String jsonString =  objectWriter.writeValueAsString(challengeResponse);

        assertTrue(jsonString.endsWith("2022-10-09T20:15:30.000Z\"}"));

        ObjectReader objectReader = objectMapper.readerFor(ChallengeResponse.class);
        ChallengeResponse parsedResponse = objectReader.readValue(jsonString);

        assertEquals(challengeResponse.getStatus(), parsedResponse.getStatus());
        assertEquals(challengeResponse.getType(), parsedResponse.getType());
        assertEquals(challengeResponse.getToken(), parsedResponse.getToken());
        assertEquals(challengeResponse.getValidated(), parsedResponse.getValidated());

    }
}
